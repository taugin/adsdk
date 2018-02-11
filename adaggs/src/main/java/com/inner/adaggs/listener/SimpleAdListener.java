package com.inner.adaggs.listener;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SimpleAdListener implements OnAdListener {

    private SimpleAdListener mSimpleAdListener;
    public SimpleAdListener() {
    }

    public SimpleAdListener(SimpleAdListener l) {
        mSimpleAdListener = l;
    }

    @Override
    public void onAdLoaded() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdLoaded();
        }
    }

    @Override
    public void onAdShow() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdShow();
        }
    }

    @Override
    public void onAdClick() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdClick();
        }
    }

    @Override
    public void onAdDismiss() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdDismiss();
        }
    }

    @Override
    public void onAdFailed() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdFailed();
        }
    }

    @Override
    public void onAdImpression() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdImpression();
        }
    }

    @Override
    public void onAdOpened() {
        if (mSimpleAdListener != null) {
            mSimpleAdListener.onAdOpened();
        }
    }
}
