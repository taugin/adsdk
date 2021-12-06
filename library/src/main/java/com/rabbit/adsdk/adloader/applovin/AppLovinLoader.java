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
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
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

    /**
     * 提前初始化applovin，避免applovin被mopub使用Application Context初始化
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
                    appLovinSdk.initializeSdk(config -> Log.iv(Log.TAG, "applovin sdk init successfully"));
                    if (isDebug(activity)) {
                        String gaid = Utils.getString(activity, Constant.PREF_GAID);
                        Log.iv(Log.TAG, "applovin debug mode gaid : " + gaid);
                        if (!TextUtils.isEmpty(gaid)) {
                            appLovinSdk.getSettings().setTestDeviceAdvertisingIds(Arrays.asList(new String[]{gaid}));
                        }
                    }
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

    private static boolean isDebug(Context context) {
        boolean isDebug = false;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> commonConfig = config.get("common_config");
            if (commonConfig != null) {
                try {
                    isDebug = Boolean.parseBoolean(commonConfig.get("applovin_debug"));
                } catch (Exception e) {
                }
            }
        }
        return isDebug;
    }

    private static String getSdkKey(Context context) {
        String applovinSdkKey = null;
        Map<String, Map<String, String>> config = DataManager.get(context).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("com.mopub.mobileads.AppLovinAdapterConfiguration");
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadInterstitialForMax(appLovinSdk, activity);
    }

    @Override
    public boolean showInterstitial() {
        return showInterstitialForMax();
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }
        AppLovinSdk appLovinSdk = getInstance(activity);
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        loadRewardedVideoForMax(appLovinSdk, activity);
    }

    @Override
    public boolean showRewardedVideo() {
        return showRewardedVideoForMax();
    }

    protected static final Map<Integer, MaxAdFormat> ADSIZE = new HashMap<>();

    static {
        ADSIZE.put(Constant.BANNER, MaxAdFormat.BANNER);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, MaxAdFormat.MREC);
    }

    private MaxInterstitialAd interstitialAd;
    private MaxInterstitialAd loadingInterstitialAd;

    private MaxAdView loadingMaxAdView;
    private MaxAdView maxAdView;
    private MaxAdView lastUseMaxAdView;

    private MaxRewardedAd rewardedAd;
    private MaxRewardedAd loadingRewardedAd;

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
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                maxAdView = loadingMaxAdView;
                putCachedAdTime(loadingMaxAdView);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(error));
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad displayed"));
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
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                notifyAdShowFailed(error != null ? error.getCode() : Constant.AD_ERROR_SHOW);
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

        loadingMaxAdView.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad);
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingMaxAdView.loadAd();
    }

    private void showBannerForMax(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
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
            maxAdView = null;
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("show banner error : " + e));
        }
    }

    private boolean isInterstitialLoadedForMax() {
        boolean loaded = interstitialAd != null && !isCachedAdExpired(interstitialAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadInterstitialForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        loadingInterstitialAd = new MaxInterstitialAd(getPid(), appLovinSdk, activity);
        loadingInterstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                interstitialAd = loadingInterstitialAd;
                putCachedAdTime(loadingInterstitialAd);
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
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad displayed"));
                reportAdImp();
                notifyAdImp();
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
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetInterstitial();
                notifyAdShowFailed(error != null ? error.getCode() : Constant.AD_ERROR_SHOW);
            }
        });

        loadingInterstitialAd.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad);
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingInterstitialAd.loadAd();
    }

    private boolean showInterstitialForMax() {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAd != null) {
            Log.v(Log.TAG, "");
            interstitialAd.showAd();
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetInterstitial();
        }
        return false;
    }

    private boolean isRewardedVideoLoadedForMax() {
        boolean loaded = rewardedAd != null && !isCachedAdExpired(rewardedAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadRewardedVideoForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        loadingRewardedAd = MaxRewardedAd.getInstance(getPid(), appLovinSdk, activity);
        loadingRewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.v(Log.TAG, "");
                notifyRewardAdsStarted();
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.v(Log.TAG, "");
                notifyRewardAdsCompleted();
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                Log.v(Log.TAG, "");
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
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                rewardedAd = loadingRewardedAd;
                putCachedAdTime(loadingRewardedAd);
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
                notifyAdLoadFailed(Constant.AD_ERROR_LOAD);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad displayed"));
                reportAdImp();
                notifyAdOpened();
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
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad display failed : " + error));
                clearLastShowTime();
                onResetReward();
                notifyAdShowFailed(error != null ? error.getCode() : Constant.AD_ERROR_SHOW);
            }
        });

        loadingRewardedAd.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
                reportMaxAdImpData(ad);
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingRewardedAd.loadAd();
    }

    private boolean showRewardedVideoForMax() {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedAd != null && rewardedAd.isReady()) {
            rewardedAd.showAd();
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetReward();
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
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(rewardedAd);
        if (rewardedAd != null) {
            rewardedAd.destroy();
            rewardedAd = null;
        }
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

    private void reportMaxAdImpData(MaxAd maxAd) {
        try {
            double revenue = maxAd.getRevenue(); // In USD
            String countryCode = getInstance(getActivity()).getConfiguration().getCountryCode(); // "US" for the United States, etc - Note: Do not confuse this with currency code which is "USD" in most cases!
            String networkName = maxAd.getNetworkName(); // Display name of the network that showed the ad (e.g. "AdColony")
            String adUnitId = maxAd.getAdUnitId(); // The MAX Ad Unit ID
            MaxAdFormat adFormat = maxAd.getFormat(); // The ad format of the ad (e.g. BANNER, MREC, INTERSTITIAL, REWARDED)
            String placement = maxAd.getPlacement(); // The placement this ad's postbacks are tied to
            String placementId = maxAd.getNetworkPlacement();
            Map<String, Object> map = new HashMap<>();
            map.put("value", revenue);
            map.put("ad_network", networkName);
            map.put("ad_network_pid", placementId);
            map.put("ad_unit_id", adUnitId);
            map.put("ad_format", adFormat.getDisplayName());
            map.put("ad_unit_name", placement);
            map.put("ad_provider", getSdkName());
            map.put("ad_country_code", countryCode);
            String gaid = Utils.getString(mContext, Constant.PREF_GAID);
            map.put("ad_gaid", gaid);
            if (isReportAdImpData()) {
                InternalStat.reportEvent(getContext(), "Ad_Impression_Revenue", map);
            }
            Log.iv(Log.TAG, "applovin max imp data : " + map);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
}
