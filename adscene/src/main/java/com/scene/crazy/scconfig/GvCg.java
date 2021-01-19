package com.scene.crazy.scconfig;

import com.scene.crazy.base.BCg;

/**
 * Created by Administrator on 2018/2/9.
 */

public class GvCg extends BCg {

    public static final String GTPOLICY_NAME = "gt" + CONFIG_SUFFIX;

    @Override
    public String getName() {
        return GTPOLICY_NAME;
    }

    @Override
    public String toString() {
        return "gv{" +
                "e=" + isEnable() +
                ", d=" + getUpDelay() +
                ", i=" + getInterval() +
                ", mc=" + getMaxCount() +
                ", mv=" + getMaxVersion() +
                ", mi=" + getMinInterval() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                ", so=" + getScreenOrientation() +
                ", to=" + getTimeOut() +
                ", sb=" + isShowBottom() +
                '}';
    }
}
