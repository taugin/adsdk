package com.inner.adagg.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.AdExtra;
import com.inner.adaggs.AdParams;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.listener.SimpleAdAggsListener;

public class MainActivity extends Activity {

    private RelativeLayout mAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = findViewById(R.id.ad_container);
        AdAggs.get(this).init(true);

        if (false) {
            AdAggs.get(this).loadAdView("open_splash", new SimpleAdAggsListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    AdAggs.get(MainActivity.this).showAdView(pidName, mAdContainer);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }

                @Override
                public void onClick(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }
            });
        } else {
            // View view = getLayoutInflater().inflate(R.layout.fb_native, null);
            // mAdContainer.addView(view);

            AdParams adParams = new AdParams.Builder()
                    .setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADMOB_MEDIUM_RECTANGLE)
                    .setNativeTemplateId(AdExtra.NATIVE_TEMPLATE_SMALL)
                    .build();
            AdAggs.get(this).loadAdView("main_top", adParams, new SimpleAdAggsListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    AdAggs.get(MainActivity.this).showAdView(pidName, mAdContainer);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }

                @Override
                public void onClick(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }
            });
        }

        //AdAggs.get(this).loadComplexAds("ad_outer_place", mOnAdAggsListener);
    }

    public void onClick(View v) {
        if (true) {
            if (AdAggs.get(this).isInterstitialLoaded("open_social")) {
                AdAggs.get(MainActivity.this).showInterstitial("open_social");
            } else {
                AdAggs.get(this).loadInterstitial("open_social");
            }
        } else {
            if (AdAggs.get(this).isComplexAdsLoaded("ad_outer_place")) {
                AdAggs.get(this).showComplexAds("ad_outer_place", mAdContainer);
            } else {
                AdAggs.get(this).loadComplexAds("ad_outer_place", mOnAdAggsListener);
            }
        }
    }

    private OnAdAggsListener mOnAdAggsListener = new OnAdAggsListener() {
        @Override
        public void onLoaded(String pidName, String source, String adType) {
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onShow(String pidName, String source, String adType) {
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onClick(String pidName, String source, String adType) {
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onError(String pidName, String source, String adType) {
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
