package com.hauyu.adsdk.stat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.hauyu.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-19.
 */

public class InternalStat {
    private static Object mFacebookObject = null;

    /**
     * 发送Firebase统计事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private static void sendFirebaseAnalytics(Context context, String value, String eventId, Map<String, String> extra) {
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
            Log.iv(Log.TAG, "error : " + error);
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
    private static void sendUmeng(Context context, String value, String eventId, Map<String, String> extra) {
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
            Log.iv(Log.TAG, "error : " + error);
        }
    }

    /**
     * 发送appsflyer统计事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private static void sendAppsflyer(Context context, String value, String eventId, Map<String, String> extra) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("entry_point", value);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        eventValue.put(entry.getKey(), entry.getValue());
                    }
                }
            }
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
            Log.iv(Log.TAG, "error : " + error);
        }
    }

    private static void initFacebook(Context context) {
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
            Log.iv(Log.TAG, "error : " + error);
        }
    }

    private static void sendFacebook(Context context, String value, String eventId, Map<String, String> extra) {
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
            Log.iv(Log.TAG, "error : " + error);
        }
    }

    public static void reportEvent(Context context, String key) {
        reportEvent(context, key, null, null);
    }

    public static void reportEvent(Context context, String key, Map<String, String> map) {
        reportEvent(context, key, null, map);
    }

    public static void reportEvent(Context context, String key, String value) {
        reportEvent(context, key, value, null);
    }

    public static void reportEvent(Context context, String key, String value, Map<String, String> map) {
        sendUmeng(context, value, key, map);
        sendAppsflyer(context, value, key, map);
        sendFirebaseAnalytics(context, value, key, map);
        sendFacebook(context, value, key, map);
    }
}
