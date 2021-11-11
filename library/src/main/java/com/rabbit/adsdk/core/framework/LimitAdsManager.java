package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PlaceConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

public class LimitAdsManager {
    private static LimitAdsManager sLimitAdsManager;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final String DEFAULT_LIST_TEXT = "[]";
    private static final String LIMIT_TYPE_IMP = "imp";
    private static final String LIMIT_TYPE_CLK = "clk";
    private static final String LIMIT_CFG = "limitcfg";
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

    public void recordAdImp(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        try {
            appendAdImpTimestamp();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    public void recordAdClick(String sdk, String placeName, String render) {
        Log.iv(Log.TAG, "sdk : " + sdk + " , place name : " + placeName + " , render : " + render);
        try {
            appendAdClkTimestamp();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private synchronized void appendAdImpTimestamp() {
        updateLimitConfig();
        if (mLimitConfig != null) {
            if (isLimitAd()) {
                Log.iv(Log.TAG, "ad is limiting while appending imp");
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
        if (mLimitConfig != null) {
            if (isLimitAd()) {
                Log.iv(Log.TAG, "ad is limiting while appending clk");
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
            Log.iv(Log.TAG, "now : " + now + " , last limit time : " + lastLimitTime + " , expTime : " + expTime + " , limit duration : " + limitDuration);
            if (expTime <= limitDuration) {
                recordLastLimitStatus(type, now);
            }
        }
    }

    /**
     * 记录下满足广告限制的最后时间和类型(展示 OR 点击)
     */
    private void recordLastLimitStatus(String type, long time) {
        Log.iv(Log.TAG, "record last limit status");
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
     * 更新广告限制配置
     */
    private void updateLimitConfig() {
        mLimitConfig = parseLimitConfig();
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
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                Log.iv(Log.TAG, "now : " + sdf.format(new Date(nowTime)) + " , last : " + sdf.format(new Date(lastLimitTime)) + " , exp : " + expTime);
                isInLimitTime = expTime > 0;
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                if (expTime < 0 ) {
                    expTime = 0;
                }
                Log.iv(Log.TAG, "exp time : " + sdf.format(new Date(expTime)));
            }
        }
        Log.iv(Log.TAG, "limit type : " + limitType + " , in limit time : " + isInLimitTime);
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
            updateLimitConfig();
            if (isLimitAd() && mLimitConfig != null) {
                String placeNameSuffix = mLimitConfig.getPlaceNameSuffix();
                Log.iv(Log.TAG, "place name suffix : " + placeNameSuffix);
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
                }
            }
            Log.iv(Log.TAG, "place name with suffix : " + placeNameWithSuffix);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return placeNameWithSuffix;
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
                Log.iv(Log.TAG, "put link list to sp size : " + size);
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
            Log.iv(Log.TAG, "get link list from sp size : " + size);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
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
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private LimitConfig parseLimitConfig() {
        String limitConfigContent = AdSdk.get(mContext).getString(LIMIT_CFG);
        if (!TextUtils.isEmpty(limitConfigContent)) {
            String contentMD5 = Utils.string2MD5(limitConfigContent);
            if (mLimitConfig != null && TextUtils.equals(contentMD5, mLimitConfig.getContentMD5())) {
                Log.iv(Log.TAG, "limit config has parsed");
                return mLimitConfig;
            }
            try {
                JSONObject jsonObject = new JSONObject(limitConfigContent);
                LimitConfig limitConfig = new LimitConfig();
                limitConfig.setContentMD5(contentMD5);
                if (jsonObject.has(LimitConfig.LIMIT_ENABLE)) {
                    limitConfig.setEnable(jsonObject.getBoolean(LimitConfig.LIMIT_ENABLE));
                }
                if (jsonObject.has(LimitConfig.LIMIT_SUFFIX)) {
                    limitConfig.setPlaceNameSuffix(jsonObject.getString(LimitConfig.LIMIT_SUFFIX));
                }
                if (jsonObject.has(LimitConfig.LIMIT_IMP)) {
                    limitConfig.setImpLimitPolicy(parseLimitPolicy(jsonObject.getString(LimitConfig.LIMIT_IMP)));
                }
                if (jsonObject.has(LimitConfig.LIMIT_CLK)) {
                    limitConfig.setClkLimitPolicy(parseLimitPolicy(jsonObject.getString(LimitConfig.LIMIT_CLK)));
                }
                return limitConfig;
            } catch (Exception e) {
            }
        }
        return null;
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
        private static final String LIMIT_ENABLE = "le";
        private static final String LIMIT_SUFFIX = "ls";
        private static final String LIMIT_IMP = "li";
        private static final String LIMIT_CLK = "lc";
        private static final String LIMIT_DURATION = "ld";
        private static final String LIMIT_TIME = "lt";
        private static final String LIMIT_COUNT = "lc";
        private boolean enable;
        private String placeNameSuffix;
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

        public String getPlaceNameSuffix() {
            return placeNameSuffix;
        }

        public void setPlaceNameSuffix(String placeNameSuffix) {
            this.placeNameSuffix = placeNameSuffix;
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
