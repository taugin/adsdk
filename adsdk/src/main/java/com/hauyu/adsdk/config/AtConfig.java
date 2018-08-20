package com.hauyu.adsdk.config;

import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AtConfig extends AttrConfig {
    private boolean enable;

    private int upDelay;

    private int interval;

    private int maxCount;

    private List<String> excludes;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getUpDelay() {
        return upDelay;
    }

    public void setUpDelay(int upDelay) {
        this.upDelay = upDelay;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return "AtConfig{" +
                "enable=" + enable +
                ", interval=" + interval +
                ", countryList=" + getCountryList() +
                ", attrList=" + getAttrList() +
                ", mediaList=" + getMediaList() +
                '}';
    }
}
