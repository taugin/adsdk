package com.inner.adsdk.listener;

/**
 * Listener for AdPlaceLoader
 */

public interface OnAdSdkListener {
    public void onLoaded(String pidName, String source, String adType);
    public void onShow(String pidName, String source, String adType);
    public void onClick(String pidName, String source, String adType);
    public void onDismiss(String pidName, String source, String adType);
    public void onError(String pidName, String source, String adType);
}
