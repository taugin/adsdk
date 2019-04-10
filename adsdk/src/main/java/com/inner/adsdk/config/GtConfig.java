package com.inner.adsdk.config;

import com.inner.adsdk.constant.Constant;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/9.
 */

public class GtConfig extends BaseConfig {

    @Override
    public String getName() {
        return Constant.GTPOLICY_NAME;
    }

    @Override
    public String toString() {
        return "gt{" +
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
                ", sba=" + isShowBottomActivity() +
                ", ntr=" + getNtRate() +
                ", cit=" + Constant.SDF_1.format(new Date(getConfigInstallTime())) +
                '}';
    }
}
