package com.komob.adsdk.adloader.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.komob.adsdk.AdError;
import com.komob.adsdk.AdImpData;
import com.komob.adsdk.AdReward;
import com.komob.adsdk.InternalStat;
import com.komob.adsdk.OnAdEventListener;
import com.komob.adsdk.OnAdFilterListener;
import com.komob.adsdk.adloader.listener.IManagerListener;
import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.adloader.listener.OnAdBaseListener;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.AdPolicy;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.core.framework.AdLoadManager;
import com.komob.adsdk.core.framework.AdStatManager;
import com.komob.adsdk.core.framework.BounceRateManager;
import com.komob.adsdk.core.framework.FBStatManager;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.data.config.AdPlace;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.data.parse.IParser;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.stat.EventImpl;
import com.komob.adsdk.stat.IEvent;
import com.komob.adsdk.utils.Utils;
import com.komob.api.RFileConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 */

public abstract class AbstractSdkLoader implements ISdkLoader {

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
    private int mBannerSize = Constant.NO_SET;
    private IEvent mStat;
    private static final Random sRandom = new Random(System.currentTimeMillis());
    private long mLastFullScreenShowTime = 0;
    private final AtomicBoolean mLoadTimeout = new AtomicBoolean(false);
    private String mAdNetwork;
    private double mAdRevenue;
    private int mLoadState = STATE_NONE;

    private long mCostTime = 0;

    private boolean mCached = false;

    private static List<ISdkLoader> mAdLoaders = new ArrayList<ISdkLoader>();

