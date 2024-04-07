package com.mix.ads.core.framework;

/**
 * Created by Administrator on 2018/4/3.
 */

public class Params {

    private int mNativeRootLayout;
    private int mBannerSize = -1;
    private int adTitle;
    private int adIcon;
    private int adCover;
    private int adMediaView;
    private int adDetail;
    private int adAction;
    private int adChoices;
    private int adSponsored;
    private int adSocial;
    private String sceneName;

    public Params() {
    }

    public void setAdRootLayout(int layout) {
        mNativeRootLayout = layout;
    }

    public int getNativeRootLayout() {
        return mNativeRootLayout;
    }

    public void setBannerSize(int size) {
        mBannerSize = size;
    }

    public int getBannerSize() {
        return mBannerSize;
    }

    public int getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(int adTitle) {
        this.adTitle = adTitle;
    }

    public int getAdIcon() {
        return adIcon;
    }

    public void setAdIcon(int adIcon) {
        this.adIcon = adIcon;
    }

    public int getAdCover() {
        return adCover;
    }

    public void setAdCover(int adCover) {
        this.adCover = adCover;
    }

    public int getAdMediaView() {
        return adMediaView;
    }

    public void setAdMediaView(int adMediaView) {
        this.adMediaView = adMediaView;
    }

    public int getAdDetail() {
        return adDetail;
    }

    public void setAdDetail(int adDetail) {
        this.adDetail = adDetail;
    }

    public int getAdAction() {
        return adAction;
    }

    public void setAdAction(int adAction) {
        this.adAction = adAction;
    }

    public int getAdChoices() {
        return adChoices;
    }

    public void setAdChoices(int adChoices) {
        this.adChoices = adChoices;
    }

    public int getAdSponsored() {
        return adSponsored;
    }

    public void setAdSponsored(int adSponsored) {
        this.adSponsored = adSponsored;
    }

    public int getAdSocial() {
        return adSocial;
    }

    public void setAdSocial(int adSocial) {
        this.adSocial = adSocial;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getSceneName() {
        return sceneName;
    }
}
