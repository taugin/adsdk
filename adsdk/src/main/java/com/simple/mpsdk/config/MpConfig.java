package com.simple.mpsdk.config;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class MpConfig {

    private List<MpPlace> mpPlaceList;

    public List<MpPlace> getMpPlaceList() {
        return mpPlaceList;
    }

    public void setMpPlaceList(List<MpPlace> mpPlaceList) {
        this.mpPlaceList = mpPlaceList;
    }

    /**
     * 通过名字获取广告配置
     *
     * @param name
     * @return
     */
    public MpPlace get(String name) {
        if (mpPlaceList == null || mpPlaceList.isEmpty() || TextUtils.isEmpty(name)) {
            return null;
        }
        for (MpPlace config : mpPlaceList) {
            if (config != null && name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "adc{" +
                ", list=" + mpPlaceList +
                '}';
    }
}
