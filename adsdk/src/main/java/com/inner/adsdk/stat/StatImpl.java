package com.inner.adsdk.stat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class StatImpl implements IStat {

    private static StatImpl sStatImpl;

    public static StatImpl get() {
        synchronized (StatImpl.class) {
            if (sStatImpl == null) {
                createInstance();
            }
        }
        return sStatImpl;
    }

    private static void createInstance() {
        synchronized (StatImpl.class) {
            if (sStatImpl == null) {
                sStatImpl = new StatImpl();
            }
        }
    }

    private StatImpl() {
    }

    public void init() {
    }

    private boolean checkArgument(Context context, String pidName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(pidName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.e(Log.TAG, "context or pidname or sdk or type all must not be empty or null");
            return false;
        }
        return true;
    }

    private String generateEventId(String action, String sdk, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append("_");
        builder.append(type);
        builder.append("_");
        builder.append(sdk);
        return builder.toString();
    }

    /**
     * 发送Firebase统计事件
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendFirebaseAnalytics(Context context, String value, String eventId, Map<String, String> extra) {
        Log.d(Log.TAG, "StatImpl Firebase Analytics");
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
            Log.v(Log.TAG, "StatImpl Firebase error : " + error);
        }
    }

    /**
     * 发送友盟计数事件
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendUmeng(Context context, String value, String eventId, Map<String, String> extra) {
        Log.d(Log.TAG, "StatImpl sendUmeng Analytics");
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
            Log.v(Log.TAG, "StatImpl sendUmeng error : " + error);
        }
    }

    /**
     * 发送友盟计算事件
     * @param context
     * @param eventId
     * @param extra
     * @param value
     */
    private void sendUmengEventValue(Context context, String eventId, Map<String, String> extra, int value) {
        Log.d(Log.TAG, "StatImpl sendUmeng Analytics");
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
            Log.v(Log.TAG, "StatImpl sendUmengEventValue error : " + error);
        }
    }

    /**
     * 发送appsflyer统计事件
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendAppsflyer(Context context, String value, String eventId, Map<String, String> extra) {
        Log.d(Log.TAG, "StatImpl sendAppsflyer Analytics");
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("entry_point", value);
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.appsflyer.AppsFlyerLib");
            Method method = clazz.getMethod("getInstance");
            Object instance = method.invoke(null);
            method = clazz.getMethod("trackEvent", Context.class, String.class, Map.class);
            method.invoke(instance, context, eventId, eventValue);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.v(Log.TAG, "StatImpl sendAppsflyer error : " + error);
        }
    }

    @Override
    public void reportAdRequest(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("request", sdk, type);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("loaded", sdk, type);
        sendFirebaseAnalytics(context, pidName, eventId, extra);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("show", sdk, type);
        sendFirebaseAnalytics(context, pidName, eventId, extra);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdClick(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("click", sdk, type);
        sendFirebaseAnalytics(context, pidName, eventId, extra);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdError(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!isReportError(context)) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId("error", sdk, type);
        sendFirebaseAnalytics(context, pidName, eventId, extra);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdOuterRequest(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_request";
        sendFirebaseAnalytics(context, null, eventId, null);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterLoaded(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_loaded";
        sendFirebaseAnalytics(context, null, eventId, null);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterShow(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_show";
        sendFirebaseAnalytics(context, null, eventId, null);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterShowing(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_showing";
        sendFirebaseAnalytics(context, null, eventId, null);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId);
    }

    @Override
    public void reportAdOuterShowTimes(Context context, int times) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_showtimes";
        String value = String.valueOf(times);
        sendFirebaseAnalytics(context, value, eventId, null);
        sendUmeng(context, value, eventId, null);
        Log.v(Log.TAG, "StatImpl stat key : outer_gt_showtimes , times : " + times);
    }

    @Override
    public void reportAdLoadSuccessTime(Context context, String sdk, String type, int value) {
        if (!isReportTime(context)) {
            return;
        }
        String eventId = "load_ad_success_time";
        if (!checkArgument(context, eventId, sdk, type)) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("sdk", sdk);
        map.put("type", type);
        sendUmengEventValue(context, eventId, map, value);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , value : " + value);
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("sdk", sdk);
        map.put("type", type);
        map.put("error", error);
        sendUmengEventValue(context, eventId, map, value);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , error : " + error + " , value : " + value);
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
}