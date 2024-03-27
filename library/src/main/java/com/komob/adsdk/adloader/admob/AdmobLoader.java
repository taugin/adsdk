package com.komob.adsdk.adloader.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.komob.adsdk.AdReward;
import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.adloader.base.AbstractSdkLoader;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.log.Log;
import com.komob.api.RFileConfig;
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
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.data.config.PidConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 * 由于bidding需要获取到令牌，因此要先完成admob的初始化，然后再进行广告加载
 */

public class AdmobLoader extends AbstractSdkLoader {

    protected static final Map<Integer, AdSize> ADSIZE = new HashMap<>();
    private static AtomicBoolean sAdmobInited = new AtomicBoolean(false);
    private static int sSDKInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private AdView bannerView;
    private AdView loadingView;
    private InterstitialAd mInterstitialAd;
    private AppOpenAd mAppOpenAd;

    private RewardedAd mRewardedAd;

    private AdLoader.Builder loadingBuilder;
    private NativeAd mNativeAd;

    private AdView lastUseBannerView;
    private NativeAd lastUseNativeAd;

    private AdmobBannerListener admobBannerListener;
    private AdmobNativeListener admobNativeListener;
    private AdmobSplashListener admobSplashListener;
    private AdmobRewardListener admobRewardListener;
    private AdmobInterstitialListener admobInterstitialListener;

    private List<NativeAd> nativeAdList = Collections.synchronizedList(new ArrayList<NativeAd>());

    private AdmobBindNativeView admobBindNativeView = new AdmobBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return admobBindNativeView;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        initBannerSize();
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

