package com.hauyu.adsdk.core;

import android.text.TextUtils;

import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.tradplus.ads.base.TradPlus;

/**
 * Created by Administrator on 2018-10-25.
 */

public class ModuleLoaderHelper {

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

    private static boolean hasAdmobModule() {
        try {
            MobileAds.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return false;
    }

    private static boolean hasApplovinModule() {
        try {
            AppLovinSdk.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return false;
    }

    private static boolean hasTradPlusModule() {
        try {
            TradPlus.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return false;
    }
}
