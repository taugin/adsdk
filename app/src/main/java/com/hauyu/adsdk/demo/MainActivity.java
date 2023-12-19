package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.komob.adsdk.AdExtra;
import com.komob.adsdk.AdImpData;
import com.komob.adsdk.AdParams;
import com.komob.adsdk.AdReward;
import com.komob.adsdk.AdSdk;
import com.komob.adsdk.OnAdFilterListener;
import com.komob.adsdk.SimpleAdSdkListener;
import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.demo.view.CustomDrawable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import me.samlss.broccoli.Broccoli;
import me.samlss.broccoli.BroccoliGradientDrawable;
import me.samlss.broccoli.PlaceholderParameter;

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
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

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
    private TextView mTimeView;
    private Timer mTimer;
    private CharSequence[] mEntries;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0x1, 0, "CustomView");
        menu.add(0, 0x2, 0, "AdListView");
        menu.add(0, 0x3, 0, "VpnChecker");
        menu.add(0, 0x4, 0, "ShowAdImp");
        menu.add(0, 0x5, 0, "CheckRunningApp");
        menu.add(0, 0x6, 0, "CheckRoundCpm");
        menu.add(0, 0x7, 0, "GenerateProguard");
        menu.add(0, 0x8, 0, "ParseRss");
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
            } else if (item.getItemId() == 0x4) {
                showAdImpression();
            } else if (item.getItemId() == 0x5) {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        PackageManager packageManager = getPackageManager();
                        List<PackageInfo> list = App.getRunningPackageList(getApplicationContext());
                        for (PackageInfo packageInfo : list) {
                            Log.v(Log.TAG, "app : " + packageInfo.applicationInfo.loadLabel(packageManager));
                        }
                    }
                });
            } else if (item.getItemId() == 0x6) {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(Log.TAG, "start check round cpm");
                        Map<String, Integer> map = new HashMap<>();
                        int maxImp = 1000000;
                        double calc1 = 0f;
                        double calc2 = 0f;
                        Random random = new Random(System.currentTimeMillis());
                        for (double index = 0.000001; index < 10000; ) {
                            double cpm = index;//index < 500000 ? random.nextDouble() : random.nextDouble() * 1000;
                            calc1 += cpm;
                            String roundCpm = Utils.calcRoundCpm(cpm);
                            calc2 += Double.parseDouble(roundCpm);
                            try {
                                Integer integer = map.get(roundCpm);
                                int times = integer != null ? integer.intValue() : 0;
                                map.put(roundCpm, times + 1);
                            } catch (Exception e) {
                                Log.v(Log.TAG, "error : " + e);
                            }
                            if (index < 0.0001f) {
                                index += 0.000001f;
                            } else if (index < 0.01f) {
                                index += 0.0001f;
                            } else if (index < 1f) {
                                index += 0.01f;
                            } else if (index < 10f) {
                                index += 1f;
                            } else if (index < 100f) {
                                index += 1f;
                            } else if (index < 500f) {
                                index += 1f;
                            } else if (index < 1000f) {
                                index += 10f;
                            } else if (index < 10000f) {
                                index += 100f;
                            }
                        }
                        TreeSet<String> treeSet = new TreeSet<String>(new Comparator<String>() {
                            @Override
                            public int compare(String t1, String t2) {
                                return Double.compare(Double.parseDouble(t1), Double.parseDouble(t2));
                            }
                        });
                        treeSet.addAll(map.keySet());
                        for (String s : treeSet) {
                            Log.v(Log.TAG, "round cpm : " + s + " , count : " + map.get(s));
                        }
                        Log.v(Log.TAG, "count : " + map.size() + " , calc1 : " + calc1 + " , calc2 : " + calc2);
                    }
                });
            } else if (item.getItemId() == 0x7) {
                generateProguardDict();
            } else if (item.getItemId() == 0x8) {
                RssParser.doUrl(this);
            }
        }
        return true;
    }


    public static void disableSyncProvider(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, SplashActivity.class);
            int enableStatus = packageManager.getComponentEnabledSetting(componentName);
            if (enableStatus != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        } catch (Exception | Error e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChangeLanguage.showLanguageDialogForTestMode(findViewById(R.id.change_language_layout));
        mLanguageView = findViewById(R.id.change_language);
        mTimeView = findViewById(R.id.time_view);
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
        mEntries = entries = getResources().getStringArray(R.array.ad_layout);
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
            mAdLayoutSpinner.setOnItemSelectedListener(this);
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
        String debugValue = "[c:" + Utils.isUsbConnected(this) + "|d:" + Utils.isDebugEnabled(this) + "]";
        setTitle(getTitle() + " - " + debugValue);
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
        timer();
    }

    private void timer() {
        TimerTask tTask = new TimerTask() {

            @Override
            public void run() {
                long time = AdSdk.get(getApplicationContext()).getCurrentTimeMillis();
                mTimeView.setText(sdf.format(time));
            }
        };
        mTimer = new Timer();
        mTimer.schedule(tTask, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
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
            String value = AdSdk.get(this).getString("md5:open_app");
            Log.v(Log.TAG, "open_app --> value : " + value);
            ChangeLanguage.showLanguageDialog(true);
        } else if (v.getId() == R.id.show_max_interstitial) {
            String maxPlaceName = AdSdk.get(this).getMaxPlaceName(Constant.TYPE_INTERSTITIAL);
            if (!TextUtils.isEmpty(maxPlaceName)) {
                AdSdk.get(this).showInterstitial(maxPlaceName, "scene_max_interstitial");
            } else {
                runToast("No loaded place name");
            }
        } else if (v.getId() == R.id.show_max_native) {
            String maxPlaceName = AdSdk.get(this).getMaxPlaceName(Constant.TYPE_NATIVE);
            if (!TextUtils.isEmpty(maxPlaceName)) {
                ViewGroup frameLayout = new LinearLayout(this);
                AdParams adParams = getNativeParams();
                adParams.setSceneName("scene_show_max_native");
                AdSdk.get(mContext).showAdView(maxPlaceName, adParams, frameLayout);
                showNativeAds(frameLayout);
            } else {
                runToast("No loaded place name");
            }
        } else if (v.getId() == R.id.show_max_splash) {
            String maxPlaceName = AdSdk.get(this).getMaxPlaceName(Constant.TYPE_SPLASH);
            if (!TextUtils.isEmpty(maxPlaceName)) {
                mSplashContainer.setVisibility(View.VISIBLE);
                AdSdk.get(this).showSplash(maxPlaceName, mSplashContainer, "scene_max_splash_123321");
                mSplashContainer.setVisibility(View.VISIBLE);
            } else {
                runToast("No loaded place name");
            }
        } else if (v.getId() == R.id.show_max_reward) {
            String maxPlaceName = AdSdk.get(this).getMaxPlaceName(Constant.TYPE_REWARD);
            if (!TextUtils.isEmpty(maxPlaceName)) {
                AdSdk.get(this).showRewardedVideo(maxPlaceName, "scene_max_reward");
            } else {
                runToast("No loaded place name");
            }
        } else if (v.getId() == R.id.show_complex_ads) {
            String maxPlaceName = AdSdk.get(this).getMaxPlaceName();
            AdSdk.get(this).showComplexAds(maxPlaceName, "scene_complex_ads_789987");
        } else if (v.getId() == R.id.native_interstitial) {
            String tag = (String) v.getTag();
            loadAdViewByLayout(tag, (TextView) v);
        }
    }

    private void loadAdViewByLayout(String tag, TextView textView) {
        if (AdSdk.get(this).isComplexAdsLoaded("for_native_layout")) {
            AdSdk.get(this).showComplexAds("for_native_layout", "scene_for_native_layout");
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
            AdSdk.get(mContext).showSplash(splashPlace, mSplashContainer, "scene_show_splash_456654");
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
            adParams.setSceneName("scene_show_native");
            AdSdk.get(mContext).showAdView(nativePlace, adParams, frameLayout);
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
        public void onImpression(String placeName, String source, String adType, String network, String pid, String sceneName) {
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
        if (parent == mAdLayoutSpinner) {
            onSpanItemSelect(parent, view, position, id);
            return;
        }
        try {
            String prefKey = (String) parent.getTag();
            Utils.putLong(this, prefKey, position);
        } catch (Exception e) {
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void onSpanItemSelect(AdapterView<?> parent, View view, int position, long id) {
        String layout = mEntries[position].toString();
        int resLayout = 0;
        if ("Random".equalsIgnoreCase(layout)) {
        } else if ("Custom Small".equalsIgnoreCase(layout)) {
            resLayout = R.layout.ad_common_native_card_small;
        } else if ("Custom Medium".equalsIgnoreCase(layout)) {
            resLayout = R.layout.ad_common_native_card_medium;
        } else if ("Custom Large".equalsIgnoreCase(layout)) {
            resLayout = R.layout.ad_common_native_card_large;
        } else {
            layout = layout.toLowerCase(Locale.ENGLISH);
            resLayout = getResources().getIdentifier("rab_card_" + layout, "layout", getPackageName());
        }
        mNativeBannerLayout.removeAllViews();
        if (resLayout != 0) {
            View adView = LayoutInflater.from(this).inflate(resLayout, null);
            mNativeBannerLayout.addView(adView);
            showBroccoli(adView);
        }
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
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType + " , error : " + error);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onShow(String placeName, String source, String adType, String pid) {
            Log.d(TAG, "placeName : " + placeName + " , source : " + source + " , adType : " + adType);
            updateLoadStatus(textView, placeName);
        }

        @Override
        public void onImpression(String placeName, String source, String adType, String network, String pid, String sceneName) {
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


    public void showAdImpression() {
        Activity activity = ActivityMonitor.get(mContext).getTopActivity();
        Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        dialog.setContentView(createDialogView(mContext));
        dialog.show();
    }

    private View createDialogView(Context context) {
        List<Map<String, Object>> mapList = DBManager.get(context).queryAllAdType();
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout titleLayout = new LinearLayout(context);
        rootLayout.addView(titleLayout, -1, -2);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        int height = Utils.dp2px(context, 24);
        double totalRevenue = DBManager.get(context).queryAdRevenue();
        TextView totalTextView = null;
        totalTextView = new TextView(context);
        totalTextView.setGravity(Gravity.CENTER);
        totalTextView.setTextColor(Color.BLACK);
        totalTextView.setText("REVENUE : " + BigDecimal.valueOf(totalRevenue).setScale(3, RoundingMode.HALF_EVEN).toPlainString());
        totalTextView.setBackgroundColor(Color.GREEN);
        titleLayout.addView(totalTextView, -1, height);

        LinearLayout adTypeLayout = new LinearLayout(context);
        adTypeLayout.setBackgroundColor(Color.RED);
        int padding = Utils.dp2px(context, 1);
        adTypeLayout.setPadding(padding, padding, padding, padding);
        adTypeLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(padding, padding, padding, padding);
        rootLayout.addView(adTypeLayout, layoutParams);

        if (mapList != null && !mapList.isEmpty()) {
            int size = mapList.size();
            int sizeWithHeader = size + 1;
            for (int index = 0; index < sizeWithHeader; index++) {
                LinearLayout rowLayout = new LinearLayout(context);
                adTypeLayout.addView(rowLayout, -1, -1);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, height);
                params.weight = 1;
                TextView typeView = new TextView(context);
                typeView.setBackgroundColor(Color.WHITE);
                typeView.setGravity(Gravity.CENTER);
                TextView revenueView = new TextView(context);
                revenueView.setBackgroundColor(Color.WHITE);
                revenueView.setGravity(Gravity.CENTER);
                TextView impView = new TextView(context);
                impView.setBackgroundColor(Color.WHITE);
                impView.setGravity(Gravity.CENTER);
                rowLayout.addView(typeView, params);
                rowLayout.addView(revenueView, params);
                rowLayout.addView(impView, params);
                if (index == 0) {
                    typeView.setText("AdType");
                    revenueView.setText("Revenue");
                    impView.setText("Impression");
                } else {
                    Map<String, Object> map = mapList.get(index - 1);
                    if (map != null) {
                        double revenue = (double) map.get("ad_type_revenue");
                        typeView.setText(String.valueOf(map.get("ad_type")));
                        revenueView.setText(BigDecimal.valueOf(revenue).setScale(3, RoundingMode.HALF_EVEN).toPlainString());
                        impView.setText(String.valueOf(map.get("ad_type_impression")));
                    }
                }
            }
        }

        ListView listView = new ListView(context);
        rootLayout.addView(listView, -1, -1);
        List<AdImpData> list = DBManager.get(context).queryAllImps();
        int size = list != null ? list.size() : 0;
        totalTextView.setText(totalTextView.getText() + " , IMP : " + size + "");
        if (list != null && !list.isEmpty()) {
            ArrayAdapter<AdImpData> adapter = new ArrayAdapter<AdImpData>(context, android.R.layout.simple_list_item_1, list) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView adapterView = (TextView) super.getView(position, convertView, parent);
                    adapterView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    AdImpData adImpData = getItem(position);
                    String str = "<font color=red><big>" + (position + 1) + ".</big></font> " + adImpData.getPlacement()
                            + "<br>" + adImpData.getUnitName() + "<font color=red>*</font></br>"
                            + "<br>[<font color=red>" + adImpData.getPlatform() + "</font>][<font color='#a00'>" + adImpData.getNetwork() + "</font>]"
                            + "<br>[<font color=red>" + adImpData.getAdType() + "</font>]"
                            + "<br>" + adImpData.getUnitId()
                            + "<br>" + (adImpData.getNetworkPid() != null ? adImpData.getNetworkPid() : "-")
                            + "<br><font color=red>Revenue:</font>" + adImpData.getValue()
                            + "<br><font color=red>Scene:</font>" + adImpData.getPlacement();
                    adapterView.setText(Html.fromHtml(str));
                    return adapterView;
                }
            };
            listView.setAdapter(adapter);
        }
        return rootLayout;
    }

    private void generateProguardDict() {
        List<String> dict = new ArrayList<>();
        List<String> templateDict = Arrays.asList("O", "0");
        int len = 10;
        Random random = new Random(System.currentTimeMillis());
        for (; dict.size() < 30; ) {
            StringBuilder builder = new StringBuilder();
            builder.append("O");
            for (int index = 0; index < len - 1; index++) {
                builder.append(templateDict.get(random.nextInt(templateDict.size())));
            }
            String output = builder.toString();
            if (!dict.contains(output)) {
                dict.add(output);
            }
        }
        for (String s : dict) {
            Log.v(Log.TAG, "s : " + s);
        }
    }

    public static void showBroccoli(View adLayoutView) {
        try {
            Broccoli broccoli = new Broccoli();
            broccoli.addPlaceholders(adLayoutView.findViewById(R.id.rab_native_icon)
                    , adLayoutView.findViewById(R.id.rab_native_title)
                    , adLayoutView.findViewById(R.id.rab_native_detail)
                    , adLayoutView.findViewById(R.id.rab_native_action_btn));

            broccoli.addPlaceholder(new PlaceholderParameter.Builder()
                    .setDrawable(new BroccoliGradientDrawable(Color.parseColor("#DDDDDD"),
                            Color.parseColor("#CCCCCC"), 0, 1000, new LinearInterpolator()))
                    .setView(adLayoutView.findViewById(R.id.rab_native_cover_info))
                    .build());
            broccoli.show();
        } catch (Exception e) {
        }
    }
}
