package com.hauyu.adsdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.rabbit.adsdk.AdSdk;
import com.scene.crazy.SceneSdk;
import com.umeng.analytics.MobclickAgent;
import com.umeng.cconfig.RemoteConfigSettings;
import com.umeng.cconfig.UMRemoteConfig;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initUmeng();
        AdSdk.get(this).init();
        SceneSdk.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    private void initUmeng() {
        UMConfigure.init(this, "5f44faa1f9d1496ef418b17c", "umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        UMRemoteConfig.getInstance().setConfigSettings(new RemoteConfigSettings.Builder().setAutoUpdateModeEnabled(true).build());
        UMRemoteConfig.getInstance().init(this);
    }
}
