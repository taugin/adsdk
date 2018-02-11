package com.inner.adaggs.config;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdInners {
    // 广告内策略
    private InnerPolicy innerPolicy;

    // 广告配置
    private List<AdPlaceConfig> adPlaceList;


    public InnerPolicy getInnerPolicy() {
        return innerPolicy;
    }

    public void setInnerPolicy(InnerPolicy innerPolicy) {
        this.innerPolicy = innerPolicy;
    }

    public List<AdPlaceConfig> getAdPlaceList() {
        return adPlaceList;
    }

    public void setAdPlaceList(List<AdPlaceConfig> adPlaceList) {
        this.adPlaceList = adPlaceList;
    }

    /**
     * 通过名字获取广告配置
     *
     * @param name
     * @return
     */
    public AdPlaceConfig get(String name) {
        if (adPlaceList == null || adPlaceList.isEmpty() || TextUtils.isEmpty(name)) {
            return null;
        }
        for (AdPlaceConfig config : adPlaceList) {
            if (config != null && name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "AdInners{" +
                "innerPolicy=" + innerPolicy +
                ", adPlaceList=" + adPlaceList +
                '}';
    }
}
