package com.appub.ads.a;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.StatImpl;

/**
 * Created by Administrator on 2018-10-16.
 */

public class FSA extends Activity {

    private GestureDetector mGestureDetector;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                fa();
                StatImpl.get().reportFinishFSA(getBaseContext(), "close_fsa_byuser", "touch");
                return super.onDown(e);
            }
        });
        register();
        show();
    }

    private void show() {
        String pidName = null;
        Intent intent = getIntent();
        if (intent != null) {
            pidName = intent.getStringExtra(Intent.EXTRA_TITLE);
        }
        if (!TextUtils.isEmpty(pidName)) {
            AdSdk.get(this).showComplexAds(pidName, null);
            StatImpl.get().reportAdOuterShow(this);
        } else {
            fa();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        StatImpl.get().reportFinishFSA(this, "close_fsa_byuser", "backpressed");
    }

    private void fa() {
        Log.v(Log.TAG, "");
        try {
            finish();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
    }

    private void register() {
        IntentFilter filter = new IntentFilter(getPackageName() + "action.FA");
        try {
            registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void unregister() {
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fa();
        }
    };
}
