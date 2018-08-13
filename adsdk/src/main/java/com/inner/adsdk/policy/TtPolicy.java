package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.config.TtConfig;
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

public class TtPolicy {
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
        if (mTtConfig != null && (mTtConfig.getExcludes() == null || mTtConfig.getExcludes().isEmpty())) {
            mTtConfig.setExcludes(WHITE_LIST);
        }
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
        Log.v(Log.TAG, "tt");
        return true;
    }

    public boolean isInWhiteList(String pkgname, String className) {
        // exclude launcher
        if (pkgname != null && pkgname.contains("launcher")
                || className != null && className.contains("launcher")) {
            Log.v(Log.TAG, "exclude launcher");
            return true;
        }
        if (mTtConfig != null && mTtConfig.getExcludes() != null && mTtConfig.getExcludes().contains(pkgname)) {
            Log.v(Log.TAG, "white name " + pkgname);
            return true;
        }
        return false;
    }
}
