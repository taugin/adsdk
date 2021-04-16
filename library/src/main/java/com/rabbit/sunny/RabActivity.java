package com.rabbit.sunny;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.core.framework.AdPlaceLoader;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2018-10-16.
 */

public class RabActivity extends Activity {

    private String mPlaceName;
    private String mSource;
    private String mAdType;
    private Handler mHandler = null;
    private RelativeLayout mAdLayout;
    private ImageView mCloseView;
    private ISdkLoader mISdkLoader;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        mHandler = new Handler();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDataAndView();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            super.setRequestedOrientation(requestedOrientation);
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        closeViewMonitor();
    }

    private void closeViewMonitor() {
        try {
            View closeView = getWindow().getDecorView().findViewWithTag("native_cancel_button");
            if (closeView != null) {
                closeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishActivityWithDelay();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void updateFullScreenState() {
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                Window window = getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setAttributes(attributes);
                    window.setStatusBarColor(Color.TRANSPARENT);
                } else {
                    attributes.flags |= flagTranslucentStatus;
                    window.setAttributes(attributes);
                }
            }
        } catch (Exception | Error e) {
        }
    }

    private void updateDataAndView() {
        Log.iv(Log.TAG, "update data and view");
        updateFullScreenState();
        showNativeAd();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mPlaceName = intent.getStringExtra(Intent.EXTRA_TITLE);
            mSource = intent.getStringExtra(Intent.EXTRA_TEXT);
            mAdType = intent.getStringExtra(Intent.EXTRA_TEMPLATE);
        }
    }

    /**
     * 展示原生广告
     */
    private void showNativeAd() {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);
        setContentView(rootLayout);
        mAdLayout = new RelativeLayout(this);
        mAdLayout.setGravity(Gravity.CENTER);
        rootLayout.addView(mAdLayout, -1, -1);
        mCloseView = generateCloseView();
        int size = Utils.dp2px(this, 24);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        int margin = dp2px(this, 8);
        params.setMargins(margin, margin, 0, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityWithDelay();
            }
        });
        rootLayout.addView(mCloseView, params);
        showAdViewInternal();
    }

    private void showAdViewInternal() {
        if (mAdLayout != null) {
            mISdkLoader = AdPlaceLoader.sLoaderMap.remove(String.format(Locale.getDefault(), "%s_%s_%s", mSource, mAdType, mPlaceName));
            Params params = AdPlaceLoader.sParamsMap.remove(String.format(Locale.getDefault(), "%s_%s_%s", mSource, mAdType, mPlaceName));
            if (mISdkLoader != null) {
                try {
                    // Spread类型的广告插屏类型需要单独处理
                    if (mISdkLoader.getPidConfig().isSpread()
                            && mISdkLoader.isInterstitialType()
                            && mISdkLoader.isInterstitialLoaded()) {
                        mISdkLoader.showInterstitialWithNative(mAdLayout, params);
                        return;
                    }
                } catch (Exception e) {
                }
                if (mISdkLoader.isBannerType() && mISdkLoader.isBannerLoaded()) {
                    mISdkLoader.showBanner(mAdLayout);
                    return;
                }
                if (mISdkLoader.isNativeType() && mISdkLoader.isNativeLoaded()) {
                    mISdkLoader.showNative(mAdLayout, params);
                    return;
                }
            }
        }
        fa();
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private ImageView generateCloseView() {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        Shape shape = new OvalShape();

        ShapeDrawable shapePressed = new ShapeDrawable(shape);
        shapePressed.getPaint().setColor(Color.parseColor("#88888888"));

        shape = new OvalShape();
        ShapeDrawable shapeNormal = new ShapeDrawable(shape);
        shapeNormal.getPaint().setColor(Color.parseColor("#88AAAAAA"));

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        imageView.setBackground(drawable);
        int padding = Utils.dp2px(this, 4);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setClickable(true);
        return imageView;
    }

    private void finishActivityWithDelay() {
        finishActivityWithDelay(500);
    }

    private void finishActivityWithDelay(final int delay) {
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fa();
                }
            }, delay);
        } else {
            fa();
        }
    }

    private void fa() {
        Log.v(Log.TAG, "");
        try {
            finish();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdSdk.get(this).destroy(mPlaceName);
        if (mISdkLoader != null) {
            mISdkLoader.notifyAdViewUIDismiss();
        }
    }
}