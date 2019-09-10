package com.simple.mpsdk.internallistener;

import android.app.Activity;

import com.simple.mpsdk.listener.OnMpSdkListener;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface IManagerListener {
    /**
     * 获取banner和native的listener
     * @param loader
     * @return
     */
    OnMpBaseListener getAdBaseListener(ISdkLoader loader);
    void registerAdBaseListener(ISdkLoader loader, OnMpBaseListener l);

    OnMpSdkListener getOnAdSdkListener();
    boolean isCurrent(String source, String pidName);
    Activity getActivity();
}
