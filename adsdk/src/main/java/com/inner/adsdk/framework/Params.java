package com.inner.adsdk.framework;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */

public class Params {

    private View mNativeRootView;
    private int mNativeCardStyle;
    private Map<String, Integer> mBannerSize = new HashMap<String, Integer>();
    private int adTitle;
    private int adSubTitle;
    private int adIcon;
    private int adCover;
    private int adView;
    private int adDetail;
    private int adAction;
    private int adChoices;
    private int adSponsored;
    private int adSocial;

    public Params() {
    }

    public void setNativeRootView(View view) {
        mNativeRootView = view;
    }

    public View getNativeRootView() {
        return mNativeRootView;
    }

    public void setNativeCardStyle(int tid) {
        mNativeCardStyle = tid;
    }

    public int getNativeCardStyle() {
        return mNativeCardStyle;
    }

    public void setBannerSize(String sdk, int size) {
        mBannerSize.put(sdk, size);
    }

    public Map<String, Integer> getBannerSize() {
        return mBannerSize;
    }

    public int getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(int adTitle) {
        this.adTitle = adTitle;
    }

    public int getAdSubTitle() {
        return adSubTitle;
    }

    public void setAdSubTitle(int adSubTitle) {
        this.adSubTitle = adSubTitle;
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

    public int getAdView() {
        return adView;
    }

    public void setAdView(int adView) {
        this.adView = adView;
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
}
