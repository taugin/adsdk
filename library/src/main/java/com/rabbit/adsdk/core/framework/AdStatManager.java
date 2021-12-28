package com.rabbit.adsdk.core.framework;

import android.content.Context;

import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

public class AdStatManager {
    private static AdStatManager sAdStatManager;
    /**
     * 所有广告的总展示次数
     */
    private static final String PREF_ALL_IMP_COUNT = "pref_all_imp_count";
    /**
     * 所有广告的总点击次数
     */
    private static final String PREF_ALL_CLK_COUNT = "pref_all_clk_count";

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

    public void recordAdImp(String sdk, String placeName, String network) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        recordAllImpCount();
        BlockAdsManager.get(mContext).recordAdImp(sdk, placeName, network);
        LimitAdsManager.get(mContext).recordAdImp(sdk, placeName, network);
    }

    public void recordAdClick(String sdk, String placeName, String network) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        recordAllClkCount();
        BlockAdsManager.get(mContext).recordAdClick(sdk, placeName, network);
        LimitAdsManager.get(mContext).recordAdClick(sdk, placeName, network);
    }


    private void recordAllImpCount() {
        long impCount = Utils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
        long allImpCount = impCount + 1;
        Utils.putLong(mContext, PREF_ALL_IMP_COUNT, allImpCount);
        Log.iv(Log.TAG, "all imp count : " + allImpCount);
    }

    private void recordAllClkCount() {
        long clkCount = Utils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
        long allClkCount = clkCount + 1;
        Utils.putLong(mContext, PREF_ALL_CLK_COUNT, allClkCount);
        Log.iv(Log.TAG, "all clk count : " + allClkCount);
    }

    public long getAllImpCount() {
        return Utils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
    }

    public long getAllClkCount() {
        return Utils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
    }
}
