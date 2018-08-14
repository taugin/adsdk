package com.inner.adsdk.policy;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.ActivityMonitor;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AtPolicy {
    private static final List<String> WHITE_LIST;

    static {
        WHITE_LIST = new ArrayList<String>();
        WHITE_LIST.add("com.android.launcher");
        WHITE_LIST.add("com.google.android.talk");
        WHITE_LIST.add("com.google.android.apps.plus");
        WHITE_LIST.add("com.android.providers.downloads.ui");
        WHITE_LIST.add("com.android.music");
        WHITE_LIST.add("com.android.browser");
        WHITE_LIST.add("com.android.settings");
        WHITE_LIST.add("com.android.camera2");
        WHITE_LIST.add("com.android.deskclock");
        WHITE_LIST.add("com.android.calculator2");
        WHITE_LIST.add("com.android.chrome");
        WHITE_LIST.add("com.android.contacts");
        WHITE_LIST.add("com.android.dialer");
        WHITE_LIST.add("com.android.email");
        WHITE_LIST.add("com.android.gallery3d");
        WHITE_LIST.add("com.android.mms");
        WHITE_LIST.add("com.android.vending");
        WHITE_LIST.add("com.facebook.katana");
        WHITE_LIST.add("com.facebook.orca");
        WHITE_LIST.add("com.google.android.apps.books");
        WHITE_LIST.add("com.google.android.apps.docs");
        WHITE_LIST.add("com.google.android.apps.fireball");
        WHITE_LIST.add("com.google.android.apps.magazines");
        WHITE_LIST.add("com.google.android.apps.maps");
        WHITE_LIST.add("com.google.android.apps.photos");
        WHITE_LIST.add("com.google.android.apps.playconsole");
        WHITE_LIST.add("com.google.android.apps.translate");
        WHITE_LIST.add("com.google.android.calendar");
        WHITE_LIST.add("com.google.android.gms");
        WHITE_LIST.add("com.google.android.gm");
        WHITE_LIST.add("com.google.android.googlequicksearchbox");
        WHITE_LIST.add("com.google.android.music");
        WHITE_LIST.add("com.google.android.play.games");
        WHITE_LIST.add("com.google.android.videos");
        WHITE_LIST.add("com.google.android.youtube");
        WHITE_LIST.add("com.imo.android.imoim");
        WHITE_LIST.add("com.instagram.android");
        WHITE_LIST.add("com.whatsapp");
    }

    private static AtPolicy sAtPolicy;

    public static AtPolicy get(Context context) {
        synchronized (AtPolicy.class) {
            if (sAtPolicy == null) {
                createInstance(context);
            }
        }
        return sAtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (AtPolicy.class) {
            if (sAtPolicy == null) {
                sAtPolicy = new AtPolicy(context);
            }
        }
    }

    private AtPolicy(Context context) {
        mContext = context;
        mAttrChecker = new AttrChecker(context);
    }

    private Context mContext;
    private AtConfig mAtConfig;
    private AttrChecker mAttrChecker;

    public void init() {
    }

    public void setPolicy(AtConfig atConfig) {
        mAtConfig = atConfig;
        if (mAtConfig != null && (mAtConfig.getExcludes() == null || mAtConfig.getExcludes().isEmpty())) {
            mAtConfig.setExcludes(WHITE_LIST);
        }
    }

    public void reportAtShow() {
        Utils.putLong(mContext, Constant.PREF_TT_LAST_TIME, System.currentTimeMillis());
    }

    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_TT_LAST_TIME, 0);
    }

    private long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
    }

    private boolean isDelayAllow() {
        if (mAtConfig != null && mAtConfig.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mAtConfig.getUpDelay();
        }
        return true;
    }

    private boolean isIntervalAllow() {
        if (mAtConfig != null && mAtConfig.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            boolean intervalAllow = now - last > mAtConfig.getInterval();
            Log.v(Log.TAG, "AtConfig.isIntervalAllow now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)) + " , do : " + intervalAllow);
            return intervalAllow;
        }
        return true;
    }

    private boolean isTopApp() {
        boolean appOnTop = ActivityMonitor.get(mContext).appOnTop();
        boolean isTopApp = Utils.isTopActivy(mContext);
        Log.v(Log.TAG, "appOnTop : " + appOnTop + " , isTopApp : " + isTopApp);
        return appOnTop;
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mAtConfig != null) {
            return mAtConfig.isEnable();
        }
        return false;
    }

    private boolean checkAdAtConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "config not allowed");
            return false;
        }

        if (mAtConfig != null && !mAttrChecker.isAttributionAllow(mAtConfig.getAttrList())) {
            Log.v(Log.TAG, "attr not allowed");
            return false;
        }

        if (mAtConfig != null && !mAttrChecker.isCountryAllow(mAtConfig.getCountryList())) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (mAtConfig != null && !mAttrChecker.isMediaSourceAllow(mAtConfig.getMediaList())) {
            Log.v(Log.TAG, "mediasource not allowed");
            return false;
        }

        return true;
    }

    public boolean isAtAllowed() {
        Log.v(Log.TAG, "atConfig : " + mAtConfig);
        if (!checkAdAtConfig()) {
            return false;
        }

        if (!isDelayAllow()) {
            Log.v(Log.TAG, "delay not allowed");
            return false;
        }

        if (!isIntervalAllow()) {
            Log.v(Log.TAG, "interval not allowed");
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

    public boolean isInWhiteList(String pkgname, String className) {
        // exclude launcher
        if (pkgname != null && pkgname.contains("launcher")
                || className != null && className.contains("launcher")) {
            Log.v(Log.TAG, "exclude launcher");
            return true;
        }
        if (mAtConfig != null && mAtConfig.getExcludes() != null && mAtConfig.getExcludes().contains(pkgname)) {
            Log.v(Log.TAG, "white name " + pkgname);
            return true;
        }
        if (mContext != null && TextUtils.equals(pkgname, mContext.getPackageName())) {
            Log.v(Log.TAG, "exclude self");
            return true;
        }
        return false;
    }
}
