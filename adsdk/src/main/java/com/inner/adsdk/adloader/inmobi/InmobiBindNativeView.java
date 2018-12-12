package com.inner.adsdk.adloader.inmobi;

import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inmobi.ads.InMobiNative;
import com.inner.adsdk.R;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/26.
 */

public class InmobiBindNativeView {
    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, InMobiNative nativeAd, PidConfig pidConfig) {
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
            Log.e(Log.TAG, "Can not find inmobi native layout###");
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, InMobiNative nativeAd, PidConfig pidConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        try {
            showNativeAdView(rootView, nativeAd, pidConfig);
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

    private void bindNativeWithCard(ViewGroup adContainer, int cardId, InMobiNative nativeAd, PidConfig pidConfig) {
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
        if (!(rootView instanceof FrameLayout)) {
            throw new AndroidRuntimeException("Root View must be a FrameLayout");
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
        try {
            showNativeAdView(rootView, nativeAd, pidConfig);
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

    private void showNativeAdView(View rootView, final InMobiNative nativeAd, PidConfig pidConfig) throws Exception {
        ViewGroup rootLayout = (ViewGroup) rootView;

        TextView titleView = rootLayout.findViewById(mParams.getAdTitle());
        ImageView icon = rootLayout.findViewById(mParams.getAdIcon());
        TextView detail = rootLayout.findViewById(mParams.getAdDetail());
        AppCompatButton btnAction = rootLayout.findViewById(mParams.getAdAction());

        List<View> actionView = new ArrayList<View>();

        if (titleView != null) {
            titleView.setText(nativeAd.getAdTitle());
            if (!TextUtils.isEmpty(nativeAd.getAdTitle())) {
                btnAction.setVisibility(View.VISIBLE);
            }
        }
        if (detail != null) {
            detail.setText(nativeAd.getAdDescription());
            if (!TextUtils.isEmpty(nativeAd.getAdDescription())) {
                btnAction.setVisibility(View.VISIBLE);
            }
        }
        if (btnAction != null) {
            btnAction.setText(nativeAd.getAdCtaText());
            if (!TextUtils.isEmpty(nativeAd.getAdCtaText())) {
                btnAction.setVisibility(View.VISIBLE);
            }
            actionView.add(btnAction);
        }

        String iconUrl = nativeAd.getAdIconUrl();
        if (icon != null) {
            Picasso.with(rootLayout.getContext())
                    .load(iconUrl)
                    .into(icon);
            actionView.add(icon);
            if (!TextUtils.isEmpty(iconUrl)) {
                icon.setVisibility(View.VISIBLE);
            }
        }

        ImageView coverView = rootLayout.findViewById(mParams.getAdCover());
        if (coverView != null) {
            coverView.setVisibility(View.GONE);
        }

        ViewGroup mediaViewLayout = rootLayout.findViewById(mParams.getAdMediaView());
        if (mediaViewLayout != null) {
            View mediaView = nativeAd.getPrimaryViewOfWidth(rootLayout.getContext(), mediaViewLayout,
                    rootLayout, rootLayout.getWidth());
            mediaViewLayout.setVisibility(View.VISIBLE);
            if (mediaView != null) {
                mediaViewLayout.addView(mediaView);
            }
            actionView.add(mediaViewLayout);
        }

        for (View view : actionView) {
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (nativeAd != null) {
                            nativeAd.reportAdClickAndOpenLandingPage();
                        }
                    }
                });
            }
        }
    }
}
