package com.inner.adsdk.adloader.inmobi;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.inmobi.ads.listeners.NativeAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2018/6/28.
 */

public class InmobiLoader extends AbstractSdkLoader {

    private InMobiBanner loadingView;
    private InMobiBanner mInMobiBanner;
    private InMobiBanner gInMobiBanner;
    private InMobiInterstitial mInMobiInterstitial;
    private InMobiInterstitial mInMobiRewardVideo;
    private InMobiNative mInMobiNative;
    private InMobiNative mLoadingNative;
    private InMobiNative gInMobiNative;
    private Params mParams;

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        String appId = null;
        if (!TextUtils.isEmpty(adId)) {
            appId = adId;
        } else if (!TextUtils.isEmpty(getAppId())) {
            appId = getAppId();
        } else {
            appId = "3a7c9443ab95449084eb270ee0154fcd";
        }
        JSONObject consentObject = new JSONObject();
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "1");
        } catch (JSONException e) {
        }
        InMobiSdk.init(getContext(), appId, consentObject);
        //InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_INMOBI;
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
                if (mInMobiBanner != null) {
                    clearCachedAdTime(mInMobiBanner);
                }
            }
        }
        long lPid = 0;
        try {
            lPid = Long.valueOf(mPidConfig.getPid());
        } catch (NumberFormatException e) {
            Log.v(Log.TAG, "pid convert error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        setLoading(true, STATE_REQUEST);
        setBannerSize(adSize);

        loadingView = new InMobiBanner(getContext(), lPid);
        if (adSize == Constant.BANNER) {
            loadingView.setBannerSize(320, 50);
        } else if (adSize == Constant.MEDIUM_RECTANGLE) {
            loadingView.setBannerSize(300, 250);
        }
        loadingView.setListener(new BannerAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                super.onAdLoadSucceeded(inMobiBanner);
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(inMobiBanner);
                mInMobiBanner = loadingView;
                reportAdLoaded();
                notifyAdLoaded(false);
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
                String error = inMobiAdRequestStatus == null ? "" : inMobiAdRequestStatus.getMessage();
                Log.v(Log.TAG, "reason : " + error + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(inMobiAdRequestStatus));
                }
                reportAdError(error);
            }

            @Override
            public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                super.onAdClicked(inMobiBanner, map);
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (isDestroyAfterClick()) {
                    mInMobiBanner = null;
                }
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                super.onAdDisplayed(inMobiBanner);
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                super.onAdDismissed(inMobiBanner);
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
                reportAdClose();
            }

            @Override
            public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                super.onUserLeftApplication(inMobiBanner);
            }
        });
        loadingView.load();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mInMobiBanner != null && !isCachedAdExpired(mInMobiBanner);
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "showBanner");
        try {
            clearCachedAdTime(mInMobiBanner);
            viewGroup.removeAllViews();
            ViewParent viewParent = mInMobiBanner.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mInMobiBanner);
            }
            viewGroup.addView(mInMobiBanner);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            gInMobiBanner = mInMobiBanner;
            if (!isDestroyAfterClick()) {
                mInMobiBanner = null;
            }
            reportAdShowing();
        } catch (Exception e) {
            Log.e(Log.TAG, "showBanner error : " + e);
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
                if (mInMobiInterstitial != null) {
                    mInMobiInterstitial.setListener(null);
                    clearCachedAdTime(mInMobiInterstitial);
                }
            }
        }

        long lPid = -1;
        try {
            lPid = Long.valueOf(mPidConfig.getPid());
        } catch (NumberFormatException e) {
        }
        if (lPid < 0) {
            Log.v(Log.TAG, "pid convert error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        setLoading(true, STATE_REQUEST);

        mInMobiInterstitial = new InMobiInterstitial(getContext(), lPid, new InterstitialAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                super.onAdLoadSucceeded(inMobiInterstitial);
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(InmobiLoader.this.mInMobiInterstitial);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(InmobiLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiInterstitial, inMobiAdRequestStatus);
                String error = inMobiAdRequestStatus == null ? "" : inMobiAdRequestStatus.getMessage();
                Log.v(Log.TAG, "reason : " + error + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(inMobiAdRequestStatus));
                }
                reportAdError(error);
            }

            @Override
            public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onAdClicked(inMobiInterstitial, map);
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                reportAdClick();
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                super.onAdDisplayed(inMobiInterstitial);
                Log.v(Log.TAG, "");
                reportAdShowing();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                super.onAdDismissed(inMobiInterstitial);
                Log.v(Log.TAG, "");
                InmobiLoader.this.mInMobiInterstitial = null;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
                reportAdClose();
            }

            @Override
            public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                super.onUserLeftApplication(inMobiInterstitial);
            }
        });
        mInMobiInterstitial.load();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (mInMobiInterstitial != null) {
            loaded = mInMobiInterstitial.isReady() && !isCachedAdExpired(mInMobiInterstitial);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        if (mInMobiInterstitial != null && mInMobiInterstitial.isReady()) {
            mInMobiInterstitial.show();
            clearCachedAdTime(mInMobiInterstitial);
            mInMobiInterstitial = null;
            reportAdCallShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (mInMobiRewardVideo != null) {
            loaded = mInMobiRewardVideo.isReady() && !isCachedAdExpired(mInMobiRewardVideo);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isRewaredVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onRewardedVideoAdLoaded(this);
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
            }
        }

        long lPid = -1;
        try {
            lPid = Long.valueOf(mPidConfig.getPid());
        } catch (NumberFormatException e) {
        }
        if (lPid < 0) {
            Log.v(Log.TAG, "pid convert error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        setLoading(true, STATE_REQUEST);

        mInMobiRewardVideo = new InMobiInterstitial(getContext(), lPid, new InterstitialAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(InmobiLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                String error = inMobiAdRequestStatus == null ? "" : inMobiAdRequestStatus.getMessage();
                Log.v(Log.TAG, "reason : " + error + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(inMobiAdRequestStatus));
                }
                reportAdError(error);
            }

            @Override
            public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
                reportAdClick();
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
                reportAdShowing();
            }

            @Override
            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
                reportAdClose();
            }

            @Override
            public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null && map != null && map.size() > 0) {
                    try {
                        AdReward adReward = new AdReward();
                        for (Object key : map.keySet()) {
                            Object value = map.get(key);
                            adReward.setType(String.valueOf(key));
                            adReward.setAmount(String.valueOf(value));
                        }
                        Log.v(Log.TAG, "Ad Reward: " + adReward.toString());
                        getAdListener().onRewarded(adReward);
                    } catch (Exception e) {
                    }
                }
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }
                reportAdReward();
            }
        });

        mInMobiRewardVideo.load();

        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showRewardedVideo() {
        if (mInMobiRewardVideo != null && mInMobiRewardVideo.isReady()) {
            mInMobiRewardVideo.show();
            clearCachedAdTime(mInMobiRewardVideo);
            mInMobiRewardVideo = null;
            reportAdCallShow();
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = super.isNativeLoaded();
        if (mInMobiNative != null) {
            loaded = !isCachedAdExpired(mInMobiNative);
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
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (mLoadingNative != null) {
                    mLoadingNative.setListener(null);
                    mLoadingNative.destroy();
                    clearCachedAdTime(mInMobiNative);
                }
            }
        }

        long lPid = -1;
        try {
            lPid = Long.valueOf(mPidConfig.getPid());
        } catch (NumberFormatException e) {
        }
        if (lPid < 0) {
            Log.v(Log.TAG, "pid convert error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }

        setLoading(true, STATE_REQUEST);

        mLoadingNative = new InMobiNative(mContext, lPid, new NativeAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiNative nativeAd) {
                super.onAdLoadSucceeded(nativeAd);
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                mInMobiNative = nativeAd;
                putCachedAdTime(mInMobiNative);
                notifyAdLoaded(false);
                reportAdLoaded();
            }

            @Override
            public void onAdLoadFailed(InMobiNative nativeAd, InMobiAdRequestStatus status) {
                super.onAdLoadFailed(nativeAd, status);
                Log.v(Log.TAG, "reason : " + status.getMessage() + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(toSdkError(status));
                }
                if (status != null) {
                    reportAdError(status.getMessage());
                }
            }

            @Override
            public void onAdImpressed(InMobiNative nativeAd) {
                super.onAdImpressed(nativeAd);
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdImpression();
                }
                reportAdShowing();
            }

            @Override
            public void onAdClicked(InMobiNative nativeAd) {
                super.onAdClicked(nativeAd);
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                reportAdClick();
                if (isDestroyAfterClick()) {
                    mInMobiNative = null;
                }
            }

            @Override
            public void onAdFullScreenDismissed(InMobiNative inMobiNative) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
                reportAdClose();
            }
        });
        mLoadingNative.load();
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        Log.v(Log.TAG, "showNative - inmobi");
        if (params != null) {
            mParams = params;
        }
        InmobiBindNativeView inmobiBindNativeView = new InmobiBindNativeView();
        clearCachedAdTime(mInMobiNative);
        inmobiBindNativeView.bindNative(mParams, viewGroup, mInMobiNative, mPidConfig);
        gInMobiNative = mInMobiNative;
        if (!isDestroyAfterClick()) {
            mInMobiNative = null;
        }
    }

    @Override
    public void destroy() {
        if (gInMobiNative != null) {
            gInMobiNative.destroy();
            gInMobiNative = null;
        }
        if (gInMobiBanner != null) {
            gInMobiBanner = null;
        }
    }

    protected int toSdkError(InMobiAdRequestStatus status) {
        InMobiAdRequestStatus.StatusCode code = InMobiAdRequestStatus.StatusCode.NO_FILL;
        if (status != null) {
            code = status.getStatusCode();
            if (code == InMobiAdRequestStatus.StatusCode.INTERNAL_ERROR) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (code == InMobiAdRequestStatus.StatusCode.INVALID_RESPONSE_IN_LOAD) {
                return Constant.AD_ERROR_INVALID_REQUEST;
            }
            if (code == InMobiAdRequestStatus.StatusCode.NETWORK_UNREACHABLE) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (code == InMobiAdRequestStatus.StatusCode.NO_FILL) {
                return Constant.AD_ERROR_NOFILL;
            }
            if (code == InMobiAdRequestStatus.StatusCode.REQUEST_TIMED_OUT) {
                return Constant.AD_ERROR_TIMEOUT;
            }
            if (code == InMobiAdRequestStatus.StatusCode.SERVER_ERROR) {
                return Constant.AD_ERROR_SERVER;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }
}