    public AbstractSdkLoader() {
        mAdLoaders.add(this);
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
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
        });
    }

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        mContext = context;
        mPidConfig = pidConfig;
        mStat = EventImpl.get();
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
                activity = RFileConfig.getFA((Application) mContext.getApplicationContext());
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
    public boolean showSplash(ViewGroup viewGroup, String sceneName) {
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

        // 判断仅签名加载模式
        if (!isMatchSign()) {
            processSignNotMatch();
            return false;
        }

        // 判断仅包名加载模式
        if (!isMatchPack()) {
            processPackNotMatch();
            return false;
        }

        if (isBlockMistakeClick()) {
            processBlockMistakeClick();
            return false;
        }
        return true;
    }

    private void processExceedReqTimes() {
        long reqTimes = AdPolicy.get(mContext).getReqTimes(getAdPlaceName(), getSdkName());
        Log.iv(Log.TAG, formatLog("exceed max req times : " + reqTimes + "/" + getMaxReqTimes()));
        notifyAdLoadFailed(Constant.AD_ERROR_EXCEED_REQ_TIME, "exceed max req times");
    }

    private boolean isExceedReqTimes() {
        return AdPolicy.get(mContext).isExceedMaxReqTimes(getAdPlaceName(), getSdkName(), getMaxReqTimes());
    }

    private boolean isMatchSign() {
        if (mPidConfig != null && mPidConfig.isOnlySignLoad()) {
            // 如果配置签名加载，则必须完全匹配才返回true
            Collection<String> signList = DataManager.get(mContext).getSignList();
            String signMd5 = Utils.getSignMd5(mContext);
            if (signMd5 != null) {
                signMd5 = signMd5.toLowerCase(Locale.ENGLISH);
            }
            return signList != null && signMd5 != null && signList.contains(signMd5);
        }
        return true;
    }

    private void processSignNotMatch() {
        Log.iv(Log.TAG, formatLog("sign not match"));
        notifyAdLoadFailed(Constant.AD_ERROR_SIGN_NOT_MATCH, "sign not match");
    }

    private boolean isMatchPack() {
        if (mPidConfig != null && mPidConfig.isOnlyPackLoad()) {
            // 如果配置签名加载，则必须完全匹配才返回true
            Collection<String> packList = DataManager.get(mContext).getPackList();
            String packName = mContext != null ? mContext.getPackageName() : null;
            if (packName != null) {
                packName = packName.toLowerCase(Locale.ENGLISH);
            }
            return packList != null && packName != null && packList.contains(packName);
        }
        return true;
    }

    private void processPackNotMatch() {
        Log.iv(Log.TAG, formatLog("pack not match"));
        notifyAdLoadFailed(Constant.AD_ERROR_PACK_NOT_MATCH, "pack not match");
    }

    private boolean isBlockMistakeClick() {
        return BounceRateManager.get(mContext).blockMistakePid(getPid());
    }

    private void processBlockMistakeClick() {
        Log.iv(Log.TAG, formatLog("block mistake click"));
        notifyAdLoadFailed(Constant.AD_ERROR_BLOCK_MISTAKE_CLICK, "block mistake click");
    }

    protected void printInterfaceLog(String action) {
        Log.iv(Log.TAG, action + "|" + getSdkName() + "|" + getAdType() + "|" + getAdPlaceName() + "|" + getPid());
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
            Log.iv(Log.TAG, "error : " + e);
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
            Log.iv(Log.TAG, pref + " : " + System.currentTimeMillis());
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

    protected String getPid() {
        if (mPidConfig != null) {
            return mPidConfig.getPid();
        }
        return null;
    }

    protected String getAppId() {
        if (mPidConfig != null) {
            return mPidConfig.getAppId();
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
        return RFileConfig.getVersion();
    }

    protected String getAppVersion() {
        return Utils.getVersionName(mContext);
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
        setCached(cached);
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
        Log.iv(Log.TAG, "set revenue place name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , network : " + network + " , type : " + getAdType() + " , value : " + mAdRevenue);
    }

    protected void setSpreadRevenue(String network, double adRevenue) {
        mAdNetwork = network;
        mAdRevenue = adRevenue;
    }

    private void resetAdNetworkAndRevenue() {
        mAdNetwork = null;
        mAdRevenue = 0f;
    }

    @Override
    public double getRevenue() {
        return mAdRevenue;
    }

    @Override
    public long getCostTime() {
        return mCostTime;
    }

    @Override
    public String getNetwork() {
        return mAdNetwork;
    }

    @Override
    public void notifyBidResult(String adType, String firstPlatform, String firstNetwork, double firstPrice, String secondPlatform, String secondNetwork, double secondPrice) {
        // Log.iv(Log.TAG, "firstPlatform : " + firstPlatform + " , ad type : " + adType + " , first : " + firstNetwork + "|" + firstPrice + " , second : " + secondNetwork + "|" + secondPrice);
    }

    @Override
    public boolean isSlaveAds() {
        if (mPidConfig != null) {
            return mPidConfig.isSlaveAds();
        }
        return false;
    }

    @Override
    public boolean isCached() {
        return mCached;
    }

    @Override
    public void setCached(boolean cached) {
        mCached = cached;
    }

    protected String generateImpressionId() {
        return UUID.randomUUID().toString();
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
        reportAdLoaded(null);
    }

    protected void reportAdLoaded(String network) {
        if (mStat != null) {
            mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), network, getAdType(), getPid(), getCpm(), null);
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
            mStat.reportAdImp(mContext, getAdPlaceName(), getSdkName(), network, getAdType(), getPid(), networkPid, getCpm(), null);
            if (nativeAssets != null) {
                nativeAssets.clear();
            }
        }
    }

    protected void reportAdClick() {
        reportAdClick(getSdkName(), getPid(), null);
    }

    protected void reportAdClick(String network, String networkPid, String impressionId) {
        if (mStat != null) {
            Map<String, Object> extra = new HashMap<>();
            String adPlacement = DBManager.get(mContext).queryAdPlacement(impressionId);
            if (TextUtils.isEmpty(adPlacement)) {
                adPlacement = getAdPlaceName();
            }
            extra.put("placement", adPlacement);
            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), network, getAdType(), getPid(), networkPid, getCpm(), extra, impressionId);
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
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onRequest(getAdPlaceName(), getSdkName(), getAdType(), getPid());
        }
    }

    /**
     * banner or native loaded
     */
    private void notifyAdLoaded(ISdkLoader loader) {
        if (getAdListener() != null) {
            getAdListener().onAdLoaded(loader);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onLoaded(getAdPlaceName(), getSdkName(), getAdType(), getPid());
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
            l.onShow(getAdPlaceName(), getSdkName(), getAdType(), getPid());
        }
    }

    protected void notifyAdImp() {
        notifyAdImp(null, null);
    }

    /**
     * banner or native impression
     */
    protected void notifyAdImp(String network, String sceneName) {
        if (getAdListener() != null) {
            getAdListener().onAdImp(network, sceneName);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onImpression(getAdPlaceName(), getSdkName(), getAdType(), getPid(), sceneName);
        }
    }

    protected void notifyAdClick() {
        notifyAdClick(null, null);
    }

    /**
     * banner or native click
     */
    protected void notifyAdClick(String network, String impressionId) {
        if (getAdListener() != null) {
            getAdListener().onAdClick(network);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onClick(getAdPlaceName(), getSdkName(), getAdType(), getPid(), impressionId);
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
            l.onDismiss(getAdPlaceName(), getSdkName(), getAdType(), getPid());
        }
    }

    protected void notifyAdLoadFailed(AdError error, String msg) {
        if (getAdListener() != null && !mLoadTimeout.getAndSet(false)) {
            getAdListener().onAdLoadFailed(error, msg);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onLoadFailed(getAdPlaceName(), getSdkName(), getAdType(), getPid());
        }
    }

    protected void notifyAdShowFailed(AdError error, String msg) {
        if (getAdListener() != null) {
            getAdListener().onAdShowFailed(error, msg);
        }
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onShowFailed(getAdPlaceName(), getSdkName(), getAdType(), getPid(), msg);
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

    protected void onReportAdImpData(Map<String, Object> adImpMap, String impressionId) {
        if (adImpMap != null) {
            try {
                // adImpMap.put("vpn_status", Utils.isVPNConnected(mContext) ? "on" : "off");
                adImpMap.put("active_days", EventImpl.get().getActiveDayString());
                adImpMap.put("active_date", EventImpl.get().getActiveDate());
                adImpMap.put("active_year", EventImpl.get().getActiveYear());
                adImpMap.put("country", Utils.getCountryFromLocale(mContext));
                adImpMap.put(Constant.AD_TYPE, getAdType());
                try {
                    if (EventImpl.get().getActiveDays() == 0) {
                        adImpMap.put(Constant.AD_PLACEMENT_NEW, adImpMap.get(Constant.AD_PLACEMENT));
                        Double adRevenue = (Double) adImpMap.get(Constant.AD_VALUE);
                        String roundCpm = Utils.calcRoundCpm(adRevenue * 1000);
                        adImpMap.put(Constant.AD_ROUND_CPM_NEW, roundCpm);
                    }
                } catch (Exception e) {
                }
                try {
                    Double adRevenue = (Double) adImpMap.get(Constant.AD_VALUE);
                    String roundCpm = Utils.calcRoundCpm(adRevenue * 1000);
                    adImpMap.put(Constant.AD_ROUND_CPM, roundCpm);
                    String network = (String) adImpMap.get(Constant.AD_NETWORK);
                    adImpMap.put(String.format(Locale.ENGLISH, "%s_%s", Constant.AD_ROUND_CPM, Utils.formatNetwork(network)), roundCpm);
                } catch (Exception exception) {
                }
            } catch (Exception e) {
            }
        }
        InternalStat.reportEvent(getContext(), Constant.AD_IMPRESSION_REVENUE, adImpMap);
        FBStatManager.get(mContext).reportFirebaseImpression(adImpMap);
        if (adImpMap != null) {
            adImpMap.put(Constant.AD_IMPRESSION_ID, impressionId);
            adImpMap.put(Constant.AD_IMP_TIME, System.currentTimeMillis());
        }
        printImpData(adImpMap);
        AdImpData adImpData = AdImpData.createAdImpData(adImpMap);
        OnAdEventListener l = AdLoadManager.get(mContext).getOnAdEventListener();
        if (l != null) {
            l.onAdImpData(adImpData);
        }
        AdStatManager.get(mContext).recordAdImpression(adImpData);
        reportAdImpression(adImpData);
        reportTaichiEvent(adImpData);
        try {
            notifyBidResultInternal(adImpData);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        resetAdNetworkAndRevenue();
    }

    private void printImpData(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\n");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.append("  " + entry.getKey() + " : " + entry.getValue());
            builder.append("\n");
        }
        builder.append("}");
        Log.iv(Log.TAG, getSdkName() + " imp data : " + builder.toString());
    }

    private void notifyBidResultInternal(AdImpData adImpData) {
        String adType = adImpData.getAdType();
        String platform = adImpData.getPlatform();
        String network = Utils.formatNetwork(adImpData.getNetwork());
        double topRevenue = adImpData.getValue();
        String secondPlatform = null;
        String secondNetwork = null;
        double secondPrice = 0f;
        List<ISdkLoader> varList = find(adType);
        if (varList != null && !varList.isEmpty()) {
            if (varList.size() >= 2) {
                ISdkLoader iSdkLoader = varList.get(1);
                secondPrice = iSdkLoader.getRevenue();
                if (secondPrice > 0f) {
                    secondPlatform = iSdkLoader.getSdkName();
                    secondNetwork = Utils.formatNetwork(iSdkLoader.getNetwork());
                }
            }
            for (ISdkLoader iSdkLoader : varList) {
                if (iSdkLoader != null) {
                    iSdkLoader.notifyBidResult(adType, platform, network, topRevenue, secondPlatform, secondNetwork, secondPrice);
                }
            }
        }
    }

    private List<ISdkLoader> find(String adType) {
        if (TextUtils.isEmpty(adType)) {
            return null;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            return null;
        }
        List<ISdkLoader> varList = new ArrayList<>();
        for (ISdkLoader iSdkLoader : mAdLoaders) {
            if (iSdkLoader != null && TextUtils.equals(iSdkLoader.getAdType(), adType)) {
                varList.add(iSdkLoader);
            }
        }
        Collections.sort(varList, new Comparator<ISdkLoader>() {
            @Override
            public int compare(ISdkLoader o1, ISdkLoader o2) {
                try {
                    return Double.compare(o2.getRevenue(), o1.getRevenue());
                } catch (Exception e) {
                }
                return 0;
            }
        });
        return varList;
    }

    protected boolean viewInScreen(View view) {
        try {
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            Rect rect = new Rect();
            view.getLocalVisibleRect(rect);
            return !(rect.top < 0 || rect.bottom > dm.heightPixels);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 上报ad_impression事件，firebase通过此事件计算收入
     *
     * @param adImpData
     */
    private void reportAdImpression(AdImpData adImpData) {
        try {
            if (adImpData != null && isEnableReportAdImpression()) {
                String networkName = adImpData.getNetwork();
                boolean isReportFirebase = true;
                if (isForbidReportAdImpressionAdmob()) {
                    if (networkName != null) {
                        String temp = networkName.toLowerCase(Locale.ENGLISH);
                        if (temp != null && temp.contains("admob")) {
                            isReportFirebase = false;
                        }
                    }
                }
                String platform = adImpData.getPlatform();
                String unitName = adImpData.getUnitName();
                String placement = adImpData.getPlacement();
                String adType = adImpData.getAdType();
                String networkPid = adImpData.getNetworkPid();
                String unitId = adImpData.getUnitId();
                String adPrecision = adImpData.getPrecision();
                boolean adBidding = adImpData.isBidding();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("ad_platform", platform);
                params.put("ad_source", networkName);
                params.put("ad_format", adImpData.getAdFormat());
                params.put("ad_type", adType);
                params.put("ad_unit_name", unitName);
                params.put("ad_placement", placement);
                params.put("ad_network_pid", networkPid);
                params.put("ad_unit_id", unitId);
                params.put("ad_precision", adPrecision);
                params.put("ad_bidding", adBidding);
                params.put("value", adImpData.getValue());
                params.put("micro_value", Double.valueOf(adImpData.getValue() * 1000000).intValue());
                params.put("currency", "USD"); // All Applovin revenue is sent in USD
                if (isReportFirebase) {
                    InternalStat.sendFirebaseAnalytics(mContext, Constant.AD_IMPRESSION, null, params);
                }
            }
        } catch (Exception e) {
        }
    }

    private void reportTaichiEvent(AdImpData adImpData) {
        try {
            if (adImpData != null && isEnableReportTaichi30()) {
                Double revenue = adImpData.getValue();
                if (revenue != null && revenue.doubleValue() > 0) {
                    reportTaichiEvent(mContext, revenue.floatValue());
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 是否禁止上报admob广告展示价值，默认不禁止，只有当admob与firebase关联时，才需要禁止
     *
     * @return
     */
    private boolean isForbidReportAdImpressionAdmob() {
        boolean result = false;
        try {
            String str = DataManager.get(mContext).getString("ad_forbid_report_admob");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }

    /**
     * 上报taichi事件
     *
     * @param context
     * @param revenue
     */
    private void reportTaichiEvent(Context context, float revenue) {
        String prefRevenue = "pref_total_taichi_revenue";
        float lastTotalRevenue = Utils.getFloat(context, prefRevenue);
        float curTotalRevenue = lastTotalRevenue + revenue;
        Log.iv(Log.TAG, "last total revenue : " + lastTotalRevenue + " , current total revenue : " + curTotalRevenue + " , revenue : " + revenue);
        if (curTotalRevenue >= 0.01f) {
            Utils.putFloat(context, prefRevenue, 0f);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("currency", "USD");
            map.put("value", curTotalRevenue);
            map.put("micro_value", Double.valueOf(curTotalRevenue * 1000000).intValue());
            InternalStat.reportEvent(context, Constant.AD_TOTAL_ADS_REVENUE_001, map);
        } else {
            Utils.putFloat(context, prefRevenue, curTotalRevenue);
        }
    }

    /**
     * 是否允许上报ad_impression事件
     *
     * @return
     */
    private boolean isEnableReportAdImpression() {
        boolean result = true;
        try {
            String str = DataManager.get(mContext).getString("ad_enable_report_ad_impression");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }

    /**
     * 是否允许上报taichi30事件
     *
     * @return
     */
    private boolean isEnableReportTaichi30() {
        boolean result = true;
        try {
            String str = DataManager.get(mContext).getString("ad_enable_report_taichi30");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }

    public class AbstractAdListener {
        public String impressionId = null;
        public String sceneName = null;
    }


    public interface SDKInitializeListener {
        void onInitializeSuccess();

        void onInitializeFailure(String error);
    }

    public interface SDKInitializeState {
        int SDK_STATE_UN_INITIALIZE = 0;
        int SDK_STATE_INITIALIZING = 1;
        int SDK_STATE_INITIALIZE_SUCCESS = 2;
        int SDK_STATE_INITIALIZE_FAILURE = 3;
    }

    private CountDownTimer mStateChecker;

    protected int getSdkInitializeState() {
        return SDKInitializeState.SDK_STATE_UN_INITIALIZE;
    }

    protected void setSdkInitializeState(int state) {
    }

    protected void initializeSdk(SDKInitializeListener sdkInitializeListener) {
    }

    private long getInitTimeout() {
        return 30000;
    }

    private void checkSdkInitializeState(final SDKInitializeListener sdkInitializeListener) {
        if (mStateChecker == null) {
            Log.iv(Log.TAG, getSdkName() + " sdk init start checking");
            mStateChecker = new CountDownTimer(getInitTimeout(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.iv(Log.TAG, getSdkName() + " sdk init state check");
                    if (getSdkInitializeState() == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                        if (mStateChecker != null) {
                            mStateChecker.cancel();
                            mStateChecker = null;
                        }
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeSuccess();
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Log.iv(Log.TAG, getSdkName() + " sdk init timeout");
                    mStateChecker = null;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeFailure("timeout");
                    }
                }
            };
            mStateChecker.start();
        } else {
            Log.iv(Log.TAG, getSdkName() + " sdk initializing");
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeFailure("initializing");
            }
        }
    }

    protected void configSdkInit(final SDKInitializeListener sdkInitializeListener) {
        if (getSdkInitializeState() == SDKInitializeState.SDK_STATE_INITIALIZING) {
            checkSdkInitializeState(sdkInitializeListener);
        } else {
            if (getSdkInitializeState() == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeSuccess();
                }
                return;
            }
            setSdkInitializeState(SDKInitializeState.SDK_STATE_INITIALIZING);
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSdkInitializeState(SDKInitializeState.SDK_STATE_UN_INITIALIZE);
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeFailure("timeout");
                        }
                    }
                }, getInitTimeout());
            }
            initializeSdk(new SDKInitializeListener() {
                @Override
                public void onInitializeSuccess() {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    setSdkInitializeState(SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS);
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess();
                    }
                }

                @Override
                public void onInitializeFailure(String error) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    setSdkInitializeState(SDKInitializeState.SDK_STATE_INITIALIZE_FAILURE);
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeFailure(error);
                    }
                }
            });
        }
    }
}