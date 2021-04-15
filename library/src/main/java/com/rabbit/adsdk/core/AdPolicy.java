package com.rabbit.adsdk.core;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.Random;

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
    private static final String LAST_RESET_TIME_SUFFIEX = "_last_reset_time";
    private Context mContext;
    private Random mRandom;

    public void reportAdPlaceShow(String originPlaceName, AdPlace adPlace) {
        if (adPlace == null) {
            Log.v(Log.TAG, "adPlace == null");
            return;
        }
        String placeName = originPlaceName;
        if (TextUtils.isEmpty(originPlaceName)) {
            placeName = adPlace.getName();
        }
        if (TextUtils.isEmpty(placeName)) {
            Log.v(Log.TAG, "placeName == null");
            return;
        }
        long loadCount = Utils.getLong(mContext, getShowCountKey(placeName), 0);
        Utils.putLong(mContext, getShowCountKey(placeName), loadCount + 1);
        Log.d(Log.TAG, "[" + placeName + "]" + " show count : " + (loadCount + 1));
    }

    public boolean reachMaxShowCount(String placeName, int maxCount) {
        long loadCount = Utils.getLong(mContext, getShowCountKey(placeName), 0);
        return loadCount > maxCount;
    }

    public boolean allowAdPlaceLoad(AdPlace adPlace) {
        if (adPlace == null) {
            Log.v(Log.TAG, "adPlace is null");
            return false;
        }
        String placeName = adPlace.getName();
        if (TextUtils.isEmpty(placeName)) {
            Log.v(Log.TAG, "placeName is null");
            return false;
        }
        resetShowCountEveryDay(placeName);
        boolean exceedMaxCount = reachMaxShowCount(placeName, adPlace.getMaxCount());
        if (exceedMaxCount) {
            long maxCount = adPlace.getMaxCount();
            Log.v(Log.TAG, "[" + placeName + "]" + " exceed max count " + maxCount);
            return false;
        }
        boolean allowByPercent = allowLoadByPercent(adPlace.getPercent());
        if (!allowByPercent) {
            Log.v(Log.TAG, "percent not allow");
            return false;
        }
        return true;
    }

    public boolean allowLoadByPercent(int percent) {
        if (percent <= 0 || percent > 100) return false;
        return mRandom.nextInt(100)  < percent;
    }

    private String getShowCountKey(String placeName) {
        return placeName + SHOW_COUNT_SUFFIX;
    }

    private String getResetTimeKey(String placeName) {
        return placeName + LAST_RESET_TIME_SUFFIEX;
    }

    private void resetShowCountEveryDay(String placeName) {
        long resetTime = Utils.getLong(mContext, getResetTimeKey(placeName), 0);
        long curTime = System.currentTimeMillis();
        if (curTime - resetTime > ONE_DAY) {
            Log.v(Log.TAG, "reset load count : " + placeName);
            Utils.putLong(mContext, getShowCountKey(placeName), 0);
            Utils.putLong(mContext, getResetTimeKey(placeName), curTime);
        }
    }
}
