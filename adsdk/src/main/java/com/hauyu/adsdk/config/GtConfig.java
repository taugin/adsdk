package com.hauyu.adsdk.config;

/**
 * Created by Administrator on 2018/2/9.
 */

public class GtConfig extends AttrConfig {
    private boolean enable = false;
    private long upDelay;
    private long interval;
    private int maxCount;
    private int maxVersion;
    private long minInterval;
    private int screenOrientation;
    private long timeOut = 300000;
    private boolean showBottomActivity = true;
    private long configInstallTime;

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

    public boolean isShowBottomActivity() {
        return showBottomActivity;
    }

    public void setShowBottomActivity(boolean showBottomActivity) {
        this.showBottomActivity = showBottomActivity;
    }

    public long getConfigInstallTime() {
        return configInstallTime;
    }

    public void setConfigInstallTime(long configInstallTime) {
        this.configInstallTime = configInstallTime;
    }

    @Override
    public String toString() {
        return "gt{" +
                "e=" + enable +
                ", d=" + upDelay +
                ", i=" + interval +
                ", mc=" + maxCount +
                ", mv=" + maxVersion +
                ", mi=" + minInterval +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                ", so=" + screenOrientation +
                ", to=" + timeOut +
                '}';
    }
}
