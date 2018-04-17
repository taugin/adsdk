package com.inner.adsdk.adloader.base;

import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.listener.OnAdSdkListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private OnAdSdkListener mOnAdSdkListener;
    private String source;
    private String adType;
    private String pidName;

    public SimpleAdBaseBaseListener() {
    }

    public SimpleAdBaseBaseListener(String pidName, String source, String adType, OnAdSdkListener l) {
        this.source = source;
        this.adType = adType;
        this.pidName = pidName;
        mOnAdSdkListener = l;
    }

    @Override
    public void onAdLoaded() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onAdFailed() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onError(pidName, source, adType);
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialError() {
        if (mOnAdSdkListener != null) {
            mOnAdSdkListener.onError(pidName, source, adType);
        }
    }
}
