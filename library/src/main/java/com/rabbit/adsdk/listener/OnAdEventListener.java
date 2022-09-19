package com.rabbit.adsdk.listener;

import com.rabbit.adsdk.AdImpData;

public interface OnAdEventListener {
    void onRequest(String placeName, String sdkName, String adType, String pid, String requestId);
    void onLoaded(String placeName, String sdkName, String adType, String pid, String requestId);
    void onLoadFailed(String placeName, String sdkName, String adType, String pid, String requestId);
    void onShow(String placeName, String sdkName, String adType, String pid, String requestId);
    void onShowFailed(String placeName, String sdkName, String adType, String pid, String requestId, String msg);
    void onImp(String placeName, String sdkName, String adType, String pid, String requestId);
    void onClick(String placeName, String sdkName, String adType, String pid, String requestId);
    void onDismiss(String placeName, String sdkName, String adType, String pid, String requestId);
    void onAdImpression(AdImpData adImpData);
}
