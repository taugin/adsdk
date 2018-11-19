package com.hauyu.adsdk.config;


import com.hauyu.adsdk.constant.Constant;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/9.
 */

public class HtConfig extends BaseConfig {

    @Override
    public String toString() {
        return "ht{" +
                "e=" + isEnable() +
                ", d=" + getUpDelay() +
                ", i=" + getInterval() +
                ", mv=" + getMaxVersion() +
                ", mc=" + getMaxCount() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                ", cit=" + Constant.SDF_1.format(new Date(getConfigInstallTime())) +
                '}';
    }
}
