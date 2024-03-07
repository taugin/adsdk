package com.komob.adsdk.adloader.applovin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.komob.adsdk.AdReward;
import com.komob.adsdk.adloader.base.AbstractSdkLoader;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.utils.VUIHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/7/16.
 * 由于bidding需要获取到令牌，因此要先完成applovin的初始化，然后再进行广告加载
 */

public class AppLovinLoader extends AbstractSdkLoader {

    private static AtomicBoolean sApplovinInited = new AtomicBoolean(false);

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static AppLovinSdkSettings sAppLovinSdkSettings;

    private static int sSDKInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
    private MaxNativeAdLoader mMaxNativeAdLoader;
    private MaxNativeAdView mTemplateNativeView;
    private MaxAd mMaxAd;
    private ApplovinBindView mApplovinBindView = new ApplovinBindView();
    private MaxAppOpenAd mMaxAppOpenAd;
    private MaxNativeListener maxNativeListener;
    private MaxSplashListener maxSplashListener;
    private MaxInterstitialListener maxInterstitialListener;
    private MaxRewardListener maxRewardListener;

    @Override
    protected BaseBindNativeView getBaseBindNativeView() {
        return mApplovinBindView;
    }

    /**
     * 显示applovin中介调试界面
     *
     * @param context
     */
    public static void showApplovinMediationDebugger(Context context) {
        try {
            Activity activity = VUIHelper.getFA((Application) context.getApplicationContext());
            AppLovinSdk appLovinSdk = getInstance(activity);
            if (appLovinSdk != null) {
                appLovinSdk.showMediationDebugger();
            }
        } catch (Exception | Error e) {
        }
    }

