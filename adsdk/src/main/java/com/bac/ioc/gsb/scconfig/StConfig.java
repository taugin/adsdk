package com.bac.ioc.gsb.scconfig;


import com.hauyu.adsdk.common.BaseConfig;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StConfig extends BaseConfig {

    public static final String STPOLICY_NAME = "st" + CONFIG_SUFFIX;

    @Override
    public String getName() {
        return STPOLICY_NAME;
    }

    @Override
    public String toString() {
        return "st{" +
                "e=" + isEnable() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
