package com.inner.adaggs.adloader.adfb;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.inner.adaggs.R;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.framework.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class FBBindNativeView {

    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            return;
        }
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(rootView, nativeAd, pidConfig);
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, nativeAd, pidConfig);
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            return;
        }
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
        bindNativeViewWithTemplate(rootView, nativeAd, pidConfig);
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 外部传入ViewRoot
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            return;
        }
        if (nativeAd == null || !nativeAd.isAdLoaded()) {
            return;
        }
        if (pidConfig == null) {
            return;
        }

        if (mParams == null) {
            return;
        }

        Context context = rootView.getContext();

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        TextView subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView socialView = rootView.findViewById(mParams.getAdSocial());
        TextView detail = rootView.findViewById(mParams.getAdDetail());
        AppCompatButton btnAction = rootView.findViewById(mParams.getAdAction());
        RelativeLayout adChoiceContainer = rootView.findViewById(mParams.getAdChoices());
        MediaView mediaCover = new MediaView(rootView.getContext());
        ViewGroup coverLayout = rootView.findViewById(mParams.getAdView());

        if (coverLayout != null) {
            coverLayout.addView(mediaCover);
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

            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());

            if (largeInteraction && rootView != null) {
                nativeAd.registerViewForInteraction(rootView, actionView);
            } else {
                nativeAd.registerViewForInteraction(btnAction);
            }
        }
    }

    /**
     * 使用模板显示原生广告
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithTemplate(View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            return;
        }
        if (nativeAd == null || !nativeAd.isAdLoaded()) {
            return;
        }
        if (pidConfig == null) {
            return;
        }
        ImageView imageCover = rootView.findViewById(R.id.fb_image_cover);
        ImageView icon = rootView.findViewById(R.id.fb_icon);
        TextView titleView = rootView.findViewById(R.id.fb_title);
        TextView subTitleView = rootView.findViewById(R.id.fb_sub_title);
        TextView socialView = rootView.findViewById(R.id.fb_social);
        TextView detail = rootView.findViewById(R.id.fb_detail);
        AppCompatButton btnAction = rootView.findViewById(R.id.fb_action_btn);
        RelativeLayout adChoiceContainer = rootView.findViewById(R.id.fb_ad_choices_container);
        MediaView mediaCover = rootView.findViewById(R.id.fb_media_cover);

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

            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());

            if (largeInteraction && rootView != null) {
                nativeAd.registerViewForInteraction(rootView, actionView);
            } else {
                nativeAd.registerViewForInteraction(btnAction);
            }
        }
    }

    public static boolean percentRandomBoolean(int percent) {
        if (percent <= 0 || percent > 100) return false;
        int randomVal = new Random().nextInt(100);
        return randomVal <= percent;
    }

    private static int getId(Context context, String name) {
        if (context == null || TextUtils.isEmpty(name)) {
            return -1;
        }
        return context.getResources().getIdentifier(name, "id", context.getPackageName());
    }
}