package com.rabbit.adsdk.stat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.BlockAdsManager;
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

    public String getUserFlag() {
        try {
            Calendar calendar = Calendar.getInstance();
            int nowYear = calendar.get(Calendar.YEAR);
            int nowMonth = calendar.get(Calendar.MONTH) + 1;
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long nowTime = calendar.getTimeInMillis();

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
            long activeTime = calendar.getTimeInMillis();

            try {
                Log.v(Log.TAG, String.format("now : %d-%02d-%02d , active : %d-%02d-%02d, nowTime : %d , activeTime : %d", nowYear, nowMonth, nowDay, activeYear, activeMonth, activeDay, nowTime, activeTime));
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            if (nowTime < activeTime) {
                return "error";
            }
            if (nowTime == activeTime) {
                return "true";
            }
            if (nowTime > activeTime) {
                return "false";
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return "error";
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
    public void reportAdRequest(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "request", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_request", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdLoaded(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "loaded", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_loaded", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdShow(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "show", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_show", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdImp(Context context, String placeName, String sdk, String render, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "imp", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_imp", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdClick(Context context, String placeName, String sdk, String render, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "click", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_click", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdReward(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "receive", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_reward", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdError(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!isReportError(context)) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId(context, "error", sdk, type);
        extra = addExtraForError(context, extra);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_error", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
    }

    @Override
    public void reportAdClose(Context context, String placeName, String sdk, String type, String pid, String ecpm, Map<String, String> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "close", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm);
        reportEvent(context, placeName, eventId, extra);
        reportEvent(context, placeName, "e_ad_close", extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + placeName + " , extra : " + extra);
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", placeName);
        map.put("sdk", sdk);
        map.put("type", type);
        if (isReportUmeng(context)) {
            Map<String, Object> umengMap = null;
            if (map != null && !map.isEmpty()) {
                umengMap = new HashMap<String, Object>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    umengMap.put(entry.getKey(), entry.getValue());
                }
            }
            InternalStat.sendUmengEventValue(context, eventId, umengMap, value);
        }
        reportEvent(context, null, eventId, map);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , value : " + value);
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", placeName);
        map.put("sdk", sdk);
        map.put("type", type);
        map.put("error", error);
        if (isReportUmeng(context)) {
            Map<String, Object> umengMap = null;
            if (map != null && !map.isEmpty()) {
                umengMap = new HashMap<String, Object>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    umengMap.put(entry.getKey(), entry.getValue());
                }
            }
            InternalStat.sendUmengEventValue(context, eventId, umengMap, value);
        }
        reportEvent(context, null, eventId, map);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , sdk : " + sdk + " , type : " + type + " , error : " + error + " , value : " + value);
    }

    @Override
    public void reportKVEvent(Context context, String key, String value, Map<String, String> extra) {
        if (context == null) {
            return;
        }
        String eventId = generateEventIdAlias(context, key);
        reportEvent(context, value, eventId, extra);
        Log.iv(Log.TAG, "Report Event upload key : " + eventId + " , value : " + value + " , extra : " + extra);
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
        Log.v(Log.TAG, "is report error : " + result);
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
        Log.v(Log.TAG, "is report time : " + result);
        return result;
    }

    private boolean isReportUmeng(Context context) {
        String value = DataManager.get(context).getString("report_umeng");
        boolean result = parseReport(value, true);
        Log.v(Log.TAG, "is report umeng : " + result);
        return result;
    }

    private boolean isReportFirebase(Context context) {
        String value = DataManager.get(context).getString("report_firebase");
        boolean result = parseReport(value, true);
        Log.v(Log.TAG, "is report firebase : " + result);
        return result;
    }

    private boolean isReportFacebook(Context context) {
        String value = DataManager.get(context).getString("report_facebook");
        boolean result = parseReport(value, true);
        Log.v(Log.TAG, "is report facebook : " + result);
        return result;
    }

    private boolean isReportFlurry(Context context) {
        String value = DataManager.get(context).getString("report_flurry");
        boolean result = parseReport(value, true);
        Log.v(Log.TAG, "is report flurry : " + result);
        return result;
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

    private void reportEvent(Context context, String value, String eventId, Map<String, String> extra) {
        Map<String, Object> maps = null;
        if (extra != null && !extra.isEmpty()) {
            maps = new HashMap<String, Object>();
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                maps.put(entry.getKey(), entry.getValue());
            }
        }
        if (isReportFirebase(context)) {
            InternalStat.sendFirebaseAnalytics(context, value, eventId, maps);
        }
        if (isReportUmeng(context)) {
            InternalStat.sendUmeng(context, value, eventId, maps);
        }
        if (isReportFacebook(context)) {
            InternalStat.sendFacebook(context, value, eventId, maps);
        }
        if (isReportFlurry(context)) {
            InternalStat.sendFlurry(context, value, eventId, maps);
        }
    }

    private Map<String, String> addExtra(Map<String, String> extra, String name, String sdk, String type, String pid, String ecpm) {
        if (extra == null) {
            extra = new HashMap<String, String>();
        }
        extra.put("name", name);
        extra.put("sdk", sdk);
        extra.put("type", type);
        extra.put("pid", pid);
        extra.put("ecpm", ecpm);
        extra.put("new_user", getUserFlag());
        return extra;
    }
}