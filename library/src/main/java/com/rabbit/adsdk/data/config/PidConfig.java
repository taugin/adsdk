package com.rabbit.adsdk.data.config;

import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class PidConfig {

    private String placeName;

    private String sdk;

    private String pid;

    private String adType;

    private boolean disable;

    private long noFill = 30 * 1000;

    private long cacheTime;

    private long timeOut;

    private long delayLoadTime = 0;

    private double cpm;

    private String bannerSize;

    private List<String> clickView;

    private List<String> clickViewRender;

    private int cnt;

    private List<String> nativeLayout;

    private List<String> ctaColor;

    private AdPlace adPlace;

    private boolean activityContext;

    private int ratio = 100;

    private Map<String, String> subNativeLayout;

    private int splashOrientation = 1;

    private Map<String, String> extra;

    // 是否为模板广告
    private boolean template;

    // 最大请求次数
    private int maxReqTimes;

    private String sceneId;

    // 是否展示开屏图标
    private boolean showSplashIcon = true;

    // 开始加载超时值
    private int splashTimeout = 15000;

    // 是否禁止VPN模式加载
    private boolean disableVpnLoad = false;

    private boolean useAvgValue = true;

    private int minAvgCount = 3;

    private boolean disableDebugLoad;
    
    private boolean onlySignLoad;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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

    public double getCpm() {
        return cpm;
    }

    public void setCpm(double cpm) {
        this.cpm = cpm;
    }

    public String getBannerSize() {
        return bannerSize;
    }

    public void setBannerSize(String bannerSize) {
        this.bannerSize = bannerSize;
    }

    public List<String> getClickView() {
        return clickView;
    }

    public void setClickView(List<String> clickView) {
        this.clickView = clickView;
    }

    public List<String> getClickViewRender() {
        return clickViewRender;
    }

    public void setClickViewRender(List<String> clickViewRender) {
        this.clickViewRender = clickViewRender;
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

    public Map<String, String> getSubNativeLayout() {
        return subNativeLayout;
    }

    public void setSubNativeLayout(Map<String, String> subNativeLayout) {
        this.subNativeLayout = subNativeLayout;
    }

    public int getSplashOrientation() {
        return splashOrientation;
    }

    public void setSplashOrientation(int splashOrientation) {
        this.splashOrientation = splashOrientation;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public int getMaxReqTimes() {
        return maxReqTimes;
    }

    public void setMaxReqTimes(int maxReqTimes) {
        this.maxReqTimes = maxReqTimes;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public boolean isShowSplashIcon() {
        return showSplashIcon;
    }

    public void setShowSplashIcon(boolean showSplashIcon) {
        this.showSplashIcon = showSplashIcon;
    }

    public int getSplashTimeout() {
        return splashTimeout;
    }

    public void setSplashTimeout(int splashTimeout) {
        this.splashTimeout = splashTimeout;
    }

    public boolean isDisableVpnLoad() {
        return disableVpnLoad;
    }

    public void setDisableVpnLoad(boolean disableVpnLoad) {
        this.disableVpnLoad = disableVpnLoad;
    }

    public boolean isUseAvgValue() {
        return useAvgValue;
    }

    public void setUseAvgValue(boolean useAvgValue) {
        this.useAvgValue = useAvgValue;
    }

    public int getMinAvgCount() {
        return minAvgCount;
    }

    public void setMinAvgCount(int minAvgCount) {
        this.minAvgCount = minAvgCount;
    }

    public boolean isDisableDebugLoad() {
        return disableDebugLoad;
    }

    public void setDisableDebugLoad(boolean disableDebugLoad) {
        this.disableDebugLoad = disableDebugLoad;
    }

    public boolean isOnlySignLoad() {
        return onlySignLoad;
    }

    public void setOnlySignLoad(boolean onlySignLoad) {
        this.onlySignLoad = onlySignLoad;
    }

    public boolean isAdmob() {
        return TextUtils.equals(Constant.AD_SDK_ADMOB, sdk);
    }

    public boolean isFB() {
        return TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk);
    }

    public boolean isApplovin() {
        return TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk);
    }

    public boolean isMintegral() {
        return TextUtils.equals(Constant.AD_SDK_MINTEGRAL, sdk);
    }

    public boolean isInmobi() {
        return TextUtils.equals(Constant.AD_SDK_INMOBI, sdk);
    }

    public boolean isTradPlus() {
        return TextUtils.equals(Constant.AD_SDK_TRADPLUS, sdk);
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

    public boolean isSplashType() {
        return TextUtils.equals(Constant.TYPE_SPLASH, adType);
    }

    @Override
    public String toString() {
        return "PidConfig{" +
                "name=" + placeName +
                ", sdk=" + sdk +
                ", pid=" + pid +
                ", type=" + adType +
                ", ecpm=" + cpm +
                ", nl=" + nativeLayout +
                ", snl=" + subNativeLayout +
                ", ac=" + activityContext +
                ", bs=" + bannerSize +
                ", dis=" + disable +
                '}';
    }
}
