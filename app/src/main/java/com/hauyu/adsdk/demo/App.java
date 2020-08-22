package com.hauyu.adsdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.hauyu.adsdk.AdSdk;
import com.verk.BcSdk;


/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BcSdk.init(this);
        AdSdk.get(this).init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
