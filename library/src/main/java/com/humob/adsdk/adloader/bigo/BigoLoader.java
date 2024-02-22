package com.humob.adsdk.adloader.bigo;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.humob.adsdk.adloader.base.AbstractSdkLoader;
import com.humob.adsdk.adloader.base.BaseBindNativeView;
import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.core.framework.Params;
import com.humob.adsdk.log.Log;
import com.humob.adsdk.utils.Utils;
import com.humob.api.AdActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import sg.bigo.ads.BigoAdSdk;
import sg.bigo.ads.api.AdBid;
import sg.bigo.ads.api.AdConfig;
import sg.bigo.ads.api.AdError;
import sg.bigo.ads.api.AdInteractionListener;
import sg.bigo.ads.api.AdLoadListener;
import sg.bigo.ads.api.InterstitialAd;
import sg.bigo.ads.api.InterstitialAdLoader;
import sg.bigo.ads.api.InterstitialAdRequest;
import sg.bigo.ads.api.NativeAd;
import sg.bigo.ads.api.NativeAdLoader;
import sg.bigo.ads.api.NativeAdRequest;

/**
 * Created by Administrator on 2018/2/9.
 * 由于bidding需要获取到令牌，因此要先完成admob的初始化，然后再进行广告加载
 */

public class BigoLoader extends AbstractSdkLoader {

    private static AtomicBoolean sBigoInited = new AtomicBoolean(false);
    private static int sSDKInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private InterstitialAd mInterstitialAd;
    private NativeAd mNativeAd;
    private NativeAd lastUseNativeAd;
    // 保存加载成功的竞价信息
    private AdBid mAdBid;

    private BigoNativeListener bigoNativeListener;
    private BigoInterstitialListener bigoInterstitialListener;

