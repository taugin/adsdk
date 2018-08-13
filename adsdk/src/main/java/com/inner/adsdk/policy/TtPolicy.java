package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.config.TtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.Date;

/**
 * Created by Administrator on 2018-8-10.
 */

public class TtPolicy {
    private static TtPolicy sTtPolicy;

    public static TtPolicy get(Context context) {
        synchronized (TtPolicy.class) {
            if (sTtPolicy == null) {
                createInstance(context);
            }
        }
        return sTtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (TtPolicy.class) {
            if (sTtPolicy == null) {
                sTtPolicy = new TtPolicy(context);
            }
        }
    }

    private TtPolicy(Context context) {
        mContext = context;
        mAttrChecker = new AttrChecker(context);
    }

    private Context mContext;
    private TtConfig mTtConfig;
    private AttrChecker mAttrChecker;

    public void init() {
    }

    public void setPolicy(TtConfig ttConfig) {
        mTtConfig = ttConfig;
    }

    public void reportTtShow() {
        Utils.putLong(mContext, Constant.PREF_TT_LAST_TIME, System.currentTimeMillis());
    }

    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_TT_LAST_TIME, 0);
    }

    private boolean isIntervalAllow() {
        if (mTtConfig != null && mTtConfig.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            boolean intervalAllow = now - last > mTtConfig.getInterval();
            Log.v(Log.TAG, "TtConfig.isIntervalAllow now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)) + " , do : " + intervalAllow);
            return intervalAllow;
        }
        return true;
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mTtConfig != null) {
            return mTtConfig.isEnable();
        }
        return false;
    }

    private boolean checkAdFtConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "config not allowed");
            return false;
        }

        if (mTtConfig != null && !mAttrChecker.isAttributionAllow(mTtConfig.getAttrList())) {
            Log.v(Log.TAG, "attribution not allowed");
            return false;
        }

        if (mTtConfig != null && !mAttrChecker.isCountryAllow(mTtConfig.getCountryList())) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (mTtConfig != null && !mAttrChecker.isMediaSourceAllow(mTtConfig.getMediaList())) {
            Log.v(Log.TAG, "mediasource not allowed");
            return false;
        }

        return true;
    }

    public boolean isTtAllowed() {
        Log.v(Log.TAG, "ttConfig : " + mTtConfig);
        if (!checkAdFtConfig()) {
            return false;
        }
        if (!isIntervalAllow()) {
            return false;
        }
        Log.v(Log.TAG, "tt");
        return true;
    }

    public boolean isInWhiteList(String pkgname, String className) {
        // exclude launcher
        if (pkgname != null && pkgname.contains("launcher")
                || className != null && className.contains("launcher")) {
            Log.v(Log.TAG, "launcher not show");
            return true;
        }
        return false;
    }
}
