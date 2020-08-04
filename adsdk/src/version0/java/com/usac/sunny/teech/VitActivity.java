package com.usac.sunny.teech;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bacad.ioc.gsb.SceneSdk;
import com.bacad.ioc.gsb.base.BPcy;
import com.bacad.ioc.gsb.base.Cher;
import com.earch.sunny.picfg.IAdvance;
import com.earch.sunny.picfg.SpreadCfg;
import com.earch.sunny.view.MyTextView;
import com.earch.sunny.view.ScrollLayout;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.http.Http;
import com.hauyu.adsdk.http.OnImageCallback;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;
import com.hauyu.adsdk.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by Administrator on 2018-10-16.
 */

public class VitActivity extends Activity implements IAdvance {

    private SpreadCfg mSpreadCfg;
    private GestureDetector mGestureDetector;
    private String mPidName;
    private String mSource;
    private String mAdType;
    private String mAction;
    private String mSceneType;
    private Handler mHandler = null;
    private boolean mInLockView;
    private ViewGroup mLockAdLayout;
    private boolean mInChargeView;
    private Cher mCher;
    private ViewGroup mAdLayout;
    private ImageView mCloseView;
    private TextView mSponsoredView;
    private long mDelayClose;
    private boolean mCanClose = true;

    @Keep
    public static void init(Context context) {
        SceneSdk.init(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } catch (Exception e) {
        } catch (Error e) {
        }
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
            int id = getResources().getIdentifier("native_cancel_btn", "id", getPackageName());
            View closeView = findViewById(id);
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
            if (isLockView() || mInChargeView) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
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
        updateFullScreenState();
        if (mInChargeView) {
            ensureChargeWrapper();
            if (mCher != null) {
                mCher.showChargeView(mPidName);
            }
        } else if (isLockView()) {
            hideNavigationBar(this);
            showLockScreenView();
        } else if (mSpreadCfg != null) {
            showSpread();
        } else if (!TextUtils.isEmpty(mPidName)) {
            registerArgument();
            show();
        } else {
            finishActivityWithDelay(10);
        }
    }

