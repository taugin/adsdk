package com.hauyu.adsdk.stat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.data.config.AdSwitch;
import com.hauyu.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class EventImpl implements IEvent {

    private static EventImpl sEventImpl;

    private Object mFacebookObject = null;

    public static EventImpl get() {
        synchronized (EventImpl.class) {
            if (sEventImpl == null) {
                createInstance();
            }
        }
        return sEventImpl;
    }

    private static void createInstance() {
        synchronized (EventImpl.class) {
            if (sEventImpl == null) {
                sEventImpl = new EventImpl();
            }
        }
    }

    private EventImpl() {
    }

    public void init() {
    }

    private String generateEventIdAlias(Context context, String eventId) {
        return eventId;
    }

    private boolean checkArgument(Context context, String pidName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(pidName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.iv(Log.TAG, "context == null or pidname == null or sdk == null or type all must not be empty or null");
            return false;
        }
        return true;
    }

    private String generateEventId(Context context, String action, String sdk, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append("_");
        builder.append(type);
        builder.append("_");
        builder.append(sdk);
        return generateEventIdAlias(context, builder.toString());
    }

    /**
     * 发送Firebase统计事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendFirebaseAnalytics(Context context, String value, String eventId, Map<String, String> extra) {
        Log.iv(Log.TAG, "Report Event Firebase Analytics");
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        } else {
            bundle.putString("entry_point", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        bundle.putString(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        String error = null;
        try {
            Class<?> clazz = Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            Method method = clazz.getMethod("getInstance", Context.class);
            Object instance = method.invoke(null, context);
            method = clazz.getMethod("logEvent", String.class, Bundle.class);
            method.invoke(instance, eventId, bundle);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event Firebase error : " + error);
        }
    }

    /**
     * 发送友盟计数事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendUmeng(Context context, String value, String eventId, Map<String, String> extra) {
        Log.iv(Log.TAG, "Report Event sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        } else {
            map.put("entry_point", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEvent", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event sendUmeng error : " + error);
        }
    }

    /**
     * 发送友盟计算事件
     *
     * @param context
     * @param eventId
     * @param extra
     * @param value
     */
    private void sendUmengEventValue(Context context, String eventId, Map<String, String> extra, int value) {
        if (!isReportUmeng(context)) {
            return;
        }
        Log.iv(Log.TAG, "Report Event sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventValue", Context.class, String.class, Map.class, int.class);
            method.invoke(null, context, eventId, map, value);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event sendUmengEventValue error : " + error);
        }
    }

    private void initFacebook(Context context) {
        if (mFacebookObject != null) {
            return;
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.facebook.appevents.AppEventsLogger");
            Method method = clazz.getMethod("newLogger", Context.class);
            mFacebookObject = method.invoke(null, context);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event initFacebook error : " + error);
        }
    }

    private void sendFacebook(Context context, String value, String eventId, Map<String, String> extra) {
        initFacebook(context);
        if (mFacebookObject == null) {
            return;
        }
        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        } else {
            bundle.putString("entry_point", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        bundle.putString(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        String error = null;
        try {
            Class<?> clazz = Class.forName("com.facebook.appevents.AppEventsLogger");
            Method method = clazz.getMethod("logEvent", String.class, Bundle.class);
            method.invoke(mFacebookObject, eventId, bundle);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event sendFacebook error : " + error);
        }
    }

    @Override
    public void reportAdRequest(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "request", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "loaded", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        // sendAppsflyer(context, pidName, eventId, extra);
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdCallShow(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "callshow", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdShow(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "show", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdClick(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "click", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdReward(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "receive", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdError(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!isReportError(context)) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId(context, "error", sdk, type);
        extra = addExtraForError(context, extra);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdClose(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "close", sdk, type);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, extra);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdLoadSuccessTime(Context context, String sdk, String type, int value) {
        if (!isReportTime(context)) {
            return;
        }
        String eventId = "load_ad_success_time";
        eventId = generateEventIdAlias(context, eventId);
        if (!checkArgument(context, eventId, sdk, type)) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("sdk", sdk);
        map.put("type", type);
        if (isReportUmeng(context)) {
            sendUmengEventValue(context, eventId, map, value);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, null, eventId, map);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , value : " + value);
    }

    @Override
    public void reportAdLoadFailureTime(Context context, String sdk, String type, String error, int value) {
        if (!isReportTime(context)) {
            return;
        }
        if (!checkArgument(context, error, sdk, type)) {
            return;
        }
        String eventId = "load_ad_failure_time";
        eventId = generateEventIdAlias(context, eventId);
        Map<String, String> map = new HashMap<String, String>();
        map.put("sdk", sdk);
        map.put("type", type);
        map.put("error", error);
        if (isReportUmeng(context)) {
            sendUmengEventValue(context, eventId, map, value);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, null, eventId, map);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , error : " + error + " , value : " + value);
    }

    @Override
    public void reportKVEvent(Context context, String key, String value) {
        if (context == null) {
            return;
        }
        String eventId = generateEventIdAlias(context, key);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, value, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, value, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, value, eventId, null);
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + value);
    }

    @Override
    public void reportAdPlaceSeqRequest(Context context, String pidName) {
    }

    @Override
    public void reportAdPlaceSeqLoaded(Context context, String pidName) {
    }

    @Override
    public void reportAdPlaceSeqError(Context context, String pidName) {
    }

    private boolean isReportError(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isReportError();
        }
        return true;
    }

    private boolean isReportTime(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isReportTime();
        }
        return true;
    }

    private boolean isReportUmeng(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isReportUmeng();
        }
        return true;
    }

    private boolean isReportFirebase(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isReportFirebase();
        }
        return true;
    }

    private boolean isReportFacebook(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isReportFacebook();
        }
        return true;
    }

    private Map<String, String> addExtraForError(Context context, Map<String, String> extra) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            StringBuilder builder = new StringBuilder();
            boolean isConnected = false;
            String networkType = null;
            String subworkType = null;
            if (networkInfo != null) {
                isConnected = networkInfo.isAvailable();
                networkType = networkInfo.getTypeName();
                subworkType = networkInfo.getSubtypeName();
            }
            builder.append(isConnected ? "Y" : "N");
            if (!TextUtils.isEmpty(networkType)) {
                builder.append("[");
                builder.append(networkType);
                if (!TextUtils.isEmpty(subworkType)) {
                    builder.append("-");
                    builder.append(subworkType);
                }
                builder.append("]");
            }
            if (extra == null) {
                extra = new HashMap<String, String>();
            }
            extra.put("network", builder.toString());
        } catch (Exception e) {
        } catch (Error e) {
        }
        return extra;
    }
}