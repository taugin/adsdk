package com.inner.adagg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.AdExtra;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.listener.SimpleAdAggsListener;
import com.inner.adaggs.log.Log;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdContainer = findViewById(R.id.ad_container);
        AdAggs.get(this).init();

        if (true) {
            AdAggs.get(this).loadInterstitial("open_social", new SimpleAdAggsListener() {
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
            });
        } else {
            // View view = getLayoutInflater().inflate(R.layout.fb_native, null);
            // mAdContainer.addView(view);

            Map<String, Object> map = new HashMap<String, Object>();
            // map.put(AdExtra.KEY_FB_ROOTVIEW, view);
            map.put(AdExtra.KEY_FB_TEMPLATE, 1);
            AdAggs.get(this).loadAdView("main_top", map, new SimpleAdAggsListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    AdAggs.get(MainActivity.this).showAdView("main_top", mAdContainer);
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

        //AdAggs.get(this).loadMixedAds("app_out", mOnAdAggsListener);
    }

    public void onClick(View v) {
        if (true) {
            if (AdAggs.get(this).isInterstitialLoaded("open_social")) {
                AdAggs.get(MainActivity.this).showInterstitial("open_social");
            } else {
                AdAggs.get(this).loadInterstitial("open_social");
            }
        } else {
            if (AdAggs.get(this).isMixedAdsLoaded("app_out")) {
                AdAggs.get(this).showMixedAds("app_out", mAdContainer);
            } else {
                AdAggs.get(this).loadMixedAds("app_out", mOnAdAggsListener);
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
