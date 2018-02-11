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
        bindNativeView(rootView, nativeAd, pidConfig);
    }

    public void bindNativeWithConatiner(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            return;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.fb_native_template_1, null);
        bindNativeView(rootView, nativeAd, pidConfig);
        try {
            adContainer.removeAllViews();
            adContainer.addView(rootView);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
        }
    }

    private void bindNativeView(View rootView, NativeAd nativeAd, PidConfig pidConfig) {
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

        mediaCover.setVisibility(View.VISIBLE);
        imageCover.setVisibility(View.GONE);
        if (nativeAd != null && nativeAd.isAdLoaded()) {
            NativeAd.Image adIcon = nativeAd.getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, icon);

            // Download and setting the cover image.
            mediaCover.setNativeAd(nativeAd);

            // Add adChoices icon
            AdChoicesView adChoicesView = new AdChoicesView(rootView.getContext(), nativeAd, true);
            adChoiceContainer.addView(adChoicesView, 0);

            titleView.setText(nativeAd.getAdTitle());
            subTitleView.setText(nativeAd.getAdSubtitle());
            detail.setText(nativeAd.getAdBody());
            btnAction.setText(nativeAd.getAdCallToAction());
            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());
            if (largeInteraction && rootView != null) {
                List<View> actionView = new ArrayList<>();
                actionView.add(titleView);
                actionView.add(mediaCover);
                actionView.add(icon);
                actionView.add(subTitleView);
                actionView.add(detail);
                actionView.add(btnAction);
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
