package com.bacad.ioc.gsb.scloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bacad.ioc.gsb.base.Bldr;
import com.bacad.ioc.gsb.base.CSvr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.bacad.ioc.gsb.scpolicy.HvPcy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HvAdl extends Bldr {

    private static HvAdl sHvAdl;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private AdSdk mAdSdk;

    private HvAdl(Context context) {
        super(HvPcy.get(context));
        mContext = context.getApplicationContext();
        CSvr.get(context).registerTriggerListener(this);
    }

    public static HvAdl get(Context context) {
        if (sHvAdl == null) {
            create(context);
        }
        return sHvAdl;
    }

    private static void create(Context context) {
        synchronized (HvAdl.class) {
            if (sHvAdl == null) {
                sHvAdl = new HvAdl(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        HvPcy.get(mContext).init();
        updateHtPolicy();
    }

    @Override
    protected Context getContext() {
        return mContext;
    }

    @Override
    public void onHomePressed(Context context) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fireHome();
            }
        }, 500);
    }

    private void updateHtPolicy() {
        HvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteHtPolicy());
    }

    private void fireHome() {
        if (mAdSdk != null) {
            updateHtPolicy();
            if (!HvPcy.get(mContext).isHtAllowed()) {
                return;
            }
            String placeName = getPlaceNameAdv();
            if (TextUtils.isEmpty(placeName)) {
                Log.iv(Log.TAG, getType() + " not found place name");
                return;
            }
            if (HvPcy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, getType() + " is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            HvPcy.get(mContext).setLoading(true);
            SceneEventImpl.get().reportAdSceneRequest(mContext, HvPcy.get(mContext).getType(), placeName);
            mAdSdk.loadComplexAds(placeName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    SceneEventImpl.get().reportAdSceneLoaded(mContext, HvPcy.get(mContext).getType(), pidName);
                    HvPcy.get(mContext).setLoading(false);
                    if (HvPcy.get(mContext).isHtAllowed()) {
                        if (HvPcy.get(mContext).isShowBottom()
                                || Constant.TYPE_BANNER.equals(adType)
                                || Constant.TYPE_NATIVE.equals(adType)) {
                            show(pidName, source, adType, HvPcy.get(mContext).getType());
                        } else {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        }
                        SceneEventImpl.get().reportAdSceneShow(mContext, HvPcy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdSceneDisallow(mContext, HvPcy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HvPcy.get(mContext).reportImpression(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && HvPcy.get(mContext).isShowBottom()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onImp(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HvPcy.get(mContext).reportImpression(true);
                    SceneEventImpl.get().reportAdSceneImp(mContext, HvPcy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HvPcy.get(mContext).setLoading(false);
                }
            });
        }
    }
}