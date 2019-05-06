package com.hauyu.adsdk.policy;

import android.content.Context;

import com.hauyu.adsdk.common.BasePolicy;
import com.hauyu.adsdk.config.GtConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtPolicy extends BasePolicy {
    private static GtPolicy sGtPolicy;

    public static GtPolicy get(Context context) {
        synchronized (GtPolicy.class) {
            if (sGtPolicy == null) {
                createInstance(context);
            }
        }
        return sGtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (GtPolicy.class) {
            if (sGtPolicy == null) {
                sGtPolicy = new GtPolicy(context);
            }
        }
    }

    private GtPolicy(Context context) {
        super(context, "gt");
        mAttrChecker = new AttrChecker(context);
    }

    private GtConfig mGtConfig;

    public void init() {
    }

    public void setPolicy(GtConfig gtConfig) {
        super.setPolicy(gtConfig);
        mGtConfig = gtConfig;
    }

    public boolean isGtAllowed() {
        Log.iv(Log.TAG, "gt : " + mGtConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            Log.iv(Log.TAG, "app is on the top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.iv(Log.TAG, "screen is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.iv(Log.TAG, "screen is not on");
            return false;
        }
        return true;
    }
}
