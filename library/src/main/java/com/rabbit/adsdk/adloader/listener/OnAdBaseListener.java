package com.rabbit.adsdk.adloader.
        listener;

import com.rabbit.adsdk.AdReward;

/**
 * Listener for ad sdk
 */

public interface OnAdBaseListener {
    /**
     * banner or native loaded
     */
    public void onAdLoaded(ISdkLoader loader);

    /**
     * ad show
     */
    public void onAdShow();

    /**
     * banner or native impression
     */
    public void onAdImp(String render);

    /**
     * banner or native click
     */
    public void onAdClick(String render);

    /**
     * banner or native dismiss
     */
    public void onAdDismiss(boolean complexAds);

    /**
     * banner or native fail
     */
    public void onAdFailed(int error);

    /**
     * banner or native opened
     */
    public void onAdOpened();

    /**
     * reward
     */
    public void onRewarded(AdReward reward);

    /**
     * reward complete
     */
    public void onRewardAdsCompleted();

    /**
     * reward start
     */
    public void onRewardAdsStarted();
}
