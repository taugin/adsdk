package com.rabbit.adsdk.adloader.admob;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

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
    private static boolean sAdmobInited = false;
    private AdView bannerView;
    private AdView loadingView;
    private InterstitialAd mInterstitialAd;

    private RewardedAd mRewardedAd;

    private AdLoader.Builder loadingBuilder;
    private NativeAd mNativeAd;
    private Params mParams;

    private AdView lastUseBannerView;
    private NativeAd lastUseNativeAd;
    private List<NativeAd> nativeAdList = Collections.synchronizedList(new ArrayList<NativeAd>());

    private AdmobBindNativeView admobBindNativeView = new AdmobBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return admobBindNativeView;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        initBannerSize();
        if (!sAdmobInited) {
            sAdmobInited = true;
            MobileAds.initialize(mContext, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.iv(Log.TAG, "admob init successfully");
                }
            });
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADMOB;
    }

    private void initBannerSize() {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER);
        ADSIZE.put(Constant.FULL_BANNER, AdSize.FULL_BANNER);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.LARGE_BANNER);
        ADSIZE.put(Constant.LEADERBOARD, AdSize.LEADERBOARD);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE);
        ADSIZE.put(Constant.WIDE_SKYSCRAPER, AdSize.WIDE_SKYSCRAPER);
        ADSIZE.put(Constant.SMART_BANNER, AdSize.SMART_BANNER);
        ADSIZE.put(Constant.ADAPTIVE_BANNER, getAdSize());
    }

    private AdSize getAdSize() {
        try {
            DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;
            int adWidth = (int) (widthPixels / density);
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
        } catch (Exception | Error e) {
        }
        return null;
    }

    @Override
    public void loadBanner(int adSize) {
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

        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER;
        }
        Log.iv(Log.TAG, formatLog("banner size : " + size));
        loadingView = new AdView(mContext);
        loadingView.setAdUnitId(mPidConfig.getPid());
        loadingView.setAdSize(size);
        loadingView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.iv(Log.TAG, formatLog("ad closed"));
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdFailed(toSdkError(loadAdError));
            }

            @Override
            public void onAdOpened() {
                Log.iv(Log.TAG, formatLog("ad opened"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                bannerView = loadingView;
                putCachedAdTime(loadingView);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdClicked() {
                Log.iv(Log.TAG, formatLog("ad click"));
            }

            @Override
            public void onAdImpression() {
                Log.iv(Log.TAG, formatLog("ad impression"));
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
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
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
            Log.e(Log.TAG, "admob loader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mInterstitialAd != null && !isCachedAdExpired(mInterstitialAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadInterstitial() {
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

        setLoading(true, STATE_REQUEST);
        InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                mInterstitialAd = interstitialAd;
                putCachedAdTime(interstitialAd);
                reportAdLoaded();
                notifyAdLoaded(AdmobLoader.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                mInterstitialAd = null;
                reportAdError(codeToError(loadAdError));
                notifyAdFailed(toSdkError(loadAdError));
            }
        };
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        InterstitialAd.load(mContext, mPidConfig.getPid(), new AdRequest.Builder().build(), interstitialAdLoadCallback);
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(adError)));
                    clearLastShowTime();
                    onResetInterstitial();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad showed full screen content"));
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad dismissed full screen content"));
                    reportAdClose();
                    notifyAdDismiss();
                    clearLastShowTime();
                    onResetInterstitial();
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp();
                    notifyAdImp();
                }
            });
            mInterstitialAd.show(getActivity());
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        }
        return false;
    }

    @Override
    public void loadRewardedVideo() {
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

        setLoading(true, STATE_REQUEST);

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        RewardedAd.load(mContext, mPidConfig.getPid(), new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                mRewardedAd = rewardedAd;
                putCachedAdTime(mRewardedAd);
                reportAdLoaded();
                notifyAdLoaded(AdmobLoader.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdFailed(toSdkError(loadAdError));
            }
        });
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = mRewardedAd != null && !isCachedAdExpired(mRewardedAd) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(adError)));
                    notifyAdFailed(toSdkError(adError));
                    clearLastShowTime();
                    onResetReward();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad showed full screen content"));
                    notifyAdOpened();
                    notifyRewardAdsStarted();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad dismissed full screen content"));
                    reportAdClose();
                    notifyAdDismiss();
                    clearLastShowTime();
                    onResetReward();
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp();
                    notifyAdImp();
                }
            });
            Activity activity = getActivity();
            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    Log.iv(Log.TAG, formatLog("ad earned reward"));
                    reportAdReward();
                    AdReward item = new AdReward();
                    if (rewardItem != null) {
                        item.setAmount(String.valueOf(rewardItem.getAmount()));
                        item.setType(rewardItem.getType());
                        notifyRewarded(item);
                    }
                }
            });
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = super.isNativeLoaded();
        if (isLoadMultipleNative()) {
            try {
                for (NativeAd nAd : nativeAdList) {
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
            if (mNativeAd != null) {
                loaded = !isCachedAdExpired(mNativeAd);
            }
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
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isNativeLoaded()) {
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

        try {
            if (nativeAdList != null && !nativeAdList.isEmpty()) {
                nativeAdList.clear();
            }
        } catch (Exception e) {
        }
        setLoading(true, STATE_REQUEST);
        loadingBuilder = new AdLoader.Builder(mContext, mPidConfig.getPid());
        loadingBuilder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                if (isLoadMultipleNative()) {
                    try {
                        nativeAdList.add(nativeAd);
                    } catch (Exception e) {
                    }
                } else {
                    mNativeAd = nativeAd;
                }
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(nativeAd);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdImpression() {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp();
                notifyAdImp();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                if (loadAdError != null && loadAdError.getCode() == AdRequest.ERROR_CODE_NO_FILL) {
                    updateLastNoFillTime();
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdFailed(toSdkError(loadAdError));
            }

            @Override
            public void onAdClosed() {
                Log.iv(Log.TAG, formatLog("ad closed"));
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
                mNativeAd = nativeAdList.remove(0);
            } catch (Exception e) {
            }
        }
        clearCachedAdTime(mNativeAd);
        admobBindNativeView.bindNative(mParams, viewGroup, mNativeAd, mPidConfig);
        lastUseNativeAd = mNativeAd;
        mNativeAd = null;
        reportAdShow();
        notifyAdShow();
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

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(mInterstitialAd);
        mInterstitialAd = null;
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(mRewardedAd);
        mRewardedAd = null;
    }

    private String codeToError(AdError adError) {
        if (adError != null) {
            int code = adError.getCode();
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
        return "ERROR[NULL]";
    }

    protected int toSdkError(AdError adError) {
        if (adError != null) {
            int code = adError.getCode();
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
        }
        return Constant.AD_ERROR_UNKNOWN;
    }
}
