package com.komob.adsdk.data.config;

import java.util.Map;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpreadConfig {

    public static final String AD_SPREAD_NAME = "cfg_spread_info";

    private String banner;
    private String icon;
    private String title;
    private String packageName;
    private String detail;
    private String linkUrl;
    private String cta;
    private boolean disable;
    private Map<String, String> ctaLocale;
    private long loadingTime;

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public Map<String, String> getCtaLocale() {
        return ctaLocale;
    }

    public void setCtaLocale(Map<String, String> ctaLocale) {
        this.ctaLocale = ctaLocale;
    }

    public long getLoadingTime() {
        return loadingTime;
    }

    public void setLoadingTime(long loadingTime) {
        this.loadingTime = loadingTime;
    }

    @Override
    public String toString() {
        return "SpreadCfg{" +
                "banner='" + banner + '\'' +
                ", icon='" + icon + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", cta='" + cta + '\'' +
                '}';
    }
}
