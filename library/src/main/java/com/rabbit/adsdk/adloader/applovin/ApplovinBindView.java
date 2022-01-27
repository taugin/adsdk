package com.rabbit.adsdk.adloader.applovin;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

public class ApplovinBindView extends BaseBindNativeView {
    private Params mParams;
    public MaxNativeAdView bindMaxNativeAdView(Activity activity, Params params, PidConfig pidConfig) {
        mParams = params;
        if (pidConfig != null) {
            int rootLayout = getBestNativeLayout(activity, pidConfig, mParams, Constant.AD_SDK_APPLOVIN);
            if (rootLayout > 0) {
                View rootView = LayoutInflater.from(activity).inflate(rootLayout, null);
                View ctaButton = rootView.findViewById(params.getAdAction());
                if (!(ctaButton instanceof Button)) {
                    Button button = new Button(activity);
                    if (ctaButton instanceof TextView) {
                        ColorStateList textColor = ((TextView) ctaButton).getTextColors();
                        button.setTextColor(textColor);
                    }
                    button.setSingleLine(true);
                    button.setBackground(ctaButton.getBackground());
                    replaceSrcViewToDstView(ctaButton, button);
                }
                MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(rootView)
                        .setTitleTextViewId(params.getAdTitle())
                        .setBodyTextViewId(params.getAdDetail())
                        .setAdvertiserTextViewId(params.getAdSocial())
                        .setIconImageViewId(params.getAdIcon())
                        .setMediaContentViewGroupId(params.getAdMediaView())
                        .setOptionsContentViewGroupId(params.getAdChoices())
                        .setCallToActionButtonId(params.getAdAction())
                        .build();
                MaxNativeAdView maxNativeAdView = new MaxNativeAdView(binder, activity);
                updateCtaButtonBackground(maxNativeAdView, pidConfig, mParams);
                return maxNativeAdView;
            } else {
                Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
            }
        }
        return new MaxNativeAdView("", activity);
    }
}
