package com.inner.adaggs.adloader.listener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    OnAdListener getAdListener(IAdLoader loader);
    void registerAdListener(IAdLoader loader, OnAdListener l);
    void clearAdListener(IAdLoader loader);
}
