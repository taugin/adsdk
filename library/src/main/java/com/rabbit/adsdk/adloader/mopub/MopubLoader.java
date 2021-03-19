package com.rabbit.adsdk.adloader.mopub;

import android.app.Activity;
import android.content.Context;
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
import com.mopub.mobileads.MoPubRewardedVideos;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MopubLoader extends AbstractSdkLoader {

    protected static final Map<Integer, MoPubView.MoPubAdSize> ADSIZE = new HashMap<>();

    static {
        ADSIZE.put(Constant.BANNER, MoPubView.MoPubAdSize.HEIGHT_50);
        ADSIZE.put(Constant.LARGE_BANNER, MoPubView.MoPubAdSize.HEIGHT_90);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, MoPubView.MoPubAdSize.HEIGHT_250);
        ADSIZE.put(Constant.FULL_BANNER, MoPubView.MoPubAdSize.HEIGHT_280);
        ADSIZE.put(Constant.SMART_BANNER, MoPubView.MoPubAdSize.MATCH_VIEW);
    }

    private MoPubInterstitial moPubInterstitial;
    private MoPubView loadingView;
    private MoPubView moPubView;
    private NativeAd nativeAd;

    private NativeAd lastUseNativeAd;
    private MoPubView lastUseMoPubView;
    private MopubBindNativeView bindNativeView = new MopubBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return bindNativeView;
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {

            @Override
            public void onInitializationFinished() {
                PersonalInfoManager manager = MoPub.getPersonalInformationManager();
                if (manager != null && manager.shouldShowConsentDialog()) {
                    manager.loadConsentDialog(initDialogLoadListener());
                }
            }
        };
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

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
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
                    }
                }
                Map<String, String> commonConfig = config.get("common_config");
                if (commonConfig != null) {
                    String logLevel = commonConfig.get("log_level");
                    Log.iv(Log.TAG, "log_level : " + logLevel);
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
        MoPub.initializeSdk(mContext, sdkConfiguration, initSdkListener());
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_MOPUB;
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
            notifyAdLoaded(true);
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
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                moPubView = loadingView;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                notifyAdFailed(toSdkError(errorCode));
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyAdClick();
                if (isDestroyAfterClick()) {
                    moPubView = null;
                }
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                Log.v(Log.TAG, "");
                notifyAdImp();
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                Log.v(Log.TAG, "");
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        loadingView.loadAd();
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = moPubView != null && !isCachedAdExpired(moPubView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
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
            if (!isDestroyAfterClick()) {
                moPubView = null;
            }
            reportAdShow();
            reportAdImp();
            notifyAdShow();
        } catch (Exception e) {
            Log.e(Log.TAG, "mopubloader error : " + e);
        }
    }

    @Override
    public void loadInterstitial() {
        Activity activity = getActivity();

        if (activity == null) {
            Log.v(Log.TAG, "mopub interstitial need an activity context");
            notifyInterstitialError(Constant.AD_ERROR_CONTEXT);
            return;
        }

        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyInterstitialError(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyInterstitialLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyInterstitialError(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        moPubInterstitial = new MoPubInterstitial(activity, mPidConfig.getPid());
        moPubInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(moPubInterstitial);
                reportAdLoaded();
                notifyInterstitialLoaded(MopubLoader.this);
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                notifyInterstitialError(toSdkError(errorCode));
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                reportAdImp();
                notifyInterstitialImp();
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyInterstitialClick();
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                if (moPubInterstitial != null) {
                    moPubInterstitial.destroy();
                    clearCachedAdTime(moPubInterstitial);
                    moPubInterstitial = null;
                }
                reportAdClose();
                notifyInterstitialDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        moPubInterstitial.load();
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (moPubInterstitial != null) {
            loaded = moPubInterstitial.isReady() && !isCachedAdExpired(moPubInterstitial);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            boolean showed = moPubInterstitial.show();
            clearCachedAdTime(moPubInterstitial);
            moPubInterstitial = null;
            reportAdShow();
            notifyAdShow();
            return showed;
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = MoPubRewardedVideos.hasRewardedVideo(getPidConfig().getPid());
        boolean finalLoaded = loaded || isRewardPlaying();
        if (finalLoaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded + " , playing : " + isRewardPlaying());
        }
        return finalLoaded;
    }

    @Override
    public void loadRewardedVideo() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.v(Log.TAG, "mopub reward need an activity context");
            notifyRewardedVideoError(Constant.AD_ERROR_CONTEXT);
            return;
        }
//      MoPub.onCreate(activity);

        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyRewardedVideoError(Constant.AD_ERROR_CONFIG);
            return;
        }

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getPid())
                .build();
        MoPub.initializeSdk(activity, sdkConfiguration, initSdkListener());
        MoPubRewardedAdManager.updateActivity(activity);

        if (isRewardedVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyRewardedVideoAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyRewardedVideoError(Constant.AD_ERROR_LOADING);
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
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                notifyRewardedVideoAdLoaded(MopubLoader.this);
            }

            @Override
            public void onRewardedAdLoadFailure(String s, MoPubErrorCode moPubErrorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(moPubErrorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(moPubErrorCode));
                notifyRewardedVideoError(toSdkError(moPubErrorCode));
            }

            @Override
            public void onRewardedAdStarted(String s) {
                Log.v(Log.TAG, "");
                setRewardPlaying(true);
                reportAdImp();
                notifyRewardedVideoAdOpened();
                notifyRewardedVideoStarted();
            }

            @Override
            public void onRewardedAdClicked(String s) {
                Log.v(Log.TAG, "");
                reportAdClick();
                notifyRewardedVideoAdClicked();
            }

            @Override
            public void onRewardedAdClosed(String s) {
                Log.v(Log.TAG, "");
                setRewardPlaying(false);
                reportAdClose();
                notifyRewardedVideoAdClosed();
            }

            @Override
            public void onRewardedAdShowError(String s, MoPubErrorCode moPubErrorCode) {
            }

            @Override
            public void onRewardedAdCompleted(Set<String> set, MoPubReward moPubReward) {
                Log.v(Log.TAG, "");
                reportAdReward();
                notifyRewardedVideoCompleted();
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
        MoPubRewardedAds.loadRewardedAd(getPidConfig().getPid());
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        MoPubRewardedAds.showRewardedAd(getPidConfig().getPid());
        reportAdShow();
        notifyAdShow();
        return true;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
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
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "nofill error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdFailed(Constant.AD_ERROR_FILLTIME);
            return;
        }
        if (isNativeLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
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
        MoPubNative moPubNative = new MoPubNative(mContext, mPidConfig.getPid(), new MoPubNative.MoPubNativeNetworkListener() {

            @Override
            public void onNativeLoad(final NativeAd nAd) {
                Log.v(Log.TAG, "ad loaded name : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                nativeAd = nAd;
                putCachedAdTime(nativeAd);
                notifyAdLoaded(false);
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , msg : " + codeToErrorNative(errorCode) + " , pid : " + getPid());
                reportAdError(codeToErrorNative(errorCode));
                setLoading(false, STATE_FAILURE);
                notifyAdFailed(toSdkError2(errorCode));
            }
        });

        bindNativeView.bindMopubNative(params, mContext, moPubNative, mPidConfig);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        moPubNative.makeRequest();
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        lastUseNativeAd = nativeAd;
        if (nativeAd != null) {
            clearCachedAdTime(nativeAd);
            nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                @Override
                public void onImpression(View view) {
                    Log.v(Log.TAG, "");
                    reportAdImp();
                    notifyAdImp();
                }

                @Override
                public void onClick(View view) {
                    Log.v(Log.TAG, "");
                    reportAdClick();
                    notifyAdClick();
                    if (isDestroyAfterClick()) {
                        nativeAd = null;
                    }
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
                reportAdShow();
                notifyAdShow();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
            if (!isDestroyAfterClick()) {
                nativeAd = null;
            }
        } else {
            Log.e(Log.TAG, "nativeAd is null");
        }
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

}
