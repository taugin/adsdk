package com.mix.ads.adloader.listener;

import com.mix.ads.MiError;
import com.mix.ads.MiReward;

/**
 * Listener for ad sdk
 */

public interface OnAdBaseListener {

    /**
     * ad request
     */
    public void onAdRequest();

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
    public void onAdImp(String network, String sceneName);

    /**
     * banner or native click
     */
    public void onAdClick(String network);

    /**
     * banner or native dismiss
     */
    public void onAdDismiss(boolean complexAds);

    /**
     * banner or native fail
     */
    public void onAdLoadFailed(MiError error, String msg);

    /**
     * banner or native show failed
     */
    public void onAdShowFailed(MiError error, String msg);

    /**
     * banner or native opened
     */
    public void onAdOpened();

    /**
     * reward
     */
    public void onRewarded(MiReward reward);

    /**
     * reward complete
     */
    public void onRewardAdsCompleted();

    /**
     * reward start
     */
    public void onRewardAdsStarted();
}