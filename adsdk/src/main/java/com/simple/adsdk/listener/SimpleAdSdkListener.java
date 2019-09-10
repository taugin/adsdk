package com.simple.adsdk.listener;

import com.simple.adsdk.AdReward;

/**
 * Created by Administrator on 2018/2/11.
 */

public class SimpleAdSdkListener implements OnAdSdkListener {
    @Override
    public void onLoaded(String pidName, String adType) {
    }

    @Override
    public void onLoading(String pidName, String adType) {
    }

    @Override
    public void onShow(String pidName, String adType) {
    }

    @Override
    public void onClick(String pidName, String adType) {
    }

    @Override
    public void onDismiss(String pidName, String adType) {
    }

    @Override
    public void onError(String pidName, String adType) {
    }

    @Override
    public void onError(String pidName, String adType, int errorCode) {
    }

    @Override
    public void onRewarded(String pidName, String adType, AdReward item) {
    }

    @Override
    public void onCompleted(String pidName, String adType) {
    }

    @Override
    public void onStarted(String pidName, String adType) {
    }
}
