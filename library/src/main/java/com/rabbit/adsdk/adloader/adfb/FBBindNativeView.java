package com.rabbit.adsdk.adloader.adfb;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/11.
 */

public class FBBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindFBNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindFBNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindFBNative adContainer == null###");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        if (rootLayout <= 0 && mParams.getNativeCardStyle() > 0) {
            rootLayout = getAdViewLayout(adContainer.getContext(), mParams.getNativeCardStyle(), pidConfig);
            bindParamsViewId(mParams);
        }

        if (rootLayout > 0) {
            bindNativeViewWithRootView(adContainer, rootLayout, nativeAd, pidConfig);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }

    /**
     * 外部传入ViewRoot
     *
     * @param rootLayout
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, NativeAd nativeAd, PidConfig pidConfig) {
        if (rootLayout <= 0) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootLayout == 0x0");
            return;
        }
        if (nativeAd == null || !nativeAd.isAdLoaded()) {
            Log.v(Log.TAG, "bindNativeViewWithRootView nativeAd == null or nativeAd.isAdLoaded() == false");
            return;
        }
        if (pidConfig == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView pidConfig == null");
            return;
        }

        if (mParams == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView mParams == null");
            return;
        }
        // 恢复icon图标
        View rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);

        NativeAdLayout adView = new NativeAdLayout(rootView.getContext());
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        adView.addView(rootView);

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView socialView = rootView.findViewById(mParams.getAdSocial());
        TextView bodyView = rootView.findViewById(mParams.getAdDetail());
        TextView btnAction = rootView.findViewById(mParams.getAdAction());
        ViewGroup adChoiceContainer = rootView.findViewById(mParams.getAdChoices());
        MediaView mediaView = createMediaView(rootView.getContext());
        ViewGroup mediaLayout = rootView.findViewById(mParams.getAdMediaView());

        if (mediaLayout != null && mediaView != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            mediaLayout.addView(mediaView, params);
            mediaLayout.setVisibility(View.VISIBLE);
        }

        if (mediaView != null) {
            mediaView.setVisibility(View.VISIBLE);
        }
        if (imageCover != null) {
            imageCover.setVisibility(View.GONE);
        }

        // 可点击的视图
        List<View> actionView = new ArrayList<View>();

        if (nativeAd != null && nativeAd.isAdLoaded()) {
            MediaView iconView = createIconView(rootView.getContext(), icon);
            if (iconView != null) {
                if (isClickable(AD_ICON, pidConfig)) {
                    actionView.add(iconView);
                }
                iconView.setVisibility(View.VISIBLE);
            }

            // Download and setting the cover image.
            if (mediaView != null && isClickable(AD_MEDIA, pidConfig)) {
                actionView.add(mediaView);
            }

            // Add adChoices icon
            if (adChoiceContainer != null) {
                AdOptionsView adOptionsView = new AdOptionsView(rootView.getContext(), nativeAd, adView);
                adChoiceContainer.addView(adOptionsView, 0);
                adChoiceContainer.setPadding(0, 0, 0, 0);
                adChoiceContainer.setBackgroundColor(Color.parseColor("#88FFFFFF"));
            }

            if (titleView != null) {
                titleView.setText(nativeAd.getAdHeadline());
                if (isClickable(AD_TITLE, pidConfig)) {
                    actionView.add(titleView);
                }

                if (!TextUtils.isEmpty(nativeAd.getAdHeadline())) {
                    titleView.setVisibility(View.VISIBLE);
                }
            }

            if (bodyView != null) {
                bodyView.setText(nativeAd.getAdBodyText());
                if (isClickable(AD_DETAIL, pidConfig)) {
                    actionView.add(bodyView);
                }

                if (!TextUtils.isEmpty(nativeAd.getAdBodyText())) {
                    bodyView.setVisibility(View.VISIBLE);
                }
            }

            if (btnAction != null) {
                btnAction.setText(nativeAd.getAdCallToAction());
                if (isClickable(AD_CTA, pidConfig)) {
                    actionView.add(btnAction);
                }

                if (!TextUtils.isEmpty(nativeAd.getAdCallToAction())) {
                    btnAction.setVisibility(View.VISIBLE);
                }
            }

            if (socialView != null) {
                socialView.setText(nativeAd.getAdSocialContext());
                if (isClickable(AD_SOCIAL, pidConfig)) {
                    actionView.add(socialView);
                }

                if (!TextUtils.isEmpty(nativeAd.getAdSocialContext())) {
                    socialView.setVisibility(View.VISIBLE);
                }
            }

            if (rootView != null) {
                nativeAd.registerViewForInteraction(rootView, mediaView, iconView, actionView);
            }
            Log.iv(Log.TAG, "clickable view : " + pidConfig.getClickView());
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(adView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
            putAdvertiserInfo(nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private MediaView createMediaView(Context context) {
        try {
            return new MediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private MediaView createIconView(Context context, ImageView icon) {
        MediaView iconView = null;
        if (icon != null) {
            iconView = createMediaView(context);
            if (iconView != null) {
                iconView.setId(icon.getId());
                iconView.setVisibility(icon.getVisibility());
                ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
                android.widget.RelativeLayout.LayoutParams iconViewParams = new android.widget.RelativeLayout.LayoutParams(iconParams.width, iconParams.height);
                if (iconParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) iconParams;
                    iconViewParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
                }
                if (iconParams instanceof android.widget.RelativeLayout.LayoutParams) {
                    android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) iconParams;
                    int[] rules = mainImageViewRelativeLayoutParams.getRules();

                    for (int i = 0; i < rules.length; ++i) {
                        iconViewParams.addRule(i, rules[i]);
                    }
                }
                ViewGroup viewGroup = (ViewGroup) icon.getParent();
                if (viewGroup != null) {
                    int index = viewGroup.indexOfChild(icon);
                    viewGroup.removeView(icon);
                    viewGroup.addView(iconView, index, iconViewParams);
                }
            }
        }
        return iconView;
    }

    private void putAdvertiserInfo(NativeAd nativeAd) {
        try {
            putValue(AD_TITLE, nativeAd.getAdHeadline());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, nativeAd.getAdBodyText());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ADVERTISER, nativeAd.getAdvertiserName());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CHOICES, nativeAd.getAdChoicesText());
        } catch (Exception e) {
        }
        try {
            putValue(AD_COVER, nativeAd.getAdCoverImage().getUrl());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, nativeAd.getAdCallToAction());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ICON, nativeAd.getAdIcon().getUrl());
        } catch (Exception e) {
        }
        try {
            putValue(AD_RATE, nativeAd.getAdStarRating().toString());
        } catch (Exception e) {
        }
    }
}