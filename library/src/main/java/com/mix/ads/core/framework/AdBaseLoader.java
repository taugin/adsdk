package com.mix.ads.core.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.mix.ads.MiParams;
import com.mix.ads.data.config.AdPlace;
import com.mix.ads.OnAdSdkListener;

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
    public void setOriginPlaceName(String placeName) {
    }

    @Override
    public String getPlaceName() {
        return null;
    }

    @Override
    public boolean needReload(AdPlace adPlace) {
        return false;
    }

    @Override
    public void setOnAdSdkListener(OnAdSdkListener l, boolean loaded) {
    }

    @Override
    public OnAdSdkListener getOnAdSdkListener() {
        return null;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return false;
    }

    @Override
    public void loadInterstitial(Activity activity) {
    }

    @Override
    public void showInterstitial(String sceneName) {
    }

    @Override
    public boolean isRewardedVideoLoaded() {
        return false;
    }

    @Override
    public void loadRewardedVideo(Activity activity) {
    }

    @Override
    public void showRewardedVideo(String sceneName) {
    }

    @Override
    public boolean isAdViewLoaded(String adType) {
        return false;
    }

    @Override
    public void loadAdView(MiParams miParams) {
    }

    @Override
    public void showAdView(ViewGroup adContainer, String adType, MiParams miParams) {
    }

    @Override
    public boolean isSplashLoaded() {
        return false;
    }

    @Override
    public void loadSplash(Activity activity) {
    }

    @Override
    public void showSplash(ViewGroup viewGroup, String sceneName) {
    }

    @Override
    public boolean isComplexAdsLoaded() {
        return false;
    }

    @Override
    public String getLoadedType() {
        return null;
    }

    @Override
    public String getLoadedSdk() {
        return null;
    }

    @Override
    public void loadComplexAds(MiParams miParams) {
    }

    @Override
    public boolean showComplexAds(String sceneName) {
        return false;
    }

    @Override
    public int getAdCount() {
        return 0;
    }

    @Override
    public int getLoadedAdCount() {
        return 0;
    }

    @Override
    public String getAdMode() {
        return null;
    }

    @Override
    public double getMaxRevenue(String adType, boolean containSlave) {
        return 0;
    }

    @Override
    public boolean isLoading() {
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
