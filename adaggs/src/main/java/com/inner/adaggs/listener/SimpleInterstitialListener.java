package com.inner.adaggs.listener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleInterstitialListener implements OnInterstitialListener {

    private OnInterstitialListener mOnInterstitialListener;

    public SimpleInterstitialListener() {

    }

    public SimpleInterstitialListener(OnInterstitialListener l) {
        mOnInterstitialListener = l;
    }

    @Override
    public void onInterstitialLoaded() {
        if (mOnInterstitialListener != null) {
            mOnInterstitialListener.onInterstitialLoaded();
        }
    }

    @Override
    public void onInterstitialShow() {
        if (mOnInterstitialListener != null) {
            mOnInterstitialListener.onInterstitialShow();
        }
    }

    @Override
    public void onInterstitialDismiss() {
        if (mOnInterstitialListener != null) {
            mOnInterstitialListener.onInterstitialDismiss();
        }
    }

    @Override
    public void onInterstitialError() {
        if (mOnInterstitialListener != null) {
            mOnInterstitialListener.onInterstitialError();
        }
    }
}
