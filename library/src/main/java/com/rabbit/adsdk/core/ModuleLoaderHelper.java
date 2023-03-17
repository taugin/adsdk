package com.rabbit.adsdk.core;

import android.text.TextUtils;

import com.applovin.sdk.AppLovinSdk;
import com.facebook.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.inmobi.ads.InMobiNative;
import com.mbridge.msdk.MBridgeSDK;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.log.Log;
import com.tradplus.ads.base.TradPlus;

/**
 * Created by Administrator on 2018-10-25.
 */

public class ModuleLoaderHelper {

    public static boolean isModuleLoaded(String sdk) {

        if (TextUtils.equals(Constant.AD_SDK_ADMOB, sdk)) {
            return hasAdmobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk)) {
            return hasFBModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk)) {
            return hasApplovinModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_MINTEGRAL, sdk)) {
            return hasMintegralModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_INMOBI, sdk)) {
            return hasInmobiModule();
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

    private static boolean hasFBModule() {
        try {
            InterstitialAd.class.getName();
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

    private static boolean hasMintegralModule() {
        try {
            MBridgeSDK.class.getName();
            return true;
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return false;
    }

    private static boolean hasInmobiModule() {
        try {
            InMobiNative.class.getName();
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
