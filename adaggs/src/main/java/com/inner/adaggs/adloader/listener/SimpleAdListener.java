package com.inner.adaggs.adloader.listener;

import com.inner.adaggs.listener.OnAdAggsListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdListener implements OnAdListener {

    private OnAdAggsListener mOnAdAggsListener;
    private String source;
    private String adType;

    public SimpleAdListener() {
    }

    public SimpleAdListener(String source, String adType, OnAdAggsListener l) {
        this.source = source;
        this.adType = adType;
        mOnAdAggsListener = l;
    }

    @Override
    public void onAdLoaded() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onLoaded(source, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(source, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onClick(source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onDismiss(source, adType);
        }
    }

    @Override
    public void onAdFailed() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onError(source, adType);
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(source, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }
}
