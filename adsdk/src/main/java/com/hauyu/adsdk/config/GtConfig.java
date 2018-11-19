package com.hauyu.adsdk.config;

import com.hauyu.adsdk.constant.Constant;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/9.
 */

public class GtConfig extends BaseConfig {
    private boolean showBottomActivity;

    public boolean isShowBottomActivity() {
        return showBottomActivity;
    }

    public void setShowBottomActivity(boolean showBottomActivity) {
        this.showBottomActivity = showBottomActivity;
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
                ", sba=" + showBottomActivity +
                ", ntr=" + getNtRate() +
                ", cit=" + Constant.SDF_1.format(new Date(getConfigInstallTime())) +
                '}';
    }
}
