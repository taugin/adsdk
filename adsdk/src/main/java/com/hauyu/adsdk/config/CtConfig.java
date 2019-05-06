package com.hauyu.adsdk.config;


import com.hauyu.adsdk.common.BaseConfig;
import com.hauyu.adsdk.constant.Constant;

/**
 * Created by Administrator on 2018-8-10.
 */

public class CtConfig extends BaseConfig {

    @Override
    public String getName() {
        return Constant.CTPOLICY_NAME;
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
