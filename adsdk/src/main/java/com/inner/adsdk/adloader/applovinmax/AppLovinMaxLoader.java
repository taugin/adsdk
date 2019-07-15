package com.inner.adsdk.adloader.applovinmax;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.WindowManager;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2018/7/16.
 */

public class AppLovinMaxLoader extends AbstractSdkLoader {

    private static final String SDK_KEY = "qC49QxuXCicf3ao_ZHzdFCS5Rcx2SZOLq9ztmepKseAgFTKqOHV-uoO-5sOlwx2VEOerfq6OXuFPJmKVFpHIW2";

    private MaxInterstitialAd interstitialAd;

    static private MaxRewardedAd rewardedAd;

    private static AppLovinSdkSettings sAppLovinSdkSettings;

    private static AppLovinSdkConfiguration sAppLovinSdkConfiguration;

    @Override
    public void init(Context context) {
        super.init(context);
        try {
            getInstance().setMediationProvider("max");
        } catch (Exception e) {
        }
        AppLovinSdk.initializeSdk(context, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(AppLovinSdkConfiguration config) {
                Log.v(Log.TAG, "applovin max initialized");
                sAppLovinSdkConfiguration = config;
            }
        });
    }

    private AppLovinSdk getInstance() {
        if (sAppLovinSdkSettings == null) {
            sAppLovinSdkSettings = new AppLovinSdkSettings(mContext);
        }
        return AppLovinSdk.getInstance(SDK_KEY, sAppLovinSdkSettings, mContext);
    }

    private boolean isApplovinInited() {
        return sAppLovinSdkConfiguration != null;
    }

    public String getSdkName() {
        return Constant.AD_SDK_APPLOVIN_MAX;
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (interstitialAd != null) {
            loaded = interstitialAd.isReady() && !isCachedAdExpired(interstitialAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadInterstitial() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded(this);
            }
            return;
        }
        if (isLoading()) {
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (interstitialAd != null) {
                    clearCachedAdTime(interstitialAd);
                }
            }
        }
        Activity activity = null;
        try {
            activity = createFakeActivity((Application) mContext.getApplicationContext());
        } catch (Exception e) {
        }
        if (activity == null) {
            Log.v(Log.TAG, "apploin max interstitial need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
        setLoading(true, STATE_REQUEST);
        interstitialAd = new MaxInterstitialAd(getPid(), getInstance(), activity);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(interstitialAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(AppLovinMaxLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdShow();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(Log.TAG, "");
                interstitialAd = null;
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, int errorCode) {
                Log.v(Log.TAG, "applovin max interstitial display failed error : " + codeToError(errorCode));
            }
        });
        reportAdRequest();
        Log.v(Log.TAG, "");
        interstitialAd.loadAd();
    }

    @Override
    public boolean showInterstitial() {
        if (interstitialAd != null) {
            Log.v(Log.TAG, "");
            reportAdCallShow();
            interstitialAd.showAd();
            clearCachedAdTime(interstitialAd);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = super.isInterstitialLoaded();
        if (rewardedAd != null) {
            loaded = rewardedAd.isReady() && !isCachedAdExpired(rewardedAd);
        }
        if (loaded) {
            Log.d(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - loaded : " + loaded);
        }
        return loaded;
    }

    @Override
    public void loadRewardedVideo() {
        if (!checkPidConfig()) {
            Log.v(Log.TAG, "config error : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
            return;
        }
        if (isRewaredVideoLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onRewardedVideoAdLoaded(this);
            }
            return;
        }
        if (isLoading(true)) {
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (rewardedAd != null) {
                    clearCachedAdTime(rewardedAd);
                }
            }
        }
        Activity activity = null;
        try {
            activity = createFakeActivity((Application) mContext.getApplicationContext());
        } catch (Exception e) {
        }
        if (activity == null) {
            Log.v(Log.TAG, "apploin max reward video need an activity context");
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_CONTEXT);
            }
            return;
        }
        setLoading(true, STATE_REQUEST, true);
        rewardedAd = MaxRewardedAd.getInstance(getPid(), getInstance(), activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoStarted();
                }
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoCompleted();
                }
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                Log.v(Log.TAG, "");
                AdReward item = null;
                try {
                    String currencyName = reward.getLabel();
                    String amountGivenString = String.valueOf(reward.getAmount());
                    item = new AdReward();
                    item.setAmount(amountGivenString);
                    item.setType(currencyName);
                } catch (Exception e) {
                }
                reportAdReward();
                if (getAdListener() != null) {
                    getAdListener().onRewarded(item);
                }
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS, true);
                putCachedAdTime(rewardedAd);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(AppLovinMaxLoader.this);
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, int errorCode) {
                Log.v(Log.TAG, "reason : " + codeToError(errorCode) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE, true);
                reportAdError(codeToError(errorCode));
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdShow();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdShowed();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClosed();
                }
                rewardedAd = null;
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(Log.TAG, "");
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, int errorCode) {
                Log.v(Log.TAG, "applovin max reward video display failed error : " + codeToError(errorCode));
            }
        });

        Log.v(Log.TAG, "");
        reportAdRequest();
        rewardedAd.loadAd();
    }

    @Override
    public boolean showRewardedVideo() {
        if (rewardedAd != null && rewardedAd.isReady()) {
            Log.v(Log.TAG, "");
            reportAdCallShow();
            rewardedAd.showAd();
            clearCachedAdTime(rewardedAd);
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    private String codeToError(int code) {
        if (code == AppLovinErrorCodes.NO_FILL) {
            return "NO_FILL[" + code + "]";
        }
        if (code == AppLovinErrorCodes.NO_NETWORK) {
            return "NO_NETWORK[" + code + "]";
        }
        return "UNKNOWN[" + code + "]";
    }

    public static Activity createFakeActivity(Application application) {
        Activity activity = new Activity();
        try {
            Class ContextWrapperClass = Class.forName("android.content.ContextWrapper");
            Field mBase = ContextWrapperClass.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(activity, application.getBaseContext());

            Class ActivityClass = Class.forName("android.app.Activity");
            Field mApplication = ActivityClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            mApplication.set(activity, application);

            WindowManager wm = (WindowManager)application.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
            Field mWindowManager = ActivityClass.getDeclaredField("mWindowManager");
            mWindowManager.setAccessible(true);
            mWindowManager.set(activity, wm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }
}
