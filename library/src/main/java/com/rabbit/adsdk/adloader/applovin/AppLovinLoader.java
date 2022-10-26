package com.rabbit.adsdk.adloader.applovin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
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
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.MView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/7/16.
 */

public class AppLovinLoader extends AbstractSdkLoader {

    private static AtomicBoolean sApplovinInited = new AtomicBoolean(false);
    private static AppLovinSdkSettings sAppLovinSdkSettings;
    private MaxNativeAdLoader mMaxNativeAdLoader;
    private MaxNativeAdView mTemplateNativeView;
    private MaxAd mMaxAd;
    private Params mParams;
    private ApplovinBindView mApplovinBindView = new ApplovinBindView();
    private MaxAppOpenAd mMaxAppOpenAd;

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
            Activity activity = MView.createFakeActivity((Application) context.getApplicationContext());
            AppLovinSdk appLovinSdk = getInstance(activity);
            if (appLovinSdk != null) {
                appLovinSdk.showMediationDebugger();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 提前初始化applovin，避免applovin被使用Application Context初始化
     *
     * @return
     */
    public static void initApplovin(Context context) {
        if (!sApplovinInited.getAndSet(true)) {
            try {
                Activity activity = MView.createFakeActivity((Application) context.getApplicationContext());
                AppLovinSdk appLovinSdk = getInstance(activity);
                if (appLovinSdk != null) {
                    appLovinSdk.setMediationProvider("max");
                    if (isDebugDevice(activity)) {
                        String gaid = Utils.getString(activity, Constant.PREF_GAID);
                        Log.iv(Log.TAG, "applovin debug mode gaid : " + gaid);
                        if (!TextUtils.isEmpty(gaid)) {
                            appLovinSdk.getSettings().setTestDeviceAdvertisingIds(Arrays.asList(new String[]{gaid}));
                        }
                    }
                    try {
                        appLovinSdk.getSettings().setVerboseLogging(isShowVerbose(activity));
                    } catch (Exception e) {
                    }
                    Log.iv(Log.TAG, "start initializing applovin sdk");
                    appLovinSdk.initializeSdk(config -> {
                        Log.iv(Log.TAG, "applovin sdk init successfully");
                        try {
                            if (isShowDebugger(activity)) {
                                appLovinSdk.showMediationDebugger();
                            }
                        } catch (Exception e) {
                        }
                    });
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
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
        boolean isDebug = false;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("applovin.sdk.config");
            if (applovinConfig != null) {
                try {
                    isDebug = Boolean.parseBoolean(applovinConfig.get("applovin_debug_device"));
                } catch (Exception e) {
                }
            }
        }
        return isDebug;
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

    private static AppLovinSdk getInstance(Activity activity) {
        if (activity == null) {
            Log.iv(Log.TAG, "applovin init activity is null");
            return null;
        }
        String sdkKey = getSdkKey(activity);
        if (TextUtils.isEmpty(sdkKey)) {
            Log.iv(Log.TAG, "applovin init applovin.sdk.key is null");
            return null;
        }
        if (sAppLovinSdkSettings == null) {
            sAppLovinSdkSettings = new AppLovinSdkSettings(activity);
        }
        return AppLovinSdk.getInstance(sdkKey, sAppLovinSdkSettings, activity);
    }

    @Override
    public boolean isBannerLoaded() {
        return isBannerLoadedForMax();
    }

    @Override
    public void loadBanner(int adSize) {
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
            notifyAdLoaded(this);
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
            notifyAdLoaded(this);
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
        try {
            return showRewardedVideoForMax(sceneName);
        } catch (Exception e) {
            notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, e != null ? e.getMessage() : null);
        }
        return false;
    }

    protected static final Map<Integer, MaxAdFormat> ADSIZE = new HashMap<>();

    static {
        ADSIZE.put(Constant.BANNER, MaxAdFormat.BANNER);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, MaxAdFormat.MREC);
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
        loadingMaxAdView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
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
                    reportAdLoaded();
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
                notifyAdImp(network);
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
                reportAdClick(network, networkPid);
                notifyAdClick(network);
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
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingMaxAdView.setPlacement(getSceneId());
        loadingMaxAdView.loadAd();
    }

    private void setRevenueCallback(MaxAdView maxAdView) {
        if (maxAdView != null) {
            maxAdView.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad revenue paid" + getLoadedInfo(ad)));
                    reportMaxAdImpData(ad, getAdPlaceName());
                }
            });
        }
    }

    private void showBannerForMax(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            reportAdShow();
            notifyAdShow();
            setRevenueCallback(maxAdView);
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
            Log.e(Log.TAG, formatShowErrorLog(String.valueOf(e)));
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
            interstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(interstitialAd);
                    String network = getNetwork(ad);
                    setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                    reportAdLoaded();
                    notifyAdLoaded(AppLovinLoader.this);
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
                    notifyAdImp(network);
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
                    reportAdClick(network, networkPid);
                    notifyAdClick(network);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                    clearLastShowTime();
                    onResetInterstitial();
                    notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
                }
            });

            interstitialAd.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad revenue paid"));
                    reportMaxAdImpData(ad, getAdPlaceName());
                }
            });
        }
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        interstitialAd.loadAd();
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
            Log.e(Log.TAG, formatShowErrorLog("MaxInterstitialAd not ready"));
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
        rewardedAd.setListener(new MaxRewardedAdListener() {
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
                reportAdLoaded();
                notifyAdLoaded(AppLovinLoader.this);
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
                notifyAdImp(network);
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
                reportAdClick(network, networkPid);
                notifyAdClick(network);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetReward();
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
            }
        });

        rewardedAd.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad, getAdPlaceName());
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        rewardedAd.loadAd();
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
            Log.e(Log.TAG, formatShowErrorLog("MaxRewardedAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "MaxRewardedAd not ready");
        }
        return false;
    }

    @Override
    public void loadNative(Params params) {
        mParams = params;
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
        if (mMaxNativeAdLoader == null) {
            mMaxNativeAdLoader = new MaxNativeAdLoader(getPid(), getInstance(activity), activity);
            mMaxNativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                @Override
                public void onNativeAdLoaded(MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(maxAd)));
                    mMaxAd = maxAd;
                    mTemplateNativeView = maxNativeAdView;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mMaxAd);
                    String network = getNetwork(maxAd);
                    setAdNetworkAndRevenue(network, getMaxAdRevenue(maxAd));
                    reportAdLoaded();
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
                    reportAdClick(network, networkPid);
                    notifyAdClick(network);
                }
            });
            mMaxNativeAdLoader.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad revenue paid"));
                    String network = getNetwork(ad);
                    String networkPid = getNetworkPid(ad);
                    reportAdImp(network, networkPid);
                    notifyAdImp(network);
                    reportMaxAdImpData(ad, getAdPlaceName());
                }
            });
        }
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMaxNativeAdLoader.setPlacement(getSceneId());
        mMaxNativeAdLoader.loadAd();
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
        if (params != null) {
            mParams = params;
        }
        printInterfaceLog(ACTION_SHOW);
        try {
            MaxNativeAdView maxNativeAdView = null;
            if (isTemplateRendering()) {
                maxNativeAdView = mTemplateNativeView;
            } else {
                if (mMaxNativeAdLoader != null && mParams != null && mMaxAd != null) {
                    maxNativeAdView = mApplovinBindView.bindMaxNativeAdView(getContext(), mParams, mPidConfig, mMaxAd.getNetworkName());
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
            Log.e(Log.TAG, formatShowErrorLog(String.valueOf(e)));
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
            notifyAdLoaded(this);
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
    public boolean showSplash(ViewGroup viewGroup) {
        try {
            return showSplashForMax(viewGroup);
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

            mMaxAppOpenAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(ad)));
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mMaxAppOpenAd);
                    String network = getNetwork(ad);
                    setAdNetworkAndRevenue(network, getMaxAdRevenue(ad));
                    reportAdLoaded();
                    notifyAdLoaded(AppLovinLoader.this);
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
                    notifyAdImp(network);
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
                    reportAdClick(network, networkPid);
                    notifyAdClick(network);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                    clearLastShowTime();
                    onResetSplash();
                    notifyAdShowFailed(Constant.AD_ERROR_SHOW, "[" + getNetwork(ad) + "]" + toErrorMessage(error));
                }
            });

            mMaxAppOpenAd.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd ad) {
                    Log.iv(Log.TAG, formatLog("ad revenue paid"));
                    reportMaxAdImpData(ad, getAdPlaceName());
                }
            });
        }
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMaxAppOpenAd.loadAd();
    }

    private boolean showSplashForMax(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        if (mMaxAppOpenAd != null && mMaxAppOpenAd.isReady()) {
            Log.iv(Log.TAG, "");
            reportAdShow();
            notifyAdShow();
            mMaxAppOpenAd.showAd(getSceneId());
            updateLastShowTime();
            return true;
        } else {
            onResetSplash();
            Log.e(Log.TAG, formatShowErrorLog("MaxAppOpenAd not ready"));
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

    private void reportMaxAdImpData(MaxAd maxAd, String placeName) {
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
            String placement = maxAd.getPlacement(); // The placement this ad's postbacks are tied to
            placement = getSceneId(placement);
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
            map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            onReportAdImpData(map);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
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
