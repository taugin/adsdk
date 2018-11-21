package com.hauyu.adsdk.policy;

import android.content.Context;

import com.hauyu.adsdk.config.CtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class CtPolicy extends BasePolicy {
    private static CtPolicy sCtPolicy;

    public static CtPolicy get(Context context) {
        synchronized (CtPolicy.class) {
            if (sCtPolicy == null) {
                createInstance(context);
            }
        }
        return sCtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (CtPolicy.class) {
            if (sCtPolicy == null) {
                sCtPolicy = new CtPolicy(context);
            }
        }
    }

    private CtPolicy(Context context) {
        super(context, "ct");
    }

    private CtConfig mCtConfig;

    public void init() {
    }

    public void setPolicy(CtConfig ctConfig) {
        super.setPolicy(ctConfig);
        mCtConfig = ctConfig;
    }

    public void disableMonitor() {
        Utils.putLong(mContext, Constant.PREF_CHARGE_MONITOR, System.currentTimeMillis());
    }

    public boolean allowDisableMonitor() {
        if (mCtConfig != null) {
            return mCtConfig.getDisableInterval() > 0;
        }
        return false;
    }

    private boolean isDisable() {
        if (mCtConfig != null) {
            long now = System.currentTimeMillis();
            long dis = Utils.getLong(mContext, Constant.PREF_CHARGE_MONITOR);
            long inv = mCtConfig.getDisableInterval();
            return now - dis < inv;
        }
        return false;
    }

    public boolean isCtAllowed() {
        Log.v(Log.TAG, "ct : " + mCtConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isDisable()) {
            Log.v(Log.TAG, "user disable");
            return false;
        }

        return true;
    }
}
