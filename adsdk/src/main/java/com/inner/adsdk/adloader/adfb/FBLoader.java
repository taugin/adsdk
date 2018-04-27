package com.inner.adsdk.adloader.adfb;

import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.inner.adsdk.adloader.base.AbstractAdLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public class FBLoader extends AbstractAdLoader {

    private static final HashMap<Integer, AdSize> ADSIZE = new HashMap<Integer, AdSize>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER_HEIGHT_50);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.BANNER_HEIGHT_90);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.RECTANGLE_HEIGHT_250);
    }

    private InterstitialAd fbInterstitial;
    private NativeAd nativeAd;
    private AdView bannerView;
    private Params mParams;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            return;
        }
        if (!matchNoFillTime()) {
            Log.v(Log.TAG, "no fill interval not match");
            return;
        }
        if (isBannerLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onAdLoaded();
            }
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            return;
        }
        setLoading(true);
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER_HEIGHT_50;
        }
        final AdView adView = new AdView(mContext, mPidConfig.getPid(), size);
        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                bannerView = adView;
                putCachedAdTime(adView);
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onAdLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        });
        adView.loadAd();
    }

    @Override
    public boolean isBannerLoaded() {
        return bannerView != null && !isCachedAdExpired(bannerView);
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "");
        try {
            clearCachedAdTime(bannerView);
            viewGroup.removeAllViews();
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            bannerView = null;
            if (mStat != null) {
                mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public boolean isBannerType() {
        if (mPidConfig != null) {
            return mPidConfig.isBannerType();
        }
        return super.isBannerType();
    }

    @Override
    public boolean isNativeType() {
        if (mPidConfig != null) {
            return mPidConfig.isNativeType();
        }
        return super.isNativeType();
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (fbInterstitial != null) {
            return fbInterstitial.isAdLoaded() && !isCachedAdExpired(fbInterstitial);
        }
        return super.isInterstitialLoaded();
    }

    @Override
    public void loadInterstitial() {
        if (!checkPidConfig()) {
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
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            return;
        }
        setLoading(true);
        fbInterstitial = new InterstitialAd(mContext, mPidConfig.getPid());
        fbInterstitial.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.v(Log.TAG, "");
                fbInterstitial = null;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                putCachedAdTime(fbInterstitial);
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded();
                }
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
            }
        });
        fbInterstitial.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (fbInterstitial != null && fbInterstitial.isAdLoaded()) {
            fbInterstitial.show();
            clearCachedAdTime(fbInterstitial);
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        if (nativeAd != null) {
            return nativeAd.isAdLoaded() && !isCachedAdExpired(nativeAd);
        }
        return super.isNativeLoaded();
    }

    @Override
    public void loadNative(Params params) {
        mParams = params;

        if (!checkPidConfig()) {
            return;
        }
        if (isNativeLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onAdLoaded();
            }
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            return;
        }
        setLoading(true);
        nativeAd = new NativeAd(mContext, mPidConfig.getPid());
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage());
                    if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
                        updateLastNoFillTime();
                    }
                }
                setLoading(false);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                putCachedAdTime(nativeAd);
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onAdLoaded();
                }
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
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
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdImpression();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }
        });
        nativeAd.loadAd(EnumSet.of(NativeAd.MediaCacheFlag.IMAGE));
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup) {
        FBBindNativeView fbBindNativeView = new FBBindNativeView();
        clearCachedAdTime(nativeAd);
        fbBindNativeView.bindNative(mParams, viewGroup, nativeAd, mPidConfig);
        nativeAd = null;
    }

    @Override
    public void destroy() {
        if (fbInterstitial != null) {
            fbInterstitial.destroy();
        }
        if (bannerView != null) {
            bannerView.destroy();
        }
    }
}