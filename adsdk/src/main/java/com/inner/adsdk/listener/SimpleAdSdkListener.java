package com.inner.adsdk.listener;

import com.inner.adsdk.AdReward;

/**
 * Created by Administrator on 2018/2/11.
 */

public class SimpleAdSdkListener implements OnAdSdkListener {
    @Override
    public void onLoaded(String pidName, String source, String adType) {
    }

    @Override
    public void onLoading(String pidName, String source, String adType) {
    }

    @Override
    public void onShow(String pidName, String source, String adType) {
    }

    @Override
    public void onClick(String pidName, String source, String adType) {
    }

    @Override
    public void onDismiss(String pidName, String source, String adType) {
    }

    @Override
    public void onError(String pidName, String source, String adType) {
    }

    @Override
    public void onRewarded(String pidName, String source, String adType, AdReward item) {
    }

    @Override
    public void onCompleted(String pidName, String source, String adType) {
    }

    @Override
    public void onStarted(String pidName, String source, String adType) {
    }
}
