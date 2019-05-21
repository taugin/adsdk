package com.inner.adsdk.adloader.dspmob;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.we.sdk.exchange.AdSize;
import com.we.sdk.exchange.DspMob;
import com.we.sdk.exchange.ExchangeAdError;
import com.we.sdk.exchange.ExchangeAdListener;
import com.we.sdk.exchange.ExchangeBannerAd;
import com.we.sdk.exchange.ExchangeInterstitialAd;

public class DspMobLoader extends AbstractSdkLoader {

    private final String TAG = "DspMobLoader";

    private boolean mIsBannerLoaded;
    private ExchangeBannerAd mBannerAd;

    private boolean mIsInterstitialLoaded;
    private ExchangeInterstitialAd mInterstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_DSPMOB;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        DspMob.init(mContext);
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mIsBannerLoaded && mBannerAd != null && !isCachedAdExpired(mBannerAd);
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
                if (mBannerAd != null) {
                    mBannerAd.destroy();
                    clearCachedAdTime(mBannerAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);

        mBannerAd = new ExchangeBannerAd(mContext);
        if (adSize == Constant.BANNER) {
            mBannerAd.setAdSize(AdSize.Banner_320_50);
        } else if (adSize == Constant.MEDIUM_RECTANGLE) {
            mBannerAd.setAdSize(AdSize.Banner_300_250);
        } else {
            mBannerAd.setAdSize(AdSize.Banner_320_50);
        }
        mBannerAd.setBidFloor((float) getPidConfig().getEcpm() / 100f);
        mBannerAd.setListener(new ExchangeAdListener() {
            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mBannerAd);
                mIsBannerLoaded = true;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onAdShown() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                reportAdClick();
                reportAdClickForLTV();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    mIsBannerLoaded = false;
                }
            }

            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(ExchangeAdError adError) {
                Log.v(Log.TAG, "reason : " + codeToError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(adError));
                }
                reportAdError(codeToError(adError));
            }
        });
        mBannerAd.request();

        reportAdRequest();
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "show");
        try {
            clearCachedAdTime(mBannerAd);
            viewGroup.removeAllViews();
            ViewParent viewParent = mBannerAd.getAdView().getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mBannerAd.getAdView());
            }
            viewGroup.addView(mBannerAd.getAdView());
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }

            if (!isDestroyAfterClick()) {
                mIsBannerLoaded = false;
            }
            reportAdShow();
            reportAdImpForLTV();
        } catch (Exception e) {
            Log.e(Log.TAG, "loader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mIsInterstitialLoaded && mInterstitialAd != null && mInterstitialAd.isReady() && !isCachedAdExpired(mInterstitialAd);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isReady()) {
            mInterstitialAd.show();

            clearCachedAdTime(mInterstitialAd);
            reportAdCallShow();
            reportAdShowForLTV();
            return true;
        }
        return false;
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
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (mInterstitialAd != null) {
                    mInterstitialAd.destroy();
                    clearCachedAdTime(mInterstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mInterstitialAd = new ExchangeInterstitialAd(mContext);
        mInterstitialAd.setBidFloor((float) getPidConfig().getEcpm() / 100f);
        mInterstitialAd.setListener(new ExchangeAdListener() {
            @Override
            public void onAdLoaded() {
                mIsInterstitialLoaded = true;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mInterstitialAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(DspMobLoader.this);
                }
            }

            @Override
            public void onAdShown() {
                Log.i(TAG, "onAdImpression");
                Log.v(Log.TAG, "");
                reportAdShow();
                reportAdImpForLTV();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdClicked() {
                Log.i(TAG, "onAdClicked");
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                reportAdClick();
                reportAdClickForLTV();
            }

            @Override
            public void onAdClosed() {
                Log.i(TAG, "onAdDismissed");
                mInterstitialAd = null;
                mIsInterstitialLoaded = false;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(ExchangeAdError adError) {
                Log.v(Log.TAG, "reason : " + codeToError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(adError));
                }
                reportAdError(codeToError(adError));
            }
        });
        mInterstitialAd.request();

        reportAdRequest();
    }

    private String codeToError(ExchangeAdError adError) {
        int errorCode = -1;
        if (adError != null) {
            errorCode = adError.getCode();
            if (errorCode == ExchangeAdError.ERROR_CODE_NO_FILL) {
                return "NO_FILL[" + errorCode + "]";
            }
            if (errorCode == ExchangeAdError.ERROR_CODE_INTERNAL_ERROR) {
                return "INTERNAL_ERROR[" + errorCode + "]";
            }
            if (errorCode == ExchangeAdError.ERROR_CODE_INVALID_REQUEST) {
                return "INVALID_REQUEST[" + errorCode + "]";
            }
            if (errorCode == ExchangeAdError.ERROR_CODE_NETWORK_ERROR) {
                return "NETWORK_ERROR[" + errorCode + "]";
            }
            if (errorCode == ExchangeAdError.ERROR_CODE_TIMEOUT) {
                return "NETWORK_ERROR[" + errorCode + "]";
            }
        }
        return "UNKNOWN[" + errorCode + "]";
    }

    protected int toSdkError(ExchangeAdError adError) {
        int code = Constant.AD_ERROR_UNKNOWN;
        if (adError != null) {
            code = adError.getCode();
            if (code == ExchangeAdError.ERROR_CODE_NO_FILL) {
                return Constant.AD_ERROR_NOFILL;
            }
            if (code == ExchangeAdError.ERROR_CODE_INTERNAL_ERROR) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (code == ExchangeAdError.ERROR_CODE_INVALID_REQUEST) {
                return Constant.AD_ERROR_INVALID_REQUEST;
            }
            if (code == ExchangeAdError.ERROR_CODE_NETWORK_ERROR) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (code == ExchangeAdError.ERROR_CODE_TIMEOUT) {
                return Constant.AD_ERROR_TIMEOUT;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mBannerAd != null) {
            mBannerAd.destroy();
            mBannerAd = null;
        }
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }
}