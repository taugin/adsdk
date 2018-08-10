package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StConfig extends AttrConfig {
    private boolean enable = false;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "StConfig{" +
                "enable=" + enable +
                ", countryList=" + getCountryList() +
                ", attrList=" + getAttrList() +
                ", mediaList=" + getMediaList() +
                '}';
    }
}
