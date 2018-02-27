package com.inner.adaggs.config;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdvOuter {
    private AdPlace adPop;
    private AdPlace adFilm;
    private OuterPolicy outerPolicy;

    public AdPlace getAdPop() {
        return adPop;
    }

    public void setAdPop(AdPlace adPop) {
        this.adPop = adPop;
    }

    public AdPlace getAdFilm() {
        return adFilm;
    }

    public void setAdFilm(AdPlace adFilm) {
        this.adFilm = adFilm;
    }

    public OuterPolicy getOuterPolicy() {
        return outerPolicy;
    }

    public void setOuterPolicy(OuterPolicy outerPolicy) {
        this.outerPolicy = outerPolicy;
    }

    @Override
    public String toString() {
        return "AdvOuter{" +
                "adPop=" + adPop +
                ", adFilm=" + adFilm +
                ", outerPolicy=" + outerPolicy +
                '}';
    }
}
