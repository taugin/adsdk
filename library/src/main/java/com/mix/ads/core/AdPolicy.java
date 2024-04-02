package com.mix.ads.core;

import android.content.Context;
import android.text.TextUtils;

import com.mix.ads.data.config.AdPlace;
import com.mix.ads.log.Log;
import com.mix.ads.utils.Utils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Created by Administrator on 2018/3/27.
 */

public class AdPolicy {
    private static AdPolicy sAdPolicy;

    public static AdPolicy get(Context context) {
        synchronized (AdPolicy.class) {
            if (sAdPolicy == null) {
                createInstance(context);
            }
        }
        return sAdPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (AdPolicy.class) {
            if (sAdPolicy == null) {
                sAdPolicy = new AdPolicy(context);
            }
        }
    }

    private AdPolicy(Context context) {
        mContext = context;
        mRandom = new Random(System.currentTimeMillis());
    }

    private static final long ONE_DAY = 24 * 60 * 60 * 1000l;
    private static final String SHOW_COUNT_SUFFIX = "_showcount";
    private static final String LAST_RESET_TIME_SUFFIX = "_last_reset_time";
    private static final String PREF_RECORD_PLACE_REQ_TIMES = "pref_record_%s_%s_req_times";
    private static final String PREF_RECORD_MAX_REQ_TIME_KEY_SET = "pref_record_max_req_time_key_set";
    private static final String PREF_RESET_MAX_REQ_TIME_DATETIME = "pref_reset_max_req_time_datetime";
    private Context mContext;
    private Random mRandom;

    public void reportAdPlaceShow(String originPlaceName, AdPlace adPlace) {
        if (adPlace == null) {
            Log.iv(Log.TAG_SDK, "adPlace == null");
            return;
        }
        String placeName = originPlaceName;
        if (TextUtils.isEmpty(originPlaceName)) {
            placeName = adPlace.getName();
        }
        if (TextUtils.isEmpty(placeName)) {
            Log.iv(Log.TAG_SDK, "placeName == null");
            return;
        }
        long loadCount = Utils.getLong(mContext, getShowCountKey(placeName), 0);
        Utils.putLong(mContext, getShowCountKey(placeName), loadCount + 1);
        Log.iv(Log.TAG_SDK, "[" + placeName + "]" + " show count : " + (loadCount + 1));
    }

    public boolean reachMaxShowCount(String placeName, int maxCount) {
        long loadCount = Utils.getLong(mContext, getShowCountKey(placeName), 0);
        return loadCount > maxCount;
    }

    public boolean allowAdPlaceLoad(AdPlace adPlace) {
        if (adPlace == null) {
            Log.iv(Log.TAG_SDK, "adPlace is null");
            return false;
        }
        String placeName = adPlace.getName();
        if (TextUtils.isEmpty(placeName)) {
            Log.iv(Log.TAG_SDK, "placeName is null");
            return false;
        }
        resetShowCountEveryDay(placeName);
        boolean exceedMaxCount = reachMaxShowCount(placeName, adPlace.getMaxCount());
        if (exceedMaxCount) {
            long maxCount = adPlace.getMaxCount();
            Log.iv(Log.TAG_SDK, "[" + placeName + "]" + " exceed max count " + maxCount);
            return false;
        }
        boolean allowByPercent = allowLoadByPercent(adPlace.getPercent());
        if (!allowByPercent) {
            Log.iv(Log.TAG_SDK, "percent not allow");
            return false;
        }
        return true;
    }

    public boolean allowLoadByPercent(int percent) {
        if (percent <= 0 || percent > 100) return false;
        return mRandom.nextInt(100) < percent;
    }

    private String getShowCountKey(String placeName) {
        return placeName + SHOW_COUNT_SUFFIX;
    }

    private String getResetTimeKey(String placeName) {
        return placeName + LAST_RESET_TIME_SUFFIX;
    }

    private void resetShowCountEveryDay(String placeName) {
        long resetTime = Utils.getLong(mContext, getResetTimeKey(placeName), 0);
        long curTime = System.currentTimeMillis();
        if (curTime - resetTime > ONE_DAY) {
            Log.iv(Log.TAG_SDK, "reset load count : " + placeName);
            Utils.putLong(mContext, getShowCountKey(placeName), 0);
            Utils.putLong(mContext, getResetTimeKey(placeName), curTime);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public boolean isExceedMaxReqTimes(String placeName, String sdk, int maxReqTimes) {
        if (maxReqTimes > 0) {
            resetPlatformEventCountIfNeed(mContext);
            String prefKeys = String.format(Locale.ENGLISH, PREF_RECORD_PLACE_REQ_TIMES, placeName, sdk);
            long times = Utils.getLong(mContext, prefKeys, 0);
            Log.iv(Log.TAG_SDK, placeName + " - " + sdk + " req times : [" + times + "/" + maxReqTimes + "]");
            return times >= maxReqTimes;
        }
        return false;
    }

    /**
     * 记录某个平台某个位置的请求次数
     *
     * @param placeName
     * @param sdk
     */
    public void recordRequestTimes(String placeName, String sdk, int maxReqTimes) {
        if (maxReqTimes > 0) {
            String prefKeys = String.format(Locale.ENGLISH, PREF_RECORD_PLACE_REQ_TIMES, placeName, sdk);
            long times = Utils.getLong(mContext, prefKeys, 0);
            times += 1;
            Utils.putLong(mContext, prefKeys, times);
            recordMaxReqTimeKeySet(mContext, prefKeys);
        }
    }

    public long getReqTimes(String placeName, String sdk) {
        String prefKeys = String.format(Locale.ENGLISH, PREF_RECORD_PLACE_REQ_TIMES, placeName, sdk);
        return Utils.getLong(mContext, prefKeys, 0);
    }

    /**
     * 记录打点平台列表, 参数{@link #PREF_RECORD_MAX_REQ_TIME_KEY_SET}
     *
     * @param context
     * @param prefKeys
     */
    private void recordMaxReqTimeKeySet(Context context, String prefKeys) {
        Set<String> sets = Utils.getStringSet(context, PREF_RECORD_MAX_REQ_TIME_KEY_SET);
        Set<String> newSets;
        if (sets != null && !sets.isEmpty()) {
            newSets = new HashSet<>(sets);
        } else {
            newSets = new HashSet<>();
        }
        newSets.add(prefKeys);
        Log.iv(Log.TAG_SDK, "record max req time pref key set : " + newSets);
        Utils.putStringSet(context, PREF_RECORD_MAX_REQ_TIME_KEY_SET, newSets);
    }

    private static void resetPlatformEventCountIfNeed(Context context) {
        long nowDate = Utils.getTodayTime();
        long lastDate = Utils.getLong(context, PREF_RESET_MAX_REQ_TIME_DATETIME, 0);
        if (nowDate != lastDate) {
            Set<String> sets = Utils.getStringSet(context, PREF_RECORD_MAX_REQ_TIME_KEY_SET);
            if (sets != null && !sets.isEmpty()) {
                for (String s : sets) {
                    Utils.putLong(context, s, 0);
                }
            }
            Utils.putLong(context, PREF_RESET_MAX_REQ_TIME_DATETIME, nowDate);
        }
    }
}
