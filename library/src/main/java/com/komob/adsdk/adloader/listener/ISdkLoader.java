package com.komob.adsdk.adloader.listener;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.config.PidConfig;

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

    public boolean showSplash(ViewGroup viewGroup, String sceneName);

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

    double getCpm();

    void notifyAdViewUIDismiss();

    void showInterstitialWithNative(ViewGroup viewGroup, Params params);

    double getRevenue();

    long getCostTime();

    String getNetwork();

    void notifyBidResult(String platform, String adType, String firstNetwork, double firstPrice, String secondNetwork, double secondPrice);
}
