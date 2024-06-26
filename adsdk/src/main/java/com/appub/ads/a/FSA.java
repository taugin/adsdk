package com.appub.ads.a;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
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
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.SpConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.http.Http;
import com.inner.adsdk.http.OnImageCallback;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.loader.ChargeWrapper;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.policy.AtPolicy;
import com.inner.adsdk.policy.CtPolicy;
import com.inner.adsdk.policy.GtPolicy;
import com.inner.adsdk.policy.HtPolicy;
import com.inner.adsdk.policy.LtPolicy;
import com.inner.adsdk.policy.StPolicy;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018-10-16.
 */

public class FSA extends Activity {

    private static final String NATIVE_TEMPLATE = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=viewport content=\"width=device-width,minimum-scale=1,maximum-scale=1,user-scalable=no\"><title>Install Page</title><style>html,body{margin:0;padding:0;height:100%;width:100%}.container{display:flex;flex-direction:column;height:100%}.row-box-1{flex:1}.row-box-2{flex:2}.row-box-3{flex:3}.cover img{width:auto;height:auto;max-width:100%;max-height:200px;clear:both;display:block;margin:auto;}.app{align-self:center;display:flex;flex-direction:column;text-align:center}.app-icon img{width:72px}.app-title{font-size:24px}.app-desc{font-size:16px;color:grey}.app-title,.app-title{padding:12px}.install-box{text-align:center}.install-btn{background:green;margin-bottom:30px}a.install-btn{display:inline-block;width:50%;height:48px;line-height:48px;border-radius:6px;color:white;text-decoration:none}</style></head><body><div class=\"container\"><div class=\"cover\"><img src=\"#COVER_URL#\"alt=\"\" id=\"cover-img\"></div><div class=\"row-box-3 app\"><div class=\"row-box-1\"></div><div class=\"row-box-2 app-icon\"><img src=\"#ICON_URL#\"alt=\"\"></div><div class=\"app-title\">#TITLE#</div><div class=\"app-desc\">#DESC#</div><div class=\"row-box-1\"></div></div></div><script>var coverImg=document.getElementById('cover-img');coverImg.addEventListener('error',function(){this.parentNode.remove(this)},false)</script></body></html>";

