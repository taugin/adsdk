package com.hauyu.adsdk.demo;

import android.os.Bundle;
import android.view.Window;

import com.hauyu.adsdk.demo.view.ScanningAnimatorView;

public class CustomViewActivity extends BaseActivity {

    private ScanningAnimatorView scanningAnimatorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_view_layout);
        scanningAnimatorView = findViewById(R.id.scanningView);
        scanningAnimatorView.post(new Runnable() {
            @Override
            public void run() {
                scanningAnimatorView.startAnim();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanningAnimatorView.stopAnim();
    }
}
