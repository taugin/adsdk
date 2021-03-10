package com.rabbit.adsdk.adloader.base;

import android.text.TextUtils;

import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.AdPlaceLoader;
import com.rabbit.adsdk.listener.OnAdSdkListener;
import com.rabbit.adsdk.log.Log;

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

    private String getAdMode() {
        if (listener != null) {
            return listener.getAdMode();
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
        String adMode = getAdMode();
        Log.v(Log.TAG, "ad mode : " + adMode);
        if (hasNotifyLoaded() && !TextUtils.equals(adMode, Constant.MODE_QUE)) {
            Log.v(Log.TAG, "has notify loaded ******************");
            return;
        }
        notifyAdLoaded();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onAdShow() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onShow(placeName, source, adType);
        }
    }

    @Override
    public void onAdImp() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType, false);
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
                placeLoaderListener.onLoading(placeName, source, adType);
            } else {
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
        String adMode = getAdMode();
        Log.v(Log.TAG, "ad mode : " + adMode);
        if (hasNotifyLoaded() && !TextUtils.equals(adMode, Constant.MODE_QUE)) {
            Log.v(Log.TAG, "has notify loaded ++++++++++++++++++");
            return;
        }
        notifyAdLoaded();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialImp() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType, false);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType);
            } else {
                placeLoaderListener.onError(placeName, source, adType, error);
            }
        }
    }

    @Override
    public void onRewarded(AdReward reward) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onRewarded(placeName, source, adType, reward);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onDismiss(placeName, source, adType, false);
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded(ISdkLoader loader) {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        String adMode = getAdMode();
        Log.v(Log.TAG, "ad mode : " + adMode);
        if (hasNotifyLoaded() && !TextUtils.equals(adMode, Constant.MODE_QUE)) {
            Log.v(Log.TAG, "has notify loaded ------------------");
            return;
        }
        notifyAdLoaded();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                placeLoaderListener.onLoading(placeName, source, adType);
            } else {
                placeLoaderListener.onError(placeName, source, adType, error);
            }
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onImp(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onCompleted(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoStarted() {
        OnAdSdkListener placeLoaderListener = getOnAdPlaceLoaderListener();
        if (placeLoaderListener != null && isCurrent()) {
            placeLoaderListener.onStarted(placeName, source, adType);
        }
    }
}
