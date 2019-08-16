package com.hauyu.adsdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.gekes.fvs.tdsvap.GFAPSD;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;


/**
 * Created by Administrator on 2018-10-31.
 */

public class NativeBannerActivity extends GFAPSD {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getRootLayout(Context context, String adType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ad_ntlayout, null);
        final FrameLayout banner1 = view.findViewById(R.id.banner1);
        final FrameLayout banner2 = view.findViewById(R.id.banner2);
        final FrameLayout banner3 = view.findViewById(R.id.banner3);
        AdSdk.get(this).loadAdView("banner1", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                AdSdk.get(getApplication()).showAdView(pidName, banner1);
            }
        });
        AdSdk.get(this).loadAdView("banner2", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                AdSdk.get(getApplication()).showAdView(pidName, banner2);
            }
        });
        AdSdk.get(this).loadAdView("banner3", new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                AdSdk.get(getApplication()).showAdView(pidName, banner3);
            }
        });
        return view;
    }

    @Override
    protected int getAdLayoutId(String adType) {
        return R.id.nt_layout;
    }

    @Override
    protected AdParams getAdParams() {
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
        return adParams;
    }
}
