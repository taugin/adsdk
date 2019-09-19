package com.bacad.ioc.gsb.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AttrChecker;
import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018-11-19.
 */

public class BPcy implements Handler.Callback {

    private static final String PREF_LAST_SHOWTIME = "pref_%s_last_showtime";
    private static final String PREF_TOTAL_SHOWTIMES = "pref_%s_total_showtimes";
    private static final String PREF_FIRST_SHOWTIME_ONEDAY = "pref_%s_first_showtime_oneday";
    private static final String PREF_REQUEST_TIME = "pref_%s_request_time";

    protected AttrChecker mAttrChecker;
    protected Context mContext;
    protected BCg mBCg;
    private String mType;
    private boolean mLoading = false;
    private Handler mHandler;
    private static final String LAST_SCENE_TIME = "pref_last_scene_time";
    public static final String LAST_SCENE_TYPE = "pref_last_scene_type";

    private static Map<String, BPcy> sPcyMap = new HashMap<String, BPcy>();

    public static BPcy getPcyByType(String type) {
        try {
            return sPcyMap.get(type);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    protected BPcy(Context context, String type) {
        sPcyMap.put(type, this);
        mContext = context;
        mType = type;
        mHandler = new Handler(this);
        mAttrChecker = new AttrChecker(context);
    }

    protected void setPolicy(BCg BCg) {
        mBCg = BCg;
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
        if (mBCg != null) {
            timeOut = mBCg.getTimeOut();
        }
        return timeOut;
    }

    public String getPlaceNameAdv() {
        if (mBCg != null) {
            return mBCg.getPlaceNameAdv();
        }
        return null;
    }

    public String getPlaceNameInt() {
        if (mBCg != null) {
            return mBCg.getPlaceNameInt();
        }
        return null;
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
        if (mBCg != null) {
            long now = System.currentTimeMillis();
            long lastReqTime = Utils.getLong(mContext, getPrefKey(PREF_REQUEST_TIME), 0);
            long leftTime = mBCg.getMinInterval() - (now - lastReqTime);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " mi : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - lastReqTime >= mBCg.getMinInterval();
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
            reportTotalShowTimes();
            updateLastShowTime();
            reportLastScene();
        }
    }

    private void reportLastScene() {
        Utils.putLong(mContext, LAST_SCENE_TIME, System.currentTimeMillis());
        Utils.putString(mContext, LAST_SCENE_TYPE, getType());
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
        return CSvr.get(mContext).getFirstStartUpTime();
    }

    /**
     * 记录ad展示次数
     */
    private void reportTotalShowTimes() {
        long now = System.currentTimeMillis();
        long last = getLastShowTime();
        if (now - last > 10000) {
            long times = getTotalShowTimes();
            times += 1;
            if (times <= 0) {
                times = 1;
            }
            Utils.putLong(mContext, getPrefKey(PREF_TOTAL_SHOWTIMES), times);
        }
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
        SceneEventImpl.get().reportAdOuterShowTimes(mContext, getType(), times);
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    protected boolean isConfigAllow() {
        if (mBCg != null) {
            return mBCg.isEnable();
        }
        return false;
    }

    protected boolean isAttrAllow() {
        if (mBCg != null && !mAttrChecker.isAttributionAllow(mBCg.getAttrList())) {
            Log.iv(Log.TAG, "attr not allowed");
            return false;
        }

        if (mBCg != null && !mAttrChecker.isCountryAllow(mBCg.getCountryList())) {
            Log.iv(Log.TAG, "country not allowed");
            return false;
        }

        if (mBCg != null && !mAttrChecker.isMediaSourceAllow(mBCg.getMediaList())) {
            Log.iv(Log.TAG, "ms not allowed");
            return false;
        }

        if (mBCg != null && !mAttrChecker.isVersionAllow(mBCg.getVerList())) {
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
        if (mBCg != null && mBCg.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mBCg.getUpDelay();
        }
        return true;
    }

    /**
     * 展示间隔是否允许
     *
     * @return
     */
    protected boolean isIntervalAllow() {
        if (mBCg != null && mBCg.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            long leftTime = mBCg.getInterval() - (now - last);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " i : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - last > mBCg.getInterval();
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
        if (mBCg != null && mBCg.getMaxCount() > 0) {
            long times = getTotalShowTimes();
            Log.d(Log.TAG, "total show times : " + times + " , mc : " + mBCg.getMaxCount());
            // 此处<=的逻辑会导致最大展示次数多1次
            return times <= mBCg.getMaxCount();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    protected boolean isAppVerAllow() {
        if (mBCg != null && mBCg.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mBCg.getMaxVersion();
        }
        return true;
    }


    protected boolean isTopApp() {
        boolean appOnTop = ActivityMonitor.get(mContext).appOnTop();
        return appOnTop;
    }

    protected boolean matchInstallTime() {
        if (mBCg != null) {
            long configInstallTime = mBCg.getConfigInstallTime();
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

    public boolean isShowBottom() {
        if (mBCg != null){
            return mBCg.isShowBottom();
        }
        return false;
    }

    protected boolean isScreenOrientationAllow() {
        if (mBCg != null) {
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            try {
                orientation = mContext.getResources().getConfiguration().orientation;
            } catch (Exception e) {
            }
            int configOrientation = mBCg.getScreenOrientation();
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
        if (mBCg != null && mBCg.getSceneInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastSceneTime();
            long leftTime = mBCg.getSceneInterval() - (now - last);
            if (leftTime > 0) {
                Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                Log.iv(Log.TAG, mType + " si : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
            }
            return now - last > mBCg.getSceneInterval();
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
            Log.iv(Log.TAG, "disable con");
            return false;
        }

        if (isSceneDisabledByUser()) {
            Log.iv(Log.TAG, "disable by user");
            return false;
        }

        if (!isAttrAllow()) {
            return false;
        }

        if (!isDelayAllow()) {
            Log.iv(Log.TAG, "disable d");
            return false;
        }

        if (!isIntervalAllow()) {
            Log.iv(Log.TAG, "disable i");
            return false;
        }

        if (!isSceneIntervalAllow()) {
            Log.iv(Log.TAG, "disable si");
            return false;
        }

        if (!isMaxShowAllow()) {
            Log.iv(Log.TAG, "disable mc");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.iv(Log.TAG, "disable mv");
            return false;
        }

        if (!matchInstallTime()) {
            Log.iv(Log.TAG, "disable cit");
            return false;
        }

        if (!isScreenOrientationAllow()) {
            Log.iv(Log.TAG, "disable so");
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
