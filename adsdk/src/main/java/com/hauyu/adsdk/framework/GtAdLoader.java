package com.hauyu.adsdk.framework;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.GtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.policy.GtPolicy;
import com.hauyu.adsdk.stat.StatImpl;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtAdLoader extends BottomLoader {

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

    @Override
    protected Context getContext() {
        return mContext;
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
            mAdSdk.loadComplexAds(getNextPidName(), generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).setLoading(false);
                    StatImpl.get().reportAdOuterLoaded(mContext);
                    if (GtPolicy.get(mContext).isGtAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (GtPolicy.get(mContext).isShowBottomActivity()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType);
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                                StatImpl.get().reportAdOuterShow(mContext);
                            }
                        }
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportShowing(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && GtPolicy.get(mContext).isShowBottomActivity()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportShowing(true);
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

    private String getNextPidName() {
        int nTRate = GtPolicy.get(mContext).getNTRate();
        boolean isNtPid = new Random(System.currentTimeMillis()).nextInt(100) < nTRate;
        if (isNtPid) {
            return Constant.NTPLACE_OUTER_NAME;
        }
        return Constant.GTPLACE_OUTER_NAME;
    }
}