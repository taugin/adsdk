package com.inner.adaggs.config;

import android.text.TextUtils;

import com.inner.adaggs.constant.Constant;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PidConfig {

    private String name;

    private String sdk;

    private String pid;

    private int ctr;

    private String adType;

    private int outer;

    private int film;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getCtr() {
        return ctr;
    }

    public void setCtr(int ctr) {
        this.ctr = ctr;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String adType) {
        this.adType = adType;
    }

    public int getOuter() {
        return outer;
    }

    public void setOuter(int outer) {
        this.outer = outer;
    }

    public int getFilm() {
        return film;
    }

    public void setFilm(int film) {
        this.film = film;
    }

    public boolean isAdmob() {
        return TextUtils.equals(Constant.AD_SDK_ADMOB, sdk);
    }

    public boolean isFB() {
        return TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk);
    }

    public boolean isAdx() {
        return TextUtils.equals(Constant.AD_SDK_ADX, sdk);
    }

    public boolean isBannerType() {
        return TextUtils.equals(Constant.TYPE_BANNER, adType);
    }

    public boolean isNativeType() {
        return TextUtils.equals(Constant.TYPE_NATIVE, adType);
    }

    public boolean isInterstitialType() {
        return TextUtils.equals(Constant.TYPE_INTERSTITIAL, adType);
    }

    @Override
    public String toString() {
        return "PidConfig{" +
                "name='" + name + '\'' +
                ", sdk='" + sdk + '\'' +
                ", pid='" + pid + '\'' +
                ", ctr=" + ctr +
                ", adType='" + adType + '\'' +
                '}';
    }
}
