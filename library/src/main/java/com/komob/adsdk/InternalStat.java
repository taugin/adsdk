package com.komob.adsdk;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-19.
 */

public class InternalStat {

    private static final String SDK_NAME_FIREBASE = "firebase";
    private static final List<String> sFirebaseWhiteList;

    private static final Map<String, Boolean> sSdkIntegrated;

    static {
        sFirebaseWhiteList = Arrays.asList(Constant.AD_IMPRESSION, Constant.AD_IMPRESSION_REVENUE, "app_first_open_ano", "app_first_open_ao", "ad_spread_installed", "Total_Ads_Revenue_*", "gav_*");

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

    public static void sendFirebaseAnalytics(Context context, String eventId, String value, Map<String, Object> extra, boolean enable) {
        if (!enable) {
            return;
        }
        String platform = SDK_NAME_FIREBASE;
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        }
        mapToBundle(extra, bundle);
        if (bundle != null && bundle.size() > 25) {
            try {
                bundle.remove(Constant.AD_PLACEMENT_NEW);
                bundle.remove(Constant.AD_ROUND_CPM_NEW);
            } catch (Exception e) {
            }
        }
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + bundle);
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
        sendFirebaseAnalytics(context, key, value, map, isInFirebaseWhiteList(key));
    }

    public static boolean isInFirebaseWhiteList(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        try {
            if (sFirebaseWhiteList != null && !sFirebaseWhiteList.isEmpty()) {
                if (sFirebaseWhiteList.contains(key)) {
                    return true;
                }
                for (String item : sFirebaseWhiteList) {
                    if (item != null && item.contains("*") && key.startsWith(item.replace("*", ""))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
