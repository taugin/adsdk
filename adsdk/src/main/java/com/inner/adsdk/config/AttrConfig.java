package com.inner.adsdk.config;

import java.util.List;

/**
 * Created by Administrator on 2018/7/18.
 */

public class AttrConfig {

    private List<String> countryList;
    private List<String> attrList;
    private List<String> mediaList;

    public List<String> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<String> countryList) {
        this.countryList = countryList;
    }

    public List<String> getAttrList() {
        return attrList;
    }

    public void setAttrList(List<String> attrList) {
        this.attrList = attrList;
    }

    public List<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<String> mediaList) {
        this.mediaList = mediaList;
    }
}
