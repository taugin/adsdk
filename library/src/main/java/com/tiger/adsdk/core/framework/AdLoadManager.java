package com.tiger.adsdk.core.framework;

import android.content.Context;

import com.tiger.adsdk.listener.OnAdEventListener;
import com.tiger.adsdk.listener.OnAdFilterListener;

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
    private OnAdFilterListener mOnAdFilterListener;
    private OnAdEventListener mOnAdEventListener;

    public OnAdFilterListener getOnAdFilterListener() {
        return mOnAdFilterListener;
    }

    public void setOnAdFilterListener(OnAdFilterListener onAdFilterListener) {
        mOnAdFilterListener = onAdFilterListener;
    }

    public OnAdEventListener getOnAdEventListener() {
        return mOnAdEventListener;
    }

    public void setOnAdEventListener(OnAdEventListener l) {
        mOnAdEventListener = l;
    }
}
