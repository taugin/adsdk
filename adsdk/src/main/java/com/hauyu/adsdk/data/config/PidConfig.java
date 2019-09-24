package com.hauyu.adsdk.data.config;

import android.text.TextUtils;

import com.hauyu.adsdk.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PidConfig {

    private String adPlaceName;

    private String sdk;

    private String pid;

    private int ctr = 100;

    private String adType;

    private boolean disable;

    private long noFill = 30 * 1000;

    private long cacheTime;

    private long timeOut;

    private long delayLoadTime = 0;

    private double ecpm;

    // 0 : double click, 1 : finish for ctr
    private boolean finishForCtr = false;

    // delay to click time
    private long delayClickTime;

    private boolean destroyAfterClick;

    private String appId;

    private String extId;

    private String bannerSize;

    private List<String> clickViews;

    private int cnt;

    private String layout;

    private List<String> ctaColor;

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

    public long getDelayLoadTime() {
        return delayLoadTime;
    }

    public void setDelayLoadTime(long delayLoadTime) {
        this.delayLoadTime = delayLoadTime;
    }

    public double getEcpm() {
        return ecpm;
    }

    public void setEcpm(double ecpm) {
        this.ecpm = ecpm;
    }

    public boolean isFinishForCtr() {
        return finishForCtr;
    }

    public void setFinishForCtr(boolean finishForCtr) {
        this.finishForCtr = finishForCtr;
    }

    public long getDelayClickTime() {
        return delayClickTime;
    }

    public void setDelayClickTime(long delayClickTime) {
        this.delayClickTime = delayClickTime;
    }

    public boolean isDestroyAfterClick() {
        return destroyAfterClick;
    }

    public void setDestroyAfterClick(boolean destroyAfterClick) {
        this.destroyAfterClick = destroyAfterClick;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getBannerSize() {
        return bannerSize;
    }

    public void setBannerSize(String bannerSize) {
        this.bannerSize = bannerSize;
    }

    public List<String> getClickViews() {
        return clickViews;
    }

    public void setClickViews(List<String> clickViews) {
        this.clickViews = clickViews;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public List<String> getCtaColor() {
        return ctaColor;
    }

    public void setCtaColor(List<String> ctaColor) {
        this.ctaColor = ctaColor;
    }

    public boolean isAdmob() {
        return TextUtils.equals(Constant.AD_SDK_ADMOB, sdk);
    }

    public boolean isMopub() {
        return TextUtils.equals(Constant.AD_SDK_MOPUB, sdk);
    }

    public boolean isFB() {
        return TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk);
    }

    public boolean isAdx() {
        return TextUtils.equals(Constant.AD_SDK_ADX, sdk);
    }

    public boolean isDfp() {
        return TextUtils.equals(Constant.AD_SDK_DFP, sdk);
    }

    public boolean isSpread() {
        return TextUtils.equals(Constant.AD_SDK_SPREAD, sdk);
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

    public boolean isRewardedVideoType() {
        return TextUtils.equals(Constant.TYPE_REWARD, adType);
    }

    @Override
    public String toString() {
        return "PidConfig{" +
                "placeName='" + adPlaceName + '\'' +
                ", sdk='" + sdk + '\'' +
                ", pid='" + pid + '\'' +
                ", ctr=" + ctr +
                ", adType='" + adType + '\'' +
                ", ecpm='" + ecpm + '\'' +
                ", dac='" + destroyAfterClick + '\'' +
                '}';
    }
}
