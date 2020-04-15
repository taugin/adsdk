package com.hauyu.adsdk.adloader.adfb;

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
import com.facebook.ads.NativeAdsManager;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

import java.util.HashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public class FBLoader extends AbstractSdkLoader {

    private static final HashMap<Integer, AdSize> ADSIZE = new HashMap<Integer, AdSize>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER_HEIGHT_50);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.BANNER_HEIGHT_90);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.RECTANGLE_HEIGHT_250);
    }

    private InterstitialAd fbInterstitial;
    private NativeAd nativeAd;
    private AdView bannerView;
    private Params mParams;
    private AdView loadingView;
    private RewardedVideoAd rewardedVideoAd;

    private NativeAd gNativeAd;
    private AdView gBannerView;
    private NativeAdsManager nativeAdsManager;
    private int mShowNativeCount = 0;

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        AudienceNetworkAds.initialize(context);
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
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
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "nofill error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_FILLTIME);
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
        setLoading(true, STATE_REQUEST);
        AdSize size = ADSIZE.get(adSize);
        setBannerSize(adSize);
        if (size == null) {
            size = AdSize.BANNER_HEIGHT_50;
        }
        loadingView = new AdView(mContext, mPidConfig.getPid(), size);
        loadingView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , pid : " + getPid());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(getError(adError));
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(adError));
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                bannerView = loadingView;
                putCachedAdTime(loadingView);
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    bannerView = null;
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingView.loadAd();
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = bannerView != null && !isCachedAdExpired(bannerView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
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
            gBannerView = bannerView;
            if (!isDestroyAfterClick()) {
                bannerView = null;
            }
            reportAdShow();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (fbInterstitial != null) {
            loaded = fbInterstitial.isAdLoaded() && !isCachedAdExpired(fbInterstitial);
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
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "nofill error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_FILLTIME);
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
        setLoading(true, STATE_REQUEST);
        fbInterstitial = new InterstitialAd(mContext, mPidConfig.getPid());
        fbInterstitial.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialImp();
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.v(Log.TAG, "");
                if (fbInterstitial != null) {
                    fbInterstitial.destroy();
                    fbInterstitial = null;
                }
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , pid : " + getPid());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(getError(adError));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(adError));
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(fbInterstitial);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(FBLoader.this);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
            }
        });

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        fbInterstitial.loadAd();
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (fbInterstitial != null && fbInterstitial.isAdLoaded()) {
            fbInterstitial.show();
            clearCachedAdTime(fbInterstitial);
            reportAdShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (isLoadMultipleNative()) {
            if (nativeAdsManager != null) {
                int uniqueAdCount = nativeAdsManager.getUniqueNativeAdCount();
                loaded = nativeAdsManager.isLoaded() && mShowNativeCount < uniqueAdCount && !isCachedAdExpired(nativeAdsManager);
            }
        } else {
            if (nativeAd != null) {
                loaded = nativeAd.isAdLoaded() && !isCachedAdExpired(nativeAd);
            }
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadNative(Params params) {
        mParams = params;
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "nofill error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_FILLTIME);
            }
            return;
        }
        if (isNativeLoaded()) {
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

        setLoading(true, STATE_REQUEST);
        printInterfaceLog(ACTION_LOAD);
        // 一次加载多个FB广告
        if (isLoadMultipleNative()) {
            mShowNativeCount = 0;
            nativeAdsManager = new NativeAdsManager(mContext, mPidConfig.getPid(), mPidConfig.getCnt());
            nativeAdsManager.setListener(new NativeAdsManager.Listener() {
                @Override
                public void onAdsLoaded() {
                    Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , cnt : " + (nativeAdsManager != null ? nativeAdsManager.getUniqueNativeAdCount() : 0));
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(nativeAdsManager);
                    reportAdLoaded();
                    notifyAdLoaded(false);
                }

                @Override
                public void onAdError(AdError adError) {
                    if (adError != null) {
                        Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , pid : " + getPid());
                        if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                            updateLastNoFillTime();
                        }
                    }
                    setLoading(false, STATE_FAILURE);
                    reportAdError(getError(adError));
                    if (getAdListener() != null) {
                        getAdListener().onAdFailed(toSdkError(adError));
                    }
                }
            });
            reportAdRequest();
            nativeAdsManager.loadAds(NativeAd.MediaCacheFlag.ALL);
        } else {
            nativeAd = new NativeAd(mContext, mPidConfig.getPid());
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    if (adError != null) {
                        Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , pid : " + getPid());
                        if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                            updateLastNoFillTime();
                        }
                    }
                    setLoading(false, STATE_FAILURE);
                    reportAdError(getError(adError));
                    if (getAdListener() != null) {
                        getAdListener().onAdFailed(toSdkError(adError));
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(nativeAd);
                    reportAdLoaded();
                    notifyAdLoaded(false);
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.v(Log.TAG, "");
                    reportAdClick();
                    if (getAdListener() != null) {
                        getAdListener().onAdClick();
                    }
                    if (isDestroyAfterClick()) {
                        nativeAd = null;
                    }
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.v(Log.TAG, "");
                    reportAdImp();
                    if (getAdListener() != null) {
                        getAdListener().onAdImp();
                    }
                }
            });
            reportAdRequest();
            nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
        }
    }

    /**
     * 设置FB广告监听器，多个native被加载，在展示的时候设置监听器
     * @param nAd
     */
    private void setFBNativeAfterLoadListener(final NativeAd nAd) {
        if (nAd == null) {
            return;
        }
        nAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "multiple native click");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "multiple native click");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }
        });
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        // 多个FB Native被加载
        if (isLoadMultipleNative()) {
            if (nativeAdsManager != null) {
                NativeAd nAd = nativeAdsManager.nextNativeAd();
                int uniqueCount = nativeAdsManager.getUniqueNativeAdCount();
                if (nAd != null) {
                    setFBNativeAfterLoadListener(nAd);
                    FBBindNativeView fbBindNativeView = new FBBindNativeView();
                    fbBindNativeView.bindFBNative(mParams, viewGroup, nAd, mPidConfig);
                    mShowNativeCount++;
                }
                if (mShowNativeCount >= uniqueCount) {
                    clearCachedAdTime(nativeAdsManager);
                }
            }
        } else {
            FBBindNativeView fbBindNativeView = new FBBindNativeView();
            clearCachedAdTime(nativeAd);
            fbBindNativeView.bindFBNative(mParams, viewGroup, nativeAd, mPidConfig);
            gNativeAd = nativeAd;
            if (!isDestroyAfterClick()) {
                nativeAd = null;
            }
        }
        reportAdShow();
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onRewardedVideoError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "nofill error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onRewardedVideoError(Constant.AD_ERROR_FILLTIME);
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
                getAdListener().onRewardedVideoError(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        rewardedVideoAd = new RewardedVideoAd(mContext, mPidConfig.getPid());
        rewardedVideoAd.setAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoCompleted() {
                Log.v(Log.TAG, "");
                reportAdReward();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }

                if (getAdListener() != null) {
                    AdReward adReward = new AdReward();
                    adReward.setType(Constant.ECPM);
                    double ecpm = 0;
                    if (mPidConfig != null) {
                        ecpm = mPidConfig.getEcpm();
                    }
                    adReward.setAmount(String.valueOf(ecpm));
                    getAdListener().onRewarded(adReward);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdOpened();
                }
            }

            @Override
            public void onRewardedVideoClosed() {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
                if (rewardedVideoAd != null) {
                    rewardedVideoAd.destroy();
                    rewardedVideoAd = null;
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , pid : " + getPid());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(getError(adError));
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoError(toSdkError(adError));
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(rewardedVideoAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(FBLoader.this);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        rewardedVideoAd.loadAd();
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = super.isRewardedVideoLoaded();
        if (rewardedVideoAd != null) {
            loaded = rewardedVideoAd.isAdLoaded() && !rewardedVideoAd.isAdInvalidated()/* && !isCachedAdExpired(rewardedVideoAd)*/;
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedVideoAd != null && rewardedVideoAd.isAdLoaded() && !rewardedVideoAd.isAdInvalidated()) {
            rewardedVideoAd.show();
            clearCachedAdTime(rewardedVideoAd);
            reportAdShow();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        if (gBannerView != null) {
            gBannerView.destroy();
            gBannerView = null;
        }
        if (gNativeAd != null) {
            gNativeAd.destroy();
            gNativeAd = null;
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
}