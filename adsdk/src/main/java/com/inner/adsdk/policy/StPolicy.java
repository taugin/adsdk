package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.framework.ActivityMonitor;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StPolicy {
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
        mContext = context;
        mAttrChecker = new AttrChecker(context);
    }

    private Context mContext;
    private StConfig mStConfig;
    private boolean mStShowing = false;
    private AttrChecker mAttrChecker;

    public void init() {
    }

    public void setPolicy(StConfig stConfig) {
        mStConfig = stConfig;
    }

    public void reportStShowing(boolean showing) {
        mStShowing = showing;
    }

    private boolean isStShowing() {
        return mStShowing;
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mStConfig != null) {
            return mStConfig.isEnable();
        }
        return false;
    }

    private boolean checkAdStConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "config not allowed");
            return false;
        }

        if (mStConfig != null && !mAttrChecker.isAttributionAllow(mStConfig.getAttrList())) {
            Log.v(Log.TAG, "attribution not allowed");
            return false;
        }

        if (mStConfig != null && !mAttrChecker.isCountryAllow(mStConfig.getCountryList())) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (mStConfig != null && !mAttrChecker.isMediaSourceAllow(mStConfig.getMediaList())) {
            Log.v(Log.TAG, "mediasource not allowed");
            return false;
        }

        return true;
    }

    private boolean isTopApp() {
        boolean appOnTop = ActivityMonitor.get(mContext).appOnTop();
        boolean isTopApp = Utils.isTopActivy(mContext);
        Log.v(Log.TAG, "appOnTop : " + appOnTop + " , isTopApp : " + isTopApp);
        return appOnTop;
    }

    public boolean isStAllowed() {
        Log.v(Log.TAG, "stconfig : " + mStConfig);
        if (!checkAdStConfig()) {
            return false;
        }

        if (isStShowing()) {
            Log.v(Log.TAG, "st is showing");
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
