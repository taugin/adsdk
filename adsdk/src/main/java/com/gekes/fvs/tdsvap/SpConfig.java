package com.gekes.fvs.tdsvap;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpConfig implements Serializable {

    public static final String ADSPREAD_NAME = "sp" + "config";

    private String banner;
    private String icon;
    private String title;
    private String pkgname;
    private String subTitle;
    private String detail;
    private String linkUrl;
    private String cta;
    private boolean disable;

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

    public String getPkgname() {
        return pkgname;
    }

    public void setPkgname(String pkgname) {
        this.pkgname = pkgname;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
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

    @Override
    public String toString() {
        return "SpConfig{" +
                "banner='" + banner + '\'' +
                ", icon='" + icon + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", detail='" + detail + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", cta='" + cta + '\'' +
                '}';
    }
}
