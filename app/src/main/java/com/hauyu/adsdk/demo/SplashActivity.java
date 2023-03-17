package com.hauyu.adsdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tiger.adsdk.AdExtra;
import com.tiger.adsdk.AdParams;
import com.tiger.adsdk.AdSdk;
import com.tiger.adsdk.listener.SimpleAdSdkListener;

import java.util.Locale;
import java.util.Random;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "MA";
    private ViewGroup adContainer;
    private ViewGroup splashLayout;
    private View adContainerLayout;
    private Handler mHandler = new Handler();
    private View appInfoLayout = null;
    private String mNativeSplashPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sdkArray[] = getResources().getStringArray(R.array.ad_sdk);
        String sdk = sdkArray[new Random().nextInt(sdkArray.length)];
        sdk = "spread";
        mNativeSplashPlace = String.format(Locale.ENGLISH, "native_%s", sdk.toLowerCase());
        setContentView(R.layout.act_splash);
        adContainer = findViewById(R.id.ad_container);
        splashLayout = findViewById(R.id.splash_layout);
        appInfoLayout = findViewById(R.id.app_info_layout);
        adContainerLayout = findViewById(R.id.ad_container_layout);
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
        AdParams adParams = new AdParams.Builder().setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_ROUND).build();
        AdSdk.get(this).loadAdView(mNativeSplashPlace, adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String placeName, String source, String adType, String pid) {
                if (!isFinishing()) {
                    removeState();
                    showNativeSplash();
                }
            }

            @Override
            public void onLoadFailed(String placeName, String source, String adType, String pid, int error) {
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
            adContainerLayout.setVisibility(View.VISIBLE);
            splashLayout.setVisibility(View.GONE);
            AdSdk.get(this).showAdView(mNativeSplashPlace, adContainer);
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
