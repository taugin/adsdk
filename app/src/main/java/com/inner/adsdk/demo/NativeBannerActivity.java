package com.inner.adsdk.demo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appub.ads.a.FSA;
import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;

/**
 * Created by Administrator on 2018-10-31.
 */

public class NativeBannerActivity extends FSA {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.width = (int) (dm.widthPixels * 0.9f);
        params.height = (int) (dm.heightPixels * 0.6f);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    protected ViewGroup getRootLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(context);
        tv.setText(android.R.string.ok);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(50);
        tv.setGravity(Gravity.CENTER);
        layout.addView(tv);
        RelativeLayout rl = new RelativeLayout(context);
        rl.setGravity(Gravity.CENTER);
        rl.setId(0x1000);
        layout.addView(rl);
        return null;
    }

    @Override
    protected int getAdLayoutId() {
        return 0;//0x1000;
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
