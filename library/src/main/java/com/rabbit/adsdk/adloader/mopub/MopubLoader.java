package com.rabbit.adsdk.adloader.mopub;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedAdListener;
import com.mopub.mobileads.MoPubRewardedAdManager;
import com.mopub.mobileads.MoPubRewardedAds;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.FacebookAdRenderer;
import com.mopub.nativeads.GooglePlayServicesAdRenderer;
import com.mopub.nativeads.MintegralAdRenderer;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.network.ImpressionData;
import com.mopub.network.ImpressionListener;
import com.mopub.network.ImpressionsEmitter;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.applovin.AppLovinLoader;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MopubLoader extends AbstractSdkLoader {

    private static AtomicBoolean sHasSetImpressionListener = new AtomicBoolean(false);
    protected static final Map<Integer, MoPubView.MoPubAdSize> ADSIZE = new HashMap<>();
    private static SDKInitializeState sSdkInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    static {
        ADSIZE.put(Constant.BANNER, MoPubView.MoPubAdSize.HEIGHT_50);
        ADSIZE.put(Constant.LARGE_BANNER, MoPubView.MoPubAdSize.HEIGHT_90);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, MoPubView.MoPubAdSize.HEIGHT_250);
        ADSIZE.put(Constant.FULL_BANNER, MoPubView.MoPubAdSize.HEIGHT_280);
        ADSIZE.put(Constant.SMART_BANNER, MoPubView.MoPubAdSize.MATCH_VIEW);
    }

    private MoPubInterstitial moPubInterstitial;
    private MoPubInterstitial moPubLoadingInterstitial;

    private MoPubView loadingView;
    private MoPubView moPubView;
    private NativeAd nativeAd;

    private NativeAd lastUseNativeAd;
    private MoPubView lastUseMoPubView;
    private MopubBindNativeView bindNativeView = new MopubBindNativeView();

    private String mLoadedRewardUnit;
    private CountDownTimer mStateChecker;

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        if (!sHasSetImpressionListener.getAndSet(true)) {
            setImpressionListener();
        }
    }

    private ImpressionListener mImpressionListener = (adUnitId, impressionData) -> {
        String impData = null;
        try {
            impData = impressionData.getJsonRepresentation().toString(2);
        } catch (Exception | Error e) {
        }
        Log.iv(Log.TAG, getSdkName() + " impression pid : " + adUnitId + " , impData : " + impData);
        reportAdImpressionRevenue(impressionData);
    };

    private void setImpressionListener() {
        Log.iv(Log.TAG, "add impression listener for mopub");
        try {
            ImpressionsEmitter.removeListener(mImpressionListener);
        } catch (Exception e) {
            Log.iv(Log.TAG, "remove impression listener for mopub error");
        }
        ImpressionsEmitter.addListener(mImpressionListener);
    }

    protected BaseBindNativeView getBaseBindNativeView() {
        return bindNativeView;
    }

    private ConsentDialogListener initDialogLoadListener() {
        return new ConsentDialogListener() {

            @Override
            public void onConsentDialogLoaded() {
                PersonalInfoManager manager = MoPub.getPersonalInformationManager();
                if (manager != null) {
                    manager.showConsentDialog();
                }
            }

            @Override
            public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
            }
        };
    }

    private void checkSdkInitializeState(final SDKInitializeListener sdkInitializeListener) {
        if (mStateChecker == null) {
            Log.iv(Log.TAG, getSdkName() + " sdk init start checking");
            mStateChecker = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.iv(Log.TAG, getSdkName() + " sdk init state check");
                    if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                        if (mStateChecker != null) {
                            mStateChecker.cancel();
                            mStateChecker = null;
                        }
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeSuccess(null, null);
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Log.iv(Log.TAG, getSdkName() + " sdk init timeout");
                    mStateChecker = null;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeFailure("timeout");
                    }
                }
            };
            mStateChecker.start();
        } else {
            Log.iv(Log.TAG, getSdkName() + " sdk initializing");
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeFailure("initializing");
            }
        }
    }

    private void configSdkInit(final SDKInitializeListener sdkInitializeListener) {
        if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZING) {
            checkSdkInitializeState(sdkInitializeListener);
        } else {
            if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeSuccess(null, null);
                }
                return;
            }
            sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZING;
            if (sHandler != null) {
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sSdkInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeFailure("timeout");
                        }
                    }
                }, 10000);
            }
            String adUnit = null;
            try {
                adUnit = getPidConfig().getPid();
            } catch (Exception e) {
            }
            SdkConfiguration.Builder builder = new SdkConfiguration.Builder(adUnit);
            try {
                Map<String, Map<String, String>> config = DataManager.get(mContext).getMediationConfig();
                Log.iv(Log.TAG, "config : " + config);
                if (config != null && !config.isEmpty()) {
                    for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
                        String key = entry.getKey();
                        if (!TextUtils.isEmpty(key) && !TextUtils.equals(key, "common_config")) {
                            builder.withMediatedNetworkConfiguration(entry.getKey(), entry.getValue());
                            builder.withAdditionalNetwork(entry.getKey());
                        }
                    }
                    Map<String, String> commonConfig = config.get("common_config");
                    if (commonConfig != null) {
                        String logLevel = commonConfig.get("mopub_log_level");
                        Log.iv(Log.TAG, "mopub log level : " + logLevel);
                        if (TextUtils.equals(logLevel, "debug")) {
                            builder.withLogLevel(MoPubLog.LogLevel.DEBUG);
                        } else if (TextUtils.equals(logLevel, "info")) {
                            builder.withLogLevel(MoPubLog.LogLevel.INFO);
                        } else if (TextUtils.equals(logLevel, "none")) {
                            builder.withLogLevel(MoPubLog.LogLevel.NONE);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            SdkConfiguration sdkConfiguration = builder.build();
            Activity activity = getActivity();
            MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    Log.iv(Log.TAG, getSdkName() + " sdk init successfully");
                    if (sHandler != null) {
                        sHandler.removeCallbacksAndMessages(null);
                    }
                    sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess(null, null);
                    }
                    PersonalInfoManager manager = MoPub.getPersonalInformationManager();
                    if (manager != null && manager.shouldShowConsentDialog()) {
                        manager.loadConsentDialog(initDialogLoadListener());
                    }
                }
            });
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_MOPUB;
    }

    @Override
    public void loadBanner(final int adSize) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess(String appId, String appSecret) {
                loadBannerInternal(adSize);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE);
            }
        });
    }

    private void loadBannerInternal(int adSize) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        boolean activityContext = false;
        try {
            activityContext = getPidConfig().isActivityContext();
        } catch (Exception e) {
        }
        Context context = activityContext ? getActivity() : mContext;
        if (context == null) {
            context = mContext;
        }
        setBannerSize(adSize);
        MoPubView.MoPubAdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = MoPubView.MoPubAdSize.HEIGHT_50;
        }
        loadingView = new MoPubView(context);
        loadingView.setAutorefreshEnabled(false);
        loadingView.setAdUnitId(mPidConfig.getPid());
        loadingView.setAdSize(size);
        loadingView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                moPubView = loadingView;
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(errorCode), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                notifyAdLoadFailed(toSdkError(errorCode));
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                Log.iv(Log.TAG, formatLog("ad banner click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                Log.iv(Log.TAG, formatLog("ad banner expanded"));
                notifyAdImp();
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                Log.iv(Log.TAG, formatLog("ad banner collapsed"));
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadingView.loadAd();
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = moPubView != null && !isCachedAdExpired(moPubView);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            clearCachedAdTime(moPubView);
            viewGroup.removeAllViews();
            ViewParent viewParent = moPubView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(moPubView);
            }
            viewGroup.addView(moPubView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            lastUseMoPubView = moPubView;
            moPubView = null;
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("show error : " + e));
        }
    }

    @Override
    public void loadInterstitial() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess(String appId, String appSecret) {
                loadInterstitialInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE);
            }
        });
    }

    private void loadInterstitialInternal() {
        Activity activity = getActivity();

        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        moPubLoadingInterstitial = new MoPubInterstitial(activity, mPidConfig.getPid());
        moPubLoadingInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                moPubInterstitial = moPubLoadingInterstitial;
                putCachedAdTime(moPubInterstitial);
                reportAdLoaded();
                notifyAdLoaded(MopubLoader.this);
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(errorCode), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(codeToError(errorCode));
                notifyAdLoadFailed(toSdkError(errorCode));
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                Log.iv(Log.TAG, formatLog("ad interstitial shown"));
                reportAdImp();
                notifyAdImp();
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                Log.iv(Log.TAG, formatLog("ad interstitial click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                Log.iv(Log.TAG, formatLog("ad interstitial dismissed"));
                clearLastShowTime();
                onResetInterstitial();
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        moPubLoadingInterstitial.load();
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = moPubInterstitial != null && !isCachedAdExpired(moPubInterstitial) && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            boolean showed = moPubInterstitial.show();
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return showed;
        } else {
            onResetInterstitial();
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = !TextUtils.isEmpty(mLoadedRewardUnit)
                && !isCachedAdExpired(mLoadedRewardUnit)
                && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadRewardedVideo() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess(String appId, String appSecret) {
                loadRewardedVideoInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE);
            }
        });
    }

    private void loadRewardedVideoInternal() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }

        MoPubRewardedAdManager.updateActivity(activity);

        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        MoPubRewardedAdManager.setRewardedAdListener(new MoPubRewardedAdListener() {
            @Override
            public void onRewardedAdLoadSuccess(String s) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mLoadedRewardUnit = s;
                putCachedAdTime(s);
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                notifyAdLoaded(MopubLoader.this);
            }

            @Override
            public void onRewardedAdLoadFailure(String s, MoPubErrorCode moPubErrorCode) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(moPubErrorCode), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(codeToError(moPubErrorCode));
                notifyAdLoadFailed(toSdkError(moPubErrorCode));
            }

            @Override
            public void onRewardedAdStarted(String s) {
                Log.iv(Log.TAG, formatLog("ad reward start"));
                reportAdImp();
                notifyAdImp();
                notifyAdOpened();
                notifyRewardAdsStarted();
            }

            @Override
            public void onRewardedAdClicked(String s) {
                Log.iv(Log.TAG, formatLog("ad reward click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onRewardedAdClosed(String s) {
                Log.iv(Log.TAG, formatLog("ad reward closed"));
                clearLastShowTime();
                onResetReward();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onRewardedAdShowError(String s, MoPubErrorCode moPubErrorCode) {
                onResetReward();
                notifyAdShowFailed(toSdkError(moPubErrorCode));
            }

            @Override
            public void onRewardedAdCompleted(Set<String> set, MoPubReward moPubReward) {
                Log.iv(Log.TAG, formatLog("ad reward complete"));
                reportAdReward();
                notifyRewardAdsCompleted();
                AdReward adReward = new AdReward();
                adReward.setType(Constant.ECPM);
                double ecpm = 0;
                if (mPidConfig != null) {
                    ecpm = mPidConfig.getEcpm();
                }
                adReward.setAmount(String.valueOf(ecpm));
                notifyRewarded(adReward);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        MoPubRewardedAds.loadRewardedAd(getPidConfig().getPid());
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (MoPubRewardedAds.hasRewardedAd(getPidConfig().getPid())) {
            MoPubRewardedAds.showRewardedAd(getPidConfig().getPid());
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetReward();
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (nativeAd != null) {
            loaded = !isCachedAdExpired(nativeAd);
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
            public void onInitializeSuccess(String appId, String appSecret) {
                loadNativeInternal(params);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE);
            }
        });
    }

    private void loadNativeInternal(Params params) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        MoPubNative moPubNative = new MoPubNative(mContext, mPidConfig.getPid(), new MoPubNative.MoPubNativeNetworkListener() {

            @Override
            public void onNativeLoad(final NativeAd nAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                nativeAd = nAd;
                putCachedAdTime(nativeAd);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToErrorNative(errorCode), true));
                reportAdError(codeToErrorNative(errorCode));
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(toSdkError2(errorCode));
            }
        });

        bindNativeView.bindMopubNative(params, mContext, moPubNative, mPidConfig);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        moPubNative.makeRequest();
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (nativeAd != null) {
            clearCachedAdTime(nativeAd);
            nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                @Override
                public void onImpression(View view) {
                    String render = getRender();
                    Log.iv(Log.TAG, formatLog("ad network impression render : " + render));
                    reportAdImp(render);
                    notifyAdImp(render);
                    if (bindNativeView != null) {
                        bindNativeView.updateClickView(viewGroup, getPidConfig(), render);
                    }
                }

                @Override
                public void onClick(View view) {
                    String render = getRender();
                    Log.iv(Log.TAG, formatLog("ad network click render : " + render));
                    reportAdClick(render);
                    notifyAdClick(render);
                }
            });

            try {
                View adView = nativeAd.createAdView(mContext, viewGroup);
                nativeAd.prepare(adView);
                nativeAd.renderAdView(adView);
                ViewParent parent = adView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(adView);
                }
                viewGroup.removeAllViews();
                viewGroup.addView(adView);
                if (viewGroup.getVisibility() != View.VISIBLE) {
                    viewGroup.setVisibility(View.VISIBLE);
                }
                boolean staticRender = nativeAd.getMoPubAdRenderer() instanceof MoPubStaticNativeAdRenderer;
                if (bindNativeView != null) {
                    bindNativeView.notifyAdViewShowing(adView, getPidConfig(), staticRender);
                    bindNativeView.putAdvertiserInfo(nativeAd);
                }
                lastUseNativeAd = nativeAd;
                nativeAd = null;
                reportAdShow();
                notifyAdShow();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
        } else {
            Log.iv(Log.TAG, formatLog("nativeAd is null"));
        }
    }

    private String getRender() {
        String render = Constant.AD_SDK_MOPUB;
        try {
            if (lastUseNativeAd != null) {
                MoPubAdRenderer adRenderer = lastUseNativeAd.getMoPubAdRenderer();
                if (adRenderer instanceof GooglePlayServicesAdRenderer) {
                    render = Constant.AD_SDK_ADMOB;
                } else if (adRenderer instanceof FacebookAdRenderer) {
                    render = Constant.AD_SDK_FACEBOOK;
                } else if (adRenderer instanceof MintegralAdRenderer) {
                    render = Constant.AD_SDK_MINTEGRAL;
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return render;
    }

    @Override
    public void resume() {
        if (mManagerListener != null) {
            Activity activity = mManagerListener.getActivity();
            if (activity != null) {
                MoPub.onResume(activity);
            }
        }
    }

    @Override
    public void pause() {
        if (mManagerListener != null) {
            Activity activity = mManagerListener.getActivity();
            if (activity != null) {
                MoPub.onPause(activity);
            }
        }
    }

    @Override
    public void destroy() {
        if (lastUseMoPubView != null) {
            lastUseMoPubView.destroy();
            lastUseMoPubView = null;
        }
        if (lastUseNativeAd != null) {
            lastUseNativeAd.destroy();
            lastUseNativeAd = null;
        }
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(moPubInterstitial);
        if (moPubInterstitial != null) {
            moPubInterstitial.destroy();
            moPubInterstitial = null;
        }
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(mLoadedRewardUnit);
        if (mLoadedRewardUnit != null) {
            mLoadedRewardUnit = null;
        }
    }

    private String codeToError(MoPubErrorCode errorCode) {
        if (errorCode != null) {
            return errorCode.toString();
        }
        return "UNKNOWN";
    }

    private String codeToErrorNative(NativeErrorCode errorCode) {
        if (errorCode != null) {
            return errorCode.toString();
        }
        return "UNKNOWN";
    }

    protected int toSdkError(MoPubErrorCode errorCode) {
        if (errorCode == MoPubErrorCode.INTERNAL_ERROR) {
            return Constant.AD_ERROR_INTERNAL;
        }
        if (errorCode == MoPubErrorCode.NETWORK_INVALID_STATE) {
            return Constant.AD_ERROR_INVALID_REQUEST;
        }
        if (errorCode == MoPubErrorCode.NETWORK_INVALID_STATE) {
            return Constant.AD_ERROR_NETWORK;
        }
        if (errorCode == MoPubErrorCode.NO_FILL) {
            return Constant.AD_ERROR_NOFILL;
        }
        if (errorCode == MoPubErrorCode.NETWORK_TIMEOUT) {
            return Constant.AD_ERROR_TIMEOUT;
        }
        if (errorCode == MoPubErrorCode.SERVER_ERROR) {
            return Constant.AD_ERROR_SERVER;
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    protected int toSdkError2(NativeErrorCode errorCode) {
        if (errorCode == NativeErrorCode.NETWORK_INVALID_STATE) {
            return Constant.AD_ERROR_INVALID_REQUEST;
        }
        if (errorCode == NativeErrorCode.NETWORK_INVALID_STATE) {
            return Constant.AD_ERROR_NETWORK;
        }
        if (errorCode == NativeErrorCode.NETWORK_NO_FILL) {
            return Constant.AD_ERROR_NOFILL;
        }
        if (errorCode == NativeErrorCode.NETWORK_TIMEOUT) {
            return Constant.AD_ERROR_TIMEOUT;
        }
        if (errorCode == NativeErrorCode.SERVER_ERROR_RESPONSE_CODE) {
            return Constant.AD_ERROR_SERVER;
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    private void reportAdImpressionRevenue(ImpressionData impressionData) {
        try {
            if (impressionData != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", impressionData.getPublisherRevenue());
                map.put("currency", impressionData.getCurrency());
                map.put("precisionType", impressionData.getPrecision());
                map.put("ad_network", impressionData.getNetworkName());
                map.put("ad_format", impressionData.getAdUnitFormat());
                map.put("ad_unit_id", impressionData.getAdUnitId());
                map.put("ad_unit_name", impressionData.getAdUnitName());
                map.put("ad_group_name", impressionData.getAdGroupName());
                map.put("ad_provider", getSdkName());
                String gaid = Utils.getString(mContext, Constant.PREF_GAID);
                map.put("ad_gaid", gaid);
                if (isReportAdImpData()) {
                    InternalStat.reportEvent(getContext(), "Ad_Impression_Revenue", map);
                }
                StringBuilder builder = new StringBuilder("{");
                builder.append("\n");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    builder.append("  " + entry.getKey() + " : " + entry.getValue());
                    builder.append("\n");
                }
                builder.append("}");
                Log.iv(Log.TAG, getSdkName() + " imp data : " + builder.toString());
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
}
