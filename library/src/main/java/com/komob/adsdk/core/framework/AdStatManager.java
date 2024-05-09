package com.komob.adsdk.core.framework;

import android.content.Context;

import com.komob.adsdk.AdImpData;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static ExecutorService sExecutorService = Executors.newSingleThreadExecutor();

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
        Log.iv(Log.TAG_SDK, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        recordAllImpCount();
        ReplaceManager.get(mContext).reportAdImp(placeName);
    }

    public void recordAdClick(String sdk, String placeName, String pid, String network, Map<String, Object> extra, String impressionId) {
        Log.iv(Log.TAG_SDK, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        recordAllClkCount();
        BounceRateManager.get(mContext).onAdClick(pid, extra);
        recordAdClick(impressionId);
    }

    private void recordAllImpCount() {
        long impCount = Utils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
        long allImpCount = impCount + 1;
        Utils.putLong(mContext, PREF_ALL_IMP_COUNT, allImpCount);
        Log.iv(Log.TAG_SDK, "all imp count : " + allImpCount);
    }

    private void recordAllClkCount() {
        long clkCount = Utils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
        long allClkCount = clkCount + 1;
        Utils.putLong(mContext, PREF_ALL_CLK_COUNT, allClkCount);
        Log.iv(Log.TAG_SDK, "all clk count : " + allClkCount);
    }

    public long getAllImpCount() {
        return Utils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
    }

    public long getAllClkCount() {
        return Utils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
    }

    public void recordAdImpression(final AdImpData adImpData) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (adImpData != null) {
                    DBManager.get(mContext).insertAdImpression(adImpData);
                }
            }
        });
    }

    public void recordAdClick(final String impressionId) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                DBManager.get(mContext).updateClickTimes(impressionId);
            }
        });
    }
}
