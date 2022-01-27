package com.rabbit.adsdk.adloader.core;

import android.content.Context;

import com.rabbit.adsdk.listener.OnAdFilterListener;
import com.rabbit.adsdk.listener.OnAdImpressionListener;

public class AdLoaderManager {
    private static AdLoaderManager sAdLoaderManager;

    public static AdLoaderManager get(Context context) {
        synchronized (AdLoaderManager.class) {
            if (sAdLoaderManager == null) {
                createInstance(context);
            }
        }
        return sAdLoaderManager;
    }

    private static void createInstance(Context context) {
        synchronized (AdLoaderManager.class) {
            if (sAdLoaderManager == null) {
                sAdLoaderManager = new AdLoaderManager(context);
            }
        }
    }

    private AdLoaderManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    private Context mContext;
    // 广告加载过滤器
    private OnAdFilterListener mOnAdFilterListener;
    private OnAdImpressionListener mOnAdImpressionListener;

    public OnAdFilterListener getAdLoaderFilter() {
        return mOnAdFilterListener;
    }

    public void setAdLoaderFilter(OnAdFilterListener onAdFilterListener) {
        mOnAdFilterListener = onAdFilterListener;
    }

    public OnAdImpressionListener getOnAdImpressionListener() {
        return mOnAdImpressionListener;
    }

    public void setOnAdImpressionListener(OnAdImpressionListener l) {
        mOnAdImpressionListener = l;
    }
}
