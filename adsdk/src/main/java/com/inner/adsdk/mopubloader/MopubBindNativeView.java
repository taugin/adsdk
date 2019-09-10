package com.inner.adsdk.mopubloader;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.ads.AdIconView;
import com.facebook.ads.MediaView;
import com.inner.adsdk.baseloader.BaseBindNativeView;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;
import com.mopub.nativeads.FacebookAdRenderer;
import com.mopub.nativeads.GooglePlayServicesAdRenderer;
import com.mopub.nativeads.InMobiNativeAdRenderer;
import com.mopub.nativeads.MediaLayout;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.ViewBinder;
import com.sub.great.good.R;

/**
 * Created by Administrator on 2018/2/11.
 */

public class MopubBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindMopubNative(Params params, Context context, MoPubNative nativeAd) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "********************mParams is null");
            return;
        }
        if (context == null) {
            Log.e(Log.TAG, "********************context is null");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(context, rootView, nativeAd);
        } else if (rootLayout > 0) {
            rootView = LayoutInflater.from(context).inflate(rootLayout, null);
            bindNativeViewWithRootView(context, rootView, nativeAd);
        } else if (cardId > 0) {
            bindNativeWithCard(context, cardId, nativeAd);
        } else {
            Log.e(Log.TAG, "********************mopub layout is null");
        }
    }

    private void bindNativeWithCard(Context context, int template, MoPubNative nativeAd) {
        int layoutId = R.layout.cpu_card_2;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.cpu_card_0;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.cpu_card_1;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.cpu_card_2;
        } else if (template == Constant.NATIVE_CARD_FULL) {
            layoutId = R.layout.cpu_card_3;
        }
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(context, rootView, nativeAd);
    }

    /**
     * 使用模板显示原生广告
     *
     * @param rootView
     * @param nativeAd
     */
    private void bindNativeViewWithTemplate(Context context, View rootView, MoPubNative nativeAd) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        mParams.setAdRootView(rootView);
        bindNativeViewWithRootView(context, rootView, nativeAd);
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootView
     * @param nativeAd
     */
    private void bindNativeViewWithRootView(Context context, View rootView, MoPubNative nativeAd) {
        if (rootView == null) {
            Log.v(Log.TAG, "********************rootView is null");
            return;
        }
        if (nativeAd == null) {
            Log.v(Log.TAG, "********************nativeAd is null");
            return;
        }

        if (mParams == null) {
            Log.v(Log.TAG, "********************mParams is null");
            return;
        }

        // 恢复icon图标
        try {
            restoreIconView(rootView, mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindVideoRender(context, nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }

        try {
            restoreIconView(rootView, mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindStaticRender(context, nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }

        try {
            restoreIconView(rootView, mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindAdMobRender(context, nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }

        try {
            restoreIconView(rootView, mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindFBRender(context, nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }

        try {
            restoreIconView(rootView, mParams.getAdIcon());
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindInmobiRender(context, nativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindVideoRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "********************bindVideoRender  root layout is null");
        }
        MoPubVideoAdRender mopubVideoRender = new MoPubVideoAdRender(getVideoViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(mopubVideoRender);
    }

    private MediaViewBinder getVideoViewBinder(Context context, View layout) {
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        MediaLayout mediaLayout = createMediaLayout(context);
        mediaLayout.setId(getMediaLayoutId());
        coverLayout.addView(mediaLayout);

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = createImageView(context);
        imageView.setId(getImageViewId());
        int size = Utils.dp2px(context, 20);
        adChoiceLayout.addView(imageView, size, size);

        MediaViewBinder videoViewBinder = new MediaViewBinder.Builder(mParams.getNativeRootLayout())
                .mediaLayoutId(mediaLayout.getId())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(imageView.getId())
                .build();
        return videoViewBinder;
    }

    private void bindStaticRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "********************bindStaticRender root layout is null");
        }
        MoPubStaticAdRender moPubAdRenderer = new MoPubStaticAdRender(getStaticViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(moPubAdRenderer);
    }

    private ViewBinder getStaticViewBinder(Context context, View layout) {
        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = createImageView(context);
        imageView.setId(getImageViewId());
        int size = Utils.dp2px(context, 20);
        adChoiceLayout.addView(imageView, size, size);

        ViewBinder viewBinder = new ViewBinder.Builder(mParams.getNativeRootLayout())
                .mainImageId(mParams.getAdCover())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(imageView.getId())
                .addExtra(InMobiNativeAdRenderer.VIEW_BINDER_KEY_PRIMARY_AD_VIEW_LAYOUT, mParams.getAdMediaView())
                .build();
        return viewBinder;
    }

    private void bindAdMobRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "********************bindAdMobRender root layout is null");
        }
        GooglePlayServicesAdRenderer adRender = new GooglePlayServicesAdRenderer(getVideoViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(adRender);
    }

    private void bindFBRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "********************bindFBRender root layout is null");
        }

        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        MediaView mediaView = createMediaView(context);
        mediaView.setId(getMediaViewId());
        coverLayout.addView(mediaView);

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        RelativeLayout adChoiceRelativeLayout = new RelativeLayout(layout.getContext());
        adChoiceRelativeLayout.setId(getRelativeLayoutId());
        adChoiceLayout.addView(adChoiceRelativeLayout);

        AdIconView iconView = createAdIconView(layout.getContext(),
                (ImageView) layout.findViewById(mParams.getAdIcon()));

        FacebookAdRenderer.FacebookViewBinder binder =
                new FacebookAdRenderer.FacebookViewBinder.Builder(mParams.getNativeRootLayout())
                        .titleId(mParams.getAdTitle())
                        .textId(mParams.getAdDetail())
                        .advertiserNameId(mParams.getAdSponsored())
                        .callToActionId(mParams.getAdAction())
                        .mediaViewId(mediaView.getId())
                        .adIconViewId(iconView.getId())
                        .adChoicesRelativeLayoutId(adChoiceRelativeLayout.getId())
                        .build();

        FacebookAdRenderer render = new FacebookAdRenderer(binder, layout);
        nativeAd.registerAdRenderer(render);
    }

    private AdIconView createAdIconView(Context context, ImageView icon) {
        AdIconView iconView = createAdIconView(context);
        if (icon != null && iconView != null) {
            iconView.setId(icon.getId());
            ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
            android.widget.RelativeLayout.LayoutParams iconViewParams = new android.widget.RelativeLayout.LayoutParams(iconParams.width, iconParams.height);
            if (iconParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) iconParams;
                iconViewParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
            }
            if (iconParams instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) iconParams;
                int[] rules = mainImageViewRelativeLayoutParams.getRules();

                for (int i = 0; i < rules.length; ++i) {
                    iconViewParams.addRule(i, rules[i]);
                }
            }
            ViewGroup viewGroup = (ViewGroup) icon.getParent();
            if (viewGroup != null) {
                int index = viewGroup.indexOfChild(icon);
                viewGroup.removeView(icon);
                viewGroup.addView(iconView, index, iconViewParams);
            }
        }
        return iconView;
    }

    private void bindInmobiRender(Context context, MoPubNative nativeAd) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "********************bindInmobiRender root layout is null");
        }
        InMobiNativeAdRenderer render = new InMobiNativeAdRenderer(getStaticViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(render);
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

    private MediaView createMediaView(Context context) {
        try {
            return new MediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private AdIconView createAdIconView(Context context) {
        try {
            return new AdIconView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private ImageView createImageView(Context context) {
        try {
            return new ImageView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private int getMediaLayoutId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000001;
    }

    private int getImageViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000002;
    }

    private int getRelativeLayoutId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000003;
    }

    private int getMediaViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000004;
    }
}