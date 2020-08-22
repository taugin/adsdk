package com.hauyu.adsdk.listener;

import com.hauyu.adsdk.AdReward;

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
    public void onImp(String pidName, String source, String adType) {
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
    public void onError(String pidName, String source, String adType, int errorCode) {
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

    @Override
    public void onUpdate(String pidName, String source, String adType) {
    }
}
