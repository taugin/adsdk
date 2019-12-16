package com.hauyu.adsdk.adloader.adfb;

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
import com.gekes.fvs.tdsvap.R;
import com.hauyu.adsdk.adloader.base.BaseBindNativeView;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

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
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
        } else if (rootLayout > 0) {
            if (adContainer != null && adContainer.getContext() != null) {
                rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
                bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
            }
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, nativeAd, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find fb native layout###");
        }
        onAdViewShown(adContainer, pidConfig, mParams);
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.had_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.had_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.had_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.had_card_large;
        } else if (template == Constant.NATIVE_CARD_FULL) {
            layoutId = getFullLayout(pidConfig);
        } else if (template == Constant.NATIVE_CARD_TINY) {
            layoutId = R.layout.had_card_tiny;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(adContainer, rootView, nativeAd, pidConfig);
    }

    /**
     * 使用模板显示原生广告
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithTemplate(ViewGroup adContainer, View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootView == null");
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
        try {
            restoreIconView(rootView, pidConfig.getSdk(), mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        NativeAdLayout adView = new NativeAdLayout(rootView.getContext());
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        adView.addView(rootView);

        restoreAdViewContent(mParams, rootView);

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        TextView subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView socialView = rootView.findViewById(mParams.getAdSocial());
        TextView detail = rootView.findViewById(mParams.getAdDetail());
        TextView btnAction = rootView.findViewById(mParams.getAdAction());
        ViewGroup adChoiceContainer = rootView.findViewById(mParams.getAdChoices());
        MediaView mediaView = createMediaView(rootView.getContext());
        ViewGroup mediaLayout = rootView.findViewById(mParams.getAdMediaView());

        TextView bodyView = detail != null ? detail : subTitleView;

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
                titleView.setText(nativeAd.getAdvertiserName());
                if (isClickable(AD_TITLE, pidConfig)) {
                    actionView.add(titleView);
                }

                if (!TextUtils.isEmpty(nativeAd.getAdvertiserName())) {
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
            Log.iv(Log.TAG, "clickable view : " + pidConfig.getClickViews());
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(adView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
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
}