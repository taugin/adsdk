package com.inner.adaggs.adloader.admob;

import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.inner.adaggs.listener.AbstractAdLoader;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdmobLoader extends AbstractAdLoader {

    protected static final Map<Integer, AdSize> ADSIZE = new HashMap<>();

    static {
        ADSIZE.put(Constant.BANNER, AdSize.BANNER);
        ADSIZE.put(Constant.FULL_BANNER, AdSize.FULL_BANNER);
        ADSIZE.put(Constant.LARGE_BANNER, AdSize.LARGE_BANNER);
        ADSIZE.put(Constant.LEADERBOARD, AdSize.LEADERBOARD);
        ADSIZE.put(Constant.MEDIUM_RECTANGLE, AdSize.MEDIUM_RECTANGLE);
        ADSIZE.put(Constant.WIDE_SKYSCRAPER, AdSize.WIDE_SKYSCRAPER);
        ADSIZE.put(Constant.SMART_BANNER, AdSize.SMART_BANNER);
    }

    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADMOB;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            return;
        }
        adView = new AdView(mContext);
        adView.setAdUnitId(mPidConfig.getPid());
        adView.setAdSize(ADSIZE.get(adSize));
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                if (mOnAdListener != null) {
                    mOnAdListener.onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                if (mOnAdListener != null) {
                    mOnAdListener.onAdFailed();
                }
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
                if (mStat != null) {
                    mStat.reportAdmobBannerShow(mContext, getPidName(), null);
                }
            }

            @Override
            public void onAdLoaded() {
                if (mStat != null) {
                    mStat.reportAdmobBannerLoaded(mContext, getPidName(), null);
                }
                if (mOnAdListener != null) {
                    mOnAdListener.onAdLoaded();
                }
            }

            @Override
            public void onAdClicked() {
                if (mStat != null) {
                    mStat.reportAdmobBannerClick(mContext, getPidName(), null);
                }
                if (mOnAdListener != null) {
                    mOnAdListener.onAdClick();
                }
            }

            @Override
            public void onAdImpression() {
                if (mOnAdListener != null) {
                    mOnAdListener.onAdShow();
                }
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdmobBannerRequest(mContext, getPidName(), null);
        }
    }

    @Override
    public View getAdView() {
        return adView;
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (interstitialAd != null) {
            return interstitialAd.isLoaded();
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
        interstitialAd = new InterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                Log.d(Log.TAG, "");
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.e(Log.TAG, "error : " + i);
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialError();
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdmobInterstitialClick(mContext, getPidName(), null);
                }
            }

            @Override
            public void onAdOpened() {
                Log.d(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdmobInterstitialShow(mContext, getPidName(), null);
                }
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialShow();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.d(Log.TAG, "mOnInterstitialListener : " + mOnInterstitialListener);
                if (mStat != null) {
                    mStat.reportAdmobInterstitialLoaded(mContext, getPidName(), null);
                }
                if (mOnInterstitialListener != null) {
                    mOnInterstitialListener.onInterstitialLoaded();
                }
            }

            @Override
            public void onAdClicked() {
                Log.d(Log.TAG, "");
            }

            @Override
            public void onAdImpression() {
                Log.d(Log.TAG, "");
            }
        });
        interstitialAd.loadAd(new AdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdmobInterstitialRequest(mContext, getPidName(), null);
        }
        Log.d(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            return true;
        }
        return false;
    }

    @Override
    public boolean isBannerType() {
        if (mPidConfig != null) {
            mPidConfig.isBannerType();
        }
        return super.isBannerType();
    }

    @Override
    public boolean isNativeType() {
        if (mPidConfig != null) {
            mPidConfig.isNativeType();
        }
        return super.isNativeType();
    }

    @Override
    public boolean isInterstitialType() {
        if (mPidConfig != null) {
            mPidConfig.isInterstitialType();
        }
        return super.isInterstitialType();
    }
}
