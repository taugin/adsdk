package com.bacad.ioc.gsb.scconfig;

import com.bacad.ioc.gsb.common.BCg;
import com.hauyu.adsdk.constant.Constant;

import java.util.Date;

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
                ", ntr=" + getNtr() +
                ", cit=" + Constant.SDF_1.format(new Date(getConfigInstallTime())) +
                '}';
    }
}
