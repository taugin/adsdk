package com.hauyu.adsdk.core;

import android.text.TextUtils;

import com.facebook.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.hauyu.adsdk.constant.Constant;
import com.mopub.mobileads.MoPubInterstitial;

/**
 * Created by Administrator on 2018-10-25.
 */

public class AdHelper {

    public static boolean isModuleLoaded(String sdk) {

        if (TextUtils.equals(Constant.AD_SDK_DFP, sdk)) {
            return hasDfpModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_ADMOB, sdk)) {
            return hasAdmobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk)) {
            return hasFBModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_MOPUB, sdk)) {
            return hasMopubModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_SPREAD, sdk)) {
            return true;
        }
        return false;
    }

    private static boolean hasDfpModule() {
        try {
            MobileAds.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasAdmobModule() {
        try {
            MobileAds.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasFBModule() {
        try {
            InterstitialAd.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasMopubModule() {
        try {
            MoPubInterstitial.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }
}
