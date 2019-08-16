package com.hauyu.adsdk.adloader.admob;

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
import com.gekes.fvs.tdsvap.R;
import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.Params;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.adloader.base.BaseBindNativeView;

/**
 * Created by Administrator on 2018/4/26.
 */

public class AdmobBindNativeView extends BaseBindNativeView {
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
            Log.e(Log.TAG, "Can not find admob native layout###");
        }
        onAdViewShown(adContainer, pidConfig, mParams);
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        View view = null;
        preSetMediaView(rootView, pidConfig);
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
            postSetMediaView(rootView, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int cardId, UnifiedNativeAd nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        int layoutId = R.layout.had_card_small;
        if (cardId == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.had_card_small;
        } else if (cardId == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.had_card_medium;
        } else if (cardId == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.had_card_large;
        } else if (cardId == Constant.NATIVE_CARD_FULL) {
            layoutId = getFullLayout();
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
        preSetMediaView(rootView, pidConfig);
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
            postSetMediaView(rootView, pidConfig);
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

        /**
         * 默认adchoiceview不可见
         */
        restoreAdChoiceView(rootView, mParams.getAdChoices());

        adView.addView(rootView);

        clearClickListener(mParams, rootView);

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = adView.findViewById(mParams.getAdCover());
        // 由于admob没有subTitle的接口，因此将使用subTitle或者detailView
        View detailView = adView.findViewById(mParams.getAdDetail());
        View subTitleView = adView.findViewById(mParams.getAdSubTitle());
        View bodyView = detailView != null ? detailView : subTitleView;

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
                ((TextView)titleView).setText(nativeAd.getHeadline());
                titleView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(nativeAd.getBody())) {
            if (bodyView instanceof TextView) {
                ((TextView)bodyView).setText(nativeAd.getBody());
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
