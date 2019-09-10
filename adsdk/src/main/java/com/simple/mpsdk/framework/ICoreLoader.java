package com.simple.mpsdk.framework;

import android.app.Activity;
import android.view.ViewGroup;

import com.simple.mpsdk.MpParams;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.listener.OnMpSdkListener;

/**
 * Created by wangchao1 on 2018/5/1.
 */

public interface ICoreLoader {

    public void init();

    public void setAdPlaceConfig(MpPlace mpPlace);

    public boolean needReload(MpPlace mpPlace);

    public void setOnAdSdkListener(OnMpSdkListener l);

    public boolean isInterstitialLoaded();
    public void loadInterstitial(Activity activity);
    public void showInterstitial();

    public boolean isBannerLoaded();
    public void loadBanner(MpParams mpParams);
    public void showBanner(ViewGroup adContainer, MpParams mpParams);

    public boolean isNativeLoaded();
    public void loadNative(MpParams mpParams);
    public void showNative(ViewGroup adContainer, MpParams mpParams);

    public boolean isCommonViewLoaded();
    public void loadCommonView(MpParams mpParams);
    public void showCommonView(ViewGroup adContainer, MpParams mpParams);

    public void loadRewardVideo(Activity activity);
    public void showRewardVideo();
    public boolean isRewardVideoLoaded();

    public void resume();
    public void pause();

    public void destroy();
}
