package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;

public class SplashActivity extends Activity {
    private static final String TAG = "MA";
    private FrameLayout adContainer;
    private ViewGroup splashLayout;
    private Handler mHandler = new Handler();
    private View appInfoLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_splash);
        adContainer = findViewById(R.id.ad_container);
        splashLayout = findViewById(R.id.splash_layout);
        appInfoLayout = findViewById(R.id.app_info_layout);
        appInfoLayout.setVisibility(View.GONE);
        appInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    Log.d(TAG, "enter app from click");
                    startApp();
                }
            }
        });
        initAds(false);
    }

    public void initAds(boolean fromPrivacy) {
        setTimeOutState(fromPrivacy ? 10000 : 8000);
        loadNativeSplashRetry(1, true);
    }

    private void loadNativeSplashRetry(final int retryTimes, final boolean enterApp) {
        Log.v(TAG, "load splash time : " + retryTimes);
        if (retryTimes <= 0 || isFinishing()) {
            Log.v(TAG, "no need retry");
            return;
        }
        AdParams adParams = new AdParams.Builder().setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_WRAP).build();
        AdSdk.get(this).loadAdView("native_splash", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String placeName, String source, String adType, String pid) {
                if (!isFinishing()) {
                    removeState();
                    showNativeSplash();
                }
            }

            @Override
            public void onError(String placeName, String source, String adType, String pid, int error) {
                if (AdSdk.get(getApplicationContext()).isAdPlaceError(placeName)) {
                    Log.v(TAG, "splash error " + retryTimes);
                    if (retryTimes > 1) {
                        loadNativeSplashRetryDelay(retryTimes - 1, enterApp);
                    } else if (enterApp) {
                        removeState();
                        if (!isFinishing()) {
                            Log.d(TAG, "splash error to open app");
                            startApp();
                        }
                    }
                }
            }
        });
    }

    private void loadNativeSplashRetryDelay(final int retryTimes, final boolean enterApp) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadNativeSplashRetry(retryTimes, enterApp);
            }
        }, 1000);
    }

    private void showNativeSplash() {
        Log.v(TAG, "show splash");
        if (adContainer != null) {
            appInfoLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            splashLayout.setVisibility(View.GONE);
            AdSdk.get(this).showAdView("native_splash", adContainer);
        }
    }

    /**
     * 设置三秒超时
     */
    private void setTimeOutState(int delay) {
        Log.d(TAG, "set time out state");
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeState();
                    if (!isFinishing()) {
                        Log.d(TAG, "time out start app");
                        startApp();
                    }
                }
            }, delay);
        }
    }

    private void removeState() {
        Log.d(TAG, "remote state");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}
