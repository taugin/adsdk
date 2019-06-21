package com.inner.adsdk.loader;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.common.BaseLoader;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.GtPolicy;
import com.inner.adsdk.stat.StatImpl;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtAdLoader extends BaseLoader {

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
                Log.iv(Log.TAG, "mi not allow");
                return;
            }
            if (GtPolicy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, "gt is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            String outerPidName = getNextPidName();
            StatImpl.get().reportAdOuterRequest(mContext, GtPolicy.get(mContext).getType(), outerPidName);
            GtPolicy.get(mContext).startGtRequest();
            GtPolicy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(outerPidName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).setLoading(false);
                    StatImpl.get().reportAdOuterLoaded(mContext, GtPolicy.get(mContext).getType(), pidName);
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
                            }
                        }
                        StatImpl.get().reportAdOuterCallShow(mContext, GtPolicy.get(mContext).getType(), pidName);
                    } else {
                        StatImpl.get().reportAdOuterDisallow(mContext, GtPolicy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
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
                    Log.iv(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportShowing(true);
                    StatImpl.get().reportAdOuterShowing(mContext, GtPolicy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
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