package com.inner.adsdk.adloader.base;

import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.ISdkLoader;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.OnAdSdkListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private OnAdSdkListener mOnAdSdkListener;
    private IManagerListener listener;
    private String adType;
    private String placeName;
    private String pidName;

    public SimpleAdBaseBaseListener(String placeName, String adType, String pidName, IManagerListener l) {
        this.adType = adType;
        this.placeName = placeName;
        this.pidName = pidName;
        this.listener = l;
        if (l != null) {
            mOnAdSdkListener = l.getOnAdSdkListener();
        }
    }

    private boolean isCurrent() {
        if (listener != null) {
            return listener.isCurrent(adType, pidName);
        }
        return false;
    }

    @Override
    public void onAdLoaded(ISdkLoader loader) {
        // 所有加载成功的loader都将通知给adplaceloader
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onAdFailed(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnAdSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnAdSdkListener.onLoading(placeName, adType);
            } else {
                mOnAdSdkListener.onError(placeName, adType);
                mOnAdSdkListener.onError(placeName, adType, error);
            }
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded(ISdkLoader loader) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnAdSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnAdSdkListener.onLoading(placeName, adType);
            } else {
                mOnAdSdkListener.onError(placeName, adType);
                mOnAdSdkListener.onError(placeName, adType, error);
            }
        }
    }

    @Override
    public void onRewarded(AdReward reward) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onRewarded(placeName, adType, reward);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded(ISdkLoader loader) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdShowed() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onCompleted(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoStarted() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onStarted(placeName, adType);
        }
    }
}
