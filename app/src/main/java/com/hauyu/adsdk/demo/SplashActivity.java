package com.hauyu.adsdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
        startApp();
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
