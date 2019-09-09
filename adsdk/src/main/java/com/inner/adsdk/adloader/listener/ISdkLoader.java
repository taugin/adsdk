package com.inner.adsdk.adloader.listener;

import android.content.Context;
import android.view.ViewGroup;

import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.framework.Params;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface ISdkLoader {

    public void setListenerManager(IManagerListener l);

    public void init(Context context, AdPlace adPlace);

    public Context getContext();

    // 获取loader名称
    public String getSdkName();

    public String getAdType();

    public AdPlace getAdPlace();

    // 加载插屏
    public void loadInterstitial();

    // 展示插屏
    public boolean showInterstitial();

    // 加载原生
    public void loadNative(Params params);

    // 展示原生
    public void showNative(ViewGroup viewGroup, Params params);

    public void loadBanner(int adSize);

    public void showBanner(ViewGroup viewGroup);

    public void loadRewardedVideo();

    public boolean showRewardedVideo();

    public boolean isInterstitialLoaded();

    public boolean isBannerLoaded();

    public boolean isNativeLoaded();

    public boolean isRewaredVideoLoaded();

    public void resume();

    public void pause();

    public void destroy();

    public boolean isBannerType();

    public boolean isNativeType();

    public boolean isInterstitialType();

    public boolean isRewardedVideoType();

    public int getBannerSize();

    public String getName();
}
