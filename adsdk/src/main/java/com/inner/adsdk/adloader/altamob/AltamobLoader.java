package com.inner.adsdk.adloader.altamob;

import android.text.TextUtils;

import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.mobi.sdk.AD;
import com.mobi.sdk.ADError;
import com.mobi.sdk.ADSDK;
import com.mobi.sdk.InterstitialAd;
import com.mobi.sdk.InterstitialAdListener;

public class AltamobLoader extends AbstractSdkLoader {

    private final String TAG = "AltamobLoader";

    private boolean mIsInterstitialLoaded;
    private InterstitialAd mInterstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ALTAMOB;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);

        String appkey = getMetaData("appKey");
        if (TextUtils.isEmpty(appkey)) {
            Log.e(Log.TAG, ">>>>>>>>>>>>>>>>>>miss [<meta-data android:name=\"appKey\" android:value=\"\"/>]<<<<<<<<<<<<<<<<<<");
        }
        ADSDK.getInstance(mContext).init();
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mIsInterstitialLoaded && mInterstitialAd != null && !isCachedAdExpired(mInterstitialAd);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (isInterstitialLoaded()) {
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

        mInterstitialAd = new InterstitialAd(mContext, mPidConfig.getPid());
        mInterstitialAd.setInterstitialAdListener(new InterstitialAdListener() {
            @Override
            public void onLoaded(AD ad, String s) {
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
            public void onError(ADError adError, String s) {
                Log.i(TAG, "Failed loading fullscreen ad! with error: " + adError.errorCode);
                Log.v(Log.TAG, "reason : " + codeToError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(adError), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onClick() {
                Log.i(TAG, "onAdClicked");
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
            public void onShowed() {
                Log.i(TAG, "onAdImpression");
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
            public void onDismissed() {
                Log.i(TAG, "onAdDismissed");
                mInterstitialAd = null;
                mIsInterstitialLoaded = false;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        });
        mInterstitialAd.loadAd();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }

    public static String codeToError(ADError adError) {
        if (adError.errorCode == ADError.NETWORK_ERROR.errorCode) {
            return "ERROR_CODE_NETWORK_ERROR[" + adError.errorCode + "]";
        } else if (adError.errorCode == ADError.NO_FILL.errorCode
                || adError.errorCode == ADError.NO_AD_ERROR.errorCode) {
            return "ERROR_CODE_NO_FILL[" + adError.errorCode + "]";
        } else if (adError.errorCode == ADError.LOAD_TOO_FREQUENTLY.errorCode
                || adError.errorCode == ADError.MISSING_PROPERTIES.errorCode
                || adError.errorCode == ADError.NO_ANDROID_ID_ERROR.errorCode
                || adError.errorCode == ADError.APP_KEY_ERROR.errorCode
                || adError.errorCode == ADError.CONFIG_ERROR.errorCode
                || adError.errorCode == ADError.AD_FEQ_ERROR.errorCode) {
            return "ERROR_CODE_INVALID_REQUEST[" + adError.errorCode + "]";
        } else if (adError.errorCode == ADError.SERVER_ERROR.errorCode
                || adError.errorCode == ADError.INTERNAL_ERROR.errorCode
                || adError.errorCode == ADError.AD_IMPL_ERROR.errorCode
                || adError.errorCode == ADError.AD_CLICK_ERROR.errorCode) {
            return "ERROR_CODE_INTERNAL_ERROR[" + adError.errorCode + "]";
        }
        return "UNKNOWN[" + adError.errorCode + "]";
    }
}
