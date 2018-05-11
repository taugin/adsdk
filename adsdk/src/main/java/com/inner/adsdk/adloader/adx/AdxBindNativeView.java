package com.inner.adsdk.adloader.adx;

import android.content.Context;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.inner.adsdk.R;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import java.util.List;

/**
 * Created by Administrator on 2018/4/26.
 */

public class AdxBindNativeView {
    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            return;
        }
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, nativeAd, pidConfig);
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        if (!(rootView instanceof FrameLayout)) {
            throw new AndroidRuntimeException("Root View must be a FrameLayout");
        }
        if (nativeAd instanceof NativeContentAd) {
            showContentAdView(rootView, (NativeContentAd) nativeAd, pidConfig);
        } else if (nativeAd instanceof NativeAppInstallAd) {
            showInstallAdView(rootView, (NativeAppInstallAd) nativeAd, pidConfig);
        }
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

    private void bindNativeWithCard(ViewGroup adContainer, int cardId, NativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        int layoutId = R.layout.adx_native_card_medium;
        if (cardId == Constant.NATIVE_CARD_SMALL) {
        } else if (cardId == Constant.NATIVE_CARD_MEDIUM) {
        } else if (cardId == Constant.NATIVE_CARD_LARGE) {
        }
        View rootView = LayoutInflater.from(adContainer.getContext()).inflate(layoutId, null);
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        if (!(rootView instanceof FrameLayout)) {
            throw new AndroidRuntimeException("Root View must be a FrameLayout");
        }
        mParams.setAdTitle(R.id.adx_title);
        mParams.setAdDetail(R.id.adx_detail);
        mParams.setAdIcon(R.id.adx_icon);
        mParams.setAdAction(R.id.adx_action);
        mParams.setAdCover(R.id.adx_cover);
        mParams.setAdMediaView(R.id.adx_mediaview);
        if (nativeAd instanceof NativeContentAd) {
            showContentAdView(rootView, (NativeContentAd) nativeAd, pidConfig);
        } else if (nativeAd instanceof NativeAppInstallAd) {
            showInstallAdView(rootView, (NativeAppInstallAd) nativeAd, pidConfig);
        }
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

    private void showContentAdView(View rootView, NativeContentAd nativeAd, PidConfig pidConfig) {
        NativeContentAdView adView = new NativeContentAdView(rootView.getContext());
        FrameLayout rootLayout = (FrameLayout) rootView;
        View childView = rootLayout.getChildAt(0);
        rootLayout.removeView(childView);
        adView.addView(childView);
        rootLayout.addView(adView);

        adView.setHeadlineView(rootView.findViewById(mParams.getAdTitle()));
        adView.setImageView(adView.findViewById(mParams.getAdCover()));
        adView.setBodyView(adView.findViewById(mParams.getAdDetail()));
        adView.setCallToActionView(adView.findViewById(mParams.getAdAction()));
        adView.setLogoView(adView.findViewById(mParams.getAdIcon()));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        List<NativeAd.Image> images = nativeAd.getImages();
        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }
        NativeAd.Image logoImage = nativeAd.getLogo();

        View logoView = adView.getLogoView();
        if (logoView != null) {
            if (logoImage == null) {
                logoView.setVisibility(View.INVISIBLE);
            } else {
                ((ImageView) logoView).setImageDrawable(logoImage.getDrawable());
                logoView.setVisibility(View.VISIBLE);
            }
        }
        adView.setNativeAd(nativeAd);
    }

    private void showInstallAdView(View rootView, NativeAppInstallAd nativeAd, PidConfig pidConfig) {
        NativeAppInstallAdView adView = new NativeAppInstallAdView(rootView.getContext());
        FrameLayout rootLayout = (FrameLayout) rootView;
        View childView = rootLayout.getChildAt(0);
        rootLayout.removeView(childView);
        adView.addView(childView);
        rootLayout.addView(adView);

        adView.setHeadlineView(rootView.findViewById(mParams.getAdTitle()));
        adView.setImageView(adView.findViewById(mParams.getAdCover()));
        adView.setBodyView(adView.findViewById(mParams.getAdDetail()));
        adView.setCallToActionView(adView.findViewById(mParams.getAdAction()));
        adView.setIconView(adView.findViewById(mParams.getAdIcon()));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        ((ImageView) adView.getIconView()).setImageDrawable(
                nativeAd.getIcon().getDrawable());

        FrameLayout mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        MediaView mediaView = createMediaView(rootView.getContext());
        mediaViewLayout.addView(mediaView);

        ImageView coverView = (ImageView) adView.getImageView();

        VideoController vc = nativeAd.getVideoController();
        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            coverView.setVisibility(View.GONE);
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        } else {
            adView.setImageView(coverView);
            mediaViewLayout.setVisibility(View.GONE);
            List<com.google.android.gms.ads.formats.NativeAd.Image> images = nativeAd.getImages();
            if (images.size() > 0) {
                coverView.setImageDrawable(images.get(0).getDrawable());
            }
        }

        if (adView.getStarRatingView() != null) {
            if (nativeAd.getStarRating() == null) {
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) adView.getStarRatingView())
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        }
        adView.setNativeAd(nativeAd);
    }

    private MediaView createMediaView(Context context) {
        try {
            MediaView mediaView = new MediaView(context);
            return mediaView;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }
}
