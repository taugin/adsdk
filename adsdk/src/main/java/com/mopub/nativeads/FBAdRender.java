package com.mopub.nativeads;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.MediaView;

public class FBAdRender extends FacebookAdRenderer {

    private final ViewBinder mViewBinder;
    private View mLayout;

    /**
     * layout 中需要有 ViewBinder 中的元素。
     */
    public FBAdRender(ViewBinder viewBinder, View layout) {
        super(viewBinder);
        mViewBinder = viewBinder;
        mLayout = layout;
    }

    @Override
    public View createAdView(Context context, ViewGroup parent) {
        View mainImageView = mLayout.findViewById(mViewBinder.mainImageId);
        if (mainImageView == null) {
            return mLayout;
        } else {
            ViewGroup.LayoutParams mainImageViewLayoutParams = mainImageView.getLayoutParams();
            android.widget.RelativeLayout.LayoutParams mediaViewLayoutParams = new android.widget.RelativeLayout.LayoutParams(mainImageViewLayoutParams.width, mainImageViewLayoutParams.height);
            if (mainImageViewLayoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)mainImageViewLayoutParams;
                mediaViewLayoutParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
            }

            int i;
            if (mainImageViewLayoutParams instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams)mainImageViewLayoutParams;
                int[] rules = mainImageViewRelativeLayoutParams.getRules();

                for(i = 0; i < rules.length; ++i) {
                    mediaViewLayoutParams.addRule(i, rules[i]);
                }

                mainImageView.setVisibility(4);
            } else {
                mainImageView.setVisibility(8);
            }

            MediaView mediaView = new MediaView(context);
            ViewGroup mainImageParent = (ViewGroup)mainImageView.getParent();
            i = mainImageParent.indexOfChild(mainImageView);
            mainImageParent.addView(mediaView, i + 1, mediaViewLayoutParams);
            return mLayout;
        }
    }
}
