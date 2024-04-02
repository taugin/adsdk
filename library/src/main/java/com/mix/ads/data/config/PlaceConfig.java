package com.mix.ads.data.config;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PlaceConfig {

    // 广告配置
    private List<AdPlace> adPlaceList;

    private String adConfigMd5;

    private boolean disableVpnLoad = false;

    private String applovinSdkKey;

    public List<AdPlace> getAdPlaceList() {
        return adPlaceList;
    }

    public void setAdPlaceList(List<AdPlace> adPlaceList) {
        this.adPlaceList = adPlaceList;
    }

    public String getAdConfigMd5() {
        return adConfigMd5;
    }

    public void setAdConfigMd5(String adConfigMd5) {
        this.adConfigMd5 = adConfigMd5;
    }

    public boolean isDisableVpnLoad() {
        return disableVpnLoad;
    }

    public void setDisableVpnLoad(boolean disableVpnLoad) {
        this.disableVpnLoad = disableVpnLoad;
    }

    public String getApplovinSdkKey() {
        return applovinSdkKey;
    }

    public void setApplovinSdkKey(String applovinSdkKey) {
        this.applovinSdkKey = applovinSdkKey;
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
        return "PlaceConfig{" +
                ", adPlaceList=" + adPlaceList +
                '}';
    }
}
