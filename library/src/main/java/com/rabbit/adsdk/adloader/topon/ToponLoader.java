package com.rabbit.adsdk.adloader.topon;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.NativeAd;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;
import com.tradplus.ads.base.bean.TPAdInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToponLoader extends AbstractSdkLoader {

    private static AtomicBoolean sAtomicBoolean = new AtomicBoolean(false);
    private ATBannerView mATBannerView;
    private ATInterstitial mATInterstitial;
    private ATRewardVideoAd mATRewardVideoAd;
    private NativeAd mNativeAd;
    private ATNative mATNative;
    private ToponBindView mToponBindView = new ToponBindView();

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_TOPON;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
        String appId = null;
        String appKey = null;
        if (mPidConfig != null) {
            Map<String, String> extra = mPidConfig.getExtra();
            if (extra != null) {
                appId = extra.get(Constant.APP_ID);
                appKey = extra.get(Constant.APP_KEY);
            }
        }
        if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appKey)) {
            if (!sAtomicBoolean.getAndSet(true)) {
                ATSDK.integrationChecking(mContext);
                Log.iv(Log.TAG, "init " + getSdkName() + " with app id : " + appId + " , appKey : " + appKey);
                ATSDK.init(mContext, appId, appKey);
            } else {
                Log.iv(Log.TAG, getSdkName() + " has initialized");
            }
        } else {
            Log.e(Log.TAG, getSdkName() + " app id or app key is empty");
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
        ATBannerView atBannerView = new ATBannerView(activity);
        atBannerView.setPlacementId(getPid());
        atBannerView.setBannerAdListener(new ATBannerListener() {
            @Override
            public void onBannerLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                mATBannerView = atBannerView;
                putCachedAdTime(mATBannerView);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onBannerFailed(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(adError), true));
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onBannerClicked(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onBannerShow(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner impression"));
                reportAdImp();
                notifyAdImp();
                reportToponImpressionData(atAdInfo);
            }

            @Override
            public void onBannerClose(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner collapsed"));
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onBannerAutoRefreshed(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad banner refreshed"));
            }

            @Override
            public void onBannerAutoRefreshFail(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad auto refresh failed : " + codeToError(adError), true));
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        atBannerView.setScenario(getSceneId());
        atBannerView.loadAd();
    }

    private String getLoadedInfo(TPAdInfo tpAdInfo) {
        return null;
    }

    @Override
    public boolean isBannerLoaded() {
        boolean loaded = mATBannerView != null && !isCachedAdExpired(mATBannerView);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
        printInterfaceLog(ACTION_SHOW);
        try {
            viewGroup.removeAllViews();
            ViewParent viewParent = mATBannerView.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mATBannerView);
            }
            viewGroup.addView(mATBannerView);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            clearCachedAdTime(mATBannerView);
            mATBannerView = null;
            reportAdShow();
            notifyAdShow();
        } catch (Exception e) {
            Log.iv(Log.TAG, formatLog("show error : " + e));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : TPBaner not ready");
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
        mATInterstitial = new ATInterstitial(activity, getPid());
        mATInterstitial.setAdListener(new ATInterstitialListener() {
            @Override
            public void onInterstitialAdLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mATInterstitial);
                reportAdLoaded();
                notifyAdLoaded(ToponLoader.this);
            }

            @Override
            public void onInterstitialAdLoadFail(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(adError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetInterstitial();
                reportAdError(codeToError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onInterstitialAdClicked(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onInterstitialAdShow(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial impression"));
                reportAdImp();
                notifyAdImp();
                reportToponImpressionData(atAdInfo);
            }

            @Override
            public void onInterstitialAdClose(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial dismissed"));
                clearLastShowTime();
                onResetInterstitial();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onInterstitialAdVideoStart(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial video start"));
            }

            @Override
            public void onInterstitialAdVideoEnd(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad interstitial video end"));
            }

            @Override
            public void onInterstitialAdVideoError(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad interstitial show error"));
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, "ad interstitial show error");
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mATInterstitial.load();
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (mATInterstitial != null) {
            loaded = mATInterstitial.isAdReady() && !isCachedAdExpired(mATInterstitial);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (mATInterstitial != null && mATInterstitial.isAdReady()) {
            Activity activity = getActivity();
            mATInterstitial.show(activity, getSceneId());
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetInterstitial();
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
        mATRewardVideoAd = new ATRewardVideoAd(activity, getPid());
        mATRewardVideoAd.setAdListener(new ATRewardVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.iv(Log.TAG, formatLog("ad load success"));
                putCachedAdTime(mATRewardVideoAd);
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                notifyAdLoaded(ToponLoader.this);
            }

            @Override
            public void onRewardedVideoAdFailed(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(adError), true));
                setLoading(false, STATE_FAILURE);
                clearLastShowTime();
                onResetReward();
                reportAdError(codeToError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onRewardedVideoAdPlayStart(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward start"));
                reportAdImp();
                notifyAdImp();
                reportToponImpressionData(atAdInfo);
            }

            @Override
            public void onRewardedVideoAdPlayEnd(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward video start"));
            }

            @Override
            public void onRewardedVideoAdPlayFailed(AdError adError, ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward video error"));
                onResetReward();
                notifyAdShowFailed(Constant.AD_ERROR_UNKNOWN, "ad reward video error : " + toErrorMessage(adError));
            }

            @Override
            public void onRewardedVideoAdClosed(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward closed"));
                clearLastShowTime();
                onResetReward();
                reportAdClose();
                notifyAdDismiss();
            }

            @Override
            public void onRewardedVideoAdPlayClicked(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onReward(ATAdInfo atAdInfo) {
                Log.iv(Log.TAG, formatLog("ad reward complete"));
                reportAdReward();
                notifyRewardAdsCompleted();
                AdReward adReward = new AdReward();
                adReward.setType(atAdInfo.getScenarioRewardName());
                adReward.setAmount(String.valueOf(atAdInfo.getScenarioRewardNumber()));
                notifyRewarded(adReward);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mATRewardVideoAd.load();
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        boolean loaded = mATRewardVideoAd != null && mATRewardVideoAd.isAdReady()&& !isCachedAdExpired(mATRewardVideoAd);
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public boolean showRewardedVideo() {
        printInterfaceLog(ACTION_SHOW);
        if (mATRewardVideoAd != null) {
            Activity activity = getActivity();
            mATRewardVideoAd.show(activity, getSceneId());
            updateLastShowTime();
            reportAdShow();
            notifyAdShow();
            return true;
        } else {
            onResetReward();
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
        mATNative = new ATNative(mContext, getPid(), new ATNativeNetworkListener() {
            @Override
            public void onNativeAdLoaded() {
                if (mATNative != null) {
                    Log.iv(Log.TAG, formatLog("ad load success"));
                    reportAdLoaded();
                    setLoading(false, STATE_SUCCESS);
                    mNativeAd = mATNative.getNativeAd(getSceneId());
                    putCachedAdTime(mNativeAd);
                    notifySdkLoaderLoaded(false);
                } else {
                    Log.iv(Log.TAG, formatLog("ad load failed : ATNative not ready", true));
                    setLoading(false, STATE_FAILURE);
                    notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "ATNative not ready");
                }
            }

            @Override
            public void onNativeAdLoadFail(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(adError), true));
                reportAdError(codeToError(adError));
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        try {
            Map<String, String> extra = mPidConfig.getExtra();
            if (extra != null) {
                String widthString = extra.get(Constant.WIDTH);
                String heightString = extra.get(Constant.HEIGHT);
                Map<String, Object> localMap = new HashMap<>();
                localMap.put(ATAdConst.KEY.AD_WIDTH, Integer.parseInt(widthString));
                localMap.put(ATAdConst.KEY.AD_HEIGHT, Integer.parseInt(heightString));
                mATNative.setLocalExtra(localMap);
            }
        } catch (Exception e) {
        }
        mATNative.makeAdRequest();
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (mNativeAd != null) {
            loaded = !isCachedAdExpired(mNativeAd);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (mNativeAd != null) {
            mNativeAd.setNativeEventListener(new ATNativeEventListener() {
                @Override
                public void onAdImpressed(ATNativeAdView atNativeAdView, ATAdInfo atAdInfo) {
                    String render = getNetwork(atAdInfo);
                    Log.iv(Log.TAG, formatLog("ad network impression render : " + render));
                    reportAdImp(render);
                    notifyAdImp(render);
                    reportToponImpressionData(atAdInfo);
                }

                @Override
                public void onAdClicked(ATNativeAdView atNativeAdView, ATAdInfo atAdInfo) {
                    String network = getNetwork(atAdInfo);
                    Log.iv(Log.TAG, formatLog("ad network click render : " + network));
                    reportAdClick(network);
                    notifyAdClick(network);
                }

                @Override
                public void onAdVideoStart(ATNativeAdView atNativeAdView) {
                    Log.iv(Log.TAG, formatLog("ad video start"));
                }

                @Override
                public void onAdVideoEnd(ATNativeAdView atNativeAdView) {
                    Log.iv(Log.TAG, formatLog("ad video end"));
                }

                @Override
                public void onAdVideoProgress(ATNativeAdView atNativeAdView, int i) {
                }
            });
            try {
                mToponBindView.bindNativeView(mContext, viewGroup, mNativeAd, mPidConfig, params);
                clearCachedAdTime(mNativeAd);
                mNativeAd = null;
                reportAdShow();
                notifyAdShow();
            } catch (Exception e) {
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : NativeAd show exception [" + e + "]");
            }
        } else {
            Log.iv(Log.TAG, formatLog("NativeAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : NativeAd not ready");
        }
    }

    private String getNetwork(ATAdInfo atAdInfo) {
        if (atAdInfo != null) {
            int networkFirmId = atAdInfo.getNetworkFirmId();
            String networkName = sNetworkFirmTable.get(networkFirmId);
            if (TextUtils.isEmpty(networkName)) {
                networkName = String.valueOf(networkFirmId);
            } else {
                networkName = networkName.toLowerCase();
            }
            return networkName;
        }
        return null;
    }

    private void reportToponImpressionData(ATAdInfo atAdInfo) {
        try {
            double ecpm = atAdInfo.getEcpm();
            Map<String, Object> map = new HashMap<>();
            map.put("value", ecpm);
            map.put("ad_network", sNetworkFirmTable.get(atAdInfo.getNetworkFirmId()));
            map.put("ad_network_pid", atAdInfo.getNetworkPlacementId());
            map.put("ad_unit_id", getPid());
            map.put("ad_format", getAdType());
            map.put("ad_unit_name", getAdPlaceName());
            map.put("ad_provider", getSdkName());
            map.put("ad_bidding", atAdInfo.isHeaderBiddingAdsource());
            map.put("ad_precision", atAdInfo.getEcpmPrecision());
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
            Log.e(Log.TAG, "report topnon error : " + e);
        }
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(mATInterstitial);
        if (mATInterstitial != null) {
            mATInterstitial = null;
        }
    }

    @Override
    protected void onResetReward() {
        super.onResetReward();
        clearCachedAdTime(mATRewardVideoAd);
        if (mATRewardVideoAd != null) {
            mATRewardVideoAd = null;
        }
    }

    private String codeToError(AdError adError) {
        if (adError != null) {
            return adError.getDesc();
        }
        return null;
    }

    private int toSdkError(AdError adError) {
        return Constant.AD_ERROR_LOAD;
    }

    private String toErrorMessage(AdError adError) {
        if (adError != null) {
            return adError.getFullErrorInfo();
        }
        return null;
    }

    public static final Map<Integer, String> sNetworkFirmTable;
    static {
        sNetworkFirmTable = new HashMap<>();
        sNetworkFirmTable.put(1, "Facebook");
        sNetworkFirmTable.put(2, "Admob");
        sNetworkFirmTable.put(3, "Inmobi");
        sNetworkFirmTable.put(4, "Flurry");
        sNetworkFirmTable.put(5, "Applovin");
        sNetworkFirmTable.put(6, "Mintegral");
        sNetworkFirmTable.put(7, "Mopub");
        sNetworkFirmTable.put(10, "Tapjoy");
        sNetworkFirmTable.put(11, "Ironsource");
        sNetworkFirmTable.put(12, "UnityAds");
        sNetworkFirmTable.put(13, "Vungle");
        sNetworkFirmTable.put(14, "Adcolony");
        sNetworkFirmTable.put(66, "TopOn Adx");
    }
}
