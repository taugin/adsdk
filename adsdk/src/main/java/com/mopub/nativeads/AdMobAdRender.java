package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class AdMobAdRender extends GooglePlayServicesAdRenderer {

    // from GooglePlayServicesAdRenderer
    @IdRes
    private static final int ID_WRAPPING_FRAME = 1001;

    private View mLayout;

    /**
     * layout 中需要有 MediaViewBinder 中的元素。
     */
    public AdMobAdRender(MediaViewBinder viewBinder, View layout) {
        super(viewBinder);
        mLayout = layout;
    }

    @Override
    @NonNull
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        FrameLayout wrappingView = new FrameLayout(context);
        wrappingView.setId(ID_WRAPPING_FRAME);
        wrappingView.addView(mLayout);
        Log.i("MoPubToAdMobNative", "Ad view created.");
        return wrappingView;
    }
}
