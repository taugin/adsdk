package com.appub.ads.a;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.SpConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.policy.GtPolicy;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInLockView = false;
        parseIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        mHandler = new Handler();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } catch (Exception e) {
        } catch (Error e) {
        }
        updateDataAndView();
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

    @Override
    public void setContentView(View view) {
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    public void setContentView(int layoutResID) {
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        Log.e(Log.TAG, "ignore function setContentView");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mInLockView = false;
        parseIntent();
        updateDataAndView();
    }

    private void updateDataAndView() {
        if (isLockView()) {
            showLockScreenView();
        } else if (mSpConfig != null) {
            showSpread();
        } else {
            registerArgument();
            show();
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

        ImageView imageView = generateCloseView();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        int margin = dp2px(this, 10);
        params.setMargins(margin, margin, 0, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityWithDelay();
            }
        });
        rootLayout.addView(imageView, params);

        AdSdk.get(this).showComplexAds(mPidName, getAdParams(), mSource, mAdType, adLayout);
        GtPolicy.get(this).reportGtShowing(true);
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private ImageView generateCloseView() {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        imageView.setBackgroundResource(android.R.drawable.list_selector_background);
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
            AdSdk.get(this).showComplexAds(mPidName, mSource, mAdType, null);
            StatImpl.get().reportAdOuterShow(this);
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
        super.onBackPressed();
        if ((Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType))
                && !Constant.AD_SDK_SPREAD.equals(mSource)) {
            StatImpl.get().reportFinishFSA(this, "close_fsa_byuser", "backpressed");
        }
    }

    private void finishActivityWithDelay() {
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
            RelativeLayout rootLayout = new RelativeLayout(this);
            rootLayout.setBackgroundColor(Color.WHITE);
            super.setContentView(rootLayout);
            RelativeLayout adLayout = new RelativeLayout(this);
            adLayout.setGravity(Gravity.CENTER);
            rootLayout.addView(adLayout, -1, -1);

            ImageView imageView = generateCloseView();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
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

            String nativeData = NATIVE_TEMPLATE.replace("#COVER_URL#", mSpConfig.getBanner());
            nativeData = nativeData.replace("#ICON_URL#", mSpConfig.getIcon());
            nativeData = nativeData.replace("#TITLE#", mSpConfig.getTitle());
            nativeData = nativeData.replace("#DESC#", mSpConfig.getDetail());
            WebView webview = new WebView(this);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webview.loadData(nativeData, "text/html", "utf-8");

            RelativeLayout buttonLayout = new RelativeLayout(this);
            buttonLayout.setId(generateViewId(0x1000001));
            params = new RelativeLayout.LayoutParams(-1, dp2px(this, 84));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adLayout.addView(buttonLayout, params);

            Button button = new Button(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setElevation(dp2px(this, 6));
                button.setTranslationZ(0);
            }
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor("#CC7122e5")));
            drawable.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(Color.parseColor("#7122e5")));
            button.setId(generateViewId(0x1000002));
            button.setBackground(drawable);
            button.setPadding(0, 0, 0, 0);
            button.setGravity(Gravity.CENTER);
            button.getPaint().setFakeBoldText(true);
            button.setTextColor(Color.WHITE);
            button.setTypeface(button.getTypeface(), Typeface.BOLD);
            button.setSingleLine();
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            button.setText(mSpConfig.getCta());
            params = new RelativeLayout.LayoutParams(-1, -1);
            margin = dp2px(this, 16);
            params.setMargins(margin, margin, margin, margin);
            buttonLayout.addView(button, params);

            params = new RelativeLayout.LayoutParams(-1, -1);
            params.addRule(RelativeLayout.ABOVE, buttonLayout.getId());
            adLayout.addView(webview, params);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = mSpConfig.getLinkUrl();
                    if (TextUtils.isEmpty(url)) {
                        url = "market://details?id=" + mSpConfig.getPkgname();
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.v(Log.TAG, "error : " + e);
                    }
                    StatImpl.get().reportAdClick(getBaseContext(), mPidName, mSource, mAdType, null);
                }
            });
            StatImpl.get().reportAdShow(this, mPidName, mSource, mAdType, null);
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
            finish();
        }
    }

    private int generateViewId(int defaultId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return defaultId;
    }

    private boolean checkArgs() {
        if (mSpConfig == null
                || TextUtils.isEmpty(mSpConfig.getBanner())
                || TextUtils.isEmpty(mSpConfig.getIcon())
                || TextUtils.isEmpty(mSpConfig.getTitle())
                || TextUtils.isEmpty(mSpConfig.getPkgname())
                || TextUtils.isEmpty(mSpConfig.getDetail())
                || TextUtils.isEmpty(mSpConfig.getCta())) {
            return false;
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

    private void showLockScreenView() {
        Log.v(Log.TAG, "show lock screen view");

        // 1，create Activity layout
        LinearLayout layout = new LinearLayout(this);
        super.setContentView(layout);

        layout.setOrientation(LinearLayout.VERTICAL);

        // 2，create ViewPager
        ViewPager viewPager = new ViewPager(this);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    fa();
                }
            }
        });

        // 3，create Ad Pager layout
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

        // 3.1，create TimeView
        mTimeTextView = new TextView(this);
        mTimeTextView.setTextColor(Color.WHITE);
        mTimeTextView.setGravity(Gravity.CENTER);
        mTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
        mTimeTextView.setPadding(0, Utils.dp2px(this, 24f), 0, Utils.dp2px(this, 6f));
        LinearLayout.LayoutParams timeViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeViewParams.weight = 0;
        timeViewParams.topMargin = Utils.dp2px(this, 24f);
        pagerLayout.addView(mTimeTextView, timeViewParams);

        // 3.2，create WeekView
        mWeekTextView = new TextView(this);
        mWeekTextView.setTextColor(Color.WHITE);
        mWeekTextView.setGravity(Gravity.CENTER);
        mWeekTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mWeekTextView.setPadding(0, Utils.dp2px(this, 0f), 0, Utils.dp2px(this, 30f));
        LinearLayout.LayoutParams weekViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        weekViewParams.weight = 0;
        pagerLayout.addView(mWeekTextView, weekViewParams);

        // 3.3，create Ad Layout
        RelativeLayout adLayout = new RelativeLayout(this);
        adLayout.setGravity(Gravity.CENTER);
        adLayout.setPadding(Utils.dp2px(this, 8f), 0, Utils.dp2px(this, 8f), 0);
        mLockAdLayout = adLayout;
        LinearLayout.LayoutParams adLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        adLayoutParams.weight = 1;
        pagerLayout.addView(mLockAdLayout, adLayoutParams);

        // 3.4，create Scroll View
        TextView slideView = new MyTextView(this);
        slideView.setText("Slide To Unlock >>");
        slideView.setTextColor(Color.WHITE);
        slideView.setGravity(Gravity.CENTER);
        slideView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        LinearLayout.LayoutParams slideViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slideViewParams.weight = 0;
        slideViewParams.bottomMargin = Utils.dp2px(this, 16f);
        slideViewParams.topMargin = Utils.dp2px(this, 16f);
        pagerLayout.addView(slideView, slideViewParams);

        // 4，set ViewPager Adapter
        LsViewPagerAdapter adapter = new LsViewPagerAdapter(pagerLayout);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

        // 5，add ViewPager to Activity layout
        layout.addView(viewPager, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

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
            e.printStackTrace();
        }
    }

    private void showLockViewAd() {
        AdParams params = new AdParams.Builder()
                .setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE)
                .setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE)
                .setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_MEDIUM)
                .build();
        AdSdk.get(this).loadAdView(Constant.LTPLACE_OUTER_NAME, params, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                AdSdk.get(getBaseContext()).showAdView(pidName, getAdParams(), mLockAdLayout);
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
}