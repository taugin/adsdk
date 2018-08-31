package com.hauyu.adsdk.adloader.appnext;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appnext.nativeads.MediaView;
import com.appnext.nativeads.NativeAd;
import com.appnext.nativeads.NativeAdView;
import com.hauyu.adsdk.R;
import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.Params;
import com.hauyu.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class AppnextBindNativeView {
    private Params mParams;

    public void bindAppnextNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindAppnextNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindAppnextNative adContainer == null###");
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
            Log.e(Log.TAG, "Can not find appnext native layout###");
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.native_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(adContainer, rootView, nativeAd, pidConfig);
        try {
            adContainer.removeAllViews();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, new Throwable());
        }
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
        if (nativeAd == null/* || !nativeAd.isLoaded()*/) {
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

        NativeAdView adView = new NativeAdView(rootView.getContext());
        FrameLayout rootLayout = (FrameLayout) rootView;
        View childView = rootLayout.getChildAt(0);
        rootLayout.removeView(childView);
        adView.addView(childView);
        rootLayout.addView(adView);

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        TextView subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView socialView = rootView.findViewById(mParams.getAdSocial());
        TextView detail = rootView.findViewById(mParams.getAdDetail());
        AppCompatButton btnAction = rootView.findViewById(mParams.getAdAction());
        ViewGroup adChoiceContainer = rootView.findViewById(mParams.getAdChoices());
        MediaView mediaView = createMediaView(rootView.getContext());
        ViewGroup mediaLayout = rootView.findViewById(mParams.getAdMediaView());

        if (mediaLayout != null) {
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

        if (nativeAd != null/* && nativeAd.isLoaded()*/) {
            if (icon != null) {
                nativeAd.downloadAndDisplayImage(icon, nativeAd.getIconURL());
            }

            // Download and setting the cover image.
            if (mediaView != null) {
                nativeAd.setMediaView(mediaView);
                actionView.add(mediaView);
            }

            // Add adChoices icon
            if (adChoiceContainer != null) {
                // AdChoicesView adChoicesView = new AdChoicesView(rootView.getContext(), nativeAd, true);
                // adChoiceContainer.addView(adChoicesView, 0);
            }

            if (titleView != null) {
                titleView.setText(nativeAd.getAdTitle());
                actionView.add(titleView);
            }

            if (subTitleView != null) {
                subTitleView.setText(nativeAd.getAdDescription());
                actionView.add(subTitleView);
            }
            if (detail != null) {
                detail.setText(nativeAd.getAdDescription());
                actionView.add(detail);
            }
            if (btnAction != null) {
                btnAction.setText(nativeAd.getCTAText());
                actionView.add(btnAction);
            }

            if (socialView != null) {
                // socialView.setText(nativeAd.getAdSocialContext());
            }

            nativeAd.setNativeAdView(adView);

            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());

            if (largeInteraction && btnAction != null) {
                nativeAd.registerClickableViews(actionView);
            } else {
                nativeAd.registerClickableViews(btnAction);
            }
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, new Throwable());
        }
    }

    public static boolean percentRandomBoolean(int percent) {
        if (percent <= 0 || percent > 100) return false;
        int randomVal = new Random().nextInt(100);
        return randomVal <= percent;
    }

    private MediaView createMediaView(Context context) {
        try {
            return new MediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }
}