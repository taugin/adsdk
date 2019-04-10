package com.inner.adsdk.config;

import java.util.List;

/**
 * Created by Administrator on 2018/7/18.
 */

public abstract class BaseConfig {
    private boolean enable = false;
    private long upDelay;
    private long interval;
    private int maxCount;
    private int maxVersion;
    private long minInterval;
    private int screenOrientation;
    private long configInstallTime;
    private long timeOut = 300000;
    private List<String> countryList;
    private List<String> attrList;
    private List<String> mediaList;
    private int ntRate;
    private boolean showBottomActivity = true;
    private String placeNameInt;
    private String placeNameAdv;

    public abstract String getName();

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

    public long getConfigInstallTime() {
        return configInstallTime;
    }

    public void setConfigInstallTime(long configInstallTime) {
        this.configInstallTime = configInstallTime;
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

    public int getNtRate() {
        return ntRate;
    }

    public void setNtRate(int ntRate) {
        this.ntRate = ntRate;
    }

    public boolean isShowBottomActivity() {
        return showBottomActivity;
    }

    public void setShowBottomActivity(boolean showBottomActivity) {
        this.showBottomActivity = showBottomActivity;
    }

    public String getPlaceNameInt() {
        return placeNameInt;
    }

    public void setPlaceNameInt(String placeNameInt) {
        this.placeNameInt = placeNameInt;
    }

    public String getPlaceNameAdv() {
        return placeNameAdv;
    }

    public void setPlaceNameAdv(String placeNameAdv) {
        this.placeNameAdv = placeNameAdv;
    }
}
