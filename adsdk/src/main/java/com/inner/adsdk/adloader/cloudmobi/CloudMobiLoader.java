package com.inner.adsdk.adloader.cloudmobi;

import android.text.TextUtils;

import com.cloudtech.ads.callback.CTAdEventListener;
import com.cloudtech.ads.core.CTNative;
import com.cloudtech.ads.core.CTService;
import com.cloudtech.ads.vo.AdsNativeVO;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

public class CloudMobiLoader extends AbstractSdkLoader {

    private final String TAG = "CloudMobiLoader";

    private boolean mIsInterstitialLoaded;
    private CTNative mInterstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_CLOUDMOBI;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        if (!TextUtils.isEmpty(adId)) {
            CTService.init(mContext, adId);
        } else if (!TextUtils.isEmpty(getAppId())) {
            CTService.init(mContext, getAppId());
        } else if (!TextUtils.isEmpty(getPid())) {
            CTService.init(mContext, getPid());
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mIsInterstitialLoaded
                && mInterstitialAd != null
                && CTService.isInterstitialAvailable(mInterstitialAd)
                && !isCachedAdExpired(mInterstitialAd);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (isInterstitialLoaded()) {
            CTService.showInterstitial(mInterstitialAd);

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
                getAdListener().onInterstitialLoaded(CloudMobiLoader.this);
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
                    clearCachedAdTime(mInterstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        CTService.preloadMRAIDInterstitial(mContext, mPidConfig.getPid(),
                new MyCTAdEventListener(){
                    @Override
                    public void onInterstitialLoadSucceed(CTNative result) {
                    }

                    @Override
                    public void onAdviewGotAdSucceed(CTNative result){
                        mInterstitialAd = result;
                        mIsInterstitialLoaded = true;
                        setLoading(false, STATE_SUCCESS);
                        putCachedAdTime(mInterstitialAd);
                        reportAdLoaded();
                        if (getAdListener() != null) {
                            setLoadedFlag();
                            getAdListener().onInterstitialLoaded(CloudMobiLoader.this);
                        }
                    }

                    @Override
                    public void onAdviewClosed(CTNative result) {
                        Log.i(TAG, "onAdDismissed");
                        mInterstitialAd = null;
                        mIsInterstitialLoaded = false;
                        if (getAdListener() != null) {
                            getAdListener().onInterstitialDismiss();
                        }
                    }

                    @Override
                    public void onAdviewGotAdFail(CTNative result) {
                        Log.i(TAG, "Failed loading fullscreen ad! with error: " + result.getErrorsMsg());
                        Log.v(Log.TAG, "reason : " + result.getErrorsMsg() + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                        setLoading(false, STATE_FAILURE);
                        if (getAdListener() != null) {
                            getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                        }
                        reportAdError(result.getErrorsMsg());
                    }

                    @Override
                    public void onAdviewClicked(CTNative result) {
                        Log.i(TAG, "onAdClicked");
                        if (getAdListener() != null) {
                            getAdListener().onInterstitialClick();
                        }
                        reportAdClick();
                        reportAdClickForLTV();
                    }
                });

        reportAdRequest();
    }

    public class MyCTAdEventListener extends CTAdEventListener {

        @Override
        public void onAdviewGotAdSucceed(CTNative result) {
        }

        @Override
        public void onAdsVoGotAdSucceed(AdsNativeVO result) {
        }

        @Override
        public void onInterstitialLoadSucceed(CTNative result) {
        }

        @Override
        public void onAdviewGotAdFail(CTNative result) {
        }

        @Override
        public void onAdviewIntoLandpage(CTNative result) {
        }

        @Override
        public void onStartLandingPageFail(CTNative result) {
        }

        @Override
        public void onAdviewDismissedLandpage(CTNative result) {
        }

        @Override
        public void onAdviewClicked(CTNative result) {
        }

        @Override
        public void onAdviewClosed(CTNative result) {
        }

        @Override
        public void onAdviewDestroyed(CTNative result) {
        }
    }
}
