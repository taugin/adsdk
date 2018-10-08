package com.inner.adsdk.adloader.mopub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inner.adsdk.R;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.mopub.nativeads.MediaLayout;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;

import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class MopubBindNativeView {
    private Params mParams;

    public void bindMopubNative(Params params, Context context, MoPubNative nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindMopubNative mParams == null###");
            return;
        }
        if (context == null) {
            Log.e(Log.TAG, "bindMopubNative context == null###");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
        } else if (rootLayout > 0) {
            rootView = LayoutInflater.from(context).inflate(rootLayout, null);
            bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
        } else if (cardId > 0) {
            bindNativeWithCard(context, cardId, nativeAd, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find Mopub native layout###");
        }
    }

    private void bindNativeWithCard(Context context, int template, MoPubNative nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.native_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        }
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(context, rootView, nativeAd, pidConfig);
    }

    /**
     * 使用模板显示原生广告
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithTemplate(Context context, View rootView, MoPubNative nativeAd, PidConfig pidConfig) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        mParams.setAdPrivacy(R.id.native_adprivacy);
        bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, View rootView, MoPubNative nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootView == null");
            return;
        }
        if (nativeAd == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView nativeAd == null");
            return;
        }
        if (pidConfig == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView pidConfig == null");
            return;
        }

        if (mParams == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView mParams == null");
            return;
        }

        bindVideoRender(context, nativeAd);
        bindStaticRender(context, nativeAd);

    }

    private void bindVideoRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        }
        MoPubVideoAdRender mopubVideoRender = new MoPubVideoAdRender(getVideoViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(mopubVideoRender);
    }

    private MediaViewBinder getVideoViewBinder(Context context, View layout) {
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        MediaLayout mediaLayout = createMediaLayout(context);
        mediaLayout.setId(0x10001);
        coverLayout.addView(mediaLayout);

        MediaViewBinder videoViewBinder = new MediaViewBinder.Builder(mParams.getNativeRootLayout())
                .mediaLayoutId(mediaLayout.getId())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(mParams.getAdPrivacy())
                .build();
        return videoViewBinder;
    }

    private void bindStaticRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        }
        MoPubStaticAdRender moPubAdRenderer = new MoPubStaticAdRender(getStaticViewBinder(), layout);
        nativeAd.registerAdRenderer(moPubAdRenderer);
    }

    private ViewBinder getStaticViewBinder() {
        ViewBinder viewBinder = new ViewBinder.Builder(mParams.getNativeRootLayout())
                .mainImageId(mParams.getAdCover())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(mParams.getAdPrivacy())
                .build();
        return viewBinder;
    }

    private MediaLayout createMediaLayout(Context context) {
        try {
            return new MediaLayout(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }
}