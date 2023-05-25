package com.hauyu.adsdk;

public interface OnAdEventListener {
    default void onRequest(String placeName, String sdkName, String adType, String pid) {
    }

    default void onLoaded(String placeName, String sdkName, String adType, String pid) {
    }

    default void onLoadFailed(String placeName, String sdkName, String adType, String pid) {
    }

    default void onShow(String placeName, String sdkName, String adType, String pid) {
    }

    default void onShowFailed(String placeName, String sdkName, String adType, String pid, String msg) {
    }

    default void onImp(String placeName, String sdkName, String adType, String pid) {
    }

    default void onClick(String placeName, String sdkName, String adType, String pid, String impressionId) {
    }

    default void onDismiss(String placeName, String sdkName, String adType, String pid) {
    }

    default void onAdImpression(AdImpData adImpData) {
    }
}
