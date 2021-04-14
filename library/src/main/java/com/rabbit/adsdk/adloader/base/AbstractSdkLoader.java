package com.rabbit.adsdk.adloader.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.core.AdLoaderManager;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.CheatManager;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.listener.AdLoaderFilter;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.adsdk.stat.IEvent;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.MView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public abstract class AbstractSdkLoader implements ISdkLoader, Handler.Callback {

    protected static final String ACTION_LOAD = "load";

    protected static final String ACTION_SHOW = "show";
    // 广告的最大默认缓存时间
    protected static final long MAX_CACHED_TIME = 30 * 60 * 1000;
    // 加载未返回的超时消息
    protected static final int MSG_LOADING_TIMEOUT = 1000;
    // 加载未返回的超时时间1分钟
    protected static final int LOADING_TIMEOUT = 60 * 1000;

    // 加载未返回的超时消息
    protected static final int MSG_PLAYING_TIMEOUT = 1001;
    // 加载未返回的超时时间5分钟
    protected static final int PLAYING_TIMEOUT = 60 * 1000;

    protected static final int STATE_REQUEST = 1;
    protected static final int STATE_SUCCESS = 2;
    protected static final int STATE_FAILURE = 3;
    protected static final int STATE_TIMTOUT = 4;

    private static Map<Object, Long> mCachedTime = new ConcurrentHashMap<Object, Long>();
    protected PidConfig mPidConfig;
    protected Context mContext;
    protected IManagerListener mManagerListener;
    private boolean mLoading = false;
    private boolean mLoadedFlag = false;
    private Handler mHandler = null;
    private long mRequestTime = 0;
    private int mBannerSize = Constant.NOSET;
    private IEvent mStat;
    private boolean mRewardVideoPlaying = false;
    private static final Random sRandom = new Random(System.currentTimeMillis());

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        mContext = context;
        mPidConfig = pidConfig;
        mStat = EventImpl.get();
        mHandler = new Handler(this);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public Activity getActivity() {
        Activity activity = null;
        if (mManagerListener != null) {
            activity = mManagerListener.getActivity();
        }
        if (activity == null) {
            try {
                activity = MView.createFakeActivity((Application) mContext.getApplicationContext());
                if (activity != null) {
                    Log.iv(Log.TAG, getSdkName() + " " + getAdType() + " use fk activity");
                }
            } catch (Exception e) {
            }
        }
        return activity;
    }

    @Override
    public abstract String getSdkName();

    public String getAdType() {
        if (mPidConfig != null) {
            return mPidConfig.getAdType();
        }
        return null;
    }

    @Override
    public PidConfig getPidConfig() {
        return mPidConfig;
    }

    @Override
    public void loadInterstitial() {
        notifyAdFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public boolean showInterstitial() {
        return false;
    }

    @Override
    public void loadNative(Params params) {
        notifyAdFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
    }

    @Override
    public void loadBanner(int adSize) {
        notifyAdFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public void loadRewardedVideo() {
        notifyAdFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public boolean showRewardedVideo() {
        return false;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return false;
    }

    @Override
    public boolean isBannerLoaded() {
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        return false;
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        return false;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getAdPlaceName() {
        String adPlaceName = null;
        if (mManagerListener != null) {
            adPlaceName = mManagerListener.getOriginPidName();
        }
        if (!TextUtils.isEmpty(adPlaceName)) {
            return adPlaceName;
        }
        if (mPidConfig != null) {
            return mPidConfig.getAdPlaceName();
        }
        return null;
    }

    @Override
    public boolean isBannerType() {
        if (mPidConfig != null) {
            return mPidConfig.isBannerType();
        }
        return false;
    }

    @Override
    public boolean isNativeType() {
        if (mPidConfig != null) {
            return mPidConfig.isNativeType();
        }
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        if (mPidConfig != null) {
            return mPidConfig.isInterstitialType();
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoType() {
        if (mPidConfig != null) {
            return mPidConfig.isRewardedVideoType();
        }
        return false;
    }

    private void setLoadedFlag() {
        mLoadedFlag = true;
        if (mManagerListener != null) {
            mManagerListener.setLoader(this);
        }
    }

    @Override
    public boolean hasLoadedFlag() {
        return mLoadedFlag;
    }

    @Override
    public boolean useAndClearFlag() {
        boolean flag = mLoadedFlag;
        mLoadedFlag = false;
        return flag;
    }

    protected boolean checkPidConfig() {
        if (mPidConfig == null) {
            Log.e(Log.TAG, "pid config is null");
            return false;
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.e(Log.TAG, "sdk not equals");
            return false;
        }
        if (TextUtils.isEmpty(mPidConfig.getPid())) {
            Log.e(Log.TAG, "pid is empty");
            return false;
        }
        return true;
    }

    /**
     * 检测通用配置
     *
     * @return
     */
    protected boolean checkCommonConfig() {
        // 检测作弊用户
        if (isUserCheat()) {
            processCheatUser();
            return false;
        }

        // 检测adLoader是否被过滤
        if (isAdFilter()) {
            processAdLoaderFilter();
            return false;
        }

        // 是否满足展示比率
        if (!isImpByRatio()) {
            processImpByRatio();
            return false;
        }
        return true;
    }

    private void processCheatUser() {
        Log.d(Log.TAG, "cheat user : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
        notifyAdFailed(Constant.AD_ERROR_CHEAT);
    }

    private boolean isUserCheat() {
        return CheatManager.get(mContext).isUserCheat(getSdkName(), getAdPlaceName());
    }

    protected void printInterfaceLog(String action) {
        Log.iv(Log.TAG, action + " | " + getSdkName() + " | " + getAdType() + " | " + getAdPlaceName() + " | " + getPid());
    }

    /**
     * 通过placename， sdk， type过滤此广告是否需要加载
     *
     * @return
     */
    private boolean isAdFilter() {
        AdLoaderFilter adLoaderFilter = AdLoaderManager.get(mContext).getAdLoaderFilter();
        if (adLoaderFilter != null) {
            return adLoaderFilter.doFilter(getAdPlaceName(), getSdkName(), getAdType());
        }
        return false;
    }

    private void processAdLoaderFilter() {
        Log.iv(Log.TAG, "loader is filter : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
        notifyAdFailed(Constant.AD_ERROR_FILTERED);
    }

    private boolean isImpByRatio() {
        int maxRatio = mPidConfig.getRatio();
        int randomRatio = sRandom.nextInt(100);
        Log.iv(Log.TAG, "random ratio : " + randomRatio + " , max ratio : " + maxRatio);
        return randomRatio < maxRatio;
    }

    private void processImpByRatio() {
        Log.iv(Log.TAG, "loader not ratio : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
        notifyAdFailed(Constant.AD_ERROR_RATIO);
    }

    private OnAdBaseListener getAdListener() {
        if (mManagerListener != null) {
            return mManagerListener.getAdBaseListener(this);
        }
        return null;
    }

    protected synchronized boolean isLoading() {
        return mLoading;
    }

    protected synchronized void setLoading(boolean loading, int state) {
        mLoading = loading;
        reportLoadAdTime(state);
        if (mLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIMEOUT, getTimeout());
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - send time out message : " + getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - remove time out message");
            }
        }
    }

    protected synchronized boolean isRewardPlaying() {
        return mRewardVideoPlaying;
    }

    protected synchronized void setRewardPlaying(boolean playing) {
        mRewardVideoPlaying = playing;
        if (mRewardVideoPlaying) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_PLAYING_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_PLAYING_TIMEOUT, PLAYING_TIMEOUT);
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - reward playing : " + getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_PLAYING_TIMEOUT);
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - reward dismiss");
            }
        }
    }

    protected void putCachedAdTime(Object object) {
        try {
            mCachedTime.put(object, SystemClock.elapsedRealtime());
        } catch (Exception e) {
        }
    }

    protected boolean isCachedAdExpired(Object object) {
        try {
            Long timeObj = mCachedTime.get(object);
            if (timeObj == null) {
                return true;
            }
            long cachedTime = timeObj.longValue();
            if (cachedTime <= 0) {
                return true;
            }
            return SystemClock.elapsedRealtime() - cachedTime > getMaxCachedTime();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return false;
    }

    protected void clearCachedAdTime(Object object) {
        try {
            mCachedTime.remove(object);
        } catch (Exception | Error e) {
        }
    }

    protected boolean isDestroyAfterClick() {
        try {
            return mPidConfig.isDestroyAfterClick();
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean allowUseLoader() {
        if (mPidConfig != null) {
            return !mPidConfig.isDisable();
        }
        return true;
    }


    /**
     * 防止多次加载NoFill的广告导致惩罚时间
     *
     * @return
     */
    protected boolean matchNoFillTime() {
        return System.currentTimeMillis() - getLastNoFillTime() >= mPidConfig.getNoFill();
    }

    /**
     * 更新最后填充
     */
    protected void updateLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            Utils.putLong(mContext, pref, System.currentTimeMillis());
            Log.d(Log.TAG, pref + " : " + System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    /**
     * 获取最后填充时间
     *
     * @return
     */
    protected long getLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            return Utils.getLong(mContext, pref, 0);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 获取广告最大缓存时间
     *
     * @return
     */
    private long getMaxCachedTime() {
        long cacheTime = 0;
        if (mPidConfig != null) {
            cacheTime = mPidConfig.getCacheTime();
        }
        if (cacheTime <= 0) {
            cacheTime = MAX_CACHED_TIME;
        }
        return cacheTime;
    }

    /**
     * 获取广告加载超时时间
     *
     * @return
     */
    private long getTimeout() {
        long timeOut = 0;
        if (mPidConfig != null) {
            timeOut = mPidConfig.getTimeOut();
        }
        if (timeOut <= 0) {
            timeOut = LOADING_TIMEOUT;
        }
        return timeOut;
    }

    protected void onLoadTimeout() {
        Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - load time out");
        reportAdError("AD_ERROR_TIMEOUT");
        setLoading(false, STATE_TIMTOUT);
        if (TextUtils.equals(getAdType(), Constant.TYPE_INTERSTITIAL)) {
            notifyAdFailed(Constant.AD_ERROR_TIMEOUT);
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_REWARD)) {
            notifyAdFailed(Constant.AD_ERROR_TIMEOUT);
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_BANNER)
                || TextUtils.equals(getAdType(), Constant.TYPE_NATIVE)) {
            notifyAdFailed(Constant.AD_ERROR_TIMEOUT);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == MSG_LOADING_TIMEOUT) {
            onLoadTimeout();
            return true;
        }
        return false;
    }

    protected String getPid() {
        if (mPidConfig != null) {
            return mPidConfig.getPid();
        }
        return null;
    }

    private void reportLoadAdTime(int state) {
        if (state == STATE_REQUEST) {
            mRequestTime = SystemClock.elapsedRealtime();
        } else if (state == STATE_SUCCESS) {
            if (mRequestTime > 0) {
                try {
                    int time = Math.round((SystemClock.elapsedRealtime() - mRequestTime) / (float) 100);
                    mStat.reportAdLoadSuccessTime(mContext, getAdPlaceName(), getSdkName(), getAdType(), time);
                } catch (Exception e) {
                }
                mRequestTime = 0;
            }
        } else {
            if (mRequestTime > 0) {
                try {
                    String error = "STATE_FAILURE";
                    if (state == STATE_TIMTOUT) {
                        error = "STATE_TIMTOUT";
                    }
                    int time = Math.round((SystemClock.elapsedRealtime() - mRequestTime) / (float) 100);
                    mStat.reportAdLoadFailureTime(mContext, getAdPlaceName(), getSdkName(), getAdType(), error, time);
                } catch (Exception e) {
                }
                mRequestTime = 0;
            }
        }
    }

    /**
     * 对于已经加载成功的banner，延迟一段时间再进行通知
     *
     * @param runnable
     */
    private void delayNotifyAdLoaded(Runnable runnable, boolean cached) {
        long delay = getDelayNotifyLoadTime(cached);
        Log.v(Log.TAG, "delay notify loaded time : " + delay);
        if (delay <= 0) {
            if (runnable != null) {
                runnable.run();
            }
        } else {
            if (mHandler != null) {
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, delay);
            }
        }
    }

    private long getDelayNotifyLoadTime(boolean cached) {
        long delay = 0;
        if (mPidConfig != null) {
            delay = mPidConfig.getDelayLoadTime();
        }

        // 缓存的banner最小延迟为500ms
        if (cached && TextUtils.equals(getAdType(), Constant.TYPE_BANNER)) {
            if (delay <= 0) {
                delay = 500;
            }
        }
        return delay;
    }

    /**
     * 通知ad加载成功
     * 增加延迟通知功能，便于优先级高但是加载时间长的ad能够优先展示
     *
     * @param cached
     */
    protected void notifyAdLoaded(boolean cached) {
        delayNotifyAdLoaded(mNotifyLoadRunnable, cached);
    }

    private Runnable mNotifyLoadRunnable = new Runnable() {
        @Override
        public void run() {
            notifyAdLoadedByListener();
        }
    };

    private void notifyAdLoadedByListener() {
        notifyAdLoaded(this);
    }

    protected String getMetaData(String key) {
        ApplicationInfo info = null;
        try {
            info = mContext.getPackageManager()
                    .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info != null ? info.metaData.getString(key) : null;
    }

    protected String getAppId() {
        if (mPidConfig != null) {
            return mPidConfig.getAppId();
        }
        return null;
    }

    protected String getExtId() {
        if (mPidConfig != null) {
            return mPidConfig.getExtId();
        }
        return null;
    }

    protected boolean isLoadMultipleNative() {
        if (mPidConfig != null && mPidConfig.getCnt() > 1) {
            return true;
        }
        return false;
    }

    protected void setBannerSize(int size) {
        mBannerSize = size;
    }

    @Override
    public int getBannerSize() {
        return mBannerSize;
    }

    @Override
    public double getEcpm() {
        if (mPidConfig != null) {
            return mPidConfig.getEcpm();
        }
        return 0;
    }

    @Override
    public void notifyAdViewUIDismiss() {
        reportAdClose();
        notifyAdDismiss(true);
    }

    @Override
    public void showInterstitialWithNative(ViewGroup viewGroup, Params params) {
    }

    @Override
    public String toString() {
        return "pn = " + getAdPlaceName() + " , " +
                "tp = " + getAdType() + " , " +
                "sr = " + getSdkName() + " , " +
                "ba = " + Constant.Banner.valueOf(getBannerSize()) + " , " +
                "ec = " + getEcpm();
    }

    protected void reportAdRequest() {
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected void reportAdLoaded() {
        if (mStat != null) {
            mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected void reportAdShow() {
        if (mStat != null) {
            mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected void reportAdImp() {
        reportAdImp(null);
    }

    protected void reportAdImp(String render) {
        if (mStat != null) {
            BaseBindNativeView baseBindNativeView = getBaseBindNativeView();
            Map<String, String> extra = null;
            if (baseBindNativeView != null) {
                extra = baseBindNativeView.getAdvMap();
            }
            if (extra != null && !extra.isEmpty()) {
                Log.v(Log.TAG, "extra : " + extra);
            }
            Map<String, String> mediaTypeExtra = null;
            if (!TextUtils.isEmpty(render)) {
                mediaTypeExtra = new HashMap<String, String>();
                mediaTypeExtra.put("render", render);
            }
            mStat.reportAdImp(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), mediaTypeExtra);
            if (extra != null) {
                extra.clear();
            }
        }
    }

    protected void reportAdClick() {
        if (mStat != null) {
            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected void reportAdReward() {
        if (mStat != null) {
            mStat.reportAdReward(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected void reportAdError(String error) {
        if (mStat != null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("error", "[" + getSdkName() + "]" + error);
            mStat.reportAdError(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), map);
        }
    }

    protected void reportAdClose() {
        if (mStat != null) {
            mStat.reportAdClose(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), String.valueOf(getEcpm()), null);
        }
    }

    protected BaseBindNativeView getBaseBindNativeView() {
        return null;
    }


    /**
     * banner or native loaded
     */
    protected void notifyAdLoaded(ISdkLoader loader) {
        if (getAdListener() != null) {
            setLoadedFlag();
            getAdListener().onAdLoaded(loader);
        }
    }

    /**
     * ad show
     */
    protected void notifyAdShow() {
        if (getAdListener() != null) {
            getAdListener().onAdShow();
        }
    }

    /**
     * banner or native impression
     */
    protected void notifyAdImp() {
        if (getAdListener() != null) {
            getAdListener().onAdImp();
        }
    }

    /**
     * banner or native click
     */
    protected void notifyAdClick() {
        if (getAdListener() != null) {
            getAdListener().onAdClick();
        }
    }

    protected void notifyAdDismiss() {
        notifyAdDismiss(false);
    }

    /**
     * banner or native dismiss
     */
    protected void notifyAdDismiss(boolean complexAds) {
        if (getAdListener() != null) {
            getAdListener().onAdDismiss(complexAds);
        }
    }

    /**
     * banner or native fail
     */
    protected void notifyAdFailed(int error) {
        if (getAdListener() != null) {
            getAdListener().onAdFailed(error);
        }
    }

    /**
     * banner or native opened
     */
    protected void notifyAdOpened() {
        if (getAdListener() != null) {
            getAdListener().onAdOpened();
        }
    }

    /**
     * reward
     */
    protected void notifyRewarded(AdReward reward) {
        if (getAdListener() != null) {
            getAdListener().onRewarded(reward);
        }
    }

    /**
     * reward complete
     */
    protected void notifyRewardAdsCompleted() {
        if (getAdListener() != null) {
            getAdListener().onRewardAdsCompleted();
        }
    }

    /**
     * reward start
     */
    protected void notifyRewardAdsStarted() {
        if (getAdListener() != null) {
            getAdListener().onRewardAdsStarted();
        }
    }
}