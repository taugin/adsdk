package com.simple.mpsdk.listener;

import com.simple.mpsdk.RewardItem;

/**
 * Created by Administrator on 2018/2/11.
 */

public class SimpleMpSdkListener implements OnMpSdkListener {
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
    public void onRewarded(String pidName, String adType, RewardItem item) {
    }

    @Override
    public void onCompleted(String pidName, String adType) {
    }

    @Override
    public void onStarted(String pidName, String adType) {
    }
}
