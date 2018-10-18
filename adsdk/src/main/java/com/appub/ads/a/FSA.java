package com.appub.ads.a;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.StatImpl;


/**
 * Created by Administrator on 2018-10-16.
 */

public class FSA extends Activity {

    private GestureDetector mGestureDetector;
    private String mPidName;
    private String mSource;
    private String mAdType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        init();
        show();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mPidName = intent.getStringExtra(Intent.EXTRA_TITLE);
            mSource = intent.getStringExtra(Intent.EXTRA_TEXT);
            mAdType = intent.getStringExtra(Intent.EXTRA_TEMPLATE);
        }
    }

    private void init() {
        if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            register();
            initGesture();
        }
    }

    private void show() {
        if (Constant.TYPE_NATIVE.equalsIgnoreCase(mAdType)
                || Constant.TYPE_BANNER.equalsIgnoreCase(mAdType)) {
            showNAd();
        } else if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            showGAd();
        } else {
            fa();
        }
    }

    private void showNAd() {
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);
        setContentView(rootLayout);
        RelativeLayout adLayout = new RelativeLayout(this);
        adLayout.setGravity(Gravity.CENTER);
        rootLayout.addView(adLayout, -1, -1);

        ImageView imageView = generateCloseView();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        int margin = dp2px(this, 10);
        params.setMargins(margin, margin, 0, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fa();
            }
        });
        rootLayout.addView(imageView, params);

        AdSdk.get(this).showComplexAds(mPidName, mSource, mAdType, adLayout);
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

    private void initGesture() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                fa();
                StatImpl.get().reportFinishFSA(getBaseContext(), "close_fsa_byuser", "touch");
                return super.onDown(e);
            }
        });
    }

    private void showGAd() {
        if (!TextUtils.isEmpty(mPidName)) {
            AdSdk.get(this).showComplexAds(mPidName, mSource, mAdType, null);
            StatImpl.get().reportAdOuterShow(this);
        } else {
            fa();
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
        super.onBackPressed();
        if (Constant.TYPE_INTERSTITIAL.equalsIgnoreCase(mAdType)
                || Constant.TYPE_REWARD.equalsIgnoreCase(mAdType)) {
            StatImpl.get().reportFinishFSA(this, "close_fsa_byuser", "backpressed");
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
    }

    private void register() {
        IntentFilter filter = new IntentFilter(getPackageName() + "action.FA");
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
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fa();
        }
    };
}
