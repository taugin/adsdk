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
import com.applovin.mediation.MaxAdViewAdListener;
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

    private static AppLovinSdkSettings sAppLovinSdkSettings;

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
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
            appLovinSdk.getSettings().setTestDeviceAdvertisingIds(Arrays.asList("YOUR_GAID_HERE"));
        }
    }

    public String getSdkName() {
        return Constant.AD_SDK_APPLOVIN;
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
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType() + " , empty applovin_sdk_key");
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isBannerLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
            }
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
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType() + " , empty applovin_sdk_key");
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.v(Log.TAG, "applovin interstitial need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
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
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType() + " , empty applovin_sdk_key");
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.v(Log.TAG, "applovin reward need an activity context");
            if (getAdListener() != null) {
                getAdListener().onRewardedVideoError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onRewardedVideoAdLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
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

    private AppLovinIncentivizedInterstitial incentivizedInterstitial;

    private boolean isBannerLoadedNormal() {
        boolean loaded = appLovinAdView != null && !isCachedAdExpired(appLovinAdView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
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
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingAdView);
                appLovinAdView = loadingAdView;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void failedToReceiveAd(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
            }
        });

        loadingAdView.setAdDisplayListener(new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }
        });

        loadingAdView.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    appLovinAdView = null;
                }
            }
        });
        loadingAdView.loadNextAd();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    private void showBannerNormal(ViewGroup viewGroup) {
        Log.v(Log.TAG, "applovin loader");
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
            if (!isDestroyAfterClick()) {
                appLovinAdView = null;
            }
            reportAdShow();
            reportAdImp();
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
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private void loadInterstitialNormal(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        if (interstitialAdDialog == null) {
            interstitialAdDialog = AppLovinInterstitialAd.create(appLovinSdk, activity);
            interstitialAdDialog.setAdClickListener(new AppLovinAdClickListener() {
                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdClick();
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialClick();
                    }
                }
            });
            interstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdImp();
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialImp();
                    }
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    loadedAd = null;
                    reportAdClose();
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialDismiss();
                    }
                }
            });
        }
        try {
            appLovinSdk.getAdService().loadNextAdForZoneId(mPidConfig.getPid(), new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                    loadedAd = appLovinAd;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(loadedAd);
                    reportAdLoaded();
                    if (getAdListener() != null) {
                        setLoadedFlag();
                        getAdListener().onInterstitialLoaded(AppLovinLoader.this);
                    }
                }

                @Override
                public void failedToReceiveAd(int i) {
                    Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                    setLoading(false, STATE_FAILURE);
                    reportAdError(codeToError(i));
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                    }
                }
            });
        } catch (Exception e) {
            Log.v(Log.TAG, "reason : " + String.valueOf(e) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
            setLoading(false, STATE_FAILURE);
            reportAdError(String.valueOf(e));
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
            }
        }
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    private boolean showInterstitialNormal() {
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
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private void loadRewardedVideoNormal(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(mPidConfig.getPid(), appLovinSdk);
        incentivizedInterstitial.preload(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(incentivizedInterstitial);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(AppLovinLoader.this);
                }
            }

            @Override
            public void failedToReceiveAd(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
            }
        });
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    private boolean showRewardedVideoNormal() {
        if (incentivizedInterstitial != null && incentivizedInterstitial.isAdReadyToDisplay()) {
            incentivizedInterstitial.show(mContext, new AppLovinAdRewardListener() {
                @Override
                public void userRewardVerified(AppLovinAd appLovinAd, Map<String, String> map) {
                    Log.v(Log.TAG, "");
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
                    if (getAdListener() != null) {
                        getAdListener().onRewarded(item);
                    }
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
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoStarted();
                    }
                }

                @Override
                public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoCompleted();
                    }
                }
            }, new AppLovinAdDisplayListener() {

                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdImp();
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdOpened();
                    }
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdClose();
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdClosed();
                    }
                }
            }, new AppLovinAdClickListener() {

                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdClick();
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdClicked();
                    }
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

    private MaxAdView loadingMaxAdView;

    private MaxAdView maxAdView;

    static private MaxRewardedAd rewardedAd;

    private boolean isBannerLoadedForMax() {
        boolean loaded = maxAdView != null && !isCachedAdExpired(maxAdView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
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
        Log.iv(Log.TAG, "applovin max banner size : " + maxAdFormat + " , adSize : " + adSize);
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
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingMaxAdView);
                maxAdView = loadingMaxAdView;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    maxAdView = null;
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, int errorCode) {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdExpanded(MaxAd ad) {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
                Log.v(Log.TAG, "");
            }
        });
        loadingMaxAdView.loadAd();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    private void showBannerForMax(ViewGroup viewGroup) {
        Log.v(Log.TAG, "applovin loader");
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
            if (!isDestroyAfterClick()) {
                maxAdView = null;
            }
            reportAdShow();
            reportAdImp();
        } catch (Exception e) {
            Log.e(Log.TAG, "applovin loader error : " + e);
        }
    }

    private boolean isInterstitialLoadedForMax() {
        boolean loaded = false;
        if (interstitialAd != null) {
            loaded = interstitialAd.isReady() && !isCachedAdExpired(interstitialAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private void loadInterstitialForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        interstitialAd = new MaxInterstitialAd(getPid(), appLovinSdk, activity);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(AppLovinLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialImp();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, int errorCode) {
                Log.v(Log.TAG, "applovin max interstitial display failed error : " + codeToError(errorCode));
            }
        });
        reportAdRequest();
        Log.v(Log.TAG, "");
        interstitialAd.loadAd();
    }

    private boolean showInterstitialForMax() {
        if (interstitialAd != null) {
            Log.v(Log.TAG, "");
            reportAdShow();
            interstitialAd.showAd();
            clearCachedAdTime(interstitialAd);
            return true;
        }
        return false;
    }

    private boolean isRewardedVideoLoadedForMax() {
        boolean loaded = false;
        if (rewardedAd != null) {
            loaded = rewardedAd.isReady() && !isCachedAdExpired(rewardedAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private void loadRewardedVideoForMax(AppLovinSdk appLovinSdk, Activity activity) {
        setLoading(true, STATE_REQUEST);
        rewardedAd = MaxRewardedAd.getInstance(getPid(), appLovinSdk, activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }
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
                if (getAdListener() != null) {
                    getAdListener().onRewarded(item);
                }
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(rewardedAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(AppLovinLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdOpened();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, int errorCode) {
                Log.v(Log.TAG, "applovin max reward video display failed error : " + codeToError(errorCode));
            }
        });

        Log.v(Log.TAG, "");
        reportAdRequest();
        rewardedAd.loadAd();
    }

    private boolean showRewardedVideoForMax() {
        Log.v(Log.TAG, "ready to show applovin max reward video");
        if (rewardedAd != null && rewardedAd.isReady()) {
            Log.v(Log.TAG, "");
            reportAdShow();
            rewardedAd.showAd();
            clearCachedAdTime(rewardedAd);
            return true;
        }
        return false;
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
}
