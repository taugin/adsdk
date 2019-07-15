package com.inner.adsdk.framework;

import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adywind.core.api.SDK;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.appnext.base.Appnext;
import com.brandio.ads.Placement;
import com.cloudtech.ads.core.CTService;
import com.duapps.ad.base.DuAdNetwork;
import com.facebook.ads.InterstitialAd;
import com.fyber.inneractive.sdk.external.InneractiveAdManager;
import com.google.android.gms.ads.MobileAds;
import com.inmobi.ads.InMobiInterstitial;
import com.inner.adsdk.constant.Constant;
import com.mobi.sdk.ADSDK;
import com.mopub.mobileads.MoPubInterstitial;
import com.we.sdk.exchange.DspMob;
import com.wemob.ads.Sdk;


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
        if (TextUtils.equals(Constant.AD_SDK_ADX, sdk)) {
            return hasAdxModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_FACEBOOK, sdk)) {
            return hasFBModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_INMOBI, sdk)) {
            return hasInMobiModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN, sdk)) {
            return hasApplovinModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPNEXT, sdk)) {
            return hasAppnextModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_INNERACTIVE, sdk)) {
            return hasInnerActiveModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_MOPUB, sdk)) {
            return hasMopubModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_WEMOB, sdk)) {
            return hasWemobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_SPREAD, sdk)) {
            return true;
        }
        if (TextUtils.equals(Constant.AD_SDK_DSPMOB, sdk)) {
            return hasDspMobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_DAP, sdk)) {
            return hasDapMobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_ALTAMOB, sdk)) {
            return hasAltamobMobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_CLOUDMOBI, sdk)) {
            return hasCloudMobiModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_MOBVISTA, sdk)) {
            return hasMobvistaMobModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_DISPLAYIO, sdk)) {
            return hasDisplayIoModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_ADCOLONY, sdk)) {
            return hasAdColonyModule();
        }
        if (TextUtils.equals(Constant.AD_SDK_APPLOVIN_MAX, sdk)) {
            return hasApplovinMaxModule();
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

    private static boolean hasAdxModule() {
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

    private static boolean hasInMobiModule() {
        try {
            InMobiInterstitial.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasApplovinModule() {
        try {
            AppLovinSdk.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasAppnextModule() {
        try {
            Appnext.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasInnerActiveModule() {
        try {
            InneractiveAdManager.class.getName();
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

    private static boolean hasWemobModule() {
        try {
            Sdk.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasDspMobModule() {
        try {
            DspMob.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasDapMobModule() {
        try {
            DuAdNetwork.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasAltamobMobModule() {
        try {
            ADSDK.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasCloudMobiModule() {
        try {
            CTService.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasMobvistaMobModule() {
        try {
            SDK.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasDisplayIoModule() {
        try {
            Placement.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasAdColonyModule() {
        try {
            AdColony.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static boolean hasApplovinMaxModule() {
        try {
            MaxInterstitialAd.class.getName();
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }
}
