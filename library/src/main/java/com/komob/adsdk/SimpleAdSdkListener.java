package com.komob.adsdk;

/**
 * Created by Administrator on 2018/2/11.
 */

public class SimpleAdSdkListener implements OnAdSdkListener {
    @Override
    public void onRequest(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onLoaded(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onLoading(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onShow(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onImpression(String placeName, String source, String adType, String network, String pid, String sceneName) {
    }

    @Override
    public void onClick(String placeName, String source, String adType, String network, String pid) {
    }

    @Override
    public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
    }

    @Override
    public void onLoadFailed(String placeName, String source, String adType, String pid, AdError error) {
    }

    @Override
    public void onShowFailed(String placeName, String source, String adType, String pid, AdError error) {
    }

    @Override
    public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
    }

    @Override
    public void onCompleted(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onStarted(String placeName, String source, String adType, String pid) {
    }

    @Override
    public void onUpdate(String placeName, String source, String adType, String pid) {
    }
}
