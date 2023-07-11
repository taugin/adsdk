package com.hauyu.adsdk;

import android.app.Activity;
import android.app.Application;
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

import com.hauyu.adsdk.adloader.listener.ISdkLoader;
import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.core.framework.AdPlaceLoader;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.log.Log;

import java.lang.reflect.Field;
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
    private String mSceneName;

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
            Log.iv(Log.TAG, "error : " + e);
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
            mSceneName = intent.getStringExtra(Intent.EXTRA_REFERRER_NAME);
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
            if (params != null) {
                params.setSceneName(mSceneName);
            }
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
        Log.iv(Log.TAG, "");
        try {
            mActivity.finish();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    static class FActivity extends Activity {
        private Application application;

        public FActivity(Application application) {
            this.application = application;
        }

        @Override
        public boolean isFinishing() {
            return false;
        }

        @Override
        public void startActivity(Intent intent) {
            try {
                configIntent(application, intent);
                application.startActivity(intent);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }

        @Override
        public Context getApplicationContext() {
            try {
                return application.getApplicationContext();
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return super.getApplicationContext();
        }

        @Override
        public String getLocalClassName() {
            try {
                return super.getLocalClassName();
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return VUIHelper.class.getName();
        }

        @Override
        public Object getSystemService(String name) {
            try {
                return application.getSystemService(name);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return super.getSystemService(name);
        }

        @Override
        public <T extends View> T findViewById(int id) {
            try {
                return super.findViewById(id);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
                try {
                    Activity topActivity = ActivityMonitor.get(this).getTopActivity();
                    if (topActivity != null) {
                        Window window = topActivity.getWindow();
                        if (window != null) {
                            return window.findViewById(id);
                        }
                    }
                } catch (Exception | Error error) {
                    Log.iv(Log.TAG, "error : " + error);
                }
            }
            return null;
        }

        @Override
        public Window getWindow() {
            try {
                Activity topActivity = ActivityMonitor.get(this).getTopActivity();
                if (topActivity != null) {
                    Window window = topActivity.getWindow();
                    return window;
                }
            } catch (Exception e) {
            }
            return super.getWindow();
        }
    }

    public static Activity getFA(final Application application) {
        Activity activity = new FActivity(application);
        try {
            Class ContextWrapperClass = Class.forName("android.content.ContextWrapper");
            Field mBase = ContextWrapperClass.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(activity, application.getBaseContext());

            Class ActivityClass = Class.forName("android.app.Activity");
            Field mApplication = ActivityClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            mApplication.set(activity, application);

            WindowManager wm = (WindowManager) application.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
            Field mWindowManager = ActivityClass.getDeclaredField("mWindowManager");
            mWindowManager.setAccessible(true);
            mWindowManager.set(activity, wm);
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return activity;
    }

    public static Context createAContext(final Context context) {
        AppContext appContext = new AppContext(context);
        return appContext;
    }

    private static class AppContext extends Application {

        public AppContext(Context base) {
            super();
            attachBaseContext(base);
        }

        @Override
        public void startActivity(Intent intent) {
            configIntent(getBaseContext(), intent);
            super.startActivity(intent);
        }

        @Override
        public void startActivity(Intent intent, Bundle options) {
            configIntent(getBaseContext(), intent);
            super.startActivity(intent, options);
        }
    }

    private static void configIntent(Context context, Intent intent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}
