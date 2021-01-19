package com.scene.crazy.scconfig;


import com.scene.crazy.base.BCg;

/**
 * Created by Administrator on 2018/2/9.
 */

public class HvCg extends BCg {

    public static final String HTPOLICY_NAME = "ht" + CONFIG_SUFFIX;

    @Override
    public String getName() {
        return HTPOLICY_NAME;
    }

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
                '}';
    }
}
