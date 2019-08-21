package com.bacad.ioc.gsb.scloader;

import android.content.Context;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.Bldr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.bacad.ioc.gsb.scpolicy.GvPcy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GvAdl extends Bldr {
    public static final String GTPLACE_OUTER_NAME = "gt_outer_place";
    public static final String NTPLACE_OUTER_NAME = "nt_outer_place";

    private static GvAdl sGvAdl;

    private Context mContext;
    private AdSdk mAdSdk;

    private GvAdl(Context context) {
        mContext = context.getApplicationContext();
        AdReceiver.get(context).registerTriggerListener(this);
    }

    public static GvAdl get(Context context) {
        if (sGvAdl == null) {
            create(context);
        }
        return sGvAdl;
    }

    private static void create(Context context) {
        synchronized (GvAdl.class) {
            if (sGvAdl == null) {
                sGvAdl = new GvAdl(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        GvPcy.get(mContext).init();
        updateAdPolicy();
    }

    @Override
    protected Context getContext() {
        return mContext;
    }

    @Override
    public void onAlarm(Context context) {
        GvAdl.get(context).onFire();
    }

    private void updateAdPolicy() {
        GvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteGtPolicy());
    }

    public void onFire() {
        fireOuterAd();
    }

    private void fireOuterAd() {
        if (mAdSdk != null) {
            updateAdPolicy();
            if (!GvPcy.get(mContext).isGtAllowed()) {
                return;
            }
            if (!GvPcy.get(mContext).isMatchMinInterval()) {
                Log.iv(Log.TAG, "mi not allow");
                return;
            }
            if (GvPcy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, "gt is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            String outerPidName = getNextPidName();
            SceneEventImpl.get().reportAdOuterRequest(mContext, GvPcy.get(mContext).getType(), outerPidName);
            GvPcy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(outerPidName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).setLoading(false);
                    SceneEventImpl.get().reportAdOuterLoaded(mContext, GvPcy.get(mContext).getType(), pidName);
                    if (GvPcy.get(mContext).isGtAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (GvPcy.get(mContext).isShowBottomActivity()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType);
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                            }
                        }
                        SceneEventImpl.get().reportAdOuterCallShow(mContext, GvPcy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdOuterDisallow(mContext, GvPcy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).reportShowing(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && GvPcy.get(mContext).isShowBottomActivity()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "show place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).reportShowing(true);
                    SceneEventImpl.get().reportAdOuterShowing(mContext, GvPcy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).updateLastFailTime();
                    GvPcy.get(mContext).setLoading(false);
                }
            });
        }
    }

    private String getNextPidName() {
        int nTRate = GvPcy.get(mContext).getNTRate();
        boolean isNtPid = new Random(System.currentTimeMillis()).nextInt(100) < nTRate;
        if (isNtPid) {
            return NTPLACE_OUTER_NAME;
        }
        return GTPLACE_OUTER_NAME;
    }
}