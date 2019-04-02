package com.inner.adsdk.adloader.dispio;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

import io.display.sdk.AdProvider;
import io.display.sdk.AdRequest;
import io.display.sdk.BannerPlacement;
import io.display.sdk.Controller;
import io.display.sdk.Placement;
import io.display.sdk.ads.Ad;
import io.display.sdk.ads.BannerAdContainer;
import io.display.sdk.listeners.AdEventListener;
import io.display.sdk.listeners.AdLoadListener;
import io.display.sdk.listeners.AdRequestListener;
import io.display.sdk.listeners.SdkInitListener;

/**
 * Created by Administrator on 2019-2-12.
 */

public class DisplayIoLoader extends AbstractSdkLoader {

    private static final int MSG_BANNER_SHOWN = 123456;
    private static final int DELAY_BANNER_SHOWN = 2000;

    private String mRequestId;
    private RelativeLayout mAdView;
    private Ad mInterstitial;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_DISPLAYIO;
    }

    @Override
    public void setPidConfig(PidConfig config) {
        super.setPidConfig(config);
        Controller.getInstance().setSdkInitListener(new SdkInitListener() {
            @Override
            public void onInit() {
                Log.v(Log.TAG, "display io init success");
            }

            @Override
            public void onInitError(String errorMessage) {
                Log.v(Log.TAG, "display io init error : " + errorMessage);
            }
        });
        Controller.getInstance().init(mContext, mPidConfig.getAppId());
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
                if (mAdView != null) {
                    clearCachedAdTime(mAdView);
                    mAdView = null;
                }
            }
        }

        if (!Controller.getInstance().isInitialized()) {
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
            }
            return;
        }

        try {
            setLoading(true, STATE_REQUEST);
            Placement placement = Controller.getInstance().getPlacement(mPidConfig.getPid());
            AdRequest adRequest = null;
            try {
                adRequest = placement.getLastAdRequest();
            } catch (Exception e) {
            }
            if (adRequest == null) {
                adRequest = placement.newAdRequest();
            }
            adRequest.setAdRequestListener(new AdRequestListener() {
                @Override
                public void onAdReceived(AdProvider adProvider) {
                    adProvider.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onLoaded(Ad ad) {
                            Log.v(Log.TAG, "");
                            setLoading(false, STATE_SUCCESS);
                            if (ad != null) {
                                ad.setEventListener(new AdEventListener() {
                                    @Override
                                    public void onShown(Ad ad) {
                                        if (mHandler != null && !mHandler.hasMessages(MSG_BANNER_SHOWN)) {
                                            mHandler.sendEmptyMessageDelayed(MSG_BANNER_SHOWN, DELAY_BANNER_SHOWN);
                                            Log.v(Log.TAG, "");
                                            if (getAdListener() != null) {
                                                getAdListener().onAdShow();
                                            }
                                            if (mStat != null) {
                                                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                                            }
                                            if (mStat != null) {
                                                mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailedToShow(Ad ad) {
                                    }

                                    @Override
                                    public void onClicked(Ad ad) {
                                        Log.v(Log.TAG, "");
                                        if (getAdListener() != null) {
                                            getAdListener().onAdClick();
                                        }
                                    }

                                    @Override
                                    public void onClosed(Ad ad) {
                                        Log.v(Log.TAG, "");
                                        if (getAdListener() != null) {
                                            getAdListener().onAdDismiss();
                                        }
                                    }

                                    @Override
                                    public void onAdCompleted(Ad ad) {
                                    }
                                });
                            }
                            mAdView = BannerAdContainer.getAdView(mContext);
                            putCachedAdTime(mAdView);
                            if (mStat != null) {
                                mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                            }
                            notifyAdLoaded(false);
                        }

                        @Override
                        public void onFailedToLoad() {
                            Log.v(Log.TAG, "");
                            setLoading(false, STATE_FAILURE);
                            if (getAdListener() != null) {
                                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                            }
                            if (mStat != null) {
                                mStat.reportAdError(mContext, "failed to load", getSdkName(), getAdType(), null);
                            }
                        }
                    });
                    try {
                        adProvider.loadAd();
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                        setLoading(false, STATE_FAILURE);
                        if (getAdListener() != null) {
                            getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                        }
                        if (mStat != null) {
                            mStat.reportAdError(mContext, (e != null ? e.getLocalizedMessage() : "null"), getSdkName(), getAdType(), null);
                        }
                    }
                }

                @Override
                public void onNoAds() {
                    setLoading(false, STATE_FAILURE);
                    if (getAdListener() != null) {
                        getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                    }
                    if (mStat != null) {
                        mStat.reportAdError(mContext, "no ads", getSdkName(), getAdType(), null);
                    }
                }
            });
            adRequest.requestAd();
            mRequestId = adRequest.getId();
            if (mStat != null) {
                mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            Log.v(Log.TAG, "");
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            setLoading(false, STATE_FAILURE);
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
            }
            if (mStat != null) {
                mStat.reportAdError(mContext, (e != null ? e.getLocalizedMessage() : "null"), getSdkName(), getAdType(), null);
            }
        }
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mAdView != null && !isCachedAdExpired(mAdView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "display io loader");
        try {
            clearCachedAdTime(mAdView);
            viewGroup.removeAllViews();
            ViewParent viewParent = mAdView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mAdView);
            }
            viewGroup.addView(mAdView);
            BannerPlacement placement =
                    (BannerPlacement) Controller.getInstance().getPlacement(mPidConfig.getPid());
            BannerAdContainer bannerAdContainer =
                    placement.getBannerContainer(mContext, mRequestId);
            bannerAdContainer.bindTo(mAdView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            mAdView = null;
        } catch (Exception e) {
            Log.e(Log.TAG, "display io loader error : " + e, e);
        }
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
                getAdListener().onInterstitialLoaded();
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
                if (mInterstitial != null) {
                    clearCachedAdTime(mInterstitial);
                }
            }
        }

        if (!Controller.getInstance().isInitialized()) {
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
            }
            return;
        }

        try {
            setLoading(true, STATE_REQUEST);
            Placement placement = Controller.getInstance().getPlacement(mPidConfig.getPid());
            AdRequest adRequest = null;
            try {
                adRequest = placement.getLastAdRequest();
            } catch (Exception e) {
            }
            if (adRequest == null) {
                adRequest = placement.newAdRequest();
            }
            adRequest.setAdRequestListener(new AdRequestListener() {
                @Override
                public void onAdReceived(AdProvider adProvider) {
                    adProvider.setAdLoadListener(new AdLoadListener() {
                        @Override
                        public void onLoaded(Ad ad) {
                            Log.v(Log.TAG, "");
                            setLoading(false, STATE_SUCCESS);
                            if (ad != null) {
                                ad.setEventListener(new AdEventListener() {
                                    @Override
                                    public void onShown(Ad ad) {
                                        if (mStat != null) {
                                            mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                                        }
                                        if (mStat != null) {
                                            mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                                        }
                                        Log.v(Log.TAG, "");
                                    }

                                    @Override
                                    public void onFailedToShow(Ad ad) {
                                        Log.v(Log.TAG, "");
                                    }

                                    @Override
                                    public void onClicked(Ad ad) {
                                        if (getAdListener() != null) {
                                            getAdListener().onInterstitialClick();
                                        }
                                        if (mStat != null) {
                                            mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                                        }
                                        if (mStat != null) {
                                            mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                                        }
                                    }

                                    @Override
                                    public void onClosed(Ad ad) {
                                        Log.v(Log.TAG, "");
                                        if (getAdListener() != null) {
                                            getAdListener().onInterstitialDismiss();
                                        }
                                    }

                                    @Override
                                    public void onAdCompleted(Ad ad) {
                                        Log.v(Log.TAG, "");
                                    }
                                });
                            }
                            mInterstitial = ad;
                            putCachedAdTime(mInterstitial);
                            if (mStat != null) {
                                mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                            }
                            notifyAdLoaded(false);
                        }

                        @Override
                        public void onFailedToLoad() {
                            Log.v(Log.TAG, "");
                            setLoading(false, STATE_FAILURE);
                            if (getAdListener() != null) {
                                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                            }
                            if (mStat != null) {
                                mStat.reportAdError(mContext, "failed to load", getSdkName(), getAdType(), null);
                            }
                        }
                    });
                    try {
                        adProvider.loadAd();
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                        setLoading(false, STATE_FAILURE);
                        if (getAdListener() != null) {
                            getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                        }
                        if (mStat != null) {
                            mStat.reportAdError(mContext, (e != null ? e.getLocalizedMessage() : "null"), getSdkName(), getAdType(), null);
                        }
                    }
                }

                @Override
                public void onNoAds() {
                    setLoading(false, STATE_FAILURE);
                    if (getAdListener() != null) {
                        getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                    }
                    if (mStat != null) {
                        mStat.reportAdError(mContext, "no ads", getSdkName(), getAdType(), null);
                    }
                }
            });
            adRequest.requestAd();
            mRequestId = adRequest.getId();
            if (mStat != null) {
                mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            Log.v(Log.TAG, "");
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            setLoading(false, STATE_FAILURE);
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
            }
            if (mStat != null) {
                mStat.reportAdError(mContext, (e != null ? e.getLocalizedMessage() : "null"), getSdkName(), getAdType(), null);
            }
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (mInterstitial != null) {
            loaded = mInterstitial.isLoaded() && !isCachedAdExpired(mInterstitial);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (mInterstitial != null && mInterstitial.isLoaded()) {
            mInterstitial.showAd(mContext);
            clearCachedAdTime(mInterstitial);
            mInterstitial = null;
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

    @Override
    public void resume() {
        try {
            Controller.getInstance()
                    .getPlacement(mPidConfig.getPid())
                    .getAdRequestById(mRequestId)
                    .getAdProvider().getAd().activityResumed();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void pause() {
        try {
            Controller.getInstance()
                    .getPlacement(mPidConfig.getPid())
                    .getAdRequestById(mRequestId)
                    .getAdProvider().getAd().activityPaused();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            Controller.getInstance()
                    .getPlacement(mPidConfig.getPid())
                    .getAdRequestById(mRequestId)
                    .getAdProvider().getAd().close();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        Controller.getInstance().onDestroy();
    }
}