package com.tiger.adsdk.core.framework;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tiger.adsdk.AdSdk;
import com.tiger.adsdk.data.DataManager;
import com.tiger.adsdk.log.Log;
import com.tiger.adsdk.stat.EventImpl;
import com.tiger.adsdk.stat.InternalStat;
import com.tiger.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class BounceRateManager implements ActivityMonitor.OnAppMonitorCallback {

    private static BounceRateManager sBounceRateManager;

    public static BounceRateManager get(Context context) {
        synchronized (BounceRateManager.class) {
            if (sBounceRateManager == null) {
                createInstance(context);
            }
        }
        return sBounceRateManager;
    }

    private static void createInstance(Context context) {
        synchronized (BounceRateManager.class) {
            if (sBounceRateManager == null) {
                sBounceRateManager = new BounceRateManager(context);
            }
        }
    }

    private Context mContext;
    private Map<String, Object> mExtra;
    private long mAdClickTime = 0;
    private volatile boolean mAdClick = false;
    private String mPid;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BounceRateManager(Context context) {
        mContext = context;
    }

    public void init() {
        ActivityMonitor.get(mContext).setOnAppMonitorCallback(this);
    }

    public void onAdClick(String pid, Map<String, Object> extra) {
        if (isInvalidDurationCheckEnable()) {
            mExtra = extra;
            mPid = pid;
            Log.iv(Log.TAG, "App ad was clicked.");
            mAdClick = true;
            mHandler.removeCallbacks(mResetRunnable);
            mHandler.postDelayed(mResetRunnable, getResetTime());
        }
    }

    private long getResetTime() {
        return getInvalidDuration() + 1000;
    }

    private Runnable mResetRunnable = new Runnable() {
        @Override
        public void run() {
            mAdClick = false;
            mAdClickTime = 0;
            mExtra = null;
        }
    };

    @Override
    public void onForeground(boolean fromBackground, WeakReference<Activity> weakReference) {
        String topClass = "";
        if (weakReference != null) {
            Activity activity = weakReference.get();
            if (activity != null) {
                topClass = " : " + activity.getClass();
            }
        }
        Log.iv(Log.TAG, "App is still foreground." + topClass);
        if (isInvalidDurationCheckEnable()) {
            if (mAdClick) {
                if (mAdClickTime > 0) {
                    long now = System.currentTimeMillis();
                    long adDuration = now - mAdClickTime;
                    long invalidDuration = getInvalidDuration();
                    boolean invalidAdClick = adDuration < invalidDuration;
                    Log.iv(Log.TAG, "ad duration time : " + adDuration + " , invalidAdClick : " + invalidAdClick);
                    mAdClickTime = 0;
                    if (invalidAdClick) {
                        if (mExtra != null) {
                            mExtra.put("ivtime", adDuration);
                        }
                        EventImpl.get().reportEvent(mContext, "e_ad_ivclk", null, mExtra);
                        Log.iv(Log.TAG, "report invalid ad click : " + mExtra);
                        mExtra = null;
                    }
                    processMistakeClick(mPid, adDuration);
                } else {
                    mAdClickTime = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public void onBackground() {
        Log.iv(Log.TAG, "App went background.");
        if (isInvalidDurationCheckEnable()) {
            if (mAdClick) {
                mAdClickTime = System.currentTimeMillis();
            }
        }
    }

    private long getInvalidDuration() {
        long invalidDuration = 2500;
        String durationString = DataManager.get(mContext).getString("ad_invalid_traffic_check_duration");
        if (!TextUtils.isEmpty(durationString)) {
            try {
                invalidDuration = Long.parseLong(durationString);
            } catch (Exception e) {
            }
        }
        return invalidDuration;
    }

    private boolean isInvalidDurationCheckEnable() {
        boolean ivtCheckEnable = true;
        String checkEnable = DataManager.get(mContext).getString("ad_invalid_traffic_check_enable");
        if (!TextUtils.isEmpty(checkEnable)) {
            try {
                ivtCheckEnable = Boolean.parseBoolean(checkEnable);
            } catch (Exception e) {
            }
        }
        return ivtCheckEnable;
    }


    private static final String MSCLK_CFG = "cfg_msclk_info";
    private static final String MSCLK_PLACEMENT = "msclk_placement";
    private static final String MSCLK_BLOCK = "msclk_block";
    private static final String MSCLK_DURATION = "msclk_duration";
    private static final String MSCLK_DELAY_TIME = "msclk_delay_time";
    private static final String MSCLK_TRIGGER_COUNT = "msclk_trigger_count";
    private static final String PREF_MISTAKE_TIME = "pref_%s_mistake_time";
    private static final String PREF_MISTAKE_COUNT = "pref_%s_mistake_count";
    private Map<String, MsClkCfg> mMsClkCfgMap;
    private String mMsClkCfgMd5 = null;

    private void processMistakeClick(String pid, long adClickDuration) {
        if (TextUtils.isEmpty(pid)) {
            Log.iv(Log.TAG, "bounce rate empty pid : " + pid);
            return;
        }
        if (adClickDuration <= 0) {
            Log.iv(Log.TAG, "bounce rate less 0 time : " + adClickDuration);
            return;
        }
        parseMsClkConfig();
        checkMistakeClick(pid, adClickDuration);
    }

    private void checkMistakeClick(String pid, long adClickDuration) {
        try {
            if (mMsClkCfgMap != null && !mMsClkCfgMap.isEmpty()) {
                String pidMd5 = Utils.string2MD5(pid);
                MsClkCfg msClkCfg = mMsClkCfgMap.get(pidMd5);
                if (msClkCfg != null) {
                    long msclkDuration = msClkCfg.getMsClkDuration();
                    if (adClickDuration <= msclkDuration) {
                        String prefKey = String.format(Locale.ENGLISH, PREF_MISTAKE_TIME, pidMd5);
                        Utils.putLong(mContext, prefKey, System.currentTimeMillis());
                        prefKey = String.format(Locale.ENGLISH, PREF_MISTAKE_COUNT, pidMd5);
                        long triggerCount = Utils.getLong(mContext, prefKey, 0) + +1;
                        Utils.putLong(mContext, prefKey, triggerCount);
                        Log.iv(Log.TAG, "pid : " + pid + " mistake click");
                        String msClkAdsInfo = String.format(Locale.ENGLISH, "%s|%s", pid, msclkDuration);
                        InternalStat.reportEvent(mContext, "msclk_ads_info", msClkAdsInfo);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public boolean blockMistakePid(String pid) {
        parseMsClkConfig();
        if (mMsClkCfgMap != null && !mMsClkCfgMap.isEmpty()) {
            String pidMd5 = Utils.string2MD5(pid);
            MsClkCfg msClkCfg = mMsClkCfgMap.get(pidMd5);
            if (msClkCfg != null) {
                boolean isBlock = msClkCfg.isMsClkBlock();
                int triggerCount = msClkCfg.getMsClkTriggerCount();
                long mistakeDelayTime = msClkCfg.getMsClkDelayTime();
                String prefKey = String.format(Locale.ENGLISH, PREF_MISTAKE_TIME, pidMd5);
                long mistakeTime = Utils.getLong(mContext, prefKey, 0);
                prefKey = String.format(Locale.ENGLISH, PREF_MISTAKE_COUNT, pidMd5);
                long mistakeCount = Utils.getLong(mContext, prefKey, 0);
                boolean blockPid = isBlock && mistakeCount >= triggerCount && System.currentTimeMillis() < mistakeTime + mistakeDelayTime;
                return blockPid;
            }
        }
        return false;
    }

    private void parseMsClkConfig() {
        String msclkConfigContent = AdSdk.get(mContext).getString(MSCLK_CFG);
        if (!TextUtils.isEmpty(msclkConfigContent)) {
            String contentMD5 = Utils.string2MD5(msclkConfigContent);
            if (mMsClkCfgMap != null && !mMsClkCfgMap.isEmpty() && TextUtils.equals(contentMD5, mMsClkCfgMd5)) {
                Log.iv(Log.TAG_SDK, "msclk config has parsed");
                return;
            }
            mMsClkCfgMd5 = contentMD5;
            try {
                JSONArray jsonArray = new JSONArray(msclkConfigContent);
                if (jsonArray != null && jsonArray.length() > 0) {
                    mMsClkCfgMap = new HashMap<>();
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        MsClkCfg msclkConfig = new MsClkCfg();
                        if (jsonObject.has(MSCLK_PLACEMENT)) {
                            msclkConfig.setMsClkPlacement(jsonObject.getString(MSCLK_PLACEMENT));
                        }
                        if (jsonObject.has(MSCLK_BLOCK)) {
                            msclkConfig.setMsClkBlock(jsonObject.getBoolean(MSCLK_BLOCK));
                        }
                        if (jsonObject.has(MSCLK_DURATION)) {
                            msclkConfig.setMsClkDuration(jsonObject.getLong(MSCLK_DURATION));
                        }
                        if (jsonObject.has(MSCLK_DELAY_TIME)) {
                            msclkConfig.setMsClkDelayTime(jsonObject.getLong(MSCLK_DELAY_TIME));
                        }
                        if (jsonObject.has(MSCLK_TRIGGER_COUNT)) {
                            msclkConfig.setMsClkTriggerCount(jsonObject.getInt(MSCLK_TRIGGER_COUNT));
                        }
                        String pid = msclkConfig.getMsClkPlacement();
                        if (!TextUtils.isEmpty(pid)) {
                            mMsClkCfgMap.put(Utils.string2MD5(pid), msclkConfig);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        Log.iv(Log.TAG, "mMsClkCfgMap : " + mMsClkCfgMap);
    }

    private class MsClkCfg {
        /**
         * 检测的广告位ID
         */
        private String msClkPlacement;
        /**
         * 是否阻塞误点击的广告位
         */
        private boolean msClkBlock;
        /**
         * 误点击的时间差
         */
        private long msClkDuration;
        /**
         * 广告位被阻塞的时长
         */
        private long msClkDelayTime;
        /**
         * 广告位触发误点击的最小次数
         */
        private int msClkTriggerCount = 1;

        public String getMsClkPlacement() {
            return msClkPlacement;
        }

        public void setMsClkPlacement(String msClkPlacement) {
            this.msClkPlacement = msClkPlacement;
        }

        public boolean isMsClkBlock() {
            return msClkBlock;
        }

        public void setMsClkBlock(boolean msClkBlock) {
            this.msClkBlock = msClkBlock;
        }

        public long getMsClkDuration() {
            return msClkDuration;
        }

        public void setMsClkDuration(long msClkDuration) {
            this.msClkDuration = msClkDuration;
        }

        public long getMsClkDelayTime() {
            return msClkDelayTime;
        }

        public void setMsClkDelayTime(long msClkDelayTime) {
            this.msClkDelayTime = msClkDelayTime;
        }

        public int getMsClkTriggerCount() {
            return msClkTriggerCount;
        }

        public void setMsClkTriggerCount(int msClkTriggerCount) {
            this.msClkTriggerCount = msClkTriggerCount;
        }
    }
}
