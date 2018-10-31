package com.inner.adsdk.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.appub.ads.a.FSA;

/**
 * Created by Administrator on 2018-10-31.
 */

public class NativeBannerActivity extends FSA {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.width = (int) (dm.widthPixels * 0.9f);
        params.height = (int) (dm.heightPixels * 0.6f);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
