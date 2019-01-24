package com.inner.adsdk.adloader.addfp;

import android.content.Context;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.inner.adsdk.R;
import com.inner.adsdk.adloader.base.BaseBindNativeView;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

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
            Log.e(Log.TAG, "Can not find dfp native layout###");
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        View view = null;
        try {
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

    private void bindNativeWithCard(ViewGroup adContainer, int cardId, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        int layoutId = R.layout.native_card_small;
        if (cardId == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (cardId == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (cardId == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        }
        View rootView = LayoutInflater.from(adContainer.getContext()).inflate(layoutId, null);
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        View view = null;
        try {
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
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        // 恢复icon图标
        try {
            restoreIconView(rootView, pidConfig.getSdk(), mParams.getAdIcon());
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        adView.addView(rootView);

        adView.setHeadlineView(rootView.findViewById(mParams.getAdTitle()));
        adView.setImageView(adView.findViewById(mParams.getAdCover()));

        // 由于dfp没有subTitle的接口，因此将使用subTitle或者detailView
        View detailView = adView.findViewById(mParams.getAdDetail());
        View subTitleView = adView.findViewById(mParams.getAdSubTitle());
        View bodyView = detailView != null ? detailView : subTitleView;

        adView.setBodyView(bodyView);

        adView.setCallToActionView(adView.findViewById(mParams.getAdAction()));
        adView.setIconView(adView.findViewById(mParams.getAdIcon()));

        if (!TextUtils.isEmpty(nativeAd.getHeadline())) {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

            adView.getHeadlineView().setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(nativeAd.getBody())) {
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

            adView.getBodyView().setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(nativeAd.getCallToAction())) {
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

            adView.getCallToActionView().setVisibility(View.VISIBLE);
        }

        NativeAd.Image image = nativeAd.getIcon();
        if (image != null && image.getDrawable() != null) {
            View iconView = adView.getIconView();
            if (iconView instanceof ImageView) {
                ((ImageView) iconView).setImageDrawable(image.getDrawable());

                iconView.setVisibility(View.VISIBLE);
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

        // google 强制使用MediaView
        /**
         VideoController vc = nativeAd.getVideoController();
         if (vc.hasVideoContent()) {
         adView.setMediaView(mediaView);
         coverView.setVisibility(View.GONE);
         mediaViewLayout.setVisibility(View.VISIBLE);
         vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
         public void onVideoEnd() {
         super.onVideoEnd();
         }
         });
         } else {
         adView.setImageView(coverView);
         mediaViewLayout.setVisibility(View.GONE);
         List<NativeAd.Image> images = nativeAd.getImages();
         if (images.size() > 0 && images.get(0) != null) {
         coverView.setImageDrawable(images.get(0).getDrawable());
         }
         }*/

        try {
            if (adView.getStarRatingView() != null) {
                if (nativeAd.getStarRating() == null) {
                    adView.getStarRatingView().setVisibility(View.INVISIBLE);
                } else {
                    ((RatingBar) adView.getStarRatingView())
                            .setRating(nativeAd.getStarRating().floatValue());
                    adView.getStarRatingView().setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        adView.setNativeAd(nativeAd);
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
}
