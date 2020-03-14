package com.bacad.ioc.gsb.common;

import java.util.List;

/**
 * Created by Administrator on 2018/7/18.
 */

public abstract class BCg {

    protected static String CONFIG_SUFFIX = "config";

    private boolean enable = false;
    private long upDelay;
    private long interval;
    private int maxCount;
    private int maxVersion;
    private long minInterval;
    // 默认竖屏方向展示
    private int screenOrientation = 1;
    private long timeOut = 300000;
    private List<String> countryList;
    private List<String> attrList;
    private List<String> mediaList;
    private List<String> verList;
    private boolean showBottom = true;
    private String adExtra;
    private String adMain;
    private long sceneInterval = 300000;
    private long delayClose = 0;
    private boolean excludeAndroidQ = true;
    private boolean useRealTime = true;
    private List<String> exIps;

    public abstract String getName();

    private void initValue() {
        enable = false;
        upDelay = 0;
        interval = 0;
        maxCount = 0;
        maxVersion = 0;
        minInterval = 0;
        screenOrientation = 0;
        timeOut = 300000;
        countryList = null;
        attrList = null;
        mediaList = null;
        verList = null;
        showBottom = true;
        adExtra = null;
        adMain = null;
        sceneInterval = 300000;
        delayClose = 0;
        excludeAndroidQ = true;
        useRealTime = true;
        exIps = null;
    }

    public BCg() {
        initValue();
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getUpDelay() {
        return upDelay;
    }

    public void setUpDelay(long upDelay) {
        this.upDelay = upDelay;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public long getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(long minInterval) {
        this.minInterval = minInterval;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public List<String> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<String> countryList) {
        this.countryList = countryList;
    }

    public List<String> getAttrList() {
        return attrList;
    }

    public void setAttrList(List<String> attrList) {
        this.attrList = attrList;
    }

    public List<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<String> mediaList) {
        this.mediaList = mediaList;
    }

    public List<String> getVerList() {
        return verList;
    }

    public void setVerList(List<String> verList) {
        this.verList = verList;
    }

    public boolean isShowBottom() {
        return showBottom;
    }

    public void setShowBottom(boolean showBottom) {
        this.showBottom = showBottom;
    }

    public String getAdExtra() {
        return adExtra;
    }

    public void setAdExtra(String adExtra) {
        this.adExtra = adExtra;
    }

    public String getAdMain() {
        return adMain;
    }

    public void setAdMain(String adMain) {
        this.adMain = adMain;
    }

    public long getSceneInterval() {
        return sceneInterval;
    }

    public void setSceneInterval(long sceneInterval) {
        this.sceneInterval = sceneInterval;
    }

    public long getDelayClose() {
        return delayClose;
    }

    public void setDelayClose(long delayClose) {
        this.delayClose = delayClose;
    }

    public boolean isExcludeAndroidQ() {
        return excludeAndroidQ;
    }

    public void setExcludeAndroidQ(boolean excludeAndroidQ) {
        this.excludeAndroidQ = excludeAndroidQ;
    }

    public boolean isUseRealTime() {
        return useRealTime;
    }

    public void setUseRealTime(boolean useRealTime) {
        this.useRealTime = useRealTime;
    }

    public List<String> getExIps() {
        return exIps;
    }

    public void setExIps(List<String> exIps) {
        this.exIps = exIps;
    }

    public void clear() {
        initValue();
    }
}
