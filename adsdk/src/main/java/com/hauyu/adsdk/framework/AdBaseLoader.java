package com.hauyu.adsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.config.AdPlace;
import com.hauyu.adsdk.listener.OnAdSdkListener;

import java.util.Map;

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
    public void setOriginPidName(String pidName) {
    }

    @Override
    public void setAdIds(Map<String, String> adids) {
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
    public boolean isAdViewLoaded() {
        return false;
    }

    @Override
    public void loadAdView(AdParams adParams) {
    }

    @Override
    public void showAdView(ViewGroup adContainer, AdParams adParams) {
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
    public void loadComplexAds(AdParams adParams) {
    }

    @Override
    public void showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType) {
    }

    @Override
    public void showComplexAds() {
    }

    @Override
    public int getAdCount() {
        return 0;
    }

    @Override
    public String getAdMode() {
        return null;
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