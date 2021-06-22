package com.rabbit.adsdk.adloader.applovin;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.applovin.adview.AppLovinAdView;
import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
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
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/7/16.
 */

public class AppLovinLoader extends AbstractSdkLoader {

    private static boolean sApplovinInited = false;
    private static AppLovinSdkSettings sAppLovinSdkSettings;

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        if (!sApplovinInited) {
            sApplovinInited = true;
            AppLovinSdk appLovinSdk = getInstance();
            if (appLovinSdk != null) {
                try {
                    appLovinSdk.setMediationProvider("max");
                } catch (Exception e) {
                }
                appLovinSdk.initializeSdk(new AppLovinSdk.SdkInitializationListener() {
                    @Override
                    public void onSdkInitialized(AppLovinSdkConfiguration config) {
                        Log.iv(Log.TAG, "applovin sdk init successfully");
                    }
                });
                if (isDebug()) {
                    String gaid = Utils.getString(mContext, Constant.PREF_GAID);
                    Log.iv(Log.TAG, "applovin debug mode gaid : " + gaid);
                    if (!TextUtils.isEmpty(gaid)) {
                        appLovinSdk.getSettings().setTestDeviceAdvertisingIds(Arrays.asList(new String[]{gaid}));
                    }
                }
            }
        }
    }

    public String getSdkName() {
        return Constant.AD_SDK_APPLOVIN;
    }

    private boolean isDebug() {
        boolean isDebug = false;
        Map<String, Map<String, String>> config = DataManager.get(mContext).getMediationConfig();
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

    private String getSdkKey() {
        String applovinSdkKey = null;
        Map<String, Map<String, String>> config = DataManager.get(mContext).getMediationConfig();
        if (config != null) {
            Map<String, String> applovinConfig = config.get("com.mopub.mobileads.AppLovinAdapterConfiguration");
            if (applovinConfig != null) {
                applovinSdkKey = applovinConfig.get("sdk_key");
            }
        }
        return applovinSdkKey;
    }

    private AppLovinSdk getInstance() {
        String sdkKey = getSdkKey();
        Log.iv(Log.TAG, "applovin sdk key : " + sdkKey);
        if (TextUtils.isEmpty(sdkKey)) {
            return null;
        }
        if (sAppLovinSdkSettings == null) {
            sAppLovinSdkSettings = new AppLovinSdkSettings(mContext);
        }
        return AppLovinSdk.getInstance(sdkKey, sAppLovinSdkSettings, mContext);
    }

    private boolean isApplovinMax() {
        PidConfig pidConfig = getPidConfig();
        if (pidConfig != null) {
            return pidConfig.isMax();
        }
        return false;
    }

    @Override
    public boolean isBannerLoaded() {
        if (isApplovinMax()) {
            return isBannerLoadedForMax();
        }
        return isBannerLoadedNormal();
    }

    @Override
    public void loadBanner(int adSize) {
        AppLovinSdk appLovinSdk = getInstance();
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        if (isApplovinMax()) {
            loadBannerForMax(adSize, appLovinSdk);
        } else {
            loadBannerNormal(adSize, appLovinSdk);
        }
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        if (isApplovinMax()) {
            showBannerForMax(viewGroup);
        } else {
            showBannerNormal(viewGroup);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (isApplovinMax()) {
            return isInterstitialLoadedForMax();
        }
        return isInterstitialLoadedNormal();
    }

    @Override
    public void loadInterstitial() {
        AppLovinSdk appLovinSdk = getInstance();
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        if (isApplovinMax()) {
            loadInterstitialForMax(appLovinSdk, activity);
        } else {
            loadInterstitialNormal(appLovinSdk, activity);
        }
    }

    @Override
    public boolean showInterstitial() {
        if (isApplovinMax()) {
            return showInterstitialForMax();
        }
        return showInterstitialNormal();
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        if (isApplovinMax()) {
            return isRewardedVideoLoadedForMax();
        }
        return isRewardedVideoLoadedNormal();
    }

    @Override
    public void loadRewardedVideo() {
        AppLovinSdk appLovinSdk = getInstance();
        if (appLovinSdk == null) {
            Log.iv(Log.TAG, formatLog("error applovin_sdk_key"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        if (isApplovinMax()) {
            loadRewardedVideoForMax(appLovinSdk, activity);
        } else {
            loadRewardedVideoNormal(appLovinSdk, activity);
        }
    }

    @Override
    public boolean showRewardedVideo() {
        if (isApplovinMax()) {
            return showRewardedVideoForMax();
        }
        return showRewardedVideoNormal();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    private AppLovinAd loadedAd;
    private AppLovinInterstitialAdDialog interstitialAdDialog;

    private AppLovinAdView appLovinAdView;
    private AppLovinAdView loadingAdView;
    private AppLovinAdView lastUseAdView;

    private AppLovinIncentivizedInterstitial incentivizedInterstitial;

    private boolean isBannerLoadedNormal() {
        boolean loaded = appLovinAdView != null && !isCachedAdExpired(appLovinAdView);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadBannerNormal(int adSize, AppLovinSdk appLovinSdk) {
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        loadingAdView = new AppLovinAdView(appLovinSdk, AppLovinAdSize.BANNER, mPidConfig.getPid(), mContext);
        loadingAdView.setAdLoadListener(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingAdView);
                appLovinAdView = loadingAdView;
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void failedToReceiveAd(int i) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(i), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                notifyAdFailed(Constant.AD_ERROR_LOAD);
            }
        });

        loadingAdView.setAdDisplayListener(new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                Log.iv(Log.TAG, formatLog("ad displayed"));
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                Log.iv(Log.TAG, formatLog("ad hidden"));
                reportAdClose();
                notifyAdDismiss();
                if (lastUseAdView != null) {
                    lastUseAdView.pause();
                }
            }
        });

        loadingAdView.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }
        });
        loadingAdView.loadNextAd();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
    }

    private void showBannerNormal(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            clearCachedAdTime(appLovinAdView);
            viewGroup.removeAllViews();
            ViewParent viewParent = appLovinAdView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(appLovinAdView);
            }
            viewGroup.addView(appLovinAdView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseAdView = appLovinAdView;
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
        } catch (Exception e) {
            Log.e(Log.TAG, "applovin loader error : " + e);
        }
    }

    private boolean isInterstitialLoadedNormal() {
        boolean loaded = super.isInterstitialLoaded();
        if (loadedAd != null && interstitialAdDialog != null) {
            loaded = !isCachedAdExpired(loadedAd);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadInterstitialNormal(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        if (interstitialAdDialog == null) {
            interstitialAdDialog = AppLovinInterstitialAd.create(appLovinSdk, activity);
        }
        if (interstitialAdDialog != null) {
            interstitialAdDialog.setAdClickListener(new AppLovinAdClickListener() {
                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick();
                    notifyAdClick();
                }
            });
            interstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad displayed"));
                    reportAdImp();
                    notifyAdImp();
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad hidden"));
                    loadedAd = null;
                    reportAdClose();
                    notifyAdDismiss();
                }
            });
        }
        try {
            appLovinSdk.getAdService().loadNextAdForZoneId(mPidConfig.getPid(), new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad load success"));
                    loadedAd = appLovinAd;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(loadedAd);
                    reportAdLoaded();
                    notifyAdLoaded(AppLovinLoader.this);
                }

                @Override
                public void failedToReceiveAd(int i) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(i), true));
                    setLoading(false, STATE_FAILURE);
                    reportAdError(codeToError(i));
                    notifyAdFailed(Constant.AD_ERROR_LOAD);
                }
            });
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("ad load failed : " + String.valueOf(e), true));
            setLoading(false, STATE_FAILURE);
            reportAdError(String.valueOf(e));
            notifyAdFailed(Constant.AD_ERROR_LOAD);
        }
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
    }

    private boolean showInterstitialNormal() {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAdDialog != null && loadedAd != null) {
            interstitialAdDialog.showAndRender(loadedAd);
            clearCachedAdTime(loadedAd);
            loadedAd = null;
            reportAdShow();
            return true;
        }
        return false;
    }

    private boolean isRewardedVideoLoadedNormal() {
        boolean loaded = super.isRewardedVideoLoaded();
        if (incentivizedInterstitial != null) {
            loaded = incentivizedInterstitial.isAdReadyToDisplay() && !isCachedAdExpired(incentivizedInterstitial);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void loadRewardedVideoNormal(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(mPidConfig.getPid(), appLovinSdk);
        incentivizedInterstitial.preload(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(incentivizedInterstitial);
                reportAdLoaded();
                notifyAdLoaded(AppLovinLoader.this);
            }

            @Override
            public void failedToReceiveAd(int i) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(i), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                notifyAdFailed(Constant.AD_ERROR_LOAD);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
    }

    private boolean showRewardedVideoNormal() {
        printInterfaceLog(ACTION_SHOW);
        if (incentivizedInterstitial != null && incentivizedInterstitial.isAdReadyToDisplay()) {
            incentivizedInterstitial.show(mContext, new AppLovinAdRewardListener() {
                @Override
                public void userRewardVerified(AppLovinAd appLovinAd, Map<String, String> map) {
                    Log.iv(Log.TAG, formatLog("ad reward verified"));
                    AdReward item = null;
                    try {
                        String currencyName = (String) map.get("currency");
                        String amountGivenString = (String) map.get("amount");
                        item = new AdReward();
                        item.setAmount(amountGivenString);
                        item.setType(currencyName);
                    } catch (Exception e) {
                    }
                    reportAdReward();
                    notifyRewarded(item);
                }

                @Override
                public void userOverQuota(AppLovinAd appLovinAd, Map<String, String> map) {
                }

                @Override
                public void userRewardRejected(AppLovinAd appLovinAd, Map<String, String> map) {
                }

                @Override
                public void validationRequestFailed(AppLovinAd appLovinAd, int i) {
                }

                @Override
                public void userDeclinedToViewAd(AppLovinAd appLovinAd) {
                }
            }, new AppLovinAdVideoPlaybackListener() {

                @Override
                public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad playback began"));
                    notifyRewardAdsStarted();
                }

                @Override
                public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                    Log.iv(Log.TAG, formatLog("ad playback ended"));
                    notifyRewardAdsCompleted();
                }
            }, new AppLovinAdDisplayListener() {

                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad displayed"));
                    reportAdImp();
                    notifyAdOpened();
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad hidden"));
                    reportAdClose();
                    notifyAdDismiss();
                }
            }, new AppLovinAdClickListener() {

                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick();
                    notifyAdClick();
                }
            });
            clearCachedAdTime(incentivizedInterstitial);
            incentivizedInterstitial = null;
            reportAdShow();
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
                putCachedAdTime(loadingMaxAdView);
                maxAdView = loadingMaxAdView;
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(error));
                notifyAdFailed(Constant.AD_ERROR_LOAD);
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
            }
        });

        loadingMaxAdView.loadAd();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
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
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("show banner error : " + e));
        }
    }

    private boolean isInterstitialLoadedForMax() {
        boolean loaded = false;
        if (interstitialAd != null) {
            loaded = interstitialAd.isReady() && !isCachedAdExpired(interstitialAd);
        }
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
                clearResetTimer();
                onResetInterstitial();
                reportAdError(codeToError(error));
                notifyAdFailed(Constant.AD_ERROR_LOAD);
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
                clearResetTimer();
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
                clearResetTimer();
                onResetInterstitial();
            }
        });

        loadingInterstitialAd.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingInterstitialAd.loadAd();
    }

    private boolean showInterstitialForMax() {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAd != null) {
            Log.v(Log.TAG, "");
            interstitialAd.showAd();
            setResetTimer();
            reportAdShow();
            notifyAdShow();
            return true;
        }
        return false;
    }

    private boolean isRewardedVideoLoadedForMax() {
        boolean loaded = rewardedAd != null && !isCachedAdExpired(rewardedAd);
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
                clearResetTimer();
                onResetReward();
                reportAdError(codeToError(error));
                notifyAdFailed(Constant.AD_ERROR_LOAD);
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
                clearResetTimer();
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
                clearResetTimer();
                onResetReward();
            }
        });

        loadingRewardedAd.setRevenueListener(new MaxAdRevenueListener() {
            @Override
            public void onAdRevenuePaid(MaxAd ad) {
                Log.iv(Log.TAG, formatLog("ad revenue paid"));
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingRewardedAd.loadAd();
    }

    private boolean showRewardedVideoForMax() {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedAd != null && rewardedAd.isReady()) {
            rewardedAd.showAd();
            setResetTimer();
            reportAdShow();
            notifyAdShow();
            return true;
        }
        return false;
    }

    @Override
    public void resume() {
        Log.iv(Log.TAG, "resume ...");
        if (lastUseAdView != null) {
            lastUseAdView.resume();
        }
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.startAutoRefresh();
        }
    }

    @Override
    public void pause() {
        Log.iv(Log.TAG, "pause ...");
        if (lastUseAdView != null) {
            lastUseAdView.pause();
        }
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.stopAutoRefresh();
        }
    }

    @Override
    public void destroy() {
        if (lastUseAdView != null) {
            lastUseAdView.destroy();
        }
        if (lastUseMaxAdView != null) {
            lastUseMaxAdView.destroy();
        }
    }

    @Override
    protected void onResetInterstitial() {
        clearCachedAdTime(interstitialAd);
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    @Override
    protected void onResetReward() {
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
}
