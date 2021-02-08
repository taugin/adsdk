package com.rabbit.adsdk.adloader.mopub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2018-10-8.
 */

public class MoPubStaticAdRender extends MoPubStaticNativeAdRenderer {

    private View mLayout;

    /**
     * Constructs a native ad renderer with a view binder.
     *
     * @param viewBinder The view binder to use when inflating and rendering an ad.
     */
    public MoPubStaticAdRender(@NonNull ViewBinder viewBinder, View layout) {
        super(viewBinder);
        mLayout = layout;
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return mLayout;
    }

    @Override
    public void renderAdView(@NonNull View view, @NonNull StaticNativeAd staticNativeAd) {
        super.renderAdView(view, staticNativeAd);
    }
}
