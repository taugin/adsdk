package com.humob.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.core.framework.Params;
import com.humob.adsdk.utils.Utils;
import com.humob.adsdk.utils.VUIHelper;
import com.humob.adsdk.log.Log;

import java.util.Map;

/**
 * Created by Administrator on 2018-10-16.
 */

public class AdActivity extends Activity {
    private VUIHelper mVUIHelper = new VUIHelper(this);

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent intent = this.getIntent();
            if (intent != null) {
                String placeName = intent.getStringExtra(Intent.EXTRA_TITLE);
                String source = intent.getStringExtra(Intent.EXTRA_TEXT);
                String adType = intent.getStringExtra(Intent.EXTRA_TEMPLATE);
            }
        } catch (Exception e) {
        }
        mVUIHelper.onCreate();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            super.setRequestedOrientation(requestedOrientation);
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            View closeView = this.getWindow().getDecorView().findViewWithTag(String.valueOf(getClass()) + View.generateViewId());
            if (closeView != null) {
                closeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishActivityWithDelay();
                    }
                });
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onBackPressed() {
        if (mVUIHelper.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVUIHelper.onDestroy();
    }

    private void updateFullScreenState() {
        try {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
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
        Intent intent = this.getIntent();
        if (intent != null) {
            String placeName = intent.getStringExtra(Intent.EXTRA_TITLE);
            String source = intent.getStringExtra(Intent.EXTRA_TEXT);
            String adType = intent.getStringExtra(Intent.EXTRA_TEMPLATE);
        }
    }

    /**
     * 展示原生广告
     */
    private void showNativeAd() {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);
        this.setContentView(rootLayout);
        RelativeLayout mAdLayout = new RelativeLayout(this);
        mAdLayout.setGravity(Gravity.CENTER);
        rootLayout.addView(mAdLayout, -1, -1);
        View closeView = generateCloseView();
        int size = Utils.dp2px(this, 24);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        int margin = dp2px(this, 8);
        params.setMargins(margin, margin, 0, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityWithDelay();
            }
        });
        rootLayout.addView(closeView, params);
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
        shapeNormal.getPaint().setColor(Color.parseColor("#AA000000"));

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        imageView.setBackground(drawable);
        int padding = Utils.dp2px(this, 2);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setClickable(true);
        return imageView;
    }

    private void finishActivityWithDelay() {
        Log.iv(Log.TAG, "");
        try {
            finish();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    public static String getVersion() {
        return BuildConfig.SDK_VERSION_NAME;
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static void bindLayoutMap(Map<String, Integer> LAYOUT_MAPS) {
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MICRO, R.layout.kom_layout_micro);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_TINY, R.layout.kom_layout_tiny);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LITTLE, R.layout.kom_layout_little);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_SMALL, R.layout.kom_layout_small);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MEDIUM, R.layout.kom_layout_medium);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LARGE, R.layout.kom_layout_large);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_RECT, R.layout.kom_layout_rect);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_WRAP, R.layout.kom_layout_wrap);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_ROUND, R.layout.kom_layout_round);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FULL, R.layout.kom_layout_full);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MIX, R.layout.kom_layout_mix);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FOOT, R.layout.kom_layout_foot);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_HEAD, R.layout.kom_layout_head);
    }

    public static int getLayoutLittle() {
        return R.layout.kom_layout_little;
    }

    public static void bindLayoutId(Params params) {
        if (params == null) {
            return;
        }
        params.setAdTitle(R.id.rab_native_title);
        params.setAdSocial(R.id.rab_native_social);
        params.setAdDetail(R.id.rab_native_detail);
        params.setAdIcon(R.id.rab_native_icon);
        params.setAdAction(R.id.rab_native_action_btn);
        params.setAdCover(R.id.rab_native_image_cover);
        params.setAdChoices(R.id.rab_native_ad_choices_container);
        params.setAdMediaView(R.id.rab_native_media_cover);
    }

    public static int getDefaultIconColor() {
        return R.color.komDefaultIconColor;
    }
}