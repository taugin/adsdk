package com.inner.adsdk.adloader.mobvista;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.adywind.componentad.interstitial.api.InterstitialAd;
import com.adywind.componentad.interstitial.api.InterstitialAdListener;
import com.adywind.core.api.Ad;
import com.adywind.core.api.AdError;
import com.adywind.core.api.AdListener;
import com.adywind.core.api.SDK;
import com.adywind.nativeads.api.NativeAds;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MobvistaLoader extends AbstractSdkLoader {

    private final String TAG = "MobvistaLoader";

    private InterstitialAd mInterstitialAd;
    private boolean mIsInterstitialLoaded;

    private NativeAds mNativeAds;
    private boolean mIsNativeAdLoaded;
    private Params mParams;
    private Ad mAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_MOBVISTA;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);

        if (!TextUtils.isEmpty(adId)) {
            try {
                JSONObject object = new JSONObject(adId);
                String appId = object.optString("appid");
                String appKey = object.optString("appkey");

                SDK.setUploadDataLevel(mContext, SDK.UPLOAD_DATA_ALL);
                SDK.init(mContext, appId, appKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        if (mInterstitialAd != null && mIsInterstitialLoaded) {
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
                    mInterstitialAd.destory();
                    clearCachedAdTime(mInterstitialAd);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mInterstitialAd = new InterstitialAd(mContext, mPidConfig.getPid());
        mInterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onLoadError(int i) {
                Log.i(TAG, "Failed loading fullscreen ad! with error: " + interstitialCodeToError(i));
                Log.v(Log.TAG, "reason : " + interstitialCodeToError(i)
                        + " , placename : " + getAdPlaceName()
                        + " , sdk : " + getSdkName()
                        + " , type : " + getAdType()
                        + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, interstitialCodeToError(i), getSdkName(), getAdType(), null);
                }
            }

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
            public void onAdShowed() {
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
            public void onAdClose() {
                Log.i(TAG, "onAdDismissed");
                mInterstitialAd = null;
                mIsInterstitialLoaded = false;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        });
        mInterstitialAd.fill();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (mAd != null) {
            loaded = !isCachedAdExpired(mAd);
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
                if (mNativeAds != null) {
                    mNativeAds.release();
                    if (mAd != null) {
                        clearCachedAdTime(mAd);
                    }
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        mNativeAds = new NativeAds(mContext, mPidConfig.getPid(), 1);
        mNativeAds.setListener(new AdListener() {
            @Override
            public void onLoadError(AdError adError) {
                Log.v(Log.TAG, "reason : " + adError.getMessage()
                        + " , placename : " + getAdPlaceName()
                        + " , sdk : " + getSdkName()
                        + " , type : " + getAdType());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
                }
                if (mStat != null) {
                    mStat.reportAdError(mContext, adError.getMessage(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdfilled() {
            }

            @Override
            public void onAdLoaded(List<Ad> list) {
                if (list != null && !list.isEmpty()) {
                    Ad ad = list.get(0);
                    if (ad != null) {
                        Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName()
                                + " , sdk : " + getSdkName()
                                + " , type : " + getAdType());
                        mAd = ad;
                        setLoading(false, STATE_SUCCESS);
                        putCachedAdTime(mAd);
                        notifyAdLoaded(false);
                        if (mStat != null) {
                            mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                        }
                        return;
                    }
                }

                AdError adError = new AdError();
                adError.setCode(AdError.ERROR_CODE_APPINSTALLED);
                adError.setMessage(AdError.ERROR_MSG_APPINSTALLED);
                onLoadError(adError);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
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
            public void onAdClickStart(Ad ad) {
            }

            @Override
            public void onAdClickEnd(Ad ad) {
            }
        });
        mNativeAds.loadAd();

        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        Log.v(Log.TAG, "showNative - admob");
        if (params != null) {
            mParams = params;
        }
        MobvistaBindNativeView nativeView = new MobvistaBindNativeView();
        clearCachedAdTime(mAd);
        nativeView.bindNative(mParams, viewGroup, mNativeAds, mAd, mPidConfig);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mInterstitialAd != null) {
            mInterstitialAd.destory();
        }
        if (mNativeAds != null) {
            mNativeAds.release();
        }
    }

    private String interstitialCodeToError(int code) {
        switch (code) {
            case 1:
                return "Network Error";
            case 2:
                return "Server Error";
            case 3:
                return "Sdk Init Error";
            case 4:
                return "Activity Is Null or Closing";
            case 5:
                return "Device Data Permission Is Too Low";
            default:
                return "Internal Error";
        }
    }
}
