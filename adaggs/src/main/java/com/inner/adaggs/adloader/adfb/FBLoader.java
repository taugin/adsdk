package com.inner.adaggs.adloader.adfb;

import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.inner.adaggs.adloader.listener.AbstractAdLoader;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;

import java.util.EnumSet;

/**
 * Created by Administrator on 2018/2/9.
 */

public class FBLoader extends AbstractAdLoader {

    private InterstitialAd fbInterstitial;
    private NativeAd nativeAd;
    private View nativeRootView;
    private int mNativeTemplate;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
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
            Log.e(Log.TAG, "already loaded");
            if (mOnInterstitialListener != null) {
                mOnInterstitialListener.onInterstitialLoaded();
            }
            return;
        }
        fbInterstitial = new InterstitialAd(mContext, mPidConfig.getPid());
        fbInterstitial.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.d(Log.TAG, "");
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialShow();
                }
                if (mStat != null) {
                    mStat.reportAdmobInterstitialShow(mContext, getPidName(), null);
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.d(Log.TAG, "");
                fbInterstitial = null;
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialDismiss();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(Log.TAG, "error : " + adError.getErrorCode() + " , errorMsg : " + adError.getErrorMessage());
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialError();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(Log.TAG, "");
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialLoaded();
                }
                if (mStat != null) {
                    mStat.reportAdmobInterstitialLoaded(mContext, getPidName(), null);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdmobInterstitialClick(mContext, getPidName(), null);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(Log.TAG, "");
            }
        });
        fbInterstitial.loadAd();
        Log.d(Log.TAG, "");
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
        if (isInterstitialLoaded()) {
            Log.e(Log.TAG, "native already loaded");
            if (mOnAdListener != null) {
                mOnAdListener.onAdLoaded();
            }
            return;
        }
        nativeAd = new NativeAd(mContext, mPidConfig.getPid());
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if (adError != null) {
                    Log.e(Log.TAG, "error : " + adError.getErrorCode() + " , errormsg : " + adError.getErrorMessage());
                }
                if (mOnAdListener != null) {
                    mOnAdListener.onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(Log.TAG, "");
                if (mOnAdListener != null) {
                    mOnAdListener.onAdLoaded();
                }
                if (mStat != null) {
                    mStat.reportFBNativeLoaded(mContext, getPidName(), null);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(Log.TAG, "");
                if (mOnAdListener != null) {
                    mOnAdListener.onAdClick();
                }
                if (mStat != null) {
                    mStat.reportFBNativeClick(mContext, getPidName(), null);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(Log.TAG, "");
                if (mOnAdListener != null) {
                    mOnAdListener.onAdImpression();
                }
                if (mStat != null) {
                    mStat.reportFBNativeShow(mContext, getPidName(), null);
                }
            }
        });
        nativeAd.loadAd(EnumSet.of(NativeAd.MediaCacheFlag.IMAGE));
        if (mStat != null) {
            mStat.reportFBNativeRequest(mContext, getPidName(), null);
        }
        Log.d(Log.TAG, "");
    }

    @Override
    public void showNative(ViewGroup viewGroup) {
        FBBindNativeView fbBindNativeView = new FBBindNativeView();
        if (nativeRootView != null) {
            fbBindNativeView.bindNative(nativeRootView, nativeAd, mPidConfig);
        } else {
            fbBindNativeView.bindNativeWithConatiner(viewGroup, mNativeTemplate, nativeAd, mPidConfig);
        }
    }
}
