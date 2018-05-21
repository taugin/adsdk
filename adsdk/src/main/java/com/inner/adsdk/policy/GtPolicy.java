package com.inner.adsdk.policy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.ActivityMonitor;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtPolicy {
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
        mContext = context;
    }

    private Context mContext;
    private GtConfig mGtConfig;
    private boolean mGtShowing = false;

    public void init() {
        reportFirstStartUpTime();
    }

    public void setPolicy(GtConfig gtConfig) {
        mGtConfig = gtConfig;
    }

    /**
     * 记录ad展示标记
     *
     * @param showing
     */
    public void reportGtShowing(boolean showing) {
        mGtShowing = showing;
        if (mGtShowing) {
            updateLastShowTime();
            reportTotalShowTimes();
        }
    }

    /**
     * 返回ad是否展示
     *
     * @return
     */
    public boolean isGtShowing() {
        return mGtShowing;
    }

    /**
     * 更新ad最后展示时间
     */
    private void updateLastShowTime() {
        Utils.putLong(mContext, Constant.PREF_LAST_GT_SHOWTIME, System.currentTimeMillis());
    }

    /**
     * 获取ad最后展示时间
     *
     * @return
     */
    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_LAST_GT_SHOWTIME, 0);
    }

    /**
     * 记录应用首次启动时间
     */
    private void reportFirstStartUpTime() {
        if (Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0) <= 0) {
            Utils.putLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, System.currentTimeMillis());
        }
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    private long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
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
        Utils.putLong(mContext, Constant.PREF_GT_SHOW_TIMES, times);
        recordFirstShowTime();
    }

    private void resetTotalShowTimes() {
        Log.d(Log.TAG, "reset total show times");
        Utils.putLong(mContext, Constant.PREF_GT_SHOW_TIMES, 0);
    }

    /**
     * 获取ad展示次数
     *
     * @return
     */
    private long getTotalShowTimes() {
        return Utils.getLong(mContext, Constant.PREF_GT_SHOW_TIMES, 0);
    }

    private String getAFStatus() {
        return Utils.getString(mContext, Constant.AF_STATUS);
    }

    private String getMediaSource() {
        return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
    }

    private String getCountry() {
        String country = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = mContext.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = mContext.getResources().getConfiguration().locale;
            }
            country = locale.getCountry();
        } catch(Exception e) {
        }
        if (!TextUtils.isEmpty(country)) {
            country = country.toLowerCase(Locale.getDefault());
        }
        return country;
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mGtConfig != null) {
            return mGtConfig.isEnable();
        }
        return false;
    }

    /**
     * 延迟间隔是否允许
     *
     * @return
     */
    private boolean isDelayAllow() {
        if (mGtConfig != null && mGtConfig.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mGtConfig.getUpDelay();
        }
        return true;
    }

    /**
     * 展示间隔是否允许
     *
     * @return
     */
    private boolean isIntervalAllow() {
        if (mGtConfig != null && mGtConfig.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            Log.v(Log.TAG, "GtConfig.isIntervalAllow now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)));
            return now - last > mGtConfig.getInterval();
        }
        return true;
    }

    private void recordFirstShowTime() {
        long times = getTotalShowTimes();
        if (times == 1) {
            Utils.putLong(mContext, Constant.PREF_FIRST_SHOW_TIME_ONEDAY, System.currentTimeMillis());
        }
    }

    /**
     * 24小时清除计数
     */
    private void resetTotalShowIfNeed() {
        long now = System.currentTimeMillis();
        long lastDay = Utils.getLong(mContext, Constant.PREF_FIRST_SHOW_TIME_ONEDAY, now);
        Log.v(Log.TAG, "GtConfig.resetTotalShowIfNeed now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(lastDay)));
        if (now - lastDay > Constant.ONE_DAY_TIME) {
            int times = (int) getTotalShowTimes();
            if (times > 0) {
                StatImpl.get().reportAdGtShowTimes(mContext, times);
            }
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
        if (mGtConfig != null && mGtConfig.getMaxCount() > 0) {
            long times = getTotalShowTimes();
            Log.d(Log.TAG, "total show times : " + times + " , mc : " + mGtConfig.getMaxCount());
            // 此处<=的逻辑会导致最大展示次数多1次
            return times <= mGtConfig.getMaxCount();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    private boolean isAppVerAllow() {
        if (mGtConfig != null && mGtConfig.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mGtConfig.getMaxVersion();
        }
        return true;
    }

    /**
     * 归因是否允许(自然/非自然)
     *
     * @return
     */
    private boolean isAttributionAllow() {
        boolean disableAttribution = android.util.Log.isLoggable("disable_attribute", android.util.Log.VERBOSE);
        Log.v(Log.TAG, "da : " + disableAttribution);
        if (disableAttribution) {
            return true;
        }
        String afStatus = getAFStatus();
        Log.d(Log.TAG, "af_status : " + afStatus);
        if (mGtConfig != null) {
            List<String> attr = mGtConfig.getAttrList();
            if (attr != null && !attr.contains(afStatus)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCountryAllow() {
        String country = getCountry();
        Log.v(Log.TAG, "country : " + country);
        if (mGtConfig != null) {
            List<String> countryList = mGtConfig.getCountryList();
            if (countryList != null && !countryList.isEmpty()) {
                List<String> includeCountries = new ArrayList<String>();
                List<String> excludeCountries = new ArrayList<String>();
                for (String s : countryList) {
                    if (s != null) {
                        if (s.startsWith("!")) {
                            excludeCountries.add(s);
                        } else {
                            includeCountries.add(s);
                        }
                    }
                }
                if (includeCountries.size() > 0) {
                    // 包含列表如果不包含当前国家，则返回false
                    if (!includeCountries.contains(country)) {
                        return false;
                    }
                } else if (excludeCountries.size() > 0) {
                    // 排斥列表如果包含当前国家，则返回
                    if (excludeCountries.contains("!" + country)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 判断媒体源是否允许
     * @return
     */
    private boolean isMediaSourceAllow() {
        String mediaSource = getMediaSource();
        Log.d(Log.TAG, "media_source : " + mediaSource);
        if (mGtConfig != null) {
            List<String> mediaList = mGtConfig.getMediaList();
            if (mediaList != null && !mediaList.isEmpty()) {
                List<String> includeMs = new ArrayList<String>();
                List<String> excludeMs = new ArrayList<String>();
                for (String s : mediaList) {
                    if (s != null) {
                        if (s.startsWith("!")) {
                            excludeMs.add(s);
                        } else {
                            includeMs.add(s);
                        }
                    }
                }
                if (includeMs.size() > 0) {
                    // 包含列表如果不包含当前媒体源，则返回false
                    if (!includeMs.contains(mediaSource)) {
                        return false;
                    }
                } else if (excludeMs.size() > 0) {
                    // 排斥列表如果包含当前媒体源，则返回false
                    if (excludeMs.contains("!" + mediaSource)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isTopApp() {
        boolean appOnTop = ActivityMonitor.get(mContext).appOnTop();
        boolean isTopApp = Utils.isTopActivy(mContext);
        Log.v(Log.TAG, "appOnTop : " + appOnTop + " , isTopApp : " + isTopApp);
        return appOnTop;
    }

    private boolean checkAdGtConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "config not allowed");
            return false;
        }

        if (!isAttributionAllow()) {
            Log.v(Log.TAG, "attribution not allowed");
            return false;
        }

        if (!isCountryAllow()) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (!isMediaSourceAllow()) {
            Log.v(Log.TAG, "mediasource not allowed");
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

        if (!isMaxShowAllow()) {
            Log.v(Log.TAG, "maxshow not allowed");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.v(Log.TAG, "maxver not allowed");
            return false;
        }
        return true;
    }

    public boolean shouldShowAdGt() {
        Log.v(Log.TAG, "gtconfig : " + mGtConfig);
        if (!checkAdGtConfig()) {
            return false;
        }

        if (isGtShowing()) {
            Log.v(Log.TAG, "gt is showing");
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
