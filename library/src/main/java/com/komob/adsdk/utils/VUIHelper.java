package com.komob.adsdk.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.core.framework.ActivityMonitor;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.log.Log;

import java.lang.reflect.Field;

public class VUIHelper {

    public View generateNativeView(Context context, ISdkLoader iSdkLoader, Params params, View.OnClickListener listener) {
        return generateNativeLayout(context, iSdkLoader, params, listener);
    }

    /**
     * 展示原生广告
     */
    private ViewGroup generateNativeLayout(Context context, ISdkLoader iSdkLoader, Params params, View.OnClickListener listener) {
        RelativeLayout rootLayout = new RelativeLayout(context);
        rootLayout.setBackgroundColor(Color.WHITE);
        RelativeLayout adLayout = new RelativeLayout(context);
        adLayout.setGravity(Gravity.CENTER);
        rootLayout.addView(adLayout, -1, -1);
        ImageView closeView = generateCloseView(context);
        int size = Utils.dp2px(context, 24);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
        int margin = Utils.dp2px(context, 8);
        layoutParams.setMargins(margin, margin, 0, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeView.setOnClickListener(listener);
        rootLayout.addView(closeView, layoutParams);
        rootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    View cancelView = rootLayout.findViewWithTag("native_cancel_button");
                    if (cancelView != null) {
                        cancelView.setOnClickListener(listener);
                    }
                } catch (Exception e) {
                }
            }
        }, 500);
        boolean showResult = showAdViewInternal(adLayout, iSdkLoader, params);
        return showResult ? rootLayout : null;
    }

    private boolean showAdViewInternal(ViewGroup adRootLayout, ISdkLoader iSdkLoader, Params params) {
        if (adRootLayout != null && iSdkLoader != null) {
            try {
                // Spread类型的广告插屏类型需要单独处理
                if (iSdkLoader.getPidConfig().isSpread()
                        && iSdkLoader.isInterstitialType()
                        && iSdkLoader.isInterstitialLoaded()) {
                    iSdkLoader.showInterstitialWithNative(adRootLayout, params);
                    return true;
                }
            } catch (Exception e) {
            }
            if (iSdkLoader.isBannerType() && iSdkLoader.isBannerLoaded()) {
                iSdkLoader.showBanner(adRootLayout);
                return true;
            }
            if (iSdkLoader.isNativeType() && iSdkLoader.isNativeLoaded()) {
                iSdkLoader.showNative(adRootLayout, params);
                return true;
            }
        }
        return false;
    }

    private ImageView generateCloseView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        Shape shape = new OvalShape();

        ShapeDrawable shapePressed = new ShapeDrawable(shape);
        shapePressed.getPaint().setColor(Color.parseColor("#88888888"));

        shape = new OvalShape();
        ShapeDrawable shapeNormal = new ShapeDrawable(shape);
        shapeNormal.getPaint().setColor(Color.parseColor("#AA000000"));

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        imageView.setBackground(drawable);
        int padding = Utils.dp2px(context, 2);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setClickable(true);
        return imageView;
    }

}
