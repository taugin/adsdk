package com.simple.mpsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.simple.mpsdk.MpParams;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.listener.OnMpSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public class AbCoreLoader implements ICoreLoader {
    @Override
    public void init() {
    }

    @Override
    public void setAdPlaceConfig(MpPlace mpPlace) {
    }

    @Override
    public boolean needReload(MpPlace mpPlace) {
        return false;
    }

    @Override
    public void setOnAdSdkListener(OnMpSdkListener l) {
    }

    @Override
    public boolean isInterstitialLoaded() {
        return false;
    }

    @Override
    public void loadInterstitial(Activity activity) {
    }

    @Override
    public void showInterstitial() {
    }

    @Override
    public boolean isBannerLoaded() {
        return false;
    }

    @Override
    public void loadBanner(MpParams mpParams) {

    }

    @Override
    public void showBanner(ViewGroup adContainer, MpParams mpParams) {

    }

    @Override
    public boolean isNativeLoaded() {
        return false;
    }

    @Override
    public void loadNative(MpParams mpParams) {

    }

    @Override
    public void showNative(ViewGroup adContainer, MpParams mpParams) {

    }

    @Override
    public boolean isCommonViewLoaded() {
        return false;
    }

    @Override
    public void loadCommonView(MpParams mpParams) {

    }

    @Override
    public void showCommonView(ViewGroup adContainer, MpParams mpParams) {

    }

    @Override
    public void loadRewardVideo(Activity activity) {
    }

    @Override
    public void showRewardVideo() {
    }

    @Override
    public boolean isRewardVideoLoaded() {
        return false;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }
}
