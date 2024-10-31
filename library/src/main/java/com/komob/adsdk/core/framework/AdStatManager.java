package com.komob.adsdk.core.framework;

import android.content.Context;

import com.komob.adsdk.AdImpData;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.stat.AdImpReport;
import com.komob.adsdk.utils.SpUtils;

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
        long impCount = SpUtils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
        long allImpCount = impCount + 1;
        SpUtils.putLong(mContext, PREF_ALL_IMP_COUNT, allImpCount);
        Log.iv(Log.TAG_SDK, "all imp count : " + allImpCount);
    }

    private void recordAllClkCount() {
        long clkCount = SpUtils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
        long allClkCount = clkCount + 1;
        SpUtils.putLong(mContext, PREF_ALL_CLK_COUNT, allClkCount);
        Log.iv(Log.TAG_SDK, "all clk count : " + allClkCount);
    }

    public long getAllImpCount() {
        return SpUtils.getLong(mContext, PREF_ALL_IMP_COUNT, 0);
    }

    public long getAllClkCount() {
        return SpUtils.getLong(mContext, PREF_ALL_CLK_COUNT, 0);
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
        AdImpReport.reportAdImpression(mContext, adImpData);
        AdImpReport.reportTaichiEvent(mContext, adImpData);
        AdImpReport.reportAdImpressionAll(mContext, adImpData);
    }

    public void recordAdClick(final String impressionId) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                DBManager.get(mContext).updateClickTimes(impressionId);
                try {
                    AdImpData adImpData = DBManager.get(mContext).queryImpData(impressionId);
                    AdImpReport.reportAdClickAll(mContext, adImpData);
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
            }
        });
    }
}
