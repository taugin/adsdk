package com.inner.adaggs.adloader.adfb;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.inner.adaggs.listener.AbstractAdLoader;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;

/**
 * Created by Administrator on 2018/2/9.
 */

public class FBLoader extends AbstractAdLoader {

    private InterstitialAd fbInterstitial;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_FACEBOOK;
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
}
