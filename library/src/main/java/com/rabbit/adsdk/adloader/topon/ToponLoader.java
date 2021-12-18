package com.rabbit.adsdk.adloader.topon;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.NativeAd;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToponLoader extends AbstractSdkLoader {

    private static AtomicBoolean sAtomicBoolean = new AtomicBoolean(false);
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
                    mNativeAd = mATNative.getNativeAd(getAdPlaceName());
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
                notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : NativeAd show exception");
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
