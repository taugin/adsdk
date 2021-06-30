package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlockAdsManager {
    private static final String PREF_AD_PLACE_IMP_COUNT = "pref_block_ads_%s_%s_imp_count";
    private static final String PREF_AD_PLACE_CLK_COUNT = "pref_block_ads_%s_%s_clk_count";
    private static final String PREF_AD_SDK_IMP_COUNT = "pref_block_ads_%s_imp_count";
    private static final String PREF_AD_SDK_CLK_COUNT = "pref_block_ads_%s_clk_count";
    private static final String PREF_AD_CLICK_COUNT_DATE = "pref_block_ads_count_date";
    private static final String PREF_AD_CLICK_KEY_LIST = "pref_block_ads_key_list";

    private static final String CFG_BLOCK_ADS = "blockcfg";
    private static final String OPT_MAX_CLK = "max_clk";
    private static final String OPT_MIN_IMP = "min_imp";
    private static final String OPT_BLOCK_ADS = "block_ads";
    private static final String OPT_REMOVE_ADS = "remove_ads";
    private static final String OPT_GAIDS = "gaids";
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private static BlockAdsManager sBlockAdsManager;

    public static BlockAdsManager get(Context context) {
        synchronized (BlockAdsManager.class) {
            if (sBlockAdsManager == null) {
                createInstance(context);
            }
        }
        return sBlockAdsManager;
    }

    private static void createInstance(Context context) {
        synchronized (BlockAdsManager.class) {
            if (sBlockAdsManager == null) {
                sBlockAdsManager = new BlockAdsManager(context);
            }
        }
    }

    private BlockAdsManager(Context context) {
        mContext = context;
    }

    private Context mContext;
    private String mLastCfgMd5 = null;
    private Map<String, BlockCfg> mBlockMap = new HashMap<String, BlockCfg>();

    private void parseBlockAdsConfig() {
        String configString = AdSdk.get(mContext).getString(CFG_BLOCK_ADS);
        if (!TextUtils.isEmpty(configString)) {
            String currentMd5 = Utils.string2MD5(configString);
            if (mBlockMap != null && !mBlockMap.isEmpty() && TextUtils.equals(currentMd5, mLastCfgMd5)) {
                return;
            }
            Log.iv(Log.TAG, "parse block config");
            mLastCfgMd5 = currentMd5;
            mBlockMap.clear();
            try {
                JSONObject cfgJobj = new JSONObject(configString);
                Iterator<String> jobjKeys = cfgJobj.keys();
                if (jobjKeys != null) {
                    while (jobjKeys.hasNext()) {
                        String blockKey = jobjKeys.next();
                        JSONObject blockObj = cfgJobj.getJSONObject(blockKey);
                        BlockCfg blockCfg = new BlockCfg();
                        if (blockObj != null) {
                            blockCfg.blockKey = blockKey;
                            if (blockObj.has(OPT_BLOCK_ADS)) {
                                blockCfg.blockAds = blockObj.getBoolean(OPT_BLOCK_ADS);
                            }
                            if (blockObj.has(OPT_REMOVE_ADS)) {
                                blockCfg.removeAds = blockObj.getBoolean(OPT_REMOVE_ADS);
                            }
                            if (blockObj.has(OPT_MAX_CLK)) {
                                blockCfg.maxClk = blockObj.getInt(OPT_MAX_CLK);
                            }
                            if (blockObj.has(OPT_MIN_IMP)) {
                                blockCfg.minImp = blockObj.getInt(OPT_MIN_IMP);
                            }
                            if (blockObj.has(OPT_GAIDS)) {
                                blockCfg.gaidList = parseStringList(blockObj.getString(OPT_GAIDS));
                            }
                            Log.iv(Log.TAG, "block cfg : " + blockCfg);
                            mBlockMap.put(blockCfg.blockKey, blockCfg);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        }
    }

    /**
     * 判断是否屏蔽广告
     *
     * @param sdk
     * @param placeName
     * @return
     */
    public boolean isBlockAds(String sdk, String placeName) {
        parseBlockAdsConfig();
        resetBlockAdsData();
        BlockCfg blockCfg = getBlockAdsConfig(sdk, placeName);
        Log.iv(Log.TAG, sdk + " - " + placeName + " block cfg : " + blockCfg);
        if (blockCfg != null && blockCfg.blockAds) {
            return isBlockAdsByGAID(blockCfg) || isBlockAdsByConfig(sdk, placeName, blockCfg);
        }
        return false;
    }

    public boolean isRemoveAds(String sdk, String placeName) {
        BlockCfg blockCfg = getBlockAdsConfig(sdk, placeName);
        if (blockCfg != null) {
            return blockCfg.removeAds;
        }
        return false;
    }

    /**
     * 是否过滤了指定gaid的用户
     *
     * @return
     */
    private boolean isBlockAdsByGAID(BlockCfg blockCfg) {
        boolean blockByGaid = false;
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (blockCfg != null && blockCfg.gaidList != null && !blockCfg.gaidList.isEmpty()) {
            if (!TextUtils.isEmpty(gaid)) {
                blockByGaid = blockCfg.gaidList.contains(gaid);
            }
        }
        if (blockByGaid && blockCfg != null) {
            Log.iv(Log.TAG, "block ads gaid [" + gaid + "] placement [" + blockCfg.blockKey + "]");
        }
        return blockByGaid;
    }

    private boolean isBlockAdsByConfig(String sdk, String placeName, BlockCfg blockCfg) {
        boolean isBlockAds = false;
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (blockCfg != null && (TextUtils.equals(keyConfig, blockCfg.blockKey)
                || TextUtils.equals(sdk, blockCfg.blockKey))) {
            String prefImpKey;
            String prefClkKey;
            String placement;
            if (TextUtils.equals(keyConfig, blockCfg.blockKey)) {
                // 具体广告位
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
                placement = blockCfg.blockKey;
            } else {
                // 具体平台
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
                placement = String.format(Locale.getDefault(), "%s#%s", sdk, placeName);
            }
            long impCount = Utils.getLong(mContext, prefImpKey, 0);
            long clkCount = Utils.getLong(mContext, prefClkKey, 0);
            isBlockAds = judgeBlockAds(blockCfg.maxClk, blockCfg.minImp, (int) impCount, (int) clkCount);
            if (isBlockAds) {
                Log.iv(Log.TAG, "block ads info : " + getBlockInfo(placement, blockCfg.maxClk, blockCfg.minImp, (int) impCount, (int) clkCount));
            }
        }
        return isBlockAds;
    }

    private void reportBlockAdsInfo(String sdk, String placeName) {
        BlockCfg blockCfg = getBlockAdsConfig(sdk, placeName);
        String keyConfig = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
        if (blockCfg != null && (TextUtils.equals(keyConfig, blockCfg.blockKey)
                || TextUtils.equals(sdk, blockCfg.blockKey))) {
            long impCount = 0;
            long clkCount = 0;
            String prefImpKey = null;
            String prefClkKey = null;
            String placement = null;
            if (TextUtils.equals(keyConfig, blockCfg.blockKey)) {
                // 具体广告位
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
                placement = blockCfg.blockKey;
            } else {
                // 具体平台
                prefImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                prefClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
                placement = String.format(Locale.getDefault(), "%s#%s", sdk, placeName);
            }
            impCount = Utils.getLong(mContext, prefImpKey, 0);
            clkCount = Utils.getLong(mContext, prefClkKey, 0);
            boolean isBlockAds = judgeBlockAds(blockCfg.maxClk, blockCfg.minImp, (int) impCount, (int) clkCount);
            if (isBlockAds) {
                String blockInfo = getBlockInfo(placement, blockCfg.maxClk, blockCfg.minImp, (int) impCount, (int) clkCount);
                Log.iv(Log.TAG, "report block info : " + blockInfo);
                InternalStat.reportEvent(mContext, "block_ads_info", blockInfo);
            }
        }
    }

    private String getBlockInfo(String placement, int maxClk, int minImp, int impCount, int clkCount) {
        String gaid = Utils.getString(mContext, Constant.PREF_GAID);
        if (TextUtils.isEmpty(gaid)) {
            gaid = "unknown";
        }
        String locale = getLocale(mContext);
        if (TextUtils.isEmpty(locale)) {
            locale = "unknown";
        }
        String userFlag = getUserFlag();
        String datetime = sSimpleDateFormat.format(new Date());
        return String.format(Locale.getDefault(), "%s|%s|%s|%s|%s|%d/%d|%d/%d", gaid, userFlag, locale, datetime, placement, maxClk, minImp, clkCount, impCount);
    }

    private String getUserFlag() {
        String userFlag = EventImpl.get().getUserFlag();
        if (TextUtils.equals(userFlag, "true")) {
            return "1";
        }
        if (TextUtils.equals(userFlag, "false")) {
            return "0";
        }
        return "-1";
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

    private boolean judgeBlockAds(int maxClk, int minImp, int impCount, int clkCount) {
        boolean isBlockAds = false;
        if (maxClk > 0) {
            if (minImp > 0) {
                isBlockAds = impCount >= minImp && clkCount >= maxClk;
            } else {
                isBlockAds = clkCount >= maxClk;
            }
        }
        return isBlockAds;
    }

    private BlockCfg getBlockAdsConfig(String sdk, String placeName) {
        if (mBlockMap != null && !mBlockMap.isEmpty()) {
            BlockCfg blockCfg;
            String blockKey = String.format(Locale.getDefault(), "%s_%s", sdk, placeName);
            blockCfg = mBlockMap.get(blockKey);
            if (blockCfg == null) {
                blockCfg = mBlockMap.get(sdk);
            }
            return blockCfg;
        }
        return null;
    }

    public void recordAdImp(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        if (!TextUtils.isEmpty(render) && !TextUtils.equals(sdk, render)) {
            sdk = render;
        }
        recordAdImp(sdk, placeName);
    }

    public void recordAdImp(String sdk, String placeName) {
        // 记录PLACE层级的展示次数
        String prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1, true);
        // 记录SDK层级的展示次数
        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1, true);
        printLog(sdk, placeName);
        recordAdKeyList(sdk, placeName);
    }

    public void recordAdClick(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        if (!TextUtils.isEmpty(render) && !TextUtils.equals(sdk, render)) {
            sdk = render;
        }
        recordAdClick(sdk, placeName);
    }

    public void recordAdClick(String sdk, String placeName) {
        // 记录PLACE层级的点击次数
        String prefKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        long count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1, true);
        // 记录SDK层级的点击次数
        prefKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
        Log.iv(Log.TAG, "prefKey : " + prefKey);
        count = Utils.getLong(mContext, prefKey, 0);
        Utils.putLong(mContext, prefKey, count + 1, true);
        printLog(sdk, placeName);
        reportBlockAdsInfo(sdk, placeName);
    }

    private void resetBlockAdsData() {
        long nowDate = getTodayTime();
        long lastDate = Utils.getLong(mContext, PREF_AD_CLICK_COUNT_DATE, 0);
        if (nowDate > lastDate) {
            Utils.putLong(mContext, PREF_AD_CLICK_COUNT_DATE, nowDate, true);
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
                String keySplit[] = key.split("#");
                if (keySplit == null || keySplit.length != 2) {
                    continue;
                }
                String sdk = keySplit[0];
                String placeName = keySplit[1];
                Log.iv(Log.TAG, String.format(Locale.getDefault(), "[%s - %s] reset count data", sdk, placeName));
                String prefPlaceImpKey = String.format(Locale.getDefault(), PREF_AD_PLACE_IMP_COUNT, sdk, placeName);
                Utils.putLong(mContext, prefPlaceImpKey, 0, true);
                String prefPlaceClkKey = String.format(Locale.getDefault(), PREF_AD_PLACE_CLK_COUNT, sdk, placeName);
                Utils.putLong(mContext, prefPlaceClkKey, 0, true);

                Log.iv(Log.TAG, String.format(Locale.getDefault(), "[%s] reset count data", sdk));
                String prefSdkImpKey = String.format(Locale.getDefault(), PREF_AD_SDK_IMP_COUNT, sdk);
                Utils.putLong(mContext, prefSdkImpKey, 0, true);
                String prefSdkClkKey = String.format(Locale.getDefault(), PREF_AD_SDK_CLK_COUNT, sdk);
                Utils.putLong(mContext, prefSdkClkKey, 0, true);
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
            String newKey = String.format(Locale.getDefault(), "%s#%s", sdk, placeName);
            List<String> allKeyList = new ArrayList<String>();
            String keyListString = Utils.getString(mContext, PREF_AD_CLICK_KEY_LIST, null);
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
            Utils.putString(mContext, PREF_AD_CLICK_KEY_LIST, allKeyString);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private List<String> getKeyList() {
        String keyListString = Utils.getString(mContext, PREF_AD_CLICK_KEY_LIST, null);
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

    class BlockCfg {
        public boolean blockAds = false;
        public boolean removeAds = false;
        public int maxClk;
        public int minImp;
        public String blockKey;
        public List<String> gaidList;

        @Override
        public String toString() {
            return "BlockCfg{" +
                    "blockAds=" + blockAds +
                    ", removeAds=" + removeAds +
                    ", maxClk=" + maxClk +
                    ", minImp=" + minImp +
                    ", blockKey='" + blockKey + '\'' +
                    ", gaidList=" + gaidList +
                    '}';
        }
    }
}
