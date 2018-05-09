package com.inner.adsdk.config;

import android.text.TextUtils;

import com.inner.adsdk.constant.Constant;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PidConfig {

    private String adPlaceName;

    private String sdk;

    private String pid;

    private int ctr;

    private String adType;

    private boolean disable;

    private long noFill = 30 * 1000;

    private long cacheTime;

    private long timeOut;

    public String getAdPlaceName() {
        return adPlaceName;
    }

    public void setAdPlaceName(String adPlaceName) {
        this.adPlaceName = adPlaceName;
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

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public long getNoFill() {
        return noFill;
    }

    public void setNoFill(long noFill) {
        this.noFill = noFill;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
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
                "adPlaceName='" + adPlaceName + '\'' +
                ", sdk='" + sdk + '\'' +
                ", pid='" + pid + '\'' +
                ", ctr=" + ctr +
                ", adType='" + adType + '\'' +
                '}';
    }
}
