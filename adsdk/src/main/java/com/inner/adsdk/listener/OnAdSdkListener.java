package com.inner.adsdk.listener;

import com.inner.adsdk.AdReward;

/**
 * Listener for AdPlaceLoader
 */

public interface OnAdSdkListener {
    public void onLoaded(String pidName, String source, String adType);
    public void onLoading(String pidName, String source, String adType);
    public void onShow(String pidName, String source, String adType);
    public void onClick(String pidName, String source, String adType);
    public void onDismiss(String pidName, String source, String adType);
    public void onError(String pidName, String source, String adType);
    public void onError(String pidName, String source, String adType, int errorCode);
    public void onRewarded(String pidName, String source, String adType, AdReward item);
    public void onCompleted(String pidName, String source, String adType);
    public void onStarted(String pidName, String source, String adType);
}
