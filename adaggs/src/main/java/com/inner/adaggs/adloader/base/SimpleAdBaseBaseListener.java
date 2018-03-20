package com.inner.adaggs.adloader.base;

import com.inner.adaggs.adloader.listener.OnAdBaseListener;
import com.inner.adaggs.listener.OnAdAggsListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdBaseBaseListener implements OnAdBaseListener {

    private OnAdAggsListener mOnAdAggsListener;
    private String source;
    private String adType;
    private String pidName;

    public SimpleAdBaseBaseListener() {
    }

    public SimpleAdBaseBaseListener(String pidName, String source, String adType, OnAdAggsListener l) {
        this.source = source;
        this.adType = adType;
        this.pidName = pidName;
        mOnAdAggsListener = l;
    }

    @Override
    public void onAdLoaded() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onAdShow() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdClick() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onAdDismiss() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onAdFailed() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onError(pidName, source, adType);
        }
    }

    @Override
    public void onAdImpression() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onAdOpened() {
    }

    @Override
    public void onInterstitialLoaded() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onLoaded(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onClick(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onDismiss(pidName, source, adType);
        }
    }

    @Override
    public void onInterstitialError() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onError(pidName, source, adType);
        }
    }
}
