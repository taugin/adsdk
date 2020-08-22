package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdReward;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    private static final int LAYOUT[] = new int[]{
            R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium
    };
    private static final Map<String, Integer> LAYOUT_MAP;

    static {
        LAYOUT_MAP = new HashMap<>();
        LAYOUT_MAP.put("tiny", AdExtra.NATIVE_CARD_TINY);
        LAYOUT_MAP.put("small", AdExtra.NATIVE_CARD_SMALL);
        LAYOUT_MAP.put("medium", AdExtra.NATIVE_CARD_MEDIUM);
        LAYOUT_MAP.put("large", AdExtra.NATIVE_CARD_LARGE);
        LAYOUT_MAP.put("full", AdExtra.NATIVE_CARD_FULL);
    }

    private static final String TAG = "MA";
    private Context mContext;
    private RelativeLayout mNativeBannerLayout;
    private Button mRewardButton;
    private int mRewardShowTimes = 0;
    private int mRewardGetTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRewardButton = findViewById(R.id.reward_video_queue);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = getApplicationContext();
        AdSdk.get(mContext).init();
        updateRewardButton();
        mRewardButton.post(mUpdateRewardButton);
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
        if (v.getId() == R.id.gt_outer) {
            // loadGtOuter();
            if (AdSdk.get(this).isComplexAdsLoaded()) {
                AdSdk.get(this).showComplexAds();
            }
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial();
        } else if (v.getId() == R.id.complex) {
            loadAdComplex();
        } else if (v.getId() == R.id.native_common) {
            loadAdViewCommon();
        } else if (v.getId() == R.id.reward_video) {
            if (AdSdk.get(mContext).isRewardedVideoLoaded("reward_video")) {
                AdSdk.get(mContext).showRewardedVideo("reward_video");
            } else {
                AdSdk.get(mContext).loadRewardedVideo("reward_video", new SimpleAdSdkListener() {
                    @Override
                    public void onRewarded(String pidName, String source, String adType, AdReward item) {
                        Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType + " , item : " + item);
                        runToast(item.toString());
                    }
                });
            }
        } else if (v.getId() == R.id.reward_video_queue) {
            AdSdk.get(mContext).showRewardedVideo("reward_video");
        } else {
            String tag = (String) v.getTag();
            loadAdViewByLayout(tag);
        }
    }

    private void loadAdViewByLayout(String tag) {
        int layout = LAYOUT_MAP.get(tag);
        AdParams.Builder builder = new AdParams.Builder();
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, layout);
        AdParams adParams = builder.build();
        AdSdk.get(mContext).loadComplexAds("for_layout", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                AdSdk.get(mContext).showComplexAds(pidName);
            }
        });
    }

    private void loadGtOuter() {
        if (AdSdk.get(mContext).isComplexAdsLoaded("nt_outer_place")) {
            AdSdk.get(mContext).showComplexAds("nt_outer_place");
        } else {
            AdSdk.get(mContext).loadComplexAds("nt_outer_place", new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    AdSdk.get(mContext).showComplexAds(pidName);
                }
            });
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
        if (AdSdk.get(mContext).isComplexAdsLoaded("banner_and_native")) {
            AdSdk.get(mContext).showComplexAds("banner_and_native");
            return;
        }
        AdParams.Builder builder = new AdParams.Builder();
        int styles[] = new int[]{AdExtra.NATIVE_CARD_FULL, AdExtra.NATIVE_CARD_SMALL, AdExtra.NATIVE_CARD_MEDIUM, AdExtra.NATIVE_CARD_TINY, AdExtra.NATIVE_CARD_LARGE};
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, styles[new Random(System.currentTimeMillis()).nextInt(styles.length)]);
        AdParams adParams = builder.build();
        AdSdk.get(mContext).loadComplexAds("banner_and_native", adParams, new SimpleAdSdkListener() {
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
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.common_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_TINY);
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
            Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            if (AdExtra.AD_TYPE_BANNER.equalsIgnoreCase(adType) || AdExtra.AD_TYPE_NATIVE.equalsIgnoreCase(adType)) {
                // showAdView(pidName);
                AdSdk.get(getBaseContext()).showAdView(pidName, mNativeBannerLayout);
            } else if (AdExtra.AD_TYPE_INTERSTITIAL.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(pidName);
            } else if (AdExtra.AD_TYPE_REWARD.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showRewardedVideo(pidName);
            }
        }

        @Override
        public void onLoading(String pidName, String source, String adType) {
            Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onImp(String pidName, String source, String adType) {
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

    private Runnable mUpdateRewardButton = new Runnable() {
        @Override
        public void run() {
            loadRewardIfNeed();
            updateRewardButton();
            mRewardButton.postDelayed(this, 5000);
        }
    };

    private void loadRewardIfNeed() {
        if (!AdSdk.get(this).isRewardedVideoLoaded("reward_video") && ActivityMonitor.get(this).appOnTop()) {
            AdSdk.get(this).loadRewardedVideo("reward_video", mRewardListener);
        } else {
            AdSdk.get(this).setOnAdSdkListener("reward_video", mRewardListener);
        }
    }

    private SimpleAdSdkListener mRewardListener = new SimpleAdSdkListener() {
        @Override
        public void onLoaded(String pidName, String source, String adType) {
            updateRewardButton();
        }

        @Override
        public void onImp(String pidName, String source, String adType) {
            super.onImp(pidName, source, adType);
            mRewardShowTimes++;
            updateRewardButton();
        }

        @Override
        public void onRewarded(String pidName, String source, String adType, AdReward item) {
            super.onRewarded(pidName, source, adType, item);
            mRewardGetTimes++;
            updateRewardButton();
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            super.onDismiss(pidName, source, adType);
            updateRewardButton();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRewardButton.removeCallbacks(mUpdateRewardButton);
    }

    private void updateRewardButton() {
        int loadedAdCount = AdSdk.get(this).getLoadedAdCount("reward_video");
        mRewardButton.setText("已加载激励视频数 : " + loadedAdCount + " (" + mRewardGetTimes + "/" + mRewardShowTimes + ")");
        mRewardButton.setEnabled(loadedAdCount > 0);
    }
}
