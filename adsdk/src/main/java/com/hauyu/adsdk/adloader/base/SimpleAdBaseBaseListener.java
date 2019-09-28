package com.hauyu.adsdk.adloader.base;

import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.adloader.listener.IManagerListener;
import com.hauyu.adsdk.adloader.listener.ISdkLoader;
import com.hauyu.adsdk.adloader.listener.OnAdBaseListener;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.AdPlaceLoader;
import com.hauyu.adsdk.listener.OnAdSdkListener;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private IManagerListener listener;
    private String source;
    private String adType;
    private String placeName;
    private String pidName;

    public SimpleAdBaseBaseListener(String placeName, String source, String adType, String pidName, IManagerListener l) {
        this.source = source;
        this.adType = adType;
        this.placeName = placeName;
        this.pidName = pidName;
        this.listener = l;
    }

    private boolean isCurrent() {
        if (listener != null) {
            return listener.isCurrent(source, adType, pidName);
        }
        return false;
    }

    private boolean hasNotifyLoaded() {
        if (listener != null) {
            return listener.hasNotifyLoaded();
        }
        return false;
    }

    private void notifyAdLoaded() {
        if (listener != null) {
            listener.notifyAdLoaded();
        }
    }

    private OnAdSdkListener getOnAdSdkListener() {
        return getOnAdSdkListener(false);
    }

    private OnAdSdkListener getOnAdSdkListener(boolean loaded) {
        if (listener != null) {
            return listener.getOnAdSdkListener(loaded);
        }
        return null;
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
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        try {
            if (placeLoaderListener instanceof AdPlaceLoader.AdPlaceLoaderListener) {
                ((AdPlaceLoader.AdPlaceLoaderListener) placeLoaderListener).onLoaded(loader);
            }
        } catch (Exception e) {
        }

        if (hasNotifyLoaded()) {
            Log.v(Log.TAG, "has notify loaded ******************");
            return;
        }
        notifyAdLoaded();
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onLoaded(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onLoaded(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onAdImp() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onImp(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onImp(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onClick(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onClick(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onDismiss(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onDismiss(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onAdFailed(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                adSdkListener.onLoading(placeName, source, adType);
            } else {
                adSdkListener.onError(placeName, source, adType);
                adSdkListener.onError(placeName, source, adType, error);
            }
        }
        if (adSdkLoadedListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                adSdkLoadedListener.onLoading(placeName, source, adType);
            } else {
                adSdkLoadedListener.onError(placeName, source, adType);
                adSdkLoadedListener.onError(placeName, source, adType, error);
            }
        }
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType);
            } else {
                placeLoaderListener.onError(placeName, source, adType);
                placeLoaderListener.onError(placeName, source, adType, error);
            }
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded(ISdkLoader loader) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        try {
            if (placeLoaderListener instanceof AdPlaceLoader.AdPlaceLoaderListener) {
                ((AdPlaceLoader.AdPlaceLoaderListener) placeLoaderListener).onLoaded(loader);
            }
        } catch (Exception e) {
        }

        if (hasNotifyLoaded()) {
            Log.v(Log.TAG, "has notify loaded ++++++++++++++++++");
            return;
        }
        notifyAdLoaded();
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onLoaded(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onLoaded(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialImp() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onImp(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onImp(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onClick(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onClick(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onDismiss(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onDismiss(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                adSdkListener.onLoading(placeName, source, adType);
            } else {
                adSdkListener.onError(placeName, source, adType);
                adSdkListener.onError(placeName, source, adType, error);
            }
        }
        if (adSdkLoadedListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                adSdkLoadedListener.onLoading(placeName, source, adType);
            } else {
                adSdkLoadedListener.onError(placeName, source, adType);
                adSdkLoadedListener.onError(placeName, source, adType, error);
            }
        }
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType);
            } else {
                placeLoaderListener.onError(placeName, source, adType);
                placeLoaderListener.onError(placeName, source, adType, error);
            }
        }
    }

    @Override
    public void onRewarded(AdReward reward) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onRewarded(placeName, source, adType, reward);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onRewarded(placeName, source, adType, reward);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onRewarded(placeName, source, adType, reward);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onDismiss(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onDismiss(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onClick(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onClick(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded(ISdkLoader loader) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        try {
            if (placeLoaderListener instanceof AdPlaceLoader.AdPlaceLoaderListener) {
                ((AdPlaceLoader.AdPlaceLoaderListener) placeLoaderListener).onLoaded(loader);
            }
        } catch (Exception e) {
        }

        if (hasNotifyLoaded()) {
            Log.v(Log.TAG, "has notify loaded ------------------");
            return;
        }
        notifyAdLoaded();

        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onLoaded(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onLoaded(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onImp(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onImp(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onCompleted(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onCompleted(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onCompleted(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoStarted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        OnAdSdkListener adSdkListener = getOnAdSdkListener();
        OnAdSdkListener adSdkLoadedListener = getOnAdSdkListener(true);
        if (adSdkListener != null && isCurrent()) {
            adSdkListener.onStarted(placeName, source, adType);
        }
        if (adSdkLoadedListener != null && isCurrent()) {
            adSdkLoadedListener.onStarted(placeName, source, adType);
        }
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onStarted(placeName, source, adType);
        }
    }
}
