package com.rabbit.adsdk.listener;

import com.rabbit.adsdk.AdReward;

/**
 * Created by Administrator on 2018/2/11.
 */

public class SimpleAdSdkListener implements OnAdSdkListener {
    @Override
    public void onLoaded(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onLoading(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onShow(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onImp(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onClick(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onDismiss(String pidName, String source, String adType, String pid, boolean complexAds) {
    }

    @Override
    public void onError(String pidName, String source, String adType, String pid, int error) {
    }

    @Override
    public void onRewarded(String pidName, String source, String adType, String pid, AdReward item) {
    }

    @Override
    public void onCompleted(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onStarted(String pidName, String source, String adType, String pid) {
    }

    @Override
    public void onUpdate(String pidName, String source, String adType, String pid) {
    }
}
