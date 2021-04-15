package com.rabbit.adsdk.adloader.listener;

import android.app.Activity;

import com.rabbit.adsdk.listener.OnAdSdkListener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    /**
     * 获取banner和native的listener
     * @param loader
     * @return
     */
    OnAdBaseListener getAdBaseListener(ISdkLoader loader);
    void registerAdBaseListener(ISdkLoader loader, OnAdBaseListener l);

    OnAdSdkListener getOnAdPlaceLoaderListener();
    void setLoader(ISdkLoader adLoader);
    Activity getActivity();
    String getOriginPidName();
    String getAdMode();
}
