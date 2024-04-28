package com.mix.ads.data.config;

import android.text.TextUtils;

import com.mix.ads.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlace {

    private String name;

    private String mode; // seq : 顺序请求, con : 并发请求, ran : 随机请求

    private List<PidConfig> pidList;

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

    private List<String> ctaColor;

    private List<String> clickView;

    private int retryTimes = 0;

    private String sceneId;

    private boolean order = true;

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

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "AdPlace{" +
                "name=" + name +
                ", mode=" + mode +
                ", list=" + pidList +
                ", as=" + clickSwitch +
                ", ai=" + autoInterval +
                ", bs=" + bannerSize +
                ", uv=" + uniqueValue +
                ", loo=" + loadOnlyOnce +
                '}';
    }
}