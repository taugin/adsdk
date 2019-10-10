package com.simple.mpsdk.mopubloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

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
import com.mopub.nativeads.FacebookAdRenderer;
import com.mopub.nativeads.GooglePlayServicesAdRenderer;
import com.mopub.nativeads.InMobiNativeAdRenderer;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.simple.mpsdk.RewardItem;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.framework.Params;
import com.simple.mpsdk.log.LogHelper;

import java.util.Set;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MopubLoader extends BaseMopubLoader {

    private MoPubInterstitial moPubInterstitial;
    private MoPubView loadingView;
    private MoPubView moPubView;
    private NativeAd nativeAd;
    private Params mParams;


    private NativeAd gNativeAd;
    private MoPubView gMoPubView;

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
    public void init(Context context, MpPlace mpPlace) {
        super.init(context, mpPlace);
        String adUnit = null;
        try {
            adUnit = mMpPlace.getPid();
        } catch (Exception e) {
        }
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnit)
                .build();
        MoPub.initializeSdk(mContext, sdkConfiguration, initSdkListener());
    }

    @Override
    public String getName() {
        if (mMpPlace != null) {
            return mMpPlace.getName();
        }
        return null;
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_MOPUB;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPlaceConfig()) {
            LogHelper.v(LogHelper.TAG, "incorrect config : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isBannerLoaded()) {
            LogHelper.d(LogHelper.TAG, "mopub has loaded : " + getName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
            return;
        }
        if (isLoading()) {
            LogHelper.d(LogHelper.TAG, "mopub is loading : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        loadingView = new MoPubView(mContext);
        loadingView.setAutorefreshEnabled(false);
        loadingView.setAdUnitId(mMpPlace.getPid());
        loadingView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                LogHelper.v(LogHelper.TAG, "placement loaded : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                moPubView = loadingView;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getName(), getSdkName(), getAdType(), null);
                }
                notifyAdLoaded(false);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                LogHelper.v(LogHelper.TAG, "error message : " + codeToError(errorCode) + " , placename : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(errorCode));
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                LogHelper.v(LogHelper.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }
        });
        loadingView.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getName(), getSdkName(), getAdType(), null);
        }
        LogHelper.v(LogHelper.TAG, "");
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = moPubView != null && !isCachedAdExpired(moPubView);
        if (loaded) {
            LogHelper.d(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + getName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        LogHelper.v(LogHelper.TAG, "mopubloader show banner");
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
            if (mStat != null) {
                mStat.reportAdImp(mContext, getName(), getSdkName(), getAdType(), null);
            }
        } catch (Exception e) {
            LogHelper.e(LogHelper.TAG, "mopubloader error : " + e);
        }
    }

    @Override
    public void loadInterstitial() {
        Activity activity = null;
        if (mManagerListener != null) {
            activity = mManagerListener.getActivity();
        }
        if (activity == null && false) {
            LogHelper.v(LogHelper.TAG, "mopub interstitial need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }

        if (!checkPlaceConfig()) {
            LogHelper.v(LogHelper.TAG, "incorrect config : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isInterstitialLoaded()) {
            LogHelper.d(LogHelper.TAG, "mopub has loaded : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            LogHelper.d(LogHelper.TAG, "mopub is loading : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        moPubInterstitial = new MoPubInterstitial(mContext, mMpPlace.getPid());
        moPubInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                LogHelper.v(LogHelper.TAG, "placement loaded : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(moPubInterstitial);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialLoaded(MopubLoader.this);
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                LogHelper.v(LogHelper.TAG, "error message : " + codeToError(errorCode) + " , placename : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(errorCode));
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                LogHelper.v(LogHelper.TAG, "");
                if (mStat != null) {
                    mStat.reportAdImp(mContext, getName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                LogHelper.v(LogHelper.TAG, "");
                if (moPubInterstitial != null) {
                    moPubInterstitial.destroy();
                    clearCachedAdTime(moPubInterstitial);
                    moPubInterstitial = null;
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        });
        moPubInterstitial.load();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getName(), getSdkName(), getAdType(), null);
        }
        LogHelper.v(LogHelper.TAG, "");
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (moPubInterstitial != null) {
            loaded = moPubInterstitial.isReady() && !isCachedAdExpired(moPubInterstitial);
        }
        if (loaded) {
            LogHelper.d(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + getName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            boolean showed = moPubInterstitial.show();
            clearCachedAdTime(moPubInterstitial);
            moPubInterstitial = null;
            if (mStat != null) {
                mStat.reportAdShow(mContext, getName(), getSdkName(), getAdType(), null);
            }
            return showed;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = MoPubRewardedVideos.hasRewardedVideo(mMpPlace.getPid());
        if (loaded) {
            LogHelper.d(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + getName() + " - loaded : " + loaded);
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
            LogHelper.v(LogHelper.TAG, "mopub reward need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
//      MoPub.onCreate(activity);

        if (!checkPlaceConfig()) {
            LogHelper.v(LogHelper.TAG, "incorrect config : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getPid())
                .build();
        MoPub.initializeSdk(activity, sdkConfiguration, initSdkListener());

        if (isRewaredVideoLoaded()) {
            LogHelper.d(LogHelper.TAG, "mopub has loaded : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onRewardedVideoAdLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            LogHelper.d(LogHelper.TAG, "mopub is loading : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        MoPubRewardedVideos.setRewardedVideoListener(new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
                LogHelper.v(LogHelper.TAG, "placement loaded : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdLoaded(MopubLoader.this);
                }
            }

            @Override
            public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                LogHelper.v(LogHelper.TAG, "error message : " + codeToError(errorCode) + " , placename : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(errorCode));
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onRewardedVideoStarted(@NonNull String adUnitId) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
                if (mStat != null) {
                    mStat.reportAdImp(mContext, getName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
            }

            @Override
            public void onRewardedVideoClicked(@NonNull String adUnitId) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onRewardedVideoClosed(@NonNull String adUnitId) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
                LogHelper.v(LogHelper.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }
                if (getAdListener() != null) {
                    RewardItem rewardItem = new RewardItem();
                    rewardItem.setType(Constant.ECPM);
                    double ecpm = 0;
                    if (mMpPlace != null) {
                        ecpm = getEcpm();
                    }
                    rewardItem.setAmount(String.valueOf(ecpm));
                    getAdListener().onRewarded(rewardItem);
                }
            }
        });
        MoPubRewardedVideos.loadRewardedVideo(mMpPlace.getPid());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getName(), getSdkName(), getAdType(), null);
        }
        LogHelper.v(LogHelper.TAG, "");
    }

    @Override
    public boolean showRewardedVideo() {
        MoPubRewardedVideos.showRewardedVideo(mMpPlace.getPid());
        if (mStat != null) {
            mStat.reportAdShow(mContext, getName(), getSdkName(), getAdType(), null);
        }
        return true;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (nativeAd != null) {
            loaded = !isCachedAdExpired(nativeAd);
        }
        if (loaded) {
            LogHelper.d(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + getName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadNative(Params params) {
        mParams = params;
        if (!checkPlaceConfig()) {
            LogHelper.v(LogHelper.TAG, "incorrect config : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isNativeLoaded()) {
            LogHelper.d(LogHelper.TAG, "mopub has loaded : " + getName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
            return;
        }
        if (isLoading()) {
            LogHelper.d(LogHelper.TAG, "mopub is loading : " + getName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        MoPubNative moPubNative = new MoPubNative(mContext, mMpPlace.getPid(), new MoPubNative.MoPubNativeNetworkListener() {

            @Override
            public void onNativeLoad(final NativeAd nAd) {
                LogHelper.v(LogHelper.TAG, "placement loaded : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                nativeAd = nAd;
                putCachedAdTime(nativeAd);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getName(), getSdkName(), getAdType(), null);
                }
                notifyAdLoaded(false);
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                LogHelper.e(LogHelper.TAG, "aderror placename : " + getName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , msg : " + codeToErrorNative(errorCode) + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError2(errorCode));
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToErrorNative(errorCode), getSdkName(), getAdType(), null);
                }
            }
        });

        MopubBindNativeView bindNativeView = new MopubBindNativeView();
        bindNativeView.bindMopubNative(mParams, mContext, moPubNative, mMpPlace);
        moPubNative.makeRequest();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getName(), getSdkName(), getAdType(), null);
        }
        LogHelper.v(LogHelper.TAG, "");
    }

    private void reportMoPubNativeType() {
        if (gNativeAd != null) {
            MoPubAdRenderer render = gNativeAd.getMoPubAdRenderer();
            if (render instanceof MoPubStaticNativeAdRenderer) {
                if (mStat != null) {
                    mStat.reportAdImp(mContext, getName() + "_static", getSdkName(), getAdType(), null);
                }
            } else if (render instanceof MoPubVideoNativeAdRenderer) {
                if (mStat != null) {
                    mStat.reportAdImp(mContext, getName() + "_video", getSdkName(), getAdType(), null);
                }
            }
        }
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        LogHelper.v(LogHelper.TAG, "showNative - mopub");
        if (params != null) {
            mParams = params;
        }
        gNativeAd = nativeAd;
        if (nativeAd != null) {
            clearCachedAdTime(nativeAd);
            nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                @Override
                public void onImpression(View view) {
                    LogHelper.v(LogHelper.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onAdImpression();
                    }
                    if (mStat != null) {
                        mStat.reportAdImp(mContext, getName(), getSdkName(), getAdType(), null);
                    }
                    reportMoPubNativeType();
                }

                @Override
                public void onClick(View view) {
                    LogHelper.v(LogHelper.TAG, "");
                    if (getAdListener() != null) {
                        getAdListener().onAdClick();
                    }
                    if (mStat != null) {
                        mStat.reportAdClick(mContext, getName(), getSdkName(), getAdType(), null);
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
                MoPubAdRenderer moPubAdRenderer = nativeAd.getMoPubAdRenderer();
                if (moPubAdRenderer instanceof MoPubVideoNativeAdRenderer
                        || moPubAdRenderer instanceof GooglePlayServicesAdRenderer
                        || moPubAdRenderer instanceof FacebookAdRenderer
                        || moPubAdRenderer instanceof InMobiNativeAdRenderer) {
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
                } else if (moPubAdRenderer instanceof MoPubStaticNativeAdRenderer) {
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
                showIconView(adView, mParams.getAdIcon());
            } catch (Exception e) {
                LogHelper.e(LogHelper.TAG, "error : " + e);
            }
        } else {
            LogHelper.e(LogHelper.TAG, "nativeAd is null");
        }
    }

    private void showIconView(View adView, int iconId) {
        try {
            if (mParams.getAdIcon() > 0) {
                View view = adView.findViewById(iconId);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();
                    if (drawable != null) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                } else if (view instanceof ViewGroup) {
                    ImageView imageView = null;
                    ViewGroup iconLayout = (ViewGroup) view;
                    if (iconLayout != null) {
                        int count = iconLayout.getChildCount();
                        for (int index = 0; index < count; index++) {
                            View v = iconLayout.getChildAt(index);
                            if (v instanceof ImageView) {
                                imageView = (ImageView) v;
                                break;
                            }
                        }
                        if (imageView != null) {
                            Drawable drawable = imageView.getDrawable();
                            if (drawable != null) {
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
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
