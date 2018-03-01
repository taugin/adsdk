package com.inner.adaggs.adloader.listener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    OnAdListener getAdListener(IAdLoader loader);
    void registerAdListener(IAdLoader loader, OnAdListener l);
    void clearAdListener(IAdLoader loader);

    OnInterstitialListener getIntListener(IAdLoader loader);
    void registerIntListener(IAdLoader loader, OnInterstitialListener l);
    void clearIntListener(IAdLoader loader);
}
