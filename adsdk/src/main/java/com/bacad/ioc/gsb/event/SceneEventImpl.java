package com.bacad.ioc.gsb.event;

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

public class SceneEventImpl implements IEvent {

    private static SceneEventImpl sSceneEventImpl;

    private Object mFacebookObject = null;

    public static SceneEventImpl get() {
        synchronized (SceneEventImpl.class) {
            if (sSceneEventImpl == null) {
                createInstance();
            }
        }
        return sSceneEventImpl;
    }

    private static void createInstance() {
        synchronized (SceneEventImpl.class) {
            if (sSceneEventImpl == null) {
                sSceneEventImpl = new SceneEventImpl();
            }
        }
    }

    private SceneEventImpl() {
    }

    public void init() {
    }

    private String generateEventIdAlias(Context context, String eventId) {
        return eventId;
    }

    private boolean checkArgument(Context context, String pidName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(pidName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.e(Log.TAG, "context == null or pidname == null or sdk == null or type all must not be empty or null");
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
        Log.d(Log.TAG, "SceneEventImpl Firebase Analytics");
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
            Log.v(Log.TAG, "SceneEventImpl Firebase error : " + error);
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
        Log.d(Log.TAG, "SceneEventImpl sendUmeng Analytics");
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
            Log.v(Log.TAG, "SceneEventImpl sendUmeng error : " + error);
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
        Log.d(Log.TAG, "SceneEventImpl sendUmeng Analytics");
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
            Log.v(Log.TAG, "SceneEventImpl sendUmengEventValue error : " + error);
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
            Log.v(Log.TAG, "SceneEventImpl initFacebook error : " + error);
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
            Log.v(Log.TAG, "SceneEventImpl sendFacebook error : " + error);
        }
    }

    private String generateAdOuterKey(String adOuterType, String op) {
        return "outer_" + adOuterType + "_" + op;
    }

    @Override
    public void reportAdOuterRequest(Context context, String adOuterType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "request");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterLoaded(Context context, String adOuterType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "loaded");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterCallShow(Context context, String adOuterType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "callshow");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterShowing(Context context, String adOuterType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "showing");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterDisallow(Context context, String adOuterType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "disallow");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterShowTimes(Context context, String adOuterType, int times) {
        if (context == null) {
            return;
        }
        String eventId = generateAdOuterKey(adOuterType, "showtimes");
        eventId = generateEventIdAlias(context, eventId);
        String value = String.valueOf(times);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, value, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, value, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, value, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId + ", times : " + times);
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