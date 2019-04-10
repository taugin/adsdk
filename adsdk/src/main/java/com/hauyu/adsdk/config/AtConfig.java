package com.hauyu.adsdk.config;


import com.hauyu.adsdk.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AtConfig extends BaseConfig {

    private List<String> excludes;

    private boolean showOnFirstPage;

    @Override
    public String getName() {
        return Constant.ATPOLICY_NAME;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isShowOnFirstPage() {
        return showOnFirstPage;
    }

    public void setShowOnFirstPage(boolean showOnFirstPage) {
        this.showOnFirstPage = showOnFirstPage;
    }

    @Override
    public String toString() {
        return "at{" +
                "e=" + isShowOnFirstPage() +
                ", i=" + getInterval() +
                ", cl=" + getCountryList() +
                ", al=" + getAttrList() +
                ", ml=" + getMediaList() +
                '}';
    }
}
