package com.inner.adaggs.config;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdConfig {
    private AdvInner advInner;
    private AdvOuter advOuter;

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

    @Override
    public String toString() {
        return "AdConfig{" +
                "advInner=" + advInner +
                ", advOuter=" + advOuter +
                '}';
    }
}
