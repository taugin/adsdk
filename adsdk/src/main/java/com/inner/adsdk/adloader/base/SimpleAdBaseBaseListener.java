package com.inner.adsdk.adloader.base;

import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.listener.OnAdSdkListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mAdPlaceLoaderListener;
    private IManagerListener listener;
    private String source;
    private String adType;
    private String pidName;

    public SimpleAdBaseBaseListener(String pidName, String source, String adType, IManagerListener l) {
        this.source = source;
        this.adType = adType;
        this.pidName = pidName;
        this.listener = l;
        if (l != null) {
            mOnAdSdkListener = l.getOnAdSdkListener();
            mAdPlaceLoaderListener = l.getOnAdPlaceLoaderListener();
        }
    }

    private boolean isCurrent() {
        if (listener != null) {
            return listener.isCurrent(source, adType);
        }
        return false;
    }

    @Override
    public void onAdLoaded() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onAdFailed(int error) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onError(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onError(pidName, source, adType);
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onLoaded(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onShow(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onClick(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onDismiss(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialError(int error) {
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onError(pidName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onError(pidName, source, adType);
        }
    }
}
