package com.rabbit.adsdk.adloader.addfp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

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
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

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

    private RewardedAd loadingRewardedAd;
    private RewardedAd rewardedAd;

    private PublisherAdView lastUseBannerView;
    private UnifiedNativeAd lastUseNativeAd;
    private AdDfpBindNativeView adDfpBindNativeView = new AdDfpBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return adDfpBindNativeView;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        if (!TextUtils.isEmpty(getAppId())) {
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
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isBannerLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
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
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                notifyAdFailed(toSdkError(i));
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                putCachedAdTime(loadingView);
                bannerView = loadingView;
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                notifyAdImp();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingView.loadAd(new PublisherAdRequest.Builder().build());
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
            lastUseBannerView = bannerView;
            bannerView = null;
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
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
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        interstitialAd = new PublisherInterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                interstitialAd = null;
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                notifyAdFailed(toSdkError(i));
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                reportAdImp();
                notifyAdImp();
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                reportAdLoaded();
                notifyAdLoaded(AdDfpLoader.this);
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
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        interstitialAd.loadAd(new PublisherAdRequest.Builder().build());
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            clearCachedAdTime(interstitialAd);
            reportAdShow();
            notifyAdShow();
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
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isNativeLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        loadingBuilder = new AdLoader.Builder(mContext, mPidConfig.getPid());
        loadingBuilder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                nativeAd = unifiedNativeAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(nativeAd);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                reportAdImp();
                notifyAdImp();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    updateLastNoFillTime();
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                notifyAdFailed(toSdkError(errorCode));
            }

            @Override
            public void onAdClosed() {
                reportAdClose();
                notifyAdDismiss();
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();
        NativeAdOptions nativeAdOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        loadingBuilder.withNativeAdOptions(nativeAdOptions);
        AdLoader adLoader = loadingBuilder.build();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        if (adLoader != null) {
            adLoader.loadAd(new PublisherAdRequest.Builder().build());
        }
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        clearCachedAdTime(nativeAd);
        adDfpBindNativeView.bindNative(mParams, viewGroup, nativeAd, mPidConfig);
        lastUseNativeAd = nativeAd;
        nativeAd = null;
        reportAdShow();
        notifyAdShow();
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);

        loadingRewardedAd = new RewardedAd(mContext, mPidConfig.getPid());
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingRewardedAd.loadAd(new PublisherAdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                rewardedAd = loadingRewardedAd;
                loadingRewardedAd = null;
                putCachedAdTime(rewardedAd);
                reportAdLoaded();
                notifyAdLoaded(AdDfpLoader.this);
            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                notifyAdFailed(toSdkError(i));
            }
        });
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = super.isRewardedVideoLoaded();
        if (rewardedAd != null) {
            loaded = rewardedAd.isLoaded() && !isCachedAdExpired(rewardedAd);
        }
        boolean finalLoaded = loaded || isRewardPlaying();
        if (finalLoaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded + " , playing : " + isRewardPlaying());
        }
        return finalLoaded;
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (rewardedAd != null && rewardedAd.isLoaded()) {
            Activity activity = getActivity();
            rewardedAd.show(activity, new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    Log.v(Log.TAG, "");
                    setRewardPlaying(true);
                    reportAdImp();
                    notifyAdOpened();
                    notifyRewardAdsStarted();
                }

                @Override
                public void onRewardedAdClosed() {
                    Log.v(Log.TAG, "");
                    setRewardPlaying(false);
                    reportAdClose();
                    notifyAdDismiss();
                    rewardedAd = null;
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    Log.v(Log.TAG, "");
                    reportAdReward();
                    AdReward item = new AdReward();
                    if (rewardItem != null) {
                        item.setAmount(String.valueOf(rewardItem.getAmount()));
                        item.setType(rewardItem.getType());
                    }
                    notifyRewarded(item);
                    rewardedAd = null;
                }

                @Override
                public void onRewardedAdFailedToShow(int i) {
                    Log.v(Log.TAG, "");
                    notifyAdFailed(toSdkError(i));
                    rewardedAd = null;
                }
            });
            clearCachedAdTime(rewardedAd);
            reportAdShow();
            notifyAdShow();
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
        if (lastUseBannerView != null) {
            lastUseBannerView.destroy();
            lastUseBannerView = null;
        }
        if (lastUseNativeAd != null) {
            lastUseNativeAd.destroy();
            lastUseNativeAd = null;
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

    protected int toSdkError(int code) {
        if (code == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
            return Constant.AD_ERROR_INTERNAL;
        }
        if (code == AdRequest.ERROR_CODE_INVALID_REQUEST) {
            return Constant.AD_ERROR_INVALID_REQUEST;
        }
        if (code == AdRequest.ERROR_CODE_NETWORK_ERROR) {
            return Constant.AD_ERROR_NETWORK;
        }
        if (code == AdRequest.ERROR_CODE_NO_FILL) {
            return Constant.AD_ERROR_NOFILL;
        }
        return Constant.AD_ERROR_UNKNOWN;
    }
}
