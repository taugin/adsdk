package com.inner.adsdk.adloader.adx;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.inner.adsdk.adloader.base.AbstractAdLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdxLoader extends AbstractAdLoader {

    private static final Map<Integer, AdSize> ADSIZE = new HashMap<Integer, AdSize>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER);
        ADSIZE.put(Constant.FULL_BANNER, AdSize.FULL_BANNER);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.LARGE_BANNER);
        ADSIZE.put(Constant.LEADERBOARD, AdSize.LEADERBOARD);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE);
        ADSIZE.put(Constant.WIDE_SKYSCRAPER, AdSize.WIDE_SKYSCRAPER);
        ADSIZE.put(Constant.SMART_BANNER, AdSize.SMART_BANNER);
    }

    private AdView bannerView;
    private InterstitialAd interstitialAd;
    private NativeAd nativeAd;
    private Params mParams;

    @Override
    public void setAdId(String adId) {
        if (!TextUtils.isEmpty(adId)) {
            MobileAds.initialize(mContext, adId);
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADX;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
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
            size = AdSize.BANNER;
        }
        final AdView adView = new AdView(mContext);
        adView.setAdUnitId(mPidConfig.getPid());
        adView.setAdSize(size);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + i + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                putCachedAdTime(adView);
                bannerView = adView;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onAdLoaded();
                }
            }

            @Override
            public void onAdClicked() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean isBannerLoaded() {
        return bannerView != null && !isCachedAdExpired(bannerView);
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "adxloader");
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
            Log.e(Log.TAG, "adxloader error : " + e);
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (interstitialAd != null) {
            return interstitialAd.isLoaded() && !isCachedAdExpired(interstitialAd);
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
        interstitialAd = new InterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v(Log.TAG, "reason : " + i + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError();
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdOpened() {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false);
                putCachedAdTime(interstitialAd);
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
                Log.v(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.v(Log.TAG, "");
            }
        });
        interstitialAd.loadAd(new AdRequest.Builder().build());
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
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        if (nativeAd != null) {
            return !isCachedAdExpired(nativeAd);
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
        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();
        NativeAdOptions nativeAdOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        AdLoader adLoader = new AdLoader.Builder(mContext, mPidConfig.getPid())
                .forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                    @Override
                    public void onAppInstallAdLoaded(NativeAppInstallAd ad) {
                        Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                        nativeAd = ad;
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
                }).forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd ad) {
                        Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                        nativeAd = ad;
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
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdLeftApplication() {
                        super.onAdLeftApplication();
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
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }

                    @Override
                    public void onAdImpression() {
                        Log.v(Log.TAG, "");
                        if (getAdListener() != null) {
                            getAdListener().onAdImpression();
                        }
                        if (mStat != null) {
                            mStat.reportAdShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e(Log.TAG, "aderror placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , error : " + errorCode);
                        if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                            updateLastNoFillTime();
                        }
                        setLoading(false);
                        if (getAdListener() != null) {
                            getAdListener().onAdFailed();
                        }
                    }
                }).withNativeAdOptions(nativeAdOptions).build();
        adLoader.loadAd(new AdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup) {
        AdxBindNativeView adxBindNativeView = new AdxBindNativeView();
        clearCachedAdTime(nativeAd);
        adxBindNativeView.bindNative(mParams, viewGroup, nativeAd, mPidConfig);
        nativeAd = null;
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
    public boolean isInterstitialType() {
        if (mPidConfig != null) {
            return mPidConfig.isInterstitialType();
        }
        return super.isInterstitialType();
    }

    @Override
    public void resume() {
        if (bannerView != null) {
            bannerView.resume();
        }
    }

    @Override
    public void pause() {
        if (bannerView != null) {
            bannerView.pause();
        }
    }

    @Override
    public void destroy() {
        if (bannerView != null) {
            bannerView.destroy();
        }
        if (nativeAd != null) {
            nativeAd = null;
        }
    }
}
