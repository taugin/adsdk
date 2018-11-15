package com.hauyu.adsdk.config;


import com.hauyu.adsdk.constant.Constant;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/9.
 */

public class HtConfig extends AttrConfig {
    private boolean enable = false;
    private long upDelay;
    private long interval;
    private int maxVersion;
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

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public long getConfigInstallTime() {
        return configInstallTime;
    }

    public void setConfigInstallTime(long configInstallTime) {
        this.configInstallTime = configInstallTime;
    }

    @Override
    public String toString() {
        return "ht{" +
                "e=" + enable +
                ", d=" + upDelay +
                ", i=" + interval +
                ", mv=" + maxVersion +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                ", cit=" + Constant.SDF_1.format(new Date(configInstallTime)) +
                '}';
    }
}
