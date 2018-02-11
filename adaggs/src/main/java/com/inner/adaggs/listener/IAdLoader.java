package com.inner.adaggs.listener;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.inner.adaggs.config.PidConfig;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IAdLoader {

    public void setContext(Context context);

    public Context getContext();

    // 获取loader名称
    public String getSdkName();

    public void setPidConfig(PidConfig config);

    public PidConfig getPidConfig();

    public void setOnInterstitialListener(OnInterstitialListener l);
    // 加载插屏
    public void loadInterstitial();

    // 展示插屏
    public boolean showInterstitial();

    public void setOnAdListener(OnAdListener l);

    // 加载原生，包括banner
    public void loadNative(View rootView, int templateId);

    // 展示原生，包括banner
    public void showNative(ViewGroup viewGroup);

    public void loadBanner(int adSize);

    public void showBanner(ViewGroup viewGroup);

    public View getAdView();

    public boolean isInterstitialLoaded();

    public boolean isBannerLoaded();

    public boolean isNativeLoaded();

    public void destroy();

    public String getPidName();

    public boolean isBannerType();

    public boolean isNativeType();

    public boolean isInterstitialType();
}
