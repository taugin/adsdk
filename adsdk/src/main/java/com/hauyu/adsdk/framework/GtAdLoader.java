package com.hauyu.adsdk.framework;

import android.content.Context;
import android.content.Intent;

import com.hauyu.adsdk.config.GtConfig;
import com.hauyu.adsdk.stat.StatImpl;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.policy.GtPolicy;
import com.appub.ads.a.FSA;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtAdLoader {

    private static GtAdLoader sGtAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;

    private GtAdLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public static GtAdLoader get(Context context) {
        if (sGtAdLoader == null) {
            create(context);
        }
        return sGtAdLoader;
    }

    private static void create(Context context) {
        synchronized (GtAdLoader.class) {
            if (sGtAdLoader == null) {
                sGtAdLoader = new GtAdLoader(context);
            }
        }
    }

    public void init(AdSdk adSdk) {
        mAdSdk = adSdk;
        if (mAdSdk == null) {
            return;
        }
        GtPolicy.get(mContext).init();
        updateAdPolicy();
    }

    private void updateAdPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        GtConfig gtConfig = DataManager.get(mContext).getRemoteGtPolicy();
        if (gtConfig == null && adConfig != null) {
            gtConfig = adConfig.getGtConfig();
        }
        GtPolicy.get(mContext).setPolicy(gtConfig);
    }

    public void onFire() {
        Log.d(Log.TAG, "onFire");
        DataManager.get(mContext).refresh();
        fireOuterAd();
    }

    private void fireOuterAd() {
        if (mAdSdk != null) {
            updateAdPolicy();
            if (!GtPolicy.get(mContext).isGtAllowed()) {
                return;
            }
            if (!GtPolicy.get(mContext).isMatchMinInterval()) {
                Log.v(Log.TAG, "mi not allow");
                return;
            }
            if (GtPolicy.get(mContext).isLoading()) {
                Log.v(Log.TAG, "gt is loading");
                return;
            }
            Log.v(Log.TAG, "");
            StatImpl.get().reportAdOuterRequest(mContext);
            GtPolicy.get(mContext).startGtRequest();
            GtPolicy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(Constant.GTPLACE_OUTER_NAME, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).setLoading(false);
                    StatImpl.get().reportAdOuterLoaded(mContext);
                    if (GtPolicy.get(mContext).isGtAllowed()) {
                        show(pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportGtShowing(false);
                    hide();
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportGtShowing(true);
                    StatImpl.get().reportAdOuterShowing(mContext);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).setLoading(false);
                }
            });
        }
    }

    private void show(String pidName) {
        try {
            Intent intent = new Intent(mContext, FSA.class);
            intent.putExtra(Intent.EXTRA_TITLE, pidName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void hide() {
        try {
            Intent intent = new Intent(mContext.getPackageName() + "action.FA");
            intent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(intent);
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
}