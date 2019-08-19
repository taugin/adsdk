package com.bac.ioc.gsb.scloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bac.ioc.gsb.scconfig.HtConfig;
import com.bac.ioc.gsb.scpolicy.HtPolicy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.common.BaseLoader;
import com.hauyu.adsdk.data.config.AdConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HtAdLoader extends BaseLoader {

    public static final String HTPLACE_OUTER_NAME = "ht_outer_place";
    private static HtAdLoader sHtAdLoader;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private AdSdk mAdSdk;

    private HtAdLoader(Context context) {
        mContext = context.getApplicationContext();
        AdReceiver.get(context).registerTriggerListener(this);
    }

    public static HtAdLoader get(Context context) {
        if (sHtAdLoader == null) {
            create(context);
        }
        return sHtAdLoader;
    }

    private static void create(Context context) {
        synchronized (HtAdLoader.class) {
            if (sHtAdLoader == null) {
                sHtAdLoader = new HtAdLoader(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        HtPolicy.get(mContext).init();
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
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        HtConfig htConfig = DataManager.get(mContext).getRemoteHtPolicy();
        if (htConfig == null && adConfig != null) {
            htConfig = adConfig.getHtConfig();
        }
        HtPolicy.get(mContext).setPolicy(htConfig);
    }

    private void fireHome() {
        if (mAdSdk != null) {
            updateHtPolicy();
            if (!HtPolicy.get(mContext).isHtAllowed()) {
                return;
            }
            if (HtPolicy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, "ht is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            HtPolicy.get(mContext).setLoading(true);
            EventImpl.get().reportAdOuterRequest(mContext, HtPolicy.get(mContext).getType(), HTPLACE_OUTER_NAME);
            mAdSdk.loadComplexAds(HTPLACE_OUTER_NAME, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    EventImpl.get().reportAdOuterLoaded(mContext, HtPolicy.get(mContext).getType(), pidName);
                    HtPolicy.get(mContext).setLoading(false);
                    if (HtPolicy.get(mContext).isHtAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (HtPolicy.get(mContext).isShowBottomActivity()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType);
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                            }
                        }
                        EventImpl.get().reportAdOuterCallShow(mContext, HtPolicy.get(mContext).getType(), pidName);
                    } else {
                        EventImpl.get().reportAdOuterDisallow(mContext, HtPolicy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).reportShowing(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && HtPolicy.get(mContext).isShowBottomActivity()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).reportShowing(true);
                    EventImpl.get().reportAdOuterShowing(mContext, HtPolicy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).setLoading(false);
                }
            });
        }
    }
}