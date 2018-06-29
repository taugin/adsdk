package com.inner.adsdk.adloader.listener;

import com.inner.adsdk.AdReward;

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

    public void onRewarded(AdReward reward);

    public void onRewardedVideoAdClosed();

    public void onRewardedVideoAdFailedToLoad();

    public void onRewardedVideoAdClicked();

    public void onRewardedVideoAdLoaded();

    public void onRewardedVideoAdShowed();

    public void onRewardedVideoCompleted();

    public void onRewardedVideoStarted();
}
