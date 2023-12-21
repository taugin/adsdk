package com.humob.adsdk.core.framework;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.humob.adsdk.AdSdk;
import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.data.DataManager;
import com.humob.adsdk.data.config.AdPlace;
import com.humob.adsdk.data.config.PlaceConfig;
import com.humob.adsdk.log.Log;
import com.humob.adsdk.stat.EventImpl;
import com.humob.adsdk.InternalStat;
import com.humob.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LimitAdsManager {
    private static LimitAdsManager sLimitAdsManager;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat sdfSimple = new SimpleDateFormat("dd-HH:mm:ss", Locale.ENGLISH);
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private static final String DEFAULT_LIST_TEXT = "[]";
    private static final String LIMIT_TYPE_IMP = "imp";
    private static final String LIMIT_TYPE_CLK = "clk";
    private static final String LIMIT_CFG = "cfg_limit_info";
    // 最近一次的限制时间
    private static final String PREF_LAST_LIMIT_TIME = "pref_last_limit_time";
    // 最近一次的限制类型：展示 OR 点击
    private static final String PREF_LAST_LIMIT_TYPE = "pref_last_limit_type";
    // 展示时间戳列表
    private static final String PREF_IMP_TIMESTAMP_LIST = "pref_imp_timestamp_list";
    // 点击时间戳列表
    private static final String PREF_CLK_TIMESTAMP_LIST = "pref_clk_timestamp_list";

    public static LimitAdsManager get(Context context) {
        synchronized (LimitAdsManager.class) {
            if (sLimitAdsManager == null) {
                createInstance(context);
            }
        }
        return sLimitAdsManager;
    }

    private static void createInstance(Context context) {
        synchronized (LimitAdsManager.class) {
            if (sLimitAdsManager == null) {
                sLimitAdsManager = new LimitAdsManager(context);
            }
        }
    }

    private Context mContext;
    private LimitConfig mLimitConfig;

    private LimitAdsManager(Context context) {
        mContext = context;
    }

    public void recordAdImp(String sdk, String placeName, String network) {
        Log.iv(Log.TAG_SDK, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        try {
            appendAdImpTimestamp();
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
    }

    public void recordAdClick(String sdk, String placeName, String network) {
        Log.iv(Log.TAG_SDK, "sdk : " + sdk + " , place name : " + placeName + " , network : " + network);
        try {
            appendAdClkTimestamp();
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
    }

    private synchronized void appendAdImpTimestamp() {
        parseLimitConfig();
        if (mLimitConfig != null && mLimitConfig.isEnable()) {
            if (isLimitAd()) {
                Log.iv(Log.TAG_SDK, "placement is limiting while appending imp");
                return;
            }
            LinkedList<Long> linkedList = getLinkedListFromSP(PREF_IMP_TIMESTAMP_LIST);
            linkedList.offer(Long.valueOf(System.currentTimeMillis()));
            if (linkedList.size() > 50) {
                linkedList.poll();
            }
            putLinkedListToSP(PREF_IMP_TIMESTAMP_LIST, linkedList);

            checkLimit(linkedList, mLimitConfig.impLimitPolicy, LIMIT_TYPE_IMP);
        }
    }

    private synchronized void appendAdClkTimestamp() {
        if (mLimitConfig != null && mLimitConfig.isEnable()) {
            if (isLimitAd()) {
                Log.iv(Log.TAG_SDK, "placement is limiting while appending clk");
                return;
            }
            LinkedList<Long> linkedList = getLinkedListFromSP(PREF_CLK_TIMESTAMP_LIST);
            linkedList.offer(Long.valueOf(System.currentTimeMillis()));
            if (linkedList.size() > 50) {
                linkedList.poll();
            }
            putLinkedListToSP(PREF_CLK_TIMESTAMP_LIST, linkedList);

            checkLimit(linkedList, mLimitConfig.clkLimitPolicy, LIMIT_TYPE_CLK);
        }
    }

    /**
     * 检测广告限制是否满足
     */
    private void checkLimit(LinkedList<Long> linkedList, LimitConfig.LimitPolicy limitPolicy, String type) {
        if (limitPolicy != null && linkedList != null && linkedList.size() >= limitPolicy.getLimitCount()) {
            long now = System.currentTimeMillis();
            long limitDuration = limitPolicy.getLimitDuration();
            long limitCount = limitPolicy.getLimitCount();
            long listSize = linkedList.size();
            int lastLimitIndex = (int) (listSize - limitCount);
            long lastLimitTime = linkedList.get(lastLimitIndex);
            long expTime = now - lastLimitTime;
            Log.iv(Log.TAG_SDK, "now : " + now + " , last limit time : " + lastLimitTime + " , expTime : " + expTime + " , limit duration : " + limitDuration);
            if (expTime <= limitDuration) {
                Log.iv(Log.TAG_SDK, "user action abnormal, use limit placement");
                recordLastLimitStatus(type, now);
                reportLimitInfo(type);
            }
        }
    }

    private void reportLimitInfo(String type) {
        int impCount = (int) AdStatManager.get(mContext).getAllImpCount();
        int clkCount = (int) AdStatManager.get(mContext).getAllClkCount();
        String reportInfo = getLimitInfo(impCount, clkCount, type);
        Log.iv(Log.TAG_SDK, "limit ads info : " + reportInfo);
        InternalStat.reportEvent(mContext, "limit_ads_info", reportInfo);
    }

    private String getLimitInfo(int impCount, int clkCount, String type) {
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
        return String.format(Locale.ENGLISH, "%s|%s|%s|%s|%s|%d/%d", gaid, userFlag, locale, type, datetime, clkCount, impCount);
    }

    private String getUserFlag() {
        int activeDays = EventImpl.get().getActiveDays();
        if (activeDays == 0) {
            return "1";
        }
        if (activeDays > 0) {
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
            channel = locale.getCountry().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
        }
        return channel;
    }

    /**
     * 记录下满足广告限制的最后时间和类型(展示 OR 点击)
     */
    private void recordLastLimitStatus(String type, long time) {
        Log.iv(Log.TAG_SDK, "record last limit status");
        Utils.putLong(mContext, PREF_LAST_LIMIT_TIME, time);
        Utils.putString(mContext, PREF_LAST_LIMIT_TYPE, type);
    }

    private long getLastLimitTime() {
        return Utils.getLong(mContext, PREF_LAST_LIMIT_TIME);
    }

    private String getLastLimitType() {
        return Utils.getString(mContext, PREF_LAST_LIMIT_TYPE, null);
    }

    private LimitConfig.LimitPolicy getLimitPolicy(String limitType) {
        LimitConfig.LimitPolicy limitPolicy = null;
        if (mLimitConfig != null) {
            if (TextUtils.equals(limitType, LIMIT_TYPE_IMP)) {
                limitPolicy = mLimitConfig.getImpLimitPolicy();
            } else if (TextUtils.equals(limitType, LIMIT_TYPE_CLK)) {
                limitPolicy = mLimitConfig.getClkLimitPolicy();
            }
        }
        return limitPolicy;
    }

    /**
     * 判断是否需要为所有广告名称添加后缀
     * 需要添加的条件：
     * 1. 用户在一定时间间隔内展示次数超过配置值
     * OR
     * 2. 用户在一定时间间隔内点击次数超过配置值
     * AND
     * 3. 用户满足条件1和条件2后时间未超过配置值
     *
     * @return
     */
    private boolean isLimitAd() {
        boolean isInLimitTime = false;
        String limitType = getLastLimitType();
        if (!TextUtils.isEmpty(limitType)) {
            LimitConfig.LimitPolicy limitPolicy = getLimitPolicy(limitType);
            if (limitPolicy != null) {
                long lastLimitTime = getLastLimitTime();
                long nowTime = System.currentTimeMillis();
                long expTime = limitPolicy.getLimitTime() - (nowTime - lastLimitTime);
                isInLimitTime = expTime > 0;
                sdfSimple.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                String logInfo = "now : " + sdf.format(new Date(nowTime)) + " , last : " + sdf.format(new Date(lastLimitTime))
                        + " , exp : " + sdfSimple.format(new Date(expTime < 0 ? 0 : expTime)) + "(" + expTime + ")";
                Log.iv(Log.TAG_SDK, logInfo);
            }
        }
        Log.iv(Log.TAG_SDK, "limit type : " + limitType + " , in limit time : " + isInLimitTime);
        return isInLimitTime;
    }

    /**
     * 为广告名称增加后缀
     *
     * @param placeName
     * @return
     */
    public String addSuffixForPlaceNameIfNeed(String placeName) {
        String placeNameWithSuffix = placeName;
        try {
            parseLimitConfig();
            if (mLimitConfig != null && mLimitConfig.isEnable()) {
                if (isLimitAd()) {
                    String placeNameSuffix = mLimitConfig.getLimitSuffix();
                    Log.iv(Log.TAG_SDK, "place name suffix : " + placeNameSuffix);
                    if (!TextUtils.isEmpty(placeNameSuffix)) {
                        placeNameWithSuffix = placeName + placeNameSuffix;
                        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(placeNameWithSuffix);
                        // 如果远程无配置，则读取本地或者远程整体广告位配置
                        if (adPlace == null) {
                            PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
                            if (localConfig != null) {
                                adPlace = localConfig.get(placeNameWithSuffix);
                            }
                        }
                        if (adPlace == null) {
                            placeNameWithSuffix = placeName;
                        }
                        Log.iv(Log.TAG_SDK, "limit place name with suffix : " + placeNameWithSuffix + " , origin place name : " + placeName);
                    }
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
        return placeNameWithSuffix;
    }

    public boolean isLimitExclude(String sdk) {
        parseLimitConfig();
        if (mLimitConfig != null && mLimitConfig.isEnable()) {
            if (isLimitAd()) {
                List<String> limitExclude = mLimitConfig.getLimitExclude();
                if (limitExclude != null) {
                    return limitExclude.contains(sdk);
                }
            }
        }
        return false;
    }

    private void putLinkedListToSP(String prefName, LinkedList<Long> linkedList) {
        if (linkedList != null) {
            Iterator<Long> iterator = linkedList.iterator();
            if (iterator != null) {
                JSONArray jsonArray = new JSONArray();
                while (iterator.hasNext()) {
                    Long value = iterator.next();
                    jsonArray.put(value);
                }
                String content = jsonArray.toString();
                int size = jsonArray.length();
                Log.iv(Log.TAG_SDK, "put link list to sp size : " + size);
                Utils.putString(mContext, prefName, content);
            }
        }
    }

    private LinkedList<Long> getLinkedListFromSP(String prefName) {
        LinkedList<Long> linkedList = new LinkedList<>();
        try {
            String content = Utils.getString(mContext, prefName, DEFAULT_LIST_TEXT);
            stringToLinkedList(content, linkedList);
            int size = linkedList.size();
            Log.iv(Log.TAG_SDK, "get link list from sp size : " + size);
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
            Utils.putString(mContext, prefName, DEFAULT_LIST_TEXT);
        }
        return linkedList;
    }

    private void stringToLinkedList(String content, LinkedList<Long> linkedList) {
        try {
            JSONArray jsonArray = new JSONArray(content);
            int length = jsonArray.length();
            if (length > 0) {
                for (int index = 0; index < length; index++) {
                    String value = jsonArray.getString(index);
                    try {
                        linkedList.offer(Long.parseLong(value));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
    }

    /**
     * 解析配置
     */
    private void parseLimitConfig() {
        String limitConfigContent = AdSdk.get(mContext).getString(LIMIT_CFG);
        if (!TextUtils.isEmpty(limitConfigContent)) {
            String contentMD5 = Utils.string2MD5(limitConfigContent);
            if (mLimitConfig != null && TextUtils.equals(contentMD5, mLimitConfig.getContentMD5())) {
                Log.iv(Log.TAG_SDK, "limit config has parsed");
                return;
            }
            LimitConfig limitConfig = null;
            try {
                JSONObject jsonObject = new JSONObject(limitConfigContent);
                limitConfig = new LimitConfig();
                limitConfig.setContentMD5(contentMD5);
                if (jsonObject.has(LimitConfig.LIMIT_ENABLE)) {
                    limitConfig.setEnable(jsonObject.getBoolean(LimitConfig.LIMIT_ENABLE));
                }
                if (jsonObject.has(LimitConfig.LIMIT_SUFFIX)) {
                    limitConfig.setLimitSuffix(jsonObject.getString(LimitConfig.LIMIT_SUFFIX));
                }
                if (jsonObject.has(LimitConfig.LIMIT_EXCLUDE)) {
                    limitConfig.setLimitExclude(parseStringList(jsonObject.getString(LimitConfig.LIMIT_EXCLUDE)));
                }
                if (jsonObject.has(LimitConfig.LIMIT_IMP)) {
                    limitConfig.setImpLimitPolicy(parseLimitPolicy(jsonObject.getString(LimitConfig.LIMIT_IMP)));
                }
                if (jsonObject.has(LimitConfig.LIMIT_CLK)) {
                    limitConfig.setClkLimitPolicy(parseLimitPolicy(jsonObject.getString(LimitConfig.LIMIT_CLK)));
                }
            } catch (Exception e) {
            }
            mLimitConfig = limitConfig;
        } else {
            mLimitConfig = null;
        }
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

    private LimitConfig.LimitPolicy parseLimitPolicy(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            LimitConfig.LimitPolicy limitPolicy = new LimitConfig.LimitPolicy();
            if (jsonObject.has(LimitConfig.LIMIT_DURATION)) {
                limitPolicy.setLimitDuration(jsonObject.getLong(LimitConfig.LIMIT_DURATION));
            }
            if (jsonObject.has(LimitConfig.LIMIT_COUNT)) {
                limitPolicy.setLimitCount(jsonObject.getLong(LimitConfig.LIMIT_COUNT));
            }
            if (jsonObject.has(LimitConfig.LIMIT_TIME)) {
                limitPolicy.setLimitTime(jsonObject.getLong(LimitConfig.LIMIT_TIME));
            }
            return limitPolicy;
        } catch (Exception e) {
        }
        return null;
    }

    public static class LimitConfig {
        private static final String LIMIT_ENABLE = "limit_enable";
        private static final String LIMIT_SUFFIX = "limit_suffix";
        private static final String LIMIT_EXCLUDE = "limit_exclude";
        private static final String LIMIT_IMP = "limit_imp";
        private static final String LIMIT_CLK = "limit_clk";
        private static final String LIMIT_DURATION = "limit_duration";
        private static final String LIMIT_TIME = "limit_time";
        private static final String LIMIT_COUNT = "limit_count";
        private boolean enable;
        private String limitSuffix;
        private List<String> limitExclude;
        private LimitPolicy impLimitPolicy;
        private LimitPolicy clkLimitPolicy;
        private String contentMD5;

        public String getContentMD5() {
            return contentMD5;
        }

        public void setContentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getLimitSuffix() {
            return limitSuffix;
        }

        public void setLimitSuffix(String limitSuffix) {
            this.limitSuffix = limitSuffix;
        }

        public List<String> getLimitExclude() {
            return limitExclude;
        }

        public void setLimitExclude(List<String> limitExclude) {
            this.limitExclude = limitExclude;
        }

        public LimitPolicy getImpLimitPolicy() {
            return impLimitPolicy;
        }

        public void setImpLimitPolicy(LimitPolicy impLimitPolicy) {
            this.impLimitPolicy = impLimitPolicy;
        }

        public LimitPolicy getClkLimitPolicy() {
            return clkLimitPolicy;
        }

        public void setClkLimitPolicy(LimitPolicy clkLimitPolicy) {
            this.clkLimitPolicy = clkLimitPolicy;
        }

        private static class LimitPolicy {
            private long limitDuration;
            private long limitTime;
            private long limitCount;

            public void setLimitDuration(long limitDuration) {
                this.limitDuration = limitDuration;
            }

            public void setLimitTime(long limitTime) {
                this.limitTime = limitTime;
            }

            public void setLimitCount(long limitCount) {
                this.limitCount = limitCount;
            }

            public long getLimitCount() {
                return Math.min(50, limitCount);
            }

            public long getLimitDuration() {
                return limitDuration;
            }

            public long getLimitTime() {
                return limitTime;
            }
        }
    }
}
