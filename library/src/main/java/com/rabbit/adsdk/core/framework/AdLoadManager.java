package com.rabbit.adsdk.core.framework;

import android.content.Context;

import com.rabbit.adsdk.listener.OnAdFilterListener;
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
    // 广告加载过滤器
    private OnAdFilterListener mOnAdFilterListener;
    private OnAdImpressionListener mOnAdImpressionListener;

    public OnAdFilterListener getOnAdFilterListener() {
        return mOnAdFilterListener;
    }

    public void setOnAdFilterListener(OnAdFilterListener onAdFilterListener) {
        mOnAdFilterListener = onAdFilterListener;
    }

    public OnAdImpressionListener getOnAdImpressionListener() {
        return mOnAdImpressionListener;
    }

    public void setOnAdImpressionListener(OnAdImpressionListener l) {
        mOnAdImpressionListener = l;
    }
}
