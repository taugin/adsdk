package com.inner.adsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.inner.adsdk.AdParams;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.listener.OnAdSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public interface IAdLoader {

    public void init();

    public void setAdPlaceConfig(AdPlace adPlace);

    public boolean needReload(AdPlace adPlace);

    public void setOnAdSdkListener(OnAdSdkListener l);

    public boolean isInterstitialLoaded();
    public void loadInterstitial(Activity activity);
    public void showInterstitial();

    public boolean isBannerLoaded();
    public void loadBanner(AdParams adParams);
    public void showBanner(ViewGroup adContainer, AdParams adParams);

    public boolean isNativeLoaded();
    public void loadNative(AdParams adParams);
    public void showNative(ViewGroup adContainer, AdParams adParams);

    public void loadRewardVideo(Activity activity);
    public void showRewardVideo();
    public boolean isRewardVideoLoaded();

    public int getAdCount();
    public String getAdMode();
    public void resume();
    public void pause();

    public void destroy();
}
