package com.rabbit.adsdk.adloader.listener;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface ISdkLoader {

    void setListenerManager(IManagerListener l);

    void init(Context context, PidConfig pidConfig);

    Context getContext();

    Activity getActivity();

    // 获取loader名称
    String getSdkName();

    String getAdType();

    PidConfig getPidConfig();

    // 加载插屏
    void loadInterstitial();

    // 展示插屏
    boolean showInterstitial(String sceneName);

    // 加载原生
    void loadNative(Params params);

    // 展示原生
    void showNative(ViewGroup viewGroup, Params params);

    void loadBanner(int adSize);

    void showBanner(ViewGroup viewGroup);

    void loadRewardedVideo();

    boolean showRewardedVideo(String sceneName);

    public void loadSplash();

    public boolean showSplash(ViewGroup viewGroup);

    boolean isInterstitialLoaded();

    boolean isBannerLoaded();

    boolean isNativeLoaded();

    boolean isRewardedVideoLoaded();

    boolean isSplashLoaded();

    void resume();

    void pause();

    void destroy();

    String getAdPlaceName();

    boolean isBannerType();

    boolean isNativeType();

    boolean isInterstitialType();

    boolean isRewardedVideoType();

    boolean isSplashType();

    boolean allowUseLoader();

    int getBannerSize();

    double getEcpm();

    void notifyAdViewUIDismiss();

    void showInterstitialWithNative(ViewGroup viewGroup, Params params);

    double getRevenue();
}
