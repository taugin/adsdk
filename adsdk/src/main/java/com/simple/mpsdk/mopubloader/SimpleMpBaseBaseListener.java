package com.simple.mpsdk.mopubloader;

import com.simple.mpsdk.RewardItem;
import com.simple.mpsdk.internallistener.IManagerListener;
import com.simple.mpsdk.internallistener.ISdkLoader;
import com.simple.mpsdk.internallistener.OnMpBaseListener;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.listener.OnMpSdkListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleMpBaseBaseListener implements OnMpBaseListener {

    private OnMpSdkListener mOnMpSdkListener;
    private IManagerListener listener;
    private String adType;
    private String placeName;
    private String pidName;

    public SimpleMpBaseBaseListener(String placeName, String adType, String pidName, IManagerListener l) {
        this.adType = adType;
        this.placeName = placeName;
        this.pidName = pidName;
        this.listener = l;
        if (l != null) {
            mOnMpSdkListener = l.getOnAdSdkListener();
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
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onAdFailed(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnMpSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnMpSdkListener.onLoading(placeName, adType);
            } else {
                mOnMpSdkListener.onError(placeName, adType);
                mOnMpSdkListener.onError(placeName, adType, error);
            }
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded(ISdkLoader loader) {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        /**
         * 错误回调不需要判断是否是当前loader
         */
        if (mOnMpSdkListener != null) {
            if (error == Constant.AD_ERROR_LOADING) {
                mOnMpSdkListener.onLoading(placeName, adType);
            } else {
                mOnMpSdkListener.onError(placeName, adType);
                mOnMpSdkListener.onError(placeName, adType, error);
            }
        }
    }

    @Override
    public void onRewarded(RewardItem reward) {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onRewarded(placeName, adType, reward);
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onDismiss(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onClick(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded(ISdkLoader loader) {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onLoaded(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoAdShowed() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onShow(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onCompleted(placeName, adType);
        }
    }

    @Override
    public void onRewardedVideoStarted() {
        if (mOnMpSdkListener != null && isCurrent()) {
            mOnMpSdkListener.onStarted(placeName, adType);
        }
    }
}
