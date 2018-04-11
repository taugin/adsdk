package com.inner.adaggs.policy;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.utils.Utils;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/27.
 */

public class PlacePolicy {
    private static PlacePolicy sPlacePolicy;

    public static PlacePolicy get(Context context) {
        synchronized (PlacePolicy.class) {
            if (sPlacePolicy == null) {
                createInstance(context);
            }
        }
        return sPlacePolicy;
    }

    private static void createInstance(Context context) {
        synchronized (PlacePolicy.class) {
            if (sPlacePolicy == null) {
                sPlacePolicy = new PlacePolicy(context);
            }
        }
    }

    private PlacePolicy(Context context) {
        mContext = context;
        mRandom = new Random(System.currentTimeMillis());
    }

    private static final long ONE_DAY = 24 * 60 * 60 * 1000l;
    private static final String LOAD_COUNT_SUFFIX = "_loadcount";
    private static final String LAST_RESET_TIME_SUFFIEX = "_last_reset_time";
    private Context mContext;
    private Random mRandom;

    public void reportAdPlaceLoad(AdPlace adPlace) {
        if (adPlace == null) {
            return;
        }
        String pidName = adPlace.getName();
        if (TextUtils.isEmpty(pidName)) {
            return;
        }
        long loadCount = Utils.getLong(mContext, getLoadCountKey(pidName), 0);
        Utils.putLong(mContext, getLoadCountKey(pidName), loadCount + 1);
        Log.d(Log.TAG, "[" + pidName + "]" + " load count : " + (loadCount + 1));
    }

    public boolean reachMaxCount(String pidName, int maxCount) {
        long loadCount = Utils.getLong(mContext, getLoadCountKey(pidName), 0);
        return loadCount > maxCount;
    }

    public boolean allowAdPlaceLoad(AdPlace adPlace) {
        if (adPlace == null) {
            Log.v(Log.TAG, "adPlace is null");
            return false;
        }
        String pidName = adPlace.getName();
        if (TextUtils.isEmpty(pidName)) {
            Log.v(Log.TAG, "pidName is null");
            return false;
        }
        resetLoadCount(pidName);
        boolean exceedMaxCount = reachMaxCount(pidName, adPlace.getMaxCount());
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

    private String getLoadCountKey(String pidName) {
        return pidName + LOAD_COUNT_SUFFIX;
    }

    private String getResetTimeKey(String pidName) {
        return pidName + LAST_RESET_TIME_SUFFIEX;
    }

    private void resetLoadCount(String pidName) {
        long resetTime = Utils.getLong(mContext, getResetTimeKey(pidName), 0);
        long curTime = System.currentTimeMillis();
        if (curTime - resetTime > ONE_DAY) {
            Log.v(Log.TAG, "reset load count : " + pidName);
            Utils.putLong(mContext, getLoadCountKey(pidName), 0);
            Utils.putLong(mContext, getResetTimeKey(pidName), curTime);
        }
    }
}
