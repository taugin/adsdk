package com.hauyu.adsdk.demo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hauyu.adsdk.demo.view.CustomDrawable;
import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.listener.OnAdFilterListener;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.rabbit.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final int LAYOUT[] = new int[]{
            //R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium
    };
    private static final Map<String, Integer> BANNER_MAP;

    private static final String BANNER_PREFIX = "banner_%s";
    private static final String INTERSTITIAL_PREFIX = "interstitial_%s";
    private static final String REWARD_PREFIX = "reward_%s";
    private static final String NATIVE_PREFIX = "native_%s";
    private static final String SPLASH_PREFIX = "splash_%s";

    static {
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
    private ViewGroup mSplashContainer;
    private TextView mLanguageView;
    private TextView mDebugView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0x1, 0, "CustomView");
        menu.add(0, 0x2, 0, "AdListView");
        menu.add(0, 0x3, 0, "VpnChecker");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            if (item.getItemId() == 0x1) {
                startActivity(new Intent(this, CustomViewActivity.class));
            } else if (item.getItemId() == 0x2) {
                startActivity(new Intent(this, AdListViewActivity.class));
            } else if (item.getItemId() == 0x3) {
                String toastString = Utils.isVPNConnected(this) ? "VPN已连接" : "VPN已断开";
                Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChangeLanguage.showLanguageDialogForTestMode(findViewById(R.id.change_language_layout));
        mLanguageView = findViewById(R.id.change_language);
        mNativeBannerLayout = findViewById(R.id.native_banner_layout);
        mAdSdkSpinner = findViewById(R.id.ad_sdk_spinner);
        mDebugView = findViewById(R.id.mediation_debugger);
        CharSequence[] entries = getResources().getStringArray(R.array.ad_sdk);
        if (entries != null) {
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_spinner_item, entries) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    return textView;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAdSdkSpinner.setAdapter(adapter);
        }
        mAdLayoutSpinner = findViewById(R.id.ad_layout_spinner);
        entries = getResources().getStringArray(R.array.ad_layout);
        if (entries != null) {
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_spinner_item, entries) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    return textView;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAdLayoutSpinner.setAdapter(adapter);
        }
        mAdBannerSizeSpinner = findViewById(R.id.ad_banner_size_spinner);
        entries = getResources().getStringArray(R.array.ad_banner_size);
        if (entries != null) {
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_spinner_item, entries) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    return textView;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mAdBannerSizeSpinner.setAdapter(adapter);
        }
        mSplashContainer = findViewById(R.id.splash_container);
        mAdSdkSpinner.setOnItemSelectedListener(this);
        mAdLayoutSpinner.setOnItemSelectedListener(this);
        mAdBannerSizeSpinner.setOnItemSelectedListener(this);
        mLanguageView.setText(ChangeLanguage.getCurrentLanguage(this, true));
        int position = 0;
        String tag = null;

        tag = (String) mAdSdkSpinner.getTag();
        position = (int) Utils.getLong(this, tag);
        mAdSdkSpinner.setSelection(position);

        tag = (String) mAdLayoutSpinner.getTag();
        position = (int) Utils.getLong(this, tag);
        mAdLayoutSpinner.setSelection(position);

        tag = (String) mAdBannerSizeSpinner.getTag();
        position = (int) Utils.getLong(this, tag);
        mAdBannerSizeSpinner.setSelection(position);
        setTitle(getTitle() + " - " + Utils.getCountry(this));
        mContext = getApplicationContext();
        AdSdk.get(this).setOnAdFilterListener(new OnAdFilterListener() {
            @Override
            public boolean doFilter(String placeName, String sdk, String type) {
                return false;
            }
        });
        String ram = getResources().getString(R.string.format_string, "76G");
        Log.v(Log.TAG, "ram : " + ram);
        String debug = mDebugView.getText().toString();
        String installPackage = getPackageManager().getInstallerPackageName(getPackageName());
        Log.v(Log.TAG, "installPackage : " + installPackage);
        String installApp = "";
        if (!TextUtils.isEmpty(installPackage)) {
            try {
                installApp = getPackageManager().getApplicationInfo(installPackage, 0).loadLabel(getPackageManager()).toString();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
            if (!TextUtils.isEmpty(installApp)) {
                installApp = "\n安装来源：" + installApp;
            }
            if (TextUtils.isEmpty(installApp)) {
                installApp = "";
            }
        }
        mDebugView.setText(debug + installApp);
        ProxyUtils.hookClick(findViewById(R.id.mediation_debugger));
        ProxyUtils.hookClick(findViewById(R.id.change_language));
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
        if (v.getId() == R.id.mediation_debugger) {
            AdSdk.get(mContext).showMediationDebugger();
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial((TextView) v);
        } else if (v.getId() == R.id.native_common) {
            loadNative((TextView) v);
        } else if (v.getId() == R.id.reward_video) {
            loadReward((TextView) v);
        } else if (v.getId() == R.id.banner) {
            loadBanner((TextView) v);
        } else if (v.getId() == R.id.splash) {
            loadSplash((TextView) v);
        } else if (v.getId() == R.id.change_language) {
            String value = AdSdk.get(this).getString("open_app", true);
            Log.v(Log.TAG, "open_app --> value : " + value);
            ChangeLanguage.showLanguageDialog(true);
        } else {
            String tag = (String) v.getTag();
            loadAdViewByLayout(tag, (TextView) v);
        }
    }

    private void loadAdViewByLayout(String tag, TextView textView) {
        if (AdSdk.get(this).isComplexAdsLoaded("for_native_layout")) {
            AdSdk.get(this).showComplexAds("for_native_layout");
        } else {
            AdParams.Builder builder = new AdParams.Builder();
            builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, tag);
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
        String interstitialPlace = String.format(Locale.ENGLISH, INTERSTITIAL_PREFIX, sdk.toLowerCase(Locale.ENGLISH));
        if (AdSdk.get(mContext).isInterstitialLoaded(interstitialPlace)) {
            String loadedSdk = AdSdk.get(mContext).getLoadedSdk(interstitialPlace);
            Log.v(TAG, "loaded sdk : " + loadedSdk);
            AdSdk.get(mContext).showInterstitial(interstitialPlace, "test_scene_interstitial");
        } else {
            AdSdk.get(mContext).loadInterstitial(interstitialPlace, new FullScreenAdListener(textView));
        }
    }

    private void loadSplash(TextView textView) {
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String splashPlace = String.format(Locale.ENGLISH, SPLASH_PREFIX, sdk.toLowerCase(Locale.ENGLISH));
        if (AdSdk.get(mContext).isSplashLoaded(splashPlace)) {
            mSplashContainer.setVisibility(View.VISIBLE);
            AdSdk.get(mContext).showSplash(splashPlace, mSplashContainer);
        } else {
            AdSdk.get(mContext).loadSplash(splashPlace, new FullScreenAdListener(textView));
        }
    }

    private void loadReward(TextView textView) {
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String rewardPlace = String.format(Locale.ENGLISH, REWARD_PREFIX, sdk.toLowerCase(Locale.ENGLISH));
        if (AdSdk.get(mContext).isRewardedVideoLoaded(rewardPlace)) {
            AdSdk.get(mContext).showRewardedVideo(rewardPlace, "test_scene_reward");
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
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        AdParams.Builder builder = new AdParams.Builder();
        String bannerPlace = String.format(Locale.ENGLISH, BANNER_PREFIX, sdk.toLowerCase(Locale.ENGLISH));
        if (AdSdk.get(mContext).isAdViewLoaded(bannerPlace)) {
            FrameLayout frameLayout = new FrameLayout(this);
            AdSdk.get(mContext).showAdView(bannerPlace, frameLayout);
            CustomDrawable.setBackground(frameLayout);
            showNativeAds(frameLayout);
        } else {
            String banner = (String) mAdBannerSizeSpinner.getSelectedItem();
            builder.setBannerSize(AdExtra.AD_SDK_COMMON, BANNER_MAP.get(banner));
            AdParams adParams = builder.build();
            AdSdk.get(mContext).loadAdView(bannerPlace, adParams, new FullScreenAdListener(textView));
        }
    }

    private AdParams getNativeParams() {
        AdParams.Builder builder = new AdParams.Builder();
        String layout = (String) mAdLayoutSpinner.getSelectedItem();
        String layoutStyle;
        String sdkArray[] = getResources().getStringArray(R.array.ad_layout);
        if ("Random".equalsIgnoreCase(layout)) {
            layoutStyle = sdkArray[new Random().nextInt(sdkArray.length - 4) + 4];
            builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, layoutStyle.toLowerCase());
        } else if ("Custom Small".equalsIgnoreCase(layout)) {
            builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.ad_common_native_card_small);
            builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
            builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
            builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
            builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
            builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
            builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
            builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        } else if ("Custom Medium".equalsIgnoreCase(layout)) {
            builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.ad_common_native_card_medium);
            builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
            builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
            builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
            builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
            builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
            builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
            builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        } else if ("Custom Large".equalsIgnoreCase(layout)) {
            builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, R.layout.ad_common_native_card_large);
            builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
            builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
            builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
            builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
            builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
            builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
            builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        } else {
            layoutStyle = layout;
            builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, layoutStyle.toLowerCase());
        }
        builder.setNativeTemplateWidth(AdExtra.AD_SDK_COMMON, Utils.dp2px(mContext, 200));
        AdParams adParams = builder.build();
        return adParams;
    }

    private void loadNative(TextView textView) {
        AdParams adParams = getNativeParams();
        String sdk = (String) mAdSdkSpinner.getSelectedItem();
        String nativePlace = String.format(Locale.ENGLISH, NATIVE_PREFIX, sdk.toLowerCase(Locale.ENGLISH));
        Switch switchButton = findViewById(R.id.mediation_template);
        if (switchButton.isChecked()) {
            nativePlace += "_template";
        }
        if (AdSdk.get(mContext).isAdViewLoaded(nativePlace)) {
            FrameLayout frameLayout = new FrameLayout(this);
            AdSdk.get(mContext).showAdView(nativePlace, null, adParams, frameLayout);
            CustomDrawable.setBackground(frameLayout);
            showNativeAds(frameLayout);
            return;
        }
        AdSdk.get(mContext).loadAdView(nativePlace, adParams, new FullScreenAdListener(textView));
    }

    private void showNativeAds(ViewGroup viewGroup) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        dialog.setContentView(viewGroup);
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = getResources().getDisplayMetrics().widthPixels;
        params.height = -2;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setDimAmount(0.8f);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#88000000")));
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
        public void onImp(String placeName, String source, String adType, String network, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
        }

        @Override
        public void onClick(String placeName, String source, String adType, String network, String pid) {
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            String prefKey = (String) parent.getTag();
            Utils.putLong(this, prefKey, position);
        } catch (Exception e) {
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
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
        public void onLoadFailed(String placeName, String source, String adType, String pid, int error) {
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onShow(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onImp(String placeName, String source, String adType, String network, String pid) {
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
            mSplashContainer.setVisibility(View.GONE);
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
