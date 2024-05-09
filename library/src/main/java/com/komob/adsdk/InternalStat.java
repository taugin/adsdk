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

    private static Object mFacebookObject = null;
    private static final String SDK_NAME_UMENG = "umeng";
    private static final String SDK_NAME_FIREBASE = "firebase";
    private static final String SDK_NAME_APPSFLYER = "appsflyer";
    private static final String SDK_NAME_TALKING_DATA = "talkingdata";
    private static final String SDK_NAME_FACEBOOK = "facebook";
    private static final List<String> sUmengWhiteList;

    private static final List<String> sFirebaseWhiteList;

    private static final Map<String, Boolean> sSdkIntegrated;

    static {
        sUmengWhiteList = Arrays.asList(
                Constant.AD_IMPRESSION_REVENUE,
                "imp_splash_admob",
                "imp_interstitial_admob",
                "imp_native_admob",
                "imp_banner_admob",
                "imp_reward_admob",
                "click_splash_admob",
                "click_interstitial_admob",
                "click_native_admob",
                "click_banner_admob",
                "click_reward_admob",
                "click_splash_admob_distinct",
                "click_interstitial_admob_distinct",
                "click_native_admob_distinct",
                "click_banner_admob_distinct",
                "click_reward_admob_distinct",
                "ad_sponsored_click",
                "ad_spread_installed"
        );

        sFirebaseWhiteList = Arrays.asList(
                Constant.AD_IMPRESSION,
                Constant.AD_IMPRESSION_REVENUE,
                "app_first_open_ano",
                "app_first_open_ao",
                "ad_spread_installed",
                "Total_Ads_Revenue_*",
                "gav_*"
        );

        sSdkIntegrated = new HashMap<>();
        boolean sdkIntegrated;
        try {
            Class.forName("com.umeng.analytics.MobclickAgent");
            sdkIntegrated = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, SDK_NAME_UMENG + " init error : " + e);
            sdkIntegrated = false;
        }
        sSdkIntegrated.put(SDK_NAME_UMENG, sdkIntegrated);

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
            Class.forName("com.tendcloud.tenddata.TalkingDataSDK");
            sdkIntegrated = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, SDK_NAME_TALKING_DATA + " init error : " + e);
            sdkIntegrated = false;
        }
        sSdkIntegrated.put(SDK_NAME_TALKING_DATA, sdkIntegrated);

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
        if (bundle != null && bundle.size() > 25) {
            try {
                bundle.remove(Constant.AD_PLACEMENT_NEW);
                bundle.remove(Constant.AD_ROUND_CPM_NEW);
            } catch (Exception e) {
            }
        }
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

    private static void checkUmengDataType(Map<String, Object> map, Map<String, Object> extra) {
        try {
            if (extra != null && !extra.isEmpty() && map != null) {
                for (Map.Entry<String, Object> entry : extra.entrySet()) {
                    if (entry != null) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if ((value instanceof String)
                                || (value instanceof Integer)
                                || (value instanceof Long)
                                || (value instanceof Short)
                                || (value instanceof Float)
                                || (value instanceof Double)
                                || (value.getClass().isArray())) {
                            map.put(key, value);
                        } else {
                            map.put(key, String.valueOf(value));
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 发送友盟计数事件
     *
     * @param context
     * @param eventId
     * @param extra
     */
    public static void sendUmeng(Context context, String eventId, Map<String, Object> extra) {
        sendUmeng(context, eventId, null, extra);
    }

    public static void sendUmeng(Context context, String eventId, String value, Map<String, Object> extra) {
        sendUmeng(context, eventId, value, extra, true);
    }

    public static void sendUmeng(Context context, String eventId, String value, Map<String, Object> extra, boolean allowReport) {
        String platform = SDK_NAME_UMENG;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        }
        checkUmengDataType(map, extra);
        Log.iv(Log.TAG_SDK, "[" + platform + "] event id : " + eventId + " , value : " + map);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventObject", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
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
     * 发送友盟计算事件
     *
     * @param context
     * @param eventId
     * @param extra
     * @param value
     */
    public static void sendUmengValue(Context context, String eventId, Map<String, Object> extra, int value) {
        sendUmengValue(context, eventId, extra, value, true);
    }

    public static void sendUmengValue(Context context, String eventId, Map<String, Object> extra, int value, boolean allowReport) {
        String platform = SDK_NAME_UMENG;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("event_id", eventId);
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
        Log.iv(Log.TAG_SDK, "[" + platform + "] event id : " + eventId + " , value : " + map);
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
            Log.iv(Log.TAG_SDK, "send " + platform + " error : " + error);
        }
    }

    private static void sendUmengError(Context context, Throwable throwable, boolean allowReport) {
        String platform = SDK_NAME_UMENG;
        if (!isReportPlatform(context, "umeng_error", platform, allowReport)) {
            return;
        }
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

    /**
     * 发送talking data统计事件
     *
     * @param context
     * @param eventId
     * @param value
     * @param extra
     */
    public static void sendTalkingData(Context context, String eventId, String value, Map<String, Object> extra) {
        sendTalkingData(context, eventId, value, extra, true);
    }

    public static void sendTalkingData(Context context, String eventId, String value, Map<String, Object> extra, boolean allowReport) {
        String platform = SDK_NAME_TALKING_DATA;
        if (!isReportPlatform(context, eventId, platform, allowReport)) {
            return;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (extra != null && !extra.isEmpty()) {
            map.putAll(extra);
        }
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        }
        Log.iv(Log.TAG_SDK, "[" + platform + "] event id : " + eventId + " , value : " + map);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.tendcloud.tenddata.TalkingDataSDK");
            Method method = clazz.getDeclaredMethod("onEvent", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
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
        sendUmeng(context, key, value, map, isInUmengWhiteList(key));
        sendAppsflyer(context, key, value, map, false);
        sendFirebaseAnalytics(context, key, value, map, isInFirebaseWhiteList(key));
        sendTalkingData(context, key, value, map);
    }

    public static void reportError(Context context, Throwable e) {
        sendUmengError(context, e, true);
    }

    public static boolean isInUmengWhiteList(String key) {
        try {
            return sUmengWhiteList.contains(key);
        } catch (Exception e) {
        }
        return false;
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
