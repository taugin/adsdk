package com.inner.adsdk.adloader.listener;

import android.app.Activity;

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
    OnAdBaseListener getAdBaseListener(ISdkLoader loader);
    void registerAdBaseListener(ISdkLoader loader, OnAdBaseListener l);

    OnAdSdkListener getOnAdSdkListener();
    boolean isCurrent(String source, String pidName);
    Activity getActivity();
}
