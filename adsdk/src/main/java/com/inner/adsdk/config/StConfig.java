package com.inner.adsdk.config;

import com.inner.adsdk.common.BaseConfig;
import com.inner.adsdk.constant.Constant;

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
