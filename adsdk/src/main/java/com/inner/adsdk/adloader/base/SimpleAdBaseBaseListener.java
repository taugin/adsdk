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

    @Override
    public void onAdLoaded() {
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
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onError(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onError(placeName, source, adType);
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
        if (mOnAdSdkListener != null && isCurrent()) {
            mOnAdSdkListener.onError(placeName, source, adType);
        }
        if (mAdPlaceLoaderListener != null && isCurrent()) {
            mAdPlaceLoaderListener.onError(placeName, source, adType);
        }
    }
}
