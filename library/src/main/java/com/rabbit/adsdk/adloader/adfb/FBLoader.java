package com.rabbit.adsdk.adloader.adfb;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 */

public class FBLoader extends AbstractSdkLoader {

    private static final HashMap<Integer, AdSize> ADSIZE = new HashMap<Integer, AdSize>();
    private static AtomicBoolean sFacebookInited = new AtomicBoolean(false);

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER_HEIGHT_50);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.BANNER_HEIGHT_90);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.RECTANGLE_HEIGHT_250);
    }

    private InterstitialAd fbInterstitial;
    private InterstitialAd fbLoadingInterstitial;

    private NativeAd nativeAd;
    private AdView bannerView;
    private AdView loadingView;

    private RewardedVideoAd rewardedVideoAd;
    private RewardedVideoAd loadingRewardedVideoAd;

    private Params mParams;
    private NativeAd lastUseNativeAd;
    private AdView lastUseBannerView;
    private FBBindNativeView fbBindNativeView = new FBBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return fbBindNativeView;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        if (!sFacebookInited.getAndSet(true)) {
            AudienceNetworkAds.initialize(context);
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!matchNoFillTime()) {
            Log.iv(Log.TAG, formatLog("nofill error"));
            notifyAdLoadFailed(Constant.AD_ERROR_FILLTIME, "no fill error");
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

        setLoading(true, STATE_REQUEST);
        AdSize size = ADSIZE.get(adSize);
        setBannerSize(adSize);
        if (size == null) {
            size = AdSize.BANNER_HEIGHT_50;
        }
        loadingView = new AdView(mContext, mPidConfig.getPid(), size);
        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage(), true));
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(getError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                bannerView = loadingView;
                putCachedAdTime(loadingView);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp();
                notifyAdImp();
            }
        };
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        AdView.AdViewLoadConfig adViewLoadConfig = loadingView.buildLoadAdConfig().withAdListener(adListener).build();
        loadingView.loadAd(adViewLoadConfig);
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = bannerView != null && !isCachedAdExpired(bannerView);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            reportAdShow();
            notifyAdShow();
            clearCachedAdTime(bannerView);
            viewGroup.removeAllViews();
            ViewParent viewParent = bannerView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(bannerView);
            }
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseBannerView = bannerView;
            bannerView = null;
        } catch (Exception e) {
            Log.e(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = fbInterstitial != null && !isCachedAdExpired(fbInterstitial) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadInterstitial() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!matchNoFillTime()) {
            Log.iv(Log.TAG, formatLog("nofill error"));
            notifyAdLoadFailed(Constant.AD_ERROR_FILLTIME, "no fill error");
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

        setLoading(true, STATE_REQUEST);
        fbLoadingInterstitial = new InterstitialAd(mContext, mPidConfig.getPid());
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad interstitial displayed"));
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad interstitial dismissed"));
                clearLastShowTime();
                onResetInterstitial();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage(), true));
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(getError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                fbInterstitial = fbLoadingInterstitial;
                fbLoadingInterstitial = null;
                putCachedAdTime(fbInterstitial);
                reportAdLoaded();
                notifyAdLoaded(FBLoader.this);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad logging impression"));
                reportAdImp();
                notifyAdImp();
            }
        };

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        InterstitialAd.InterstitialLoadAdConfig loadConfig = fbLoadingInterstitial.buildLoadAdConfig().withAdListener(interstitialAdListener).build();
        fbLoadingInterstitial.loadAd(loadConfig);
    }

    @Override
    public boolean showInterstitial(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (fbInterstitial != null && fbInterstitial.isAdLoaded()) {
            reportAdShow();
            notifyAdShow();
            fbInterstitial.show();
            updateLastShowTime();
            return true;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("InterstitialAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : InterstitialAd not ready");
            onResetInterstitial();
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (nativeAd != null) {
            loaded = nativeAd.isAdLoaded() && !isCachedAdExpired(nativeAd);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadNative(Params params) {
        mParams = params;
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!matchNoFillTime()) {
            Log.iv(Log.TAG, formatLog("nofill error"));
            notifyAdLoadFailed(Constant.AD_ERROR_FILLTIME, "no fill error");
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
        printInterfaceLog(ACTION_LOAD);
        nativeAd = new NativeAd(mContext, mPidConfig.getPid());
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage(), true));
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(getError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(nativeAd);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad logging impression"));
                reportAdImp();
                notifyAdImp();
            }
        };
        reportAdRequest();
        notifyAdRequest();
        NativeAd.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
        nativeAd.loadAd(loadAdConfig);
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        if (nativeAd != null) {
            reportAdShow();
            notifyAdShow();
            clearCachedAdTime(nativeAd);
            fbBindNativeView.bindFBNative(mParams, viewGroup, nativeAd, mPidConfig);
            lastUseNativeAd = nativeAd;
            nativeAd = null;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("NativeAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : NativeAd is null");
        }
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (!matchNoFillTime()) {
            Log.iv(Log.TAG, formatLog("nofill error"));
            notifyAdLoadFailed(Constant.AD_ERROR_FILLTIME, "no fill error");
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

        setLoading(true, STATE_REQUEST);
        loadingRewardedVideoAd = new RewardedVideoAd(mContext, mPidConfig.getPid());
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoCompleted() {
                Log.iv(Log.TAG, formatLog("ad reward complete"));
                reportAdReward();
                notifyRewardAdsCompleted();

                AdReward adReward = new AdReward();
                adReward.setType(Constant.ECPM);
                adReward.setAmount(String.valueOf(getEcpm()));
                notifyRewarded(adReward);
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad logging impression"));
                reportAdImp();
                notifyAdImp();
            }

            @Override
            public void onRewardedVideoClosed() {
                Log.iv(Log.TAG, formatLog("ad reward closed"));
                clearLastShowTime();
                onResetReward();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage(), true));
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(getError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                rewardedVideoAd = loadingRewardedVideoAd;
                loadingRewardedVideoAd = null;
                putCachedAdTime(rewardedVideoAd);
                reportAdLoaded();
                notifyAdLoaded(FBLoader.this);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }
        };
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        RewardedVideoAd.RewardedVideoLoadAdConfig loadAdConfig = loadingRewardedVideoAd.buildLoadAdConfig().withAdListener(rewardedVideoAdListener).build();
        loadingRewardedVideoAd.loadAd(loadAdConfig);
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = rewardedVideoAd != null && !rewardedVideoAd.isAdInvalidated() && !isShowTimeExpired()/* && !isCachedAdExpired(rewardedVideoAd)*/;
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedVideoAd != null && rewardedVideoAd.isAdLoaded() && !rewardedVideoAd.isAdInvalidated()) {
            reportAdShow();
            notifyAdShow();
            rewardedVideoAd.show();
            updateLastShowTime();
            return true;
        } else {
            onResetReward();
            Log.e(Log.TAG, formatShowErrorLog("RewardedVideoAd not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : RewardedVideoAd not ready");
        }
        return false;
    }

    @Override
    public void destroy() {
        if (lastUseBannerView != null) {
            lastUseBannerView.destroy();
            lastUseBannerView = null;
        }
        if (lastUseNativeAd != null) {
            lastUseNativeAd.destroy();
            lastUseNativeAd = null;
        }
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(fbInterstitial);
        if (fbInterstitial != null) {
            fbInterstitial.destroy();
            fbInterstitial = null;
        }
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(rewardedVideoAd);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }
    }

    private String getError(AdError adError) {
        int errorCode = 0;
        if (adError != null) {
            errorCode = adError.getErrorCode();
            if (errorCode == AdError.NO_FILL_ERROR_CODE) {
                return "NO_FILL_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.SERVER_ERROR_CODE) {
                return "SERVER_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.INTERNAL_ERROR_CODE) {
                return "INTERNAL_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.MEDIATION_ERROR_CODE) {
                return "MEDIATION_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.NETWORK_ERROR_CODE) {
                return "NETWORK_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE) {
                return "LOAD_TOO_FREQUENTLY_ERROR_CODE[" + errorCode + "]";
            }
            if (errorCode == AdError.INTERSTITIAL_AD_TIMEOUT) {
                return "INTERSTITIAL_AD_TIMEOUT[" + errorCode + "]";
            }
        }
        return "UNKNOWN[" + errorCode + "]";
    }

    protected int toSdkError(AdError adError) {
        int code = 0;
        if (adError != null) {
            code = adError.getErrorCode();
            if (code == AdError.NO_FILL_ERROR_CODE) {
                return Constant.AD_ERROR_NOFILL;
            }
            if (code == AdError.SERVER_ERROR_CODE) {
                return Constant.AD_ERROR_SERVER;
            }
            if (code == AdError.INTERNAL_ERROR_CODE) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (code == AdError.MEDIATION_ERROR_CODE) {
                return Constant.AD_ERROR_MEDIATION;
            }
            if (code == AdError.NETWORK_ERROR_CODE) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (code == AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE) {
                return Constant.AD_ERROR_TOO_FREQUENCY;
            }
            if (code == AdError.INTERSTITIAL_AD_TIMEOUT) {
                return Constant.AD_ERROR_TIMEOUT;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    private String toErrorMessage(AdError adError) {
        if (adError != null) {
            return "[" + adError.getErrorCode() + "] " + adError.getErrorMessage();
        }
        return null;
    }
}