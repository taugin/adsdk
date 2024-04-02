package com.mix.ads.core.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.mix.ads.MiParams;
import com.mix.ads.data.config.AdPlace;
import com.mix.ads.OnAdSdkListener;

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

    void loadAdView(MiParams miParams);

    void showAdView(ViewGroup adContainer, String adType, MiParams miParams);

    boolean isSplashLoaded();

    void loadSplash(Activity activity);

    void showSplash(ViewGroup viewGroup, String sceneName);

    boolean isComplexAdsLoaded();

    String getLoadedType();

    String getLoadedSdk();

    void loadComplexAds(MiParams miParams);

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
