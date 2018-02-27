package com.inner.adaggs.adloader.listener;

import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.OnAdAggsListener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleInterstitialListener implements OnInterstitialListener {

    private OnAdAggsListener mOnAdAggsListener;
    private String source;

    public SimpleInterstitialListener() {
    }

    public SimpleInterstitialListener(String source, OnAdAggsListener l) {
        this.source = source;
        mOnAdAggsListener = l;
    }

    @Override
    public void onInterstitialLoaded() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onLoaded(source, Constant.TYPE_INTERSTITIAL);
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onShow(source, Constant.TYPE_INTERSTITIAL);
        }
    }

    @Override
    public void onInterstitialClick() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onClick(source, Constant.TYPE_INTERSTITIAL);
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onDismiss(source, Constant.TYPE_INTERSTITIAL);
        }
    }

    @Override
    public void onInterstitialError() {
        if (mOnAdAggsListener != null) {
            mOnAdAggsListener.onError(source, Constant.TYPE_INTERSTITIAL);
        }
    }
}
