package com.inner.adagg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.AdExtra;

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

        AdAggs.get(this).loadInterstitial("open_splash");

        View view = getLayoutInflater().inflate(R.layout.fb_native, null);
        mAdContainer.addView(view);

        Map<String, Object> map = new HashMap<String, Object>();
        // map.put(AdExtra.KEY_FB_ROOTVIEW, view);
        map.put(AdExtra.KEY_FB_TEMPLATE, 1);
        AdAggs.get(this).loadAdView("main_top", map);
    }

    public void onClick(View v) {
        if (AdAggs.get(this).isInterstitialLoaded("open_splash")) {
            AdAggs.get(MainActivity.this).showInterstitial("open_splash");
        }
        if (AdAggs.get(this).isAdViewLoaded("main_top")) {
            AdAggs.get(this).showAdView("main_top", mAdContainer);
        }
    }
}
