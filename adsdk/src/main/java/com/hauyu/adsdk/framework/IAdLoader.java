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

public interface IAdLoader {

    public void init();

    public void setAdPlaceConfig(AdPlace adPlace);

    public void setOriginPidName(String pidName);

    public boolean needReload(AdPlace adPlace);

    public void setAdIds(Map<String, String> adids);
    public void setOnAdSdkListener(OnAdSdkListener l);

    public boolean isInterstitialLoaded();
    public void loadInterstitial(Activity activity);
    public void showInterstitial();

    public boolean isAdViewLoaded();
    public void loadAdView(AdParams adParams);
    public void showAdView(ViewGroup adContainer);

    public boolean isComplexAdsLoaded();
    public String getLoadedType();
    public void loadComplexAds(AdParams adParams);
    public void showComplexAds(ViewGroup adContainer, String source, String adType);

    public int getAdCount();
    public String getAdMode();
    public void resume();
    public void pause();

    public void destroy();
}
