package com.inner.adsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.inner.adsdk.AdParams;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.listener.OnAdSdkListener;

import java.util.Map;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public interface IAdLoader {

    public void init();

    public void setAdPlaceConfig(AdPlace adPlace);

    public void setOriginPidName(String pidName);

    public boolean needReload(AdPlace adPlace);

    public void setAdIds(Map<String, String> adids);
    public void setOnAdSdkListener(OnAdSdkListener l, boolean loaded);

    public boolean isInterstitialLoaded();
    public void loadInterstitial(Activity activity);
    public void showInterstitial();

    public boolean isAdViewLoaded();
    public void loadAdView(AdParams adParams);
    public void showAdView(ViewGroup adContainer, AdParams adParams);

    public boolean isComplexAdsLoaded();
    public String getLoadedType();
    public void loadComplexAds(AdParams adParams);
    public boolean showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType);

    public boolean showComplexAds();

    public int getAdCount();
    public String getAdMode();
    public boolean isLoading();
    public void resume();
    public void pause();

    public void setQueueRunning(boolean running);

    public void destroy();
}
