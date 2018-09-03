package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StConfig extends AttrConfig {
    private boolean enable = false;

    private int upDelay;

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

    @Override
    public String toString() {
        return "st{" +
                "e=" + enable +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
