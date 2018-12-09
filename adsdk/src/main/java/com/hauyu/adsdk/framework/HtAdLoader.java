package com.hauyu.adsdk.framework;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.HtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.policy.HtPolicy;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HtAdLoader extends BottomLoader {

    private static HtAdLoader sHtAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;

    private HtAdLoader(Context context) {
        mContext = context.getApplicationContext();
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

    public void init(AdSdk adSdk) {
        mAdSdk = adSdk;
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

    private void updateHtPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        HtConfig htConfig = DataManager.get(mContext).getRemoteHtPolicy();
        if (htConfig == null && adConfig != null) {
            htConfig = adConfig.getHtConfig();
        }
        HtPolicy.get(mContext).setPolicy(htConfig);
    }

    public void fireHome() {
        if (mAdSdk != null) {
            updateHtPolicy();
            if (!HtPolicy.get(mContext).isHtAllowed()) {
                return;
            }
            if (HtPolicy.get(mContext).isLoading()) {
                Log.v(Log.TAG, "ht is loading");
                return;
            }
            Log.v(Log.TAG, "");
            HtPolicy.get(mContext).setLoading(true);
            mAdSdk.loadComplexAds(Constant.HTPLACE_OUTER_NAME, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
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
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
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
                    Log.v(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).reportShowing(true);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).setLoading(false);
                }
            });
        }
    }
}