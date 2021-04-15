package com.rabbit.adsdk.adloader.base;

import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.listener.OnAdSdkListener;

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

    private OnAdSdkListener getOnAdPlaceLoaderListener() {
        if (listener != null) {
            return listener.getOnAdPlaceLoaderListener();
        }
        return null;
    }

    @Override
    public void onAdLoaded(ISdkLoader loader) {
        // 所有加载成功的loader都将通知给adplaceloader
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onLoaded(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdShow() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onShow(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdImp() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onImp(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdClick() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onClick(placeName, source, adType, pid);
        }
    }

    @Override
    public void onAdDismiss(boolean complexAds) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onDismiss(placeName, source, adType, pid, false);
        }
    }

    @Override
    public void onAdFailed(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType, pid);
            } else {
                placeLoaderListener.onError(placeName, source, adType, pid, error);
            }
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onRewarded(AdReward reward) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onRewarded(placeName, source, adType, pid, reward);
        }
    }

    @Override
    public void onRewardAdsCompleted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onCompleted(placeName, source, adType, pid);
        }
    }

    @Override
    public void onRewardAdsStarted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            placeLoaderListener.onStarted(placeName, source, adType, pid);
        }
    }
}
