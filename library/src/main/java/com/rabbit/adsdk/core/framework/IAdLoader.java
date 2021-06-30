package com.rabbit.adsdk.core.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.listener.OnAdSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public interface IAdLoader {

    void init();

    void setAdPlaceConfig(AdPlace adPlace);

    void setOriginPlaceName(String placeName);

    boolean needReload(AdPlace adPlace);

    String getPlaceName();

    void setOnAdSdkListener(OnAdSdkListener l, boolean loaded);

    OnAdSdkListener getOnAdSdkListener();

    boolean isInterstitialLoaded();

    void loadInterstitial(Activity activity);

    void showInterstitial();

    boolean isRewardedVideoLoaded();

    void loadRewardedVideo(Activity activity);

    void showRewardedVideo();

    boolean isAdViewLoaded();

    void loadAdView(AdParams adParams);

    void showAdView(ViewGroup adContainer, AdParams adParams);

    boolean isComplexAdsLoaded();

    String getLoadedType();

    String getLoadedSdk();

    void loadComplexAds(AdParams adParams);

    boolean showComplexAds();

    int getAdCount();

    int getLoadedAdCount();

    String getAdMode();

    boolean isLoading();

    void resume();

    void pause();

    void destroy();
}
