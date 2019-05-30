package com.inner.adsdk.adloader.adcolony;

import android.app.Application;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

public class AdColonyLoader extends AbstractSdkLoader {

    private AdColonyInterstitial mAdColonyInterstitial;
    private static List<String> sZoneIdList = new ArrayList<String>();

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_ADCOLONY;
    }

    @Override
    public void setAdId(String adId) {
        super.setAdId(adId);
        AdColonyAppOptions appOptions = new AdColonyAppOptions()
                .setKeepScreenOn(true);
        Application app = null;
        try {
            app = (Application) mContext.getApplicationContext();
        } catch (Exception e) {
        }
        String[] zoneIdArray = null;
        if (sZoneIdList != null) {
            if (!sZoneIdList.contains(getPid())) {
                sZoneIdList.add(getPid());
            }
            zoneIdArray = sZoneIdList.toArray(new String[sZoneIdList.size()]);
        }
        AdColony.configure(app, appOptions, getAppId(), zoneIdArray);
    }

    @Override
    public boolean isInterstitialLoaded() {
        boolean loaded = mAdColonyInterstitial != null && !mAdColonyInterstitial.isExpired();
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
                if (mAdColonyInterstitial != null) {
                    mAdColonyInterstitial.setListener(null);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                mAdColonyInterstitial = ad;
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onInterstitialLoaded(AdColonyLoader.this);
                }
            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                Log.v(Log.TAG, "reason : " + codeToError(zone) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(zone));
                }
                reportAdError(codeToError(zone));
            }

            @Override
            public void onOpened(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                reportAdShow();
                reportAdImpForLTV();
                if (getAdListener() != null) {
                    getAdListener().onInterstitialShow();
                }
            }

            public void onLeftApplication(AdColonyInterstitial ad) {
            }

            public void onClicked(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onInterstitialClick();
                }
                reportAdClick();
                reportAdClickForLTV();
            }

            public void onClosed(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                mAdColonyInterstitial = null;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        };
        AdColony.requestInterstitial(getPid(), listener);
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showInterstitial() {
        if (mAdColonyInterstitial != null && !mAdColonyInterstitial.isExpired()) {
            mAdColonyInterstitial.show();
            mAdColonyInterstitial = null;
            reportAdCallShow();
            reportAdShowForLTV();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRewaredVideoLoaded() {
        boolean loaded = mAdColonyInterstitial != null && !mAdColonyInterstitial.isExpired();
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
        if (isLoading()) {
            if (blockLoading()) {
                Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
                }
                return;
            } else {
                Log.d(Log.TAG, "clear loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
                if (mAdColonyInterstitial != null) {
                    mAdColonyInterstitial.setListener(null);
                }
            }
        }
        setLoading(true, STATE_REQUEST);

        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                mAdColonyInterstitial = ad;
                Log.v(Log.TAG, "adloaded placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType());
                setLoading(false, STATE_SUCCESS);
                reportAdLoaded();
                if (getAdListener() != null) {
                    setLoadedFlag();
                    getAdListener().onRewardedVideoAdLoaded(AdColonyLoader.this);
                }
            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                Log.v(Log.TAG, "reason : " + codeToError(zone) + " , placename : " + getAdPlaceName() + " , sdk : " + getSdkName() + " , type : " + getAdType() + " , pid : " + getPid());
                setLoading(false, STATE_FAILURE);
                if (getAdListener() != null) {
                    getAdListener().onInterstitialError(toSdkError(zone));
                }
                reportAdError(codeToError(zone));
            }

            @Override
            public void onOpened(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                reportAdShow();
                reportAdImpForLTV();
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdShowed();
                }
            }

            public void onLeftApplication(AdColonyInterstitial ad) {
            }

            public void onClicked(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    getAdListener().onRewardedVideoAdClicked();
                }
                reportAdClick();
                reportAdClickForLTV();
            }

            public void onClosed(AdColonyInterstitial ad) {
                Log.v(Log.TAG, "");
                mAdColonyInterstitial = null;
                if (getAdListener() != null) {
                    getAdListener().onInterstitialDismiss();
                }
            }
        };
        AdColony.setRewardListener(new AdColonyRewardListener() {
            @Override
            public void onReward(AdColonyReward adColonyReward) {
                Log.v(Log.TAG, "");
                if (getAdListener() != null) {
                    AdReward item = new AdReward();
                    if (adColonyReward != null) {
                        item.setAmount(String.valueOf(adColonyReward.getRewardAmount()));
                        item.setType(adColonyReward.getRewardName());
                    }
                    getAdListener().onRewarded(item);
                }
                reportAdReward();
            }
        });
        AdColony.requestInterstitial(getPid(), listener);
        reportAdRequest();
        Log.v(Log.TAG, "");
    }

    @Override
    public boolean showRewardedVideo() {
        if (mAdColonyInterstitial != null && !mAdColonyInterstitial.isExpired()) {
            mAdColonyInterstitial.show();
            mAdColonyInterstitial = null;
            reportAdCallShow();
            reportAdShowForLTV();
            return true;
        }
        return false;
    }


    private String codeToError(AdColonyZone zone) {
        return "ERROR_NOFILL";
    }

    private int toSdkError(AdColonyZone zone) {
        return Constant.AD_ERROR_NOFILL;
    }

    @Override
    public void destroy() {
    }
}
