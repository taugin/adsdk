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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hauyu.adsdk.demo.view.CustomDrawable;
import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.listener.AdLoaderFilter;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.rabbit.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity2 extends Activity {

    private static final int LAYOUT[] = new int[]{
            //R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium
    };
    private static final Map<String, Integer> LAYOUT_MAP;
    private static final Map<String, Integer> BANNER_MAP;

    private static final String BANNER_PREFIX = "banner_%s";
    private static final String INTERSTITIAL_PREFIX = "interstitial_%s";
    private static final String REWARD_PREFIX = "reward_%s";
    private static final String NATIVE_PREFIX = "native_%s";
    private static final String SPLASH_PREFIX = "splash_%s";

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

        BANNER_MAP = new HashMap<>();
        BANNER_MAP.put("NOSET", AdExtra.COMMON_BANNER);
        BANNER_MAP.put("BANNER", AdExtra.COMMON_BANNER);
        BANNER_MAP.put("FULL_BANNER", AdExtra.COMMON_FULL_BANNER);
        BANNER_MAP.put("LARGE_BANNER", AdExtra.COMMON_LARGE_BANNER);
        BANNER_MAP.put("LEADERBOARD", AdExtra.COMMON_LEADERBOARD);
        BANNER_MAP.put("MEDIUM_RECTANGLE", AdExtra.COMMON_MEDIUM_RECTANGLE);
        BANNER_MAP.put("WIDE_SKYSCRAPER", AdExtra.COMMON_WIDE_SKYSCRAPER);
        BANNER_MAP.put("SMART_BANNER", AdExtra.COMMON_SMART_BANNER);
        BANNER_MAP.put("ADAPTIVE_BANNER", AdExtra.COMMON_ADAPTIVE_BANNER);
    }

    private static final String TAG = "MA";
    private Context mContext;
    private RelativeLayout mNativeBannerLayout;
    private Spinner mAdSdkSpinner;
    private Spinner mAdLayoutSpinner;
    private Spinner mAdBannerSizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        mAdSdkSpinner = findViewById(R.id.ad_sdk_spinner);
        mAdLayoutSpinner = findViewById(R.id.ad_layout_spinner);
        mAdBannerSizeSpinner = findViewById(R.id.ad_banner_size_spinner);
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

    public void onClick(View v) {
        if (v.getId() == R.id.interstitial) {
            loadInterstitial((TextView) v);
        } else if (v.getId() == R.id.native_common) {
            loadNative((TextView) v);
        } else if (v.getId() == R.id.reward_video) {
            loadReward((TextView) v);
        } else if (v.getId() == R.id.banner) {
            loadBanner((TextView) v);
        } else if (v.getId() == R.id.splash) {
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
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String interstitialPlace = String.format(Locale.getDefault(), INTERSTITIAL_PREFIX, sdk.toLowerCase(Locale.getDefault()));
        if (AdSdk.get(mContext).isInterstitialLoaded(interstitialPlace)) {
            String loadedSdk = AdSdk.get(mContext).getLoadedSdk(interstitialPlace);
            Log.v(TAG, "loaded sdk : " + loadedSdk);
            AdSdk.get(mContext).showInterstitial(interstitialPlace);
        } else {
            AdSdk.get(mContext).loadInterstitial(interstitialPlace, new FullScreenAdListener(textView));
        }
    }

    private void loadSplash(TextView textView) {
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String splashPlace = String.format(Locale.getDefault(), SPLASH_PREFIX, sdk.toLowerCase(Locale.getDefault()));
        if (AdSdk.get(mContext).isSplashLoaded(splashPlace)) {
            AdSdk.get(mContext).showSplash(splashPlace);
        } else {
            AdSdk.get(mContext).loadSplash(splashPlace, new FullScreenAdListener(textView));
        }
    }

    private void loadReward(TextView textView) {
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String rewardPlace = String.format(Locale.getDefault(), REWARD_PREFIX, sdk.toLowerCase(Locale.getDefault()));
        if (AdSdk.get(mContext).isRewardedVideoLoaded(rewardPlace)) {
            AdSdk.get(mContext).showRewardedVideo(rewardPlace);
        } else {
            AdSdk.get(mContext).loadRewardedVideo(rewardPlace, new FullScreenAdListener(textView) {
                @Override
                public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
                    super.onRewarded(placeName, source, adType, pid, item);
                    Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , item : " + item);
                    runToast(item.toString());
                }
            });
        }
    }

    private void loadBanner(TextView textView) {
        AdParams.Builder builder = new AdParams.Builder();
        String banner = (String) mAdBannerSizeSpinner.getSelectedItem();
        builder.setBannerSize(AdExtra.AD_SDK_COMMON, BANNER_MAP.get(banner));
        AdParams adParams = builder.build();
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String bannerPlace = String.format(Locale.getDefault(), BANNER_PREFIX, sdk.toLowerCase(Locale.getDefault()));
        AdSdk.get(mContext).loadAdView(bannerPlace, adParams, mSimpleAdsdkListener);
    }

    private void loadNative(TextView textView) {
        AdParams.Builder builder = new AdParams.Builder();
        String layout = (String) mAdLayoutSpinner.getSelectedItem();
        int layoutId = 0;
        if ("Random".equalsIgnoreCase(layout)) {
            Integer [] values = new Integer[LAYOUT_MAP.size()];
            LAYOUT_MAP.values().toArray(values);
            layoutId = values[new Random().nextInt(values.length)];
        } else {
            layoutId = LAYOUT_MAP.get(layout.toLowerCase());
        }
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, layoutId);
        builder.setNativeTemplateWidth(AdExtra.AD_SDK_COMMON, Utils.dp2px(mContext, 200));
        AdParams adParams = builder.build();
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String nativePlace = String.format(Locale.getDefault(), NATIVE_PREFIX, sdk.toLowerCase(Locale.getDefault()));
        AdSdk.get(mContext).loadAdView(nativePlace, adParams, mSimpleAdsdkListener);
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
            runToast(item.toString());
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