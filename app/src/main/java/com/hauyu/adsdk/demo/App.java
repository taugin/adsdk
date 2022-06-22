package com.hauyu.adsdk.demo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.rabbit.adsdk.AdImpData;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.listener.OnAdImpressionListener;
import com.rabbit.adsdk.stat.InternalStat;
import com.rabbit.adsdk.utils.Utils;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ChangeLanguage.init(this);
        Va.setNetworkProxy();
        initUmeng();
        initTalkingData();
        AdSdk.get(this).setOnAdImpressionListener(new OnAdImpressionListener() {
            @Override
            public void onAdImpression(AdImpData adImpData) {
                if (adImpData != null) {
                    Double revenue = adImpData.getValue();
                    if (revenue != null && revenue.doubleValue() > 0) {
                        reportTaichiEvent(getApplicationContext(), revenue.floatValue());
                    }
                }
                reportUmengEvent(adImpData);
            }
        });
        AdSdk.get(this).init();
    }

    private void reportTaichiEvent(Context context, float revenue) {
        String prefRevenue = "pref_total_taichi_revenue";
        String taichiEvent = "Total_Ads_Revenue_001";
        float lastTotalRevenue = Utils.getFloat(context, prefRevenue);
        float curTotalRevenue = lastTotalRevenue + revenue;
        Log.d(Log.TAG, "lastTotalRevenue : " + lastTotalRevenue + " , curTotalRevenue : " + curTotalRevenue + " , revenue : " + revenue);
        if (curTotalRevenue >= 0.01f) {
            Utils.putFloat(context, prefRevenue, 0f);
            Map<String, Object> map = new HashMap<>();
            map.put("currency", "USD");
            map.put("value", curTotalRevenue);
            InternalStat.reportEvent(context, taichiEvent, map);
        } else {
            Utils.putFloat(context, prefRevenue, curTotalRevenue);
        }
    }

    private void reportUmengEvent(AdImpData adImpData) {
        if (adImpData == null) {
            return;
        }
        String networkName = adImpData.getNetwork();
        String platform = adImpData.getPlatform();
        String unitName = platform + "_" + adImpData.getUnitName();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ad_platform", platform);
        params.put("ad_source", networkName);
        params.put("ad_format", adImpData.getFormat());
        params.put("ad_unit_name", unitName);
        params.put("value", adImpData.getValue());
        params.put("currency", "USD"); // All Applovin revenue is sent in USD
        InternalStat.sendUmengObject(getApplicationContext(), "ad_impression", params);
        Log.v(Log.TAG, "params : " + params);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    private void initUmeng() {
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "5f44faa1f9d1496ef418b17c", "umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    private void initTalkingData() {
        String appId = "72EC6DEE7A914070B029C48AAAA7CAD9";
        String channel = getChannel(this);
        TCAgent.init(this, appId, channel);
        TCAgent.setReportUncaughtExceptions(true);
    }

    private static String getChannel(Context context) {
        String channel = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            channel = locale.getCountry().toLowerCase(Locale.getDefault());
        } catch (Exception e) {
            channel = Utils.getMetaData(context, "UMENG_CHANNEL");
        }
        return channel;
    }

}
