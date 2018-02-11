package com.inner.adaggs.adloader.listener;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface OnAdListener {
    public void onAdLoaded();

    public void onAdShow();

    public void onAdClick();

    public void onAdDismiss();

    public void onAdFailed();

    public void onAdImpression();

    public void onAdOpened();
}
