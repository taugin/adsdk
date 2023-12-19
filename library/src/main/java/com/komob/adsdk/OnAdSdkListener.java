package com.komob.adsdk;

/**
 * Listener for AdPlaceLoader
 */

public interface OnAdSdkListener {
    default void onRequest(String placeName, String source, String adType, String pid) {
    }

    default void onLoaded(String placeName, String source, String adType, String pid) {
    }

    default void onLoading(String placeName, String source, String adType, String pid) {
    }

    default void onShow(String placeName, String source, String adType, String pid) {
    }

    default void onImpression(String placeName, String source, String adType, String network, String pid, String sceneName) {
    }

    default void onClick(String placeName, String source, String adType, String network, String pid) {
    }

    default void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
    }

    default void onLoadFailed(String placeName, String source, String adType, String pid, int error) {
    }

    default void onShowFailed(String placeName, String source, String adType, String pid, int error) {
    }

    default void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
    }

    default void onCompleted(String placeName, String source, String adType, String pid) {
    }

    default void onStarted(String placeName, String source, String adType, String pid) {
    }

    default void onUpdate(String placeName, String source, String adType, String pid) {
    }
}