    private void disableSystemLS() {
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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
            mSpreadCfg = (SpreadCfg) intent.getSerializableExtra(Intent.EXTRA_STREAM);
            mInLockView = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false);
            mInChargeView = intent.getBooleanExtra(Intent.EXTRA_QUIET_MODE, false);
            mSceneType = intent.getStringExtra(Intent.EXTRA_REPLACING);
            mAction = intent.getAction();
            mDelayClose = intent.getLongExtra(Intent.ACTION_TIME_TICK, 0);
            if (!TextUtils.isEmpty(mSceneType)) {
                EventImpl.get().reportKVEvent(getBaseContext(), "show_scene_adv", mSceneType, null);
            }
        }
    }

    /**
     * 注册相关参数
     */
    private void registerArgument() {
        if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            registerBroadcast();
            registerGesture();
        }
    }

    /**
     * 展示广告
     */
    private void show() {
        if (Constant.TYPE_NATIVE.equalsIgnoreCase(mAdType)
                || Constant.TYPE_BANNER.equalsIgnoreCase(mAdType)
                || Constant.AD_SDK_SPREAD.equalsIgnoreCase(mSource)) {
            showNAd();
        } else if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            // 当前action为PICKER并且存在RESOLVE的action，则弹出 C activity
            Intent intent = Utils.getIntentByAction(this, getPackageName() + ".action.AFRESOLVE");
            if (TextUtils.equals(mAction, getPackageName() + ".action.AFPICKER") && intent != null) {
                showCActivity(intent);
            } else {
                showGAd();
            }
        } else {
            finishActivityWithDelay();
        }
    }

    private void showCActivity(Intent intent) {
        Log.v(Log.TAG, "show c activity");
        try {
            intent.putExtra(Intent.EXTRA_TITLE, mPidName);
            intent.putExtra(Intent.EXTRA_TEXT, mSource);
            intent.putExtra(Intent.EXTRA_TEMPLATE, mAdType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishActivityWithDelay();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    /**
     * 展示原生广告
     */
    private void showNAd() {
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

    /**
     * 获取推广标记
     *
     * @return
     */
    protected View getSponsoredView() {
        return mSponsoredView;
    }

    protected void showAdView() {
        if (!isAutoShowAdView()) {
            showAdViewInternal();
        }
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

    private void registerGesture() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                finishActivityWithDelay();
                EventImpl.get().reportKVEvent(getBaseContext(), "close_fsa_byuser", "touch", null);
                return super.onDown(e);
            }
        });
    }

    /**
     * 展示插屏广告
     */
    private void showGAd() {
        if (!TextUtils.isEmpty(mPidName)) {
            boolean shown = AdSdk.get(this).showComplexAdsWithResult(mPidName, null, mSource, mAdType, null);
            if (!shown) {
                finishActivityWithDelay();
            }
        } else {
            finishActivityWithDelay();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
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
            if ((Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                    || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType))
                    && !Constant.AD_SDK_SPREAD.equals(mSource)) {
                EventImpl.get().reportKVEvent(this, "close_fsa_byuser", "backpressed", null);
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
        if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            unregister();
        } else {
            AdSdk.get(this).destroy(mPidName);
        }

        if (Constant.AD_SDK_SPREAD.equals(mSource)) {
            sendBroadcast(new Intent(getPackageName() + ".action.SPDISMISS").setPackage(getPackageName()));
        }
        if (mCher != null) {
            mCher.onDestroy();
        }
        stopTimeUpdate();
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter(getPackageName() + ".action.FA");
        try {
            registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void unregister() {
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finishActivityWithDelay();
        }
    };

    /////////////////////////////////////////////////////////////////////////////

    /**
     * 展示应用内推广页面
     */
    private void showSpread() {
        if (!checkArgs()) {
            finish();
            return;
        }
        try {
            showAppLayout();
            sendBroadcast(new Intent(getPackageName() + ".action.SPSHOW").setPackage(getPackageName()));
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
            finish();
        }
    }

    /**
     * 展示APP类型的推广
     */
    private void showAppLayout() {
        try {
            SpClick spClick = new SpClick();
            spClick.setSpConfig(mSpreadCfg);
            RelativeLayout rootLayout = new RelativeLayout(this);
            rootLayout.setBackgroundColor(Color.WHITE);
            super.setContentView(rootLayout);
            RelativeLayout adLayout = new RelativeLayout(this);
            adLayout.setGravity(Gravity.CENTER);
            rootLayout.addView(adLayout, -1, -1);

            ImageView imageView = generateCloseView();
            int size = Utils.dp2px(this, 24);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            int margin = dp2px(this, 8);
            params.setMargins(margin, margin, margin, margin);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishActivityWithDelay();
                }
            });
            rootLayout.addView(imageView, params);

            // 添加推广标识
            TextView textView = new TextView(this);
            textView.setText("sponsored");
            float[] roundArray = new float[]{16.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F};
            ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
            shapeDrawable.getPaint().setColor(Color.parseColor("#44000000"));
            textView.setBackground(shapeDrawable);
            textView.setTextColor(Color.parseColor("#F5F5F5"));
            params = new RelativeLayout.LayoutParams(-2, -2);
            margin = dp2px(this, 8);
            params.setMargins(margin, margin, margin, margin);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textView.setPadding(margin / 2, margin / 4, margin / 2, margin / 4);
            rootLayout.addView(textView, params);
            mSponsoredView = textView;

            // 添加对象
            LinearLayout adContentLayout = new LinearLayout(this);
            adContentLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout mediaLayout = new LinearLayout(this);
            // mediaLayout.setBackgroundColor(Color.RED);
            LinearLayout iconLayout = new LinearLayout(this);
            // iconLayout.setBackgroundColor(Color.GREEN);
            iconLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            LinearLayout textLayout = new LinearLayout(this);
            // textLayout.setBackgroundColor(Color.BLUE);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams lp = null;
            lp = new LinearLayout.LayoutParams(-1, -1);
            lp.weight = 10;
            adContentLayout.addView(mediaLayout, lp);
            lp = new LinearLayout.LayoutParams(-1, -1);
            lp.weight = 11;
            adContentLayout.addView(iconLayout, lp);
            lp = new LinearLayout.LayoutParams(-1, -1);
            lp.weight = 9;
            adContentLayout.addView(textLayout, lp);

            ImageView mediaView = new ImageView(this);
            mediaView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAndShowImage(mediaView, mSpreadCfg.getBanner());
            mediaLayout.addView(mediaView, -1, -2);
            mediaView.setOnClickListener(spClick);

            ImageView iconView = new ImageView(this);
            iconView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAndShowImage(iconView, mSpreadCfg.getIcon());
            iconLayout.addView(iconView, -2, -2);
            iconView.setOnClickListener(spClick);

            TextView titleView = new TextView(this);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(Color.BLACK);
            titleView.setText(mSpreadCfg.getTitle());
            titleView.setMaxLines(2);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            lp = new LinearLayout.LayoutParams(-2, -2);
            lp.leftMargin = lp.rightMargin = Utils.dp2px(this, 16);
            textLayout.addView(titleView, lp);
            titleView.setOnClickListener(spClick);

            TextView descView = new TextView(this);
            descView.setGravity(Gravity.CENTER);
            descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            descView.setTextColor(Color.GRAY);
            descView.setText(mSpreadCfg.getDetail());
            descView.setMaxLines(2);
            descView.setEllipsize(TextUtils.TruncateAt.END);
            lp = new LinearLayout.LayoutParams(-2, -2);
            lp.topMargin = Utils.dp2px(this, 8);
            lp.leftMargin = lp.rightMargin = Utils.dp2px(this, 16);
            textLayout.addView(descView, lp);
            descView.setOnClickListener(spClick);

            // 添加底部按钮
            RelativeLayout buttonLayout = new RelativeLayout(this);
            buttonLayout.setId(generateViewId(0x1000001));
            buttonLayout.setClickable(false);
            params = new RelativeLayout.LayoutParams(-1, dp2px(this, 84));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adLayout.addView(buttonLayout, params);

            Button button = new Button(this);
            params = new RelativeLayout.LayoutParams(-1, -1);
            margin = dp2px(this, 16);
            params.setMargins(margin, margin, margin, margin);
            buttonLayout.addView(button, params);

            params = new RelativeLayout.LayoutParams(-1, -1);
            params.addRule(RelativeLayout.ABOVE, buttonLayout.getId());
            adLayout.addView(adContentLayout, params);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setElevation(dp2px(this, 6));
                button.setTranslationZ(0);
            }
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor("#AA4286F4")));
            drawable.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(Color.parseColor("#FF4286F4")));
            button.setBackground(drawable);
            button.setPadding(0, 0, 0, 0);
            button.setGravity(Gravity.CENTER);
            button.getPaint().setFakeBoldText(true);
            button.setTextColor(Color.WHITE);
            button.setTypeface(button.getTypeface(), Typeface.BOLD);
            button.setSingleLine();
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            button.setText(mSpreadCfg.getCta());
            button.setOnClickListener(spClick);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
            finishActivityWithDelay();
        }
    }

    private void loadAndShowImage(final ImageView imageView, String url) {
        try {
            Http.get(imageView.getContext()).loadImage(url, null, new OnImageCallback() {

                @Override
                public void onSuccess(Bitmap bitmap) {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFailure(int code, String error) {
                }
            });
        } catch (Exception e) {
        }
    }

    private static class SpClick implements View.OnClickListener {

        private SpreadCfg mSpreadCfg;

        public void setSpConfig(SpreadCfg spreadCfg) {
            mSpreadCfg = spreadCfg;
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            String url = mSpreadCfg.getLinkUrl();
            if (TextUtils.isEmpty(url)) {
                url = "market://details?id=" + mSpreadCfg.getPkgname();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Context context = v.getContext();
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.v(Log.TAG, "error : " + e);
            }
            try {
                context.sendBroadcast(new Intent(context.getPackageName() + ".action.SPCLICK").setPackage(context.getPackageName()));
            } catch (Exception e) {
            }
        }
    }

    private int generateViewId(int defaultId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return defaultId;
    }

    private boolean checkArgs() {
        if (mSpreadCfg == null) {
            return false;
        }
        if (TextUtils.isEmpty(mSpreadCfg.getBanner())
                || TextUtils.isEmpty(mSpreadCfg.getIcon())
                || TextUtils.isEmpty(mSpreadCfg.getTitle())
                || TextUtils.isEmpty(mSpreadCfg.getPkgname())
                || TextUtils.isEmpty(mSpreadCfg.getDetail())
                || TextUtils.isEmpty(mSpreadCfg.getCta())) {
            return false;
        }
        return true;
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

    /**
     * 创建ViewPager，异常时返回空值
     *
     * @return
     */
    private ViewPager createViewPager() {
        try {
            ViewPager viewPager = new ViewPager(this);
            ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        fa();
                        overridePendingTransition(0, 0);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
            try {
                viewPager.setOffscreenPageLimit(2);
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e);
            }
            try {
                viewPager.addOnPageChangeListener(listener);
            } catch (Exception | Error e) {
                try {
                    viewPager.setOnPageChangeListener(listener);
                } catch (Exception | Error error) {
                    Log.e(Log.TAG, "error : " + e);
                    return null;
                }
            }
            return viewPager;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

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
            pagerLayout.setBackgroundColor(getBackgroundColor());
        } else {
            pagerLayout.setBackground(drawable);
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
        TextView slideView = new MyTextView(this);
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
        if (false) {
            try {
                ViewPager viewPager = createViewPager();
                LsViewPagerAdapter adapter = new LsViewPagerAdapter(pagerLayout);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
                tempLayout = viewPager;
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e);
            }
        }

        if (tempLayout == null) {
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
        }

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

        registerReceiver(mTimeReceiver, filter);
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
                if (mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fa();
                        }
                    }, 500);
                } else {
                    fa();
                }
            }
        });
    }

    private class LsViewPagerAdapter extends PagerAdapter {

        private ViewGroup mAdView;

        public LsViewPagerAdapter(ViewGroup adView) {
            mAdView = adView;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position == 0) {
                View view = new View(container.getContext());
                view.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return view;
            } else if (position == 1) {
                container.addView(mAdView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return mAdView;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}