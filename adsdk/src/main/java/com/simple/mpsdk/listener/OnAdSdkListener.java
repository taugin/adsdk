package com.simple.mpsdk.listener;

import com.simple.mpsdk.RewardItem;

/**
 * Listener for AdPlaceLoader
 */

public interface OnAdSdkListener {
    public void onLoaded(String pidName, String adType);
    public void onLoading(String pidName, String adType);
    public void onShow(String pidName, String adType);
    public void onClick(String pidName, String adType);
    public void onDismiss(String pidName, String adType);
    public void onError(String pidName, String adType);
    public void onError(String pidName, String adType, int errorCode);
    public void onRewarded(String pidName, String adType, RewardItem item);
    public void onCompleted(String pidName, String adType);
    public void onStarted(String pidName, String adType);
}
