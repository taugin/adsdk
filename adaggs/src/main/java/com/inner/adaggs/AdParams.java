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

        public Builder setNativeTemplateId(int templateId) {
            params.setNativeTemplateId(templateId);
            return this;
        }

        public Builder setBannerSize(String sdk, int size) {
            params.setBannerSize(sdk, size);
            return this;
        }

        public AdParams build() {
            return new AdParams(params);
        }
    }
}
