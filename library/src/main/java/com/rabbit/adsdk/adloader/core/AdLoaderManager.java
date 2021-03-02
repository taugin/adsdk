package com.rabbit.adsdk.adloader.core;

import android.content.Context;

import com.rabbit.adsdk.listener.AdLoaderFilter;

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
    private AdLoaderFilter mAdLoaderFilter;

    public AdLoaderFilter getAdLoaderFilter() {
        return mAdLoaderFilter;
    }

    public void setAdLoaderFilter(AdLoaderFilter adLoaderFilter) {
        mAdLoaderFilter = adLoaderFilter;
    }
}