    private BigoBindNativeView bigoBindNativeView = new BigoBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return bigoBindNativeView;
    }

    @Override
    public String getSdkName() {
        return Constant.AD_NETWORK_BIGO;
    }

    private void initBigo(SDKInitializeListener sdkInitializeListener) {
        if (!sBigoInited.getAndSet(true)) {
            Log.iv(Log.TAG, "start initializing " + getSdkName() + " sdk");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess();
                    }
                }
            }, 15000);
            final long startInit = SystemClock.elapsedRealtime();
            AdConfig adConfig = new AdConfig.Builder()
                    .setAppId(getAppId())
                    .setDebug(Utils.isDebuggable(mContext))
                    .build();
            BigoAdSdk.initialize(mContext, adConfig, new BigoAdSdk.InitListener() {
                @Override
                public void onInitialized() {
                    Log.iv(Log.TAG, getSdkName() + " sdk init successfully cost time : " + (SystemClock.elapsedRealtime() - startInit));
                    try {
                        mHandler.removeCallbacksAndMessages(null);
                    } catch (Exception e) {
                    }
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess();
                    }
                }
            });
        } else {
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeSuccess();
            }
        }
    }

    @Override
    protected void initializeSdk(SDKInitializeListener sdkInitializeListener) {
        initBigo(sdkInitializeListener);
    }

    @Override
    protected int getSdkInitializeState() {
        return sSDKInitializeState;
    }

    @Override
    protected void setSdkInitializeState(int state) {
        sSDKInitializeState = state;
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mInterstitialAd != null && !mInterstitialAd.isExpired() && !isShowTimeExpired();
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadInterstitial() {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadInterstitialInternal();
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadInterstitialInternal() {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isInterstitialLoaded()) {
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
        bigoInterstitialListener = new BigoInterstitialListener();
        InterstitialAdRequest interstitialAdRequest = new InterstitialAdRequest.Builder()
                .withSlotId(getPid())
                .build();
        InterstitialAdLoader interstitialAdLoader = new InterstitialAdLoader.Builder().
                withAdLoadListener(bigoInterstitialListener.adLoadListener).build();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        interstitialAdLoader.loadAd(interstitialAdRequest);
    }

    private class BigoInterstitialListener extends AbstractAdListener {
        private AdLoadListener adLoadListener = new AdLoadListener<InterstitialAd>() {
            @Override
            public void onError(AdError error) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(error), true));
                setLoading(false, STATE_FAILURE);
                mInterstitialAd = null;
                reportAdError(codeToError(error));
                notifyAdLoadFailed(toSdkError(error), toErrorMessage(error));
            }

            @Override
            public void onAdLoaded(InterstitialAd ad) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mInterstitialAd = ad;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(mInterstitialAd);
                setInterstitialListener(mInterstitialAd);
                double price = 0f;
                try {
                    mAdBid = mInterstitialAd.getBid();
                    price = mAdBid.getPrice() / 1000;
                } catch (Exception e) {
                }
                setRevenueAverage(price);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }
        };

        private void setInterstitialListener(final InterstitialAd interstitialAd) {
            AdInteractionListener adInteractionListener = new AdInteractionListener() {
                @Override
                public void onAdClicked() {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick(getSdkName(), null, impressionId);
                    notifyAdClick(getSdkName(), impressionId);
                }

                @Override
                public void onAdOpened() {
                    Log.iv(Log.TAG, formatLog("ad opened"));
                }

                @Override
                public void onAdClosed() {
                    Log.iv(Log.TAG, formatLog("ad dismissed"));
                    clearLastShowTime();
                    onResetInterstitial();
                    reportAdClose();
                    notifyAdDismiss();
                }

                @Override
                public void onAdError(AdError adError) {
                    Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(adError), true));
                    clearLastShowTime();
                    onResetInterstitial();
                    notifyAdShowFailed(toSdkError(adError), toErrorMessage(adError));
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp(getSdkName(), null);
                    notifyAdImp(getSdkName(), sceneName);
                    impressionId = generateImpressionId();
                    reportBigoImpressionData(getRevenue(), getSdkName(), impressionId, sceneName);
                }
            };
            if (interstitialAd != null) {
                interstitialAd.setAdInteractionListener(adInteractionListener);
            }
        }
    }

    @Override
    public boolean showInterstitial(final String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        if (bigoInterstitialListener != null) {
            bigoInterstitialListener.sceneName = sceneName;
        }
        if (mInterstitialAd != null) {
            reportAdShow();
            notifyAdShow();
            mInterstitialAd.show(getActivity());
            updateLastShowTime();
            return true;
        } else {
            onResetInterstitial();
            Log.iv(Log.TAG, formatShowErrorLog("InterstitialAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "InterstitialAd not ready");
        }
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = super.isNativeLoaded();
        if (mNativeAd != null) {
            loaded = !mNativeAd.isExpired();
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void loadNative(final Params params) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess() {
                loadNativeInternal(params);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadNativeInternal(Params params) {
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
        bigoNativeListener = new BigoNativeListener();
        NativeAdRequest nativeAdRequest = new NativeAdRequest.Builder()
                .withSlotId(getPid())
                .build();
        NativeAdLoader nativeAdLoader = new NativeAdLoader.Builder()
                .withAdLoadListener(bigoNativeListener.adLoadListener).build();
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        nativeAdLoader.loadAd(nativeAdRequest);
    }

    private class BigoNativeListener extends AbstractAdListener {
        private String impressionId = null;

        private void setAdListener(NativeAd nativeAd) {
            AdInteractionListener adListener = new AdInteractionListener() {
                @Override
                public void onAdClicked() {
                    Log.iv(Log.TAG, formatLog("ad click"));
                    reportAdClick(getSdkName(), null, impressionId);
                    notifyAdClick(getSdkName(), impressionId);
                }

                @Override
                public void onAdOpened() {
                    Log.iv(Log.TAG, formatLog("ad opened"));
                }

                @Override
                public void onAdError(AdError adError) {
                    Log.iv(Log.TAG, formatLog("ad show failed : " + codeToError(adError), true));
                    notifyAdShowFailed(toSdkError(adError), "[" + getSdkName() + "]" + toErrorMessage(adError));
                }

                @Override
                public void onAdImpression() {
                    Log.iv(Log.TAG, formatLog("ad impression"));
                    reportAdImp(getSdkName(), null);
                    notifyAdImp(getSdkName(), sceneName);
                    impressionId = generateImpressionId();
                    reportBigoImpressionData(getRevenue(), getSdkName(), impressionId, sceneName);
                }

                @Override
                public void onAdClosed() {
                    Log.iv(Log.TAG, formatLog("ad closed"));
                    reportAdClose();
                    notifyAdDismiss();
                }
            };
            if (nativeAd != null) {
                nativeAd.setAdInteractionListener(adListener);
            }
        }


        private AdLoadListener adLoadListener = new AdLoadListener<NativeAd>() {
            @Override
            public void onError(AdError adError) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + codeToError(adError), true));
                if (adError != null && adError.getCode() == AdError.ERROR_CODE_NO_FILL) {
                    updateLastNoFillTime();
                }
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(adError));
                notifyAdLoadFailed(toSdkError(adError), toErrorMessage(adError));
            }

            @Override
            public void onAdLoaded(NativeAd nativeAd) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                mNativeAd = nativeAd;
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(nativeAd);
                setAdListener(nativeAd);
                double price = 0f;
                try {
                    mAdBid = mNativeAd.getBid();
                    price = mAdBid.getPrice() / 1000;
                } catch (Exception e) {
                }
                setRevenueAverage(price);
                reportAdLoaded();
                notifySdkLoaderLoaded(false);
            }
        };
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null && bigoNativeListener != null) {
            bigoNativeListener.sceneName = params.getSceneName();
        }
        if (mNativeAd != null) {
            reportAdShow();
            notifyAdShow();
            bigoBindNativeView.bindNative(params, viewGroup, mNativeAd, mPidConfig);
            lastUseNativeAd = mNativeAd;
            clearCachedAdTime(mNativeAd);
            mNativeAd = null;
        } else {
            Log.iv(Log.TAG, formatShowErrorLog("NativeAd is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "NativeAd not ready");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void destroy() {
        if (lastUseNativeAd != null) {
            lastUseNativeAd.destroy();
            lastUseNativeAd = null;
        }
    }

    @Override
    protected void onResetInterstitial() {
        super.onResetInterstitial();
        clearCachedAdTime(mInterstitialAd);
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }

    private void resetNative() {
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }

    private void reportBigoImpressionData(double revenue, String network, String impressionId, String sceneName) {
        try {
            Map<String, Object> map = new HashMap<>();
            try {
                map.put(Constant.AD_PRECISION, "precise");
            } catch (Exception e) {
            }
            if (revenue <= 0f && AdActivity.isDebuggable()) {
                revenue = (double) new Random().nextInt(50) / 1000;
                map.put(Constant.AD_PRECISION, "random");
            }
            String networkName = network;
            String adUnitId = getPid();
            String adFormat = getAdType();
            String adUnitName = getAdPlaceName();
            map.put(Constant.AD_VALUE, revenue);
            map.put(Constant.AD_MICRO_VALUE, Double.valueOf(revenue * 1000000).intValue());
            map.put(Constant.AD_CURRENCY, "USD");
            map.put(Constant.AD_NETWORK, networkName);
            map.put(Constant.AD_UNIT_ID, adUnitId);
            map.put(Constant.AD_FORMAT, adFormat);
            map.put(Constant.AD_UNIT_NAME, adUnitName);
            map.put(Constant.AD_PLACEMENT, getSceneId(sceneName));
            map.put(Constant.AD_PLATFORM, getSdkName());
            map.put(Constant.AD_SDK_VERSION, getSdkVersion());
            map.put(Constant.AD_APP_VERSION, getAppVersion());
            // map.put(Constant.AD_GAID, Utils.getString(mContext, Constant.PREF_GAID));
            onReportAdImpData(map, impressionId);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private String codeToError(AdError adError) {
        if (adError != null) {
            int code = adError.getCode();
            if (code == AdError.ERROR_CODE_UNINITIALIZED) {
                return "ERROR_CODE_UNINITIALIZED[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_INVALID_REQUEST) {
                return "ERROR_CODE_INVALID_REQUEST[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_AD_DISABLE) {
                return "ERROR_CODE_AD_DISABLE[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_NETWORK_ERROR) {
                return "ERROR_CODE_NETWORK_ERROR[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_NO_FILL) {
                return "ERROR_CODE_NO_FILL[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_INTERNAL_ERROR) {
                return "ERROR_CODE_INTERNAL_ERROR[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_ASSETS_ERROR) {
                return "ERROR_CODE_ASSETS_ERROR[" + code + "]";
            }
            if (code == AdError.ERROR_CODE_APP_ID_UNMATCHED) {
                return "ERROR_CODE_APP_ID_UNMATCHED[" + code + "]";
            }
            return "UNKNOWN[" + code + "]";
        }
        return "ERROR[NULL]";
    }

    protected int toSdkError(AdError adError) {
        if (adError != null) {
            int code = adError.getCode();
            if (code == AdError.ERROR_CODE_INTERNAL_ERROR) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (code == AdError.ERROR_CODE_INVALID_REQUEST) {
                return Constant.AD_ERROR_INVALID_REQUEST;
            }
            if (code == AdError.ERROR_CODE_NETWORK_ERROR) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (code == AdError.ERROR_CODE_NO_FILL) {
                return Constant.AD_ERROR_NOFILL;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    private String toErrorMessage(AdError adError) {
        if (adError != null) {
            return "[" + adError.getCode() + "] " + adError.getMessage();
        }
        return null;
    }

    private void setRevenueAverage(double revenue) {
        setAdNetworkAndRevenue(getSdkName(), revenue);
    }

    @Override
    public void notifyBidResult(String platform, String adType, String firstNetwork, double firstPrice, String secondNetwork, double secondPrice) {
        if (TextUtils.equals(getSdkName(), platform)) {
            if (mAdBid != null) {
                secondNetwork = TextUtils.equals(secondNetwork, Constant.AD_NETWORK_EMPTY) ? null : secondNetwork;
                mAdBid.notifyWin(secondPrice, secondNetwork);
                Log.iv(Log.TAG, getSdkName() + " bid win platform : " + platform + ", type : " + adType + " , first : " + firstNetwork + "|" + firstPrice + " , second : " + secondNetwork + "|" + secondPrice);
                mAdBid = null;
            }
        } else {
            if (getPidConfig() != null && getPidConfig().isRealTimeBidding()) {
                if (mAdBid != null) {
                    mAdBid.notifyLoss(firstPrice, firstNetwork, AdBid.LOSS_REASON_LOWER_THAN_HIGHEST_PRICE);
                    Log.iv(Log.TAG, getSdkName() + " bid loss platform : " + platform + ", type : " + adType + " , first : " + firstNetwork + "|" + firstPrice + " , second : " + secondNetwork + "|" + secondPrice + " , bigo : " + getRevenue());
                    mAdBid = null;
                    // 竞价失败的时候，销毁缓存的广告，保证下载继续加载
                    if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)) {
                        onResetInterstitial();
                    } else if (TextUtils.equals(adType, Constant.TYPE_NATIVE)) {
                        resetNative();
                    }
                }
            } else {
                if (mAdBid != null) {
                    mAdBid.notifyLoss(firstPrice, firstNetwork, AdBid.LOSS_REASON_LOWER_THAN_HIGHEST_PRICE);
                    Log.iv(Log.TAG, getSdkName() + " bid loss platform : " + platform + ", type : " + adType + " , first : " + firstNetwork + "|" + firstPrice + " , second : " + secondNetwork + "|" + secondPrice);
                }
            }
        }
    }
}
