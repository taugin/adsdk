package com.inner.adsdk.adloader.dap;

import android.text.TextUtils;

import com.duapps.ad.AbsInterstitialListener;
import com.duapps.ad.AdError;
import com.duapps.ad.InterstitialAd;
import com.duapps.ad.base.DuAdNetwork;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

public class DapLoader extends AbstractSdkLoader {

    private final String TAG = "DapLoader";

    private boolean mIsInterstitialLoaded;
    private InterstitialAd mInterstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_DAP;
    }

    // {"native":[{ "pid": xxx }]}
    // {"native":[{ "pid": xxx }, {"pid": xxx}]}
    // {"native":[{ "pid": xxx }, {"pid": xxx}], "list":[{"pid": xxx}, {"pid": xxx}]}
    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);

        String license = getMetaData("app_license");
        if (TextUtils.isEmpty(license)) {
            Log.e(Log.TAG, ">>>>>>>>>>>>>>>>>>miss [<meta-data android:name=\"app_license\" android:value=\"\"/>]<<<<<<<<<<<<<<<<<<");
        }

        try {
            String pidJson = adId;
            if (TextUtils.isEmpty(adId)) {
                DapUtil.addPid(Integer.valueOf(mPidConfig.getPid()));
                pidJson = DapUtil.getAdJson();
                Log.v(Log.TAG, pidJson);
            }
            DuAdNetwork.init(mContext, pidJson);
        } catch(Exception e) {
            Log.e(Log.TAG, ">>>>>>>>>>>>>>>>>>" + e + "<<<<<<<<<<<<<<<<<<");
        }
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

        mInterstitialAd = new InterstitialAd(mContext, Integer.valueOf(mPidConfig.getPid()), InterstitialAd.Type.SCREEN);
        mInterstitialAd.setInterstitialListener(new AbsInterstitialListener() {
            @Override
            public void onAdFail(int i) {
                Log.i(TAG, "Failed loading fullscreen ad! with error: " + i);
                Log.v(Log.TAG, "reason : " + codeToError(i) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, codeToError(i), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdReceive() {
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
            public void onAdClicked() {
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
            public void onAdPresent() {
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
            public void onAdDismissed() {
                Log.i(TAG, "onAdDismissed");
                mInterstitialAd = null;
                mIsInterstitialLoaded = false;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        });
        mInterstitialAd.load();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }


    private String codeToError(int errcode) {
        switch (errcode) {
            case AdError.NETWORK_ERROR_CODE:
                return AdError.NETWORK_ERROR.getErrorMessage();
            case AdError.NO_FILL_ERROR_CODE:
                return AdError.NO_FILL.getErrorMessage();
            case AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE:
            case AdError.NO_CHANNEL_ERROR_CODE:
                return AdError.LOAD_TOO_FREQUENTLY.getErrorMessage();
            case AdError.TIME_OUT_CODE:
                return AdError.TIME_OUT_ERROR.getErrorMessage();
            case AdError.SERVER_ERROR_CODE:
            case AdError.INTERNAL_ERROR_CODE:
            case AdError.UNKNOW_ERROR_CODE:
                return AdError.SERVER_ERROR.getErrorMessage();
            default:
                return AdError.INTERNAL_ERROR.getErrorMessage();
        }
    }
}
