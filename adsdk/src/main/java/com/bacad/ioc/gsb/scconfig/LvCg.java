package com.bacad.ioc.gsb.scconfig;


import com.bacad.ioc.gsb.base.BCg;

/**
 * Created by Administrator on 2018/2/9.
 */

public class LvCg extends BCg {

    public static final String LTPOLICY_NAME = "lt" + CONFIG_SUFFIX;

    @Override
    public String getName() {
        return LTPOLICY_NAME;
    }

    @Override
    public String toString() {
        return "lt{" +
                "e=" + isEnable() +
                ", d=" + getUpDelay() +
                ", i=" + getInterval() +
                ", mv=" + getMaxVersion() +
                ", mc=" + getMaxCount() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
