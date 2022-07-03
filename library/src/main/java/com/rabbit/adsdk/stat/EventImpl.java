package com.rabbit.adsdk.stat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.BounceRateManager;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class EventImpl implements IEvent {

    private static EventImpl sEventImpl;

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

    private Context mContext;

    private EventImpl() {
    }

    public void init(Context context) {
        mContext = context;
        recordActiveTime();
        BounceRateManager.get(context).init();
    }

    private void recordActiveTime() {
        try {
            long time = Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
            if (time <= 0) {
                Utils.putLong(mContext, Constant.PREF_USER_ACTIVE_TIME, System.currentTimeMillis());
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取活跃天数，最多统计2年的，最大值721，超过721的全按照721统计
     * @return
     */
    public int getActiveDays() {
        int activeDays = -1;
        try {
            Calendar calendar = Calendar.getInstance();
            int nowYear = calendar.get(Calendar.YEAR);
            int nowMonth = calendar.get(Calendar.MONTH) + 1;
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long nowDate = calendar.getTimeInMillis();

            long userActiveTime = Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(userActiveTime);
            int activeYear = calendar.get(Calendar.YEAR);
            int activeMonth = calendar.get(Calendar.MONTH) + 1;
            int activeDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long activeDate = calendar.getTimeInMillis();

            try {
                Log.iv(Log.TAG_SDK, String.format("now : %d-%02d-%02d , active : %d-%02d-%02d, nowDate : %d , activeDate : %d", nowYear, nowMonth, nowDay, activeYear, activeMonth, activeDay, nowDate, activeDate));
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            activeDays = Long.valueOf((nowDate - activeDate) / Constant.ONE_DAY_MS).intValue();
            if (activeDays < 0) {
                activeDays = 0;
            }
            if (activeDays > 720) {
                activeDays = 721;
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            activeDays = -1;
        }
        return activeDays;
    }

    private String generateEventIdAlias(Context context, String eventId) {
        return eventId;
    }

    private boolean checkArgument(Context context, String placeName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(placeName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.iv(Log.TAG, "context == null or place name == null or sdk == null or type all must not be empty or null");
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

    @Override
    public void reportAdRequest(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "request", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        if (extra != null) {
            extra.put("vpn_status", Utils.isVPNConnected(context) ? "on" : "off");
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_request", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdLoaded(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "loaded", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_loaded", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdReLoaded(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "reloaded", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_reloaded", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdShow(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "show", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_show", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdImp(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "imp", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
        if (extra != null) {
            extra.put("vpn_status", Utils.isVPNConnected(context) ? "on" : "off");
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_imp", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdClick(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "click", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_click", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
        BounceRateManager.get(context).onAdClick(extra);
    }

    @Override
    public void reportAdReward(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "receive", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_reward", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdError(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!isReportError(context)) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId(context, "error", sdk, type);
        extra = addExtraForError(context, extra);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_error", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdClose(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "close", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, null, null);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
        reportEvent(context, "e_ad_close", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
    }

    @Override
    public void reportAdLoadSuccessTime(Context context, String placeName, String sdk, String type, int value) {
        if (!isReportTime(context)) {
            return;
        }
        String eventId = "load_ad_success_time";
        eventId = generateEventIdAlias(context, eventId);
        if (!checkArgument(context, eventId, sdk, type)) {
            return;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", placeName);
        map.put("sdk", sdk);
        map.put("type", type);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , value : " + value);
        if (isReportUmeng(context)) {
            InternalStat.sendUmengValue(context, eventId, map, value);
        }
        reportEvent(context, eventId, null, map);
    }

    @Override
    public void reportAdLoadFailureTime(Context context, String placeName, String sdk, String type, String error, int value) {
        if (!isReportTime(context)) {
            return;
        }
        if (!checkArgument(context, error, sdk, type)) {
            return;
        }
        String eventId = "load_ad_failure_time";
        eventId = generateEventIdAlias(context, eventId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", placeName);
        map.put("sdk", sdk);
        map.put("type", type);
        map.put("error", error);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , error : " + error + " , value : " + value);
        if (isReportUmeng(context)) {
            InternalStat.sendUmengValue(context, eventId, map, value);
        }
        reportEvent(context, eventId, null, map);
    }

    @Override
    public void reportKVEvent(Context context, String key, String value, Map<String, Object> extra) {
        if (context == null) {
            return;
        }
        String eventId = generateEventIdAlias(context, key);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + value + " , extra : " + extra);
        reportEvent(context, eventId, value, extra);
    }

    @Override
    public void reportAdPlaceSeqRequest(Context context, String placeName) {
    }

    @Override
    public void reportAdPlaceSeqLoaded(Context context, String placeName) {
    }

    @Override
    public void reportAdPlaceSeqError(Context context, String placeName) {
    }

    private boolean parseReport(String value, boolean defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                Log.e(Log.TAG, "parseReport error : " + e);
            }
        }
        return defaultValue;
    }

    private boolean isReportError(Context context) {
        String value = DataManager.get(context).getString("report_error");
        boolean result = parseReport(value, false);
        Log.iv(Log.TAG_SDK, "is report error : " + result);
        return result;
    }

    /**
     * 默认不再上报成功和失败时间
     *
     * @param context
     * @return
     */
    private boolean isReportTime(Context context) {
        String value = DataManager.get(context).getString("report_time");
        boolean result = parseReport(value, false);
        Log.iv(Log.TAG_SDK, "is report time : " + result);
        return result;
    }

    private boolean isReportAppsflyer(Context context) {
        String value = DataManager.get(context).getString("ad_report_appsflyer");
        boolean result = parseReport(value, false);
        Log.iv(Log.TAG_SDK, "is report appsflyer : " + result);
        return result;
    }

    private boolean isReportUmeng(Context context) {
        String value = DataManager.get(context).getString("ad_report_umeng");
        boolean result = parseReport(value, true);
        Log.iv(Log.TAG_SDK, "is report umeng : " + result);
        return result;
    }

    private boolean isReportFirebase(Context context) {
        String value = DataManager.get(context).getString("ad_report_firebase");
        boolean result = parseReport(value, true);
        Log.iv(Log.TAG_SDK, "is report firebase : " + result);
        return result;
    }

    private boolean isReportFlurry(Context context) {
        String value = DataManager.get(context).getString("ad_report_flurry");
        boolean result = parseReport(value, true);
        Log.iv(Log.TAG_SDK, "is report flurry : " + result);
        return result;
    }

    private boolean isReportTalkingData(Context context) {
        String value = DataManager.get(context).getString("ad_report_talkingdata");
        boolean result = parseReport(value, true);
        Log.iv(Log.TAG_SDK, "is talkingdata flurry : " + result);
        return result;
    }

    private Map<String, Object> addExtraForError(Context context, Map<String, Object> extra) {
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
                extra = new HashMap<String, Object>();
            }
            extra.put("network", builder.toString());
        } catch (Exception e) {
        } catch (Error e) {
        }
        return extra;
    }

    public void reportEvent(Context context, String eventId, String value, Map<String, Object> extra) {
        Map<String, Object> maps = extra;
        if (isReportAppsflyer(context)) {
            InternalStat.sendAppsflyer(context, eventId, value, maps);
        }
        if (isReportFirebase(context)) {
            InternalStat.sendFirebaseAnalytics(context, eventId, value, maps);
        }
        if (isReportUmeng(context)) {
            InternalStat.sendUmeng(context, eventId, value, maps);
        }
        if (isReportFlurry(context)) {
            InternalStat.sendFlurry(context, eventId, value, maps);
        }
        if (isReportTalkingData(context)) {
            InternalStat.sendTalkingData(context, eventId, value, maps);
        }
    }

    private Map<String, Object> addExtra(Map<String, Object> extra, String name, String sdk, String type, String pid, double ecpm, String network, String networkPid) {
        if (extra == null) {
            extra = new HashMap<String, Object>();
        }
        extra.put("name", name);
        extra.put("sdk", sdk);
        extra.put("type", type);
        extra.put("network", network);
        extra.put("network_pid", networkPid);
        extra.put("pid", pid);
        extra.put("ecpm", ecpm);
        extra.put("active_days", getActiveDays() + "d");
        extra.put("country", Utils.getCountryFromLocale(mContext));
        return extra;
    }
}