package com.bacad.ioc.gsb.scloader;

import android.content.Context;
import android.text.TextUtils;

import com.bacad.ioc.gsb.base.Bldr;
import com.bacad.ioc.gsb.base.CSvr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.bacad.ioc.gsb.scpolicy.GvPcy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GvAdl extends Bldr {
    private static GvAdl sGvAdl;
    private Context mContext;
    private AdSdk mAdSdk;

    private GvAdl(Context context) {
        super(GvPcy.get(context));
        mContext = context.getApplicationContext();
        CSvr.get(context).registerTriggerListener(this);
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
            String placeName = getPlaceNameAdv();
            if (TextUtils.isEmpty(placeName)) {
                Log.iv(Log.TAG, getType() + " not found place name");
                return;
            }
            if (!GvPcy.get(mContext).isMatchMinInterval()) {
                Log.iv(Log.TAG, "mi not allow");
                return;
            }
            if (GvPcy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, getType() + " is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            SceneEventImpl.get().reportAdSceneRequest(mContext, GvPcy.get(mContext).getType(), placeName);
            GvPcy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(placeName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).setLoading(false);
                    SceneEventImpl.get().reportAdSceneLoaded(mContext, GvPcy.get(mContext).getType(), pidName);
                    if (GvPcy.get(mContext).isGtAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (GvPcy.get(mContext).isShowBottom()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType, GvPcy.get(mContext).getType());
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                            }
                        }
                        SceneEventImpl.get().reportAdSceneShow(mContext, GvPcy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdSceneDisallow(mContext, GvPcy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).reportImpression(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && GvPcy.get(mContext).isShowBottom()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onImp(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "show place_name : " + pidName + " , source : " + source + " , adType : " + adType);
                    GvPcy.get(mContext).reportImpression(true);
                    SceneEventImpl.get().reportAdSceneImp(mContext, GvPcy.get(mContext).getType(), pidName);
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
}