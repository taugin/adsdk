package com.inner.adsdk.adloader.inneractive;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.fyber.inneractive.sdk.external.InneractiveAdManager;
import com.fyber.inneractive.sdk.external.InneractiveAdRequest;
import com.fyber.inneractive.sdk.external.InneractiveAdRequestWithNative;
import com.fyber.inneractive.sdk.external.InneractiveAdSpot;
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager;
import com.fyber.inneractive.sdk.external.InneractiveAdViewEventsListener;
import com.fyber.inneractive.sdk.external.InneractiveAdViewUnitController;
import com.fyber.inneractive.sdk.external.InneractiveAdViewVideoContentController;
import com.fyber.inneractive.sdk.external.InneractiveErrorCode;
import com.fyber.inneractive.sdk.external.InneractiveFullscreenAdEventsListener;
import com.fyber.inneractive.sdk.external.InneractiveFullscreenUnitController;
import com.fyber.inneractive.sdk.external.InneractiveFullscreenVideoContentController;
import com.fyber.inneractive.sdk.external.InneractiveNativeAdUnitController;
import com.fyber.inneractive.sdk.external.InneractiveNativeAdViewBinder;
import com.fyber.inneractive.sdk.external.InneractiveUserConfig;
import com.fyber.inneractive.sdk.external.VideoContentListener;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

public class InnerActiveLoader extends AbstractSdkLoader {

    private final String TAG = "InnerActiveLoader";

    private boolean mIsBannerLoaded;
    private InneractiveAdSpot mBannerSpot;

    private boolean mIsFullScreenLoaded;
    private InneractiveAdSpot mFullScreenSpot;

    private boolean mIsNativeLoaded;
    private InneractiveAdSpot mNativeSpot;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_INNERACTIVE;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        if (!TextUtils.isEmpty(adId)) {
            InneractiveAdManager.initialize(mContext, adId);
        }
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mIsBannerLoaded && mBannerSpot != null && mBannerSpot.isReady() && !isCachedAdExpired(mBannerSpot);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
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
                if (mBannerSpot != null) {
                    mBannerSpot.setRequestListener(null);
                    mBannerSpot.destroy();
                    clearCachedAdTime(mBannerSpot);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mBannerSpot = InneractiveAdSpotManager.get().createSpot();
        InneractiveAdViewUnitController controller = new InneractiveAdViewUnitController();

        InneractiveAdViewVideoContentController videoContentController = new InneractiveAdViewVideoContentController();
        VideoContentListener spotVideoListener = new VideoContentListener() {

            @Override
            public void onProgress(int totalDurationInMsec, int positionInMsec) {
                Log.i(TAG, "onProgress: total time = " + totalDurationInMsec + " position = " + positionInMsec);
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "on Video Completed");
            }

            @Override
            public void onPlayerError() {
                Log.i(TAG, "on Video PlayerError");
            }
        };
        videoContentController.setEventsListener(spotVideoListener);

        controller.addContentController(videoContentController);
        mBannerSpot.addUnitController(controller);

        InneractiveAdRequest adRequest = new InneractiveAdRequest(mPidConfig.getPid());
        adRequest.setUserParams(new InneractiveUserConfig()
                // .setGender()
                // .setZipCode("<zip_code>")
                // .setAge( < age >)
        );
        // adRequest.setKeywords("pop,rock,music");

