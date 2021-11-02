package com.rabbit.adsdk.adloader.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.mopub.network.ImpressionData;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.core.AdLoaderManager;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.BlockAdsManager;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.listener.AdLoaderFilter;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.adsdk.stat.IEvent;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.MView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    protected static final int FULLSCREEN_SHOWTIME_EXPIRED = 5 * 60 * 1000;

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
    private static final Random sRandom = new Random(System.currentTimeMillis());
    private long mLastFullScreenShowTime = 0;

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        mContext = context;
        mPidConfig = pidConfig;
        mStat = EventImpl.get();
        mHandler = new Handler(Looper.getMainLooper());
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
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public boolean showInterstitial() {
        return false;
    }

    @Override
    public void loadNative(Params params) {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
    }

    @Override
    public void loadBanner(int adSize) {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public void loadRewardedVideo() {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT);
    }

    @Override
    public boolean showRewardedVideo() {
        return false;
    }

    @Override
    public void loadSplash() {
        if (getAdListener() != null) {
            getAdListener().onAdLoadFailed(Constant.AD_ERROR_UNSUPPORT);
        }
    }

    @Override
    public boolean showSplash() {
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
    public boolean isSplashLoaded() {
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
            adPlaceName = mManagerListener.getOriginPlaceName();
        }
        if (!TextUtils.isEmpty(adPlaceName)) {
            return adPlaceName;
        }
        if (mPidConfig != null) {
            return mPidConfig.getPlaceName();
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

    @Override
    public boolean isSplashType() {
        if (mPidConfig != null) {
            return mPidConfig.isSplashType();
        }
        return false;
    }

    protected boolean checkPidConfig() {
        if (mPidConfig == null) {
            Log.iv(Log.TAG, formatLog("error pid config is null"));
            return false;
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.iv(Log.TAG, formatLog("error sdk not equals"));
            return false;
        }
        if (TextUtils.isEmpty(mPidConfig.getPid())) {
            Log.iv(Log.TAG, formatLog("error pid is empty"));
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
        if (isBlockAds()) {
            processBlockAds();
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

    private void processBlockAds() {
        Log.iv(Log.TAG, formatLog("block ads"));
        notifyAdLoadFailed(Constant.AD_ERROR_BLOCK_ADS);
    }

    private boolean isBlockAds() {
        return BlockAdsManager.get(mContext).isBlockAds(getSdkName(), getAdPlaceName());
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
        Log.iv(Log.TAG, formatLog("loader is filter"));
        notifyAdLoadFailed(Constant.AD_ERROR_FILTERED);
    }

    private boolean isImpByRatio() {
        int maxRatio = mPidConfig.getRatio();
        int randomRatio = sRandom.nextInt(100);
        Log.iv(Log.TAG, formatLog("random ratio : " + randomRatio + " , max ratio : " + maxRatio));
        return randomRatio < maxRatio;
    }

    private void processImpByRatio() {
        Log.iv(Log.TAG, formatLog("ratio not match"));
        notifyAdLoadFailed(Constant.AD_ERROR_RATIO);
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
                Log.iv(Log.TAG, formatLog("send time out message : " + getTimeout()));
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                Log.iv(Log.TAG, formatLog("remove time out message"));
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
        Log.iv(Log.TAG, formatLog("load time out"));
        reportAdError("AD_ERROR_TIMEOUT");
        setLoading(false, STATE_TIMTOUT);
        if (TextUtils.equals(getAdType(), Constant.TYPE_INTERSTITIAL)) {
            notifyAdLoadFailed(Constant.AD_ERROR_TIMEOUT);
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_REWARD)) {
            notifyAdLoadFailed(Constant.AD_ERROR_TIMEOUT);
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_BANNER)
                || TextUtils.equals(getAdType(), Constant.TYPE_NATIVE)) {
            notifyAdLoadFailed(Constant.AD_ERROR_TIMEOUT);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null) {
            if (msg.what == MSG_LOADING_TIMEOUT) {
                onLoadTimeout();
                return true;
            }
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
        Log.iv(Log.TAG, formatLog("delay notify loaded time : " + delay));
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
    protected void notifySdkLoaderLoaded(boolean cached) {
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
            Map<String, String> nativeAssets = null;
            if (baseBindNativeView != null) {
                nativeAssets = baseBindNativeView.getAdvMap();
            }
            if (nativeAssets != null && !nativeAssets.isEmpty()) {
                Log.iv(Log.TAG, getAdPlaceName() + " - " + getSdkName() + " - " + getAdType() + " [" + render + "] assets : " + nativeAssets);
            }
            Map<String, String> mediaTypeExtra = null;
            if (!TextUtils.isEmpty(render)) {
                mediaTypeExtra = new HashMap<String, String>();
                mediaTypeExtra.put("render", render);
            }
            mStat.reportAdImp(mContext, getAdPlaceName(), getSdkName(), render, getAdType(), getPid(), String.valueOf(getEcpm()), mediaTypeExtra);
            if (nativeAssets != null) {
                nativeAssets.clear();
            }
        }
    }

    protected void reportAdClick() {
        reportAdClick(null);
    }

    protected void reportAdClick(String render) {
        if (mStat != null) {
            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), render, getAdType(), getPid(), String.valueOf(getEcpm()), null);
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

    protected void notifyAdImp() {
        notifyAdImp(null);
    }

    /**
     * banner or native impression
     */
    protected void notifyAdImp(String render) {
        if (getAdListener() != null) {
            getAdListener().onAdImp(render);
        }
    }

    protected void notifyAdClick() {
        notifyAdClick(null);
    }

    /**
     * banner or native click
     */
    protected void notifyAdClick(String render) {
        if (getAdListener() != null) {
            getAdListener().onAdClick(render);
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
    protected void notifyAdLoadFailed(int error) {
        if (getAdListener() != null) {
            getAdListener().onAdLoadFailed(error);
        }
    }

    protected void notifyAdShowFailed(int error) {
        if (getAdListener() != null) {
            getAdListener().onAdShowFailed(error);
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

    public static interface SDKInitializeListener {
        void onInitializeSuccess(String appId, String appSecret);

        void onInitializeFailure(String error);
    }

    public static enum SDKInitializeState {
        SDK_STATE_UN_INITIALIZE,
        SDK_STATE_INITIALIZING,
        SDK_STATE_INITIALIZE_SUCCESS,
        SDK_STATE_INITIALIZE_FAILURE;

        private SDKInitializeState() {
        }
    }

    protected String formatLog(String info) {
        return formatLog(info, false);
    }

    protected String formatLog(String info, boolean showPid) {
        String baseLog = getAdPlaceName() + " - " + getSdkName() + " - " + getAdType();
        if (showPid) {
            baseLog = baseLog + " - " + getPid();
        }
        return "[sdk loader] " + baseLog + " [" + info + "]";
    }

    protected void onResetInterstitial() {
        Log.iv(Log.TAG, formatLog("reset interstitial"));
    }

    protected void onResetReward() {
        Log.iv(Log.TAG, formatLog("reset reward"));
    }

    protected void onResetSplash() {
        Log.iv(Log.TAG, formatLog("reset splash"));
    }

    protected void updateLastShowTime() {
        Log.iv(Log.TAG, formatLog("update last show time"));
        mLastFullScreenShowTime = System.currentTimeMillis();
    }

    protected void clearLastShowTime() {
        Log.iv(Log.TAG, formatLog("clear last show time"));
        mLastFullScreenShowTime = 0;
    }

    protected boolean isShowTimeExpired() {
        if (mLastFullScreenShowTime > 0) {
            long now = System.currentTimeMillis();
            long saveTime = now - mLastFullScreenShowTime;
            return saveTime > FULLSCREEN_SHOWTIME_EXPIRED;
        }
        return false;
    }

    /**
     * 控制是否上报广告展示价值
     * @return
     */
    protected boolean isReportAdImpData() {
        String value = DataManager.get(mContext).getString("report_ad_imp_data");
        if (!TextUtils.isEmpty(value)) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
            }
        }
        return false;
    }

    /*************************************************************************************/
    // Taichi模型，为AC2.5准备数据
    private void reportAdLTVOneDayPercent(ImpressionData impressionData) {
        if (impressionData != null) {
            Double publishRevenue = impressionData.getPublisherRevenue();
            if (publishRevenue != null && !Double.isNaN(publishRevenue)) {
                calcTaiChitCPAOneDayAdRevenueCache(publishRevenue.floatValue());
            }
        }
    }

    private void resetTaiChitCPAOneDayAdRevenueCache() {
        long todayTime = Utils.getTodayTime();
        long lastTime = Utils.getLong(getContext(), "TaiChitCPAOneDayAdRevenueCacheDate", todayTime);
        if (todayTime != lastTime) {
            Utils.putLong(getContext(), "TaiChitCPAOneDayAdRevenueCacheDate", todayTime);
            Utils.putFloat(getContext(), "TaiChitCPAOneDayAdRevenueCache", 0f);
        }
    }

    private void calcTaiChitCPAOneDayAdRevenueCache(float currentImpressionRevenue) {
        resetTaiChitCPAOneDayAdRevenueCache();
        float previousOneDayAdRevenueCache = Utils.getFloat(getContext(), "TaiChitCPAOneDayAdRevenueCache", 0f);
        float currentOneDayAdRevenueCache = (float) (previousOneDayAdRevenueCache + currentImpressionRevenue);
        Utils.putFloat(getContext(), "TaiChitCPAOneDayAdRevenueCache", currentOneDayAdRevenueCache);
        reportLogTaiChiTCPAFirebaseAdRevenueEvent(previousOneDayAdRevenueCache, currentImpressionRevenue);
    }

    private Float[] getAdsLTVThreshold() {
        String adsLTVThresholdString = DataManager.get(getContext()).getString("TaiChitCPAOneDayAdRevenueLTVThreshold");
        try {
            String[] temp = adsLTVThresholdString.split("|");
            List<Float> adsLTVThreshold = new ArrayList<>();
            for (String s : temp) {
                adsLTVThreshold.add(Float.parseFloat(s));
            }
            return adsLTVThreshold.toArray(new Float[]{});
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void reportLogTaiChiTCPAFirebaseAdRevenueEvent(float previousAdsTV, float currentAdsTv) {
        Float[] adsLTVThreshold = getAdsLTVThreshold();
        if (adsLTVThreshold != null && adsLTVThreshold.length > 0) {
            for (int i = 0; i < adsLTVThreshold.length; i++) {
                if (previousAdsTV < adsLTVThreshold[i] && currentAdsTv >= adsLTVThreshold[i]) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", adsLTVThreshold[i]);
                    map.put("currency", "USD");
                    String TaichiEventName = null;
                    switch (i) {
                        case 0:
                            TaichiEventName = "AdLTV_OneDay_Top50Percent";
                            break;
                        case 1:
                            TaichiEventName = "AdLTV_OneDay_Top40Percent";
                            break;
                        case 2:
                            TaichiEventName = "AdLTV_OneDay_Top30Percent";
                            break;
                        case 3:
                            TaichiEventName = "AdLTV_OneDay_Top20Percent";
                            break;
                        default:
                            TaichiEventName = "AdLTV_OneDay_Top10Percent";
                    }
                    InternalStat.reportEvent(getContext(), TaichiEventName, map);
                }
            }
        }
    }
}