package com.inner.adsdk.adloader.base;

import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.OnAdSdkListener;
import com.inner.adsdk.log.Log;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mAdPlaceLoaderListener;
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
        if (l != null) {
            mOnAdSdkListener = l.getOnAdSdkListener();
            mAdPlaceLoaderListener = l.getOnAdPlaceLoaderListener();
        }
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

    @Override
    public void onAdLoaded() {
        if (hasNotifyLoaded()) {
            Log.v(Log.TAG, "has notify loaded ******************");
            return;
        }
        notifyAdLoaded();
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(placeName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onAdFailed(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnAdSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnAdSdkListener.onLoading(placeName, source, adType);
            } else {
                mOnAdSdkListener.onError(placeName, source, adType);
            }
        }
        if (mAdPlaceLoaderListener != null) {
            if (error  == Constant.AD_ERROR_LOADING) {
                mAdPlaceLoaderListener.onLoading(placeName, source, adType);
            } else {
                mAdPlaceLoaderListener.onError(placeName, source, adType);
            }
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(placeName, source, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded() {
        if (hasNotifyLoaded()) {
            Log.v(Log.TAG, "has notify loaded");
            return;
        }
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnAdSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnAdSdkListener.onLoading(placeName, source, adType);
            } else {
                mOnAdSdkListener.onError(placeName, source, adType);
            }
        }
        if (mAdPlaceLoaderListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mAdPlaceLoaderListener.onLoading(placeName, source, adType);
            } else {
                mAdPlaceLoaderListener.onError(placeName, source, adType);
            }
        }
    }

    @Override
    public void onRewarded(AdReward reward) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onRewarded(placeName, source, adType, reward);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onRewarded(placeName, source, adType, reward);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onDismiss(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onClick(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onLoaded(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoAdShowed() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onCompleted(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onCompleted(placeName, source, adType);
        }
    }

    @Override
    public void onRewardedVideoStarted() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onStarted(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onStarted(placeName, source, adType);
        }
    }
}
