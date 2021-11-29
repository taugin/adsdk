package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hauyu.adsdk.demo.view.CustomDrawable;
import com.hauyu.adsdk.demo.view.DynamicHeartView;
import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.listener.AdLoaderFilter;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.rabbit.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private static final int LAYOUT[] = new int[]{
            //R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium
    };
    private static final Map<String, Integer> LAYOUT_MAP;

    static {
        LAYOUT_MAP = new HashMap<>();
        LAYOUT_MAP.put("micro", AdExtra.NATIVE_CARD_MICRO);
        LAYOUT_MAP.put("tiny", AdExtra.NATIVE_CARD_TINY);
        LAYOUT_MAP.put("little", AdExtra.NATIVE_CARD_LITTLE);
        LAYOUT_MAP.put("small", AdExtra.NATIVE_CARD_SMALL);
        LAYOUT_MAP.put("medium", AdExtra.NATIVE_CARD_MEDIUM);
        LAYOUT_MAP.put("large", AdExtra.NATIVE_CARD_LARGE);
        LAYOUT_MAP.put("wrap", AdExtra.NATIVE_CARD_WRAP);
        LAYOUT_MAP.put("full", AdExtra.NATIVE_CARD_FULL);
        LAYOUT_MAP.put("head", AdExtra.NATIVE_CARD_HEAD);
        LAYOUT_MAP.put("mix", AdExtra.NATIVE_CARD_MIX);
        LAYOUT_MAP.put("foot", AdExtra.NATIVE_CARD_FOOT);
        LAYOUT_MAP.put("round", AdExtra.NATIVE_CARD_ROUND);
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        CheckBox switchView = findViewById(R.id.auto_reward);
        switchView.setOnCheckedChangeListener(this);
        mRewardButton = findViewById(R.id.reward_video_queue);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = getApplicationContext();
        AdSdk.get(this).setAdLoaderFilter(new AdLoaderFilter() {
            @Override
            public boolean doFilter(String placeName, String sdk, String type) {
                return false;
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Drawable drawable = mNativeBannerLayout.getBackground();
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).start();
        }
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
        if (v.getId() == R.id.show_complex) {
            if (AdSdk.get(this).isComplexAdsLoaded()) {
                AdSdk.get(this).showComplexAds();
            }
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial((TextView) v);
        } else if (v.getId() == R.id.complex) {
            loadAdComplex();
        } else if (v.getId() == R.id.native_common) {
            loadAdViewCommon();
        } else if (v.getId() == R.id.reward_video) {
            loadReward((TextView) v);
        } else if (v.getId() == R.id.reward_video_queue) {
            AdSdk.get(mContext).showRewardedVideo("reward_video");
        } else if (v.getId() == R.id.spread_button) {
            showSpread();
        } else if (v.getId() == R.id.show_ads_in_list) {
            startActivity(new Intent(this, ListViewForAd.class));
        } else if (v.getId() == R.id.show_admob_splash) {
            loadSplash((TextView) v);
        } else {
            String tag = (String) v.getTag();
            loadAdViewByLayout(tag, (TextView) v);
        }
    }

    private void loadAdViewByLayout(String tag, TextView textView) {
        if (AdSdk.get(this).isComplexAdsLoaded("for_native_layout")) {
            AdSdk.get(this).showComplexAds("for_native_layout");
        } else {
            int layout = LAYOUT_MAP.get(tag);
            AdParams.Builder builder = new AdParams.Builder();
            builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, layout);
            AdParams adParams = builder.build();
            AdSdk.get(mContext).loadComplexAds("for_native_layout", adParams, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String placeName, String source, String adType, String pid) {
                    Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
                    String loadedSdk = AdSdk.get(mContext).getLoadedSdk(placeName);
                    Log.v(TAG, "loaded sdk : " + loadedSdk);
                    updateLoadStatus(textView, placeName);
                }

                @Override
                public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
                    updateLoadStatus(textView, placeName);
                }
            });
        }
    }

    private void loadInterstitial(TextView textView) {
        if (AdSdk.get(mContext).isInterstitialLoaded("interstitial")) {
            String loadedSdk = AdSdk.get(mContext).getLoadedSdk("interstitial");
            Log.v(TAG, "loaded sdk : " + loadedSdk);
            AdSdk.get(mContext).showInterstitial("interstitial");
        } else {
            AdSdk.get(mContext).loadInterstitial("interstitial", new FullScreenAdListener(textView));
        }
    }

    private void loadSplash(TextView textView) {
        if (AdSdk.get(mContext).isSplashLoaded("admob_splash")) {
            AdSdk.get(mContext).showSplash("admob_splash");
        } else {
            AdSdk.get(mContext).loadSplash("admob_splash", new FullScreenAdListener(textView));
        }
    }

    private void loadReward(TextView textView) {
        if (AdSdk.get(mContext).isRewardedVideoLoaded("reward_video")) {
            AdSdk.get(mContext).showRewardedVideo("reward_video");
        } else {
            AdSdk.get(mContext).loadRewardedVideo("reward_video", new FullScreenAdListener(textView) {
                @Override
                public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
                    super.onRewarded(placeName, source, adType, pid, item);
                    Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , item : " + item);
                    runToast(item.toString());
                }
            });
        }
    }

    private void loadAdComplex() {
        if (AdSdk.get(mContext).isComplexAdsLoaded("ad_complex")) {
            String loadedSdk = AdSdk.get(mContext).getLoadedSdk("ad_complex");
            Log.v(TAG, "loaded sdk : " + loadedSdk);
            AdSdk.get(mContext).showComplexAds("ad_complex");
            return;
        }
        AdParams.Builder builder = new AdParams.Builder();
        int styles[] = new int[]{AdExtra.NATIVE_CARD_FULL, AdExtra.NATIVE_CARD_SMALL, AdExtra.NATIVE_CARD_MEDIUM, AdExtra.NATIVE_CARD_TINY, AdExtra.NATIVE_CARD_LARGE};
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, styles[new Random(System.currentTimeMillis()).nextInt(styles.length)]);
        AdParams adParams = builder.build();
        AdSdk.get(mContext).loadComplexAds("ad_complex", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String placeName, String source, String adType, String pid) {
                Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
                String loadedSdk = AdSdk.get(mContext).getLoadedSdk(placeName);
                Log.v(TAG, "loaded sdk : " + loadedSdk);
                AdSdk.get(mContext).showComplexAds(placeName);
            }

            @Override
            public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
                Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , onDestroy : " + complexAds);
            }
        });
    }

    private void loadAdViewCommon() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[new Random().nextInt(LAYOUT.length)];
