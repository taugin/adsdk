package com.hauyu.adsdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.rabbit.adsdk.AdSdk;
import com.umeng.analytics.MobclickAgent;
import com.umeng.cconfig.RemoteConfigSettings;
import com.umeng.cconfig.UMRemoteConfig;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {

    private void setNetworkProxy() {
        String PROXY_HOST = "172.16.170.218";//代理服务器地址
        String PROXY_PORT = "8888";//代理服务器端口
        System.setProperty("http.proxyHost", PROXY_HOST);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", PROXY_PORT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // setNetworkProxy();
        initUmeng();
        AdSdk.get(this).init();
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
