package com.rabbit.adsdk.listener;

import com.rabbit.adsdk.AdImpData;

public interface OnAdEventListener {
    void onRequest(String placeName, String sdkName, String adType, String pid);
    void onLoaded(String placeName, String sdkName, String adType, String pid);
    void onLoadFailed(String placeName, String sdkName, String adType, String pid);
    void onShow(String placeName, String sdkName, String adType, String pid);
    void onShowFailed(String placeName, String sdkName, String adType, String pid, String msg);
    void onImp(String placeName, String sdkName, String adType, String pid);
    void onClick(String placeName, String sdkName, String adType, String pid, String requestId);
    void onDismiss(String placeName, String sdkName, String adType, String pid);
    void onAdImpression(AdImpData adImpData);
}
