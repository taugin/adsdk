package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheatManager {
    private static final String PREF_AD_IMP_COUNT = "pref_ad_%s_%s_imp_count";
    private static final String PREF_AD_CLK_COUNT = "pref_ad_%s_%s_clk_count";
    private static final String PREF_AD_COUNT_DATE = "pref_ad_imp_clk_count_date";
    private static final String PREF_AD_KEY_LIST = "pref_ad_key_list";
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
        return interceptCheatByGAID() || interceptCheatByConfig(sdk, placeName);
    }

    /**
     * 是否过滤了指定gaid的用户
     *
     * @return
     */
    private boolean interceptCheatByGAID() {
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (!TextUtils.isEmpty(gaid)) {
            String gaidmd5 = Utils.string2MD5(gaid);
            String cfgGAID = DataManager.get(mContext).getString("cheat_gaids");
            if (!TextUtils.isEmpty(cfgGAID)) {
                List<String> gaidList = Arrays.asList(cfgGAID.split(","));
                if (gaidList != null) {
                    return gaidList.contains(gaidmd5);
                }
            }
        }
        return false;
    }

    private boolean interceptCheatByConfig(String sdk, String placeName) {
        try {
            String prefKey = String.format(Locale.getDefault(), PREF_AD_IMP_COUNT, sdk, placeName);
            long impCount = Utils.getLong(mContext, prefKey, 0);
            prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
            long clickCount = Utils.getLong(mContext, prefKey, 0);
            String cheatConfigKey = "cheat_config";
            String cheatConfigString = AdSdk.get(mContext).getString(cheatConfigKey);
            if (!TextUtils.isEmpty(cheatConfigString)) {
                int minImp = 0;
                int maxCtr = 0;
                try {
                    JSONObject cheatJobj = null;
                    JSONObject jobj = new JSONObject(cheatConfigString);
                    String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
                    if (jobj.has(keyConfig)) {
                        cheatJobj = jobj.getJSONObject(keyConfig);
                    } else if (jobj.has(sdk)) {
                        cheatJobj = jobj.getJSONObject(sdk);
                    }
                    if (cheatJobj != null) {
                        minImp = cheatJobj.getInt("min_imp");
                        maxCtr = cheatJobj.getInt("max_ctr");
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
        recordAdKeyList(sdk, placeName);
        printLog(sdk, placeName);
    }

    public void recordAdClick(String sdk, String placeName) {
        String prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        printLog(sdk, placeName);
    }

    private synchronized void recordAdKeyList(String sdk, String placeName) {
        try {
            String newKey = String.format(Locale.getDefault(), "%s,%s", sdk, placeName);
            List<String> allKeyList = new ArrayList<String>();
            String keyListString = Utils.getString(mContext, PREF_AD_KEY_LIST, null);
            List<String> lastKeyList = null;
            if (!TextUtils.isEmpty(keyListString)) {
                lastKeyList = Arrays.asList(keyListString.split("\\|"));
            }
            Log.iv(Log.TAG, "last key list : " + lastKeyList);
            if (lastKeyList != null && lastKeyList.contains(newKey)) {
                Log.iv(Log.TAG, newKey + " has saved");
                return;
            }
            if (lastKeyList != null && !lastKeyList.isEmpty()) {
                allKeyList.addAll(lastKeyList);
            }
            allKeyList.add(newKey);
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < allKeyList.size(); index++) {
                String s = allKeyList.get(index);
                if (index == allKeyList.size() - 1) {
                    builder.append(s);
                } else {
                    builder.append(s);
                    builder.append("|");
                }
            }
            String allKeyString = builder.toString();
            Log.iv(Log.TAG, "all key string : " + allKeyString);
            Utils.putString(mContext, PREF_AD_KEY_LIST, allKeyString);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private List<String> getKeyList() {
        String keyListString = Utils.getString(mContext, PREF_AD_KEY_LIST, null);
        List<String> lastKeyList = null;
        if (!TextUtils.isEmpty(keyListString)) {
            lastKeyList = Arrays.asList(keyListString.split("\\|"));
        }
        return lastKeyList;
    }

    private void resetCount(String sdk, String placeName) {
        long nowDate = getTodayTime();
        long lastDate = Utils.getLong(mContext, PREF_AD_COUNT_DATE, 0);
        if (nowDate > lastDate) {
            if (lastDate > 0 && isReportClkImp(mContext)) {
                report();
            }
            Utils.putLong(mContext, PREF_AD_COUNT_DATE, nowDate);
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

    private boolean parseReport(String value, boolean defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                Log.e(Log.TAG, "parseReport error : " + e);
            }
        }
        return defaultValue;
    }

    private boolean isReportClkImp(Context context) {
        String value = DataManager.get(context).getString("report_clk_imp");
        boolean result = parseReport(value, false);
        Log.v(Log.TAG, "is report clk imp : " + result);
        return result;
    }

    private void report() {
        try {
            List<String> keyList = getKeyList();
            Map<String, String> extra = new LinkedHashMap<String, String>();
            if (keyList != null && !keyList.isEmpty()) {
                for (String key : keyList) {
                    if (TextUtils.isEmpty(key)) {
                        continue;
                    }
                    String keySplit[] = key.split(",");
                    if (keySplit == null || keySplit.length != 2) {
                        continue;
                    }
                    String sdk = keySplit[0];
                    String placeName = keySplit[1];
                    String prefKey = String.format(Locale.getDefault(), PREF_AD_IMP_COUNT, sdk, placeName);
                    long impCount = Utils.getLong(mContext, prefKey, 0);
                    prefKey = String.format(Locale.getDefault(), PREF_AD_CLK_COUNT, sdk, placeName);
                    long clickCount = Utils.getLong(mContext, prefKey, 0);
                    // 有展示的广告位才需要上报
                    if (impCount > 0) {
                        String clkImpInfo = String.format(Locale.getDefault(), "clk_imp_%d_%d", clickCount, impCount);
                        String sdkInfo = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
                        extra.put(sdkInfo, clkImpInfo);
                    }
                }
            }
            String gaid = Utils.getString(mContext, Constant.PREF_GAID);
            if (!TextUtils.isEmpty(gaid)) {
                extra.put("uac", Utils.string2MD5(gaid));
            }
            Log.iv(Log.TAG, "event_clk_imp : " + extra);
            InternalStat.reportEvent(mContext, "event_clk_imp", extra);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
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
