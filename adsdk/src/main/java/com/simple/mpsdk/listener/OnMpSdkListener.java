package com.simple.mpsdk.listener;

import com.simple.mpsdk.RewardItem;

/**
 * Listener for CoreLoader
 */

public interface OnMpSdkListener {
    void onLoaded(String pidName, String adType);
    void onLoading(String pidName, String adType);
    void onShow(String pidName, String adType);
    void onClick(String pidName, String adType);
    void onDismiss(String pidName, String adType);
    void onError(String pidName, String adType);
    void onError(String pidName, String adType, int errorCode);
    void onRewarded(String pidName, String adType, RewardItem item);
    void onCompleted(String pidName, String adType);
    void onStarted(String pidName, String adType);
}
