package com.komob.adsdk.adloader.bigo;

import android.content.Context;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

import sg.bigo.ads.api.AdOptionsView;
import sg.bigo.ads.api.AdTag;
import sg.bigo.ads.api.MediaView;
import sg.bigo.ads.api.NativeAd;

/**
 * Created by Administrator on 2018/4/26.
 */

public class BigoBindNativeView extends BaseBindNativeView {
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
        int rootLayout = getBestNativeLayout(adContainer.getContext(), pidConfig, mParams, Constant.AD_NETWORK_BIGO);
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
        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = rootView.findViewById(mParams.getAdCover());
        TextView bodyView = rootView.findViewById(mParams.getAdDetail());
        TextView ctaView = rootView.findViewById(mParams.getAdAction());
        View adIconView = rootView.findViewById(mParams.getAdIcon());
        ViewGroup mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        MediaView mediaView = createMediaView(rootView.getContext());
        if (mediaViewLayout != null && mediaView != null) {
            mediaViewLayout.addView(mediaView, -1, -1);
        }

        View adChoiceLayout = rootView.findViewById(mParams.getAdChoices());
        if (adChoiceLayout != null && !(adChoiceLayout instanceof FrameLayout)) {
            FrameLayout frameLayout = new FrameLayout(rootView.getContext());
            replaceSrcViewToDstView(adChoiceLayout, frameLayout);
            adChoiceLayout = frameLayout;
        }
        AdOptionsView adOptionsView = createOptionsView(rootView.getContext());
        if (adChoiceLayout instanceof ViewGroup && adOptionsView != null) {
            ((ViewGroup) adChoiceLayout).addView(adOptionsView);
        }

        ImageView iconView = null;
        if (adIconView instanceof ImageView) {
            iconView = (ImageView) adIconView;
        } else {
            iconView = new ImageView(rootView.getContext());
            replaceSrcViewToDstView(adCoverView, iconView);
        }

        setDefaultAdIcon(pidConfig.getSdk(), iconView);

        titleView.setTag(AdTag.TITLE);
        titleView.setText(nativeAd.getTitle());

        bodyView.setTag(AdTag.DESCRIPTION);
        String desc = nativeAd.getDescription();
        if (TextUtils.isEmpty(desc)) {
            desc = nativeAd.getTitle();
        }
        bodyView.setText(desc);

        ctaView.setTag(AdTag.CALL_TO_ACTION);
        String cta = nativeAd.getCallToAction();
        if (!TextUtils.isEmpty(cta)) {
            ctaView.setText(cta);
        } else {
            ctaView.setText(getActionText(ctaView.getContext()));
        }

        List<View> clickableViews = new ArrayList<>();
        if (titleView != null && isClickable(AD_TITLE, pidConfig)) {
            clickableViews.add(titleView);
        }
        if (bodyView != null && isClickable(AD_DETAIL, pidConfig)) {
            clickableViews.add(bodyView);
        }
        if (adIconView != null && isClickable(AD_ICON, pidConfig)) {
            clickableViews.add(adIconView);
        }
        if (ctaView != null && isClickable(AD_CTA, pidConfig)) {
            clickableViews.add(ctaView);
        }
        nativeAd.registerViewForInteraction((ViewGroup) rootView, mediaView, iconView, adOptionsView, clickableViews);

        Log.iv(Log.TAG, "clickable view : " + pidConfig.getClickView());
        putAdvertiserInfo(nativeAd);
        return rootView;
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

    private AdOptionsView createOptionsView(Context context) {
        try {
            AdOptionsView adOptionsView = new AdOptionsView(context);
            return adOptionsView;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void putAdvertiserInfo(NativeAd nativeAd) {
        try {
            putValue(AD_TITLE, nativeAd.getTitle());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, nativeAd.getDescription());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ADVERTISER, nativeAd.getAdvertiser());
        } catch (Exception e) {
        }
        try {
            putValue(AD_PRICE, String.valueOf(nativeAd.getBid().getPrice() / 1000));
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, nativeAd.getCallToAction());
        } catch (Exception e) {
        }
    }
}
