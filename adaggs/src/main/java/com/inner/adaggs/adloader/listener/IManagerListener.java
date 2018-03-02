package com.inner.adaggs.adloader.listener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    /**
     * 获取banner和native的listener
     * @param loader
     * @return
     */
    OnAdListener getAdListener(IAdLoader loader);
    void registerAdListener(IAdLoader loader, OnAdListener l);
    void clearAdListener(IAdLoader loader);

    /**
     * 获取interstital的listener
     * @param loader
     * @return
     */
    OnInterstitialListener getIntListener(IAdLoader loader);
    void registerIntListener(IAdLoader loader, OnInterstitialListener l);
    void clearIntListener(IAdLoader loader);
}
