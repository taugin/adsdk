package com.inner.adagg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.listener.OnInterstitialListener;
import com.inner.adaggs.log.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdAggs.get(this).init();
        // MobileAds.initialize(this, "ca-app-pub-5425240585918224~3121229856");
    }

    public void onClick(View v) {
        AdAggs.get(this).loadInterstitial("open_splash", new OnInterstitialListener() {
            @Override
            public void onInterstitialLoaded() {
                Log.d(Log.TAG, "");
                AdAggs.get(MainActivity.this).showInterstitial("open_splash");
            }

            @Override
            public void onInterstitialShow() {
                Log.d(Log.TAG, "");
            }

            @Override
            public void onInterstitialDismiss() {
                Log.d(Log.TAG, "");
            }

            @Override
            public void onInterstitialError() {
                Log.d(Log.TAG, "");
            }
        });
    }
}
