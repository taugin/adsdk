package com.mix.ads;

import com.mix.ads.core.framework.Params;

/**
 * Created by Administrator on 2018/4/3.
 */

public class MiParams {

    private Params mAdParams;

    private MiParams(Params params) {
        mAdParams = params;
    }

    public void setSceneName(String sceneName) {
        try {
            mAdParams.setSceneName(sceneName);
        } catch (Exception e) {
        }
    }

    public Params getParams() {
        return mAdParams;
    }

    public static class Builder {
        private Params mAdParams = null;

        public Builder() {
            mAdParams = new Params();
        }

        public Builder setBannerSize(int size) {
            mAdParams.setBannerSize(size);
            return this;
        }

        public Builder setAdRootLayout(int layout) {
            mAdParams.setAdRootLayout(layout);
            return this;
        }

        public Builder setAdTitle(int adTitle) {
            mAdParams.setAdTitle(adTitle);
            return this;
        }

        public Builder setAdIcon(int adIcon) {
            mAdParams.setAdIcon(adIcon);
            return this;
        }

        public Builder setAdCover(int adCover) {
            mAdParams.setAdCover(adCover);
            return this;
        }

        public Builder setAdMediaView(int adView) {
            mAdParams.setAdMediaView(adView);
            return this;
        }

        public Builder setAdDetail(int adDetail) {
            mAdParams.setAdDetail(adDetail);
            return this;
        }

        public Builder setAdAction(int adAction) {
            mAdParams.setAdAction(adAction);
            return this;
        }

        public Builder setAdChoices(int adChoices) {
            mAdParams.setAdChoices(adChoices);
            return this;
        }

        public Builder setAdSponsored(int adSponsored) {
            mAdParams.setAdSponsored(adSponsored);
            return this;
        }

        public Builder setAdSocial(int adSocial) {
            mAdParams.setAdSocial(adSocial);
            return this;
        }

        public Builder setSceneName(String sceneName) {
            mAdParams.setSceneName(sceneName);
            return this;
        }

        public MiParams build() {
            return new MiParams(mAdParams);
        }
    }
}
