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
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.appopen.AppOpenAd;
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
import com.rabbit.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdmobLoader extends AbstractSdkLoader {

    protected static final Map<Integer, AdSize> ADSIZE = new HashMap<>();
    private static AtomicBoolean sAdmobInited = new AtomicBoolean(false);
    private AdView bannerView;
    private AdView loadingView;
    private InterstitialAd mInterstitialAd;
    private AppOpenAd mAppOpenAd;

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
        if (!sAdmobInited.getAndSet(true)) {
            MobileAds.initialize(getActivity());
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
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
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }

            @Override
            public void onAdOpened() {
                Log.iv(Log.TAG, formatLog("ad opened"));
                reportAdClick(Constant.AD_SDK_ADMOB);
                notifyAdClick();
            }

            @Override
            public void onAdLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                bannerView = loadingView;
                setLoading(false, STATE_SUCCESS);
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
        notifyAdRequest();
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
            bannerView.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(AdValue adValue) {
                    String network = null;
                    try {
                        network = bannerView.getResponseInfo().getMediationAdapterClassName();
                    } catch (Exception e) {
                    }
                    reportAdImp(Constant.AD_SDK_ADMOB);
                    notifyAdImp();
                    reportAdmobImpressionData(adValue, network);
                }
            });
            reportAdShow();
            notifyAdShow();
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseBannerView = bannerView;
            bannerView = null;
        } catch (Exception e) {
            Log.e(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : AdView not ready");
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
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
        InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mInterstitialAd = interstitialAd;
                setLoading(false, STATE_SUCCESS);
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
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }
        };
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
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
                    notifyAdShowFailed(toSdkError(adError), toErrorMessage(adError));
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad showed full screen content"));
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad dismissed full screen content"));
                    clearLastShowTime();
                    onResetInterstitial();
                    reportAdClose();
                    notifyAdDismiss();
                }

                @Override
                public void onAdClicked() {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick(Constant.AD_SDK_ADMOB);
                    notifyAdClick();
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp(Constant.AD_SDK_ADMOB);
                    notifyAdImp();
                }
            });
            mInterstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(AdValue adValue) {
                    String network = null;
                    try {
                        network = mInterstitialAd.getResponseInfo().getMediationAdapterClassName();
                    } catch (Exception e) {
                    }
                    reportAdmobImpressionData(adValue, network);
                }
            });
            reportAdShow();
            notifyAdShow();
            mInterstitialAd.show(getActivity());
            updateLastShowTime();
            return true;
        } else {
            onResetInterstitial();
            Log.e(Log.TAG, formatShowErrorLog("InterstitialAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : InterstitialAd not ready");
        }
        return false;
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
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

        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        RewardedAd.load(mContext, mPidConfig.getPid(), new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mRewardedAd = rewardedAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mRewardedAd);
                reportAdLoaded();
                notifyAdLoaded(AdmobLoader.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
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
                    clearLastShowTime();
                    onResetReward();
                    notifyAdShowFailed(toSdkError(adError), toErrorMessage(adError));
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
                    clearLastShowTime();
                    onResetReward();
                    reportAdClose();
                    notifyAdDismiss();
                }

                @Override
                public void onAdClicked() {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick(Constant.AD_SDK_ADMOB);
                    notifyAdClick();
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp(Constant.AD_SDK_ADMOB);
                    notifyAdImp();
                }
            });
            mRewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(@NonNull AdValue adValue) {
                    String network = null;
                    try {
                        network = mRewardedAd.getResponseInfo().getMediationAdapterClassName();
                    } catch (Exception e) {
                    }
                    reportAdmobImpressionData(adValue, network);
                }
            });
            reportAdShow();
            notifyAdShow();
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
            return true;
        } else {
            onResetReward();
            Log.e(Log.TAG, formatShowErrorLog("RewardedAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : RewardedAd not ready");
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
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
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
                reportAdClick(Constant.AD_SDK_ADMOB);
                notifyAdClick();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdImpression() {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp(Constant.AD_SDK_ADMOB);
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
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
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
        notifyAdRequest();
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
        if (mNativeAd != null) {
            reportAdShow();
            notifyAdShow();
            mNativeAd.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(@NonNull AdValue adValue) {
                    String network = null;
                    try {
                        network = mNativeAd.getResponseInfo().getMediationAdapterClassName();
                    } catch (Exception e) {
                    }
                    reportAdmobImpressionData(adValue, network);
                }
            });
            admobBindNativeView.bindNative(mParams, viewGroup, mNativeAd, mPidConfig);
            lastUseNativeAd = mNativeAd;
            clearCachedAdTime(mNativeAd);
            mNativeAd = null;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("NativeAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : NativeAd not ready");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isSplashLoaded() {
        boolean loaded = super.isSplashLoaded();
        if (mAppOpenAd != null) {
            loaded = !isCachedAdExpired(mAppOpenAd) && !isShowTimeExpired();
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadSplash() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isSplashLoaded()) {
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
        AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd appOpenAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mAppOpenAd = appOpenAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mAppOpenAd);
                reportAdLoaded();
                notifyAdLoaded(AdmobLoader.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                onResetSplash();
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }
        };
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        int splashOrientation = mPidConfig.getSplashOrientation();
        AppOpenAd.load(mContext, getPid(), new AdRequest.Builder().build(), splashOrientation, appOpenAdLoadCallback);
    }

    @Override
    public boolean showSplash(ViewGroup viewGroup) {
        Log.iv(Log.TAG, getAdPlaceName() + " - " +getSdkName() + " show splash");
        if (mAppOpenAd != null) {
            Activity activity = getActivity();
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(adError)));
                    clearLastShowTime();
                    onResetSplash();
                    notifyAdShowFailed(toSdkError(adError), toErrorMessage(adError));
                }

                @Override
                public void onAdClicked() {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick(Constant.AD_SDK_ADMOB);
                    notifyAdClick();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad showed full screen content"));
                    reportAdImp(Constant.AD_SDK_ADMOB);
                    notifyAdImp();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad dismissed full screen content"));
                    clearLastShowTime();
                    onResetSplash();
                    reportAdClose();
                    notifyAdDismiss();
                }
            };
            mAppOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            mAppOpenAd.setOnPaidEventListener(new OnPaidEventListener() {
                @Override
                public void onPaidEvent(@NonNull AdValue adValue) {
                    String network = null;
                    try {
                        network = mAppOpenAd.getResponseInfo().getMediationAdapterClassName();
                    } catch (Exception e) {
                    }
                    reportAdmobImpressionData(adValue, network);
                }
            });
            reportAdShow();
            notifyAdShow();
            mAppOpenAd.show(activity);
            updateLastShowTime();
            return true;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("AppOpenAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : AppOpenAd not ready");
            onResetSplash();
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////

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

    @Override
    protected void onResetSplash() {
        super.onResetSplash();
        clearCachedAdTime(mAppOpenAd);
        mAppOpenAd = null;
    }

    private void reportAdmobImpressionData(AdValue adValue, String network) {
        try {
            // admob给出的是百万次展示的价值，换算ecpm需要除以1000
            double revenue = adValue.getValueMicros() / (double)1000000;
            String networkName = network;
            String adUnitId = getPid();
            String adFormat = getAdType();
            String adUnitName = getAdPlaceName();
            Map<String, Object> map = new HashMap<>();
            map.put(Constant.AD_VALUE, revenue);
            map.put(Constant.AD_CURRENCY, "USD");
            map.put(Constant.AD_NETWORK, networkName);
            map.put(Constant.AD_UNIT_ID, adUnitId);
            map.put(Constant.AD_FORMAT, adFormat);
            map.put(Constant.AD_UNIT_NAME, adUnitName);
            map.put(Constant.AD_PLATFORM, getSdkName());
            map.put(Constant.AD_SDK_VERSION, getSdkVersion());
            map.put(Constant.AD_APP_VERSION, getAppVersion());
            try {
                String[] precisionTypes = new String[]{"unknown", "estimated", "publisher_provided", "precise"};
                map.put(Constant.AD_PRECISION, precisionTypes[adValue.getPrecisionType()]);
            } catch (Exception e) {
            }
            map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            StringBuilder builder = new StringBuilder("{");
            builder.append("\n");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.append("  " + entry.getKey() + " : " + entry.getValue());
                builder.append("\n");
            }
            builder.append("}");
            Log.iv(Log.TAG, getSdkName() + " imp data : " + builder.toString());
            onReportAdImpData(map);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
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

    private String toErrorMessage(AdError adError) {
        if (adError != null) {
            return "[" + adError.getCode() + "] " + adError.getMessage();
        }
        return null;
    }
}
