package com.inner.adaggs.policy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/3/19.
 */

public class OuterPolicy {
    private static OuterPolicy sOuterPolicy;

    public static OuterPolicy get(Context context) {
        synchronized (OuterPolicy.class) {
            if (sOuterPolicy == null) {
                createInstance(context);
            }
        }
        return sOuterPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (OuterPolicy.class) {
            if (sOuterPolicy == null) {
                sOuterPolicy = new OuterPolicy(context);
            }
        }
    }

    private OuterPolicy(Context context) {
        mContext = context;
    }

    private Context mContext;
    private AdPolicy mAdPolicy;
    private boolean mOuterShowing = false;

    public void init() {
        reportFirstStartUpTime();
    }

    public void setPolicy(AdPolicy adPolicy) {
        mAdPolicy = adPolicy;
    }

    /**
     * 记录ad展示标记
     *
     * @param showing
     */
    public void reportOuterShowing(boolean showing) {
        mOuterShowing = showing;
        if (mOuterShowing) {
            updateLastShowTime();
            reportTotalShowTimes();
        }
    }

    /**
     * 返回ad是否展示
     *
     * @return
     */
    public boolean isOuterShowing() {
        return mOuterShowing;
    }

    /**
     * 更新ad最后展示时间
     */
    private void updateLastShowTime() {
        Utils.putLong(mContext, Constant.PREF_LAST_OUTER_SHOWTIME, System.currentTimeMillis());
    }

    /**
     * 获取ad最后展示时间
     *
     * @return
     */
    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_LAST_OUTER_SHOWTIME, 0);
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
        if (times < 0) {
            times = 0;
        }
        Utils.putLong(mContext, Constant.PREF_OUTER_SHOW_TIMES, times);
    }

    /**
     * 获取ad展示次数
     *
     * @return
     */
    private long getTotalShowTimes() {
        return Utils.getLong(mContext, Constant.PREF_OUTER_SHOW_TIMES, 0);
    }

    private String getAFStatus() {
        return Utils.getString(mContext, "af_status");
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
        if (mAdPolicy != null) {
            return mAdPolicy.isEnable();
        }
        return false;
    }

    /**
     * 延迟间隔是否允许
     *
     * @return
     */
    private boolean isDelayAllow() {
        if (mAdPolicy != null && mAdPolicy.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mAdPolicy.getUpDelay();
        }
        return true;
    }

    /**
     * 展示间隔是否允许
     *
     * @return
     */
    private boolean isIntervalAllow() {
        if (mAdPolicy != null && mAdPolicy.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            return now - last > mAdPolicy.getInterval();
        }
        return true;
    }

    /**
     * 最大展示数是否允许
     *
     * @return
     */
    private boolean isMaxShowAllow() {
        if (mAdPolicy != null && mAdPolicy.getMaxCount() > 0) {
            long times = getTotalShowTimes();
            return times <= mAdPolicy.getMaxCount();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    private boolean isAppVerAllow() {
        if (mAdPolicy != null && mAdPolicy.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mAdPolicy.getMaxVersion();
        }
        return true;
    }

    /**
     * 归因是否允许(自然/非自然)
     *
     * @return
     */
    private boolean isAttributionAllow() {
        String afStatus = getAFStatus();
        if (mAdPolicy != null) {
            List<String> attr = mAdPolicy.getAttrList();
            if (attr != null && !attr.contains(afStatus)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCountryAllow() {
        String country = getCountry();
        Log.v(Log.TAG, "country : " + country);
        if (mAdPolicy != null) {
            List<String> countryList = mAdPolicy.getCountryList();
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

    private boolean isMediaSourceAllow() {
        return true;
    }

    private boolean checkAdOuterConfig() {
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

    public boolean shouldShowAdOuter() {
        if (!checkAdOuterConfig()) {
            return false;
        }

        if (isOuterShowing()) {
            Log.v(Log.TAG, "outer is showing");
            return false;
        }

        if (Utils.isTopActivy(mContext)) {
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
