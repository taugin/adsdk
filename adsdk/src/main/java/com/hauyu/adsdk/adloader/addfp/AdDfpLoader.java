package com.hauyu.adsdk.adloader.addfp;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.Params;
import com.hauyu.adsdk.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdDfpLoader extends AbstractSdkLoader {

    private static final Map<Integer, AdSize> ADSIZE = new HashMap<Integer, AdSize>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER);
        ADSIZE.put(Constant.FULL_BANNER, AdSize.FULL_BANNER);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.LARGE_BANNER);
        ADSIZE.put(Constant.LEADERBOARD, AdSize.LEADERBOARD);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE);
        ADSIZE.put(Constant.SMART_BANNER, AdSize.SMART_BANNER);
    }

    private PublisherAdView bannerView;
    private PublisherInterstitialAd interstitialAd;
    private UnifiedNativeAd nativeAd;
    private Params mParams;
    private PublisherAdView loadingView;
    private AdLoader.Builder loadingBuilder;
    private RewardedVideoAd loadingRewardVideo;
    private RewardedVideoAd loadedRewardVideo;

    private PublisherAdView gBannerView;
    private UnifiedNativeAd gNativeAd;

    @Override
    public void setAdId(String adId) {
        if (!TextUtils.isEmpty(adId)) {
            MobileAds.initialize(mContext, adId);
        } else if (!TextUtils.isEmpty(getAppId())) {
            MobileAds.initialize(mContext, getAppId());
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_DFP;
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
                if (loadingView != null) {
                    loadingView.setAdListener(null);
                    loadingView.destroy();
                    clearCachedAdTime(loadingView);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER;
        }
        loadingView = new PublisherAdView(mContext);
        loadingView.setAdUnitId(mPidConfig.getPid());
        loadingView.setAdSizes(size);
        loadingView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(i), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    bannerView = null;
                }
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                bannerView = loadingView;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                notifyAdLoaded(false);
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        });
        loadingView.loadAd(new PublisherAdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
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
        Log.v(Log.TAG, "dfploader");
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
            if (mStat != null) {
                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "dfploader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (interstitialAd != null) {
            loaded = interstitialAd.isLoaded() && !isCachedAdExpired(interstitialAd);
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
                getAdListener().onInterstitialLoaded();
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
                if (interstitialAd != null) {
                    interstitialAd.setAdListener(null);
                    clearCachedAdTime(interstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        interstitialAd = new PublisherInterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                interstitialAd = null;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(i), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded();
                }
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
            }
        });
        interstitialAd.loadAd(new PublisherAdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            clearCachedAdTime(interstitialAd);
            interstitialAd = null;
            if (mStat != null) {
                mStat.reportAdCallShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdShowForLTV(mContext, getSdkName(), getPid());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = super.isNativeLoaded();
        if (nativeAd != null) {
            loaded = !isCachedAdExpired(nativeAd);
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
        if (isNativeLoaded()) {
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
                if (loadingBuilder != null) {
                    loadingBuilder.forAppInstallAd(null).forContentAd(null).withAdListener(null);
                    if (nativeAd != null) {
                        clearCachedAdTime(nativeAd);
                    }
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        loadingBuilder = new AdLoader.Builder(mContext, mPidConfig.getPid());
        loadingBuilder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                nativeAd = unifiedNativeAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(nativeAd);
                notifyAdLoaded(false);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
                if (isDestroyAfterClick()) {
                    nativeAd = null;
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdImpression();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    updateLastNoFillTime();
                }
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();
        NativeAdOptions nativeAdOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        loadingBuilder.withNativeAdOptions(nativeAdOptions);
        AdLoader adLoader = loadingBuilder.build();
        if (adLoader != null) {
            adLoader.loadAd(new PublisherAdRequest.Builder().build());
        }
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        Log.v(Log.TAG, "showNative - dfp");
        if (params != null) {
            mParams = params;
        }
        AdDfpBindNativeView adDfpBindNativeView = new AdDfpBindNativeView();
        clearCachedAdTime(nativeAd);
        adDfpBindNativeView.bindNative(mParams, viewGroup, nativeAd, mPidConfig);
        gNativeAd = nativeAd;
        if (!isDestroyAfterClick()) {
            nativeAd = null;
        }
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
                getAdListener().onInterstitialLoaded();
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
                if (loadingRewardVideo != null) {
                    loadingRewardVideo.setRewardedVideoAdListener(null);
                    clearCachedAdTime(loadingRewardVideo);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        loadingRewardVideo = MobileAds.getRewardedVideoAdInstance(mContext);
        loadingRewardVideo.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                loadedRewardVideo = loadingRewardVideo;
                putCachedAdTime(loadedRewardVideo);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded();
                }
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(i), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdShowed();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                }
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    AdReward item = new AdReward();
                    if (rewardItem != null) {
                        item.setAmount(String.valueOf(rewardItem.getAmount()));
                        item.setType(rewardItem.getType());
                    }
                    getAdListener().onRewarded(item);
                }
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }
            }
        });
        loadingRewardVideo.loadAd(mPidConfig.getPid(), new PublisherAdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (loadedRewardVideo != null) {
            loaded = loadedRewardVideo.isLoaded() && !isCachedAdExpired(loadedRewardVideo);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo() {
        if (loadedRewardVideo != null && loadedRewardVideo.isLoaded()) {
            loadedRewardVideo.show();
            clearCachedAdTime(loadedRewardVideo);
            loadedRewardVideo = null;
            if (mStat != null) {
                mStat.reportAdCallShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdShowForLTV(mContext, getSdkName(), getPid());
            }
            return true;
        }
        return false;
    }

    @Override
    public void resume() {
        if (bannerView != null) {
            bannerView.resume();
        }
    }

    @Override
    public void pause() {
        if (bannerView != null) {
            bannerView.pause();
        }
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

    private String codeToError(int code) {
        if (code == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
            return "ERROR_CODE_INTERNAL_ERROR[" + code + "]";
        }
        if (code == AdRequest.ERROR_CODE_INVALID_REQUEST) {
            return "ERROR_CODE_INVALID_REQUEST[" + code + "]";
        }
        if (code == AdRequest.ERROR_CODE_NETWORK_ERROR) {
            return "ERROR_CODE_NETWORK_ERROR[" + code + "]";
        }
        if (code == AdRequest.ERROR_CODE_NO_FILL) {
            return "ERROR_CODE_NO_FILL[" + code + "]";
        }
        return "UNKNOWN[" + code + "]";
    }
}