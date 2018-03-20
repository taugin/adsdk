package com.inner.adaggs.manager;

import android.content.Context;

import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.utils.Utils;

/**
 * Created by Administrator on 2018/3/19.
 */

public class PolicyManager {
    private static PolicyManager sPolicyManager;

    public static PolicyManager get(Context context) {
        synchronized (PolicyManager.class) {
            if (sPolicyManager == null) {
                createInstance(context);
            }
        }
        return sPolicyManager;
    }

    private static void createInstance(Context context) {
        synchronized (PolicyManager.class) {
            if (sPolicyManager == null) {
                sPolicyManager = new PolicyManager(context);
            }
        }
    }

    private PolicyManager(Context context) {
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
     * @return
     */
    private long getTotalShowTimes() {
        return Utils.getLong(mContext, Constant.PREF_OUTER_SHOW_TIMES, 0);
    }

    /**
     * 配置是否允许
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
     * @return
     */
    private boolean isMaxShowAllow() {
        if (mAdPolicy != null && mAdPolicy.getMaxShow() > 0) {
            long times = getTotalShowTimes();
            return times <= mAdPolicy.getMaxShow();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
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
     * 归因是否允许, 基本包含 来源、国家、属性(自然/非自然)
     * @return
     */
    private boolean isAttributionAllow() {
        return true;
    }

    private boolean checkAdOuterConfig() {
        if (!isConfigAllow()) {
            Log.d(Log.TAG, "config not allowed");
            return false;
        }

        if (!isAttributionAllow()) {
            Log.d(Log.TAG, "attribution not allowed");
            return false;
        }

        if (!isDelayAllow()) {
            Log.d(Log.TAG, "delay not allowed");
            return false;
        }

        if (!isIntervalAllow()) {
            Log.d(Log.TAG, "interval not allowed");
            return false;
        }

        if (!isMaxShowAllow()) {
            Log.d(Log.TAG, "maxshow not allowed");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.d(Log.TAG, "maxver not allowed");
            return false;
        }
        return true;
    }

    public boolean shouldShowAdOuter() {
        if (!checkAdOuterConfig()) {
            return false;
        }

        if (isOuterShowing()) {
            Log.d(Log.TAG, "outer is showing");
            return false;
        }

        if (Utils.isTopActivy(mContext)) {
            Log.d(Log.TAG, "app is on the top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.d(Log.TAG, "screen is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.d(Log.TAG, "screen is not on");
            return false;
        }
        return true;
    }
}
