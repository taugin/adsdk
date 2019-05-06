package com.inner.adsdk.config;

import com.inner.adsdk.common.BaseConfig;
import com.inner.adsdk.constant.Constant;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/9.
 */

public class LtConfig extends BaseConfig {

    @Override
    public String getName() {
        return Constant.LTPOLICY_NAME;
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
                ", cit=" + Constant.SDF_1.format(new Date(getConfigInstallTime())) +
                '}';
    }
}
