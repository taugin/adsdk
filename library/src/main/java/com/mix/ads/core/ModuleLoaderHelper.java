package com.mix.ads.core;

import android.text.TextUtils;

import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.mix.ads.adloader.admob.AdmobLoader;
import com.mix.ads.adloader.applovin.AppLovinLoader;
import com.mix.ads.adloader.bigo.BigoLoader;
import com.mix.ads.adloader.listener.ISdkLoader;
import com.mix.ads.adloader.spread.SpLoader;
import com.mix.ads.constant.Constant;
import com.mix.ads.data.config.PidConfig;
import com.mix.ads.log.Log;

import java.util.HashMap;
import java.util.Map;

import sg.bigo.ads.BigoAdSdk;

/**
 * Created by Administrator on 2018-10-25.
 */

public class ModuleLoaderHelper {

    private static Map<String, Class<?>> sSdkLoaderMap = new HashMap<>();

    static {
        sSdkLoaderMap.put(Constant.AD_SDK_ADMOB, AdmobLoader.class);
        sSdkLoaderMap.put(Constant.AD_SDK_APPLOVIN, AppLovinLoader.class);
        sSdkLoaderMap.put(Constant.AD_SDK_SPREAD, SpLoader.class);
        sSdkLoaderMap.put(Constant.AD_NETWORK_BIGO, BigoLoader.class);
    }

    public static boolean isModuleLoaded(String sdk) {

        if (TextUtils.equals(Constant.AD_SDK_ADMOB, sdk)) {
            return hasAdmobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk)) {
            return hasApplovinModule();
        }
        if (TextUtils.equals(Constant.AD_NETWORK_BIGO, sdk)) {
            return hasBigoModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_SPREAD, sdk)) {
            return true;
        }
        return false;
    }

    public static ISdkLoader generateSdkLoader(PidConfig config) {
        if (config == null || TextUtils.isEmpty(config.getSdk()) || config.isDisable()) {
            return null;
        }
        String sdkName = config.getSdk();
        if (!isModuleLoaded(sdkName)) {
            return null;
        }
        Class<?> clazz = sSdkLoaderMap.get(sdkName);
        if (clazz == null) {
            return null;
        }
        try {
            return (ISdkLoader) clazz.newInstance();
        } catch (Exception | Error e) {
        }
        return null;
    }

    private static boolean hasAdmobModule() {
        try {
            MobileAds.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }

    private static boolean hasApplovinModule() {
        try {
            AppLovinSdk.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }

    private static boolean hasBigoModule() {
        try {
            BigoAdSdk.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }
}
