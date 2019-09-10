package com.simple.mpsdk;

import android.view.View;

import com.simple.mpsdk.framework.Params;

/**
 * Created by Administrator on 2018/4/3.
 */

public class AdParams {

    private Params mAdParams;

    private AdParams(Params params) {
        this.mAdParams = params;
    }

    public Params getParams() {
        return mAdParams;
    }

    public static class Builder {
        private Params mAdParam = null;

        public Builder() {
            mAdParam = new Params();
        }

        private Params getParams() {
            return mAdParam;
        }

        public Builder setAdCardStyle(int cardStyle) {
            getParams().setAdCardStyle(cardStyle);
            return this;
        }

        public Builder setAdRootView(View view) {
            getParams().setAdRootView(view);
            return this;
        }

        public Builder setAdRootLayout(int layout) {
            getParams().setAdRootLayout(layout);
            return this;
        }

        public Builder setAdTitle(int adTitle) {
            getParams().setAdTitle(adTitle);
            return this;
        }

        public Builder setAdSubTitle(int adSubTitle) {
            getParams().setAdSubTitle(adSubTitle);
            return this;
        }

        public Builder setAdIcon(int adIcon) {
            getParams().setAdIcon(adIcon);
            return this;
        }

        public Builder setAdCover(int adCover) {
            getParams().setAdCover(adCover);
            return this;
        }

        public Builder setAdMediaView(int adView) {
            getParams().setAdMediaView(adView);
            return this;
        }

        public Builder setAdDetail(int adDetail) {
            getParams().setAdDetail(adDetail);
            return this;
        }

        public Builder setAdAction(int adAction) {
            getParams().setAdAction(adAction);
            return this;
        }

        public Builder setAdChoices(int adChoices) {
            getParams().setAdChoices(adChoices);
            return this;
        }

        public Builder setAdSponsored(int adSponsored) {
            getParams().setAdSponsored(adSponsored);
            return this;
        }

        public Builder setAdSocial(int adSocial) {
            getParams().setAdSocial(adSocial);
            return this;
        }

        public AdParams build() {
            return new AdParams(mAdParam);
        }
    }
}
