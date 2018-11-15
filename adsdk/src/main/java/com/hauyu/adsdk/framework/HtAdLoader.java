package com.hauyu.adsdk.framework;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.appub.ads.a.FSA;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.R;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.HtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.policy.HtPolicy;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HtAdLoader {

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
                            if (Constant.TYPE_BANNER.equals(adType)
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
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).reportHtShowing(true);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    HtPolicy.get(mContext).setLoading(false);
                }
            });
        }
    }

    private AdParams generateAdParams() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);

        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.native_card_full);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.native_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.native_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.native_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.native_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.native_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.native_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.native_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.native_media_cover);
        AdParams adParams = builder.build();
        return adParams;
    }

    private void show(String pidName, String source, String adType) {
        try {
            Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.AFPICKER");
            if (intent == null) {
                intent = new Intent(mContext, FSA.class);
            }
            intent.putExtra(Intent.EXTRA_TITLE, pidName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
}