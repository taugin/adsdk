package com.hauyu.adsdk.adloader.admob;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdmobLoader extends AbstractSdkLoader {

    protected static final Map<Integer, AdSize> ADSIZE = new HashMap<>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER);
        ADSIZE.put(Constant.FULL_BANNER, AdSize.FULL_BANNER);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.LARGE_BANNER);
        ADSIZE.put(Constant.LEADERBOARD, AdSize.LEADERBOARD);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE);
        ADSIZE.put(Constant.WIDE_SKYSCRAPER, AdSize.WIDE_SKYSCRAPER);
        ADSIZE.put(Constant.SMART_BANNER, AdSize.SMART_BANNER);
    }

    private AdView bannerView;
    private AdView loadingView;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd loadingRewardVideo;
    private RewardedVideoAd loadedRewardVideo;
    private AdLoader.Builder loadingBuilder;
    private UnifiedNativeAd nativeAd;
    private Params mParams;

    private AdView gBannerView;
    private UnifiedNativeAd gNativeAd;
    private List<UnifiedNativeAd> nativeAdList = Collections.synchronizedList(new ArrayList<UnifiedNativeAd>());

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        if (!TextUtils.isEmpty(getAppId())) {
            MobileAds.initialize(mContext, getAppId());
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADMOB;
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
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER;
        }
        loadingView = new AdView(mContext);
        loadingView.setAdUnitId(mPidConfig.getPid());
        loadingView.setAdSize(size);
        loadingView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(i));
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdOpened() {
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
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                bannerView = loadingView;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingView.loadAd(new AdRequest.Builder().build());
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
            Log.e(Log.TAG, "admobloader error : " + e);
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
        interstitialAd = new InterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                interstitialAd = null;
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(i));
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialImp();
                }
            }

            /**
             * banner每隔一段时间会自动切换新的banner展示，因此会导致多次调用onAdLoaded
             */
            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(AdmobLoader.this);
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
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            clearCachedAdTime(interstitialAd);
            reportAdShow();
            return true;
        }
        return false;
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
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
            return;
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
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(AdmobLoader.this);
                }
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(i));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(i));
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.v(Log.TAG, "");
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdOpened();
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
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.v(Log.TAG, "");
                reportAdReward();
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
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
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
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingRewardVideo.loadAd(mPidConfig.getPid(), new AdRequest.Builder().build());
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = super.isRewaredVideoLoaded();
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
        printInterfaceLog(ACTION_SHOW);
        if (loadedRewardVideo != null && loadedRewardVideo.isLoaded()) {
            loadedRewardVideo.show();
            clearCachedAdTime(loadedRewardVideo);
            loadedRewardVideo = null;
            reportAdShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = super.isNativeLoaded();
        if (isLoadMultipleNative()) {
            try {
                for (UnifiedNativeAd nAd : nativeAdList) {
                    if (nAd != null) {
                        loaded = !isCachedAdExpired(nAd);
                        if (loaded) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        } else {
            if (nativeAd != null) {
                loaded = !isCachedAdExpired(nativeAd);
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
        try {
            if (nativeAdList != null && !nativeAdList.isEmpty()) {
                nativeAdList.clear();
            }
        } catch (Exception e) {
        }
        setLoading(true, STATE_REQUEST);
        loadingBuilder = new AdLoader.Builder(mContext, mPidConfig.getPid());
        loadingBuilder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                if (isLoadMultipleNative()) {
                    try {
                        nativeAdList.add(unifiedNativeAd);
                    } catch (Exception e) {
                    }
                } else {
                    nativeAd = unifiedNativeAd;
                }
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(unifiedNativeAd);
                reportAdLoaded();
                notifyAdLoaded(false);
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
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
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
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    updateLastNoFillTime();
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(errorCode));
                }
            }

            @Override
            public void onAdClosed() {
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
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
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        if (adLoader != null) {
            if (isLoadMultipleNative()) {
                adLoader.loadAds(new AdRequest.Builder().build(), mPidConfig.getCnt());
            } else {
                adLoader.loadAd(new AdRequest.Builder().build());
            }
        }
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        if (isLoadMultipleNative()) {
            try {
                nativeAd = nativeAdList.remove(0);
            } catch (Exception e) {
            }
        }
        AdmobBindNativeView admobBindNativeView = new AdmobBindNativeView();
        clearCachedAdTime(nativeAd);
        admobBindNativeView.bindNative(mParams, viewGroup, nativeAd, mPidConfig);
        gNativeAd = nativeAd;
        reportAdShow();
        if (!isDestroyAfterClick()) {
            nativeAd = null;
        }
    }

    @Override
    public void resume() {
        if (bannerView != null) {
            bannerView.resume();
        }
        if (loadingRewardVideo != null) {
            loadingRewardVideo.resume(mContext);
        }
    }

    @Override
    public void pause() {
        if (bannerView != null) {
            bannerView.pause();
        }
        if (loadingRewardVideo != null) {
            loadingRewardVideo.pause(mContext);
        }
    }

    @Override
    public void destroy() {
        if (gBannerView != null && !isDestroyAfterClick()) {
            gBannerView.destroy();
            gBannerView = null;
        }
        if (gNativeAd != null && !isDestroyAfterClick()) {
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
