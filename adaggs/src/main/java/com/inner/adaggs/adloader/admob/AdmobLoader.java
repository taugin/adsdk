package com.inner.adaggs.adloader.admob;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.inner.adaggs.adloader.listener.AbstractAdLoader;
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

    private AdView bannerView;
    private InterstitialAd interstitialAd;

    @Override
    public void init(Context context, String adId) {
        super.init(context, adId);
        if (!TextUtils.isEmpty(adId)) {
            MobileAds.initialize(context, adId);
        }
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADMOB;
    }

    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            return;
        }
        if (bannerView != null) {
            Log.e(Log.TAG, "already loaded : " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdLoaded();
                clearAdListener();
            }
            return;
        }
        AdSize size = ADSIZE.get(adSize);
        if (size == null) {
            size = AdSize.BANNER;
        }
        final AdView adView = new AdView(mContext);
        adView.setAdUnitId(mPidConfig.getPid());
        adView.setAdSize(size);
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                Log.d(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(Log.TAG, "reason : " + i);
                if (getAdListener() != null) {
                    getAdListener().onAdFailed();
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(Log.TAG, "");
            }

            @Override
            public void onAdOpened() {
                Log.d(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.d(Log.TAG, "type : " + getAdType());
                bannerView = adView;
                if (mStat != null) {
                    mStat.reportAdmobBannerLoaded(mContext, getPidName(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdLoaded();
                    clearAdListener();
                }
            }

            @Override
            public void onAdClicked() {
                Log.d(Log.TAG, "");
                if (mStat != null) {
                    mStat.reportAdmobBannerClick(mContext, getPidName(), null);
                }
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }

            @Override
            public void onAdImpression() {
                Log.d(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
        if (mStat != null) {
            mStat.reportAdmobBannerRequest(mContext, getPidName(), null);
        }
        Log.d(Log.TAG, "");
    }

    @Override
    public boolean isBannerLoaded() {
        return bannerView != null;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        Log.d(Log.TAG, "");
        try {
            viewGroup.removeAllViews();
            viewGroup.addView(bannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            bannerView = null;
            if (mStat != null) {
                mStat.reportAdmobBannerShow(mContext, getPidName(), null);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
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
            Log.e(Log.TAG, "already loaded : " + getAdType());
            if (getIntListener() != null) {
                getIntListener().onInterstitialLoaded();
                clearIntListener();
            }
            return;
        }
        interstitialAd = new InterstitialAd(mContext);
        interstitialAd.setAdUnitId(mPidConfig.getPid());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                Log.d(Log.TAG, "");
                if (getIntListener() != null) {
                    getIntListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.e(Log.TAG, "error : " + i);
                if (getIntListener() != null) {
                    getIntListener().onInterstitialError();
                }
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(Log.TAG, "");
                if (getIntListener() != null) {
                    getIntListener().onInterstitialClick();
                }
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
                if (getIntListener() != null) {
                    getIntListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.d(Log.TAG, "type : " + getAdType());
                if (mStat != null) {
                    mStat.reportAdmobInterstitialLoaded(mContext, getPidName(), null);
                }
                if (getIntListener() != null) {
                    getIntListener().onInterstitialLoaded();
                    clearIntListener();
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
}
