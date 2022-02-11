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

import java.util.List;

public class ApplovinBindView extends BaseBindNativeView {
    private Params mParams;

    public MaxNativeAdView bindMaxNativeAdView(Activity activity, Params params, PidConfig pidConfig) {
        mParams = params;
        if (pidConfig != null) {
            int rootLayout = getBestNativeLayout(activity, pidConfig, mParams, Constant.AD_SDK_APPLOVIN);
            if (rootLayout > 0) {
                View rootView = LayoutInflater.from(activity).inflate(rootLayout, null);
                View ctaButton = rootView.findViewById(params.getAdAction());
                if (ctaButton != null) {
                    if (!(ctaButton instanceof Button)) {
                        Button button = new Button(activity);
                        if (ctaButton instanceof TextView) {
                            ColorStateList textColor = ((TextView) ctaButton).getTextColors();
                            button.setTextColor(textColor);
                        }
                        button.setClickable(false);
                        button.setSingleLine(true);
                        button.setBackground(ctaButton.getBackground());
                        replaceSrcViewToDstView(ctaButton, button);
                    }
                    ctaButton.setClickable(false);
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

    /**
     * 更新原生元素点击状态
     * @param maxNativeAdView
     * @param pidConfig
     */
    public void updateClickViewStatus(MaxNativeAdView maxNativeAdView, PidConfig pidConfig) {
        try {
            List<String> clickView = getClickView(pidConfig);
            if (clickView != null && !clickView.isEmpty()) {
                if (maxNativeAdView != null) {
                    View titleView = maxNativeAdView.getTitleTextView();
                    if (titleView != null) {
                        titleView.setClickable(isClickable(AD_TITLE, pidConfig));
                    }
                    View bodyView = maxNativeAdView.getBodyTextView();
                    if (bodyView != null) {
                        bodyView.setClickable(isClickable(AD_DETAIL, pidConfig));
                    }
                    View iconView = maxNativeAdView.getIconImageView();
                    if (iconView != null) {
                        iconView.setClickable(isClickable(AD_ICON, pidConfig));
                    }
                    View ctaView = maxNativeAdView.getCallToActionButton();
                    if (ctaView != null) {
                        ctaView.setClickable(isClickable(AD_CTA, pidConfig));
                    }
                    View mainView = maxNativeAdView.getMainView();
                    if (mainView != null) {
                        mainView.setClickable(isClickable(AD_MEDIA, pidConfig));
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void fillNativeAssets(MaxNativeAdView maxNativeAdView) {
        try {
            putValue(AD_TITLE, maxNativeAdView.getTitleTextView().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, maxNativeAdView.getBodyTextView().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, maxNativeAdView.getCallToActionButton().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_SOCIAL, maxNativeAdView.getAdvertiserTextView().getText().toString());
        } catch (Exception e) {
        }
    }
}
