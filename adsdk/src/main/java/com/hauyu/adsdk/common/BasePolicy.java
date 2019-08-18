package com.hauyu.adsdk.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.core.AttrChecker;
import com.hauyu.adsdk.framework.ActivityMonitor;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;
import com.hauyu.adsdk.utils.Utils;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018-11-19.
 */

public class BasePolicy implements Handler.Callback {

    private static final String PREF_LAST_SHOWTIME = "pref_%s_last_showtime";
    private static final String PREF_TOTAL_SHOWTIMES = "pref_%s_total_showtimes";
    private static final String PREF_FIRST_SHOWTIME_ONEDAY = "pref_%s_first_showtime_oneday";
    private static final String PREF_REQUEST_TIME = "pref_%s_request_time";

    protected AttrChecker mAttrChecker;
    protected Context mContext;
    protected BaseConfig mBaseConfig;
    private String mType;
    private boolean mLoading = false;
    private Handler mHandler;
    private static final String LAST_SCENE_TIME = "pref_last_scene_time";
    public static final String LAST_SCENE_TYPE = "pref_last_scene_type";

    protected BasePolicy(Context context, String type) {
        mContext = context;
        mType = type;
        mHandler = new Handler(this);
        mAttrChecker = new AttrChecker(context);
    }

    protected void setPolicy(BaseConfig baseConfig) {
        mBaseConfig = baseConfig;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == getMsgWhat()) {
            mLoading = false;
        }
        return false;
    }

    public String getType() {
        return mType;
    }

    private long getTimeout() {
        long timeOut = 0;
        if (mBaseConfig != null) {
            timeOut = mBaseConfig.getTimeOut();
        }
        return timeOut;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    public void setLoading(boolean loading) {
        mLoading = loading;
        if (mLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(getMsgWhat());
                Log.iv(Log.TAG, mType + " send loading timeout : " + getTimeout());
                mHandler.sendEmptyMessageDelayed(getMsgWhat(), getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(getMsgWhat());
                Log.iv(Log.TAG, mType + " remove loading timeout");
            }
        }
    }

    public boolean isLoading() {
        return mLoading;
    }

    /**
     * 记录开始请求时间
     */
    public void updateLastFailTime() {
        Utils.putLong(mContext, getPrefKey(PREF_REQUEST_TIME), System.currentTimeMillis());
    }

    /**
     * 判断是否符合最小时间间隔
     *
     * @return
     */
    public boolean isMatchMinInterval() {
        if (mBaseConfig != null) {
            long now = System.currentTimeMillis();
            long lastReqTime = Utils.getLong(mContext, getPrefKey(PREF_REQUEST_TIME), 0);
            long leftTime = mBaseConfig.getMinInterval() - (now - lastReqTime);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " mi : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - lastReqTime >= mBaseConfig.getMinInterval();
        }
        return true;
    }

    /**
     * 记录ad展示标记
     *
     * @param showing
     */
    public void reportShowing(boolean showing) {
        if (showing) {
            updateLastShowTime();
            reportTotalShowTimes();
            reportLastScene();
        }
    }

    private void reportLastScene() {
        Utils.putLong(mContext, LAST_SCENE_TIME, System.currentTimeMillis());
        Utils.putString(mContext, LAST_SCENE_TYPE, getType());
    }

    /**
     * 获取原生配置比率
     * @return
     */
    public int getNTRate() {
        int nTRate = 0;
        if (mBaseConfig != null) {
            nTRate = mBaseConfig.getNtr();
        }
        if (nTRate < 0) {
            nTRate = 0;
        }
        if (nTRate > 100) {
            nTRate = 100;
        }
        return nTRate;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 更新ad最后展示时间
     */
    private void updateLastShowTime() {
        Utils.putLong(mContext, getPrefKey(PREF_LAST_SHOWTIME), System.currentTimeMillis());
    }

    /**
     * 获取ad最后展示时间
     *
     * @return
     */
    private long getLastShowTime() {
        return Utils.getLong(mContext, getPrefKey(PREF_LAST_SHOWTIME), 0);
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    private long getFirstStartUpTime() {
        return AdReceiver.get(mContext).getFirstStartUpTime();
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
        Utils.putLong(mContext, getPrefKey(PREF_TOTAL_SHOWTIMES), times);
        recordFirstShowTime();
    }

    private void resetTotalShowTimes() {
        Log.d(Log.TAG, "reset total show times");
        Utils.putLong(mContext, getPrefKey(PREF_TOTAL_SHOWTIMES), 0);
    }

    /**
     * 获取ad展示次数
     *
     * @return
     */
    private long getTotalShowTimes() {
        return Utils.getLong(mContext, getPrefKey(PREF_TOTAL_SHOWTIMES), 0);
    }


    private void recordFirstShowTime() {
        long times = getTotalShowTimes();
        if (times == 1) {
            Utils.putLong(mContext, getPrefKey(PREF_FIRST_SHOWTIME_ONEDAY), System.currentTimeMillis());
        }
    }

    /**
     * 24小时清除计数
     */
    private void resetTotalShowIfNeed() {
        long now = System.currentTimeMillis();
        long lastDay = Utils.getLong(mContext, getPrefKey(PREF_FIRST_SHOWTIME_ONEDAY), now);
        Log.iv(Log.TAG, mType + " reset total show now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(lastDay)));
        if (now - lastDay > Constant.ONE_DAY_TIME) {
            int times = (int) getTotalShowTimes();
            if (times > 0) {
                reportShowTimesOneday(mContext, times);
            }
            resetTotalShowTimes();
        }
    }

    private void reportShowTimesOneday(Context context, int times) {
        EventImpl.get().reportAdOuterShowTimes(mContext, getType(), times);
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    protected boolean isConfigAllow() {
        if (mBaseConfig != null) {
            return mBaseConfig.isEnable();
        }
        return false;
    }

    protected boolean isAttrAllow() {
        if (mBaseConfig != null && !mAttrChecker.isAttributionAllow(mBaseConfig.getAttrList())) {
            Log.iv(Log.TAG, "attr not allowed");
            return false;
        }

        if (mBaseConfig != null && !mAttrChecker.isCountryAllow(mBaseConfig.getCountryList())) {
            Log.iv(Log.TAG, "country not allowed");
            return false;
        }

        if (mBaseConfig != null && !mAttrChecker.isMediaSourceAllow(mBaseConfig.getMediaList())) {
            Log.iv(Log.TAG, "ms not allowed");
            return false;
        }

        if (mBaseConfig != null && !mAttrChecker.isVersionAllow(mBaseConfig.getVerList())) {
            Log.iv(Log.TAG, "ver not allowed");
            return false;
        }
        return true;
    }

    /**
     * 延迟间隔是否允许
     *
     * @return
     */
    protected boolean isDelayAllow() {
        if (mBaseConfig != null && mBaseConfig.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mBaseConfig.getUpDelay();
        }
        return true;
    }

    /**
     * 展示间隔是否允许
     *
     * @return
     */
    protected boolean isIntervalAllow() {
        if (mBaseConfig != null && mBaseConfig.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            long leftTime = mBaseConfig.getInterval() - (now - last);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " i : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - last > mBaseConfig.getInterval();
        }
        return true;
    }

    /**
     * 最大展示数是否允许
     *
     * @return
     */
    protected boolean isMaxShowAllow() {
        resetTotalShowIfNeed();
        if (mBaseConfig != null && mBaseConfig.getMaxCount() > 0) {
            long times = getTotalShowTimes();
            Log.d(Log.TAG, "total show times : " + times + " , mc : " + mBaseConfig.getMaxCount());
            // 此处<=的逻辑会导致最大展示次数多1次
            return times <= mBaseConfig.getMaxCount();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    protected boolean isAppVerAllow() {
        if (mBaseConfig != null && mBaseConfig.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mBaseConfig.getMaxVersion();
        }
        return true;
    }


    protected boolean isTopApp() {
        boolean appOnTop = ActivityMonitor.get(mContext).appOnTop();
        return appOnTop;
    }

    protected boolean matchInstallTime() {
        if (mBaseConfig != null) {
            long configInstallTime = mBaseConfig.getConfigInstallTime();
            long firstInstallTime = getFirstInstallTime();
            String cit = configInstallTime > 0 ? Constant.SDF_1.format(new Date(configInstallTime)) : "0";
            String fit = firstInstallTime > 0 ? Constant.SDF_1.format(new Date(firstInstallTime)) : "0";
            Log.iv(Log.TAG, "cit : " + cit + " , fit : " + fit);
            if (configInstallTime <= 0 || firstInstallTime <= 0) {
                return true;
            }
            return firstInstallTime <= configInstallTime;
        }
        return true;
    }

    private long getFirstInstallTime() {
        long firstInstallTime = 0;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            firstInstallTime = packageInfo.firstInstallTime;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return firstInstallTime;
    }

    public long getLastSceneTime() {
        return Utils.getLong(mContext, LAST_SCENE_TIME);
    }

    public String getLastSceneType() {
        return Utils.getString(mContext, LAST_SCENE_TYPE);
    }

    public boolean isShowBottomActivity() {
        if (mBaseConfig != null){
            return mBaseConfig.isShowBottomActivity();
        }
        return false;
    }

    protected boolean isScreenOrientationAllow() {
        if (mBaseConfig != null) {
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            try {
                orientation = mContext.getResources().getConfiguration().orientation;
            } catch (Exception e) {
            }
            int configOrientation = mBaseConfig.getScreenOrientation();
            if (configOrientation == 0) {
                // 不限屏幕方向
                return true;
            }
            if (configOrientation == 1) {
                // 限制竖屏方向
                return orientation == Configuration.ORIENTATION_PORTRAIT;
            }

            if (configOrientation == 2) {
                // 限制横屏方向
                return orientation == Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return true;
    }

    protected boolean isSceneIntervalAllow() {
        if (mBaseConfig != null && mBaseConfig.getSceneInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastSceneTime();
            long leftTime = mBaseConfig.getSceneInterval() - (now - last);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " si : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - last > mBaseConfig.getSceneInterval();
        }
        return true;
    }

    /**
     * 用户是否禁止场景弹出
     * @return
     */
    protected boolean isSceneDisabledByUser() {
        boolean userDisabled = Utils.getBoolean(mContext, Constant.AD_SDK_SCENE_DISABLED_PREFIX + mType, false);
        Log.iv(Log.TAG, "user disabled : " + userDisabled);
        return userDisabled;
    }

    protected boolean checkBaseConfig() {
        if (!isConfigAllow()) {
            Log.iv(Log.TAG, "con not allowed");
            return false;
        }

        if (isSceneDisabledByUser()) {
            Log.iv(Log.TAG, "user disabled");
            return false;
        }

        if (!isAttrAllow()) {
            return false;
        }

        if (!isDelayAllow()) {
            Log.iv(Log.TAG, "d not allowed");
            return false;
        }

        if (!isIntervalAllow()) {
            Log.iv(Log.TAG, "i not allowed");
            return false;
        }

        if (!isSceneIntervalAllow()) {
            Log.iv(Log.TAG, "si not allowed");
            return false;
        }

        if (!isMaxShowAllow()) {
            Log.iv(Log.TAG, "mc not allowed");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.iv(Log.TAG, "maxver not allowed");
            return false;
        }

        if (!matchInstallTime()) {
            Log.iv(Log.TAG, "cit not allowed");
            return false;
        }

        if (!isScreenOrientationAllow()) {
            Log.iv(Log.TAG, "so not allow");
            return false;
        }
        return true;
    }

    private String getPrefKey(String keyMacro) {
        String prefKey = String.format(Locale.getDefault(), keyMacro, mType);
        return prefKey;
    }

    private int getMsgWhat() {
        int msgWhat = 0;
        if (!TextUtils.isEmpty(mType)) {
            msgWhat = mType.hashCode();
        }
        return msgWhat;
    }
}
