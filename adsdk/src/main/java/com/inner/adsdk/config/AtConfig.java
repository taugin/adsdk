package com.inner.adsdk.config;

import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AtConfig extends AttrConfig {
    private boolean enable;

    private int upDelay;

    private int interval;

    private List<String> excludes;

    private boolean showOnFirstPage;

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

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isShowOnFirstPage() {
        return showOnFirstPage;
    }

    public void setShowOnFirstPage(boolean showOnFirstPage) {
        this.showOnFirstPage = showOnFirstPage;
    }

    @Override
    public String toString() {
        return "at{" +
                "e=" + enable +
                ", i=" + interval +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
