package com.hauyu.adsdk.config;


import com.hauyu.adsdk.constant.Constant;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StConfig extends BaseConfig {

    @Override
    public String getName() {
        return Constant.STPOLICY_NAME;
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
