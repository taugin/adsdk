package com.komob.adsdk.adloader.listener;

import android.app.Activity;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    /**
     * 获取banner和native的listener
     *
     * @param loader
     * @return
     */
    OnAdBaseListener getAdBaseListener(ISdkLoader loader);

    void registerAdBaseListener(ISdkLoader loader, OnAdBaseListener l);

    OnAdSdkInternalListener getOnAdPlaceLoaderListener();

    Activity getActivity();

    String getOriginPlaceName();

    String getAdMode();
}
