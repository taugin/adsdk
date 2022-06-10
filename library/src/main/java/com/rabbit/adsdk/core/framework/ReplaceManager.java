package com.rabbit.adsdk.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class ReplaceManager {

    private static final String PREF_FIRST_ACTIVE_TIME = "pref_replace_first_active_time";
    private static final String PREF_PLACE_NAME_IMP_TIMES = "pref_replace_cfg_%s_imp_times";
    private static ReplaceManager sReplaceManager;

    public static ReplaceManager get(Context context) {
        synchronized (ReplaceManager.class) {
            if (sReplaceManager == null) {
                createInstance(context);
            }
        }
        return sReplaceManager;
    }

    private static void createInstance(Context context) {
        synchronized (ReplaceManager.class) {
            if (sReplaceManager == null) {
                sReplaceManager = new ReplaceManager(context);
            }
        }
    }

    private Context mContext;
    private String mReplaceConfigMd5 = null;
    private Map<String, ReplaceConfig> mReplaceConfig;
    // 记录已替换的广告位
    private Map<String, String> mReplaceCache = new HashMap<>();

    private ReplaceManager(Context context) {
        mContext = context;
    }

    public void init() {
        recordFirstActiveTime();
    }

    private void recordFirstActiveTime() {
        long firstActiveTime = Utils.getLong(mContext, PREF_FIRST_ACTIVE_TIME);
        if (firstActiveTime <= 0) {
            Utils.putLong(mContext, PREF_FIRST_ACTIVE_TIME, System.currentTimeMillis());
        }
    }

    /**
     * 根据配置判断是否需要替换广告位
     * 原理：
     * 1. 从缓存中查询当前广告位是否已经被替换
     * 2. 如果已经替换，则直接返回替换的广告位
     * 3. 如果没有替换，则先判断当前是否是加载广告的状态
     * 4. 如果是加载广告位的状态，则根据配置文件判断是否需要替换广告位
     * @param placeName
     * @param forLoad
     * @return
     */
    public String replacePlaceName(String placeName, boolean forLoad) {
        String finalPlaceName = placeName;
        Log.iv(Log.TAG, "start to replace name : " + placeName);
        if (mReplaceCache != null) {
            finalPlaceName = mReplaceCache.get(placeName);
        }
        if (TextUtils.isEmpty(finalPlaceName)) {
            // 在加载广告位时才进行广告位替换检测
            if (forLoad) {
                parseReplaceConfig();
                if (mReplaceConfig != null) {
                    ReplaceConfig replaceConfig = mReplaceConfig.get(placeName);
                    if (replaceConfig != null) {
                        String replacePlaceName = replaceConfig.getPlaceName();
                        if (!TextUtils.isEmpty(replacePlaceName)) {
                            if ((isMatchStartupDelay(replaceConfig) || isMatchDisplayTimes(placeName, replaceConfig))
                                    && DataManager.get(mContext).isPlaceValidate(replacePlaceName)) {
                                finalPlaceName = replacePlaceName;
                                if (mReplaceCache != null) {
                                    mReplaceCache.put(placeName, finalPlaceName);
                                }
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(finalPlaceName)) {
                finalPlaceName = placeName;
            }
        }
        Log.iv(Log.TAG, "end to replace name : " + placeName + " --> " + finalPlaceName);
        return finalPlaceName;
    }

    /**
     * 解析配置文件，根据配置的MD5决定是否需要解析
     */
    private void parseReplaceConfig() {
        String replaceConfigString = DataManager.get(mContext).getString(ReplaceConfig.REPLACE_CONFIG);
        if (!TextUtils.isEmpty(replaceConfigString)) {
            String replaceConfigMd5 = Utils.string2MD5(replaceConfigString);
            if (mReplaceConfig == null || mReplaceConfig.isEmpty() || !TextUtils.equals(replaceConfigMd5, mReplaceConfigMd5)) {
                mReplaceConfigMd5 = replaceConfigMd5;
                try {
                    JSONObject jsonObject = new JSONObject(replaceConfigString);
                    mReplaceConfig = new HashMap<>();
                    Iterator<String> iterator = jsonObject.keys();
                    if (iterator != null) {
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (!TextUtils.isEmpty(key)) {
                                String value = jsonObject.getString(key);
                                ReplaceConfig replaceConfig = null;
                                if (!TextUtils.isEmpty(value)) {
                                    try {
                                        JSONObject jobj = new JSONObject(value);
                                        replaceConfig = new ReplaceConfig();
                                        if (jobj.has(ReplaceConfig.KEY_PLACE_NAME)) {
                                            replaceConfig.setPlaceName(jobj.getString(ReplaceConfig.KEY_PLACE_NAME));
                                        }
                                        if (jobj.has(ReplaceConfig.KEY_STARTUP_DELAY)) {
                                            replaceConfig.setStartupDelay(jobj.getLong(ReplaceConfig.KEY_STARTUP_DELAY));
                                        }
                                        if (jobj.has(ReplaceConfig.KEY_DISPLAY_TIMES)) {
                                            replaceConfig.setDisplayTimes(jobj.getInt(ReplaceConfig.KEY_DISPLAY_TIMES));
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                if (replaceConfig != null) {
                                    mReplaceConfig.put(key, replaceConfig);
                                }
                            }
                        }
                        Log.iv(Log.TAG, "mReplaceConfig : " + mReplaceConfig);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 判断启动延迟时间是否满足
     * @param replaceConfig
     * @return
     */
    private boolean isMatchStartupDelay(ReplaceConfig replaceConfig) {
        if (replaceConfig != null) {
            long startupDelay = replaceConfig.getStartupDelay();
            if (startupDelay > 0) {
                long firstActiveTime = Utils.getLong(mContext, PREF_FIRST_ACTIVE_TIME);
                long now = System.currentTimeMillis();
                long expTime = now - firstActiveTime;
                Log.iv(Log.TAG, "active time : " + firstActiveTime + " , now : " + now + " , expTime : " + expTime);
                return expTime >= startupDelay;
            }
        }
        return false;
    }

    /**
     * 判断展示次数是否满足
     * @param placeName
     * @param replaceConfig
     * @return
     */
    private boolean isMatchDisplayTimes(String placeName, ReplaceConfig replaceConfig) {
        if (replaceConfig != null) {
            long displayTimes = replaceConfig.getDisplayTimes();
            if (displayTimes > 0) {
                String prefKey = String.format(Locale.getDefault(), PREF_PLACE_NAME_IMP_TIMES, placeName);
                long totalImpTimes = Utils.getLong(mContext, prefKey, 0);
                Log.iv(Log.TAG, "total imp times : " + totalImpTimes + " , display times : " + displayTimes);
                return totalImpTimes >= displayTimes;
            }
        }
        return false;
    }

    /**
     * 记录总展示次数
     * @param placeName
     */
    public void reportAdImp(String placeName) {
        try {
            String prefKey = String.format(Locale.getDefault(), PREF_PLACE_NAME_IMP_TIMES, placeName);
            long lastImpTimes = Utils.getLong(mContext, prefKey, 0);
            Utils.putLong(mContext, prefKey, lastImpTimes + 1);
        } catch (Exception e) {
        }
    }

    public class ReplaceConfig {
        public static final String REPLACE_CONFIG = "adcfg_replace_info";
        public static final String KEY_PLACE_NAME = "place_name";
        public static final String KEY_STARTUP_DELAY = "startup_delay";
        public static final String KEY_DISPLAY_TIMES = "display_times";
        private String placeName;
        private long startupDelay;
        private int displayTimes;

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public long getStartupDelay() {
            return startupDelay;
        }

        public void setStartupDelay(long startupDelay) {
            this.startupDelay = startupDelay;
        }

        public int getDisplayTimes() {
            return displayTimes;
        }

        public void setDisplayTimes(int displayTimes) {
            this.displayTimes = displayTimes;
        }

        @Override
        public String toString() {
            return "ReplaceConfig{" +
                    "placeName='" + placeName + '\'' +
                    ", startupDelay=" + startupDelay +
                    ", displayTimes=" + displayTimes +
                    '}';
        }
    }
}
