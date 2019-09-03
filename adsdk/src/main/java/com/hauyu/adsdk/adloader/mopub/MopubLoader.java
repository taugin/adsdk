package com.hauyu.adsdk.adloader.mopub;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;

import java.util.Set;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MopubLoader extends AbstractSdkLoader {

    private MoPubInterstitial moPubInterstitial;
    private MoPubView loadingView;
    private MoPubView moPubView;
    private NativeAd nativeAd;
    private Params mParams;


    private NativeAd gNativeAd;
    private MoPubView gMoPubView;
    private MopubBindNativeView bindNativeView;

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
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnit)
                .build();
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
        loadingView = new MoPubView(mContext);
        loadingView.setAutorefreshEnabled(false);
        loadingView.setAdUnitId(mPidConfig.getPid());
        loadingView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
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
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(errorCode));
                }
                reportAdError(codeToError(errorCode));
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    moPubView = null;
                }
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
                reportAdClose();
            }
        });
        loadingView.loadAd();
        reportAdRequest();
        Log.v(Log.TAG, "");
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
        Log.v(Log.TAG, "mopubloader");
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
            gMoPubView = moPubView;
            if (!isDestroyAfterClick()) {
                moPubView = null;
            }
            reportAdShow();
        } catch (Exception e) {
            Log.e(Log.TAG, "mopubloader error : " + e);
        }
    }

    @Override
    public void loadInterstitial() {
        Activity activity = null;
        if (mManagerListener != null) {
            activity = mManagerListener.getActivity();
        }
        if (activity == null && false) {
            Log.v(Log.TAG, "mopub interstitial need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }

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
        moPubInterstitial = new MoPubInterstitial(mContext, mPidConfig.getPid());
        moPubInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(moPubInterstitial);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(MopubLoader.this);
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(errorCode));
                }
                reportAdError(codeToError(errorCode));
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                reportAdShow();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                reportAdClick();
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                Log.v(Log.TAG, "");
                if (moPubInterstitial != null) {
                    moPubInterstitial.destroy();
                    clearCachedAdTime(moPubInterstitial);
                    moPubInterstitial = null;
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
                reportAdClose();
            }
        });
        moPubInterstitial.load();
        reportAdRequest();
        Log.v(Log.TAG, "");
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
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            boolean showed = moPubInterstitial.show();
            clearCachedAdTime(moPubInterstitial);
            moPubInterstitial = null;
            reportAdCallShow();
            return showed;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = MoPubRewardedVideos.hasRewardedVideo(getPidConfig().getPid());
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadRewardedVideo() {
        Activity activity = null;
        if (mManagerListener != null) {
            activity = mManagerListener.getActivity();
        }
        if (activity == null) {
            Log.v(Log.TAG, "mopub reward need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
//      MoPub.onCreate(activity);

        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getPid())
                .build();
        MoPub.initializeSdk(activity, sdkConfiguration, initSdkListener());

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
        MoPubRewardedVideos.setRewardedVideoListener(new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(MopubLoader.this);
                }
            }

            @Override
            public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(errorCode));
                }
                reportAdError(codeToError(errorCode));
            }

            @Override
            public void onRewardedVideoStarted(@NonNull String adUnitId) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
                reportAdShow();
            }

            @Override
            public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
            }

            @Override
            public void onRewardedVideoClicked(@NonNull String adUnitId) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
                reportAdClick();
            }

            @Override
            public void onRewardedVideoClosed(@NonNull String adUnitId) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
                reportAdClose();
            }

            @Override
            public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
                Log.v(Log.TAG, "");
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
                reportAdReward();
            }
        });
        MoPubRewardedVideos.loadRewardedVideo(getPidConfig().getPid());
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showRewardedVideo() {
        MoPubRewardedVideos.showRewardedVideo(getPidConfig().getPid());
        reportAdCallShow();
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
        MoPubNative moPubNative = new MoPubNative(mContext, mPidConfig.getPid(), new MoPubNative.MoPubNativeNetworkListener() {

            @Override
            public void onNativeLoad(final NativeAd nAd) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                nativeAd = nAd;
                putCachedAdTime(nativeAd);
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , msg : " + codeToErrorNative(errorCode) + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError2(errorCode));
                }
                reportAdError(codeToErrorNative(errorCode));
            }
        });

        bindNativeView = new MopubBindNativeView();
        bindNativeView.bindMopubNative(mParams, mContext, moPubNative, mPidConfig);
        moPubNative.makeRequest();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    private void reportMoPubNativeType() {
        if (gNativeAd != null) {
            MoPubAdRenderer render = gNativeAd.getMoPubAdRenderer();
            if (render instanceof MoPubStaticNativeAdRenderer) {
                reportAdShowWithPlaceName(getAdPlaceName() + "_static");
            } else if (render instanceof MoPubVideoNativeAdRenderer) {
                reportAdShowWithPlaceName(getAdPlaceName() + "_video");
            }
        }
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        Log.v(Log.TAG, "showNative - mopub");
        if (params != null) {
            mParams = params;
        }
        gNativeAd = nativeAd;
        if (nativeAd != null) {
            clearCachedAdTime(nativeAd);
            nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                @Override
                public void onImpression(View view) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onAdImpression();
                    }
                    reportAdShow();
                    reportMoPubNativeType();
                }

                @Override
                public void onClick(View view) {
                    Log.v(Log.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onAdClick();
                    }
                    reportAdClick();
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
                if (nativeAd.getMoPubAdRenderer() instanceof MoPubVideoNativeAdRenderer) {
                    if (mParams.getAdMediaView() > 0) {
                        View view = adView.findViewById(mParams.getAdMediaView());
                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                    if (mParams.getAdCover() > 0) {
                        View view = adView.findViewById(mParams.getAdCover());
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        }
                    }
                } else if (nativeAd.getMoPubAdRenderer() instanceof MoPubStaticNativeAdRenderer) {
                    if (mParams.getAdMediaView() > 0) {
                        View view = adView.findViewById(mParams.getAdMediaView());
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        }
                    }
                    if (mParams.getAdCover() > 0) {
                        View view = adView.findViewById(mParams.getAdCover());
                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if (bindNativeView != null) {
                    bindNativeView.notifyMopubShowing(adView, getPidConfig(), mParams);
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
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
        if (gMoPubView != null) {
            gMoPubView.destroy();
            gMoPubView = null;
        }
        if (gNativeAd != null) {
            gNativeAd.destroy();
            gNativeAd = null;
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
