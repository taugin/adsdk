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
    private GtConfig gtConfig;

    // ST
    private StConfig stConfig;

    // AT
    private AtConfig atConfig;

    // LT
    private LtConfig ltConfig;

    // HT
    private HtConfig htConfig;

    //CT
    private CtConfig ctConfig;

    // 广告配置
    private List<AdPlace> adPlaceList;

    private AdSwitch adSwitch;

    private Map<String, String> adRefs; // 广告场景相互引用

    public Map<String, String> getAdIds() {
        return adIds;
    }

    public void setAdIds(Map<String, String> adIds) {
        this.adIds = adIds;
    }

    public GtConfig getGtConfig() {
        return gtConfig;
    }

    public void setGtConfig(GtConfig gtConfig) {
        this.gtConfig = gtConfig;
    }

    public List<AdPlace> getAdPlaceList() {
        return adPlaceList;
    }

    public void setAdPlaceList(List<AdPlace> adPlaceList) {
        this.adPlaceList = adPlaceList;
    }

    public AdSwitch getAdSwitch() {
        return adSwitch;
    }

    public void setAdSwitch(AdSwitch adSwitch) {
        this.adSwitch = adSwitch;
    }

    public Map<String, String> getAdRefs() {
        return adRefs;
    }

    public void setAdRefs(Map<String, String> adRefs) {
        this.adRefs = adRefs;
    }

    public StConfig getStConfig() {
        return stConfig;
    }

    public void setStConfig(StConfig stConfig) {
        this.stConfig = stConfig;
    }

    public AtConfig getAtConfig() {
        return atConfig;
    }

    public void setAtConfig(AtConfig atConfig) {
        this.atConfig = atConfig;
    }

    public LtConfig getLtConfig() {
        return ltConfig;
    }

    public void setLtConfig(LtConfig ltConfig) {
        this.ltConfig = ltConfig;
    }

    public HtConfig getHtConfig() {
        return htConfig;
    }

    public void setHtConfig(HtConfig htConfig) {
        this.htConfig = htConfig;
    }

    public CtConfig getCtConfig() {
        return ctConfig;
    }

    public void setCtConfig(CtConfig ctConfig) {
        this.ctConfig = ctConfig;
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
        return "adc{" +
                "gt=" + gtConfig +
                ", list=" + adPlaceList +
                '}';
    }
}
