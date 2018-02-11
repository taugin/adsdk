package com.inner.adaggs.adloader.listener;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface OnInterstitialListener {
    public void onInterstitialLoaded();

    public void onInterstitialShow();

    public void onInterstitialDismiss();

    public void onInterstitialError();
}
