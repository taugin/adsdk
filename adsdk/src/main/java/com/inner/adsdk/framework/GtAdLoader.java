package com.inner.adsdk.framework;

import android.content.Context;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.GtPolicy;
import com.inner.adsdk.stat.StatImpl;

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
                        mAdSdk.showComplexAds(pidName, null);
                        StatImpl.get().reportAdOuterShow(mContext);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportGtShowing(false);
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
}