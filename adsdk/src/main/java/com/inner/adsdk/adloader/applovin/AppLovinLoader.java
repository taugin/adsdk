package com.inner.adsdk.adloader.applovin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.applovin.adview.AppLovinAdView;
import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

import java.util.Map;

/**
 * Created by Administrator on 2018/7/16.
 */

public class AppLovinLoader extends AbstractSdkLoader {

    private static final String SDK_KEY = "aqULsa4oR7H9uQJrVI4-hGyFMgAc_RwZFPv6-zvQ5AZzbup5At4t806UgH6fi1DzQ74O5zpA8N8kGyqRquIyuO";

    private AppLovinAd loadedAd;
    private AppLovinInterstitialAdDialog interstitialAdDialog;

    private AppLovinAdView appLovinAdView;
    private AppLovinAdView loadingAdView;

    private AppLovinIncentivizedInterstitial incentivizedInterstitial;

    private AppLovinAdView gAppLovinAdView;

    private static AppLovinSdkSettings sAppLovinSdkSettings;

    @Override
    public void init(Context context) {
        super.init(context);
        AppLovinSdk.initializeSdk(context);
    }

    public String getSdkName() {
        return Constant.AD_SDK_APPLOVIN;
    }

    private AppLovinSdk getInstance() {
        if (sAppLovinSdkSettings == null) {
            sAppLovinSdkSettings = new AppLovinSdkSettings(mContext);
        }
        return AppLovinSdk.getInstance(SDK_KEY, sAppLovinSdkSettings, mContext);
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = appLovinAdView != null && !isCachedAdExpired(appLovinAdView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadBanner(int adSize) {
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
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (loadingAdView != null) {
                    loadingAdView.destroy();
                    clearCachedAdTime(loadingAdView);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        loadingAdView = new AppLovinAdView(getInstance(), AppLovinAdSize.BANNER, mPidConfig.getPid(), mContext);
        loadingAdView.setAdLoadListener(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
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
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                reportAdError(codeToError(i));
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
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
                reportAdClose();
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

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "applovinloader");
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
            gAppLovinAdView = appLovinAdView;
            if (!isDestroyAfterClick()) {
                appLovinAdView = null;
            }
            reportAdShow();
        } catch (Exception e) {
            Log.e(Log.TAG, "applovinloader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (loadedAd != null && interstitialAdDialog != null) {
            loaded = !isCachedAdExpired(loadedAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadInterstitial() {
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
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (loadedAd != null) {
                    clearCachedAdTime(loadedAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        if (interstitialAdDialog == null) {
            interstitialAdDialog = AppLovinInterstitialAd.create(getInstance(), mContext);
            interstitialAdDialog.setAdClickListener(new AppLovinAdClickListener() {
                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialClick();
                    }
                    reportAdClick();
                }
            });
            interstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    reportAdShow();
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialShow();
                    }
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    loadedAd = null;
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialDismiss();
                    }
                    reportAdClose();
                }
            });
        }
        try {
            getInstance().getAdService().loadNextAdForZoneId(mPidConfig.getPid(), new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
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
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                    }
                    reportAdError(codeToError(i));
                }
            });
        } catch (Exception e) {
            Log.v(Log.TAG, "reason : " + String.valueOf(e) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
            setLoading(false, STATE_FAILURE);
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
            }
            reportAdError(String.valueOf(e));
        }
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (interstitialAdDialog != null && loadedAd != null) {
            interstitialAdDialog.showAndRender(loadedAd);
            clearCachedAdTime(loadedAd);
            loadedAd = null;
            reportAdCallShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (incentivizedInterstitial != null) {
            loaded = incentivizedInterstitial.isAdReadyToDisplay() && !isCachedAdExpired(incentivizedInterstitial);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isRewaredVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onRewardedVideoAdLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (incentivizedInterstitial != null) {
                    clearCachedAdTime(incentivizedInterstitial);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(mPidConfig.getPid(), getInstance());
        incentivizedInterstitial.preload(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
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
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                reportAdError(codeToError(i));
            }
        });
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showRewardedVideo() {
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
                    if (getAdListener() != null) {
                        getAdListener().onRewarded(item);
                    }
                    reportAdReward();
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
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdShowed();
                    }
                    reportAdShow();
                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdClosed();
                    }
                    reportAdClose();
                }
            }, new AppLovinAdClickListener() {

                @Override
                public void adClicked(AppLovinAd appLovinAd) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onRewardedVideoAdClicked();
                    }
                    reportAdClick();
                }
            });
            clearCachedAdTime(incentivizedInterstitial);
            incentivizedInterstitial = null;
            reportAdCallShow();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        if (gAppLovinAdView != null) {
            gAppLovinAdView.destroy();
            gAppLovinAdView = null;
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
}
