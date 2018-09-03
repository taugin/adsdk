package com.inner.adsdk.policy;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

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

    public void reportAdPlaceShow(AdPlace adPlace) {
        if (adPlace == null) {
            Log.v(Log.TAG, "adPlace == null");
            return;
        }
        String pidName = adPlace.getName();
        if (TextUtils.isEmpty(pidName)) {
            Log.v(Log.TAG, "pidName == null");
            return;
        }
        long loadCount = Utils.getLong(mContext, getShowCountKey(pidName), 0);
        Utils.putLong(mContext, getShowCountKey(pidName), loadCount + 1);
        Log.d(Log.TAG, "[" + pidName + "]" + " show count : " + (loadCount + 1));
    }

    public boolean reachMaxShowCount(String pidName, int maxCount) {
        long loadCount = Utils.getLong(mContext, getShowCountKey(pidName), 0);
        return loadCount > maxCount;
    }

    public boolean allowAdPlaceLoad(AdPlace adPlace) {
        if (adPlace == null) {
            Log.v(Log.TAG, "place is null");
            return false;
        }
        String pidName = adPlace.getName();
        if (TextUtils.isEmpty(pidName)) {
            Log.v(Log.TAG, "name is null");
            return false;
        }
        resetShowCountEveryDay(pidName);
        boolean exceedMaxCount = reachMaxShowCount(pidName, adPlace.getMaxCount());
        if (exceedMaxCount) {
            long maxCount = adPlace.getMaxCount();
            Log.v(Log.TAG, "[" + pidName + "]" + " exceed max count " + maxCount);
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

    private String getShowCountKey(String pidName) {
        return pidName + SHOW_COUNT_SUFFIX;
    }

    private String getResetTimeKey(String pidName) {
        return pidName + LAST_RESET_TIME_SUFFIEX;
    }

    private void resetShowCountEveryDay(String pidName) {
        long resetTime = Utils.getLong(mContext, getResetTimeKey(pidName), 0);
        long curTime = System.currentTimeMillis();
        if (curTime - resetTime > ONE_DAY) {
            Log.v(Log.TAG, "reset load count : " + pidName);
            Utils.putLong(mContext, getShowCountKey(pidName), 0);
            Utils.putLong(mContext, getResetTimeKey(pidName), curTime);
        }
    }
}
