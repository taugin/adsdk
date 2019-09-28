package com.hauyu.adsdk.adloader.
listener;

import com.hauyu.adsdk.AdReward;
/**
 * Listener for ad sdk
 */

public interface OnAdBaseListener {
    /**
     * banner or native loaded
     */
    public void onAdLoaded(ISdkLoader loader);

    /**
     * banner or native impression
     */
    public void onAdImp();

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
     * banner or native opened
     */
    public void onAdOpened();

    /**
     * interstitial load
     */
    public void onInterstitialLoaded(ISdkLoader loader);

    /**
     * interstitial impression
     */
    public void onInterstitialImp();

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
     * reward opened
     */
    public void onRewardedVideoAdOpened();

    /**
     * reward complete
     */
    public void onRewardedVideoCompleted();

    /**
     * reward start
     */
    public void onRewardedVideoStarted();
}
