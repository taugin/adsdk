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

import com.simple.mpsdk.MpParams;
import com.simple.mpsdk.RewardItem;
import com.simple.mpsdk.MpSdk;
import com.simple.mpsdk.listener.SimpleMpSdkListener;
import com.simple.mpsdk.utils.Utils;

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
            MpSdk.NATIVE_CARD_SMALL,
            MpSdk.NATIVE_CARD_MEDIUM,
            MpSdk.NATIVE_CARD_LARGE
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
        MpSdk.get(mContext).init();
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
        MpSdk.get(mContext).loadInterstitial("interstitial_test", new SimpleMpSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                MpSdk.get(mContext).showInterstitial(pidName);
            }
        });
    }

    private void loadReward() {
        MpSdk.get(mContext).loadRewardVideo("reward_test", new SimpleMpSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                MpSdk.get(mContext).showRewardVideo(pidName);
            }
        });
    }

    private void loadBanner() {
        MpParams.Builder builder = new MpParams.Builder();
        //  设置外部布局参数
        MpParams mpParams = builder.build();
        MpSdk.get(mContext).loadCommonView("banner_test", mpParams, new SimpleMpSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , adType : " + adType);
                MpSdk.get(mContext).showCommonView(pidName, mNativeBannerLayout);
            }
        });
    }

    private void loadNative() {
        MpParams.Builder builder = new MpParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[1/*mRandom.nextInt(LAYOUT.length)*/];
        View view = LayoutInflater.from(this).inflate(layoutId, null);
//         builder.setAdRootLayout(layoutId);
        builder.setAdRootView(view);
        builder.setAdTitle(R.id.common_title);
        builder.setAdDetail(R.id.common_detail);
        builder.setAdSubTitle(R.id.common_sub_title);
        builder.setAdIcon(R.id.common_icon);
        builder.setAdAction(R.id.common_action_btn);
        builder.setAdCover(R.id.common_image_cover);
        builder.setAdChoices(R.id.common_ad_choices_container);
        builder.setAdMediaView(R.id.common_media_cover);
        MpParams mpParams = builder.build();

        MpSdk.get(mContext).loadCommonView("native_demo_test", mpParams, new SimpleMpSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                MpSdk.get(mContext).showCommonView(pidName, mNativeBannerLayout);
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

    private SimpleMpSdkListener mSimpleAdsdkListener = new SimpleMpSdkListener() {

        @Override
        public void onLoaded(String pidName, String adType) {
            if (MpSdk.AD_TYPE_BANNER.equalsIgnoreCase(adType) || MpSdk.AD_TYPE_NATIVE.equalsIgnoreCase(adType)) {
                // showAdView(pidName);
                MpSdk.get(getBaseContext()).showCommonView(pidName, mNativeBannerLayout);
            } else if (MpSdk.AD_TYPE_INTERSTITIAL.equalsIgnoreCase(adType)) {
                MpSdk.get(getBaseContext()).showInterstitial(pidName);
            } else if (MpSdk.AD_TYPE_REWARD.equalsIgnoreCase(adType)) {
                MpSdk.get(getBaseContext()).showInterstitial(pidName);
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
        public void onRewarded(String pidName, String adType, RewardItem item) {
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
