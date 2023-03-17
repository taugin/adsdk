package com.tiger.moon;

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
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tiger.adsdk.AdSdk;
import com.tiger.adsdk.adloader.listener.ISdkLoader;
import com.tiger.adsdk.core.framework.AdPlaceLoader;
import com.tiger.adsdk.core.framework.Params;
import com.tiger.adsdk.log.Log;
import com.tiger.adsdk.utils.Utils;

import java.util.Locale;

public class VUIHelper {
    private String mPlaceName;
    private String mSource;
    private String mAdType;
    private Handler mHandler = null;
    private RelativeLayout mAdLayout;
    private ImageView mCloseView;
    private ISdkLoader mISdkLoader;
    private Activity mActivity;
    private boolean mOnBackPressed = false;

    public VUIHelper(Activity activity) {
        mActivity = activity;
    }

    public void onCreate() {
        parseIntent();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActivity.setFinishOnTouchOutside(false);
        }
        mOnBackPressed = false;
        mHandler = new Handler();
        mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDataAndView();
    }

    public void onResume() {
    }

    public boolean onBackPressed() {
        return mOnBackPressed;
    }

    public void onDestroy() {
        AdSdk.get(mActivity).destroy(mPlaceName);
        if (mISdkLoader != null) {
            mISdkLoader.notifyAdViewUIDismiss();
        }
    }

    private void closeViewMonitor() {
        try {
            View closeView = mActivity.getWindow().getDecorView().findViewWithTag("native_cancel_button");
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
            Window window = mActivity.getWindow();
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
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeViewMonitor();
                }
            }, 500);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOnBackPressed = true;
                }
            }, 5000);
        }
    }

    private void parseIntent() {
        Intent intent = mActivity.getIntent();
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
        RelativeLayout rootLayout = new RelativeLayout(mActivity);
        rootLayout.setBackgroundColor(Color.WHITE);
        mActivity.setContentView(rootLayout);
        mAdLayout = new RelativeLayout(mActivity);
        mAdLayout.setGravity(Gravity.CENTER);
        rootLayout.addView(mAdLayout, -1, -1);
        mCloseView = generateCloseView();
        int size = Utils.dp2px(mActivity, 24);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        int margin = dp2px(mActivity, 8);
        params.setMargins(margin, margin, 0, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
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
            mISdkLoader = AdPlaceLoader.sLoaderMap.remove(String.format(Locale.ENGLISH, "%s_%s_%s", mSource, mAdType, mPlaceName));
            Params params = AdPlaceLoader.sParamsMap.remove(String.format(Locale.ENGLISH, "%s_%s_%s", mSource, mAdType, mPlaceName));
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
        ImageView imageView = new ImageView(mActivity);
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
        int padding = Utils.dp2px(mActivity, 2);
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
            mActivity.finish();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

}
