package com.inner.adsdk.adloader.listener;

import com.inner.adsdk.listener.OnAdSdkListener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    /**
     * 获取banner和native的listener
     * @param loader
     * @return
     */
    OnAdBaseListener getAdBaseListener(IAdLoader loader);
    void registerAdBaseListener(IAdLoader loader, OnAdBaseListener l);

    OnAdSdkListener getOnAdSdkListener();
    OnAdSdkListener getOnAdPlaceLoaderListener();
    void setLoader(IAdLoader adLoader);
    boolean isCurrent(String source, String type);
}
