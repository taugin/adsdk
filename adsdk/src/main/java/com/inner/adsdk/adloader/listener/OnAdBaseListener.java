package com.inner.adsdk.adloader.listener;

/**
 * Listener for ad sdk
 */

public interface OnAdBaseListener {
    public void onAdLoaded();

    public void onAdShow();

    public void onAdClick();

    public void onAdDismiss();

    public void onAdFailed(int error);

    public void onAdImpression();

    public void onAdOpened();

    public void onInterstitialLoaded();

    public void onInterstitialShow();

    public void onInterstitialClick();

    public void onInterstitialDismiss();

    public void onInterstitialError(int error);
}
