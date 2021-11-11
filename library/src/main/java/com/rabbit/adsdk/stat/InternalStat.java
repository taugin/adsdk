package com.rabbit.adsdk.stat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.rabbit.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-19.
 */

public class InternalStat {
    private static Object mFacebookObject = null;

    private static void mapToBundle(Map<String, Object> map, Bundle bundle) {
        if (map == null || bundle == null) {
            return;
        }
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key) && valueObj != null) {
                        if (valueObj instanceof Integer) {
                            bundle.putInt(key, ((Integer) valueObj).intValue());
                        } else if (valueObj instanceof Float) {
                            bundle.putFloat(key, ((Float) valueObj).floatValue());
                        } else if (valueObj instanceof Double) {
                            bundle.putDouble(key, ((Double) valueObj).doubleValue());
                        } else if (valueObj instanceof Boolean) {
                            bundle.putBoolean(key, ((Boolean) valueObj).booleanValue());
                        } else if (valueObj instanceof Byte) {
                            bundle.putByte(key, ((Byte) valueObj).byteValue());
                        } else if (valueObj instanceof Short) {
                            bundle.putShort(key, ((Short) valueObj).shortValue());
                        } else if (valueObj instanceof Long) {
                            bundle.putLong(key, ((Long) valueObj).longValue());
                        } else if (valueObj instanceof String) {
                            bundle.putString(key, valueObj.toString());
                        } else {
                            bundle.putString(key, String.valueOf(valueObj));
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送Firebase统计事件
     *
     * @param context
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendFirebaseAnalytics(Context context, String eventId, String value, Map<String, Object> extra) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        } else {
            bundle.putString("entry_point", eventId);
        }
        mapToBundle(extra, bundle);
        Log.iv(Log.TAG, "firebase event id : " + eventId + " , value : " + bundle);
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
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendUmeng(Context context, String eventId, String value, Map<String, Object> extra) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        } else {
            map.put("entry_point", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, Object> entry : extra.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key) && valueObj != null) {
                        if (valueObj instanceof String) {
                            map.put(entry.getKey(), valueObj.toString());
                        } else {
                            map.put(entry.getKey(), String.valueOf(valueObj));
                        }
                    }
                }
            }
        }
        Log.iv(Log.TAG, "umeng event id : " + eventId + " , value : " + map);
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
     * 发送友盟计算事件
     *
     * @param context
     * @param eventId
     * @param extra
     * @param value
     */
    public static void sendUmengEventValue(Context context, String eventId, Map<String, Object> extra, int value) {
        Log.iv(Log.TAG, "Report Event sendUmeng Event Value Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, Object> entry : extra.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key) && valueObj != null) {
                        if (valueObj instanceof String) {
                            map.put(entry.getKey(), valueObj.toString());
                        } else {
                            map.put(entry.getKey(), String.valueOf(valueObj));
                        }
                    }
                }
            }
        }
        Log.iv(Log.TAG, "umeng event id : " + eventId + " , value : " + map);
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

    private static void sendUmengError(Context context, Throwable throwable) {
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("reportError", Context.class, Throwable.class);
            method.invoke(null, context, throwable);
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
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendAppsflyer(Context context, String eventId, String value, Map<String, Object> extra) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("entry_point", value);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, Object> entry : extra.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key) && valueObj != null) {
                        eventValue.put(key, valueObj);
                    }
                }
            }
        }
        Log.iv(Log.TAG, "appsflyer event id : " + eventId + " , value : " + eventValue);
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

    /**
     *
     * @param context
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendFacebook(Context context, String eventId, String value, Map<String, Object> extra) {
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
        mapToBundle(extra, bundle);
        Log.iv(Log.TAG, "facebook event id : " + eventId + " , value : " + bundle);

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

    /**
     * 发送flurry统计事件
     *
     * @param context
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendFlurry(Context context, String eventId, String value, Map<String, Object> extra) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("entry_point", value);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, Object> entry : extra.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key) && valueObj != null) {
                        eventValue.put(key, valueObj);
                    }
                }
            }
        }
        Log.iv(Log.TAG, "flurry event id : " + eventId + " , value : " + eventValue);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.flurry.android.FlurryAgent");
            Method method = clazz.getDeclaredMethod("logEvent", String.class, Map.class);
            method.invoke(null, eventId, eventValue);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "Report Event sendFlurry error : " + error);
        }
    }

    public static void reportEvent(Context context, String key) {
        reportEvent(context, key, null, null);
    }

    public static void reportEvent(Context context, String key, Map<String, Object> map) {
        reportEvent(context, key, null, map);
    }

    public static void reportEvent(Context context, String key, String value) {
        reportEvent(context, key, value, null);
    }

    public static void reportEvent(Context context, String key, String value, Map<String, Object> map) {
        sendUmeng(context, key, value, map);
        sendAppsflyer(context, key, value, map);
        sendFirebaseAnalytics(context, key, value, map);
        sendFacebook(context, key, value, map);
        sendFlurry(context, key, value, map);
    }

    public static void reportError(Context context, Throwable e) {
        sendUmengError(context, e);
    }
}
