package com.rabbit.adsdk.adloader.mopub;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.MediaView;
import com.mbridge.msdk.nativex.view.MBMediaView;
import com.mbridge.msdk.widget.MBAdChoice;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.FacebookAdRenderer;
import com.mopub.nativeads.GooglePlayServicesMediaLayout;
import com.mopub.nativeads.GooglePlayServicesNative;
import com.mopub.nativeads.GooglePlayServicesViewBinder;
import com.mopub.nativeads.MintegralAdRenderer;
import com.mopub.nativeads.MintegralNative;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.BlockAdsManager;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/11.
 */

public class MopubBindNativeView extends BaseBindNativeView {
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
        if (pidConfig == null) {
            Log.e(Log.TAG, "bindMopubNative pidconfig == null###");
            return;
        }
        boolean useCardStyle;
        int rootLayout = mParams.getNativeRootLayout();
        int cardStyle = mParams.getNativeCardStyle();
        if (rootLayout > 0) {
            useCardStyle = false;
        } else {
            useCardStyle = true;
            rootLayout = getAdViewLayout(context, cardStyle, pidConfig);
            bindParamsViewId(mParams);
        }
        if (rootLayout > 0) {
            bindNativeViewWithRootView(context, rootLayout, nativeAd, pidConfig, useCardStyle);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootLayout
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, int rootLayout, MoPubNative nativeAd, PidConfig pidConfig, boolean useCardStyle) {
        if (rootLayout <= 0) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootLayout == 0x0");
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

        View rootView = null;
        String placeName = pidConfig.getPlaceName();
        try {
            int mopubRootLayout;
            if (useCardStyle) {
                mopubRootLayout = getSubNativeLayout(pidConfig, Constant.AD_SDK_MOPUB);
                if (mopubRootLayout > 0) {
                    Log.iv(Log.TAG, "bind mopub layout");
                    bindParamsViewId(mParams);
                } else {
                    mopubRootLayout = rootLayout;
                }
            } else {
                mopubRootLayout = rootLayout;
            }
            rootView = LayoutInflater.from(context).inflate(mopubRootLayout, null);
            bindMopubcRender(context, nativeAd, rootView);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }

        if (!BlockAdsManager.get(context).isBlockAds(Constant.AD_SDK_ADMOB, placeName)) {
            try {
                int admobRootLayout;
                if (useCardStyle) {
                    admobRootLayout = getSubNativeLayout(pidConfig, Constant.AD_SDK_ADMOB);
                    if (admobRootLayout > 0) {
                        Log.iv(Log.TAG, "bind admob layout");
                        bindParamsViewId(mParams);
                    } else {
                        admobRootLayout = rootLayout;
                    }
                } else {
                    admobRootLayout = rootLayout;
                }
                rootView = LayoutInflater.from(context).inflate(admobRootLayout, null);
                bindAdMobRender(context, rootView, nativeAd);
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            } catch (Error e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
        } else {
            Log.iv(Log.TAG, "block " + Constant.AD_SDK_ADMOB + " in mopub mediation for place name " + placeName);
        }

        if (!BlockAdsManager.get(context).isBlockAds(Constant.AD_SDK_FACEBOOK, placeName)) {
            try {
                int facebookRootLayout;
                if (useCardStyle) {
                    facebookRootLayout = getSubNativeLayout(pidConfig, Constant.AD_SDK_FACEBOOK);
                    if (facebookRootLayout > 0) {
                        Log.iv(Log.TAG, "bind facebook layout");
                        bindParamsViewId(mParams);
                    } else {
                        facebookRootLayout = rootLayout;
                    }
                } else {
                    facebookRootLayout = rootLayout;
                }
                rootView = LayoutInflater.from(context).inflate(facebookRootLayout, null);
                bindFBRender(context, rootView, nativeAd);
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            } catch (Error e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
        } else {
            Log.iv(Log.TAG, "block " + Constant.AD_SDK_FACEBOOK + " in mopub mediation for place name " + placeName);
        }

        if (!BlockAdsManager.get(context).isBlockAds(Constant.AD_SDK_MINTEGRAL, placeName)) {
            try {
                int mintegralRootLayout;
                if (useCardStyle) {
                    mintegralRootLayout = getSubNativeLayout(pidConfig, Constant.AD_SDK_MINTEGRAL);
                    if (mintegralRootLayout > 0) {
                        Log.iv(Log.TAG, "bind mintegral layout");
                        bindParamsViewId(mParams);
                    } else {
                        mintegralRootLayout = rootLayout;
                    }
                } else {
                    mintegralRootLayout = rootLayout;
                }
                rootView = LayoutInflater.from(context).inflate(mintegralRootLayout, null);
                bindMintegralRender(context, rootView, nativeAd);
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e, e);
            } catch (Error e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
        } else {
            Log.iv(Log.TAG, "block " + Constant.AD_SDK_MINTEGRAL + " in mopub mediation for place name " + placeName);
        }
    }

    ///////////////////////////////////Bind Mopub Render start////////////////////////////////////////
    private void bindMopubcRender(Context context, MoPubNative nativeAd, View layout) {
        MoPubStaticAdRender moPubAdRenderer = new MoPubStaticAdRender(getStaticViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(moPubAdRenderer);
    }

    private ViewBinder getStaticViewBinder(Context context, View layout) {
        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(context);
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
                .build();
        return viewBinder;
    }
    ///////////////////////////////////Bind Mopub Render end////////////////////////////////////////

    ///////////////////////////////////Bind Admob Render start////////////////////////////////////////
    private GooglePlayServicesMediaLayout createGooglePlayServicesMediaLayout(Context context) {
        try {
            return new GooglePlayServicesMediaLayout(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void bindAdMobRender(Context context, View layout, MoPubNative nativeAd) {
        int mediaLayoutId = 0;
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        if (coverLayout != null) {
            GooglePlayServicesMediaLayout mediaLayout = createGooglePlayServicesMediaLayout(context);
            mediaLayout.setId(getAdmobMediaLayoutId());
            coverLayout.addView(mediaLayout);
            mediaLayoutId = mediaLayout.getId();
        }

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        int padding = Utils.dp2px(context, 4);
        adChoiceLayout.setPadding(padding, padding, padding, padding);
        ImageView imageView = createImageView(context);
        imageView.setId(getImageViewId());
        int size = Utils.dp2px(context, 20);
        adChoiceLayout.addView(imageView, size, size);

        GooglePlayServicesViewBinder videoViewBinder = new GooglePlayServicesViewBinder.Builder(mParams.getNativeRootLayout())
                .mediaLayoutId(mediaLayoutId)
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(imageView.getId())
                .build();
        MoPubGoogleAdRenderer adRender = new MoPubGoogleAdRenderer(videoViewBinder, layout);
        nativeAd.registerAdRenderer(adRender);
    }
    ///////////////////////////////////Bind Admob Render end////////////////////////////////////////

    ///////////////////////////////////Bind Facebook Render start////////////////////////////////////////
    private MediaView createFacebookMediaView(Context context) {
        try {
            return new MediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void bindFBRender(Context context, View layout, MoPubNative nativeAd) {
        int mediaViewId = 0;
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        if (coverLayout != null) {
            MediaView mediaView = createFacebookMediaView(context);
            if (mediaView != null) {
                mediaView.setId(getFBMediaViewId());
                coverLayout.addView(mediaView);
                mediaViewId = mediaView.getId();
            }
        }

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        adChoiceLayout.setPadding(0, 0, 0, 0);
        RelativeLayout adChoiceRelativeLayout = new RelativeLayout(layout.getContext());
        adChoiceRelativeLayout.setId(getRelativeLayoutId());
        adChoiceRelativeLayout.setBackgroundColor(Color.parseColor("#88FFFFFF"));
        adChoiceLayout.addView(adChoiceRelativeLayout);

        ImageView imageView = layout.findViewById(mParams.getAdIcon());
        MediaView iconView = createFacebookAdIconView(context, imageView);
        FacebookAdRenderer.FacebookViewBinder binder =
                new FacebookAdRenderer.FacebookViewBinder.Builder(mParams.getNativeRootLayout()/*布局文件没有被使用*/)
                        .titleId(mParams.getAdTitle())
                        .textId(mParams.getAdDetail())
                        .advertiserNameId(mParams.getAdSponsored())
                        .callToActionId(mParams.getAdAction())
                        .mediaViewId(mediaViewId)
                        .adIconViewId(iconView.getId())
                        .adChoicesRelativeLayoutId(adChoiceRelativeLayout.getId())
                        .build();

        MopubFacebookAdRenderer render = new MopubFacebookAdRenderer(binder, layout);
        nativeAd.registerAdRenderer(render);
    }

    private MediaView createFacebookAdIconView(Context context) {
        try {
            return new MediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private MediaView createFacebookAdIconView(Context context, ImageView icon) {
        MediaView iconView = createFacebookAdIconView(context);
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
    ///////////////////////////////////Bind Facebook Render end////////////////////////////////////////

    ///////////////////////////////////Bind Mintegral Render start////////////////////////////////////////
    private MBMediaView createMintegralMediaView(Context context) {
        try {
            return new MBMediaView(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private MBAdChoice createMintegralMBAdChoice(Context context) {
        try {
            return new MBAdChoice(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void bindMintegralRender(Context context, View layout, MoPubNative nativeAd) {
        int mediaViewId = 0;
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        if (coverLayout != null) {
            MBMediaView mediaView = createMintegralMediaView(context);
            if (mediaView != null) {
                mediaView.setId(View.generateViewId());
                coverLayout.addView(mediaView);
                mediaViewId = mediaView.getId();
            }
        }

        int adChoiceId = 0;
        ViewGroup choiceLayout = layout.findViewById(mParams.getAdChoices());
        if (choiceLayout != null) {
            MBAdChoice mbAdChoice = createMintegralMBAdChoice(context);
            if (mbAdChoice != null) {
                mbAdChoice.setId(View.generateViewId());
                choiceLayout.addView(mbAdChoice);
                adChoiceId = mbAdChoice.getId();
            }
        }

        MintegralAdRenderer.ViewBinder viewBinder = new MintegralAdRenderer.ViewBinder.Builder(0)
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .mainImageId(mParams.getAdCover())
                .mediaViewId(mediaViewId)
                .adChoicesId(adChoiceId)
                .callToActionId(mParams.getAdAction())
                .textId(mParams.getAdDetail())
                .build();
        MopubMintegralAdRenderer adRenderer = new MopubMintegralAdRenderer(viewBinder, layout);
        nativeAd.registerAdRenderer(adRenderer);
    }
    ///////////////////////////////////Bind Mintegral Render end////////////////////////////////////////

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

    private int getAdmobMediaLayoutId() {
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

    private int getFBMediaViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000004;
    }

    public void notifyAdViewShowing(View view, PidConfig pidConfig, boolean staticRender) {
        updateCtaButtonBackground(view, pidConfig, mParams);
        updateAdViewVisibility(staticRender, view);
    }

    public void updateClickView(View view, PidConfig pidConfig, String render) {
        if (view == null || pidConfig == null || mParams == null) {
            Log.iv(Log.TAG, "update cv error cause of null view or config or params");
            return;
        }
        try {
            List<String> clickView = getClickView(pidConfig);
            if (clickView == null || clickView.isEmpty()) {
                Log.iv(Log.TAG, "click view empty");
                return;
            }
            List<String> clickViewRender = getClickViewRender(pidConfig);
            if (clickViewRender != null && !clickViewRender.contains(render)) {
                Log.iv(Log.TAG, "click view miss match render : " + render + " , cfg render : " + clickViewRender);
                return;
            }
            Map<String, View> viewMap = new HashMap<String, View>();
            viewMap.put(AD_ICON, view.findViewById(mParams.getAdIcon()));
            viewMap.put(AD_TITLE, view.findViewById(mParams.getAdTitle()));
            viewMap.put(AD_DETAIL, view.findViewById(mParams.getAdDetail()));
            viewMap.put(AD_CTA, view.findViewById(mParams.getAdAction()));
            viewMap.put(AD_MEDIA, view.findViewById(mParams.getAdMediaView()));
            viewMap.put(AD_COVER, view.findViewById(mParams.getAdCover()));
            viewMap.put(AD_CHOICES, view.findViewById(mParams.getAdChoices()));
            viewMap.put(AD_SPONSORED, view.findViewById(mParams.getAdSponsored()));
            viewMap.put(AD_SOCIAL, view.findViewById(mParams.getAdSocial()));
            List<View> clickElements = new ArrayList<View>();
            for (String text : clickView) {
                clickElements.add(viewMap.get(text));
            }
            Log.iv(Log.TAG, "cv : " + clickView + " , render : " + clickViewRender);
            traversalView(view, clickElements);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void traversalView(View view, List<View> enableClickView) {
        if (view == null) {
            return;
        }
        if (!enableClickView.contains(view)) {
            view.setOnClickListener(null);
            view.setClickable(false);
            if (view instanceof ViewGroup) {
                int size = ((ViewGroup) view).getChildCount();
                for (int index = 0; index < size; index++) {
                    traversalView(((ViewGroup) view).getChildAt(index), enableClickView);
                }
            }
        }
    }

    private void updateAdViewVisibility(boolean staticRender, View adView) {
        updateIconView(adView);
        updateDetailView(adView);
        // MediaView和CoverView默认都可见，不再隐藏
        // updateMediaView(staticRender, adView);
    }

    private void updateIconView(View adView) {
        try {
            int iconId = mParams.getAdIcon();
            if (iconId > 0) {
                View view = adView.findViewById(iconId);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();
                    if (drawable != null) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                } else if (view instanceof ViewGroup) {
                    ImageView imageView = null;
                    ViewGroup iconLayout = (ViewGroup) view;
                    if (iconLayout != null) {
                        int count = iconLayout.getChildCount();
                        for (int index = 0; index < count; index++) {
                            View v = iconLayout.getChildAt(index);
                            if (v instanceof ImageView) {
                                imageView = (ImageView) v;
                                break;
                            }
                        }
                        if (imageView != null) {
                            Drawable drawable = imageView.getDrawable();
                            if (drawable != null) {
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void updateDetailView(View adView) {
        try {
            int detailId = mParams.getAdDetail();
            View detailView = adView.findViewById(detailId);
            if (detailView instanceof TextView) {
                String detail = ((TextView) detailView).getText().toString();
                if (!TextUtils.isEmpty(detail)) {
                    detailView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
        }
    }

    public void updateMediaView(boolean staticRender, View adView) {
        if (staticRender) {
            if (mParams.getAdMediaView() > 0) {
                View view = adView.findViewById(mParams.getAdMediaView());
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
            if (mParams.getAdCover() > 0) {
                View view = adView.findViewById(mParams.getAdCover());
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mParams.getAdMediaView() > 0) {
                View view = adView.findViewById(mParams.getAdMediaView());
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            if (mParams.getAdCover() > 0) {
                View view = adView.findViewById(mParams.getAdCover());
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void putAdvertiserInfo(com.mopub.nativeads.NativeAd nativeAd) {
        try {
            BaseNativeAd baseNativeAd = nativeAd.getBaseNativeAd();
            processMopubStaticAd(baseNativeAd);
            processMintegralAd(baseNativeAd);
            processAdmobAd(baseNativeAd);
            processFacebookAd(baseNativeAd);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private boolean processMopubStaticAd(BaseNativeAd baseNativeAd) {
        if (baseNativeAd instanceof StaticNativeAd) {
            StaticNativeAd staticNativeAd = (StaticNativeAd) baseNativeAd;
            putStaticInfo(staticNativeAd);
            return true;
        }
        return false;
    }

    private boolean processMintegralAd(BaseNativeAd baseNativeAd) {
        if (baseNativeAd instanceof MintegralNative.MBridgeNativeAd) {
            MintegralNative.MBridgeNativeAd mintegralAd = (MintegralNative.MBridgeNativeAd) baseNativeAd;
            putMintegralInfo(mintegralAd);
            return true;
        }
        return false;
    }

    private boolean processAdmobAd(BaseNativeAd baseNativeAd) {
        if (baseNativeAd instanceof GooglePlayServicesNative.GooglePlayServicesNativeAd) {
            GooglePlayServicesNative.GooglePlayServicesNativeAd googlePlayServicesNativeAd = (GooglePlayServicesNative.GooglePlayServicesNativeAd) baseNativeAd;
            putAdmobInfo(googlePlayServicesNativeAd);
            return true;
        }
        return false;
    }

    private boolean processFacebookAd(BaseNativeAd baseNativeAd) {
        Class<?> fbNativeAd = null;
        try {
            fbNativeAd = Class.forName("com.mopub.nativeads.FacebookNative$FacebookNativeAd");
        } catch (Exception e) {
        }
        if (fbNativeAd != null) {
            String baseAdClassName = baseNativeAd.getClass().getName();
            String facebookAdClassName = fbNativeAd.getName();
            Log.iv(Log.TAG, "base ad name : " + baseAdClassName + " , facebook ad name : " + facebookAdClassName);
            if (TextUtils.equals(baseAdClassName, facebookAdClassName)) {
                putFacebookInfo(baseNativeAd, fbNativeAd);
                return true;
            }
        }
        return false;
    }

    private void putStaticInfo(StaticNativeAd staticNativeAd) {
        if (staticNativeAd != null) {
            try {
                putValue(AD_TITLE, staticNativeAd.getTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, staticNativeAd.getText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_SPONSORED, staticNativeAd.getSponsored());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CHOICES, staticNativeAd.getPrivacyInformationIconImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_COVER, staticNativeAd.getMainImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, staticNativeAd.getCallToAction());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, staticNativeAd.getIconImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, staticNativeAd.getStarRating().toString());
            } catch (Exception e) {
            }
        }
    }
    private void putMintegralInfo(MintegralNative.MBridgeNativeAd nativeAd) {
        if (nativeAd != null) {
            try {
                putValue(AD_TITLE, nativeAd.getTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, nativeAd.getText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_COVER, nativeAd.getMainImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, nativeAd.getCallToAction());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, nativeAd.getIconUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, String.valueOf(nativeAd.getStarRating()));
            } catch (Exception e) {
            }
        }
    }
    private void putAdmobInfo(GooglePlayServicesNative.GooglePlayServicesNativeAd googleNativeAd) {
        if (googleNativeAd != null) {
            try {
                putValue(AD_TITLE, googleNativeAd.getTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, googleNativeAd.getText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ADVERTISER, googleNativeAd.getAdvertiser());
            } catch (Exception e) {
            }
            try {
                putValue(AD_COVER, googleNativeAd.getMainImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, googleNativeAd.getCallToAction());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, googleNativeAd.getIconImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, googleNativeAd.getStarRating().toString());
            } catch (Exception e) {
            }
        }
    }


    private String getReflectResult(BaseNativeAd baseNativeAd, Class<?> fbNativeAd, String methodName) {
        try {
            return (String) fbNativeAd.getMethod(methodName).invoke(baseNativeAd);
        } catch (Exception e) {
        }
        return null;
    }
    private void putFacebookInfo(BaseNativeAd baseNativeAd, Class<?> fbNativeAd) {
        if (baseNativeAd != null && fbNativeAd != null) {
            try {
                putValue(AD_TITLE, getReflectResult(baseNativeAd, fbNativeAd, "getTitle"));
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, getReflectResult(baseNativeAd, fbNativeAd, "getText"));
            } catch (Exception e) {
            }
            try {
                putValue(AD_ADVERTISER, getReflectResult(baseNativeAd, fbNativeAd, "getAdvertiserName"));
            } catch (Exception e) {
            }
            try {
                putValue(AD_SPONSORED, getReflectResult(baseNativeAd, fbNativeAd, "getSponsoredName"));
            } catch (Exception e) {
            }
            try {
                putValue(AD_CHOICES, getReflectResult(baseNativeAd, fbNativeAd, "getPrivacyInformationIconClickThroughUrl"));
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, getReflectResult(baseNativeAd, fbNativeAd, "getCallToAction"));
            } catch (Exception e) {
            }
        }
    }
}