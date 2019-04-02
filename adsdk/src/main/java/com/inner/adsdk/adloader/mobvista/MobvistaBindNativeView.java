package com.inner.adsdk.adloader.mobvista;

import android.content.Context;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adywind.core.api.Ad;
import com.adywind.nativeads.api.NativeAds;
import com.appub.ads.a.R;
import com.inner.adsdk.adloader.base.BaseBindNativeView;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

public class MobvistaBindNativeView extends BaseBindNativeView {

    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, NativeAds nativeAds, Ad ad, PidConfig pidConfig) {
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
            bindNativeViewWithRootView(adContainer, rootView, nativeAds, ad, pidConfig);
        } else if (rootLayout > 0) {
            if (adContainer.getContext() != null) {
                rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
                bindNativeViewWithRootView(adContainer, rootView, nativeAds, ad, pidConfig);
            }
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, nativeAds, ad, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find mobvista native layout");
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAds nativeAds, Ad ad, PidConfig pidConfig) {
        int layoutId = R.layout.native_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        } else if (template == Constant.NATIVE_CARD_FULL) {
            layoutId = R.layout.native_card_full;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(adContainer, rootView, nativeAds, ad, pidConfig);
        try {
            adContainer.removeAllViews();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindNativeViewWithTemplate(ViewGroup adContainer, View rootView, NativeAds nativeAds, Ad ad, PidConfig pidConfig) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        bindNativeViewWithRootView(adContainer, rootView, nativeAds, ad, pidConfig);
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, NativeAds nativeAds, Ad ad, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        try {
            showAdView(rootView, nativeAds, ad, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void showAdView(View rootView, NativeAds nativeAds, Ad ad, PidConfig pidConfig) throws Exception {
        // 恢复icon图标
        try {
            restoreIconView(rootView, pidConfig.getSdk(), mParams.getAdIcon());
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        if (titleView != null) {
            titleView.setText(ad.getTitle());
            if (!TextUtils.isEmpty(ad.getTitle())) {
                titleView.setVisibility(View.VISIBLE);
            }
        }

        View detailView = rootView.findViewById(mParams.getAdDetail());
        View subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        View bodyView = detailView != null ? detailView : subTitleView;
        if (bodyView instanceof TextView) {
            ((TextView) bodyView).setText(ad.getBody());
            if (!TextUtils.isEmpty(ad.getBody())) {
                bodyView.setVisibility(View.VISIBLE);
            }
        }

        ImageView imageView = rootView.findViewById(mParams.getAdCover());
        String imageUrl = ad.getImageUrl();
        if (imageView != null && !TextUtils.isEmpty(imageUrl)) {
            String imageSize = ad.getImageSize();
            int width = 1200;
            int height = 627;
            if (!TextUtils.isEmpty(imageSize)) {
                String[] sizeArray = imageSize.split("x");
                String w = sizeArray[0];
                String h = sizeArray[1];
                if (!TextUtils.isEmpty(w) && !TextUtils.isEmpty(h)) {
                    width = Integer.valueOf(w);
                    height = Integer.valueOf(h);
                }
            }
            com.mopub.nativeads.NativeImageHelper.loadImageView(imageUrl, imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            if (imageView != null) {
                imageView.setVisibility(View.GONE);
            }
        }

        String iconUrl = ad.getIconUrl();
        ImageView iconImageView = rootView.findViewById(mParams.getAdIcon());
        if (iconImageView != null) {
            com.mopub.nativeads.NativeImageHelper.loadImageView(iconUrl, iconImageView);
            iconImageView.setVisibility(View.VISIBLE);
        }

        TextView ctaTextView = rootView.findViewById(mParams.getAdAction());
        String cta = ad.getCta();
        if (ctaTextView != null) {
            ctaTextView.setText(cta);
            if (!TextUtils.isEmpty(cta)) {
                ctaTextView.setVisibility(View.VISIBLE);
            }
        }

        List<View> viewList = new ArrayList<>();
        viewList.add(titleView);
        viewList.add(bodyView);
        viewList.add(imageView);
        viewList.add(iconImageView);
        viewList.add(ctaTextView);

        nativeAds.registerView(ad, rootView, viewList);
    }
}
