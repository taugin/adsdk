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

import com.rabbit.adsdk.AdImpData;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.applovin.AppLovinLoader;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.AdPolicy;
import com.rabbit.adsdk.core.db.DBManager;
import com.rabbit.adsdk.core.framework.AdLoadManager;
import com.rabbit.adsdk.core.framework.AdStatManager;
import com.rabbit.adsdk.core.framework.LimitAdsManager;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.data.parse.IParser;
import com.rabbit.adsdk.listener.OnAdEventListener;
import com.rabbit.adsdk.listener.OnAdFilterListener;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.adsdk.stat.IEvent;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.BuildConfig;
import com.rabbit.sunny.MView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 */

public abstract class AbstractSdkLoader implements ISdkLoader, Handler.Callback {

    protected static final String ACTION_LOAD = "load";

    protected static final String ACTION_SHOW = "show";
    // 广告的最大默认缓存时间
    protected static final long MAX_CACHED_TIME = 30 * 60 * 1000;
    // 加载未返回的超时消息
    protected static final int MSG_LOADING_TIMEOUT = 0x1234;
    // 加载未返回的超时时间1分钟
    protected static final int LOADING_TIMEOUT = 60 * 1000;

    protected static final int FULLSCREEN_SHOWTIME_EXPIRED = 5 * 60 * 1000;

    protected static final int STATE_NONE = 0;
    protected static final int STATE_REQUEST = 1;
    protected static final int STATE_SUCCESS = 2;
    protected static final int STATE_FAILURE = 3;
    protected static final int STATE_TIMEOUT = 4;

    private static final Map<Object, Long> mCachedTime = new ConcurrentHashMap<>();
    protected PidConfig mPidConfig;
    protected Context mContext;
    protected IManagerListener mManagerListener;
    private boolean mLoading = false;
    private final Handler mHandler;
    private long mRequestTime = 0;
    private int mBannerSize = Constant.NOSET;
    private IEvent mStat;
    private static final Random sRandom = new Random(System.currentTimeMillis());
    private long mLastFullScreenShowTime = 0;
    private final AtomicBoolean mLoadTimeout = new AtomicBoolean(false);
    private String mAdNetwork;
    private double mAdRevenue;
    private String mRequestId;
    int mLoadState = STATE_NONE;
    // applovin SDK需要提前初始化的SDK名称列表
    private static final List<String> sNeedInitAppLovinFirstSdks = Arrays.asList(Constant.AD_SDK_TRADPLUS, Constant.AD_SDK_APPLOVIN);

    public AbstractSdkLoader() {
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    /**
     * 判断是否先初始化applovin，聚合SDK需要重载次函数，先初始化applovin
     *
     * @return true
     */
    protected boolean isInitApplovin() {
        try {
            String sdkName = getSdkName();
            return sNeedInitAppLovinFirstSdks.contains(sdkName);
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        mContext = context;
        mPidConfig = pidConfig;
        mStat = EventImpl.get();
        if (isInitApplovin()) {
            AppLovinLoader.initApplovin(mContext);
        }
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
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT, "unsupported");
    }

    @Override
    public boolean showInterstitial(String sceneName) {
        return false;
    }

    @Override
    public void loadNative(Params params) {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT, "unsupported");
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
    }

