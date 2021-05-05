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
import java.util.List;
import java.util.Locale;

public class CheatManager {
    private static final String PREF_AD_PLACE_IMP_COUNT     = "pref_ad_cheat_%s_%s_imp_count";
    private static final String PREF_AD_PLACE_CLK_COUNT     = "pref_ad_cheat_%s_%s_clk_count";
    private static final String PREF_AD_SDK_IMP_COUNT       = "pref_ad_cheat_%s_imp_count";
    private static final String PREF_AD_SDK_CLK_COUNT       = "pref_ad_cheat_%s_clk_count";
    private static final String PREF_AD_CHEAT_COUNT_DATE    = "pref_ad_cheat_count_date";
    private static final String PREF_AD_CHEAT_KEY_LIST      = "pref_ad_cheat_key_list";

    private static final String KEY_AD_CHEAT_INTERCEPT      = "cheat_intercept";
    private static final String KEY_AD_CHEAT_GAIDS          = "cheat_gaids";
    private static final String KEY_AD_CHEAT_CONFIG         = "cheat_config";
    private static final String MAX_CLK                     = "max_clk";
    private static final String MIN_IMP                     = "min_imp";


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
        resetCheatData();
        if (isCheatInterceptEnabled()) {
            return interceptCheatByGAID() || interceptCheatByConfig(sdk, placeName);
        }
        return false;
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
            String cfgGAID = DataManager.get(mContext).getString(KEY_AD_CHEAT_GAIDS);
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
        boolean isCheatUser = false;
        CheatInfo cheatInfo = getJudgeConfig(sdk, placeName);
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (cheatInfo != null) {
            long impCount = 0;
            long clkCount = 0;
            String prefImpKey = null;
            String prefClkKey = null;
            if (TextUtils.equals(keyConfig, cheatInfo.placement)) {
                // 具体广告位
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
            } else {
                // 具体平台
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
            }
            impCount = Utils.getLong(mContext, prefImpKey, 0);
            clkCount = Utils.getLong(mContext, prefClkKey, 0);
            isCheatUser = judgeCheatUser(cheatInfo.maxClk, cheatInfo.minImp, (int) impCount, (int) clkCount);
            if (isCheatUser) {
                Log.iv(Log.TAG, "intercept cheat info : " + getCheatInfo(cheatInfo.placement, cheatInfo.maxClk, cheatInfo.minImp, (int) impCount, (int) clkCount));
            }
        }
        return isCheatUser;
    }

    private void reportCheatUser(String sdk, String placeName) {
        CheatInfo cheatInfo = getJudgeConfig(sdk, placeName);
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (cheatInfo != null) {
            long impCount = 0;
            long clkCount = 0;
            String prefImpKey = null;
            String prefClkKey = null;
            if (TextUtils.equals(keyConfig, cheatInfo.placement)) {
                // 具体广告位
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
            } else {
                // 具体平台
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
            }
            impCount = Utils.getLong(mContext, prefImpKey, 0);
            clkCount = Utils.getLong(mContext, prefClkKey, 0);
            boolean isCheatUser = judgeCheatUser(cheatInfo.maxClk, cheatInfo.minImp, (int) impCount, (int) clkCount);
            if (isCheatUser) {
                String reportInfo = getCheatInfo(cheatInfo.placement, cheatInfo.maxClk, cheatInfo.minImp, (int) impCount, (int) clkCount);
                Log.iv(Log.TAG, "report cheat info : " + reportInfo);
                InternalStat.reportEvent(mContext, "cheat_info", reportInfo);
            }
        }
    }

    private String getCheatInfo(String placement, int maxClk, int minImp, int impCount, int clkCount) {
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (!TextUtils.isEmpty(gaid)) {
            gaid = Utils.string2MD5(gaid);
        } else {
            gaid = "unknown";
        }
        return String.format(Locale.getDefault(), "%s|%s|%d/%d|%d/%d", gaid, placement, maxClk, minImp, clkCount, impCount);
    }

    private boolean judgeCheatUser(int maxClk, int minImp, int impCount, int clkCount) {
        boolean isUserCheat = false;
        if (maxClk > 0) {
            if (minImp > 0) {
                isUserCheat = impCount >= minImp && clkCount > maxClk;
            } else {
                isUserCheat = clkCount > maxClk;
            }
        }
        return isUserCheat;
    }

    private CheatInfo getJudgeConfig(String sdk, String placeName) {
        int maxClk = 0;
        int minImp = 0;
        CheatInfo cheatInfo = new CheatInfo();
        String cheatConfigString = AdSdk.get(mContext).getString(KEY_AD_CHEAT_CONFIG);
        if (!TextUtils.isEmpty(cheatConfigString)) {
            try {
                JSONObject cheatJobj = null;
                JSONObject jobj = new JSONObject(cheatConfigString);
                String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
                if (jobj.has(keyConfig)) {
                    cheatJobj = jobj.getJSONObject(keyConfig);
                    cheatInfo.placement = keyConfig;
                } else if (jobj.has(sdk)) {
                    cheatJobj = jobj.getJSONObject(sdk);
                    cheatInfo.placement = sdk;
                }
                if (cheatJobj != null) {
                    if (cheatJobj.has(MAX_CLK)) {
                        maxClk = cheatJobj.getInt(MAX_CLK);
                    }
                    if (cheatJobj.has(MIN_IMP)) {
                        minImp = cheatJobj.getInt(MIN_IMP);
                    }
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        }
        cheatInfo.maxClk = maxClk;
        cheatInfo.minImp = minImp;
        return cheatInfo;
    }

    public void recordAdImp(String sdk, String placeName) {
        // 记录PLACE层级的展示次数
        String prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        // 记录SDK层级的展示次数
        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        printLog(sdk, placeName);
        recordAdKeyList(sdk, placeName);
    }

    public void recordAdClick(String sdk, String placeName) {
        // 记录PLACE层级的点击次数
        String prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        // 记录SDK层级的点击次数
        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1);
        printLog(sdk, placeName);
        reportCheatUser(sdk, placeName);
    }

    private void resetCheatData() {
        long nowDate = getTodayTime();
        long lastDate = Utils.getLong(mContext, PREF_AD_CHEAT_COUNT_DATE, 0);
        if (nowDate > lastDate) {
            Utils.putLong(mContext, PREF_AD_CHEAT_COUNT_DATE, nowDate);
            resetSdkAndPlace();
        }
    }

    private void resetSdkAndPlace() {
        List<String> keyList = getKeyList();
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
                Log.iv(Log.TAG, String.format(Locale.getDefault(), "[%s - %s] reset count data", sdk, placeName));
                String prefPlaceImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                Utils.putLong(mContext, prefPlaceImpKey, 0);
                String prefPlaceClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
                Utils.putLong(mContext, prefPlaceClkKey, 0);

                Log.iv(Log.TAG, String.format(Locale.getDefault(), "[%s] reset count data", sdk));
                String prefSdkImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                Utils.putLong(mContext, prefSdkImpKey, 0);
                String prefSdkClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
                Utils.putLong(mContext, prefSdkClkKey, 0);
            }
        }
    }

    private void printLog(String sdk, String placeName) {
        String prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
        long placeImpCount = Utils.getLong(mContext, prefKey, 0);
        prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
        long placeClickCount = Utils.getLong(mContext, prefKey, 0);

        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
        long sdkImpCount = Utils.getLong(mContext, prefKey, 0);
        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
        long sdkClickCount = Utils.getLong(mContext, prefKey, 0);
        String logInfo = String.format(Locale.getDefault(), "[%s : %d/%d] [%s - %s : %d/%d]", sdk, sdkClickCount, sdkImpCount, sdk, placeName, placeClickCount, placeImpCount);
        Log.iv(Log.TAG, logInfo);
    }

    private synchronized void recordAdKeyList(String sdk, String placeName) {
        try {
            String newKey = String.format(Locale.getDefault(), "%s,%s", sdk, placeName);
            List<String> allKeyList = new ArrayList<String>();
            String keyListString = Utils.getString(mContext, PREF_AD_CHEAT_KEY_LIST, null);
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
            Utils.putString(mContext, PREF_AD_CHEAT_KEY_LIST, allKeyString);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private List<String> getKeyList() {
        String keyListString = Utils.getString(mContext, PREF_AD_CHEAT_KEY_LIST, null);
        List<String> lastKeyList = null;
        if (!TextUtils.isEmpty(keyListString)) {
            lastKeyList = Arrays.asList(keyListString.split("\\|"));
        }
        return lastKeyList;
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                Log.e(Log.TAG, "parseBoolean error : " + e);
            }
        }
        return defaultValue;
    }

    private boolean isCheatInterceptEnabled() {
        return parseBoolean(AdSdk.get(mContext).getString(KEY_AD_CHEAT_INTERCEPT), false);
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

    class CheatInfo {
        public int maxClk;
        public int minImp;
        public String placement = "unknown";
    }
}