    /**
     * 提前初始化applovin，避免applovin被使用Application Context初始化
     *
     * @return
     */
    private void initApplovin(SDKInitializeListener sdkInitializeListener) {
        if (!sApplovinInited.getAndSet(true)) {
            try {
                AppLovinSdk appLovinSdk = getInstance(mContext);
                if (appLovinSdk != null) {
                    appLovinSdk.setMediationProvider("max");
                    if (isDebugDevice(mContext)) {
                        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
                        Log.iv(Log.TAG, "applovin debug mode gaid : " + gaid);
                        if (!TextUtils.isEmpty(gaid)) {
                            appLovinSdk.getSettings().setTestDeviceAdvertisingIds(Arrays.asList(new String[]{gaid}));
                        }
                    }
                    try {
                        appLovinSdk.getSettings().setVerboseLogging(isShowVerbose(mContext));
                    } catch (Exception e) {
                    }
                    Log.iv(Log.TAG, "start initializing " + getSdkName() + " sdk");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sdkInitializeListener != null) {
                                sdkInitializeListener.onInitializeSuccess();
                            }
                        }
                    }, 15000);
                    final long startInit = SystemClock.elapsedRealtime();
                    appLovinSdk.initializeSdk(config -> {
                        Log.iv(Log.TAG, getSdkName() + " sdk init successfully cost time : " + (SystemClock.elapsedRealtime() - startInit));
                        mHandler.removeCallbacksAndMessages(null);
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeSuccess();
                        }
                        try {
                            if (isShowDebugger(mContext)) {
                                appLovinSdk.showMediationDebugger();
                            }
                        } catch (Exception e) {
                        }
                    });
                }
            } catch (Exception e) {
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeFailure("" + e);
                }
            }
        } else {
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeSuccess();
            }
        }
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        initBannerSize();
    }

    public String getSdkName() {
        return Constant.AD_SDK_APPLOVIN;
    }

    /**
     * 设置为调试设备
     *
     * @param context
     * @return
     */
    private static boolean isDebugDevice(Context context) {
        return DataManager.get(context).isApplovinInTestMode();
    }

    /**
     * 是否输出verbose信息
     *
     * @param context
     * @return
     */
    private static boolean isShowVerbose(Context context) {
        boolean isShowVerbose = false;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("applovin.sdk.config");
            if (applovinConfig != null) {
                try {
                    isShowVerbose = Boolean.parseBoolean(applovinConfig.get("applovin_show_verbose"));
                } catch (Exception e) {
                }
            }
        }
        return isShowVerbose;
    }

    private static boolean isShowDebugger(Context context) {
        boolean isShowVerbose = false;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("applovin.sdk.config");
            if (applovinConfig != null) {
                try {
                    isShowVerbose = Boolean.parseBoolean(applovinConfig.get("applovin_show_debugger"));
                } catch (Exception e) {
                }
            }
        }
        return isShowVerbose;
    }

    private static String getSdkKey(Context context) {
        String applovinSdkKey = null;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("applovin.sdk.config");
            if (applovinConfig == null) {
                applovinConfig = config.get("com.mopub.mobileads.AppLovinAdapterConfiguration");
            }
            if (applovinConfig != null) {
                applovinSdkKey = applovinConfig.get("sdk_key");
            }
        }
        if (TextUtils.isEmpty(applovinSdkKey)) {
            applovinSdkKey = Utils.getMetaData(context, "applovin.sdk.key");
        }
        return applovinSdkKey;
    }

    private static AppLovinSdk getInstance(Context context) {
        String sdkKey = getSdkKey(context);
        if (TextUtils.isEmpty(sdkKey)) {
            sdkKey = "L7OrRia7Fum7esJFM51m6xd799x4HmN4iNA6H9I7PhlFH_NqVTDDu87T7R58p4gAR3xJNedZzM-0HBT1XwUEv7";
        }
        if (sAppLovinSdkSettings == null) {
            sAppLovinSdkSettings = new AppLovinSdkSettings(context);
        }
        return AppLovinSdk.getInstance(sdkKey, sAppLovinSdkSettings, context);
    }

    @Override
    protected void initializeSdk(SDKInitializeListener sdkInitializeListener) {
        initApplovin(sdkInitializeListener);
    }

    @Override
    protected int getSdkInitializeState() {
        if (sSDKInitializeState != SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
            AppLovinSdk appLovinSdk = getInstance(mContext);
            if (appLovinSdk != null && appLovinSdk.isInitialized()) {
                return SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS;
            }
        }
        return sSDKInitializeState;
    }

    @Override
    protected void setSdkInitializeState(int state) {
        sSDKInitializeState = state;
    }

    @Override
    public boolean isBannerLoaded() {
        return isBannerLoadedForMax();
    }

    @Override
    public void loadBanner(final int adSize) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadBannerInternal(adSize);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadBannerInternal(int adSize) {
        AppLovinSdk appLovinSdk = getInstance(getActivity());
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadBannerForMax(adSize, appLovinSdk);
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        showBannerForMax(viewGroup);
    }

    @Override
    public boolean isInterstitialLoaded() {
        return isInterstitialLoadedForMax();
    }

    @Override
    public void loadInterstitial() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadInterstitialInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadInterstitialInternal() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadInterstitialForMax(appLovinSdk, activity);
    }

    @Override
    public boolean showInterstitial(String sceneName) {
        if (maxInterstitialListener != null) {
            maxInterstitialListener.sceneName = sceneName;
        }
        try {
            return showInterstitialForMax(sceneName);
        } catch (Exception e) {
            notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, e != null ? e.getMessage() : null);
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        return isRewardedVideoLoadedForMax();
    }

    @Override
    public void loadRewardedVideo() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadRewardedVideoInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadRewardedVideoInternal() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadRewardedVideoForMax(appLovinSdk, activity);
    }

    @Override
    public boolean showRewardedVideo(String sceneName) {
        if (maxRewardListener != null) {
            maxRewardListener.sceneName = sceneName;
        }
        try {
            return showRewardedVideoForMax(sceneName);
        } catch (Exception e) {
            notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, e != null ? e.getMessage() : null);
        }
        return false;
    }

    protected static final Map<Integer, MaxAdFormat> ADSIZE = new HashMap<>();

    private void initBannerSize() {
        try {
            ADSIZE.put(Constant.BANNER, MaxAdFormat.BANNER);
            ADSIZE.put(Constant.MEDIUM_RECTANGLE, MaxAdFormat.MREC);
        } catch (Exception | Error e) {
        }
    }

    private MaxInterstitialAd interstitialAd;

    private MaxAdView loadingMaxAdView;
    private MaxAdView maxAdView;
    private MaxAdView lastUseMaxAdView;

    private MaxRewardedAd rewardedAd;

    private boolean isBannerLoadedForMax() {
        boolean loaded = maxAdView != null && !isCachedAdExpired(maxAdView);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadBannerForMax(int adSize, AppLovinSdk appLovinSdk) {
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        MaxAdFormat maxAdFormat = ADSIZE.get(adSize);
        if (maxAdFormat == null) {
            maxAdFormat = MaxAdFormat.BANNER;
        }
        Log.iv(Log.TAG, formatLog("size : " + maxAdFormat + " , adSize : " + adSize));
        loadingMaxAdView = new MaxAdView(mPidConfig.getPid(), maxAdFormat, appLovinSdk, getActivity());
        if (maxAdFormat == MaxAdFormat.BANNER) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = Utils.dp2px(mContext, 50);
            loadingMaxAdView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        } else if (maxAdFormat == MaxAdFormat.MREC) {
            int width = Utils.dp2px(mContext, 300);
            int height = Utils.dp2px(mContext, 250);
            loadingMaxAdView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        }
        MaxBannerListener maxBannerListener = new MaxBannerListener();
        loadingMaxAdView.setListener(maxBannerListener.maxAdViewAdListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingMaxAdView.setRevenueListener(maxBannerListener.maxAdRevenueListener);
        loadingMaxAdView.setPlacement(getSceneId());
        loadingMaxAdView.loadAd();
    }

    private class MaxBannerListener extends AbstractAdListener {
        MaxAdViewAdListener maxAdViewAdListener = new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                if (!isStateSuccess()) {
                    if (lastUseMaxAdView != null) {
                        lastUseMaxAdView.setRevenueListener(null);
                    }
                    if (loadingMaxAdView != null) {
                        loadingMaxAdView.stopAutoRefresh();
                    }
                    maxAdView = loadingMaxAdView;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(loadingMaxAdView);
                    String network = getNetwork(ad);
                    setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                    reportAdLoaded(network);
                    notifySdkLoaderLoaded(false);
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(error));
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD, toErrorMessage(error));
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad displayed network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network, getFinalSceneName(ad, sceneName));
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad hidden"));
                reportAdClose();
                notifyAdDismiss();
                if (lastUseMaxAdView != null) {
                    lastUseMaxAdView.stopAutoRefresh();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
            }

            @Override
            public void onAdExpanded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad expanded"));
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad collapsed"));
            }
        };

        private MaxAdRevenueListener maxAdRevenueListener = new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                if (viewInScreen(lastUseMaxAdView)) {
                    impressionId = generateImpressionId();
                    Log.iv(Log.TAG, formatLog("ad revenue paid" + getLoadedInfo(ad)));
                    reportMaxAdImpData(ad, getAdPlaceName(), impressionId, sceneName);
                }
            }
        };
    }

    private void showBannerForMax(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            reportAdShow();
            notifyAdShow();
            clearCachedAdTime(maxAdView);
            viewGroup.removeAllViews();
            ViewParent viewParent = maxAdView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(maxAdView);
            }
            viewGroup.addView(maxAdView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseMaxAdView = maxAdView;
            if (loadingMaxAdView != null) {
                loadingMaxAdView.startAutoRefresh();
            }
            maxAdView = null;
        } catch (Exception e) {
            Log.iv(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxAdView not ready");
        }
    }

    private boolean isInterstitialLoadedForMax() {
        boolean loaded = interstitialAd != null && interstitialAd.isReady() && !isCachedAdExpired(interstitialAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadInterstitialForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        if (interstitialAd == null) {
            interstitialAd = new MaxInterstitialAd(getPid(), appLovinSdk, activity);
        }
        maxInterstitialListener = new MaxInterstitialListener();
        interstitialAd.setListener(maxInterstitialListener.maxAdListener);
        interstitialAd.setRevenueListener(maxInterstitialListener.maxAdRevenueListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        interstitialAd.loadAd();
    }

    private class MaxInterstitialListener extends AbstractAdListener {
        MaxAdListener maxAdListener = new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                String network = getNetwork(ad);
                setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                reportAdLoaded(network);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(codeToError(error));
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD, toErrorMessage(error));
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad displayed network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network, getFinalSceneName(ad, sceneName));
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad hidden"));
                clearLastShowTime();
                onResetInterstitial();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetInterstitial();
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
            }
        };

        private MaxAdRevenueListener maxAdRevenueListener = new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                impressionId = generateImpressionId();
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad, getAdPlaceName(), impressionId, sceneName);
            }
        };
    }

    private boolean showInterstitialForMax(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAd != null && interstitialAd.isReady()) {
            Log.iv(Log.TAG, "");
            reportAdShow();
            notifyAdShow();
            interstitialAd.showAd(getSceneId(sceneName));
            updateLastShowTime();
            return true;
        } else {
            onResetInterstitial();
            Log.iv(Log.TAG, formatShowErrorLog("MaxInterstitialAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxInterstitialAd not ready");
        }
        return false;
    }

    private boolean isRewardedVideoLoadedForMax() {
        boolean loaded = rewardedAd != null && rewardedAd.isReady() && !isCachedAdExpired(rewardedAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadRewardedVideoForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        rewardedAd = MaxRewardedAd.getInstance(getPid(), appLovinSdk, activity);
        maxRewardListener = new MaxRewardListener();
        rewardedAd.setListener(maxRewardListener.maxRewardedAdListener);
        rewardedAd.setRevenueListener(maxRewardListener.maxAdRevenueListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        rewardedAd.loadAd();
    }

    private class MaxRewardListener extends AbstractAdListener {
        MaxRewardedAdListener maxRewardedAdListener = new MaxRewardedAdListener() {
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.iv(Log.TAG, "");
                notifyRewardAdsStarted();
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.iv(Log.TAG, "");
                notifyRewardAdsCompleted();
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                Log.iv(Log.TAG, "");
                AdReward item = null;
                try {
                    String currencyName = reward.getLabel();
                    String amountGivenString = String.valueOf(reward.getAmount());
                    item = new AdReward();
                    item.setAmount(amountGivenString);
                    item.setType(currencyName);
                } catch (Exception e) {
                }
                reportAdReward();
                notifyRewarded(item);
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(rewardedAd);
                String network = getNetwork(ad);
                setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                reportAdLoaded(network);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(codeToError(error));
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD, toErrorMessage(error));
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad displayed network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdOpened();
                notifyAdImp(network, getFinalSceneName(ad, sceneName));
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad hidden"));
                clearLastShowTime();
                onResetReward();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetReward();
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
            }
        };

        private MaxAdRevenueListener maxAdRevenueListener = new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                impressionId = generateImpressionId();
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad, getAdPlaceName(), impressionId, sceneName);
            }
        };
    }

    private boolean showRewardedVideoForMax(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedAd != null && rewardedAd.isReady()) {
            reportAdShow();
            notifyAdShow();
            rewardedAd.showAd(getSceneId(sceneName));
            updateLastShowTime();
            return true;
        } else {
            onResetReward();
            Log.iv(Log.TAG, formatShowErrorLog("MaxRewardedAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxRewardedAd not ready");
        }
        return false;
    }

    @Override
    public void loadNative(final Params params) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadNativeInternal(params);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadNativeInternal(Params params) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        setLoading(true, STATE_REQUEST);
        mMaxNativeAdLoader = new MaxNativeAdLoader(getPid(), getInstance(activity), activity);
        maxNativeListener = new MaxNativeListener();
        mMaxNativeAdLoader.setNativeAdListener(maxNativeListener.maxNativeAdListener);
        mMaxNativeAdLoader.setRevenueListener(maxNativeListener.maxAdRevenueListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMaxNativeAdLoader.setPlacement(getSceneId());
        mMaxNativeAdLoader.loadAd();
    }

    private class MaxNativeListener extends AbstractAdListener {
        MaxNativeAdListener maxNativeAdListener = new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(maxAd)));
                mMaxAd = maxAd;
                mTemplateNativeView = maxNativeAdView;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mMaxAd);
                String network = getNetwork(maxAd);
                setAdNetworkAndRevenue(network, getMaxAdRevenue(maxAd));
                reportAdLoaded(network);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onNativeAdLoadFailed(String s, MaxError maxError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + maxError, true));
                reportAdError(s);
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(Constant.AD_ERROR_NOFILL, toErrorMessage(maxError));
            }

            @Override
            public void onNativeAdClicked(MaxAd maxAd) {
                String network = getNetwork(maxAd);
                String networkPid = getNetworkPid(maxAd);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid, impressionId);
                notifyAdClick(network, impressionId);
            }
        };
        private MaxAdRevenueListener maxAdRevenueListener = new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                impressionId = generateImpressionId();
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                reportAdImp(network, networkPid);
                notifyAdImp(network, getFinalSceneName(ad, sceneName));
                reportMaxAdImpData(ad, getAdPlaceName(), impressionId, sceneName);
            }
        };
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = mMaxAd != null && !isCachedAdExpired(mMaxAd);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        if (params != null && maxNativeListener != null) {
            maxNativeListener.sceneName = params.getSceneName();
        }
        printInterfaceLog(ACTION_SHOW);
        try {
            MaxNativeAdView maxNativeAdView = null;
            if (isTemplateRendering()) {
                maxNativeAdView = mTemplateNativeView;
            } else {
                if (mMaxNativeAdLoader != null && params != null && mMaxAd != null) {
                    maxNativeAdView = mApplovinBindView.bindMaxNativeAdView(getContext(), params, mPidConfig, mMaxAd.getNetworkName());
                    mMaxNativeAdLoader.render(maxNativeAdView, mMaxAd);
                }
            }
            if (maxNativeAdView != null) {
                reportAdShow();
                notifyAdShow();
                viewGroup.removeAllViews();
                ViewParent viewParent = maxNativeAdView.getParent();
                if (viewParent instanceof ViewGroup) {
                    ((ViewGroup) viewParent).removeView(maxNativeAdView);
                }
                viewGroup.addView(maxNativeAdView);
                if (viewGroup.getVisibility() != View.VISIBLE) {
                    viewGroup.setVisibility(View.VISIBLE);
                }
                if (!isTemplateRendering()) {
                    mApplovinBindView.updateApplovinNative(getContext(), maxNativeAdView, mPidConfig);
                }
            } else {
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxNativeAdView is null");
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, e != null ? e.getMessage() : null);
        }
        clearCachedAdTime(mMaxAd);
        mMaxAd = null;
    }

    @Override
    public boolean isSplashLoaded() {
        return isSplashLoadedForMax();
    }

    @Override
    public void loadSplash() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadSplashInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadSplashInternal() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isSplashLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadSplashForMax(appLovinSdk, activity);
    }

    @Override
    public boolean showSplash(ViewGroup viewGroup, String sceneName) {
        if (maxSplashListener != null) {
            maxSplashListener.sceneName = sceneName;
        }
        try {
            return showSplashForMax(viewGroup, sceneName);
        } catch (Exception e) {
            notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, e != null ? e.getMessage() : null);
        }
        return false;
    }

    private boolean isSplashLoadedForMax() {
        boolean loaded = mMaxAppOpenAd != null && mMaxAppOpenAd.isReady() && !isCachedAdExpired(mMaxAppOpenAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadSplashForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        if (mMaxAppOpenAd == null) {
            mMaxAppOpenAd = new MaxAppOpenAd(getPid(), appLovinSdk);
        }
        maxSplashListener = new MaxSplashListener();
        mMaxAppOpenAd.setListener(maxSplashListener.maxAdListener);
        mMaxAppOpenAd.setRevenueListener(maxSplashListener.maxAdRevenueListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMaxAppOpenAd.loadAd();
    }

    private class MaxSplashListener extends AbstractAdListener {
        MaxAdListener maxAdListener = new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mMaxAppOpenAd);
                String network = getNetwork(ad);
                setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                reportAdLoaded(network);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetSplash();
                reportAdError(codeToError(error));
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD, toErrorMessage(error));
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad displayed network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network, getFinalSceneName(ad, sceneName));
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad hidden"));
                clearLastShowTime();
                onResetSplash();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                String network = getNetwork(ad);
                String networkPid = getNetworkPid(ad);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetSplash();
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
            }
        };

        private MaxAdRevenueListener maxAdRevenueListener = new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                impressionId = generateImpressionId();
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad, getAdPlaceName(), impressionId, sceneName);
            }
        };
    }

    private boolean showSplashForMax(ViewGroup viewGroup, String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (mMaxAppOpenAd != null && mMaxAppOpenAd.isReady()) {
            Log.iv(Log.TAG, "");
            reportAdShow();
            notifyAdShow();
            mMaxAppOpenAd.showAd(getSceneId(sceneName));
            updateLastShowTime();
            return true;
        } else {
            onResetSplash();
            Log.iv(Log.TAG, formatShowErrorLog("MaxAppOpenAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxAppOpenAd not ready");
        }
        return false;
    }

    @Override
    public void resume() {
        Log.iv(Log.TAG, "resume ...");
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.startAutoRefresh();
        }
    }

    @Override
    public void pause() {
        Log.iv(Log.TAG, "pause ...");
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.stopAutoRefresh();
        }
    }

    @Override
    public void destroy() {
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.destroy();
        }
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(interstitialAd);
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(rewardedAd);
    }

    @Override
    protected void onResetSplash() {
        super.onResetSplash();
        clearCachedAdTime(mMaxAppOpenAd);
    }

    private String codeToError(int code) {
        if (code == AppLovinErrorCodes.NO_FILL) {
            return "NO_FILL[" + code + "]";
        }
        if (code == AppLovinErrorCodes.NO_NETWORK) {
            return "NO_NETWORK[" + code + "]";
        }
        return "UNKNOWN[" + code + "]";
    }

    private String codeToError(MaxError error) {
        int code = -1;
        if (error != null) {
            code = error.getCode();
            if (code == AppLovinErrorCodes.NO_FILL) {
                return "NO_FILL[" + code + "]";
            }
            if (code == AppLovinErrorCodes.NO_NETWORK) {
                return "NO_NETWORK[" + code + "]";
            }
        }
        return "UNKNOWN[" + code + "]";
    }

    private String toErrorMessage(MaxError error) {
        if (error != null) {
            return "[" + error.getCode() + "] " + error.getMessage();
        }
        return null;
    }

    private String getFinalSceneName(MaxAd maxAd, String sceneName) {
        String placement = maxAd.getPlacement(); // The placement this ad's postbacks are tied to
        placement = getSceneId(!TextUtils.isEmpty(sceneName) ? sceneName : placement);
        return placement;
    }

    private void reportMaxAdImpData(MaxAd maxAd, String placeName, String impressionId, String sceneName) {
        try {
            // applovin给出的是单次展示的价值，换算ecpm需要乘以1000
            double revenue = maxAd.getRevenue(); // In USD
            String countryCode = "US";
            try {
                countryCode = getInstance(getActivity()).getConfiguration().getCountryCode(); // "US" for the United States, etc - Note: Do not confuse this with currency code which is "USD" in most cases!
            } catch (Exception e) {
            }
            String networkName = maxAd.getNetworkName(); // Display name of the network that showed the ad (e.g. "AdColony")
            String adUnitId = maxAd.getAdUnitId(); // The MAX Ad Unit ID
            MaxAdFormat adFormat = maxAd.getFormat(); // The ad format of the ad (e.g. BANNER, MREC, INTERSTITIAL, REWARDED)
            String placement = getFinalSceneName(maxAd, sceneName); // The placement this ad's postbacks are tied to
            String placementId = maxAd.getNetworkPlacement();
            String precision = maxAd.getRevenuePrecision();
            Map<String, Object> map = new HashMap<>();
            map.put(Constant.AD_VALUE, revenue);
            map.put(Constant.AD_MICRO_VALUE, Double.valueOf(revenue * 1000000).intValue());
            map.put(Constant.AD_CURRENCY, "USD");
            map.put(Constant.AD_NETWORK, networkName);
            map.put(Constant.AD_NETWORK_PID, placementId);
            map.put(Constant.AD_UNIT_ID, adUnitId);
            map.put(Constant.AD_FORMAT, adFormat.getLabel());
            map.put(Constant.AD_UNIT_NAME, placeName);
            map.put(Constant.AD_PLACEMENT, placement);
            map.put(Constant.AD_PLATFORM, getSdkName());
            map.put(Constant.AD_PRECISION, precision);
            map.put(Constant.AD_COUNTRY_CODE, countryCode);
            map.put(Constant.AD_SDK_VERSION, getSdkVersion());
            map.put(Constant.AD_APP_VERSION, getAppVersion());
            // map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            onReportAdImpData(map, impressionId);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private double getMaxAdRevenue(MaxAd maxAd) {
        try {
            return maxAd.getRevenue();
        } catch (Exception e) {
        }
        return 0f;
    }

    private String getLoadedInfo(MaxAd maxAd) {
        String networkName = null;
        String placement = null;
        String adRevenue = null;
        if (maxAd != null) {
            networkName = maxAd.getNetworkName();
            placement = maxAd.getNetworkPlacement();
            adRevenue = String.valueOf(getMaxAdRevenue(maxAd));
        }
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(networkName)) {
            builder.append("ad_network : " + networkName);
        }
        if (builder.length() > 0) {
            builder.append(" , ");
        }
        if (!TextUtils.isEmpty(placement)) {
            builder.append("ad_network_id : " + placement);
        }
        if (builder.length() > 0) {
            builder.append(" , ");
        }
        if (!TextUtils.isEmpty(adRevenue)) {
            builder.append("ad_revenue : " + adRevenue);
        }
        if (builder.length() > 0) {
            return " - " + builder.toString();
        }
        return builder.toString();
    }

    private String getNetwork(MaxAd maxAd) {
        if (maxAd != null) {
            return maxAd.getNetworkName();
        }
        return null;
    }

    private String getNetworkPid(MaxAd maxAd) {
        if (maxAd != null) {
            return maxAd.getNetworkPlacement();
        }
        return null;
    }
}
