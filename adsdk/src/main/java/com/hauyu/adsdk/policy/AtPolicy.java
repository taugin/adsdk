package com.hauyu.adsdk.policy;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.config.AtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.ActivityMonitor;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

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

    private long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
    }

    public void setPolicy(AtConfig atConfig) {
        mAtConfig = atConfig;
        if (mAtConfig != null && (mAtConfig.getExcludes() == null || mAtConfig.getExcludes().isEmpty())) {
            mAtConfig.setExcludes(WHITE_LIST);
        }
    }

    /**
     * 记录AT展示
     */
    public void reportAtShow() {
        Utils.putLong(mContext, Constant.PREF_AT_LAST_SHOWTIME, System.currentTimeMillis());
        reportTotalShowTimes();
    }

    /**
     * 记录每天的首次展示时间
     */
    private void recordFirstShowTime() {
        long times = getTotalShowTimes();
        if (times == 1) {
            Utils.putLong(mContext, Constant.PREF_AT_FIRST_SHOWTIME, System.currentTimeMillis());
        }
    }

    /**
     * 记录ad展示次数
     */
    private void reportTotalShowTimes() {
        long times = getTotalShowTimes();
        times += 1;
        if (times <= 0) {
            times = 1;
        }
        Utils.putLong(mContext, Constant.PREF_AT_TOTAL_SHOWTIMES, times);
        recordFirstShowTime();
    }

    /**
     * 重置当天的总展示次数
     */
    private void resetTotalShowTimes() {
        Log.d(Log.TAG, "reset total show times");
        Utils.putLong(mContext, Constant.PREF_AT_TOTAL_SHOWTIMES, 0);
    }

    /**
     * 获取ad展示次数
     *
     * @return
     */
    private long getTotalShowTimes() {
        return Utils.getLong(mContext, Constant.PREF_AT_TOTAL_SHOWTIMES, 0);
    }

    /**
     * 获取最后一次展示时间
     * @return
     */
    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_AT_LAST_SHOWTIME, 0);
    }

    /**
     * 24小时清除计数
     */
    private void resetTotalShowIfNeed() {
        long now = System.currentTimeMillis();
        long lastDay = Utils.getLong(mContext, Constant.PREF_AT_FIRST_SHOWTIME, now);
        Log.v(Log.TAG, "AtConfig.resetTotalShowIfNeed now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(lastDay)));
        if (now - lastDay > Constant.ONE_DAY_TIME) {
            resetTotalShowTimes();
        }
    }

    /**
     * 最大展示数是否允许
     *
     * @return
     */
    private boolean isMaxShowAllow() {
        resetTotalShowIfNeed();
        if (mAtConfig != null && mAtConfig.getMaxCount() > 0) {
            long times = getTotalShowTimes();
            Log.d(Log.TAG, "total show times : " + times + " , mc : " + mAtConfig.getMaxCount());
            // 此处<=的逻辑会导致最大展示次数多1次
            return times <= mAtConfig.getMaxCount();
        }
        return true;
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

        if (!isMaxShowAllow()) {
            Log.v(Log.TAG, "max show not allowed");
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

    public boolean isShowOnFirstPage() {
        if (mAtConfig != null) {
            return mAtConfig.isShowOnFirstPage();
        }
        return false;
    }
}
