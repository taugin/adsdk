package com.inner.adsdk;

import android.text.TextUtils;
import android.view.View;

import com.inner.adsdk.framework.Params;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */

public class AdParams {

    private Map<String, Params> mAdParams;

    private AdParams(Map<String, Params> params) {
        this.mAdParams = params;
    }

    public Params getParams(String sdk) {
        if (mAdParams != null && !TextUtils.isEmpty(sdk)) {
            return mAdParams.get(sdk);
        }
        return null;
    }

    public static class Builder {
        private Map<String, Params> mAdParams = null;

        public Builder() {
            mAdParams = new HashMap<String, Params>();
        }

        private Params getParams(String sdk) {
            Params params = null;
            if (mAdParams == null) {
                mAdParams = new HashMap<String, Params>();
            }
            params = mAdParams.get(sdk);
            if (params == null) {
                params = new Params();
                mAdParams.put(sdk, params);
            }
            return params;
        }

        public Builder setAdCardStyle(String sdk, int cardStyle) {
            getParams(sdk).setAdCardStyle(cardStyle);
            return this;
        }

        public Builder setBannerSize(String sdk, int size) {
            getParams(sdk).setBannerSize(sdk, size);
            return this;
        }


        public Builder setAdRootView(String sdk, View view) {
            getParams(sdk).setAdRootView(view);
            return this;
        }

        public Builder setAdRootLayout(String sdk, int layout) {
            getParams(sdk).setAdRootLayout(layout);
            return this;
        }

        public Builder setAdTitle(String sdk, int adTitle) {
            getParams(sdk).setAdTitle(adTitle);
            return this;
        }

        public Builder setAdSubTitle(String sdk, int adSubTitle) {
            getParams(sdk).setAdSubTitle(adSubTitle);
            return this;
        }

        public Builder setAdIcon(String sdk, int adIcon) {
            getParams(sdk).setAdIcon(adIcon);
            return this;
        }

        public Builder setAdCover(String sdk, int adCover) {
            getParams(sdk).setAdCover(adCover);
            return this;
        }

        public Builder setAdMediaView(String sdk, int adView) {
            getParams(sdk).setAdMediaView(adView);
            return this;
        }

        public Builder setAdDetail(String sdk, int adDetail) {
            getParams(sdk).setAdDetail(adDetail);
            return this;
        }

        public Builder setAdAction(String sdk, int adAction) {
            getParams(sdk).setAdAction(adAction);
            return this;
        }

        public Builder setAdChoices(String sdk, int adChoices) {
            getParams(sdk).setAdChoices(adChoices);
            return this;
        }

        public Builder setAdSponsored(String sdk, int adSponsored) {
            getParams(sdk).setAdSponsored(adSponsored);
            return this;
        }

        public Builder setAdSocial(String sdk, int adSocial) {
            getParams(sdk).setAdSocial(adSocial);
            return this;
        }

        public AdParams build() {
            return new AdParams(mAdParams);
        }
    }
}
