package com.inner.adaggs.adloader.adfb;

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
import com.inner.adaggs.R;
import com.inner.adaggs.config.PidConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class FBBindNativeView {

    public void bindNative(View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        bindNativeViewWithRootView(rootView, nativeAd, pidConfig);
    }

    public void bindNativeWithTemplate(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            return;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.fb_native_template_1, null);
        bindNativeViewWithTemplate(rootView, nativeAd, pidConfig);
        try {
            adContainer.removeAllViews();
            adContainer.addView(rootView);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
        }
    }

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
        ImageView imageCover = rootView.findViewById(R.id.fb_image_cover);
        ImageView icon = rootView.findViewById(R.id.fb_icon);
        TextView titleView = rootView.findViewById(R.id.fb_title);
        TextView subTitleView = rootView.findViewById(R.id.fb_sub_title);
        TextView socialView = rootView.findViewById(R.id.fb_social);
        TextView detail = rootView.findViewById(R.id.fb_detail);
        AppCompatButton btnAction = rootView.findViewById(R.id.fb_action_btn);
        RelativeLayout adChoiceContainer = rootView.findViewById(R.id.fb_ad_choices_container);
        MediaView mediaCover = new MediaView(rootView.getContext());
        ViewGroup coverLayout = rootView.findViewById(R.id.fb_media_cover);

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
}
