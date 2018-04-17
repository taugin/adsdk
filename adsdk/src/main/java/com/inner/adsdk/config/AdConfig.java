package com.inner.adsdk.config;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdConfig {

    private Map<String, String> adIds;

    // 广告内策略
    private AdPolicy adPolicy;

    // 广告配置
    private List<AdPlace> adPlaceList;

    public Map<String, String> getAdIds() {
        return adIds;
    }

    public void setAdIds(Map<String, String> adIds) {
        this.adIds = adIds;
    }

    public AdPolicy getAdPolicy() {
        return adPolicy;
    }

    public void setAdPolicy(AdPolicy adPolicy) {
        this.adPolicy = adPolicy;
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

    public void set(AdPlace adPlace) {
        if (adPlace == null) {
            return;
        }
        if (adPlaceList == null) {
            adPlaceList = new ArrayList<AdPlace>();
        }
        int len = adPlaceList.size();
        AdPlace p = null;
        boolean replaced = false;
        for (int index = 0; index < len; index++) {
            p = adPlaceList.get(index);
            if (p != null) {
                if (TextUtils.equals(p.getName(), adPlace.getName())) {
                    adPlaceList.set(index, adPlace);
                    replaced = true;
                    break;
                }
            }
        }
        if (!replaced) {
            adPlaceList.add(adPlace);
        }
    }

    @Override
    public String toString() {
        return "AdConfig{" +
                "adPolicy=" + adPolicy +
                ", adPlaceList=" + adPlaceList +
                '}';
    }
}
