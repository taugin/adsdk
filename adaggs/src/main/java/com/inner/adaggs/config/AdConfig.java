package com.inner.adaggs.config;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdConfig {
    private AdvInner advInner;
    private AdvOuter advOuter;
    private Map<String, String> adIds;

    public AdvInner getAdvInner() {
        return advInner;
    }

    public void setAdvInner(AdvInner advInner) {
        this.advInner = advInner;
    }

    public AdvOuter getAdvOuter() {
        return advOuter;
    }

    public void setAdvOuter(AdvOuter advOuter) {
        this.advOuter = advOuter;
    }

    public Map<String, String> getAdIds() {
        return adIds;
    }

    public void setAdIds(Map<String, String> adIds) {
        this.adIds = adIds;
    }

    @Override
    public String toString() {
        return "AdConfig{" +
                "advInner=" + advInner +
                ", advOuter=" + advOuter +
                '}';
    }
}
