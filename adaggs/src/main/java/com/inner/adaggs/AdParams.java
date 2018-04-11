package com.inner.adaggs;

import android.view.View;

import com.inner.adaggs.framework.Params;

/**
 * Created by Administrator on 2018/4/3.
 */

public class AdParams {

    private Params params;

    private AdParams(Params params) {
        this.params = params;
    }

    public Params getParams() {
        return params;
    }

    public static class Builder {
        private Params params;

        public Builder() {
            params = new Params();
        }

        public Builder setNativeRootView(View view) {
            params.setNativeRootView(view);
            return this;
        }

        public Builder setNativeCardStyle(int cardStyle) {
            params.setNativeCardStyle(cardStyle);
            return this;
        }

        public Builder setBannerSize(String sdk, int size) {
            params.setBannerSize(sdk, size);
            return this;
        }

        public void setAdTitle(int adTitle) {
            params.setAdTitle(adTitle);
        }

        public void setAdSubTitle(int adSubTitle) {
            params.setAdSubTitle(adSubTitle);
        }

        public void setAdIcon(int adIcon) {
            params.setAdIcon(adIcon);
        }

        public void setAdCover(int adCover) {
            params.setAdCover(adCover);
        }

        public void setAdView(int adView) {
            params.setAdView(adView);
        }

        public void setAdDetail(int adDetail) {
            params.setAdDetail(adDetail);
        }

        public void setAdAction(int adAction) {
            params.setAdAction(adAction);
        }

        public void setAdChoices(int adChoices) {
            params.setAdChoices(adChoices);
        }

        public void setAdSponsored(int adSponsored) {
            params.setAdSponsored(adSponsored);
        }

        public void setAdSocial(int adSocial) {
            params.setAdSocial(adSocial);
        }

        public AdParams build() {
            return new AdParams(params);
        }
    }
}
