package com.inner.adsdk;

import android.view.View;

import com.inner.adsdk.framework.Params;

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

        public Builder setNativeCardStyle(int cardStyle) {
            params.setNativeCardStyle(cardStyle);
            return this;
        }

        public Builder setBannerSize(String sdk, int size) {
            params.setBannerSize(sdk, size);
            return this;
        }


        public Builder setNativeRootView(View view) {
            params.setNativeRootView(view);
            return this;
        }

        public Builder setAdTitle(int adTitle) {
            params.setAdTitle(adTitle);
            return this;
        }

        public Builder setAdSubTitle(int adSubTitle) {
            params.setAdSubTitle(adSubTitle);
            return this;
        }

        public Builder setAdIcon(int adIcon) {
            params.setAdIcon(adIcon);
            return this;
        }

        public Builder setAdCover(int adCover) {
            params.setAdCover(adCover);
            return this;
        }

        public Builder setAdView(int adView) {
            params.setAdView(adView);
            return this;
        }

        public Builder setAdDetail(int adDetail) {
            params.setAdDetail(adDetail);
            return this;
        }

        public Builder setAdAction(int adAction) {
            params.setAdAction(adAction);
            return this;
        }

        public Builder setAdChoices(int adChoices) {
            params.setAdChoices(adChoices);
            return this;
        }

        public Builder setAdSponsored(int adSponsored) {
            params.setAdSponsored(adSponsored);
            return this;
        }

        public Builder setAdSocial(int adSocial) {
            params.setAdSocial(adSocial);
            return this;
        }

        public AdParams build() {
            return new AdParams(params);
        }
    }
}
