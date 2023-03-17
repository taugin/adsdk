package com.rabbit.adsdk;

import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */

public class AdParams {

    private Map<String, Params> mAdParams;

    private AdParams(Map<String, Params> params) {
        this.mAdParams = params;
        fillCommonIfNeed();
    }

    public Params getParams(String sdk) {
        if (mAdParams != null && !TextUtils.isEmpty(sdk)) {
            Params params = mAdParams.get(sdk);
            if (!TextUtils.equals(sdk, AdExtra.AD_SDK_COMMON)) {
                Params commonParams = mAdParams.get(AdExtra.AD_SDK_COMMON);
                if (commonParams != null && !TextUtils.isEmpty(commonParams.getSceneName())) {
                    if (params != null) {
                        params.setSceneName(commonParams.getSceneName());
                    }
                }
            }
            return params;
        }
        return null;
    }

    private void fillCommonIfNeed() {
        if (mAdParams == null || mAdParams.isEmpty()) {
            return;
        }
        Params commonParams = mAdParams.get(Constant.AD_SDK_COMMON);
        if (!isNativeParamsSet(commonParams)) {
            return;
        }
        String sdk = null;
        Params params = null;
        for (Map.Entry<String, Params> entry : mAdParams.entrySet()) {
            if (entry != null) {
                sdk = entry.getKey();
                params = entry.getValue();
                if (Constant.AD_SDK_COMMON.equals(sdk) || isNativeParamsSet(params)) {
                    continue;
                }
                fillCommonParams(params, commonParams);
            }
        }
    }

    private boolean isNativeParamsSet(Params params) {
        if (params == null) {
            return false;
        }
        if (params.getNativeRootLayout() <= 0
                && TextUtils.isEmpty(params.getNativeCardStyle())) {
            // 未设置commonsdk参数
            return false;
        }
        return true;
    }

    private void fillCommonParams(Params sdkParams, Params commonParams) {
        if (sdkParams == null || commonParams == null) {
            return;
        }
        sdkParams.setAdCardStyle(commonParams.getNativeCardStyle());
        sdkParams.setAdRootLayout(commonParams.getNativeRootLayout());
        sdkParams.setAdTitle(commonParams.getAdTitle());
        sdkParams.setAdSocial(commonParams.getAdSocial());
        sdkParams.setAdDetail(commonParams.getAdDetail());
        sdkParams.setAdIcon(commonParams.getAdIcon());
        sdkParams.setAdAction(commonParams.getAdAction());
        sdkParams.setAdCover(commonParams.getAdCover());
        sdkParams.setAdChoices(commonParams.getAdChoices());
        sdkParams.setAdMediaView(commonParams.getAdMediaView());
        sdkParams.setAdSponsored(commonParams.getAdSponsored());
        sdkParams.setSceneName(commonParams.getSceneName());
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

        public Builder setAdCardStyle(String sdk, String cardStyle) {
            getParams(sdk).setAdCardStyle(cardStyle);
            return this;
        }

        public Builder setBannerSize(String sdk, int size) {
            getParams(sdk).setBannerSize(sdk, size);
            return this;
        }

        public Builder setNativeTemplateWidth(String sdk, int width) {
            getParams(sdk).setNativeTemplateWidth(width);
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

        public Builder setSceneName(String sceneName) {
            getParams(AdExtra.AD_SDK_COMMON).setSceneName(sceneName);
            return this;
        }

        public AdParams build() {
            return new AdParams(mAdParams);
        }
    }
}