        InneractiveAdSpot.RequestListener spotListener = new InneractiveAdSpot.RequestListener() {

            @Override
            public void onInneractiveFailedAdRequest(InneractiveAdSpot adSpot, InneractiveErrorCode errorCode) {
                Log.i(TAG, "Failed loading Square! with error: " + errorCode);
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot adSpot) {
                if (adSpot != mBannerSpot) {
                    Log.d(TAG, "Wrong Banner Spot: Received - " + adSpot + ", Actual - " + mBannerSpot);
                    return;
                }

                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mBannerSpot);
                mIsBannerLoaded = true;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                notifyAdLoaded(false);

                InneractiveAdViewUnitController controller =
                        (InneractiveAdViewUnitController) mBannerSpot.getSelectedUnitController();
                controller.setEventsListener(new InneractiveAdViewEventsListener() {
                    @Override
                    public void onAdImpression(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdImpression");
                        Log.v(Log.TAG, "");
                        if (getAdListener() != null) {
                            getAdListener().onAdShow();
                        }
                    }

                    @Override
                    public void onAdClicked(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdClicked");
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
                    }

                    @Override
                    public void onAdWillCloseInternalBrowser(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdWillCloseInternalBrowser");
                    }

                    @Override
                    public void onAdWillOpenExternalApp(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdWillOpenExternalApp");
                    }

                    @Override
                    public void onAdExpanded(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdExpanded");
                    }

                    @Override
                    public void onAdResized(InneractiveAdSpot adSpot) {
                        // Relevant only for MRaid units
                        Log.i(TAG, "onAdResized");
                    }

                    @Override
                    public void onAdCollapsed(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdCollapsed");
                    }
                });
            }
        };
        mBannerSpot.setRequestListener(spotListener);
        mBannerSpot.requestAd(adRequest);
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "admobloader");
        try {
            clearCachedAdTime(mBannerSpot);
            viewGroup.removeAllViews();
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }

            // getting the spot's controller
            InneractiveAdViewUnitController controller = (InneractiveAdViewUnitController) mBannerSpot.getSelectedUnitController();
            controller.unbindView(viewGroup);
            // showing the ad
            controller.bindView(viewGroup);
            mIsBannerLoaded = false;
            if (mStat != null) {
                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "admobloader error : " + e);
        }
    }


    @Override
    public boolean isInterstitialLoaded() {
        return isFullScreenLoaded();
    }

    @Override
    public void loadInterstitial() {
        loadFullScreen(false);
    }

    @Override
    public boolean showInterstitial() {
        return showFullScreen();
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        return isFullScreenLoaded();
    }

    @Override
    public void loadRewardedVideo() {
        loadFullScreen(true);
    }

    @Override
    public boolean showRewardedVideo() {
        return showFullScreen();
    }

    private boolean isFullScreenLoaded() {
        boolean loaded = mIsFullScreenLoaded && mFullScreenSpot != null && mFullScreenSpot.isReady() && !isCachedAdExpired(mFullScreenSpot);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private boolean showFullScreen() {
        if (mFullScreenSpot != null && mFullScreenSpot.isReady()) {
            InneractiveFullscreenUnitController controller = (InneractiveFullscreenUnitController) mFullScreenSpot.getSelectedUnitController();
            controller.show(mContext);

            clearCachedAdTime(mFullScreenSpot);
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

    public void loadFullScreen(final boolean isRewarded) {
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
                if (isRewarded) {
                    getAdListener().onRewardedVideoAdLoaded();
                } else {
                    getAdListener().onInterstitialLoaded();
                }
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
                if (mFullScreenSpot != null) {
                    mFullScreenSpot.setRequestListener(null);
                    mFullScreenSpot.destroy();
                    clearCachedAdTime(mFullScreenSpot);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mFullScreenSpot = InneractiveAdSpotManager.get().createSpot();

        InneractiveFullscreenUnitController controller = new InneractiveFullscreenUnitController();
        mFullScreenSpot.addUnitController(controller);

        InneractiveAdRequest adRequest = new InneractiveAdRequest(mPidConfig.getPid());
        adRequest.setUserParams(new InneractiveUserConfig()
                // .setGender(<gender>)
                // .setZipCode("<zip_code>")
                // .setAge(<age>)
        );
        // adRequest.setKeywords("pop,rock,music");

        InneractiveAdSpot.RequestListener spotListener = new InneractiveAdSpot.RequestListener() {

            @Override
            public void onInneractiveFailedAdRequest(InneractiveAdSpot adSpot, InneractiveErrorCode errorCode) {
                Log.i(TAG, "Failed loading fullscreen ad! with error: " + errorCode);
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(errorCode), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot adSpot) {
                // Register for full screen ad callbacks
                InneractiveFullscreenUnitController controller =
                        (InneractiveFullscreenUnitController) mFullScreenSpot.getSelectedUnitController();
                controller.setEventsListener(new InneractiveFullscreenAdEventsListener() {
                    @Override
                    public void onAdImpression(InneractiveAdSpot inneractiveAdSpot) {
                        Log.i(TAG, "onAdImpression");
                        Log.v(Log.TAG, "");
                        if (mStat != null) {
                            mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                        }
                        if (mStat != null) {
                            mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                        }
                        if (getAdListener() != null) {
                            if (isRewarded) {
                                getAdListener().onRewardedVideoAdShowed();
                            } else {
                                getAdListener().onInterstitialShow();
                            }
                        }
                    }

                    @Override
                    public void onAdClicked(InneractiveAdSpot inneractiveAdSpot) {
                        Log.i(TAG, "onAdClicked");
                        Log.v(Log.TAG, "");
                        if (getAdListener() != null) {
                            if (isRewarded) {
                                getAdListener().onRewardedVideoAdClicked();
                            } else {
                                getAdListener().onInterstitialClick();
                            }
                        }
                        if (mStat != null) {
                            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                        }
                        if (mStat != null) {
                            mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                        }
                    }

                    @Override
                    public void onAdWillOpenExternalApp(InneractiveAdSpot inneractiveAdSpot) {
                        Log.i(TAG, "onAdWillOpenExternalApp");
                    }

                    @Override
                    public void onAdWillCloseInternalBrowser(InneractiveAdSpot inneractiveAdSpot) {
                        Log.i(TAG, "onAdWillCloseInternalBrowser");
                        onAdDismissed(inneractiveAdSpot);
                    }

                    @Override
                    public void onAdDismissed(InneractiveAdSpot inneractiveAdSpot) {
                        Log.i(TAG, "onAdDismissed");
                        mFullScreenSpot = null;
                        mIsFullScreenLoaded = false;
                        if (getAdListener() != null) {
                            if (isRewarded) {
                                getAdListener().onRewardedVideoAdClosed();
                            } else {
                                getAdListener().onInterstitialDismiss();
                            }
                        }
                    }
                });

                InneractiveFullscreenVideoContentController videoContentController = new InneractiveFullscreenVideoContentController();
                VideoContentListener spotVideoListener = new VideoContentListener() {

                    @Override
                    public void onProgress(int totalDurationInMsec, int positionInMsec) {
                        Log.i(TAG, "onProgress: total time = " + totalDurationInMsec + " position = " + positionInMsec);
                        if (positionInMsec == 0) {
                            getAdListener().onRewardedVideoStarted();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted");
                        getAdListener().onRewardedVideoCompleted();
                    }

                    @Override
                    public void onPlayerError() {
                        Log.i(TAG, "onPlayerError");
                    }
                };
                videoContentController.setEventsListener(spotVideoListener);
                controller.addContentController(videoContentController);

                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                mIsFullScreenLoaded = true;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mFullScreenSpot);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    if (isRewarded) {
                        getAdListener().onRewardedVideoAdLoaded();
                    } else {
                        getAdListener().onInterstitialLoaded();
                    }
                }
            }
        };
        mFullScreenSpot.setRequestListener(spotListener);
        mFullScreenSpot.requestAd(adRequest);

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }


    @Override
    public boolean isNativeLoaded() {
        boolean loaded = mIsNativeLoaded && mNativeSpot != null && mNativeSpot.isReady() && !isCachedAdExpired(mNativeSpot);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    private Params mParams;

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
                if (mNativeSpot != null) {
                    mNativeSpot.setRequestListener(null);
                    mNativeSpot.destroy();
                    clearCachedAdTime(mNativeSpot);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mNativeSpot = InneractiveAdSpotManager.get().createSpot();
        InneractiveNativeAdUnitController controller = new InneractiveNativeAdUnitController();

        InneractiveAdViewVideoContentController videoContentController = new InneractiveAdViewVideoContentController();
        // display Rect callbacks
        VideoContentListener spotVideoListener = new VideoContentListener() {
            @Override
            public void onProgress(int totalDurationInMsec, int positionInMsec) {
                Log.i(TAG, "onProgress: + total time = " + totalDurationInMsec + " position = " + positionInMsec);
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted");
            }

            @Override
            public void onPlayerError() {
                Log.i(TAG, "onPlayerError");
            }
        };
        videoContentController.setEventsListener(spotVideoListener);
        controller.addContentController(videoContentController);

        mNativeSpot.addUnitController(controller);

        InneractiveAdRequestWithNative request = new InneractiveAdRequestWithNative(mPidConfig.getPid());
        request.setIsInFeed(false)   // This is an in-feed ad ?
                .setTitleAssetMode(InneractiveAdRequestWithNative.NativeAssetMode.REQUIRED)
                .setActionAssetMode(InneractiveAdRequestWithNative.NativeAssetMode.OPTIONAL)
                .setIconAssetMode(InneractiveAdRequestWithNative.NativeAssetMode.REQUIRED)
                .setDescriptionAssetMode(InneractiveAdRequestWithNative.NativeAssetMode.REQUIRED)
                .setMainAssetMinSize(300, 250)
                .setIconMinSize(30, 30)
                .setMode(InneractiveAdRequestWithNative.Mode.NATIVE_AD_ALL);

        request.setUserParams(new InneractiveUserConfig()
                // .setGender(<gender>)
                // .setZipCode("<zip_code>")
                // .setAge(<age>)
        );
        // request.setKeywords("pop,rock,music");

        InneractiveAdSpot.RequestListener spotListener = new InneractiveAdSpot.RequestListener() {

            @Override
            public void onInneractiveFailedAdRequest(InneractiveAdSpot adSpot, InneractiveErrorCode errorCode) {
                Log.i(TAG, "Failed loading Native! with error: " + errorCode);
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                if (errorCode == InneractiveErrorCode.NO_FILL) {
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

            @Override
            public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot adSpot) {
                InneractiveAdViewUnitController controller =
                        (InneractiveAdViewUnitController) mNativeSpot.getSelectedUnitController();
                controller.setEventsListener(new InneractiveAdViewEventsListener() {
                    @Override
                    public void onAdImpression(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdImpression");
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
                    public void onAdClicked(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdClicked");
                        if (getAdListener() != null) {
                            getAdListener().onAdClick();
                        }
                        if (mStat != null) {
                            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                        }
                        if (mStat != null) {
                            mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                        }
                    }

                    @Override
                    public void onAdWillCloseInternalBrowser(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdWillCloseInternalBrowser");
                    }

                    @Override
                    public void onAdWillOpenExternalApp(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdWillOpenExternalApp");
                    }

                    @Override
                    public void onAdExpanded(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdExpanded");
                    }

                    @Override
                    public void onAdResized(InneractiveAdSpot adSpot) {
                        // Relevant only for MRaid units
                        Log.i(TAG, "onAdResized");
                    }

                    @Override
                    public void onAdCollapsed(InneractiveAdSpot adSpot) {
                        Log.i(TAG, "onAdCollapsed");
                    }
                });

                setLoading(false, STATE_SUCCESS);
                mIsNativeLoaded = true;
                putCachedAdTime(mNativeSpot);
                notifyAdLoaded(false);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }
        };
        mNativeSpot.setRequestListener(spotListener);
        mNativeSpot.requestAd(request);

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        if (params != null) {
            mParams = params;
        }
        // Create a binder, providing it with your assets view ids
        InneractiveNativeAdViewBinder adViewBinder = new InneractiveNativeAdViewBinder.Builder()
                .setIconViewId(mParams.getAdIcon())
                .setTitleViewId(mParams.getAdTitle())
                .setContentHostViewId(mParams.getAdMediaView())
                .setDescriptionViewId(mParams.getAdDetail())
                .setActionButtonViewId(mParams.getAdAction())
                .build();
        InneractiveNativeAdUnitController adUnitController = (InneractiveNativeAdUnitController) mNativeSpot.getSelectedUnitController();
        try {
            adUnitController.bindView(adViewBinder, viewGroup);
        } catch (IllegalArgumentException exception) {
            Log.e("StorySample", "failed binder ad to UI: " + exception.getMessage());
        }
    }

    private String codeToError(InneractiveErrorCode code) {
        return code.toString();
    }

    @Override
    public void destroy() {
        if (mBannerSpot != null) {
            mBannerSpot.destroy();
            mBannerSpot.setRequestListener(null);
            mBannerSpot = null;
        }
        if (mFullScreenSpot != null) {
            mFullScreenSpot.destroy();
            mFullScreenSpot.setRequestListener(null);
            mFullScreenSpot = null;
        }
    }
}
