package com.rabbit.adsdk.data.config;

import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PidConfig {

    private String adPlaceName;

    private String sdk;

    private String pid;

    private String adType;

    private boolean disable;

    private long noFill = 30 * 1000;

    private long cacheTime;

    private long timeOut;

    private long delayLoadTime = 0;

    private double ecpm;

    private String appId;

    private String extId;

    private String bannerSize;

    private List<String> clickViews;

    private int cnt;

    private List<String> nativeLayout;

    private List<String> ctaColor;

    private AdPlace adPlace;

    private boolean activityContext;

    private int ratio = 100;

    private boolean isMax = true;

    private Map<String, String> subNativeLayout;

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

    public List<String> getNativeLayout() {
        return nativeLayout;
    }

    public void setNativeLayout(List<String> nativeLayout) {
        this.nativeLayout = nativeLayout;
    }

    public List<String> getCtaColor() {
        return ctaColor;
    }

    public void setCtaColor(List<String> ctaColor) {
        this.ctaColor = ctaColor;
    }

    public AdPlace getAdPlace() {
        return adPlace;
    }

    public void setAdPlace(AdPlace adPlace) {
        this.adPlace = adPlace;
    }

    public boolean isActivityContext() {
        return activityContext;
    }

    public void setActivityContext(boolean activityContext) {
        this.activityContext = activityContext;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public boolean isMax() {
        return isMax;
    }

    public void setMax(boolean max) {
        isMax = max;
    }

    public Map<String, String> getSubNativeLayout() {
        return subNativeLayout;
    }

    public void setSubNativeLayout(Map<String, String> subNativeLayout) {
        this.subNativeLayout = subNativeLayout;
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

    public boolean isApplovin() {
        return TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk);
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
                "name=" + adPlaceName +
                ", sdk=" + sdk +
                ", pid=" + pid +
                ", type=" + adType +
                ", ecpm=" + ecpm +
                ", nl=" + nativeLayout +
                ", snl=" + subNativeLayout +
                ", aid=" + appId +
                ", eid=" + extId +
                ", ac=" + activityContext +
                ", bs=" + bannerSize +
                ", dis=" + disable +
                '}';
    }
}
