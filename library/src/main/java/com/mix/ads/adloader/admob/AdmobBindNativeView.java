package com.mix.ads.adloader.admob;

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

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mix.ads.adloader.base.BaseBindNativeView;
import com.mix.ads.core.framework.Params;
import com.mix.ads.data.config.PidConfig;
import com.mix.ads.log.Log;

/**
 * Created by Administrator on 2018/4/26.
 */

public class AdmobBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.iv(Log.TAG, "bindNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.iv(Log.TAG, "bindNative adContainer == null###");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        if (rootLayout > 0) {
            bindNativeViewWithRootView(adContainer, rootLayout, nativeAd, pidConfig);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
            updateAdViewStatus(adContainer, mParams, true);
        } else {
            Log.iv(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, NativeAd nativeAd, PidConfig pidConfig) {
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
            Log.iv(Log.TAG, "error : " + e);
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(view, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private View showUnifiedAdView(View rootView, NativeAd nativeAd, PidConfig pidConfig) throws Exception {
        NativeAdView nativeAdView = new NativeAdView(rootView.getContext());
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }

        nativeAdView.addView(rootView);

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = nativeAdView.findViewById(mParams.getAdCover());
        View bodyView = nativeAdView.findViewById(mParams.getAdDetail());
        View ctaView = nativeAdView.findViewById(mParams.getAdAction());
        View adIconView = nativeAdView.findViewById(mParams.getAdIcon());
        View rateBarView = nativeAdView.getStarRatingView();
        if (titleView != null && isClickable(AD_TITLE, pidConfig)) {
            nativeAdView.setHeadlineView(titleView);
        }
        if (adCoverView != null && isClickable(AD_COVER, pidConfig)) {
            nativeAdView.setImageView(adCoverView);
        }
        if (bodyView != null && isClickable(AD_DETAIL, pidConfig)) {
            nativeAdView.setBodyView(bodyView);
        }
        if (ctaView != null && isClickable(AD_CTA, pidConfig)) {
            nativeAdView.setCallToActionView(ctaView);
        }
        if (adIconView != null && isClickable(AD_ICON, pidConfig)) {
            nativeAdView.setIconView(adIconView);
        }
        if (rateBarView != null && isClickable(AD_RATE, pidConfig)) {
            nativeAdView.setStarRatingView(rateBarView);
        }

        // 设置广告元素内容
        String titleText = nativeAd.getHeadline();
        if (titleView instanceof TextView) {
            if (!TextUtils.isEmpty(titleText)) {
                ((TextView) titleView).setText(titleText);
                titleView.setVisibility(View.VISIBLE);
            }
        }

        if (bodyView instanceof TextView) {
            bodyView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(nativeAd.getBody())) {
                ((TextView) bodyView).setText(nativeAd.getBody());
            } else {
                ((TextView) bodyView).setText(titleText);
            }
        }

        if (ctaView instanceof TextView) {
            ctaView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(nativeAd.getCallToAction())) {
                ((TextView) ctaView).setText(nativeAd.getCallToAction());
            } else {
                ((TextView) ctaView).setText(getActionText(ctaView.getContext()));
            }
        }

        NativeAd.Image image = nativeAd.getIcon();
        if (image != null && image.getDrawable() != null) {
            if (adIconView instanceof ImageView) {
                ((ImageView) adIconView).setImageDrawable(image.getDrawable());
                adIconView.setVisibility(View.VISIBLE);
            }
        } else {
            try {
                if (adIconView instanceof ImageView) {
                    setDefaultAdIcon(pidConfig.getSdk(), (ImageView) adIconView);
                }
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }

        ViewGroup mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        MediaView mediaView = createMediaView(rootView.getContext());
        if (mediaViewLayout != null && mediaView != null) {
            mediaViewLayout.addView(mediaView, -1, -1);
        }

        // 废弃ImageView作为Cover
        ImageView coverView = (ImageView) nativeAdView.getImageView();
        if (coverView != null) {
            coverView.setVisibility(View.GONE);
        }

        if (mediaViewLayout != null) {
            mediaViewLayout.setVisibility(View.VISIBLE);
        }

        if (mediaView != null) {
            nativeAdView.setMediaView(mediaView);
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
            Log.iv(Log.TAG, "error : " + e);
        }
        Log.iv(Log.TAG, "clickable view : " + pidConfig.getClickView());
        nativeAdView.setNativeAd(nativeAd);
        return nativeAdView;
    }

    private MediaView createMediaView(Context context) {
        try {
            MediaView mediaView = new MediaView(context);
            return mediaView;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }
}