    private SpConfig mSpConfig;
    private GestureDetector mGestureDetector;
    private String mPidName;
    private String mSource;
    private String mAdType;
    private String mAction;
    private Handler mHandler = null;
    private boolean mInLockView;
    private ViewGroup mLockAdLayout;
    private boolean mInChargeView;
    private ChargeWrapper mChargeWrapper;
    private ViewGroup mAdLayout;
    private ImageView mCloseView;
    private TextView mSponsoredView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInLockView = false;
        mInChargeView = false;
        parseIntent();
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
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChargeWrapper != null) {
            mChargeWrapper.onResume();
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
                        finishActivityWithDelay();
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
        if (mChargeWrapper != null) {
            mChargeWrapper.onPause();
        }
    }

    protected void onAdShowing(View containerView) {
    }

    protected AdParams getAdParams() {
        return null;
    }

    protected View getRootLayout(Context context, String adType) {
        return null;
    }

    protected int getAdLayoutId(String adType) {
        return 0;
    }

    protected void onLtShowing(View containerView) {
    }

    protected AdParams getLtParams() {
        return null;
    }

    public void onCtShowing(View containerView) {
    }

    public AdParams getCtParams() {
        return null;
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
        if (mChargeWrapper == null) {
            mChargeWrapper = new ChargeWrapper(this);
        }
    }

    private void updateDataAndView() {
        updateFullScreenState();
        if (mInChargeView) {
            ensureChargeWrapper();
            if (mChargeWrapper != null) {
                mChargeWrapper.showChargeView();
            }
        } else if (isLockView()) {
            hideNavigationBar(this);
            showLockScreenView();
        } else if (mSpConfig != null) {
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
            mSpConfig = (SpConfig) intent.getSerializableExtra(Intent.EXTRA_STREAM);
            mInLockView = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false);
            mInChargeView = intent.getBooleanExtra(Intent.EXTRA_QUIET_MODE, false);
            mAction = intent.getAction();
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
                || Constant.TYPE_BANNER.equalsIgnoreCase(mAdType)) {
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
            boolean shown = AdSdk.get(this).showComplexAdsWithResult(mPidName, getAdParams(), mSource, mAdType, mAdLayout);
            if (shown) {
                onAdShowing(mAdLayout);
                if (TextUtils.equals(Constant.NTPLACE_OUTER_NAME, mPidName)
                        || TextUtils.equals(Constant.GTPLACE_OUTER_NAME, mPidName)) {
                    GtPolicy.get(this).reportShowing(true);
                } else if (TextUtils.equals(Constant.HTPLACE_OUTER_NAME, mPidName)) {
                    HtPolicy.get(this).reportShowing(true);
                } else if (TextUtils.equals(Constant.ATPLACE_OUTER_NAME, mPidName)) {
                    AtPolicy.get(this).reportShowing(true);
                } else if (TextUtils.equals(Constant.STPLACE_OUTER_NAME, mPidName)) {
                    StPolicy.get(this).reportShowing(true);
                } else if (TextUtils.equals(Constant.CTPLACE_OUTER_NAME, mPidName)) {
                    CtPolicy.get(this).reportShowing(true);
                } else if (TextUtils.equals(Constant.LTPLACE_OUTER_NAME, mPidName)) {
                    LtPolicy.get(this).reportShowing(true);
                }
            } else {
                Log.v(Log.TAG, "can not find loader for FSA");
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
                StatImpl.get().reportFinishFSA(getBaseContext(), "close_fsa_byuser", "touch");
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
        try {
            super.onBackPressed();
        } catch (Exception e) {
        }
        if ((Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType))
                && !Constant.AD_SDK_SPREAD.equals(mSource)) {
            StatImpl.get().reportFinishFSA(this, "close_fsa_byuser", "backpressed");
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
        if (mChargeWrapper != null) {
            mChargeWrapper.onDestroy();
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

            if (TextUtils.equals(SpConfig.TYPE_URL, mSpConfig.getType())
                    || TextUtils.equals(SpConfig.TYPE_HTML, mSpConfig.getType())) {
                showWebViewLayout();
            } else {
                showAppLayout();
            }
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
            spClick.setSpConfig(mSpConfig);
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
            loadAndShowImage(mediaView, mSpConfig.getBanner());
            mediaLayout.addView(mediaView, -1, -2);
            mediaView.setOnClickListener(spClick);

            ImageView iconView = new ImageView(this);
            iconView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAndShowImage(iconView, mSpConfig.getIcon());
            iconLayout.addView(iconView, -2, -2);
            iconView.setOnClickListener(spClick);

            TextView titleView = new TextView(this);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(Color.BLACK);
            titleView.setText(mSpConfig.getTitle());
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
            descView.setText(mSpConfig.getDetail());
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
            button.setText(mSpConfig.getCta());
            button.setOnClickListener(spClick);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
            finishActivityWithDelay();
        }
    }

    /**
     * 展示webview类型的推广
     */
    private void showWebViewLayout() {
        try {
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

            // 添加webview对象
            WebView webview = new WebView(this);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

            params = new RelativeLayout.LayoutParams(-1, -1);
            adLayout.addView(webview, params);

            if (TextUtils.equals(SpConfig.TYPE_URL, mSpConfig.getType())) {
                showUrlContent(webview, mSpConfig.getLinkUrl());
            } else if (TextUtils.equals(SpConfig.TYPE_HTML, mSpConfig.getType())) {
                showHtml(webview, mSpConfig.getHtml());
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void showUrlContent(WebView webView, String url) {
        if (webView == null || TextUtils.isEmpty(url)) {
            return;
        }
        try {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    Log.v(Log.TAG, "message : " + message);
                }
            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                    }
                    return true;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                    }
                    return true;
                }
            });
            webView.loadUrl(url);
        } catch (Exception e) {
            Log.e(Log.TAG, "error " + e);
        }
    }

    private void showHtml(final WebView webView, String html) {
        if (webView == null) {
            return;
        }
        try {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    Log.v(Log.TAG, "message : " + message);
                }
            });
            webView.loadData(html, "text/html", "utf-8");
        } catch (Exception e) {
            Log.e(Log.TAG, "error " + e);
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

        private SpConfig mSpConfig;

        public void setSpConfig(SpConfig spConfig) {
            mSpConfig = spConfig;
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            String url = mSpConfig.getLinkUrl();
            if (TextUtils.isEmpty(url)) {
                url = "market://details?id=" + mSpConfig.getPkgname();
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
        if (mSpConfig == null) {
            return false;
        }
        if (TextUtils.isEmpty(mSpConfig.getType()) || TextUtils.equals(SpConfig.TYPE_APP, mSpConfig.getType())) {
            if (TextUtils.isEmpty(mSpConfig.getBanner())
                    || TextUtils.isEmpty(mSpConfig.getIcon())
                    || TextUtils.isEmpty(mSpConfig.getTitle())
                    || TextUtils.isEmpty(mSpConfig.getPkgname())
                    || TextUtils.isEmpty(mSpConfig.getDetail())
                    || TextUtils.isEmpty(mSpConfig.getCta())) {
                return false;
            }
        } else if (TextUtils.equals(SpConfig.TYPE_URL, mSpConfig.getType())) {
            if (TextUtils.isEmpty(mSpConfig.getLinkUrl())) {
                return false;
            }
        } else if (TextUtils.equals(SpConfig.TYPE_HTML, mSpConfig.getType())) {
            if (TextUtils.isEmpty(mSpConfig.getHtml())) {
                return false;
            }
        } else {
            if (TextUtils.isEmpty(mSpConfig.getBanner())
                    || TextUtils.isEmpty(mSpConfig.getIcon())
                    || TextUtils.isEmpty(mSpConfig.getTitle())
                    || TextUtils.isEmpty(mSpConfig.getPkgname())
                    || TextUtils.isEmpty(mSpConfig.getDetail())
                    || TextUtils.isEmpty(mSpConfig.getCta())) {
                return false;
            }
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////

    protected Drawable getBackgroudDrawable() {
        return null;
    }

    protected int getBackgroundColor() {
        return Color.BLACK;
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
        slideView.setText(R.string.ad_slide_unlock);
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
        try {
            ViewPager viewPager = createViewPager();
            LsViewPagerAdapter adapter = new LsViewPagerAdapter(pagerLayout);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(1);
            tempLayout = viewPager;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
        }

        if (tempLayout == null || BuildConfig.DEBUG) {
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
            slideView.setText(R.string.ad_slide_unlock);
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
        AdParams params = getLtParams();
        if (params == null) {
            params = new AdParams.Builder()
                    .setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE)
                    .setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE)
                    .setBannerSize(AdExtra.AD_SDK_DSPMOB, AdExtra.DSPMOB_MEDIUM_RECTANGLE)
                    .setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_MEDIUM)
                    .build();
        }
        AdSdk.get(this).loadAdView(Constant.LTPLACE_OUTER_NAME, params, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                if (!isFinishing()) {
                    AdSdk.get(getBaseContext()).showAdView(pidName, getLtParams(), mLockAdLayout);
                    onLtShowing(mLockAdLayout);
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

    /**
     * 监听Banner或native是否可见的类
     */
    public static class MView extends View {

        private boolean mViewDetached = false;
        private boolean mViewVisible = true;

        public MView(Context context) {
            super(context);
        }

        public boolean isViewVisible() {
            return !mViewDetached && mViewVisible;
        }

        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            mViewVisible = visibility == View.VISIBLE;
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mViewDetached = true;
        }
    }

    @SuppressLint("AppCompatCustomView")
    public class MyTextView extends TextView {
        private LinearGradient mLinearGradient;
        private Matrix mGradientMatrix;
        private Paint mPaint;
        private int mViewWidth = 0;
        private int mTranslate = 0;

        private boolean mAnimating = true;
        private int delta = 15;

        public MyTextView(Context ctx) {
            this(ctx, null);
        }

        public MyTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (mViewWidth == 0) {
                mViewWidth = getMeasuredWidth();
                if (mViewWidth > 0) {
                    mPaint = getPaint();
                    String text = getText().toString();
                    int size;
                    if (text.length() > 0) {
                        size = mViewWidth * 3 / text.length();
                    } else {
                        size = mViewWidth;
                    }
                    mLinearGradient = new LinearGradient(-size, 0, 0, 0,
                            new int[]{0x88ffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0x88ffffff},
                            new float[]{0, 0.1f, 0.5f, 0.9f, 1}, Shader.TileMode.CLAMP); //边缘融合
                    mPaint.setShader(mLinearGradient);
                    mGradientMatrix = new Matrix();
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mAnimating && mGradientMatrix != null) {
                float mTextWidth = getPaint().measureText(getText().toString());
                mTranslate += delta;
                if (mTranslate > mTextWidth + 100 || mTranslate < 1) {
                    mTranslate = 0;
                }
                mGradientMatrix.setTranslate(mTranslate, 0);
                mLinearGradient.setLocalMatrix(mGradientMatrix);
                postInvalidateDelayed(50);
            }
        }
    }

    public static class DotProgress extends View {
        private Paint emptyPaint;
        private Paint filledPaint;
        private boolean init = false;
        private int step = -1;
        private float dotRadius;
        private float margin;
        private final int stepCount;

        public DotProgress(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.DotProgress, 0, 0);
            int baseColor, emptyAlpha, filledAlpha;
            try {
                emptyAlpha = a.getInteger(R.styleable.DotProgress_emptyAlpha, 63);
                filledAlpha = a.getInteger(R.styleable.DotProgress_filledAlpha, 159);
                stepCount = a.getInteger(R.styleable.DotProgress_stepCount, 3);
                baseColor = a.getColor(R.styleable.DotProgress_dotBaseColor, Color.WHITE);
                dotRadius = a.getDimensionPixelSize(R.styleable.DotProgress_dotRadius,
                        (int) (getResources().getDisplayMetrics().density * 3));
            } finally {
                a.recycle();
            }
            int baseColorR = Color.red(baseColor);
            int baseColorG = Color.green(baseColor);
            int baseColorB = Color.blue(baseColor);
            emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            emptyPaint.setARGB(emptyAlpha, baseColorR, baseColorG, baseColorB);
            filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            filledPaint.setARGB(filledAlpha, baseColorR, baseColorG, baseColorB);
        }

        public void setStep(int nextStep) {
            if (nextStep >= stepCount || nextStep < -1) {
                throw new IllegalArgumentException(
                        "Step count should be with in [-1" + (stepCount - 1) + "]");
            }
            this.step = nextStep;
            postInvalidate();
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (!init) {
                init = true;
                margin = (canvas.getWidth() - stepCount * dotRadius * 2) / (stepCount + 1);
            }
            for (int index = 0; index < stepCount; ++index) {
                canvas.drawCircle(margin + dotRadius + index * (margin + 2 * dotRadius), canvas.getHeight() / 2,
                        dotRadius, index <= step ? filledPaint : emptyPaint);
            }
        }
    }

    public static class BlinkImageView extends AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {
        private int startAlpha, endAlpha, duration;
        private boolean isBlinking = false;

        ValueAnimator colorAnimation;

        public BlinkImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.BlinkImageView, 0, 0);
            try {
                startAlpha = a.getInteger(R.styleable.BlinkImageView_startAlpha, 63);
                endAlpha = a.getInteger(R.styleable.BlinkImageView_endAlpha, 255);
                duration = a.getInteger(R.styleable.BlinkImageView_blinkDuration, 800);
            } finally {
                a.recycle();
            }
            setAlpha(startAlpha / 256f);
        }

        public void startBlink() {
            if (!isBlinking) {
                colorAnimation = ValueAnimator.ofObject(new IntEvaluator(), startAlpha, endAlpha);
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
                colorAnimation.setDuration(duration); // milliseconds
                colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                colorAnimation.addUpdateListener(this);
                colorAnimation.start();
                isBlinking = true;
            }
        }

        public void stopBlink() {
            if (isBlinking) {
                colorAnimation.removeUpdateListener(this);
                colorAnimation.end();
                postInvalidate();
                isBlinking = false;
            }
        }

        public void solid() {
            setAlpha(1.0f);
        }

        public void halftrans() {
            setAlpha(startAlpha / 256f);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int alphaValue = (int) valueAnimator.getAnimatedValue();
            setAlpha(alphaValue / 256f);
        }
    }

    public static class ScrollLayout extends FrameLayout {

        private static final int UNKNOWN = 0;
        private static final int VERTICAL = 1;
        private static final int HORIZONTAL = 2;
        private GestureDetector mGestureDetector1;
        private GestureDetector mGestureDetector2;
        private float mDownY;
        private float mDownX;
        private Rect mViewRect = new Rect();
        private Rect mClipRect = new Rect();
        private Scroller mScroller;
        private boolean mOpened = false;
        private int mMinSlop = 0;
        private int mOrientation = UNKNOWN;

        public ScrollLayout(Context context) {
            super(context);
            init();
        }

        public ScrollLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mGestureDetector1 = new GestureDetector(getContext(), new GestureListener(false));
            mGestureDetector2 = new GestureDetector(getContext(), new GestureListener(true));
            mScroller = new Scroller(getContext());
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mViewRect.set(0, 0, dm.widthPixels, dm.heightPixels);
            mClipRect.set(mViewRect);
            mMinSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }

        @Override
        public void addView(View child, int index,
                            android.view.ViewGroup.LayoutParams params) {
            if (getChildCount() > 0) {
                throw new IllegalArgumentException("max child is 1");
            }
            super.addView(child, index, params);
        }

        @Override
        public void computeScroll() {
            if (mScroller != null && mScroller.computeScrollOffset()) {
                if (mOrientation == VERTICAL) {
                    float dy = mScroller.getCurrY() - mClipRect.bottom;
                    updateVertical(dy, true);
                } else if (mOrientation == HORIZONTAL) {
                    float dx = mScroller.getCurrX() - mClipRect.left;
                    updateHorizontal(dx, true);
                }
            } else {
                if (mOpened && mScroller != null && mScroller.isFinished()) {
                    mOpened = false;
                    mOrientation = UNKNOWN;
                    openScreen();
                }
            }
        }

        private void openScreen() {
            if (mOnScreenListener != null) {
                mOnScreenListener.onScreenUnlocked();
            }
        }

        private void updateVertical(float deltaY, boolean forceUpdate) {
            if (mClipRect.bottom + deltaY < mViewRect.bottom) {
                mClipRect.bottom += deltaY;
            } else if (mScroller != null && mScroller.isFinished()) {
                mClipRect.bottom = mViewRect.bottom;
            }
            if (Math.abs(mClipRect.bottom - mViewRect.bottom) > mMinSlop || forceUpdate) {
                if (getChildAt(0) != null) {
                    getChildAt(0).setTranslationY(mClipRect.bottom - mViewRect.bottom);
                    invalidate();
                }
            }
            if (mScroller != null && mScroller.isFinished() && Math.abs(mViewRect.bottom - mClipRect.bottom) <= 0) {
                mOrientation = UNKNOWN;
            }
        }

        private void updateHorizontal(float deltaX, boolean forceUpdate) {
            if (mClipRect.left + deltaX > mViewRect.left) {
                mClipRect.left += deltaX;
            } else if (mScroller != null && mScroller.isFinished()) {
                mClipRect.left = mViewRect.left;
            }
            if (Math.abs(mClipRect.left - mViewRect.left) > mMinSlop || forceUpdate) {
                if (getChildAt(0) != null) {
                    getChildAt(0).setTranslationX(mClipRect.left - mViewRect.left);
                    invalidate();
                }
            }
            if (mScroller != null && mScroller.isFinished() && Math.abs(mClipRect.left - mViewRect.left) <= 0) {
                mOrientation = UNKNOWN;
            }
        }

        private void resetByAnimateVertical(boolean forceOpen) {
            int dy;
            int duration;
            if (mClipRect.bottom > mViewRect.height() / 2 && !forceOpen) {
                dy = mViewRect.bottom - mClipRect.bottom;
                mOpened = false;
                mScroller = new Scroller(getContext(),
                        new BounceInterpolator());
                duration = 1000;
            } else {
                dy = -mClipRect.bottom;
                mOpened = true;
                mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
                duration = 500;
            }
            mScroller.startScroll(0, mClipRect.bottom, 0, dy, duration);
            invalidate();
        }

        private void resetByAnimateHorizontal(boolean forceOpen) {
            int dx;
            int duration;
            if (mClipRect.left < mViewRect.width() / 2 && !forceOpen) {
                dx = mViewRect.left - mClipRect.left;
                mOpened = false;
                mScroller = new Scroller(getContext(),
                        new BounceInterpolator());
                duration = 1000;
            } else {
                dx = mClipRect.left;
                mOpened = true;
                mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
                duration = 500;
            }
            mScroller.startScroll(mClipRect.left, 0, dx, 0, duration);
            invalidate();
        }


        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (mGestureDetector1.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (mOrientation == VERTICAL) {
                    if (mViewRect.bottom != mClipRect.bottom) {
                        resetByAnimateVertical(false);
                    }
                } else if (mOrientation == HORIZONTAL) {
                    if (mViewRect.left != mClipRect.left) {
                        resetByAnimateHorizontal(false);
                    }
                }
            }
            return super.onInterceptTouchEvent(event);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mGestureDetector2.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (mOrientation == VERTICAL) {
                    if (mViewRect.bottom != mClipRect.bottom) {
                        resetByAnimateVertical(false);
                    }
                } else if (mOrientation == HORIZONTAL) {
                    if (mViewRect.left != mClipRect.left) {
                        resetByAnimateHorizontal(false);
                    }
                }
            }
            return super.onTouchEvent(event);
        }

        private class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private boolean mInterceptDownEvent;

            public GestureListener(boolean interceptDownEvent) {
                mInterceptDownEvent = interceptDownEvent;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                mDownX = e.getX(0);
                mDownY = e.getY(0);
                if (mScroller != null) {
                    mScroller.abortAnimation();
                }
                return mInterceptDownEvent;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                float x1 = 0f;
                float y1 = 0f;
                float x2 = 0f;
                float y2 = 0f;
                try {
                    x1 = e1.getX(0);
                    x2 = e2.getX(0);

                    y1 = e1.getY(0);
                    y2 = e2.getY(0);
                } catch (Exception e) {
                }
                if (mOrientation == UNKNOWN) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        mOrientation = HORIZONTAL;
                    } else {
                        mOrientation = VERTICAL;
                    }
                }
                if (mOrientation == VERTICAL) {
                    if (mScroller != null) {
                        mScroller.fling(0, mClipRect.bottom, 0, (int) velocityY,
                                0, 0, 0, mClipRect.bottom);
                        mClipRect.bottom = mScroller.getFinalY();
                        if (velocityY > 0 && mClipRect.bottom < getHeight() / 2) {
                            mClipRect.bottom = getHeight() / 2 + 10;
                        }
                        resetByAnimateVertical(velocityY < 0 && mScroller.getFinalY() < getHeight() / 2);
                    }
                    return true;
                } else if (mOrientation == HORIZONTAL) {
                    if (mScroller != null) {
                        mScroller.fling(mClipRect.left, 0, (int) (velocityX / 2), 0,
                                0, mClipRect.right, 0, 0);
                        mClipRect.left = mScroller.getFinalX();
                        if (velocityX < 0 && mClipRect.left > getWidth() / 2) {
                            mClipRect.left = getWidth() / 2 - 10;
                        }
                        resetByAnimateHorizontal(velocityX > 0 && mScroller.getFinalX() > getWidth() * 2 / 3);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
                float x1 = 0f;
                float y1 = 0f;
                float x2 = 0f;
                float y2 = 0f;
                try {
                    x1 = e1.getX(0);
                    x2 = e2.getX(0);

                    y1 = e1.getY(0);
                    y2 = e2.getY(0);
                } catch (Exception e) {
                }
                if (mOrientation == UNKNOWN && Math.abs(x1 - x2) > mMinSlop) {
                    mOrientation = HORIZONTAL;
                } else if (mOrientation == UNKNOWN && Math.abs(y1 - y2) > mMinSlop) {
                    mOrientation = VERTICAL;
                }
                if (mOrientation == VERTICAL) {
                    float y = e2.getY(0);
                    float deltaY = y - mDownY;
                    mDownY = y;
                    updateVertical(deltaY, false);
                    return true;
                } else if (mOrientation == HORIZONTAL) {
                    float x = e2.getX(0);
                    float deltaX = x - mDownX;
                    mDownX = x;
                    updateHorizontal(deltaX, false);
                    return true;
                }
                return false;
            }
        }

        private OnScreenListener mOnScreenListener;

        public void setOnScreenListener(OnScreenListener l) {
            mOnScreenListener = l;
        }

        public interface OnScreenListener {
            void onScreenUnlocked();
        }
    }

    public static class ShimmerView extends View {
        private static final int ANIMATION_TIME = 2000;
        private Shader mGradient = null;
        private Matrix mGradientMatrix = null;
        private Paint mPaint;
        private float mCornerRadius = 0f;
        private int mViewWidth = 0;
        private int mViewHeight = 0;
        private float mTranslateX = 0f;
        private float mTranslateY = 0f;
        private RectF rectF;
        private ValueAnimator valueAnimator;
        private boolean autoRun = true; //是否自动运行动画

        public ShimmerView(Context context) {
            super(context);
            init(context, null);
        }

        public ShimmerView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }

        public ShimmerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context, attrs);
        }

        private void init(Context context, AttributeSet attrs) {
            rectF = new RectF();
            mPaint = new Paint();
            initGradientAnimator();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            rectF.set(0, 0, getWidth(), getHeight());
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (mViewWidth == 0) {
                mViewWidth = getWidth();
                mViewHeight = getHeight();
                if (mViewWidth > 0) {
                    mGradient = new LinearGradient(0f, 0f, mViewWidth, mViewHeight,
                            new int[]{0x00ffffff, 0x73ffffff, 0x00ffffff},
                            new float[]{0.4f, 0.45f, 0.5f},
                            Shader.TileMode.CLAMP);
                    mPaint.setShader(mGradient);
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
                    mGradientMatrix = new Matrix();
                    mGradientMatrix.setTranslate((-2 * mViewWidth), mViewHeight);
                    mGradient.setLocalMatrix(mGradientMatrix);
                    rectF.set(0, 0, w, h);
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (valueAnimator != null && valueAnimator.isRunning() && mGradientMatrix != null) {
                canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, mPaint);
            }
        }

        private void initGradientAnimator() {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f);
            valueAnimator.setDuration(ANIMATION_TIME);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    //❶ 改变每次动画的平移x、y值，范围是[-2mViewWidth, 2mViewWidth]
                    mTranslateX = 4f * mViewWidth * v - mViewWidth * 2;
                    mTranslateY = mViewHeight * v;
                    //❷ 平移matrix, 设置平移量
                    if (mGradientMatrix != null) {
                        mGradientMatrix.setTranslate(mTranslateX, mTranslateY);
                    }
                    //❸ 设置线性变化的matrix
                    if (mGradient != null) {
                        mGradient.setLocalMatrix(mGradientMatrix);
                    }
                    //❹ 重绘
                    invalidate();
                }
            });
            if (autoRun && valueAnimator != null) {
                valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startAnimation();
                    }
                });
            }
        }

        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            if (visibility == View.VISIBLE) {
                startAnimation();
            } else {
                stopAnimation();
            }
        }

        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            if (visibility == View.VISIBLE) {
                startAnimation();
            } else {
                stopAnimation();
            }
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            if (!hasWindowFocus) {
                stopAnimation();
            } else {
                startAnimation();
            }
        }

        @Override
        public void setVisibility(int visibility) {
            super.setVisibility(visibility);
            if (visibility == View.VISIBLE) {
                startAnimation();
            } else {
                stopAnimation();
            }
        }

        //停止动画
        private void stopAnimation() {
            if (valueAnimator != null && valueAnimator.isRunning()) {
                valueAnimator.cancel();
                invalidate();
            }
        }

        //开始动画
        private void startAnimation() {
            if (valueAnimator != null && !valueAnimator.isRunning() && getVisibility() == View.VISIBLE) {
                valueAnimator.start();
            }
        }
    }
}