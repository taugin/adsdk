package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CheatManager {
    private static final String PREF_AD_PLACE_IMP_COUNT = "pref_ad_cheat_%s_%s_imp_count";
    private static final String PREF_AD_PLACE_CLK_COUNT = "pref_ad_cheat_%s_%s_clk_count";
    private static final String PREF_AD_SDK_IMP_COUNT = "pref_ad_cheat_%s_imp_count";
    private static final String PREF_AD_SDK_CLK_COUNT = "pref_ad_cheat_%s_clk_count";
    private static final String PREF_AD_CHEAT_COUNT_DATE = "pref_ad_cheat_count_date";
    private static final String PREF_AD_CHEAT_KEY_LIST = "pref_ad_cheat_key_list";

    private static final String CFG_AD_CHEAT_CONFIG = "cheatcfg";
    private static final String OPT_MAX_CLK = "max_clk";
    private static final String OPT_MIN_IMP = "min_imp";
    private static final String OPT_INTERCEPT = "intercept";
    private static final String OPT_GAIDS = "gaids";


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
        CheatCfg cheatCfg = getCheatConfig(sdk, placeName);
        if (cheatCfg != null) {
            if (cheatCfg.intercept) {
                return interceptCheatByGAID(cheatCfg) || interceptCheatByConfig(sdk, placeName, cheatCfg);
            }
        }
        return false;
    }

    /**
     * 是否过滤了指定gaid的用户
     *
     * @return
     */
    private boolean interceptCheatByGAID(CheatCfg cheatCfg) {
        boolean interceptByGaid = false;
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (cheatCfg != null && cheatCfg.gaidList != null && !cheatCfg.gaidList.isEmpty()) {
            if (!TextUtils.isEmpty(gaid)) {
                interceptByGaid = cheatCfg.gaidList.contains(gaid);
            }
        }
        if (interceptByGaid && cheatCfg != null) {
            Log.iv(Log.TAG, "intercept gaid [" + gaid + "] placement [" + cheatCfg.placement + "]");
        }
        return interceptByGaid;
    }

    private boolean interceptCheatByConfig(String sdk, String placeName, CheatCfg cheatCfg) {
        boolean isCheatUser = false;
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (cheatCfg != null) {
            String prefImpKey;
            String prefClkKey;
            if (TextUtils.equals(keyConfig, cheatCfg.placement)) {
                // 具体广告位
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
            } else {
                // 具体平台
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
            }
            long impCount = Utils.getLong(mContext, prefImpKey, 0);
            long clkCount = Utils.getLong(mContext, prefClkKey, 0);
            isCheatUser = judgeCheatUser(cheatCfg.maxClk, cheatCfg.minImp, (int) impCount, (int) clkCount);
            if (isCheatUser) {
                Log.iv(Log.TAG, "intercept cheat info : " + getCheatInfo(cheatCfg.placement, cheatCfg.maxClk, cheatCfg.minImp, (int) impCount, (int) clkCount));
            }
        }
        return isCheatUser;
    }

    private void reportCheatUser(String sdk, String placeName) {
        CheatCfg cheatCfg = getCheatConfig(sdk, placeName);
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (cheatCfg != null) {
            long impCount = 0;
            long clkCount = 0;
            String prefImpKey = null;
            String prefClkKey = null;
            if (TextUtils.equals(keyConfig, cheatCfg.placement)) {
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
            boolean isCheatUser = judgeCheatUser(cheatCfg.maxClk, cheatCfg.minImp, (int) impCount, (int) clkCount);
            if (isCheatUser) {
                String reportInfo = getCheatInfo(cheatCfg.placement, cheatCfg.maxClk, cheatCfg.minImp, (int) impCount, (int) clkCount);
                Log.iv(Log.TAG, "report cheat info : " + reportInfo);
                InternalStat.reportEvent(mContext, "cheat_info", reportInfo);
            }
        }
    }

    private String getCheatInfo(String placement, int maxClk, int minImp, int impCount, int clkCount) {
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (TextUtils.isEmpty(gaid)) {
            gaid = "unknown";
        }
        String locale = getLocale(mContext);
        if (TextUtils.isEmpty(locale)) {
            locale = "unknown";
        }
        return String.format(Locale.getDefault(), "%s|%s|%s|%d/%d|%d/%d", gaid, locale, placement, maxClk, minImp, clkCount, impCount);
    }

    private static String getLocale(Context context) {
        String channel = "unknown";
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            channel = locale.getCountry().toLowerCase(Locale.getDefault());
        } catch (Exception e) {
        }
        return channel;
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

    private CheatCfg getCheatConfig(String sdk, String placeName) {
        CheatCfg cheatCfg = new CheatCfg();
        String cheatConfigString = AdSdk.get(mContext).getString(CFG_AD_CHEAT_CONFIG);
        if (!TextUtils.isEmpty(cheatConfigString)) {
            try {
                JSONObject jobj = new JSONObject(cheatConfigString);
                if (jobj.has(OPT_INTERCEPT)) {
                    cheatCfg.intercept = jobj.getBoolean(OPT_INTERCEPT);
                }
                String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
                JSONObject cheatJobj = null;
                if (jobj.has(keyConfig)) {
                    cheatJobj = jobj.getJSONObject(keyConfig);
                    cheatCfg.placement = keyConfig;
                } else if (jobj.has(sdk)) {
                    cheatJobj = jobj.getJSONObject(sdk);
                    cheatCfg.placement = sdk;
                }
                if (cheatJobj != null) {
                    if (cheatJobj.has(OPT_MAX_CLK)) {
                        cheatCfg.maxClk = cheatJobj.getInt(OPT_MAX_CLK);
                    }
                    if (cheatJobj.has(OPT_MIN_IMP)) {
                        cheatCfg.minImp = cheatJobj.getInt(OPT_MIN_IMP);
                    }
                    if (cheatJobj.has(OPT_GAIDS)) {
                        cheatCfg.gaidList = parseStringList(cheatJobj.getString(OPT_GAIDS));
                    }
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        }
        return cheatCfg;
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

    private List<String> parseStringList(String str) {
        List<String> list = null;
        try {
            JSONArray jarray = new JSONArray(str);
            if (jarray != null && jarray.length() > 0) {
                list = new ArrayList<String>(jarray.length());
                for (int index = 0; index < jarray.length(); index++) {
                    String s = jarray.getString(index);
                    if (!TextUtils.isEmpty(s)) {
                        list.add(s);
                    }
                }
            }
        } catch (Exception e) {
        }
        return list;
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

    class CheatCfg {
        public boolean intercept = false;
        public int maxClk;
        public int minImp;
        public String placement = "unknown";
        public List<String> gaidList;

        @Override
        public String toString() {
            return "CheatCfg{" +
                    "intercept=" + intercept +
                    ", maxClk=" + maxClk +
                    ", minImp=" + minImp +
                    ", placement='" + placement + '\'' +
                    ", gaidList=" + gaidList +
                    '}';
        }
    }
}
