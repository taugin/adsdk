package com.komob.adsdk;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.komob.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-19.
 */

public class InternalStat {

    private static Object mFacebookObject = null;
    private static final String SDK_NAME_FIREBASE = "firebase";
    private static final String SDK_NAME_APPSFLYER = "appsflyer";
    private static final String SDK_NAME_FACEBOOK = "facebook";
    private static final Map<String, Boolean> sSdkIntegrated;

    static {
        sSdkIntegrated = new HashMap<>();
        boolean sdkIntegrated;
        try {
            Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            sdkIntegrated = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, SDK_NAME_FIREBASE + " init error : " + e);
            sdkIntegrated = false;
        }
        sSdkIntegrated.put(SDK_NAME_FIREBASE, sdkIntegrated);

        try {
            Class.forName("com.appsflyer.AppsFlyerLib");
            sdkIntegrated = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, SDK_NAME_APPSFLYER + " init error : " + e);
            sdkIntegrated = false;
        }
        sSdkIntegrated.put(SDK_NAME_APPSFLYER, sdkIntegrated);

        try {
            Class.forName("com.facebook.appevents.AppEventsLogger");
            sdkIntegrated = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, SDK_NAME_FACEBOOK + " init error : " + e);
            sdkIntegrated = false;
        }
        sSdkIntegrated.put(SDK_NAME_FACEBOOK, sdkIntegrated);
    }

    /**
     * Map转Bundle
     *
     * @param map
     * @param bundle
     */
    private static void mapToBundle(Map<String, Object> map, Bundle bundle) {
        if (map == null || bundle == null) {
            return;
        }
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key)) {
                        if (valueObj == null) {
                            bundle.putString(key, null);
                        } else if (valueObj instanceof Integer) {
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
                        } else if (valueObj instanceof boolean[]) {
                            bundle.putBooleanArray(key, (boolean[]) valueObj);
                        } else if (valueObj instanceof int[]) {
                            bundle.putIntArray(key, (int[]) valueObj);
                        } else if (valueObj instanceof long[]) {
                            bundle.putLongArray(key, (long[]) valueObj);
                        } else if (valueObj instanceof double[]) {
                            bundle.putDoubleArray(key, (double[]) valueObj);
                        } else if (valueObj instanceof String[]) {
                            bundle.putStringArray(key, (String[]) valueObj);
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
        sendFirebaseAnalytics(context, eventId, value, extra, true);
    }

    public static void sendFirebaseAnalytics(Context context, String eventId, String value, Map<String, Object> extra, boolean allowReport) {
        String platform = SDK_NAME_FIREBASE;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        }
        mapToBundle(extra, bundle);
        Log.iv(Log.TAG_SDK, "[" + platform + "] event id : " + eventId + " , value : " + bundle);
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
            Log.iv(Log.TAG_SDK, "error : " + error);
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
            Log.iv(Log.TAG_SDK, "facebook new logger error : " + error);
        }
    }

    public static void sendFacebook(Context context, String eventId, String value, Map<String, Object> extra) {
        sendFacebook(context, eventId, value, extra, true);
    }

    public static void sendFacebook(Context context, String eventId, String value, Map<String, Object> extra, boolean allowReport) {
        String platform = SDK_NAME_FACEBOOK;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
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
        Log.iv(Log.TAG, "[" + platform + "] event id : " + eventId + " , value : " + bundle);

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
            Log.iv(Log.TAG_SDK, "send " + platform + " error : " + error);
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
        sendAppsflyer(context, eventId, value, extra, true);
    }

    public static void sendAppsflyer(Context context, String eventId, String value, Map<String, Object> extra, boolean allowReport) {
        String platform = SDK_NAME_APPSFLYER;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put("event_id", eventId);
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
        Log.iv(Log.TAG_SDK, "[" + platform + "] event id : " + eventId + " , value : " + eventValue);
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
            Log.iv(Log.TAG_SDK, "send " + platform + " error : " + error);
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
        Log.iv(Log.TAG, "event id : " + key + " , value : " + value + " , extra : " + map);
        sendAppsflyer(context, key, value, map, false);
        sendFirebaseAnalytics(context, key, value, map, isInFirebaseWhiteList(key));
    }

    public static boolean isInFirebaseWhiteList(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        return true;
    }

    private static boolean isReportPlatform(Context context, String eventId, String platform, boolean allowReport) {
        boolean finalResult = false;
        try {
            Boolean sdkIntegrated = sSdkIntegrated.get(platform);
            finalResult = sdkIntegrated != null && sdkIntegrated.booleanValue() && allowReport;
            // Log.iv(Log.TAG_SDK, "[" + eventId + "] report " + platform + " : " + finalResult);
        } catch (Exception e) {
        }
        return finalResult;
    }
}