    private void initAdmob(SDKInitializeListener sdkInitializeListener) {
        if (!sAdmobInited.getAndSet(true)) {
            Log.iv(Log.TAG, "start initializing " + getSdkName() + " sdk");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess();
                    }
                }
            }, 15000);
            final long startInit = SystemClock.elapsedRealtime();
            MobileAds.initialize(getActivity(), initializationStatus -> {
                Log.iv(Log.TAG, getSdkName() + " sdk init successfully cost time : " + (SystemClock.elapsedRealtime() - startInit));
                try {
                    mHandler.removeCallbacksAndMessages(null);
                    Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                    for (String adapterClass : statusMap.keySet()) {
                        AdapterStatus status = statusMap.get(adapterClass);
                        Log.iv(Log.TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
                                adapterClass, status.getDescription(), status.getLatency()));
                    }
                } catch (Exception e) {
                }
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeSuccess();
                }
            });
            setTestMode();
        } else {
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeSuccess();
            }
        }
    }

    private void setTestMode() {
        if (DataManager.get(mContext).isAdmobInTestMode()) {
            String androidId = Utils.getAndroidId(mContext);
            if (!TextUtils.isEmpty(androidId)) {
                List<String> testDeviceIds = Arrays.asList(Utils.string2MD5(androidId).toUpperCase(Locale.ENGLISH));
                Log.iv(Log.TAG, "admob set test device id : " + testDeviceIds);
                RequestConfiguration configuration =
                        new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                MobileAds.setRequestConfiguration(configuration);
            }
        }
    }

    @Override
    protected void initializeSdk(SDKInitializeListener sdkInitializeListener) {
        initAdmob(sdkInitializeListener);
    }

    @Override
    protected int getSdkInitializeState() {
        return sSDKInitializeState;
    }

    @Override
    protected void setSdkInitializeState(int state) {
        sSDKInitializeState = state;
    }

    @Override
    public void loadBanner(final int adSize) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadBannerInternal(adSize);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadBannerInternal(int adSize) {
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
        admobBannerListener = new AdmobBannerListener();
        loadingView.setAdListener(admobBannerListener.adListener);
        loadingView.setOnPaidEventListener(admobBannerListener.mOnPaidEventListener);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingView.loadAd(new AdRequest.Builder().build());
    }

    private class AdmobBannerListener extends AbstractAdListener {
        AdListener adListener = new AdListener() {
            @Override
            public void onAdClosed() {
                Log.iv(Log.TAG, formatLog("ad closed"));
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }

            @Override
            public void onAdOpened() {
                Log.iv(Log.TAG, formatLog("ad opened"));
                String network = getBannerNetwork();
                reportAdClick(network, null, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                bannerView = loadingView;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                setRevenueAverage(getBannerNetwork());
                reportAdLoaded(getBannerNetwork());
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
        };

        private OnPaidEventListener mOnPaidEventListener = new OnPaidEventListener() {
            @Override
            public void onPaidEvent(AdValue adValue) {
                impressionId = generateImpressionId();
                String network = null;
                try {
                    network = loadingView.getResponseInfo().getMediationAdapterClassName();
                    network = adapterClassToNetwork(network);
                } catch (Exception e) {
                }
                reportAdImp();
                notifyAdImp();
                reportAdmobImpressionData(adValue, network, impressionId, null);
            }
        };
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
            reportAdShow();
            notifyAdShow();
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseBannerView = bannerView;
            bannerView = null;
        } catch (Exception e) {
            Log.iv(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "AdView not ready");
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
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadInterstitialInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadInterstitialInternal() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isInterstitialLoaded()) {
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
        admobInterstitialListener = new AdmobInterstitialListener();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        InterstitialAd.load(mContext, mPidConfig.getPid(), new AdRequest.Builder().build(), admobInterstitialListener.interstitialAdLoadCallback);
    }

    private class AdmobInterstitialListener extends AbstractAdListener {
        InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mInterstitialAd = interstitialAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                setInterstitialListener(interstitialAd);
                setRevenueAverage(getInterstitialNetwork());
                reportAdLoaded(getInterstitialNetwork());
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                mInterstitialAd = null;
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }
        };

        private void setInterstitialListener(final InterstitialAd interstitialAd) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
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
                    String network = getInterstitialNetwork();
                    reportAdClick(network, null, impressionId);
                    notifyAdClick(network, impressionId);
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    String network = getInterstitialNetwork();
                    reportAdImp(network, null);
                    notifyAdImp(network, sceneName);
                }
            };
            if (interstitialAd != null) {
                interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                interstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        impressionId = generateImpressionId();
                        String network = null;
                        try {
                            network = interstitialAd.getResponseInfo().getMediationAdapterClassName();
                            network = adapterClassToNetwork(network);
                        } catch (Exception e) {
                        }
                        reportAdmobImpressionData(adValue, network, impressionId, sceneName);
                    }
                });
            }
        }
    }

    @Override
    public boolean showInterstitial(final String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (admobInterstitialListener != null) {
            admobInterstitialListener.sceneName = sceneName;
        }
        if (mInterstitialAd != null) {
            reportAdShow();
            notifyAdShow();
            mInterstitialAd.show(getActivity());
            updateLastShowTime();
            return true;
        } else {
            onResetInterstitial();
            Log.iv(Log.TAG, formatShowErrorLog("InterstitialAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "InterstitialAd not ready");
        }
        return false;
    }

    @Override
    public void loadRewardedVideo() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadRewardedVideoInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadRewardedVideoInternal() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isRewardedVideoLoaded()) {
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
        admobRewardListener = new AdmobRewardListener();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        RewardedAd.load(mContext, mPidConfig.getPid(), new AdRequest.Builder().build(), admobRewardListener.rewardedAdLoadCallback);
    }

    private class AdmobRewardListener extends AbstractAdListener {
        RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd rewardedAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mRewardedAd = rewardedAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mRewardedAd);
                setRewardListener(rewardedAd);
                setRevenueAverage(getRewardNetwork());
                reportAdLoaded(getRewardNetwork());
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }
        };

        private void setRewardListener(final RewardedAd rewardedAd) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
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
                    String network = getRewardNetwork();
                    reportAdClick(network, null, impressionId);
                    notifyAdClick(network, impressionId);
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    String network = getRewardNetwork();
                    reportAdImp(network, null);
                    notifyAdImp(network, sceneName);
                }
            };
            if (rewardedAd != null) {
                rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                rewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        impressionId = generateImpressionId();
                        String network = null;
                        try {
                            network = rewardedAd.getResponseInfo().getMediationAdapterClassName();
                            network = adapterClassToNetwork(network);
                        } catch (Exception e) {
                        }
                        reportAdmobImpressionData(adValue, network, impressionId, sceneName);
                    }
                });
            }
        }
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
    public boolean showRewardedVideo(final String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (admobRewardListener != null) {
            admobRewardListener.sceneName = sceneName;
        }
        if (mRewardedAd != null) {
            reportAdShow();
            notifyAdShow();
            Activity activity = getActivity();
            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(RewardItem rewardItem) {
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
            Log.iv(Log.TAG, formatShowErrorLog("RewardedAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "RewardedAd not ready");
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
    public void loadNative(final Params params) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadNativeInternal(params);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadNativeInternal(Params params) {
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
        admobNativeListener = new AdmobNativeListener();
        loadingBuilder.forNativeAd(admobNativeListener.onNativeAdLoadedListener).withAdListener(admobNativeListener.adListener);

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

    private class AdmobNativeListener extends AbstractAdListener {
        private String impressionId = null;
        private AdListener adListener = new AdListener() {
            @Override
            public void onAdClicked() {
                Log.iv(Log.TAG, formatLog("ad click"));
                String network = getNativeNetwork();
                reportAdClick(network, null, impressionId);
                notifyAdClick(network, impressionId);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdImpression() {
                Log.iv(Log.TAG, formatLog("ad impression"));
                String network = getNativeNetwork();
                reportAdImp(network, null);
                notifyAdImp(network, sceneName);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
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
        };
        private NativeAd.OnNativeAdLoadedListener onNativeAdLoadedListener = new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
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
                setNativeListener(nativeAd);
                setRevenueAverage(getNativeNetwork());
                reportAdLoaded(getNativeNetwork());
                notifySdkLoaderLoaded(false);
            }
        };

        private void setNativeListener(final NativeAd nativeAd) {
            if (nativeAd != null) {
                nativeAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        impressionId = generateImpressionId();
                        String network = null;
                        try {
                            network = nativeAd.getResponseInfo().getMediationAdapterClassName();
                            network = adapterClassToNetwork(network);
                        } catch (Exception e) {
                        }
                        reportAdmobImpressionData(adValue, network, impressionId, sceneName);
                    }
                });
            }
        }

    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null && admobNativeListener != null) {
            admobNativeListener.sceneName = params.getSceneName();
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
            admobBindNativeView.bindNative(params, viewGroup, mNativeAd, mPidConfig);
            lastUseNativeAd = mNativeAd;
            clearCachedAdTime(mNativeAd);
            mNativeAd = null;
        } else {
            Log.iv(Log.TAG, formatShowErrorLog("NativeAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "NativeAd not ready");
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
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadSplashInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadSplashInternal() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isSplashLoaded()) {
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
        reportAdRequest();
        notifyAdRequest();
        int splashOrientation = mPidConfig.getSplashOrientation();
        admobSplashListener = new AdmobSplashListener();
        AppOpenAd.load(mContext, getPid(), new AdRequest.Builder().build(), splashOrientation, admobSplashListener.appOpenAdLoadCallback);
    }

    private class AdmobSplashListener extends AbstractAdListener {
        private String impressionId = null;
        AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {

            @Override
            public void onAdLoaded(AppOpenAd appOpenAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mAppOpenAd = appOpenAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mAppOpenAd);
                setSplashListener(appOpenAd);
                setRevenueAverage(getSplashNetwork());
                reportAdLoaded(getSplashNetwork());
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(loadAdError), true));
                setLoading(false, STATE_FAILURE);
                onResetSplash();
                reportAdError(codeToError(loadAdError));
                notifyAdLoadFailed(toSdkError(loadAdError), toErrorMessage(loadAdError));
            }
        };

        private void setSplashListener(final AppOpenAd appOpenAd) {
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
                    String network = getSplashNetwork();
                    reportAdClick(network, null, impressionId);
                    notifyAdClick(network, impressionId);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.iv(Log.TAG, formatLog("ad showed full screen content"));
                    String network = getSplashNetwork();
                    reportAdImp(network, null);
                    notifyAdImp(network, sceneName);
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
            if (appOpenAd != null) {
                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        impressionId = generateImpressionId();
                        String network = null;
                        try {
                            network = appOpenAd.getResponseInfo().getMediationAdapterClassName();
                            network = adapterClassToNetwork(network);
                        } catch (Exception e) {
                        }
                        reportAdmobImpressionData(adValue, network, impressionId, sceneName);
                    }
                });
            }
        }
    }

    @Override
    public boolean showSplash(ViewGroup viewGroup, String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        Log.iv(Log.TAG, getAdPlaceName() + " - " + getSdkName() + " show splash");
        if (admobSplashListener != null) {
            admobSplashListener.sceneName = sceneName;
        }
        if (mAppOpenAd != null) {
            Activity activity = getActivity();
            reportAdShow();
            notifyAdShow();
            mAppOpenAd.show(activity);
            updateLastShowTime();
            return true;
        } else {
            Log.iv(Log.TAG, formatShowErrorLog("AppOpenAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "AppOpenAd not ready");
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

    private void reportAdmobImpressionData(AdValue adValue, String network, String impressionId, String sceneName) {
        try {
            Map<String, Object> map = new HashMap<>();
            try {
                String[] precisionTypes = new String[]{"unknown", "estimated", "publisher_provided", "precise"};
                map.put(Constant.AD_PRECISION, precisionTypes[adValue.getPrecisionType()]);
            } catch (Exception e) {
            }
            // admob给出的是百万次展示的价值，换算ecpm需要除以1000
            double revenue = (double) adValue.getValueMicros() / 1000000;
            if (revenue <= 0f && RFileConfig.isDebuggable()) {
                revenue = (double) new Random().nextInt(50) / 1000;
                map.put(Constant.AD_PRECISION, "random");
            }
            String networkName = network;
            String adUnitId = getPid();
            String adFormat = getAdType();
            String adUnitName = getAdPlaceName();
            map.put(Constant.AD_VALUE, revenue);
            map.put(Constant.AD_MICRO_VALUE, Double.valueOf(revenue * 1000000).intValue());
            map.put(Constant.AD_CURRENCY, "USD");
            map.put(Constant.AD_NETWORK, networkName);
            map.put(Constant.AD_UNIT_ID, adUnitId);
            map.put(Constant.AD_FORMAT, adFormat);
            map.put(Constant.AD_UNIT_NAME, adUnitName);
            map.put(Constant.AD_PLACEMENT, getSceneId(sceneName));
            map.put(Constant.AD_PLATFORM, getSdkName());
            map.put(Constant.AD_SDK_VERSION, getSdkVersion());
            map.put(Constant.AD_APP_VERSION, getAppVersion());
            // map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            onReportAdImpData(map, impressionId);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
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

    private String getInterstitialNetwork() {
        try {
            String adapterClass = mInterstitialAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName();
            return adapterClassToNetwork(adapterClass);
        } catch (Exception e) {
        }
        return null;
    }

    private String getSplashNetwork() {
        try {
            String adapterClass = mAppOpenAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName();
            return adapterClassToNetwork(adapterClass);
        } catch (Exception e) {
        }
        return null;
    }

    private String getNativeNetwork() {
        try {
            String adapterClass = mNativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName();
            return adapterClassToNetwork(adapterClass);
        } catch (Exception e) {
        }
        return null;
    }

    private String getBannerNetwork() {
        try {
            String adapterClass = bannerView.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName();
            return adapterClassToNetwork(adapterClass);
        } catch (Exception e) {
        }
        return null;
    }

    private String getRewardNetwork() {
        try {
            String adapterClass = mRewardedAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName();
            return adapterClassToNetwork(adapterClass);
        } catch (Exception e) {
        }
        return null;
    }

    private String adapterClassToNetwork(String className) {
        String network = null;
        try {
            if (!TextUtils.isEmpty(className)) {
                int index = className.lastIndexOf(".");
                if (index >= 0) {
                    String adapterName = className.substring(index + 1);
                    adapterName = adapterName.toLowerCase(Locale.ENGLISH);
                    network = adapterName.replace("mediationadapter", "").replace("adapter", "");
                }
            }
        } catch (Exception e) {
        }
        return network;
    }

    private void setRevenueAverage(String network) {
        setAdNetworkAndRevenue(network, 0f);
    }
}
