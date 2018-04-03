package com.inner.adaggs.framework;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */

public class Params {

    private View mNativeRootView;
    private int mNativeTemplateId;
    private Map<String, Integer> mBannerSize = new HashMap<String, Integer>();

    public Params() {
    }

    public void setNativeRootView(View view) {
        mNativeRootView = view;
    }

    public View getNativeRootView() {
        return mNativeRootView;
    }

    public void setNativeTemplateId(int tid) {
        mNativeTemplateId = tid;
    }

    public int getNativeTemplateId() {
        return mNativeTemplateId;
    }

    public void setBannerSize(String sdk, int size) {
        mBannerSize.put(sdk, size);
    }

    public Map<String, Integer> getBannerSize() {
        return mBannerSize;
    }
}
