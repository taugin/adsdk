package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StPolicy extends BasePolicy {
    private static StPolicy sStPolicy;

    public static StPolicy get(Context context) {
        synchronized (StPolicy.class) {
            if (sStPolicy == null) {
                createInstance(context);
            }
        }
        return sStPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (StPolicy.class) {
            if (sStPolicy == null) {
                sStPolicy = new StPolicy(context);
            }
        }
    }

    private StPolicy(Context context) {
        super(context, "st");
    }

    private StConfig mStConfig;

    public void init() {
    }

    public void setPolicy(StConfig stConfig) {
        super.setPolicy(stConfig);
        mStConfig = stConfig;
    }

    @Override
    protected void reportShowTimesOneday(Context context, int times) {
        StatImpl.get().reportAdOuterShowTimes(mContext, getType(), times);
    }

    public boolean isStAllowed() {
        Log.v(Log.TAG, "stconfig : " + mStConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            Log.v(Log.TAG, "app is on the top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.v(Log.TAG, "screen is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.v(Log.TAG, "screen is not on");
            return false;
        }
        return true;
    }
}
