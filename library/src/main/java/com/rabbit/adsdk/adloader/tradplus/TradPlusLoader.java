package com.rabbit.adsdk.adloader.tradplus;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;
import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.bean.TPBaseAd;
import com.tradplus.ads.mgr.nativead.TPCustomNativeAd;
import com.tradplus.ads.open.TradPlusSdk;
import com.tradplus.ads.open.banner.BannerAdListener;
import com.tradplus.ads.open.banner.TPBanner;
import com.tradplus.ads.open.interstitial.InterstitialAdListener;
import com.tradplus.ads.open.interstitial.TPInterstitial;
import com.tradplus.ads.open.nativead.NativeAdListener;
import com.tradplus.ads.open.nativead.TPNative;
import com.tradplus.ads.open.reward.RewardAdListener;
import com.tradplus.ads.open.reward.TPReward;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TradPlusLoader extends AbstractSdkLoader {

    private static AtomicBoolean sAtomicBoolean = new AtomicBoolean(false);
    private TPBanner mTPBanner;
    private TPInterstitial mTPInterstitial;
    private TPReward mTPReward;
    private TPNative mTPNative;
    private TradPlusBindView mTradPlusBindView = new TradPlusBindView();

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_TRADPLUS;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        String aid = null;
        if (mPidConfig != null) {
            Map<String, String> extra = mPidConfig.getExtra();
            if (extra != null) {
                aid = extra.get(Constant.APP_ID);
            }
        }
        if (!TextUtils.isEmpty(aid)) {
            if (!sAtomicBoolean.getAndSet(true)) {
                Log.iv(Log.TAG, "init trad plus with app id : " + aid);
                TradPlusSdk.initSdk(context, aid);
            } else {
                Log.iv(Log.TAG, "trad plus has initialized");
            }
        } else {
            Log.e(Log.TAG, "app id is empty");
        }
    }


    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        Activity activity = getActivity();
        TPBanner tpBanner = new TPBanner(activity);
        tpBanner.closeAutoShow();
        tpBanner.setAdListener(new BannerAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                mTPBanner = tpBanner;
                putCachedAdTime(mTPBanner);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner impression"));
            }

            @Override
            public void onAdShowFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(tpAdError), true));
                notifyAdShowFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdLoadFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdClosed(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner collapsed"));
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onBannerRefreshed() {
                Log.iv(Log.TAG, formatLog("ad banner refreshed"));
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        tpBanner.loadAd(getPid());
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mTPBanner != null && !isCachedAdExpired(mTPBanner);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            clearCachedAdTime(mTPBanner);
            viewGroup.removeAllViews();
            ViewParent viewParent = mTPBanner.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mTPBanner);
            }
            viewGroup.addView(mTPBanner);
            mTPBanner.showAd();
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            mTPBanner = null;
            reportAdShow();
            notifyAdShow();
            reportAdImp();
            notifyAdImp();
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("show error : " + e));
        }
    }


    @Override
    public void loadInterstitial() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        setLoading(true, STATE_REQUEST);
        mTPInterstitial = new TPInterstitial(activity, getPid(), false);
        mTPInterstitial.setAdListener(new InterstitialAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mTPInterstitial);
                reportAdLoaded();
                notifyAdLoaded(TradPlusLoader.this);
            }

            @Override
            public void onAdFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial impression"));
                reportAdImp();
                notifyAdImp();
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdClosed(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial dismissed"));
                clearLastShowTime();
                onResetInterstitial();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdVideoError(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial show error"));
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mTPInterstitial.loadAd();
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (mTPInterstitial != null) {
            loaded = mTPInterstitial.isReady() && !isCachedAdExpired(mTPInterstitial);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (mTPInterstitial != null && mTPInterstitial.isReady()) {
            Activity activity = getActivity();
            mTPInterstitial.showAd(activity, "");
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetInterstitial();
        }
        return false;
    }

    @Override
    public void loadRewardedVideo() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT);
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        setLoading(true, STATE_REQUEST);
        mTPReward = new TPReward(activity, getPid(), false);
        mTPReward.setAdListener(new RewardAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                putCachedAdTime(mTPReward);
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                notifyAdLoaded(TradPlusLoader.this);
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward start"));
                reportAdImp();
                notifyAdImp();
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdClosed(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward closed"));
                clearLastShowTime();
                onResetReward();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onAdReward(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward complete"));
                reportAdReward();
                notifyRewardAdsCompleted();
                AdReward adReward = new AdReward();
                adReward.setType(tpAdInfo.rewardName);
                adReward.setAmount(String.valueOf(tpAdInfo.rewardNumber));
                notifyRewarded(adReward);
            }

            @Override
            public void onAdVideoError(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward video error"));
                onResetReward();
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mTPReward.loadAd();
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = mTPReward != null && mTPReward.isReady() && !isCachedAdExpired(mTPReward);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (mTPReward != null) {
            Activity activity = getActivity();
            mTPReward.showAd(activity, "");
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetReward();
        }
        return false;
    }


    @Override
    public void loadNative(Params params) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        setLoading(true, STATE_REQUEST);
        TPNative tpNative = new TPNative(getActivity(), getPid());
        tpNative.setAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo, TPBaseAd tpBaseAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                mTPNative = tpNative;
                putCachedAdTime(mTPNative);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad network click render : " + network));
                reportAdClick(network);
                notifyAdClick(network);
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String render = getNetwork(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad network impression render : " + render));
                reportAdImp(render);
                notifyAdImp(render);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdShowFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad show error : " + toSdkError(tpAdError)));
                notifyAdShowFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdLoadFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                reportAdError(codeToError(tpAdError));
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(toSdkError(tpAdError));
            }

            @Override
            public void onAdClosed(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad native closed"));
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        tpNative.loadAd();
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (mTPNative != null) {
            loaded = !isCachedAdExpired(mTPNative);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (mTPNative != null) {
            final TPCustomNativeAd customNativeAd = mTPNative.getNativeAd();
            if (customNativeAd != null) {
                mTradPlusBindView.bindNativeView(mContext, mPidConfig, params, customNativeAd);
                customNativeAd.showAd(viewGroup, mTradPlusBindView.getCustomTPNativeAdRender(), "");
                if (viewGroup != null && viewGroup.getVisibility() != View.VISIBLE) {
                    viewGroup.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(Log.TAG, "render native ad view error : TPCustomNativeAd == null");
            }
            clearCachedAdTime(mTPNative);
            mTPNative = null;
            reportAdShow();
            notifyAdShow();
        } else {
            Log.iv(Log.TAG, formatLog("TPNative is null"));
        }
    }

    private String getNetwork(TPAdInfo tpAdInfo) {
        return tpAdInfo != null ? tpAdInfo.adSourceName : null;
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(mTPInterstitial);
        if (mTPInterstitial != null) {
            mTPInterstitial = null;
        }
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(mTPReward);
        if (mTPReward != null) {
            mTPReward = null;
        }
    }

    private void reportTradPlusImpressionData(TPAdInfo tpAdInfo) {
        try {
            double ecpm = 0f;
            try {
                ecpm = Double.parseDouble(tpAdInfo.ecpm);
            } catch (Exception e) {
            }
            Map<String, Object> map = new HashMap<>();
            map.put("value", ecpm);
            map.put("ad_network", tpAdInfo.adSourceName);
            map.put("ad_network_pid", tpAdInfo.adSourceId);
            map.put("ad_unit_id", getPid());
            map.put("ad_format", getAdType());
            map.put("ad_unit_name", getAdPlaceName());
            map.put("ad_provider", getSdkName());
            map.put("ad_bidding", tpAdInfo.isBiddingNetwork);
            String gaid = Utils.getString(mContext, Constant.PREF_GAID);
            map.put("ad_gaid", gaid);
            if (isReportAdImpData()) {
                InternalStat.reportEvent(getContext(), "Ad_Impression_Revenue", map);
            }
            StringBuilder builder = new StringBuilder("{");
            builder.append("\n");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.append("  " + entry.getKey() + " : " + entry.getValue());
                builder.append("\n");
            }
            builder.append("}");
            Log.iv(Log.TAG, getSdkName() + " imp data : " + builder.toString());
        } catch (Exception e) {
            Log.e(Log.TAG, "report trusplus error : " + e);
        }
    }

    private String codeToError(TPAdError tpError) {
        if (tpError != null) {
            return tpError.getErrorMsg();
        }
        return null;
    }

    private int toSdkError(TPAdError tpAdError) {
        if (tpAdError != null) {
            return tpAdError.getErrorCode();
        }
        return Constant.AD_ERROR_UNKNOWN;
    }
}