    @Override
    public void loadBanner(int adSize) {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT, "unsupported");
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public void loadRewardedVideo() {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT, "unsupported");
    }

    @Override
    public boolean showRewardedVideo(String sceneName) {
        return false;
    }

    @Override
    public void loadSplash() {
        notifyAdLoadFailed(Constant.AD_ERROR_UNSUPPORT, "unsupported");
    }

    @Override
    public boolean showSplash(ViewGroup viewGroup) {
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
        // 排除异常平台
        if (isLimitExclude()) {
            processLimitAds();
            return false;
        }

        // 检测广告是否被禁止加载
        if (isAdFiltered()) {
            processAdFilter();
            return false;
        }

        // 是否满足展示比率
        if (!isMatchRatio()) {
            processUnMatchRatio();
            return false;
        }

        // 超出最大请求次数
        if (isExceedReqTimes()) {
            processExceedReqTimes();
            return false;
        }

        // 是否禁止vpn模式加载
        if (isDisableVpnLoad()) {
            processDisableVpn();
            return false;
        }
        // 是否禁止调试模式加载
        if (isDisableDebugLoad()) {
            processDisableDebug();
            return false;
        }
        return true;
    }

    private void processLimitAds() {
        Log.iv(Log.TAG, formatLog("limit ads"));
        notifyAdLoadFailed(Constant.AD_ERROR_LIMIT_ADS, "limit ads");
    }

    private boolean isLimitExclude() {
        return LimitAdsManager.get(mContext).isLimitExclude(getSdkName());
    }

    private void processExceedReqTimes() {
        long reqTimes = AdPolicy.get(mContext).getReqTimes(getAdPlaceName(), getSdkName());
        Log.iv(Log.TAG, formatLog("exceed max req times : " + reqTimes + "/" + getMaxReqTimes()));
        notifyAdLoadFailed(Constant.AD_ERROR_EXCEED_REQ_TIME, "exceed max req times");
    }

    private boolean isExceedReqTimes() {
        return AdPolicy.get(mContext).isExceedMaxReqTimes(getAdPlaceName(), getSdkName(), getMaxReqTimes());
    }

    protected void printInterfaceLog(String action) {
        Log.iv(Log.TAG, action + " | " + getSdkName() + " | " + getAdType() + " | " + getAdPlaceName() + " | " + getPid());
    }

    /**
     * 通过placename， sdk， type过滤此广告是否需要加载
     *
     * @return
     */
    private boolean isAdFiltered() {
        OnAdFilterListener onAdFilterListener = AdLoadManager.get(mContext).getOnAdFilterListener();
        if (onAdFilterListener != null) {
            return onAdFilterListener.doFilter(getAdPlaceName(), getSdkName(), getAdType());
        }
        return false;
    }

    private void processAdFilter() {
        Log.iv(Log.TAG, formatLog("ad is disable loading"));
        notifyAdLoadFailed(Constant.AD_ERROR_DISABLE_LOADING, "ad is disable loading");
    }

    private boolean isMatchRatio() {
        int maxRatio = mPidConfig.getRatio();
        int randomRatio = sRandom.nextInt(100);
        Log.iv(Log.TAG, formatLog("random ratio : " + randomRatio + " , max ratio : " + maxRatio));
        return randomRatio < maxRatio;
    }

    private void processUnMatchRatio() {
        Log.iv(Log.TAG, formatLog("ratio not match"));
        notifyAdLoadFailed(Constant.AD_ERROR_RATIO, "ratio not match");
    }

    private boolean isDisableVpnLoad() {
        boolean disableVpnGlobal = DataManager.get(mContext).isDisableVpn();
        boolean disableVpn = mPidConfig.isDisableVpnLoad();
        return (disableVpnGlobal || disableVpn) && Utils.isVPNConnected(mContext);
    }

    private void processDisableVpn() {
        Log.iv(Log.TAG, formatLog("disable vpn load"));
        notifyAdLoadFailed(Constant.AD_ERROR_DISABLE_VPN, "disable vpn load");
    }

    private void processDisableDebug() {
        Log.iv(Log.TAG, formatLog("disable debug load"));
        notifyAdLoadFailed(Constant.AD_ERROR_DISABLE_DEBUG, "disable debug load");
    }

    private int getMaxReqTimes() {
        if (mPidConfig != null) {
            return mPidConfig.getMaxReqTimes();
        }
        return 0;
    }

    private OnAdBaseListener getAdListener() {
        if (mManagerListener != null) {
            return mManagerListener.getAdBaseListener(this);
        }
        return null;
    }

    protected boolean isStateSuccess() {
        return mLoadState == STATE_SUCCESS;
    }

    protected synchronized boolean isLoading() {
        return mLoading;
    }

    protected synchronized void setLoading(boolean loading, int state) {
        mLoadState = state;
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
        // 如果忽略过期时间，则返回false，标识对象没有过期
        if (ignoreAdExpired()) {
            Log.iv(Log.TAG, formatLog("ignore ad expire"));
            return false;
        }
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

    /**
     * max、tradplus平台是聚合平台，
     * 平台本身就具有判断广告过期的条件，
     * 因此对于这些平台降忽略对插屏和激励视频过期时间的判断
     *
     * @return
     */
    private boolean ignoreAdExpired() {
        // applovin interstitial reward
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN, getSdkName())
                && (TextUtils.equals(Constant.TYPE_INTERSTITIAL, getAdType())
                || TextUtils.equals(Constant.TYPE_REWARD, getAdType())
                || TextUtils.equals(Constant.TYPE_SPLASH, getAdType()))) {
            return true;
        }
        // tradplus interstitial reward
        if (TextUtils.equals(Constant.AD_SDK_TRADPLUS, getSdkName())) {
            return true;
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
        Log.iv(Log.TAG, formatLog("load time out"));
        reportAdError("AD_ERROR_TIMEOUT");
        setLoading(false, STATE_TIMEOUT);
        notifyAdLoadFailed(Constant.AD_ERROR_TIMEOUT, "load time out");
        mLoadTimeout.set(true);
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

    private int getMinAvgCount() {
        if (mPidConfig != null) {
            return mPidConfig.getMinAvgCount();
        }
        return 0;
    }

    private boolean isDisableDebugLoad() {
        return mPidConfig != null && mPidConfig.isDisableDebugLoad() && Utils.isAdbUsbEnabled(mContext);
    }

    private boolean isUseAvgValue() {
        return mPidConfig != null && mPidConfig.isUseAvgValue();
    }

    protected String getSceneId() {
        return getSceneId(null);
    }

    protected String getSceneId(String sceneName) {
        if (!TextUtils.isEmpty(sceneName)) {
            return sceneName;
        }
        String sceneId = null;
        try {
            if (mPidConfig != null) {
                sceneId = mPidConfig.getSceneId();
                if (TextUtils.isEmpty(sceneId)) {
                    AdPlace adPlace = mPidConfig.getAdPlace();
                    if (adPlace != null) {
                        sceneId = adPlace.getSceneId();
                    }
                }
            }
            if (TextUtils.isEmpty(sceneId)) {
                String scenePrefix = DataManager.get(mContext).getScenePrefix();
                if (!TextUtils.isEmpty(scenePrefix)) {
                    sceneId = scenePrefix + getAdPlaceName();
                } else {
                    sceneId = getAdPlaceName();
                }
            }
        } catch (Exception e) {
            sceneId = getAdPlaceName();
        }
        Log.iv(Log.TAG, formatLog("scene id : " + sceneId));
        return sceneId;
    }

    protected String getSdkVersion() {
        return BuildConfig.SDK_VERSION_NAME;
    }

    protected String getAppVersion() {
        return Utils.getVersionName(mContext);
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
                    if (state == STATE_TIMEOUT) {
                        error = "STATE_TIMEOUT";
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
    public double getCpm() {
        if (mPidConfig != null) {
            return mPidConfig.getCpm();
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

    protected void setAdNetworkAndRevenue(String network, double adRevenue) {
        mAdNetwork = network;
        double finalAdRevenue = adRevenue;
        if (finalAdRevenue <= 0f && isUseAvgValue()) {
            finalAdRevenue = DBManager.get(mContext).queryAverageRevenue(getPid(), getMinAvgCount());
        }
        if (finalAdRevenue <= 0f) {
            finalAdRevenue = getCpm() / 1000f;
        }
        mAdRevenue = finalAdRevenue;
        Log.iv(Log.TAG, "network : " + network + " , type : " + getAdType() + " , value : " + mAdRevenue);
    }

    @Override
    public double getRevenue() {
        return mAdRevenue;
    }

    protected void setRequestId() {
        try {
            mRequestId = UUID.randomUUID().toString();
        } catch (Exception e) {
        }
    }

    protected String getRequestId() {
        return mRequestId;
    }

    protected boolean isTemplateRendering() {
        if (mPidConfig != null) {
            return mPidConfig.isTemplate();
        }
        return false;
    }

    @Override
    public String toString() {
        return "sdk loader{" +
                "placeName=" + getAdPlaceName() + " , " +
                "adType=" + getAdType() + " , " +
                "sdkName=" + getSdkName() + " , " +
                "adNetwork=" + mAdNetwork + " , " +
                "adRevenue=" + mAdRevenue +
                "}";
    }

    protected void reportAdRequest() {
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected void reportAdLoaded() {
        if (mStat != null) {
            mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected void reportAdReLoaded() {
        if (mStat != null) {
            mStat.reportAdReLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected void reportAdShow() {
        if (mStat != null) {
            mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected void reportAdImp() {
        reportAdImp(getSdkName(), getPid());
    }

    protected void reportAdImp(String network, String networkPid) {
        if (mStat != null) {
            BaseBindNativeView baseBindNativeView = getBaseBindNativeView();
            Map<String, String> nativeAssets = null;
            if (baseBindNativeView != null) {
                nativeAssets = baseBindNativeView.getAdvMap();
            }
            if (nativeAssets != null && !nativeAssets.isEmpty()) {
                if (!TextUtils.isEmpty(network)) {
                    nativeAssets.put(IParser.SDK, network);
                } else {
                    nativeAssets.put(IParser.SDK, getSdkName());
                }
                StringBuilder builder = new StringBuilder("{");
                builder.append("\n");
                for (Map.Entry<String, String> entry : nativeAssets.entrySet()) {
                    builder.append("  " + entry.getKey() + " : " + entry.getValue());
                    builder.append("\n");
                }
                builder.append("}");
                Log.iv(Log.TAG, getAdPlaceName() + " - " + getSdkName() + " - " + getAdType() + " [" + network + "] assets : " + builder.toString());
            }
            mStat.reportAdImp(mContext, getAdPlaceName(), getSdkName(), network, getAdType(), getPid(), networkPid, getCpm(), null, getRequestId());
            if (nativeAssets != null) {
                nativeAssets.clear();
            }
        }
    }

    protected void reportAdClick() {
        reportAdClick(getSdkName(), getPid());
    }

    protected void reportAdClick(String network, String networkPid) {
        if (mStat != null) {
            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), network, getAdType(), getPid(), networkPid, getCpm(), null, getRequestId());
        }
    }

    protected void reportAdReward() {
        if (mStat != null) {
            mStat.reportAdReward(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected void reportAdError(String error) {
        if (mStat != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("error", "[" + getSdkName() + "]" + error);
            mStat.reportAdError(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), map);
        }
    }

    protected void reportAdClose() {
        if (mStat != null) {
            mStat.reportAdClose(mContext, getAdPlaceName(), getSdkName(), getAdType(), getPid(), getCpm(), null);
        }
    }

    protected abstract BaseBindNativeView getBaseBindNativeView();

    protected void notifyAdRequest() {
        mLoadTimeout.set(false);
        if (getAdListener() != null) {
            getAdListener().onAdRequest();
        }
        AdPolicy.get(mContext).recordRequestTimes(getAdPlaceName(), getSdkName(), getMaxReqTimes());
        setRequestId();
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onRequest(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    /**
     * banner or native loaded
     */
    protected void notifyAdLoaded(ISdkLoader loader) {
        if (getAdListener() != null) {
            getAdListener().onAdLoaded(loader);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onLoaded(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    /**
     * ad show
     */
    protected void notifyAdShow() {
        if (getAdListener() != null) {
            getAdListener().onAdShow();
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onShow(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    protected void notifyAdImp() {
        notifyAdImp(null);
    }

    /**
     * banner or native impression
     */
    protected void notifyAdImp(String network) {
        if (getAdListener() != null) {
            getAdListener().onAdImp(network);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onImp(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    protected void notifyAdClick() {
        notifyAdClick(null);
    }

    /**
     * banner or native click
     */
    protected void notifyAdClick(String network) {
        if (getAdListener() != null) {
            getAdListener().onAdClick(network);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onClick(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
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
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onDismiss(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    protected void notifyAdLoadFailed(int error, String msg) {
        if (getAdListener() != null && !mLoadTimeout.getAndSet(false)) {
            getAdListener().onAdLoadFailed(error, msg);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onLoadFailed(getAdPlaceName(), getSdkName(), getAdType(), getPid(), getRequestId());
        }
    }

    protected void notifyAdShowFailed(int error, String msg) {
        if (getAdListener() != null) {
            getAdListener().onAdShowFailed(error, msg);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onShowFailed(getAdPlaceName(), getSdkName(), getAdType(), getPid(), msg, getRequestId());
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

    protected String formatShowErrorLog(String info) {
        String baseLog = "[" + getAdPlaceName() + " - " + getSdkName() + " - " + getPid() + "]";
        return "[sdk loader]" + " " + baseLog + " " + "show " + getAdType() + " error : " + "[" + info + "]";
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
     *
     * @return
     */
    protected boolean isReportAdImpData() {
        String value = DataManager.get(mContext).getString("report_ad_imp_revenue");
        if (!TextUtils.isEmpty(value)) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
            }
        }
        return true;
    }

    protected void onReportAdImpData(Map<String, Object> adImpMap) {
        if (isReportAdImpData()) {
            if (adImpMap != null) {
                try {
                    adImpMap.put("vpn_status", Utils.isVPNConnected(mContext) ? "on" : "off");
                    adImpMap.put("active_days", EventImpl.get().getActiveDayString());
                    adImpMap.put("active_date", EventImpl.get().getActiveDate());
                    adImpMap.put("active_year", EventImpl.get().getActiveYear());
                    adImpMap.put("country", Utils.getCountryFromLocale(mContext));
                    adImpMap.put(Constant.AD_TYPE, getAdType());
                    if (EventImpl.get().getActiveDays() == 0) {
                        adImpMap.put(Constant.AD_PLACEMENT_NEW, adImpMap.get(Constant.AD_PLACEMENT));
                    }
                } catch (Exception e) {
                }
            }
            InternalStat.reportEvent(getContext(), Constant.AD_IMPRESSION_REVENUE, adImpMap);
        }
        if (adImpMap != null) {
            adImpMap.put(Constant.AD_REQUEST_ID, getRequestId());
        }
        AdImpData adImpData = AdImpData.createAdImpData(adImpMap);
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onAdImpression(adImpData);
        }
        AdStatManager.get(mContext).recordAdImpression(adImpData);
    }
}