package com.inner.adaggs.config;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdvInner {
    // 广告内策略
    private InnerPolicy innerPolicy;

    // 广告配置
    private List<AdPlace> adPlaceList;


    public InnerPolicy getInnerPolicy() {
        return innerPolicy;
    }

    public void setInnerPolicy(InnerPolicy innerPolicy) {
        this.innerPolicy = innerPolicy;
    }

    public List<AdPlace> getAdPlaceList() {
        return adPlaceList;
    }

    public void setAdPlaceList(List<AdPlace> adPlaceList) {
        this.adPlaceList = adPlaceList;
    }

    /**
     * 通过名字获取广告配置
     *
     * @param name
     * @return
     */
    public AdPlace get(String name) {
        if (adPlaceList == null || adPlaceList.isEmpty() || TextUtils.isEmpty(name)) {
            return null;
        }
        for (AdPlace config : adPlaceList) {
            if (config != null && name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "AdvInner{" +
                "innerPolicy=" + innerPolicy +
                ", adPlaceList=" + adPlaceList +
                '}';
    }
}
