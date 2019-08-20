package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.scconfig.StConfig;
import com.bacad.ioc.gsb.common.BasePolicy;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

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

    public boolean isStAllowed() {
        Log.iv(Log.TAG, "s_value : " + mStConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            Log.iv(Log.TAG, "app is on top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.iv(Log.TAG, "app is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.iv(Log.TAG, "app is not on");
            return false;
        }
        return true;
    }
}
