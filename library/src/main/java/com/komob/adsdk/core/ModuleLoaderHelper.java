package com.komob.adsdk.core;

import android.text.TextUtils;

import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.komob.adsdk.adloader.admob.AdmobLoader;
import com.komob.adsdk.adloader.applovin.AppLovinLoader;
import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.adloader.spread.SpLoader;
import com.komob.adsdk.adloader.tradplus.TradPlusLoader;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.log.Log;
import com.tradplus.ads.base.TradPlus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-10-25.
 */

public class ModuleLoaderHelper {

    private static Map<String, Class<?>> sSdkLoaderMap = new HashMap<>();

    static {
        sSdkLoaderMap.put(Constant.AD_SDK_ADMOB, AdmobLoader.class);
        sSdkLoaderMap.put(Constant.AD_SDK_APPLOVIN, AppLovinLoader.class);
        sSdkLoaderMap.put(Constant.AD_SDK_TRADPLUS, TradPlusLoader.class);
        sSdkLoaderMap.put(Constant.AD_SDK_SPREAD, SpLoader.class);
    }

    public static boolean isModuleLoaded(String sdk) {

        if (TextUtils.equals(Constant.AD_SDK_ADMOB, sdk)) {
            return hasAdmobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk)) {
            return hasApplovinModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_TRADPLUS, sdk)) {
            return hasTradPlusModule();
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

    private static boolean hasTradPlusModule() {
        try {
            TradPlus.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }
}
