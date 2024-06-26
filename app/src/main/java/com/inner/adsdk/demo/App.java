package com.inner.adsdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.inner.basic.BcSdk;

/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BcSdk.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
