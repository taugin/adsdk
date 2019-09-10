package com.inner.adsdk.demo;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.AdSdk;
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
            AdSdk.NATIVE_CARD_SMALL,
            AdSdk.NATIVE_CARD_MEDIUM,
            AdSdk.NATIVE_CARD_LARGE
    };

    private static final String TAG = "MA";
    private Context mContext;
    private RelativeLayout mNativeBannerLayout;
    private Random mRandom = new Random(System.currentTimeMillis());
    private int mIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = this;
        AdSdk.get(mContext).init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if (v.getId() == R.id.interstitial) {
            loadInterstitial();
        } else if (v.getId() == R.id.banner) {
            loadBanner();
        } else if (v.getId() == R.id.native_common) {
            loadNative();
        } else if (v.getId() == R.id.reward_video) {
            loadReward();
        } else if (v.getId() == R.id.show_listview_ad) {
            startActivity(new Intent(this, ListViewForAd.class));
        }
    }

    private void loadInterstitial() {
        AdSdk.get(mContext).loadInterstitial("interstitial_test", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                AdSdk.get(mContext).showInterstitial(pidName);
            }
        });
    }

    private void loadReward() {
        AdSdk.get(mContext).loadRewardVideo("reward_test", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                AdSdk.get(mContext).showRewardVideo(pidName);
            }
        });
    }

    private void loadBanner() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        AdParams adParams = builder.build();
        AdSdk.get(mContext).loadBanner("banner_test", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , adType : " + adType);
                AdSdk.get(mContext).showBanner(pidName, mNativeBannerLayout);
            }
        });
    }

    private void loadNative() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[mIndex++%LAYOUT.length];
        View view = LayoutInflater.from(this).inflate(layoutId, null);
        // builder.setAdRootLayout(layoutId);
        builder.setAdRootView(view);
        builder.setAdTitle(R.id.common_title);
        builder.setAdDetail(R.id.common_detail);
        builder.setAdSubTitle(R.id.common_sub_title);
        builder.setAdIcon(R.id.common_icon);
        builder.setAdAction(R.id.common_action_btn);
        builder.setAdCover(R.id.common_image_cover);
        builder.setAdChoices(R.id.common_ad_choices_container);
        builder.setAdMediaView(R.id.common_media_cover);
        AdParams adParams = builder.build();

        AdSdk.get(mContext).loadNative("native_test", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                AdSdk.get(mContext).showNative(pidName, mNativeBannerLayout);
            }
        });
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
        public void onLoaded(String pidName, String adType) {
            if (AdSdk.AD_TYPE_BANNER.equalsIgnoreCase(adType) || AdSdk.AD_TYPE_NATIVE.equalsIgnoreCase(adType)) {
                // showAdView(pidName);
                AdSdk.get(getBaseContext()).showNative(pidName, mNativeBannerLayout);
            } else if (AdSdk.AD_TYPE_INTERSTITIAL.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(pidName);
            } else if (AdSdk.AD_TYPE_REWARD.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(pidName);
            }
        }

        @Override
        public void onLoading(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
        }

        @Override
        public void onShow(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
            Toast.makeText(mContext, adType + " - " + pidName + " - onShow()", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
            Toast.makeText(mContext, adType + " - " + pidName + " - onClick()", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDismiss(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
            Toast.makeText(mContext, adType + " - " + pidName + " - onDismiss()", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
        }

        @Override
        public void onRewarded(String pidName, String adType, AdReward item) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType + " , item : " + item);
            runToast(item.toString());
        }

        @Override
        public void onCompleted(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
        }

        @Override
        public void onStarted(String pidName, String adType) {
            Log.d(TAG, "pidName : " + pidName  + " , adType : " + adType);
        }
    };

}
