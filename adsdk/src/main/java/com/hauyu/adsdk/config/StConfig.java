package com.hauyu.adsdk.config;

/**
 * Created by Administrator on 2018-8-10.
 */

public class StConfig extends BaseConfig {

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
