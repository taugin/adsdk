package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018-8-10.
 */

public class TtConfig extends AttrConfig {
    private boolean enable;

    private int interval;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "TtConfig{" +
                "enable=" + enable +
                ", interval=" + interval +
                ", countryList=" + getCountryList() +
                ", attrList=" + getAttrList() +
                ", mediaList=" + getMediaList() +
                '}';
    }
}
