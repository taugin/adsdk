package com.hauyu.adsdk.config;

import android.text.TextUtils;

import com.hauyu.adsdk.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlace {

    private String name;

    private String mode; // seq : 顺序请求, con : 并发请求, ran : 随机请求

    private List<PidConfig> pidsList;

    private int maxCount = 100;

    private int percent = 100;

    private boolean autoSwitch;

    private long autoInterval;

    private String uniqueValue;

    private boolean loadOnlyOnce = true;

    private int ecpmSort;

    private boolean needCache;

    private long delayNotifyTime;

    private boolean refShare;

    private boolean globalCache;

    /**
     * 瀑布流请求间隔
     */
    private long waterfallInt;

    private String bannerSize;

    private boolean highEcpm;

    private String placeType;

    private long seqTimeout = 300000;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<PidConfig> getPidsList() {
        return pidsList;
    }

    public void setPidsList(List<PidConfig> pidsList) {
        this.pidsList = pidsList;
        setAdPlaceNames();
    }

    private void setAdPlaceNames() {
        if (pidsList != null && !pidsList.isEmpty()) {
            for (PidConfig config : pidsList) {
                if (config != null) {
                    config.setAdPlaceName(name);
                }
            }
        }
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public boolean isAutoSwitch() {
        return autoSwitch;
    }

    public void setAutoSwitch(boolean autoSwitch) {
        this.autoSwitch = autoSwitch;
    }

    public long getAutoInterval() {
        return autoInterval;
    }

    public void setAutoInterval(long autoInterval) {
        this.autoInterval = autoInterval;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public boolean isLoadOnlyOnce() {
        return loadOnlyOnce;
    }

    public void setLoadOnlyOnce(boolean loadOnlyOnce) {
        this.loadOnlyOnce = loadOnlyOnce;
    }

    public int getEcpmSort() {
        return ecpmSort;
    }

    public void setEcpmSort(int ecpmSort) {
        this.ecpmSort = ecpmSort;
    }

    public boolean isSequence() {
        return TextUtils.equals(Constant.MODE_SEQ, getMode());
    }

    public boolean isConcurrent() {
        return TextUtils.equals(Constant.MODE_CON, getMode());
    }

    public boolean isRandom() {
        return TextUtils.equals(Constant.MODE_RAN, getMode());
    }

    public boolean isNeedCache() {
        return needCache;
    }

    public void setNeedCache(boolean needCache) {
        this.needCache = needCache;
    }

    public long getDelayNotifyTime() {
        return delayNotifyTime;
    }

    public void setDelayNotifyTime(long delayNotifyTime) {
        this.delayNotifyTime = delayNotifyTime;
    }

    public boolean isRefShare() {
        return refShare;
    }

    public void setRefShare(boolean refShare) {
        this.refShare = refShare;
    }

    public boolean isGlobalCache() {
        return globalCache;
    }

    public void setGlobalCache(boolean globalCache) {
        this.globalCache = globalCache;
    }

    public long getWaterfallInt() {
        return waterfallInt;
    }

    public void setWaterfallInt(long waterfallInt) {
        this.waterfallInt = waterfallInt;
    }

    public String getBannerSize() {
        return bannerSize;
    }

    public void setBannerSize(String bannerSize) {
        this.bannerSize = bannerSize;
    }

    public boolean isHighEcpm() {
        return highEcpm;
    }

    public void setHighEcpm(boolean highEcpm) {
        this.highEcpm = highEcpm;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public long getSeqTimeout() {
        return seqTimeout;
    }

    public void setSeqTimeout(long seqTimeout) {
        this.seqTimeout = seqTimeout;
    }

    @Override
    public String toString() {
        return "AdPlace{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", list=" + pidsList +
                ", mc=" + maxCount +
                ", p=" + percent +
                ", as=" + autoSwitch +
                ", ai=" + autoInterval +
                ", bs=" + bannerSize +
                ", uv='" + uniqueValue + '\'' +
                ", loo=" + loadOnlyOnce +
                '}';
    }
}
