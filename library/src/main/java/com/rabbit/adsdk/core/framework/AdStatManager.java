package com.rabbit.adsdk.core.framework;

import android.content.Context;

import com.rabbit.adsdk.log.Log;

public class AdStatManager {
    private static AdStatManager sAdStatManager;

    public static AdStatManager get(Context context) {
        synchronized (AdStatManager.class) {
            if (sAdStatManager == null) {
                createInstance(context);
            }
        }
        return sAdStatManager;
    }

    private static void createInstance(Context context) {
        synchronized (AdStatManager.class) {
            if (sAdStatManager == null) {
                sAdStatManager = new AdStatManager(context);
            }
        }
    }

    private Context mContext;
    private AdStatManager(Context context) {
        mContext = context;
    }

    public void recordAdImp(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        BlockAdsManager.get(mContext).recordAdImp(sdk, placeName, render);
    }

    public void recordAdClick(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        BlockAdsManager.get(mContext).recordAdClick(sdk, placeName, render);
    }
}
