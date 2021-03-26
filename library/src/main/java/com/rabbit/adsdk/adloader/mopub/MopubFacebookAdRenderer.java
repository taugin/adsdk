package com.rabbit.adsdk.adloader.mopub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.FacebookAdRenderer;

/**
 * Created by Administrator on 2019-9-10.
 */

public class MopubFacebookAdRenderer extends FacebookAdRenderer {
    private View mLayout;

    public MopubFacebookAdRenderer(FacebookViewBinder viewBinder, View layout) {
        super(viewBinder);
        mLayout = layout;
    }

    @Override
    public View createAdView(Context context, ViewGroup parent) {
        return mLayout;
    }
}
