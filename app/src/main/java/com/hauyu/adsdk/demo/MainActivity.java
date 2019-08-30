package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.utils.Utils;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int LAYOUT[] = new int[]{
            R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium
    };
    private static final int CARDID[] = new int[]{
            AdExtra.NATIVE_CARD_SMALL,
            AdExtra.NATIVE_CARD_MEDIUM,
            AdExtra.NATIVE_CARD_LARGE
    };

    private static final String TAG = "MA";
    private Context mContext;
    private RelativeLayout mNativeBannerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = this;
        AdSdk.get(mContext).init();
    }

    private boolean hasEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Activity.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
            return true;
        }
        return true;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.app_usage) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else if (v.getId() == R.id.gt_outer) {
            loadGtOuter();
//            if (!hasEnable()) {
//                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//                startActivity(intent);
//            } else {
//                AtAdLoader.get(this).onFire();
//            }
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial();
        } else if (v.getId() == R.id.complex) {
            loadAdComplex();
        } else if (v.getId() == R.id.native_common) {
            loadAdViewCommon();
        } else if (v.getId() == R.id.reward_video) {
            AdSdk.get(this).loadInterstitial("reward_video", mSimpleAdsdkListener);
        }
    }

    private void loadGtOuter() {
        if (AdSdk.get(mContext).isComplexAdsLoaded("nt_outer_place")) {
            AdSdk.get(mContext).showComplexAds("nt_outer_place");
        } else {
            AdSdk.get(mContext).loadComplexAds("nt_outer_place", null);
        }
    }

    private void loadInterstitial() {
        //AdSdk.get(mContext).loadInterstitial("interstitial", mSimpleAdsdkListener);
        if (AdSdk.get(mContext).isInterstitialLoaded("interstitial")) {
            AdSdk.get(mContext).showInterstitial("interstitial");
        } else {
            AdSdk.get(mContext).loadInterstitial("interstitial", null);
        }
    }

    private void loadAdComplex() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[new Random(System.currentTimeMillis()).nextInt(LAYOUT.length)];
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        View view = LayoutInflater.from(this).inflate(layoutId, null);
        // builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        builder.setAdRootView(AdExtra.AD_SDK_COMMON, view);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.common_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        AdParams adParams = builder.build();
        AdSdk.get(mContext).loadComplexAds("ad_complex", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                AdSdk.get(mContext).showComplexAds(pidName);
            }
        });
    }

    private void loadAdViewCommon() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[new Random().nextInt(LAYOUT.length)];
        if (layoutId == R.layout.ad_common_native_card_small) {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_LARGE_BANNER);
        } else {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        }
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        View view = LayoutInflater.from(this).inflate(layoutId, null);
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, CARDID[new Random().nextInt(CARDID.length)]);
        // builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        // builder.setAdRootView(AdExtra.AD_SDK_COMMON, view);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.common_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        AdParams adParams = builder.build();

        AdSdk.get(mContext).loadAdView("banner_and_native", adParams, mSimpleAdsdkListener);
    }

    private void showAdView(String pidName) {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.ad_common_native_card_large);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.common_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        AdParams adParams = builder.build();

        RelativeLayout layout = new RelativeLayout(this);
        layout.setGravity(Gravity.CENTER);
        Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = getResources().getDisplayMetrics().widthPixels;
        dialog.getWindow().setAttributes(params);
        AdSdk.get(this).showAdView(pidName, adParams, layout);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void runToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private SimpleAdSdkListener mSimpleAdsdkListener = new SimpleAdSdkListener() {

        @Override
        public void onLoaded(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            if (AdExtra.AD_TYPE_BANNER.equalsIgnoreCase(adType) || AdExtra.AD_TYPE_NATIVE.equalsIgnoreCase(adType)) {
                // showAdView(pidName);
                AdSdk.get(getBaseContext()).showAdView(pidName, mNativeBannerLayout);
            } else if (AdExtra.AD_TYPE_INTERSTITIAL.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(pidName);
            } else if (AdExtra.AD_TYPE_REWARD.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(pidName);
            }
        }

        @Override
        public void onLoading(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onShow(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onClick(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onError(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onRewarded(String pidName, String source, String adType, AdReward item) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType + " , item : " + item);
            runToast(item.toString());
        }

        @Override
        public void onCompleted(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onStarted(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }
    };
}
