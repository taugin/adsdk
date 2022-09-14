package com.rabbit.adsdk.adloader.tradplus;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.ActivityMonitor;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.tradplus.ads.base.GlobalTradPlus;
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
import com.tradplus.ads.open.splash.SplashAdListener;
import com.tradplus.ads.open.splash.TPSplash;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TradPlusLoader extends AbstractSdkLoader {

    private static AtomicBoolean sAtomicBoolean = new AtomicBoolean(false);
    private TPBanner mTPBanner;
    private TPInterstitial mTPInterstitial;
    private TPReward mTPReward;
    private TPNative mTPNative;
    private TPSplash mTPSplash;
    private TradPlusBindView mTradPlusBindView = new TradPlusBindView();

    @Override
    protected BaseBindNativeView getBaseBindNativeView() {
        return mTradPlusBindView;
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_TRADPLUS;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        String appId = null;
        if (mPidConfig != null) {
            Map<String, String> extra = mPidConfig.getExtra();
            if (extra != null) {
                appId = extra.get(Constant.APP_ID);
            }
        }
        if (!TextUtils.isEmpty(appId)) {
            if (!sAtomicBoolean.getAndSet(true)) {
                Log.iv(Log.TAG, "init " + getSdkName() + " with app id : " + appId);
                TradPlusSdk.initSdk(context, appId);
                TradPlusSdk.setAutoExpiration(false);
            } else {
                Log.iv(Log.TAG, getSdkName() + " has initialized");
            }
        } else {
            Log.e(Log.TAG, getSdkName() + " app id is empty");
        }
    }


    @Override
    public void loadBanner(int adSize) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isBannerLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
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
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(tpAdInfo)));
                    mTPBanner = tpBanner;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mTPBanner);
                    String network = getNetwork(tpAdInfo);
                    setAdNetworkAndRevenue(network, getTradPlusAdRevenue(tpAdInfo));
                    reportAdLoaded();
                    notifySdkLoaderLoaded(false);
                } else {
                    reportAdReLoaded();
                    autoRefreshBanner(tpBanner);
                }
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid);
                notifyAdClick(network);
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad impression network : " + network));
                reportAdImp(network, networkPid);
                notifyAdImp(network);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdShowFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(tpAdError), true));
                notifyAdShowFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
            }

            @Override
            public void onAdLoadFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
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
        tpBanner.loadAd(getPid(), getSceneId());
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mTPBanner != null && !isCachedAdExpired(mTPBanner);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private void autoRefreshBanner(TPBanner tpBanner) {
        try {
            if (tpBanner != null) {
                tpBanner.showAd();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            reportAdShow();
            notifyAdShow();
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
        } catch (Exception e) {
            Log.e(Log.TAG, formatShowErrorLog(String.valueOf(e)));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPBanner not ready");
        }
    }


    @Override
    public void loadInterstitial() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
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
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(tpAdInfo)));
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mTPInterstitial);
                    String network = getNetwork(tpAdInfo);
                    setAdNetworkAndRevenue(network, getTradPlusAdRevenue(tpAdInfo));
                    reportAdLoaded();
                    notifyAdLoaded(TradPlusLoader.this);
                } else {
                    reportAdReLoaded();
                }
            }

            @Override
            public void onAdFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad impression network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid);
                notifyAdClick(network);
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
            public void onAdVideoError(TPAdInfo tpAdInfo, TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad interstitial show error"));
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, "ad interstitial show error");
            }

            @Override
            public void onAdVideoStart(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial play start"));
            }

            @Override
            public void onAdVideoEnd(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial play end"));
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
    public boolean showInterstitial(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (mTPInterstitial != null && mTPInterstitial.isReady()) {
            reportAdShow();
            notifyAdShow();
            refreshContext();
            Activity activity = getActivity();
            mTPInterstitial.showAd(activity, TextUtils.isEmpty(sceneName) ? getSceneId() : sceneName);
            updateLastShowTime();
            return true;
        } else {
            onResetInterstitial();
            Log.e(Log.TAG, formatShowErrorLog("TPInterstitial not ready"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPInterstitial not ready");
        }
        return false;
    }

    @Override
    public void loadRewardedVideo() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iv(Log.TAG, formatLog("error activity context"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONTEXT, "error activity context");
            return;
        }

        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isRewardedVideoLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
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
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(tpAdInfo)));
                    putCachedAdTime(mTPReward);
                    setLoading(false, STATE_SUCCESS);
                    String network = getNetwork(tpAdInfo);
                    setAdNetworkAndRevenue(network, getTradPlusAdRevenue(tpAdInfo));
                    reportAdLoaded();
                    notifyAdLoaded(TradPlusLoader.this);
                } else {
                    reportAdReLoaded();
                }
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid);
                notifyAdClick(network);
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad impression network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
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
            public void onAdVideoStart(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward play start"));
            }

            @Override
            public void onAdVideoEnd(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward play end"));
            }

            @Override
            public void onAdVideoError(TPAdInfo tpAdInfo, TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad reward video error"));
                onResetReward();
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, "ad reward video error");
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
    public boolean showRewardedVideo(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (mTPReward != null) {
            reportAdShow();
            notifyAdShow();
            refreshContext();
            Activity activity = getActivity();
            mTPReward.showAd(activity, TextUtils.isEmpty(sceneName) ? getSceneId() : sceneName);
            updateLastShowTime();
            return true;
        } else {
            onResetReward();
            Log.e(Log.TAG, formatShowErrorLog("TPReward is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPReward not ready");
        }
        return false;
    }


    @Override
    public void loadNative(Params params) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }
        setLoading(true, STATE_REQUEST);
        TPNative tpNative = new TPNative(getActivity(), getPid(), false);
        tpNative.setAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo, TPBaseAd tpBaseAd) {
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(tpAdInfo)));
                    mTPNative = tpNative;
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mTPNative);
                    String network = getNetwork(tpAdInfo);
                    setAdNetworkAndRevenue(network, getTradPlusAdRevenue(tpAdInfo));
                    reportAdLoaded();
                    notifySdkLoaderLoaded(false);
                } else {
                    reportAdReLoaded();
                }
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid);
                notifyAdClick(network);
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad impression network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdShowFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad show error : " + toSdkError(tpAdError)));
                notifyAdShowFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
            }

            @Override
            public void onAdLoadFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                reportAdError(codeToError(tpAdError));
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
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
                reportAdShow();
                notifyAdShow();
                if (viewGroup != null) {
                    viewGroup.removeAllViews();
                    ;
                }
                mTradPlusBindView.bindNativeView(mContext, mPidConfig, params, customNativeAd);
                customNativeAd.showAd(viewGroup, mTradPlusBindView.getCustomTPNativeAdRender(), getSceneId());
                if (viewGroup != null && viewGroup.getVisibility() != View.VISIBLE) {
                    viewGroup.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(Log.TAG, formatShowErrorLog("TPCustomNativeAd is null"));
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPCustomNativeAd not ready");
            }
            clearCachedAdTime(mTPNative);
            mTPNative = null;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("TPNative is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPNative not ready");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isSplashLoaded() {
        boolean loaded = super.isSplashLoaded();
        if (mTPSplash != null) {
            loaded = mTPSplash.isReady();
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadSplash() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isSplashLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifyAdLoaded(this);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        setLoading(true, STATE_REQUEST);
        mTPSplash = new TPSplash(getActivity(), getPid());
        mTPSplash.setAdListener(new SplashAdListener() {
            @Override
            public void onAdLoaded(TPAdInfo tpAdInfo, TPBaseAd tpBaseAd) {
                if (!isStateSuccess()) {
                    Log.iv(Log.TAG, formatLog("ad load success" + getLoadedInfo(tpAdInfo)));
                    setLoading(false, STATE_SUCCESS);
                    putCachedAdTime(mTPSplash);
                    String network = getNetwork(tpAdInfo);
                    setAdNetworkAndRevenue(network, getTradPlusAdRevenue(tpAdInfo));
                    reportAdLoaded();
                    notifyAdLoaded(TradPlusLoader.this);
                } else {
                    reportAdReLoaded();
                }
            }

            @Override
            public void onAdClicked(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad click network : " + network + " , network pid : " + networkPid));
                reportAdClick(network, networkPid);
                notifyAdClick(network);
            }

            @Override
            public void onAdImpression(TPAdInfo tpAdInfo) {
                String network = getNetwork(tpAdInfo);
                String networkPid = getNetworkPid(tpAdInfo);
                Log.iv(Log.TAG, formatLog("ad impression network : " + network + " , network pid : " + networkPid));
                reportAdImp(network, networkPid);
                notifyAdImp(network);
                reportTradPlusImpressionData(tpAdInfo);
            }

            @Override
            public void onAdShowFailed(TPAdInfo tpAdInfo, TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(tpAdError)));
                clearLastShowTime();
                onResetSplash();
                notifyAdShowFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
            }

            @Override
            public void onAdLoadFailed(TPAdError tpAdError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(tpAdError), true));
                setLoading(false, STATE_FAILURE);
                onResetSplash();
                reportAdError(codeToError(tpAdError));
                notifyAdLoadFailed(toSdkError(tpAdError), toErrorMessage(tpAdError));
            }

            @Override
            public void onAdClosed(TPAdInfo tpAdInfo) {
                Log.iv(Log.TAG, formatLog("ad dismissed"));
                clearLastShowTime();
                onResetSplash();
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        setSplashParams(mTPSplash);
        mTPSplash.loadAd(null);
    }

    @Override
    public boolean showSplash(ViewGroup viewGroup) {
        Log.iv(Log.TAG, getAdPlaceName() + " - " + getSdkName() + " show splash");
        if (mTPSplash != null && mTPSplash.isReady()) {
            reportAdShow();
            notifyAdShow();
            refreshContext();
            mTPSplash.showAd(viewGroup);
            updateLastShowTime();
            return true;
        } else {
            Log.e(Log.TAG, formatShowErrorLog("TPSplash is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPSplash not ready");
            onResetSplash();
        }
        return false;
    }

    /**
     * 设置开屏加载的参数
     *
     * @param tpSplash
     */
    private void setSplashParams(TPSplash tpSplash) {
        try {
            Map<String, Object> localExtra = new HashMap<String, Object>();
            // 增加Pangle开屏底部图标
            if (mPidConfig != null && mPidConfig.isShowSplashIcon()) {
                int icon = Utils.getApplicationIcon(mContext);
                if (icon > 0) {
                    localExtra.put("app_icon", icon);
                }
            }
            if (mPidConfig != null && mPidConfig.getSplashTimeout() > 0) {
                int splashTimeout = mPidConfig.getSplashTimeout();
                Log.iv(Log.TAG, formatLog("load splash time out : " + splashTimeout));
                localExtra.put("time_out", splashTimeout);
            }
            if (tpSplash != null && localExtra != null && !localExtra.isEmpty()) {
                tpSplash.setCustomParams(localExtra);
            }
        } catch (Exception e) {
        }
    }
    ////////////////////////////////////////////////////////////////////////////

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

    @Override
    protected void onResetSplash() {
        super.onResetSplash();
        clearCachedAdTime(mTPSplash);
        mTPSplash = null;
    }

    private void refreshContext() {
        try {
            GlobalTradPlus.getInstance().refreshContext(ActivityMonitor.get(mContext).getTopActivity());
        } catch (Exception e) {
        }
    }

    private void reportTradPlusImpressionData(TPAdInfo tpAdInfo) {
        try {
            Map<String, Object> map = new HashMap<>();
            double revenue = getTradPlusAdRevenue(tpAdInfo);
            map.put(Constant.AD_VALUE, revenue);
            map.put(Constant.AD_MICRO_VALUE, Double.valueOf(revenue * 1000000).intValue());
            map.put(Constant.AD_CURRENCY, getCurrency(tpAdInfo));
            map.put(Constant.AD_NETWORK, tpAdInfo.adSourceName);
            map.put(Constant.AD_NETWORK_PID, getNetworkPid(tpAdInfo));
            map.put(Constant.AD_UNIT_ID, getPid());
            map.put(Constant.AD_FORMAT, getAdType());
            map.put(Constant.AD_UNIT_NAME, getAdPlaceName());
            map.put(Constant.AD_PLACEMENT, tpAdInfo.sceneId);
            map.put(Constant.AD_PLATFORM, getSdkName());
            map.put(Constant.AD_BIDDING, tpAdInfo.isBiddingNetwork);
            map.put(Constant.AD_PRECISION, tpAdInfo.ecpmPrecision);
            map.put(Constant.AD_SDK_VERSION, getSdkVersion());
            map.put(Constant.AD_APP_VERSION, getAppVersion());
            map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            StringBuilder builder = new StringBuilder("{");
            builder.append("\n");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.append("  " + entry.getKey() + " : " + entry.getValue());
                builder.append("\n");
            }
            builder.append("}");
            Log.iv(Log.TAG, getSdkName() + " imp data : " + builder.toString());
            onReportAdImpData(map);
        } catch (Exception e) {
            Log.e(Log.TAG, "report trusplus error : " + e);
        }
    }

    private String getNetwork(TPAdInfo tpAdInfo) {
        return tpAdInfo != null ? tpAdInfo.adSourceName : null;
    }

    private String getNetworkPid(TPAdInfo tpAdInfo) {
        String networkUnitId = null;
        try {
            if (tpAdInfo != null) {
                if ("mintegral".equalsIgnoreCase(tpAdInfo.adSourceName)) {
                    if (tpAdInfo.configBean != null) {
                        networkUnitId = tpAdInfo.configBean.getUnitId();
                    }
                    if (TextUtils.isEmpty(networkUnitId)) {
                        networkUnitId = tpAdInfo.adSourceId;
                    }
                } else {
                    networkUnitId = tpAdInfo.adSourceId;
                }
            }
        } catch (Exception e) {
        }
        return networkUnitId;
    }

    private String getCurrency(TPAdInfo tpAdInfo) {
        if (tpAdInfo != null && !TextUtils.isEmpty(tpAdInfo.currency)) {
            return tpAdInfo.currency;
        }
        return "USD";
    }

    private double getTradPlusAdRevenue(TPAdInfo tpAdInfo) {
        double ecpm = 0f;
        if (tpAdInfo != null) {
            try {
                ecpm = Double.parseDouble(tpAdInfo.ecpm);
            } catch (Exception e) {
            }
        }
        return ecpm / 1000;
    }

    private String getLoadedInfo(TPAdInfo tpAdInfo) {
        String networkName = null;
        String placement = null;
        String adRenvue = null;
        if (tpAdInfo != null) {
            networkName = tpAdInfo.adSourceName;
            placement = getNetworkPid(tpAdInfo);
            adRenvue = String.valueOf(getTradPlusAdRevenue(tpAdInfo));
        }
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(networkName)) {
            builder.append("ad_network : " + networkName);
        }
        if (builder.length() > 0) {
            builder.append(" , ");
        }
        if (!TextUtils.isEmpty(placement)) {
            builder.append("ad_network_id : " + placement);
        }
        if (builder.length() > 0) {
            builder.append(" , ");
        }
        if (!TextUtils.isEmpty(adRenvue)) {
            builder.append("ad_revenue : " + adRenvue);
        }
        if (builder.length() > 0) {
            return " - " + builder.toString();
        }
        return builder.toString();
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

    private String toErrorMessage(TPAdError adError) {
        if (adError != null) {
            return "[" + adError.getErrorCode() + "] " + adError.getErrorMsg();
        }
        return null;
    }
}
