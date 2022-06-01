package com.rabbit.adsdk.stat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2018-12-19.
 */

public class InternalStat {

    private static final long DEFAULT_MAX_EVENT_COUNT = 10000;
    private static final String AD_REPORT_EVENT_PLATFORM_ENABLE = "ad_report_event_%s";
    private static final String AD_REPORT_EVENT_PLATFORM_WHITE = "ad_report_event_%s_white";
    private static final String AD_REPORT_EVENT_PLATFORM_BLACK = "ad_report_event_%s_black";
    private static final String AD_REPORT_EVENT_PLATFORM_COUNT = "ad_report_event_%s_count";

    private static final String PREF_AD_REPORT_EVENT_PLATFORM_COUNT = "pref_ad_report_event_%s_count";
    private static final String PREF_AD_REPORT_EVENT_RESET_DATE = "pref_ad_report_event_reset_date";
    private static final String PREF_AD_REPORT_EVENT_PLATFORM_LIST = "pref_ad_report_event_platform_list";

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

    public static void sendFirebaseAnalytics(Context context, String eventId, String value, Map<String, Object> extra, boolean defaultValue) {
        String platform = "firebase";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("event_id", eventId);
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        }
        mapToBundle(extra, bundle);
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + bundle);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            Method method = clazz.getMethod("getInstance", Context.class);
            Object instance = method.invoke(null, context);
            method = clazz.getMethod("logEvent", String.class, Bundle.class);
            method.invoke(instance, eventId, bundle);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "error : " + error);
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
        sendUmeng(context, eventId, value, extra, true);
    }

    public static void sendUmeng(Context context, String eventId, String value, Map<String, Object> extra, boolean defaultValue) {
        String platform = "umeng";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("event_id", eventId);
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
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
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + map);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEvent", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "error : " + error);
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
    public static void sendUmengObject(Context context, String eventId, Map<String, Object> extra) {
        sendUmengObject(context, eventId, extra, true);
    }

    public static void sendUmengObject(Context context, String eventId, Map<String, Object> extra, boolean defaultValue) {
        String platform = "umeng";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        checkUmengDataType(map, extra);
        Log.iv(Log.TAG_SDK, platform + " event object id : " + eventId + " , value : " + map);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventObject", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "error : " + error);
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

    public static void sendUmengValue(Context context, String eventId, Map<String, Object> extra, int value, boolean defaultValue) {
        String platform = "umeng";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
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
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + map);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventValue", Context.class, String.class, Map.class, int.class);
            method.invoke(null, context, eventId, map, value);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "Report Event sendUmengEventValue error : " + error);
        }
    }

    private static void sendUmengError(Context context, Throwable throwable, boolean defaultValue) {
        String platform = "umeng";
        if (!isReportPlatform(context, "umeng_error", platform, defaultValue)) {
            return;
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("reportError", Context.class, Throwable.class);
            method.invoke(null, context, throwable);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "error : " + error);
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

    public static void sendAppsflyer(Context context, String eventId, String value, Map<String, Object> extra, boolean defaultValue) {
        String platform = "appsflyer";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
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
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + eventValue);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.appsflyer.AppsFlyerLib");
            Method method = clazz.getMethod("getInstance");
            Object instance = method.invoke(null);
            method = clazz.getMethod("trackEvent", Context.class, String.class, Map.class);
            method.invoke(instance, context, eventId, eventValue);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "error : " + error);
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
        sendFlurry(context, eventId, value, extra, true);
    }

    public static void sendFlurry(Context context, String eventId, String value, Map<String, Object> extra, boolean defaultValue) {
        String platform = "flurry";
        if (!isReportPlatform(context, eventId, platform, defaultValue)) {
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
        Log.iv(Log.TAG_SDK, platform + " event id : " + eventId + " , value : " + eventValue);
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.flurry.android.FlurryAgent");
            Method method = clazz.getDeclaredMethod("logEvent", String.class, Map.class);
            method.invoke(null, eventId, eventValue);
            reportPlatformEventCount(context, platform);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG_SDK, "Report Event sendFlurry error : " + error);
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
        sendUmeng(context, key, value, map);
        sendAppsflyer(context, key, value, map, false);
        sendFirebaseAnalytics(context, key, value, map);
        sendFlurry(context, key, value, map);
    }

    public static void reportError(Context context, Throwable e) {
        sendUmengError(context, e, true);
    }

    private static boolean isReportPlatform(Context context, String eventId, String platform, boolean defaultValue) {
        boolean finalResult = false;
        try {
            String eventArgs = String.format(Locale.getDefault(), AD_REPORT_EVENT_PLATFORM_ENABLE, platform);
            boolean isReport = isReportEvent(context, eventArgs, defaultValue);
            boolean isEventAllow = false;
            boolean isEventCountAllow = false;
            if (isReport) {
                isEventAllow = isEventAllow(context, eventId, platform);
                isEventCountAllow = isEventCountAllow(context, platform);
            }
            finalResult = isReport && isEventAllow && isEventCountAllow;
            Log.iv(Log.TAG_SDK, "[" + eventId + "] report " + platform + " : " + finalResult + " , enable : " + finalResult + " , event allow : " + isEventAllow + " , event count allow : " + isEventCountAllow);
        } catch (Exception e) {
        }
        return finalResult;
    }

    /**
     * 判断事件是否允许上报, 白名单优先于黑名单
     *
     * @param context
     * @param eventId
     * @param platform
     * @return
     */
    private static boolean isEventAllow(Context context, String eventId, String platform) {
        try {
            String eventArgs = String.format(Locale.getDefault(), AD_REPORT_EVENT_PLATFORM_WHITE, platform);
            String whiteEventString = DataManager.get(context).getString(eventArgs);
            if (!TextUtils.isEmpty(whiteEventString)) {
                // 白名单生效
                String[] whiteEventArray = whiteEventString.split(",");
                List<String> whiteEventList = Arrays.asList(whiteEventArray);
                Log.iv(Log.TAG, platform + " white event list : " + whiteEventList);
                if (whiteEventList != null) {
                    return whiteEventList.contains(eventId);
                }
            }
            eventArgs = String.format(Locale.getDefault(), AD_REPORT_EVENT_PLATFORM_BLACK, platform);
            String blackEventString = DataManager.get(context).getString(eventArgs);
            if (!TextUtils.isEmpty(blackEventString)) {
                // 黑名单名单生效
                String[] blackEventArray = blackEventString.split(",");
                List<String> blackEventList = Arrays.asList(blackEventArray);
                Log.iv(Log.TAG, platform + " black event list : " + blackEventList);
                if (blackEventList != null) {
                    return !blackEventList.contains(eventId);
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    private static boolean isReportEvent(Context context, String reportPlatform, boolean defaultValue) {
        String value = DataManager.get(context).getString(reportPlatform);
        return parseReport(value, defaultValue);
    }

    private static boolean parseReport(String value, boolean defaultValue) {
        try {
            if (!TextUtils.isEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
        }
        return defaultValue;
    }

    /**
     * 获取远程配置的平台事件最大值，默认为{@link #DEFAULT_MAX_EVENT_COUNT}
     *
     * @param context
     * @param platform
     * @return
     */
    private static long getMaxEventCount(Context context, String platform) {
        String eventArgs = String.format(Locale.getDefault(), AD_REPORT_EVENT_PLATFORM_COUNT, platform);
        String eventCountString = DataManager.get(context).getString(eventArgs);
        long maxEventCount = DEFAULT_MAX_EVENT_COUNT;
        if (!TextUtils.isEmpty(eventCountString)) {
            try {
                maxEventCount = Long.parseLong(eventCountString);
            } catch (Exception e) {
                maxEventCount = DEFAULT_MAX_EVENT_COUNT;
            }
        }
        return maxEventCount;
    }

    /**
     * 判断当天平台事件数目是否超过最大值, 参数{@link #PREF_AD_REPORT_EVENT_PLATFORM_COUNT}
     *
     * @param context
     * @param platform
     * @return
     */
    private static boolean isEventCountAllow(Context context, String platform) {
        try {
            resetPlatformEventCountIfNeed(context);
            String eventArgs = String.format(Locale.getDefault(), PREF_AD_REPORT_EVENT_PLATFORM_COUNT, platform);
            long curEventCount = Utils.getLong(context, eventArgs);
            long maxEventCount = getMaxEventCount(context, platform);
            Log.iv(Log.TAG_SDK, "[" + platform + "] event count : " + curEventCount + "/" + maxEventCount);
            return curEventCount < maxEventCount;
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
        return false;
    }

    /**
     * 记录平台事件总数, 参数{@link #PREF_AD_REPORT_EVENT_PLATFORM_COUNT}，
     * 记录平台列表{@link #recordPlatformList}
     *
     * @param context
     * @param platform
     */
    private static void reportPlatformEventCount(Context context, String platform) {
        try {
            String eventArgs = String.format(Locale.getDefault(), PREF_AD_REPORT_EVENT_PLATFORM_COUNT, platform);
            long count = Utils.getLong(context, eventArgs, 0);
            count += 1;
            Utils.putLong(context, eventArgs, count);
            recordPlatformList(context, platform);
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
    }

    /**
     * 记录打点平台列表, 参数{@link #PREF_AD_REPORT_EVENT_PLATFORM_LIST}
     *
     * @param context
     * @param platform
     */
    private static void recordPlatformList(Context context, String platform) {
        Set<String> sets = Utils.getStringSet(context, PREF_AD_REPORT_EVENT_PLATFORM_LIST);
        Set<String> newSets;
        if (sets != null && !sets.isEmpty()) {
            newSets = new HashSet<>(sets);
        } else {
            newSets = new HashSet<>();
        }
        newSets.add(platform);
        Log.iv(Log.TAG_SDK, "record statistics platform set : " + newSets);
        Utils.putStringSet(context, PREF_AD_REPORT_EVENT_PLATFORM_LIST, newSets);
    }

    /**
     * 重置平台事件计数，所有平台一起重置, 记录日期参数{@link #PREF_AD_REPORT_EVENT_RESET_DATE}
     * 记录事件参数{@link #PREF_AD_REPORT_EVENT_PLATFORM_COUNT}
     *
     * @param context
     */
    private static void resetPlatformEventCountIfNeed(Context context) {
        long nowDate = Utils.getTodayTime();
        long lastDate = Utils.getLong(context, PREF_AD_REPORT_EVENT_RESET_DATE, 0);
        if (nowDate != lastDate) {
            Set<String> sets = Utils.getStringSet(context, PREF_AD_REPORT_EVENT_PLATFORM_LIST);
            if (sets != null && !sets.isEmpty()) {
                for (String s : sets) {
                    String eventArgs = String.format(Locale.getDefault(), PREF_AD_REPORT_EVENT_PLATFORM_COUNT, s);
                    Utils.putLong(context, eventArgs, 0);
                }
            }
            Utils.putLong(context, PREF_AD_REPORT_EVENT_RESET_DATE, nowDate);
        }
    }

    private static boolean isUmengEventObjectEnable() {
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventObject", Context.class, String.class, Map.class);
            return method != null;
        } catch (Exception | Error e) {
        }
        return false;
    }
}
