package com.inner.adsdk.adloader.listener;

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
    void clearAdBaseListener(IAdLoader loader);
}
