package com.bac.ioc.gsb.scloader;

import android.content.Context;
import android.text.TextUtils;

import com.bac.ioc.gsb.scconfig.GtConfig;
import com.bac.ioc.gsb.scpolicy.GtPolicy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.common.BaseLoader;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GtAdLoader extends BaseLoader {
    public static final String GTPLACE_OUTER_NAME = "gt_outer_place";
    public static final String NTPLACE_OUTER_NAME = "nt_outer_place";

    private static GtAdLoader sGtAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;

    private GtAdLoader(Context context) {
        mContext = context.getApplicationContext();
        AdReceiver.get(context).registerTriggerListener(this);
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

    public void init() {
        mAdSdk = AdSdk.get(mContext);
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

    @Override
    public void onAlarm(Context context) {
        GtAdLoader.get(context).onFire();
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
            EventImpl.get().reportAdOuterRequest(mContext, GtPolicy.get(mContext).getType(), outerPidName);
            GtPolicy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(outerPidName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).setLoading(false);
                    EventImpl.get().reportAdOuterLoaded(mContext, GtPolicy.get(mContext).getType(), pidName);
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
                        EventImpl.get().reportAdOuterCallShow(mContext, GtPolicy.get(mContext).getType(), pidName);
                    } else {
                        EventImpl.get().reportAdOuterDisallow(mContext, GtPolicy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss place_name : " + pidName + " , source : " + source + " , adType : " + adType);
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
                    Log.iv(Log.TAG, "show place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportShowing(true);
                    EventImpl.get().reportAdOuterShowing(mContext, GtPolicy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).updateLastFailTime();
                    GtPolicy.get(mContext).setLoading(false);
                }
            });
        }
    }

    private String getNextPidName() {
        int nTRate = GtPolicy.get(mContext).getNTRate();
        boolean isNtPid = new Random(System.currentTimeMillis()).nextInt(100) < nTRate;
        if (isNtPid) {
            return NTPLACE_OUTER_NAME;
        }
        return GTPLACE_OUTER_NAME;
    }
}