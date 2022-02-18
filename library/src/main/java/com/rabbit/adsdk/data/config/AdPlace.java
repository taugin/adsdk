package com.rabbit.adsdk.data.config;

import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlace {

    private String name;

    private String mode; // seq : 顺序请求, con : 并发请求, ran : 随机请求

    private List<PidConfig> pidList;

    private int maxCount = 100;

    private int percent = 100;

    private boolean clickSwitch;

    private long autoInterval;

    private String uniqueValue;

    private boolean loadOnlyOnce = true;

    private boolean placeCache;

    private long delayNotifyTime;

    private boolean refShare;

    /**
     * 瀑布流请求间隔
     */
    private long waterfallInt;

    private String bannerSize;

    private long seqTimeout = 300000;

    private int queueSize = 2;

    private List<String> nativeLayout;

    private List<String> ctaColor;

    private List<String> clickView;

    private List<String> clickViewRender;

    private int retryTimes = 0;

    private String sceneId;

    private boolean sort;

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

    public List<PidConfig> getPidList() {
        return pidList;
    }

    public void setPidList(List<PidConfig> pidList) {
        this.pidList = pidList;
        setAdPlaceNames();
    }

    private void setAdPlaceNames() {
        if (pidList != null && !pidList.isEmpty()) {
            for (PidConfig config : pidList) {
                if (config != null) {
                    config.setPlaceName(name);
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

    public boolean isClickSwitch() {
        return clickSwitch;
    }

    public void setClickSwitch(boolean clickSwitch) {
        this.clickSwitch = clickSwitch;
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

    public boolean isSequence() {
        return TextUtils.equals(Constant.MODE_SEQ, getMode());
    }

    public boolean isConcurrent() {
        return TextUtils.equals(Constant.MODE_CON, getMode());
    }

    public boolean isRandom() {
        return TextUtils.equals(Constant.MODE_RAN, getMode());
    }

    public boolean isPlaceCache() {
        return placeCache;
    }

    public void setPlaceCache(boolean placeCache) {
        this.placeCache = placeCache;
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

    public long getSeqTimeout() {
        return seqTimeout;
    }

    public void setSeqTimeout(long seqTimeout) {
        this.seqTimeout = seqTimeout;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public List<String> getNativeLayout() {
        return nativeLayout;
    }

    public void setNativeLayout(List<String> nativeLayout) {
        this.nativeLayout = nativeLayout;
    }

    public List<String> getCtaColor() {
        return ctaColor;
    }

    public void setCtaColor(List<String> ctaColor) {
        this.ctaColor = ctaColor;
    }

    public List<String> getClickView() {
        return clickView;
    }

    public void setClickView(List<String> clickView) {
        this.clickView = clickView;
    }

    public List<String> getClickViewRender() {
        return clickViewRender;
    }

    public void setClickViewRender(List<String> clickViewRender) {
        this.clickViewRender = clickViewRender;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "AdPlace{" +
                "name=" + name +
                ", mode=" + mode +
                ", list=" + pidList +
                ", mc=" + maxCount +
                ", p=" + percent +
                ", as=" + clickSwitch +
                ", ai=" + autoInterval +
                ", bs=" + bannerSize +
                ", uv=" + uniqueValue +
                ", loo=" + loadOnlyOnce +
                ", nl=" + nativeLayout +
                '}';
    }
}
