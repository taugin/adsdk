package com.inner.adsdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.AdReward;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.listener.SimpleAdSdkListener;

import java.util.Random;

public class MainActivity extends Activity {

    private static final int LAYOUT[] = new int[] {
            R.layout.ad_common_native_card_small,
            R.layout.ad_common_native_card_medium,
            R.layout.ad_common_native_card_medium_upbtn
    };
    private static final int CARDID[] = new int[] {
            AdExtra.NATIVE_CARD_SMALL,
            AdExtra.NATIVE_CARD_MEDIUM,
            AdExtra.NATIVE_CARD_LARGE
    };
    private static final String TAG = "MA";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        AdSdk.get(mContext).init();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.gt_outer) {
            loadGtOuter();
        } else if (v.getId() == R.id.interstitial) {
            loadInterstitial();
        } else if (v.getId() == R.id.complex) {
            loadAdComplex();
        } else if (v.getId() == R.id.native_common1) {
            loadAdViewCommon(R.layout.ad_common_native_card_medium);
        } else if (v.getId() == R.id.native_common2) {
            loadAdViewCommon(R.layout.ad_common_native_card_medium_upbtn);
        } else if (v.getId() == R.id.native_common3) {
            loadAdViewCommon(R.layout.ad_common_native_card_small);
        } else if (v.getId() == R.id.webmob_interstitial) {
            AdSdk.get(mContext).loadInterstitial("interstitial", new SimpleAdSdkListener(){
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    AdSdk.get(mContext).showInterstitial(pidName);
                }
            });
        } else if (v.getId() == R.id.webmob_native) {
            loadAdViewCommon(LAYOUT[new Random(System.currentTimeMillis()).nextInt(LAYOUT.length)]);
        } else if (v.getId() == R.id.reward_video) {
            AdSdk.get(this).loadInterstitial("reward_video", new SimpleAdSdkListener(){

                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    AdSdk.get(getBaseContext()).showInterstitial(pidName);
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
            });
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
        AdSdk.get(mContext).loadInterstitial("interstitial", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                AdSdk.get(mContext).showInterstitial(pidName);
            }
        });
    }

    private void loadAdComplex() {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        int layoutId = LAYOUT[new Random(System.currentTimeMillis()).nextInt(LAYOUT.length)];
        if (layoutId == R.layout.ad_common_native_card_small) {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_LARGE_BANNER);
        } else {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        }
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
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
                if ("interstitial".equals(adType) || "reward".equals(adType)) {
                    AdSdk.get(mContext).showComplexAds(pidName, null);
                } else {
                    showAdView(pidName);
                }
            }
        });
    }

    private void loadAdViewCommon(int layoutId) {
        AdParams.Builder builder = new AdParams.Builder();
        //  设置外部布局参数
        if (layoutId == R.layout.ad_common_native_card_small) {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_LARGE_BANNER);
        } else {
            builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        }
        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.common_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.common_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.common_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.common_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.common_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.common_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.common_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.common_media_cover);
        AdParams adParams = builder.build();

        AdSdk.get(mContext).loadAdView("banner_and_native", adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                Log.d(TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                showAdView(pidName);
            }
        });
    }

    private void showAdView(String pidName) {
        RelativeLayout layout = new RelativeLayout(this);
        layout.setGravity(Gravity.CENTER);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = getResources().getDisplayMetrics().widthPixels;
        dialog.getWindow().setAttributes(params);
        AdSdk.get(this).showAdView(pidName, layout);
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
}
