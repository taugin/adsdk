package com.inner.adagg.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.basic.BasicLib;

public class MainActivity extends Activity {

    private RelativeLayout mAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = findViewById(R.id.ad_container);
        BasicLib.init(this, "GTM-TMKR64Z1");
        AdSdk.get(this).init("GTM-TMKR64Z1");
        // loadInterstitial();
        loadAdView();
        // loadComplexAd();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.interstitial) {
            showInterstitial();
        } else if (v.getId() == R.id.banner_and_native) {
            showAdView();
        } else if (v.getId() == R.id.complex) {
            // showComplexAd();
        }
    }

    private void loadInterstitial() {
        AdSdk.get(this).loadInterstitial("Open_app", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            }
        });
    }

    private void showInterstitial() {
        if (AdSdk.get(this).isInterstitialLoaded("Open_app")) {
            AdSdk.get(MainActivity.this).showInterstitial("Open_app");
        } else {
            loadInterstitial();
        }
    }

    private void loadAdView() {
        View view = LayoutInflater.from(this).inflate(R.layout.adx_native_small, null);
        AdParams adParams = new AdParams.Builder()
                // 设置banner 参数
                .setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADMOB_LARGE_BANNER)
                .setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE)
                // 设置adx native参数
                .setAdRootView(AdExtra.AD_SDK_ADX, view)
                .setAdTitle(AdExtra.AD_SDK_ADX, R.id.adx_title)
                .setAdDetail(AdExtra.AD_SDK_ADX, R.id.adx_detail)
                .setAdIcon(AdExtra.AD_SDK_ADX, R.id.adx_icon)
                .setAdAction(AdExtra.AD_SDK_ADX, R.id.adx_action)
                .setAdCover(AdExtra.AD_SDK_ADX, R.id.adx_cover)
                .setAdMediaView(AdExtra.AD_SDK_ADX, R.id.adx_mediaview)
                // 设置fb native参数
                .setAdCardStyle(AdExtra.AD_SDK_FACEBOOK, AdExtra.NATIVE_CARD_SMALL)
                .build();

        AdSdk.get(this).loadAdView("Rest_top", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                AdSdk.get(MainActivity.this).showAdView(pidName, mAdContainer);
            }
        });
    }

    private void showAdView() {
        if (AdSdk.get(this).isAdViewLoaded("Rest_top")) {
            AdSdk.get(this).showAdView("Rest_top", mAdContainer);
        } else {
            loadAdView();
        }
    }

    private void loadComplexAd() {
        AdSdk.get(this).loadComplexAds("ad_outer_place", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            }
        });
    }

    private void showComplexAd() {
        if (AdSdk.get(this).isComplexAdsLoaded("ad_outer_place")) {
            AdSdk.get(this).showComplexAds("ad_outer_place", mAdContainer);
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
