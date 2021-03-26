package com.rabbit.adsdk.adloader.mopub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mopub.nativeads.GooglePlayServicesAdRenderer;
import com.mopub.nativeads.GooglePlayServicesViewBinder;

/**
 * Created by Administrator on 2019-9-10.
 */

public class MoPubGoogleAdRenderer extends GooglePlayServicesAdRenderer {
    private View mLayout;

    public MoPubGoogleAdRenderer(GooglePlayServicesViewBinder viewBinder, View layout) {
        super(viewBinder);
        mLayout = layout;
    }

    @SuppressLint("ResourceType")
    @Override
    public View createAdView(Context context, ViewGroup parent) {
        FrameLayout wrappingView = new FrameLayout(context);
        wrappingView.setId(1001);
        wrappingView.addView(mLayout);
        return wrappingView;
    }
}
