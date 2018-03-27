package com.inner.adagg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.AdExtra;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.listener.SimpleAdAggsListener;
import com.inner.adaggs.log.Log;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private RelativeLayout mAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = findViewById(R.id.ad_container);
        AdAggs.get(this).init(true);

        if (true) {
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

            Map<String, Object> map = new HashMap<String, Object>();
            // map.put(AdExtra.KEY_FB_ROOTVIEW, view);
            map.put(AdExtra.KEY_FB_TEMPLATE, 1);
            map.put(AdExtra.KEY_FB_BANNER_SIZE, AdExtra.FB_BANNER);
            map.put(AdExtra.KEY_ADX_BANNER_SIZE, AdExtra.ADMOB_MEDIUM_RECTANGLE);
            AdAggs.get(this).loadAdView("main_top", map, new SimpleAdAggsListener() {
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

        //AdAggs.get(this).loadComplexAds("app_out", mOnAdAggsListener);
    }

    public void onClick(View v) {
        if (true) {
            if (AdAggs.get(this).isInterstitialLoaded("open_social")) {
                AdAggs.get(MainActivity.this).showInterstitial("open_social");
            } else {
                AdAggs.get(this).loadInterstitial("open_social");
            }
        } else {
            if (AdAggs.get(this).isComplexAdsLoaded("app_out")) {
                AdAggs.get(this).showComplexAds("app_out", mAdContainer);
            } else {
                AdAggs.get(this).loadComplexAds("app_out", mOnAdAggsListener);
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
}
