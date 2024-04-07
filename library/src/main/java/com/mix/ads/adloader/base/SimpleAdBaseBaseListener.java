package com.mix.ads.adloader.base;

import com.mix.ads.MiError;
import com.mix.ads.MiReward;
import com.mix.ads.adloader.listener.IManagerListener;
import com.mix.ads.adloader.listener.ISdkLoader;
import com.mix.ads.adloader.listener.OnAdBaseListener;
import com.mix.ads.adloader.listener.OnAdSdkInternalListener;
import com.mix.ads.constant.Constant;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private IManagerListener listener;
    private String source;
    private String adType;
    private String placeName;
    private String pid;

    public SimpleAdBaseBaseListener(String placeName, String source, String adType, String pid, IManagerListener l) {
        this.source = source;
        this.adType = adType;
        this.placeName = placeName;
        this.pid = pid;
        this.listener = l;
    }

    private OnAdSdkInternalListener getOnAdPlaceLoaderListener() {
        if (listener != null) {
            return listener.getOnAdPlaceLoaderListener();
        }
        return null;
    }

    @Override
    public void onAdRequest() {
        // 所有加载成功的loader都将通知给adplaceloader
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onRequest(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdLoaded(ISdkLoader loader) {
        // 所有加载成功的loader都将通知给adplaceloader
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            long costTime = 0;
            String network = null;
            double revenue = 0;
            boolean cached = false;
            if (loader != null) {
                costTime = loader.getCostTime();
                network = loader.getNetwork();
                revenue = loader.getRevenue();
                cached = loader.isCached();
            }
            placeLoaderListener.onLoaded(placeName, source, adType, pid, network, revenue, costTime, cached);
        }
    }

    @Override
    public void onAdShow() {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onShow(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdImp(String network, String sceneName) {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onImpression(placeName, source, adType, network, pid, sceneName);
        }
    }

    @Override
    public void onAdClick(String network) {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onClick(placeName, source, adType, network, pid);
        }
    }

    @Override
    public void onAdDismiss(boolean complexAds) {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onDismiss(placeName, source, adType, pid, false);
        }
    }

    @Override
    public void onAdLoadFailed(MiError error, String msg) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType, pid);
            } else {
                placeLoaderListener.onLoadFailed(placeName, source, adType, pid, error, msg);
            }
        }
    }

    @Override
    public void onAdShowFailed(MiError error, String msg) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onShowFailed(placeName, source, adType, pid, error, msg);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onRewarded(MiReward reward) {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onRewarded(placeName, source, adType, pid, reward);
        }
    }

    @Override
    public void onRewardAdsCompleted() {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onCompleted(placeName, source, adType, pid);
        }
    }

    @Override
    public void onRewardAdsStarted() {
        OnAdSdkInternalListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onStarted(placeName, source, adType, pid);
        }
    }
}
