package com.inner.adaggs.config;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPolicy {
    private boolean enable = true;
    private long upDelay;
    private long interval;
    private int maxShow;
    private int maxVersion;

    private List<String> countryList;
    private List<String> attrList;
    private List<String> mediaList;

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

    public int getMaxShow() {
        return maxShow;
    }

    public void setMaxShow(int maxShow) {
        this.maxShow = maxShow;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
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
}
