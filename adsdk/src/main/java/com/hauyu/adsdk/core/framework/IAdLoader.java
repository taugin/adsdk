package com.hauyu.adsdk.core.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.listener.OnAdSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public interface IAdLoader {

    public void init();

    public void setAdPlaceConfig(AdPlace adPlace);

    public void setOriginPidName(String pidName);

    public boolean needReload(AdPlace adPlace);

    public String getPlaceName();

    public void setOnAdSdkListener(OnAdSdkListener l, boolean loaded);

    public boolean isInterstitialLoaded();
    public void loadInterstitial(Activity activity);
    public void showInterstitial();

    public boolean isRewardedVideoLoaded();
    public void loadRewardedVideo(Activity activity);
    public void showRewardedVideo();

    public boolean isAdViewLoaded();
    public void loadAdView(AdParams adParams);
    public void showAdView(ViewGroup adContainer, AdParams adParams);

    public boolean isComplexAdsLoaded();
    public String getLoadedType();
    public void loadComplexAds(AdParams adParams);
    public boolean showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType);

    public boolean showComplexAds();

    public int getAdCount();
    public int getLoadedAdCount();
    public String getAdMode();
    public boolean isLoading();
    public boolean isPlaceError();
    public void resume();
    public void pause();

    public void setQueueRunning(boolean running);

    public void destroy();
}
