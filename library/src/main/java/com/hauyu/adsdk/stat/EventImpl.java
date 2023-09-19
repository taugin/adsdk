package com.hauyu.adsdk.stat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.hauyu.adsdk.InternalStat;
import com.hauyu.adsdk.Utils;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.db.DBManager;
import com.hauyu.adsdk.core.framework.AdStatManager;
import com.hauyu.adsdk.core.framework.BounceRateManager;
import com.hauyu.adsdk.core.framework.FBStatManager;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.log.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
        BounceRateManager.get(context).init();
    }

    /**
     * 获取活跃天数
     *
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

            long userActiveTime = DataManager.get(mContext).getFirstActiveTime();
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
                Log.iv(Log.TAG, "error : " + e);
            }
            activeDays = Long.valueOf((nowDate - activeDate) / Constant.ONE_DAY_MS).intValue();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            activeDays = -1;
        }
        if (activeDays < 0) {
            activeDays = 0;
        }
        return activeDays;
    }

    public String getActiveDayString() {
        int activeDays = getActiveDays();
        if (activeDays > 720) {
            return "maxd";
        }
        return String.format(Locale.ENGLISH, "%03dd", activeDays);
    }

    public String getActiveDate() {
        String activeDate;
        try {
            long userActiveTime = DataManager.get(mContext).getFirstActiveTime();
            activeDate = Constant.SDF_ACTIVE_DATE.format(new Date(userActiveTime));
        } catch (Exception e) {
            activeDate = "00-00";
        }
        return activeDate;
    }

    public String getActiveYear() {
        String activeYear;
        try {
            long userActiveTime = DataManager.get(mContext).getFirstActiveTime();
            activeYear = Constant.SDF_ACTIVE_YEAR.format(new Date(userActiveTime));
        } catch (Exception e) {
            activeYear = "0000";
        }
        return activeYear;
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
        try {
            AdStatManager.get(mContext).recordAdImp(sdk, placeName, network);
        } catch (Exception e) {
        }
        if (!TextUtils.equals(sdk, Constant.AD_SDK_ADMOB) && network != null && network.toLowerCase(Locale.ENGLISH).contains(Constant.AD_SDK_ADMOB)) {
            eventId = generateEventId(context, "imp", Constant.AD_SDK_ADMOB, type);
            Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
            reportEvent(context, eventId, placeName, extra);
        }
    }

    @Override
    public void reportAdClick(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra, String impressionId) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        boolean isAdClicked = DBManager.get(context).isAdClicked(impressionId);
        String eventId = generateEventId(context, "click", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
        if (extra != null) {
            extra.put("first", String.valueOf(!isAdClicked));
        }
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra + " , impression id : " + impressionId);
        reportEvent(context, "e_ad_click", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
        reportAdClickDistinct(context, placeName, sdk, network, type, pid, networkPid, ecpm, extra, isAdClicked);
        try {
            AdStatManager.get(mContext).recordAdClick(sdk, placeName, pid, network, extra, impressionId);
        } catch (Exception e) {
        }
        if (!TextUtils.equals(sdk, Constant.AD_SDK_ADMOB) && network != null && network.toLowerCase(Locale.ENGLISH).contains(Constant.AD_SDK_ADMOB)) {
            eventId = generateEventId(context, "click", Constant.AD_SDK_ADMOB, type);
            Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
            reportEvent(context, eventId, placeName, extra);
            reportAdClickDistinct(context, placeName, Constant.AD_SDK_ADMOB, network, type, pid, networkPid, ecpm, extra, isAdClicked);
        }
        String placement = null;
        if (extra != null) {
            try {
                placement = (String) extra.get("placement");
            } catch (Exception e) {
            }
        }
        FBStatManager.get(context).reportFirebaseClick(type, network, placement);
    }

    private void reportAdClickDistinct(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra, boolean isAdClicked) {
        try {
            if (!isAdClicked) {
                String eventIdDistinct = generateEventId(context, "click", sdk + "_distinct", type);
                Log.iv(Log.TAG, "event id distinct : " + eventIdDistinct);
                extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
                reportEvent(context, eventIdDistinct, placeName, extra);
            }
        } catch (Exception e) {
        }
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
                Log.iv(Log.TAG, "parseReport error : " + e);
            }
        }
        return defaultValue;
    }

    private boolean isReportError(Context context) {
        String value = DataManager.get(context).getString("report_error");
        boolean result = parseReport(value, false);
        // Log.iv(Log.TAG_SDK, "is report error : " + result);
        return result;
    }

    /**
     * 默认不再上报成功和失败时间
     *
     * @param context
     * @return
     */
    private boolean isReportTime(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_time");
        boolean result = parseReport(value, false);
        // Log.iv(Log.TAG_SDK, "is report time : " + result);
        return result;
    }

    private boolean isReportAppsflyer(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_appsflyer");
        boolean result = parseReport(value, false);
        // Log.iv(Log.TAG_SDK, "is report appsflyer : " + result);
        return result;
    }

    private boolean isReportUmeng(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_umeng");
        boolean result = parseReport(value, true);
        // Log.iv(Log.TAG_SDK, "is report umeng : " + result);
        return result;
    }

    private boolean isReportFirebase(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_firebase");
        boolean result = parseReport(value, true);
        // Log.iv(Log.TAG_SDK, "is report firebase : " + result);
        return result;
    }

    private boolean isReportFlurry(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_flurry");
        boolean result = parseReport(value, true);
        // Log.iv(Log.TAG_SDK, "is report flurry : " + result);
        return result;
    }

    private boolean isReportTalkingData(Context context) {
        String value = InternalStat.getAdReportString(context, "ad_report_bool_td");
        boolean result = parseReport(value, true);
        // Log.iv(Log.TAG_SDK, "is talkingdata flurry : " + result);
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
            InternalStat.sendFirebaseAnalytics(context, eventId, value, maps, InternalStat.isInFirebaseWhiteList(eventId));
        }
        if (isReportUmeng(context)) {
            InternalStat.sendUmeng(context, eventId, value, maps, InternalStat.isInUmengWhiteList(eventId));
        }
        if (isReportFlurry(context)) {
            InternalStat.sendFlurry(context, eventId, value, maps, InternalStat.isInFirebaseWhiteList(eventId));
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
        extra.put("active_days", getActiveDayString());
        extra.put("active_date", getActiveDate());
        extra.put("active_year", getActiveYear());
        extra.put("country", Utils.getCountryFromLocale(mContext));
        return extra;
    }
}