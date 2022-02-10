package com.rabbit.adsdk.core.framework;

import android.content.Context;

import com.rabbit.adsdk.listener.OnAdDisableLoadingListener;
import com.rabbit.adsdk.listener.OnAdImpressionListener;

public class AdLoadManager {
    private static AdLoadManager sAdLoadManager;

    public static AdLoadManager get(Context context) {
        synchronized (AdLoadManager.class) {
            if (sAdLoadManager == null) {
                createInstance(context);
            }
        }
        return sAdLoadManager;
    }

    private static void createInstance(Context context) {
        synchronized (AdLoadManager.class) {
            if (sAdLoadManager == null) {
                sAdLoadManager = new AdLoadManager(context);
            }
        }
    }

    private AdLoadManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    private Context mContext;
    // 禁止广告加载过滤器
    private OnAdDisableLoadingListener mOnAdDisableLoadingListener;
    private OnAdImpressionListener mOnAdImpressionListener;

    public OnAdDisableLoadingListener getOnAdDisableLoadingListener() {
        return mOnAdDisableLoadingListener;
    }

    public void setOnAdDisableLoadingListener(OnAdDisableLoadingListener onAdDisableLoadingListener) {
        mOnAdDisableLoadingListener = onAdDisableLoadingListener;
    }

    public OnAdImpressionListener getOnAdImpressionListener() {
        return mOnAdImpressionListener;
    }

    public void setOnAdImpressionListener(OnAdImpressionListener l) {
        mOnAdImpressionListener = l;
    }
}
