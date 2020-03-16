package com.bacad.ioc.gsb.scconfig;


import com.bacad.ioc.gsb.base.BCg;

/**
 * Created by Administrator on 2018-8-10.
 */

public class CvCg extends BCg {

    public static final String CTPOLICY_NAME = "ct" + CONFIG_SUFFIX;

    @Override
    public String getName() {
        return CTPOLICY_NAME;
    }

    private long disableInterval;

    public long getDisableInterval() {
        return disableInterval;
    }

    public void setDisableInterval(long disableInterval) {
        this.disableInterval = disableInterval;
    }

    @Override
    public String toString() {
        return "ct{" +
                "e=" + isEnable() +
                ", d=" + getUpDelay() +
                ", i=" + getInterval() +
                ", mc=" + getMaxCount() +
                ", mv=" + getMaxVersion() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
