package com.rabbit.sunny;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
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

import androidx.annotation.Nullable;

import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.adloader.spread.SpLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.http.OnImageCallback;
import com.rabbit.adsdk.listener.OnAdSdkListener;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-10-16.
 */

public class RabActivity extends Activity implements IAdvance {

    private static final int MSG_ACTIVITY_DUP_CREATE = 0x10025;
    private static final int DELAY_ACTIVITY_DUP_CREATE = 10000;
    private SpreadCfg mSpreadCfg;
    private String mPidName;
    private String mSource;
    private String mAdType;
    private Handler mHandler = null;
    private ViewGroup mAdLayout;
    private ImageView mCloseView;
    private TextView mSponsoredView;
    private long mDelayClose;
    private boolean mCanClose = true;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (mSpreadCfg != null) {
            showSpread();
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
            mSpreadCfg = (SpreadCfg) intent.getSerializableExtra(Intent.EXTRA_STREAM);
            mDelayClose = intent.getLongExtra(Intent.ACTION_TIME_TICK, 0);
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
            boolean shown = AdSdk.get(this).showComplexAdsWithResult(mPidName, getAdParams(mAdType), mSource, mAdType, mAdLayout);
            if (shown) {
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
        if (mAdLayout != null) {
            OnAdSdkListener l = AdSdk.get(this).getOnAdSdkListener(mPidName);
            if (l != null) {
                l.onDismiss(mPidName, mSource, mAdType, true);
            }
        }
    }

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
            SpLoader.reportShow(this);
            SpLoader.reportImp(this);
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
            SpLoader.reportClick(context);
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
}