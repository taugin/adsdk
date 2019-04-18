package com.inner.adsdk.adloader.wemob;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.wemob.ads.AdError;
import com.wemob.ads.AdListener;
import com.wemob.ads.BannerAdView;
import com.wemob.ads.InterstitialAd;
import com.wemob.ads.NativeAd;
import com.wemob.ads.Sdk;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018/6/8.
 */

public class WemobLoader extends AbstractSdkLoader {

    private InterstitialAd interstitialAd;
    private BannerAdView bannerView;
    private BannerAdView loadingView;
    private NativeAd loadingNativeAd;
    private NativeAd nativeAd;
    private Params mParams;

    private BannerAdView gBannerView;
    private NativeAd gNativeAd;

    @Override
    public void setAdId(String adId) {
        if (!TextUtils.isEmpty(adId)) {
            try {
                JSONObject jobj = new JSONObject(adId);
                String appKey = jobj.getString(Constant.APPKEY);
                String channel = jobj.getString(Constant.CHANNEL);
                Sdk.instance().setAppKey(appKey);
                Sdk.instance().setChannelId(channel);
                Sdk.instance().init(mContext);
                Log.d(Log.TAG, "appkey : " + appKey + " , channel : " + channel);
            } catch (Exception e) {
                Log.d(Log.TAG, "error : " + e);
            }
        } else if (!TextUtils.isEmpty(getAppId()) && !TextUtils.isEmpty(getExtId())) {
            try {
                Sdk.instance().setAppKey(getAppId());
                Sdk.instance().setChannelId(getExtId());
                Sdk.instance().init(mContext);
                Log.d(Log.TAG, "appkey : " + getAppId() + " , channel : " + getExtId());
            } catch (Exception e) {
            }
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_WEMOB;
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
                if (loadingView != null) {
                    loadingView.setAdListener(null);
                    loadingView.destroy();
                    clearCachedAdTime(loadingView);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);
        loadingView = new BannerAdView(mContext, mPidConfig.getPid());
        loadingView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(AdError adError) {
                Log.v(Log.TAG, "reason : " + getError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, getError(adError), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdLoaded(int i) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(loadingView);
                bannerView = loadingView;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                notifyAdLoaded(false);
            }

            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    bannerView = null;
                }
            }

            @Override
            public void onAdShown() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        });
        loadingView.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = bannerView != null && !isCachedAdExpired(bannerView);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "wemobloader");
        try {
            clearCachedAdTime(bannerView);
            viewGroup.removeAllViews();
            ViewParent viewParent = bannerView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(bannerView);
            }
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            gBannerView = bannerView;
            if (!isDestroyAfterClick()) {
                bannerView = null;
            }
            if (mStat != null) {
                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdImpForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "wemobloader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (interstitialAd != null) {
            loaded = interstitialAd.isLoaded() && !isCachedAdExpired(interstitialAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
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
                if (interstitialAd != null) {
                    interstitialAd.setAdListener(null);
                    clearCachedAdTime(interstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        interstitialAd = new InterstitialAd(mContext, mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(AdError adError) {
                Log.v(Log.TAG, "reason : " + getError(adError) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, getError(adError), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdLoaded(int i) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(WemobLoader.this);
                }
            }

            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (interstitialAd != null) {
                    interstitialAd.destroy();
                    interstitialAd = null;
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
                }
            }

            @Override
            public void onAdShown() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }
        });
        interstitialAd.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            clearCachedAdTime(interstitialAd);
            interstitialAd = null;
            if (mStat != null) {
                mStat.reportAdCallShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
            if (mStat != null) {
                mStat.reportAdShowForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
            }
            return true;
        }
        return false;
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
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (loadingNativeAd != null) {
                    loadingNativeAd.setAdListener(null);
                    loadingNativeAd.destroy();
                    clearCachedAdTime(loadingNativeAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);
        loadingNativeAd = new NativeAd(mContext, mPidConfig.getPid());
        loadingNativeAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + getError(adError) + " , pid : " + getPid());
                    if (adError.errorCode == AdError.ERROR_CODE_NO_FILL) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, getError(adError), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdLoaded(int i) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                nativeAd = loadingNativeAd;
                putCachedAdTime(nativeAd);
                notifyAdLoaded(false);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
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
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdClickForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
                }
                if (isDestroyAfterClick()) {
                    nativeAd = null;
                }
            }

            @Override
            public void onAdShown() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdImpression();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (mStat != null) {
                    mStat.reportAdImpForLTV(mContext, getSdkName(), getPid(), String.valueOf(getEcpm()));
                }
            }
        });
        loadingNativeAd.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
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
    public void showNative(ViewGroup viewGroup, Params params) {
        Log.v(Log.TAG, "showNative - wemob");
        if (params != null) {
            mParams = params;
        }
        WemobBindNativeView wemobBindNativeView = new WemobBindNativeView();
        clearCachedAdTime(nativeAd);
        wemobBindNativeView.bindWemobNative(mParams, viewGroup, nativeAd, mPidConfig);
        gNativeAd = nativeAd;
        if (!isDestroyAfterClick()) {
            nativeAd = null;
        }
    }

    @Override
    public void destroy() {
        if (gBannerView != null) {
            gBannerView.destroy();
            gBannerView = null;
        }
        if (gNativeAd != null) {
            gNativeAd.destroy();
            gNativeAd = null;
        }
    }

    private String getError(AdError adError) {
        int errorCode = 0;
        if (adError != null) {
            errorCode = adError.errorCode;
            if (errorCode == AdError.ERROR_CODE_NO_FILL) {
                return "ERROR_CODE_NO_FILL[" + errorCode + "]";
            }
            if (errorCode == AdError.ERROR_CODE_TIMEOUT) {
                return "ERROR_CODE_TIMEOUT[" + errorCode + "]";
            }
            if (errorCode == AdError.ERROR_CODE_INTERNAL_ERROR) {
                return "ERROR_CODE_INTERNAL_ERROR[" + errorCode + "]";
            }
            if (errorCode == AdError.ERROR_CODE_INVALID_PID) {
                return "ERROR_CODE_INVALID_PID[" + errorCode + "]";
            }
            if (errorCode == AdError.ERROR_CODE_NETWORK_ERROR) {
                return "ERROR_CODE_NETWORK_ERROR[" + errorCode + "]";
            }
            if (errorCode == AdError.ERROR_CODE_INVALID_REQUEST) {
                return "ERROR_CODE_INVALID_REQUEST[" + errorCode + "]";
            }
        }
        return "UNKNOWN[" + errorCode + "]";
    }

    protected int toSdkError(AdError adError) {
        int errorCode = 0;
        if (adError != null) {
            errorCode = adError.errorCode;
            if (errorCode == AdError.ERROR_CODE_NO_FILL) {
                return Constant.AD_ERROR_NOFILL;
            }
            if (errorCode == AdError.ERROR_CODE_TIMEOUT) {
                return Constant.AD_ERROR_TIMEOUT;
            }
            if (errorCode == AdError.ERROR_CODE_INTERNAL_ERROR) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (errorCode == AdError.ERROR_CODE_INVALID_PID) {
                return Constant.AD_ERROR_INVALID_PID;
            }
            if (errorCode == AdError.ERROR_CODE_NETWORK_ERROR) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (errorCode == AdError.ERROR_CODE_INVALID_REQUEST) {
                return Constant.AD_ERROR_INVALID_REQUEST;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }
}
