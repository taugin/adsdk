package com.hauyu.adsdk.adloader.listener;

import com.hauyu.adsdk.AdReward;

/**
 * Listener for AdPlaceLoader
 */

public interface OnAdSdkInternalListener {
    void onRequest(String placeName, String source, String adType, String pid);

    void onLoaded(String placeName, String source, String adType, String pid);

    void onLoading(String placeName, String source, String adType, String pid);

    void onShow(String placeName, String source, String adType, String pid);

    void onImp(String placeName, String source, String adType, String network, String pid);

    void onClick(String placeName, String source, String adType, String network, String pid);

    void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds);

    void onLoadFailed(String placeName, String source, String adType, String pid, int error, String msg);

    void onShowFailed(String placeName, String source, String adType, String pid, int error, String msg);

    void onRewarded(String placeName, String source, String adType, String pid, AdReward item);

    void onCompleted(String placeName, String source, String adType, String pid);

    void onStarted(String placeName, String source, String adType, String pid);

    void onUpdate(String placeName, String source, String adType, String pid);
}