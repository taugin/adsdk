package com.earch.sunny.picfg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scene.crazy.base.BPcy;
import com.scene.crazy.base.Cher;
import com.scene.crazy.scpolicy.LvPcy;
import com.earch.sunny.view.MarView;
import com.earch.sunny.view.ScrollLayout;
import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.adloader.spread.SpLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.listener.OnAdSdkListener;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.scene.crazy.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.adsdk.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018-10-16.
 */

public class ScActivity extends Activity implements IAdvance {

    private static final int MSG_ACTIVITY_DUP_CREATE = 0x10025;
    private static final int DELAY_ACTIVITY_DUP_CREATE = 10000;
    private String mPidName;
    private String mSource;
    private String mAdType;
    private String mSceneType;
    private Handler mHandler = null;
    private boolean mInLockView;
    private ViewGroup mLockAdLayout;
    private boolean mInChargeView;
    private Cher mCher;
    private ViewGroup mAdLayout;
    private ImageView mCloseView;
    private long mDelayClose;
    private boolean mCanClose = true;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInLockView = false;
        mInChargeView = false;
        parseIntent();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } catch (Exception e) {
        }
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
        if (mCher != null) {
            mCher.onResume();
        }
        closeViewMonitor();
    }

    private void closeViewMonitor() {
        try {
            View closeView = getWindow().getDecorView().findViewWithTag("native_cancel_button");
            if (closeView != null) {
                closeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCanClose) {
                            finishActivityWithDelay();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCher != null) {
            mCher.onPause();
        }
    }

    @Override
    public void onSceneImp(String adType, View containerView) {
        Log.v(Log.TAG, "adType : " + adType);
    }

    @Override
    public AdParams getAdParams(String adType) {
        return null;
    }

    @Override
    public View getRootLayout(Context context, String adType) {
        return null;
    }

    @Override
    public int getAdLayoutId(String adType) {
        return 0;
    }

    @Override
    public void setContentView(View view) {
        if (mInChargeView) {
            super.setContentView(view);
            return;
        }
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mInChargeView) {
            super.setContentView(layoutResID);
            return;
        }
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (mInChargeView) {
            super.setContentView(view, params);
            return;
        }
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mInLockView = false;
        mInChargeView = false;
        parseIntent();
        updateDataAndView();
    }

    private void updateFullScreenState() {
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    WindowManager.LayoutParams attributes = window.getAttributes();
                    window.setAttributes(attributes);
                    window.setStatusBarColor(Color.TRANSPARENT);
                    try {
                        if (isLockView()) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        }
                    } catch (Exception | Error e) {
                    }
                } else {
                    Window window = getWindow();
                    WindowManager.LayoutParams attributes = window.getAttributes();
                    attributes.flags |= flagTranslucentStatus;
                    window.setAttributes(attributes);
                }
            }
        } catch (Exception | Error e) {
        }
    }

    private void ensureChargeWrapper() {
        if (mCher == null) {
            mCher = new Cher(this);
        }
    }

    private void updateDataAndView() {
        Log.iv(Log.TAG, "update data and view");
        if (mHandler != null) {
            if (mHandler.hasMessages(MSG_ACTIVITY_DUP_CREATE)) {
                Log.iv(Log.TAG, "activity is showing");
                return;
            }
            mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_DUP_CREATE, DELAY_ACTIVITY_DUP_CREATE);
        }
        updateFullScreenState();
        if (mInChargeView) {
            ensureChargeWrapper();
            if (mCher != null) {
                mCher.showChargeView(mPidName);
            }
        } else if (isLockView()) {
            disableSystemLS();
            hideNavigationBar(this);
            showLockScreenView();
            LvPcy.get(this).reportImpression(true);
        } else if (!TextUtils.isEmpty(mPidName)) {
            show();
        } else {
            finishActivityWithDelay(10);
        }
    }

    private void disableSystemLS() {
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
                keyguardManager.requestDismissKeyguard(this, (KeyguardManager.KeyguardDismissCallback) null);
            }
        } catch (Exception e) {
        }
    }

    private void cleanSystemLS() {
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        } catch (Exception e) {
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mPidName = intent.getStringExtra(Intent.EXTRA_TITLE);
            mSource = intent.getStringExtra(Intent.EXTRA_TEXT);
            mAdType = intent.getStringExtra(Intent.EXTRA_TEMPLATE);
            mInLockView = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false);
            mInChargeView = intent.getBooleanExtra(Intent.EXTRA_QUIET_MODE, false);
            mSceneType = intent.getStringExtra(Intent.EXTRA_REPLACING);
            mDelayClose = intent.getLongExtra(Intent.ACTION_TIME_TICK, 0);
            if (!TextUtils.isEmpty(mSceneType)) {
                EventImpl.get().reportKVEvent(getBaseContext(), "show_scene_adv", mSceneType, null);
            }
        }
    }

    /**
     * 展示广告
     */
    private void show() {
        if (Constant.TYPE_NATIVE.equalsIgnoreCase(mAdType)
                || Constant.TYPE_BANNER.equalsIgnoreCase(mAdType)
                || Constant.AD_SDK_SPREAD.equalsIgnoreCase(mSource)) {
            showNativeAd();
        } else {
            finishActivityWithDelay();
        }
    }

    /**
     * 展示原生广告
     */
    private void showNativeAd() {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);
        super.setContentView(rootLayout);
        View adRootLayout = getRootLayout(rootLayout.getContext(), mAdType);
        int adLayoutId = getAdLayoutId(mAdType);
        ViewGroup adLayout = null;

        if (adRootLayout != null && adLayoutId > 0 && ((adLayout = adRootLayout.findViewById(adLayoutId)) instanceof ViewGroup)) {
            rootLayout.addView(adRootLayout, -1, -1);
        } else {
            adLayout = new RelativeLayout(this);
            ((RelativeLayout) adLayout).setGravity(Gravity.CENTER);
            rootLayout.addView(adLayout, -1, -1);
        }
        mAdLayout = adLayout;

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
        if (isAutoShowAdView()) {
            showAdViewInternal();
        }
    }

    private void animateCloseView() {
        if (mDelayClose > 0 && mDelayClose <= 5000) {
            mCanClose = false;
            mCloseView.clearAnimation();
            mCloseView.setVisibility(View.INVISIBLE);
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    0, 1f, 0, 1f,
                    Animation.RELATIVE_TO_SELF, .5f,
                    Animation.RELATIVE_TO_SELF, .5f);
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCanClose = true;
                    mCloseView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            scaleAnimation.setDuration(mDelayClose);
            scaleAnimation.setFillAfter(true);
            mCloseView.startAnimation(scaleAnimation);
        }
    }

    /**
     * 是否立即显示广告
     *
     * @return
     */
    protected boolean isAutoShowAdView() {
        return true;
    }

    /**
     * 返回关闭按钮
     *
     * @return
     */
    protected View getCloseView() {
        return mCloseView;
    }

    private void showAdViewInternal() {
        if (mAdLayout != null) {
            boolean shown = AdSdk.get(this).showComplexAdsWithResult(mPidName, getAdParams(mSceneType), mSource, mAdType, mAdLayout);
            if (shown) {
                onSceneImp(mSceneType, mAdLayout);
                try {
                    BPcy bPcy = BPcy.getPcyByType(mSceneType);
                    Log.v(Log.TAG, "report impression type : " + mSceneType);
                    bPcy.reportImpression(true);
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
                animateCloseView();
            } else {
                Log.v(Log.TAG, "can not find loader for " + getLocalClassName());
                finishActivityWithDelay();
            }
        }
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
        shapeNormal.getPaint().setColor(Color.parseColor("#88aaaaaa"));

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        imageView.setBackground(drawable);
        int padding = Utils.dp2px(this, 2);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setClickable(true);
        return imageView;
    }

    @Override
    public void onBackPressed() {
        if (isLockView()) {
            return;
        }
        if (mCanClose) {
            try {
                super.onBackPressed();
            } catch (Exception e) {
            }
        }
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
        cleanSystemLS();
        AdSdk.get(this).destroy(mPidName);

        if (Constant.AD_SDK_SPREAD.equals(mSource)) {
            SpLoader.reportDismiss(this);
        }
        if (mCher != null) {
            mCher.onDestroy();
        }
        stopTimeUpdate();
        if (mAdLayout != null) {
            OnAdSdkListener l = AdSdk.get(this).getOnAdSdkListener(mPidName);
            if (l != null) {
                l.onDismiss(mPidName, mSource, mAdType);
            }
        }
    }

    private int generateViewId(int defaultId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return defaultId;
    }

    /////////////////////////////////////////////////////////////////////////////////

    protected Drawable getBackgroudDrawable() {
        return null;
    }

    protected int getBackgroundColor() {
        return Color.parseColor("#FF699CFF");
    }

    private boolean isLockView() {
        return mInLockView;
    }

    /**
     * 展示锁屏界面
     */
    private TextView mTimeTextView;
    private TextView mWeekTextView;
    private BroadcastReceiver mTimeReceiver;

    private void showLockScreenView() {
        Log.v(Log.TAG, "show ls view");

        // 1，create Activity layout
        LinearLayout layout = new LinearLayout(this);
        super.setContentView(layout);

        layout.setOrientation(LinearLayout.VERTICAL);

        // 2，create Ad Pager layout
        LinearLayout pagerLayout = new LinearLayout(this);
        pagerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        pagerLayout.setOrientation(LinearLayout.VERTICAL);
        Drawable drawable = getBackgroudDrawable();
        if (drawable == null) {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(this);
                drawable = wm.getDrawable();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        }
        if (drawable == null) {
            layout.setBackgroundColor(getBackgroundColor());
        } else {
            layout.setBackground(drawable);
        }

        // 2.1，create TimeView
        mTimeTextView = new TextView(this);
        mTimeTextView.setTextColor(Color.WHITE);
        mTimeTextView.setGravity(Gravity.CENTER);
        mTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 72);
        mTimeTextView.setPadding(0, Utils.dp2px(this, 24f), 0, Utils.dp2px(this, 6f));
        LinearLayout.LayoutParams timeViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeViewParams.weight = 0;
        timeViewParams.topMargin = Utils.dp2px(this, 24f);
        pagerLayout.addView(mTimeTextView, timeViewParams);

        // 2.2，create WeekView
        mWeekTextView = new TextView(this);
        mWeekTextView.setTextColor(Color.WHITE);
        mWeekTextView.setGravity(Gravity.CENTER);
        mWeekTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mWeekTextView.setPadding(0, Utils.dp2px(this, 0f), 0, Utils.dp2px(this, 30f));
        LinearLayout.LayoutParams weekViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        weekViewParams.weight = 0;
        pagerLayout.addView(mWeekTextView, weekViewParams);

        // 2.3，create Ad Layout
        RelativeLayout adLayout = new RelativeLayout(this);
        adLayout.setGravity(Gravity.CENTER);
        adLayout.setPadding(Utils.dp2px(this, 8f), 0, Utils.dp2px(this, 8f), 0);
        mLockAdLayout = adLayout;
        LinearLayout.LayoutParams adLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        adLayoutParams.weight = 1;
        pagerLayout.addView(mLockAdLayout, adLayoutParams);

        // 2.4，create Scroll View
        TextView slideView = new MarView(this);
        slideView.setText(Utils.getStringById(this, "had_slide_unlock"));
        slideView.setTextColor(Color.WHITE);
        TextPaint tp = slideView.getPaint();
        if (tp != null) {
            tp.setFakeBoldText(true);
        }
        slideView.setGravity(Gravity.CENTER);
        slideView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        LinearLayout.LayoutParams slideViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slideViewParams.weight = 0;
        slideViewParams.bottomMargin = Utils.dp2px(this, 16f);
        slideViewParams.topMargin = Utils.dp2px(this, 16f);
        pagerLayout.addView(slideView, slideViewParams);

        View tempLayout = null;
        // 3，create ViewPager, set ViewPager Adapter
        final ScrollLayout scrollLayout = new ScrollLayout(this);
        scrollLayout.setOnScreenListener(new ScrollLayout.OnScreenListener() {
            @Override
            public void onScreenUnlocked() {
                fa();
                overridePendingTransition(0, 0);
            }
        });
        scrollLayout.addView(pagerLayout);
        tempLayout = scrollLayout;
        slideView.setText(Utils.getStringById(this, "had_slide_unlock"));

        // 5，add ViewPager to Activity layout
        layout.addView(tempLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        startTimeUpdate();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLockViewAd();
            }
        }, 500);
    }

    private void startTimeUpdate() {
        updateTime();

        stopTimeUpdate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        mTimeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_TIME_TICK)) {
                    updateTime();
                }
            }
        };
        try {
            registerReceiver(mTimeReceiver, filter);
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void updateTime() {
        Date date = new Date();
        mTimeTextView.setText(new SimpleDateFormat("H:mm").format(date));
        mWeekTextView.setText(new SimpleDateFormat("yyyy/MM/dd  EEE").format(date));
    }

    private void stopTimeUpdate() {
        try {
            if (mTimeReceiver != null) {
                unregisterReceiver(mTimeReceiver);
            }
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    public static void hideNavigationBar(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // lower api
                View v = activity.getWindow().getDecorView();
                v.setSystemUiVisibility(View.GONE);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                View decorView = activity.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void showLockViewAd() {
        AdParams params = getAdParams(mSceneType);
        if (params == null) {
            params = new AdParams.Builder()
                    .setBannerSize(AdExtra.AD_SDK_COMMON, AdExtra.COMMON_MEDIUM_RECTANGLE)
                    .setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_MEDIUM)
                    .build();
        }
        AdSdk.get(this).loadAdView(mPidName, params, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                if (!isFinishing()) {
                    AdSdk.get(getBaseContext()).showAdView(pidName, getAdParams(mSceneType), mLockAdLayout);
                    onSceneImp(mSceneType, mLockAdLayout);
                }
            }

            @Override
            public void onClick(String pidName, String source, String adType) {
            }
        });
    }
}