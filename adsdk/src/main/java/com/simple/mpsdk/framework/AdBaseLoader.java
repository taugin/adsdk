package com.simple.mpsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.simple.mpsdk.AdParams;
import com.simple.mpsdk.config.AdPlace;
import com.simple.mpsdk.listener.OnAdSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public class AdBaseLoader implements IAdLoader {
    @Override
    public void init() {
    }

    @Override
    public void setAdPlaceConfig(AdPlace adPlace) {
    }

    @Override
    public boolean needReload(AdPlace adPlace) {
        return false;
    }

    @Override
    public void setOnAdSdkListener(OnAdSdkListener l) {
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
    public void loadBanner(AdParams adParams) {

    }

    @Override
    public void showBanner(ViewGroup adContainer, AdParams adParams) {

    }

    @Override
    public boolean isNativeLoaded() {
        return false;
    }

    @Override
    public void loadNative(AdParams adParams) {

    }

    @Override
    public void showNative(ViewGroup adContainer, AdParams adParams) {

    }

    @Override
    public boolean isCommonViewLoaded() {
        return false;
    }

    @Override
    public void loadCommonView(AdParams adParams) {

    }

    @Override
    public void showCommonView(ViewGroup adContainer, AdParams adParams) {

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
