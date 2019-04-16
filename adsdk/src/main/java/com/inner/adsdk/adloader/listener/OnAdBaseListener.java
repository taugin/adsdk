package com.inner.adsdk.adloader.listener;

import com.inner.adsdk.AdReward;

/**
 * Listener for ad sdk
 */

public interface OnAdBaseListener {
    /**
     * banner or native loaded
     */
    public void onAdLoaded(ISdkLoader loader);

    /**
     * banner or native show
     */
    public void onAdShow();

    /**
     * banner or native click
     */
    public void onAdClick();

    /**
     * banner or native dismiss
     */
    public void onAdDismiss();

    /**
     * banner or native fail
     */
    public void onAdFailed(int error);

    /**
     * banner or native impression
     */
    public void onAdImpression();

    /**
     * banner or native opened
     */
    public void onAdOpened();

    /**
     * interstitial load
     */
    public void onInterstitialLoaded(ISdkLoader loader);

    /**
     * interstitial show
     */
    public void onInterstitialShow();

    /**
     * interstitial click
     */
    public void onInterstitialClick();

    /**
     * interstitial dismiss
     */
    public void onInterstitialDismiss();

    /**
     * interstitial or reward video error
     */
    public void onInterstitialError(int error);

    /**
     * reward
     */
    public void onRewarded(AdReward reward);

    /**
     * reward close
     */
    public void onRewardedVideoAdClosed();

    /**
     * reward click
     */
    public void onRewardedVideoAdClicked();

    /**
     * reward load
     */
    public void onRewardedVideoAdLoaded(ISdkLoader loader);

    /**
     * reward show
     */
    public void onRewardedVideoAdShowed();

    /**
     * reward complete
     */
    public void onRewardedVideoCompleted();

    /**
     * reward start
     */
    public void onRewardedVideoStarted();
}
