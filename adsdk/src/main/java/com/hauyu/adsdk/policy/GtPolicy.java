package com.hauyu.adsdk.policy;

import android.content.Context;

import com.hauyu.adsdk.config.GtConfig;
import com.hauyu.adsdk.stat.StatImpl;
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

    @Override
    protected void reportShowTimesOneday(Context context, int times) {
        StatImpl.get().reportAdOuterShowTimes(mContext, getType(), times);
    }

    public boolean isGtAllowed() {
        logv("gt : " + mGtConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            logv("app is on the top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            logv("screen is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            logv("screen is not on");
            return false;
        }
        return true;
    }
}
