package com.rabbit.adsdk.adloader.addfp;

import android.content.Context;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/26.
 */

public class AdDfpBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindNative adContainer == null###");
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

    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootLayout <= 0) {
            throw new AndroidRuntimeException("rootLayout is 0x0");
        }
        View view = null;
        try {
            View rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
            view = showUnifiedAdView(rootView, nativeAd, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(view, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private View showUnifiedAdView(View rootView, UnifiedNativeAd nativeAd, PidConfig pidConfig) throws Exception {
        UnifiedNativeAdView adView = new UnifiedNativeAdView(rootView.getContext());
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        adView.addView(rootView);

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = adView.findViewById(mParams.getAdCover());
        View bodyView = adView.findViewById(mParams.getAdDetail());
        View ctaView = adView.findViewById(mParams.getAdAction());
        View adIconView = adView.findViewById(mParams.getAdIcon());
        View rateBarView = adView.getStarRatingView();
        if (titleView != null && isClickable(AD_TITLE, pidConfig)) {
            adView.setHeadlineView(titleView);
        }
        if (adCoverView != null && isClickable(AD_COVER, pidConfig)) {
            adView.setImageView(adCoverView);
        }
        if (bodyView != null && isClickable(AD_DETAIL, pidConfig)) {
            adView.setBodyView(bodyView);
        }
        if (ctaView != null && isClickable(AD_CTA, pidConfig)) {
            adView.setCallToActionView(ctaView);
        }
        if (adIconView != null && isClickable(AD_ICON, pidConfig)) {
            adView.setIconView(adIconView);
        }
        if (rateBarView != null && isClickable(AD_RATE, pidConfig)) {
            adView.setStarRatingView(rateBarView);
        }

        // 设置广告元素内容
        if (!TextUtils.isEmpty(nativeAd.getHeadline())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(nativeAd.getHeadline());
                titleView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(nativeAd.getBody())) {
            if (bodyView instanceof TextView) {
                ((TextView) bodyView).setText(nativeAd.getBody());
                bodyView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(nativeAd.getCallToAction())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(nativeAd.getCallToAction());
                ctaView.setVisibility(View.VISIBLE);
            }
        }

        NativeAd.Image image = nativeAd.getIcon();
        if (image != null && image.getDrawable() != null) {
            if (adIconView instanceof ImageView) {
                ((ImageView) adIconView).setImageDrawable(image.getDrawable());
                adIconView.setVisibility(View.VISIBLE);
            }
        }

        ViewGroup mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        MediaView mediaView = createMediaView(rootView.getContext());
        if (mediaViewLayout != null && mediaView != null) {
            mediaViewLayout.addView(mediaView, -1, -1);
        }

        // 废弃ImageView作为Cover
        ImageView coverView = (ImageView) adView.getImageView();
        if (coverView != null) {
            coverView.setVisibility(View.GONE);
        }

        if (mediaViewLayout != null) {
            mediaViewLayout.setVisibility(View.VISIBLE);
        }

        if (mediaView != null) {
            adView.setMediaView(mediaView);
        }

        try {
            if (rateBarView != null) {
                if (nativeAd.getStarRating() == null) {
                    rateBarView.setVisibility(View.INVISIBLE);
                } else {
                    ((RatingBar) rateBarView)
                            .setRating(nativeAd.getStarRating().floatValue());
                    rateBarView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        Log.iv(Log.TAG, "clickable view : " + pidConfig.getClickViews());
        adView.setNativeAd(nativeAd);
        putAdvertiserInfo(nativeAd);
        return adView;
    }

    private MediaView createMediaView(Context context) {
        try {
            MediaView mediaView = new MediaView(context);
            return mediaView;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void putAdvertiserInfo(UnifiedNativeAd nativeAd) {
        try {
            putValue(AD_TITLE, nativeAd.getHeadline());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, nativeAd.getBody());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ADVERTISER, nativeAd.getAdvertiser());
        } catch (Exception e) {
        }
        try {
            putValue(AD_PRICE, nativeAd.getPrice());
        } catch (Exception e) {
        }
        try {
            putValue(AD_STORE, nativeAd.getStore());
        } catch (Exception e) {
        }
        try {
            List<String> images = new ArrayList<>();
            List<NativeAd.Image> list = nativeAd.getAdChoicesInfo().getImages();
            for (NativeAd.Image image : list) {
                images.add(image.getUri().toString());
            }
            String choiceStr = nativeAd.getAdChoicesInfo().getText().toString();
            putValue(AD_CHOICES, choiceStr + "-" + images.toString());
        } catch (Exception e) {
        }
        try {
            List<String> images = new ArrayList<>();
            List<NativeAd.Image> list = nativeAd.getImages();
            for (NativeAd.Image image : list) {
                images.add(image.getUri().toString());
            }
            putValue(AD_MEDIA, images.toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, nativeAd.getCallToAction());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ICON, nativeAd.getIcon().getUri().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_RATE, nativeAd.getStarRating().toString());
        } catch (Exception e) {
        }
    }
}