//        if (layoutId == R.layout.ad_common_native_card_small) {
//            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_LARGE_BANNER);
//        } else {
//            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
//        }
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.COMMON_ADAPTIVE_BANNER);
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_TINY);
        AdParams adParams = builder.build();

        AdSdk.get(mContext).loadAdView("banner_and_native", adParams, mSimpleAdsdkListener);
    }

    private void showAdView(String placeName) {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.ad_common_native_card_large);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
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
        AdSdk.get(this).showAdView(placeName, adParams, layout);
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
        public void onRequest(String placeName, String source, String adType, String pid) {
            Log.d(Log.TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onLoaded(String placeName, String source, String adType, String pid) {
            Log.d(Log.TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            String loadedSdk = AdSdk.get(mContext).getLoadedSdk(placeName);
            Log.v(TAG, "loaded sdk : " + loadedSdk);
            if (AdExtra.AD_TYPE_BANNER.equalsIgnoreCase(adType) || AdExtra.AD_TYPE_NATIVE.equalsIgnoreCase(adType)) {
                // showAdView(placeName);
                AdSdk.get(getBaseContext()).showAdView(placeName, mNativeBannerLayout);
            } else if (AdExtra.AD_TYPE_INTERSTITIAL.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showInterstitial(placeName);
            } else if (AdExtra.AD_TYPE_REWARD.equalsIgnoreCase(adType)) {
                AdSdk.get(getBaseContext()).showRewardedVideo(placeName);
            }
            CustomDrawable.setBackground(mNativeBannerLayout);
        }

        @Override
        public void onLoading(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onImp(String placeName, String source, String adType, String render, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onClick(String placeName, String source, String adType, String render, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , onDestroy : " + complexAds);
        }

        @Override
        public void onLoadFailed(String placeName, String source, String adType, String pid, int error) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , error : " + error);
        }

        @Override
        public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , item : " + item);
            runToast(item.toString());
        }

        @Override
        public void onCompleted(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onStarted(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }
    };

    private SimpleAdSdkListener mRewardListener = new SimpleAdSdkListener() {
        @Override
        public void onLoaded(String placeName, String source, String adType, String pid) {
            updateRewardButton();
        }

        @Override
        public void onImp(String placeName, String source, String adType, String render, String pid) {
            mRewardShowTimes++;
            updateRewardButton();
        }

        @Override
        public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
            mRewardGetTimes++;
            updateRewardButton();
        }

        @Override
        public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
            updateRewardButton();
        }

        @Override
        public void onUpdate(String placeName, String source, String adType, String pid) {
            updateRewardButton();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdSdk.get(this).stopAutoReward();
        AdSdk.get(this).destroy("banner_and_native");
    }

    private void updateRewardButton() {
        int loadedAdCount = AdSdk.get(this).getLoadedAdCount("reward_video");
        Log.v(Log.TAG, "loadedAdCount : " + loadedAdCount);
        mRewardButton.setText("加载成功数 : " + loadedAdCount + " (" + mRewardGetTimes + "/" + mRewardShowTimes + ")");
        mRewardButton.setEnabled(loadedAdCount > 0);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.auto_reward) {
            if (isChecked) {
                AdSdk.get(mContext).setAutoRewardListener("reward_video", mRewardListener);
                AdSdk.get(mContext).startAutoReward("reward_video", 5000, 0);
            } else {
                AdSdk.get(this).stopAutoReward();
            }
        }
    }

    private void showSpread() {
        AdSdk.get(this).loadComplexAds("show_spread", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String placeName, String source, String adType, String pid) {
                Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
                AdSdk.get(getApplicationContext()).showComplexAds(placeName);
            }

            @Override
            public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
                Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , onDestroy : " + complexAds);
            }
        });
    }

    class FullScreenAdListener extends SimpleAdSdkListener {
        private TextView textView;

        public FullScreenAdListener(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void onLoaded(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onImp(String placeName, String source, String adType, String render, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onUpdate(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }
    }

    private void updateLoadStatus(TextView textView, String placeName) {
        if (textView == null) {
            return;
        }
        boolean loaded = AdSdk.get(this).isComplexAdsLoaded(placeName);
        textView.setTextColor(loaded ? Color.RED : Color.BLACK);
    }
}
