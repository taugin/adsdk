package com.humob.adsdk.core.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.humob.adsdk.AdParams;
import com.humob.adsdk.data.config.AdPlace;
import com.humob.adsdk.OnAdSdkListener;

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

    void showInterstitial(String sceneName);

    boolean isRewardedVideoLoaded();

    void loadRewardedVideo(Activity activity);

    void showRewardedVideo(String sceneName);

    boolean isAdViewLoaded(String adType);

    void loadAdView(AdParams adParams);

    void showAdView(ViewGroup adContainer, String adType, AdParams adParams);

    boolean isSplashLoaded();

    void loadSplash(Activity activity);

    void showSplash(ViewGroup viewGroup, String sceneName);

    boolean isComplexAdsLoaded();

    String getLoadedType();

    String getLoadedSdk();

    void loadComplexAds(AdParams adParams);

    boolean showComplexAds(String sceneName);

    int getAdCount();

    int getLoadedAdCount();

    String getAdMode();

    double getMaxRevenue(String adType, boolean containSlave);

    boolean isLoading();

    void resume();

    void pause();

    void destroy();
}
