package com.rabbit.adsdk.adloader.mopub;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.MintegralAdRenderer;

/**
 * Created by Administrator on 2019-9-10.
 */

public class MopubMintegralAdRenderer extends MintegralAdRenderer {
    private View mLayout;

    public MopubMintegralAdRenderer(MintegralAdRenderer.ViewBinder viewBinder, View layout) {
        super(viewBinder);
        mLayout = layout;
    }

    @Override
    public View createAdView(Context context, ViewGroup parent) {
        return mLayout;
    }
}
