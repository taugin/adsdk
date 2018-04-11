package com.inner.adagg.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.AdExtra;
import com.inner.adaggs.AdParams;
import com.inner.adaggs.listener.SimpleAdAggsListener;
import com.inner.basic.BasicLib;

public class MainActivity extends Activity {

    private RelativeLayout mAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = findViewById(R.id.ad_container);
        BasicLib.init(this, "GTM-TMKR64Z");
        AdAggs.get(this).init("GTM-TMKR64Z");
        loadInterstitial();
        loadAdView();
        loadComplexAd();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.interstitial) {
            showInterstitial();
        } else if (v.getId() == R.id.banner_and_native) {
            showAdView();
        } else if (v.getId() == R.id.complex) {
            showComplexAd();
        }
    }

    private void loadInterstitial() {
        AdAggs.get(this).loadInterstitial("open_splash", new SimpleAdAggsListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            }
        });
    }

    private void showInterstitial() {
        if (AdAggs.get(this).isInterstitialLoaded("open_splash")) {
            AdAggs.get(MainActivity.this).showInterstitial("open_splash");
        } else {
            loadInterstitial();
        }
    }

    private void loadAdView() {
        AdParams adParams = new AdParams.Builder()
                .setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADMOB_MEDIUM_RECTANGLE)
                .setNativeCardStyle(AdExtra.NATIVE_CARD_SMALL)
                .build();
        AdAggs.get(this).loadAdView("main_top", adParams, new SimpleAdAggsListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            }
        });
    }

    private void showAdView() {
        if (AdAggs.get(this).isAdViewLoaded("main_top")) {
            AdAggs.get(this).showAdView("main_top", mAdContainer);
        } else {
            loadAdView();
        }
    }

    private void loadComplexAd() {
        AdAggs.get(this).loadComplexAds("ad_outer_place", new SimpleAdAggsListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            }
        });
    }

    private void showComplexAd() {
        if (AdAggs.get(this).isComplexAdsLoaded("ad_outer_place")) {
            AdAggs.get(this).showComplexAds("ad_outer_place", mAdContainer);
        } else {
            loadComplexAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
