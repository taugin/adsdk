package com.inner.adaggs.adloader.adfb;

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
import com.inner.adaggs.adloader.base.AbstractAdLoader;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;

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
    private View nativeRootView;
    private int mNativeTemplate;
    private AdView bannerView;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            return;
        }
        if (bannerView != null) {
            Log.d(Log.TAG, "already loaded : " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onAdLoaded();
                clearOtherListener();
            }
            return;
        }
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER_HEIGHT_50;
        }
        final AdView adView = new AdView(mContext, mPidConfig.getPid(), size);
        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.v(Log.TAG, "error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , type : " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "type : " + getAdType());
                bannerView = adView;
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onAdLoaded();
                    clearOtherListener();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getPidName(), getSdkName(), getAdType(), null);
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
        return bannerView != null;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.v(Log.TAG, "");
        try {
            viewGroup.removeAllViews();
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            bannerView = null;
            if (mStat != null) {
                mStat.reportAdShow(mContext, getPidName(), getSdkName(), getAdType(), null);
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
            return fbInterstitial.isAdLoaded();
        }
        return super.isInterstitialLoaded();
    }

    @Override
    public void loadInterstitial() {
        if (!checkPidConfig()) {
            return;
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded();
                clearOtherListener();
            }
            return;
        }
        fbInterstitial = new InterstitialAd(mContext, mPidConfig.getPid());
        fbInterstitial.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getPidName(), getSdkName(), getAdType(), null);
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
                Log.v(Log.TAG, "error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , type : " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "type : " + getAdType());
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded();
                    clearOtherListener();
                }
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
            }
        });
        fbInterstitial.loadAd();
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getPidName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (fbInterstitial != null && fbInterstitial.isAdLoaded()) {
            fbInterstitial.show();
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        if (nativeAd != null) {
            return nativeAd.isAdLoaded();
        }
        return super.isNativeLoaded();
    }

    @Override
    public void loadNative(View rootView, int templateId) {
        nativeRootView = rootView;
        mNativeTemplate = templateId;

        if (!checkPidConfig()) {
            return;
        }
        if (isNativeLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onAdLoaded();
                clearOtherListener();
            }
            return;
        }
        nativeAd = new NativeAd(mContext, mPidConfig.getPid());
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "error : " + adError.getErrorCode() + " , msg : " + adError.getErrorMessage() + " , type : " + getAdType());
                }
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.v(Log.TAG, "type : " + getAdType());
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onAdLoaded();
                    clearOtherListener();
                }
                if (mStat != null) {
                    mStat.reportAdLoaded(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
                if (mStat != null) {
                    mStat.reportAdClick(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdImpression();
                }
                if (mStat != null) {
                    mStat.reportAdShow(mContext, getPidName(), getSdkName(), getAdType(), null);
                }
            }
        });
        nativeAd.loadAd(EnumSet.of(NativeAd.MediaCacheFlag.IMAGE));
        if (mStat != null) {
            mStat.reportAdRequest(mContext, getPidName(), getSdkName(), getAdType(), null);
        }
        Log.v(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup) {
        FBBindNativeView fbBindNativeView = new FBBindNativeView();
        if (nativeRootView != null) {
            // 使用用户传递的view
            fbBindNativeView.bindNative(nativeRootView, nativeAd, mPidConfig);
        } else {
            // 使用模板
            fbBindNativeView.bindNativeWithTemplate(viewGroup, mNativeTemplate, nativeAd, mPidConfig);
        }
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
