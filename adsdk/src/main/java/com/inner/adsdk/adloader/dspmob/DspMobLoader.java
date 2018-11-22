package com.inner.adsdk.adloader.dspmob;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.dspmob.sdk.AdError;
import com.dspmob.sdk.AdListener;
import com.dspmob.sdk.AdSize;
import com.dspmob.sdk.BannerAd;
import com.dspmob.sdk.DspMob;
import com.dspmob.sdk.InterstitialAd;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

public class DspMobLoader extends AbstractSdkLoader {

    private final String TAG = "DspMobLoader";

    private boolean mIsBannerLoaded;
    private BannerAd mBannerAd;

    private boolean mIsInterstitialLoaded;
    private InterstitialAd mInterstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_DSPMOB;
    }

    private String getAdId(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return pid;
        }
        if (pid.contains("/")) {
            return pid.substring(0, pid.indexOf("/"));
        }
        return "";
    }

    private String getPid(String pid) {
        if (TextUtils.isEmpty(pid)) {
            return pid;
        }
        if (pid.contains("/")) {
            return pid.substring(pid.indexOf("/") + 1);
        }
        return pid;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        if (!TextUtils.isEmpty(adId)) {
            DspMob.init(mContext, adId);
        } else if (!TextUtils.isEmpty(getAppId())) {
            DspMob.init(mContext, getAppId());
        } else {
            initDspMob();
        }
    }

    private void initDspMob() {
        String adId = getAdId(mPidConfig.getPid());
        if (!TextUtils.isEmpty(adId)) {
            DspMob.init(mContext, adId);
        }
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

        mBannerAd = new BannerAd(mContext);
        mBannerAd.setAdId(getPid(mPidConfig.getPid()));
        if (adSize == Constant.BANNER) {
            mBannerAd.setAdSize(AdSize.Banner_320_50);
        } else if (adSize == Constant.MEDIUM_RECTANGLE) {
            mBannerAd.setAdSize(AdSize.Banner_300_250);
        } else {
            mBannerAd.setAdSize(AdSize.Banner_320_50);
        }
        mBannerAd.setBidFloor((float) getPidConfig().getEcpm() / 100f);
        mBannerAd.setListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mBannerAd);
                mIsBannerLoaded = true;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
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
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
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
            public void onAdFailedToLoad(AdError adError) {
                Log.v(Log.TAG, "reason : " + codeToError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(adError), getSdkName(), getAdType(), null);
                }
            }
        });
        mBannerAd.request();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
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
            if (mStat != null) {
                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
            }
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
                if (mInterstitialAd != null) {
                    mInterstitialAd.destroy();
                    clearCachedAdTime(mInterstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdId(getPid(mPidConfig.getPid()));
        mInterstitialAd.setBidFloor((float) getPidConfig().getEcpm() / 100f);
        mInterstitialAd.setListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mIsInterstitialLoaded = true;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mInterstitialAd);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded();
                }
            }

            @Override
            public void onAdShown() {
                Log.i(TAG, "onAdImpression");
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid());
                }
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
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid());
                }
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
            public void onAdFailedToLoad(AdError adError) {
                Log.i(TAG, "Failed loading fullscreen ad! with error: " + adError.toString());
                Log.v(Log.TAG, "reason : " + codeToError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(adError), getSdkName(), getAdType(), null);
                }
            }
        });
        mInterstitialAd.request();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }

    private String codeToError(AdError adError) {
        int errorCode = -1;
        if (adError != null) {
            errorCode = adError.getCode();
            if (errorCode == AdError.NO_FILL.ordinal()) {
                return "NO_FILL[" + errorCode + "]";
            }
            if (errorCode == AdError.INTERNAL_ERROR.ordinal()) {
                return "INTERNAL_ERROR[" + errorCode + "]";
            }
            if (errorCode == AdError.INVALID_REQUEST.ordinal()) {
                return "INVALID_REQUEST[" + errorCode + "]";
            }
            if (errorCode == AdError.NETWORK_ERROR.ordinal()) {
                return "NETWORK_ERROR[" + errorCode + "]";
            }
            if (errorCode == AdError.TIMEOUT.ordinal()) {
                return "NETWORK_ERROR[" + errorCode + "]";
            }
        }
        return "UNKNOWN[" + errorCode + "]";
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