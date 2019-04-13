package com.hauyu.adsdk.adloader.listener;

import android.content.Context;
import android.view.ViewGroup;

import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.framework.Params;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface ISdkLoader {

    public void setListenerManager(IManagerListener l);

    public void init(Context context);

    public void setAdId(String adId);

    public Context getContext();

    // 获取loader名称
    public String getSdkName();

    public String getAdType();

    public void setPidConfig(PidConfig config);

    public PidConfig getPidConfig();

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

    public String getAdPlaceName();

    public boolean isBannerType();

    public boolean isNativeType();

    public boolean isInterstitialType();

    public boolean isRewardedVideoType();

    public void setLoadedFlag();

    public boolean hasLoadedFlag();

    public boolean useAndClearFlag();

    public boolean allowUseLoader();

    public int getBannerSize();

    public int getEcpm();
}
