package com.inner.adsdk.adloader.adfb;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.inner.adsdk.R;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class FBBindNativeView {

    private Params mParams;

    public void bindFBNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.v(Log.TAG, "bindFBNative mParams == null");
            return;
        }
        if (adContainer == null) {
            Log.v(Log.TAG, "bindFBNative adContainer == null");
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
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.fb_native_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.fb_native_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.fb_native_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.fb_native_card_large;
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
        mParams.setAdTitle(R.id.fb_title);
        mParams.setAdSubTitle(R.id.fb_sub_title);
        mParams.setAdSocial(R.id.fb_social);
        mParams.setAdDetail(R.id.fb_detail);
        mParams.setAdIcon(R.id.fb_icon);
        mParams.setAdAction(R.id.fb_action_btn);
        mParams.setAdCover(R.id.fb_image_cover);
        mParams.setAdChoices(R.id.fb_ad_choices_container);
        mParams.setAdMediaView(R.id.fb_media_cover);
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

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        TextView subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView socialView = rootView.findViewById(mParams.getAdSocial());
        TextView detail = rootView.findViewById(mParams.getAdDetail());
        AppCompatButton btnAction = rootView.findViewById(mParams.getAdAction());
        ViewGroup adChoiceContainer = rootView.findViewById(mParams.getAdChoices());
        MediaView mediaCover = createMediaView(rootView.getContext());
        ViewGroup mediaLayout = rootView.findViewById(mParams.getAdMediaView());

        if (mediaLayout != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            mediaLayout.addView(mediaCover, params);
            mediaLayout.setVisibility(View.VISIBLE);
        }

        if (mediaCover != null) {
            mediaCover.setVisibility(View.VISIBLE);
        }
        if (imageCover != null) {
            imageCover.setVisibility(View.GONE);
        }

        // 可点击的视图
        List<View> actionView = new ArrayList<View>();

        if (nativeAd != null && nativeAd.isAdLoaded()) {
            if (icon != null) {
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, icon);
                actionView.add(icon);
            }

            // Download and setting the cover image.
            if (mediaCover != null) {
                mediaCover.setNativeAd(nativeAd);
                actionView.add(mediaCover);
            }

            // Add adChoices icon
            if (adChoiceContainer != null) {
                AdChoicesView adChoicesView = new AdChoicesView(rootView.getContext(), nativeAd, true);
                adChoiceContainer.addView(adChoicesView, 0);
            }

            if (titleView != null) {
                titleView.setText(nativeAd.getAdTitle());
                actionView.add(titleView);
            }
            if (subTitleView != null) {
                subTitleView.setText(nativeAd.getAdSubtitle());
                actionView.add(subTitleView);
            }
            if (detail != null) {
                detail.setText(nativeAd.getAdBody());
                actionView.add(detail);
            }
            if (btnAction != null) {
                btnAction.setText(nativeAd.getAdCallToAction());
                actionView.add(btnAction);
            }

            if (socialView != null) {
                socialView.setText(nativeAd.getAdSocialContext());
            }

            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());

            if (largeInteraction && rootView != null) {
                nativeAd.registerViewForInteraction(rootView, actionView);
            } else {
                nativeAd.registerViewForInteraction(btnAction);
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