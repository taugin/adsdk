package com.hauyu.adsdk.adloader.mopub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2018-10-8.
 */

public class MoPubVideoAdRender extends MoPubVideoNativeAdRenderer {

    private View mLayout;

    /**
     * Constructs a native ad renderer with a view binder.
     *
     * @param mediaViewBinder The view binder to use when inflating and rendering an ad.
     */
    public MoPubVideoAdRender(@NonNull MediaViewBinder mediaViewBinder, View layout) {
        super(mediaViewBinder);
        mLayout = layout;
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return mLayout;
    }
}
