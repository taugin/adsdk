package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class CheatManager {
    private static final String PREF_AD_IMP_COUNT = "pref_ad_%s_%s_imp_count";
    private static final String PREF_AD_CLK_COUNT = "pref_ad_%s_%s_clk_count";
    private static final String PREF_AD_COUNT_DATE = "pref_ad_%s_%s_count_date";
    private static CheatManager sCheatManager;

    public static CheatManager get(Context context) {
        synchronized (CheatManager.class) {
            if (sCheatManager == null) {
                createInstance(context);
            }
        }
        return sCheatManager;
    }

    private static void createInstance(Context context) {
        synchronized (CheatManager.class) {
            if (sCheatManager == null) {
                sCheatManager = new CheatManager(context);
            }
        }
    }

    private CheatManager(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * 判断是否允许加载
     *
     * @param sdk
     * @param placeName
     * @return
     */
    public boolean isUserCheat(String sdk, String placeName) {
        try {
            String prefKey = String.format(Locale.getDefault(), PREF_AD_IMP_COUNT, sdk, placeName);
            long impCount = Utils.getLong(mContext, prefKey, 0);
            prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
            long clickCount = Utils.getLong(mContext, prefKey, 0);
            String cheatConfigKey = "user_cheat_config";
            String cheatConfigString = AdSdk.get(mContext).getString(cheatConfigKey);
            if (!TextUtils.isEmpty(cheatConfigString)) {
                int minImp = 0;
                int maxCtr = 0;
                try {
                    JSONObject jobj = new JSONObject(cheatConfigString);
                    String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
                    if (jobj.has(keyConfig)) {
                        JSONObject cheatJobj = jobj.getJSONObject(keyConfig);
                        if (cheatJobj != null) {
                            minImp = cheatJobj.getInt("min_imp");
                            maxCtr = cheatJobj.getInt("max_ctr");
                        }
                    }
                } catch (Exception e) {
                    Log.e(Log.TAG, "error : " + e);
                }
                if (minImp > 0 && maxCtr > 0 && impCount >= minImp) {
                    int ctr = Math.round(clickCount / (float) impCount * 100);
                    boolean isUserCheat = ctr > maxCtr;
                    if (isUserCheat) {
                        String cheatLog = String.format(Locale.getDefault(), "cheat info : min imp : %d, max ctr : %d, imp count : %d, click count : %d, ctr : %d", minImp, maxCtr, impCount, clickCount, ctr);
                        Log.iv(Log.TAG, cheatLog);
                    }
                    return isUserCheat;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void recordAdImp(String sdk, String placeName) {
        resetCount(sdk, placeName);
        String prefKey = String.format(Locale.getDefault(), PREF_AD_IMP_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        printLog(sdk, placeName);
    }

    public void recordAdClick(String sdk, String placeName) {
        String prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        printLog(sdk, placeName);
    }

    private void resetCount(String sdk, String placeName) {
        String prefDateKey = String.format(Locale.getDefault(), PREF_AD_COUNT_DATE, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefDateKey);
        long nowDate = getTodayTime();
        long lastDate = Utils.getLong(mContext, prefDateKey, 0);
        if (nowDate > lastDate) {
            Utils.putLong(mContext, prefDateKey, nowDate);
            Log.iv(Log.TAG, String.format(Locale.getDefault(), "%s %s reset count data", sdk, placeName));
            String prefImp = String.format(PREF_AD_IMP_COUNT, sdk, placeName);
            Utils.putLong(mContext, prefImp, 0);
            String prefClk = String.format(PREF_AD_CLK_COUNT, sdk, placeName);
            Utils.putLong(mContext, prefClk, 0);
        }
    }

    private void printLog(String sdk, String placeName) {
        String prefKey = String.format(Locale.getDefault(), PREF_AD_IMP_COUNT, sdk, placeName);
        long impCount = Utils.getLong(mContext, prefKey, 0);
        prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
        long clickCount = Utils.getLong(mContext, prefKey, 0);
        String logInfo = String.format(Locale.getDefault(), "%s %s clk/imp : %d/%d", sdk, placeName, clickCount, impCount);
        Log.iv(Log.TAG, logInfo);
    }

    /**
     * 获取当天零点毫秒数
     *
     * @return
     */
    private long getTodayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
