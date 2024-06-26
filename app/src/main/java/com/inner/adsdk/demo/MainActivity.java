package com.inner.adsdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.listener.OnAdRewardListener;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.utils.Utils;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int LAYOUT[] = new int[]{
            R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium,
            R.layout.ad_common_native_banner,
            R.layout.ad_common_native_card_large
    };
    private static final int CARDID[] = new int[]{
            AdExtra.NATIVE_CARD_SMALL,
            AdExtra.NATIVE_CARD_MEDIUM,
            AdExtra.NATIVE_CARD_LARGE
    };

    private static final String TAG = "MA";
    private Context mContext;
    private RelativeLayout mNativeBannerLayout;
    private TextView mRewardCount;
    private View mShowReward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRewardCount = findViewById(R.id.reward_count);
        mShowReward = findViewById(R.id.show_reward);
        mShowReward.setEnabled(false);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = this;
        AdSdk.get(mContext).init();
        AdSdk.get(mContext).registerListener(mOnAdRewardListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdSdk.get(mContext).unregisterListener(mOnAdRewardListener);
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
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial();
        } else if (v.getId() == R.id.complex) {
            loadAdComplex();
        } else if (v.getId() == R.id.native_common) {
            loadAdViewCommon();
        } else if (v.getId() == R.id.reward_video) {
            AdSdk.get(this).loadInterstitial("reward_video", mSimpleAdsdkListener);
        } else if (v.getId() == R.id.show_reward) {
            AdSdk.get(mContext).showRewardVideo();
        } else if (v.getId() == R.id.show_listview_ad) {
            startActivity(new Intent(this, ListViewForAd.class));
        }
    }

    private void loadGtOuter() {
        if (AdSdk.get(mContext).isInterstitialLoaded("gt_outer_place")) {
            AdSdk.get(mContext).showInterstitial("gt_outer_place");
        } else {
            AdSdk.get(mContext).loadInterstitial("gt_outer_place", null);
        }
    }

    private void loadInterstitial() {
        AdSdk.get(mContext).loadInterstitial("interstitial1", mSimpleAdsdkListener);
    }

    private void loadAdComplex() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[new Random(System.currentTimeMillis()).nextInt(LAYOUT.length)];
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        // builder.setAdRootView(AdExtra.AD_SDK_COMMON, LayoutInflater.from(this).inflate(layoutId, null));
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
            builder.setBannerSize(AdExtra.AD_SDK_INMOBI, AdExtra.INMOBI_BANNER);
        } else {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
            builder.setBannerSize(AdExtra.AD_SDK_INMOBI, AdExtra.INMOBI_MEDIUM_RECTANGLE);
        }
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
        mOnAdRewardListener.onRefresh(AdSdk.get(mContext).isRewardLoaded());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOnAdRewardListener.onRefresh(AdSdk.get(mContext).isRewardLoaded());
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
            Toast.makeText(mContext, source + " - " + adType + " - " + pidName + " - onLoaded()", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, source + " - " + adType + " - " + pidName + " - onShow()", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            Toast.makeText(mContext, source + " - " + adType + " - " + pidName + " - onClick()", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            Toast.makeText(mContext, source + " - " + adType + " - " + pidName + " - onDismiss()", Toast.LENGTH_SHORT).show();
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


    private OnAdRewardListener mOnAdRewardListener = new OnAdRewardListener() {
        @Override
        public void onRefresh(boolean adLoaded) {
            mShowReward.setEnabled(adLoaded);
        }

        @Override
        public void onReward() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Reward has complete");
            builder.setMessage("Congratulations");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int count = Integer.parseInt(mRewardCount.getText().toString());
                        mRewardCount.setText(String.valueOf(count + 1));
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e, e);
                    }
                }
            });
            builder.create().show();
        }

        @Override
        public void onDismiss() {

        }

        @Override
        public void onClick() {

        }

        @Override
        public void onNoReward() {

        }

        @Override
        public void onShow() {

        }

        @Override
        public void onLoaded() {

        }
    };
}
