package com.mix.ads.core.framework;

import android.content.Context;

import com.mix.ads.MiImpData;
import com.mix.ads.core.db.DBManager;
import com.mix.ads.log.Log;
import com.mix.ads.utils.Utils;

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
        LimitAdsManager.get(mContext).recordAdImp(sdk, placeName, network);
        ReplaceManager.get(mContext).reportAdImp(placeName);
    }

    public void recordAdClick(String sdk, String placeName, String pid, String network, Map<String, Object> extra, String impressionId) {
        Log.iv(Log.TAG_SDK, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        recordAllClkCount();
        LimitAdsManager.get(mContext).recordAdClick(sdk, placeName, network);
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

    public void recordAdImpression(final MiImpData miImpData) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (miImpData != null) {
                    DBManager.get(mContext).insertAdImpression(miImpData);
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
