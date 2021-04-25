package com.rabbit.sunny;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.rabbit.adsdk.log.Log;

/**
 * Created by Administrator on 2018-10-16.
 */

public class RabActivity extends Activity {
    private VUIHelper mVUIHelper = new VUIHelper(this);

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVUIHelper.onCreate();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            super.setRequestedOrientation(requestedOrientation);
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVUIHelper.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVUIHelper.onDestroy();
    }
}