package com.rabbit.adsdk.adloader.topon;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.core.api.ATAdInfo;
import com.anythink.nativead.api.ATNativeAdRenderer;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeImageView;
import com.anythink.nativead.api.NativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.mopub.common.util.Drawables;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ToponBindView extends BaseBindNativeView {

    public class NativeCustomRender implements ATNativeAdRenderer<CustomNativeAd> {
        private Context mContext;
        List<View> mClickView = new ArrayList<>();
        private NativeAd mNativeAd;
        private PidConfig mPidConfig;
        private Params mParams;
        private ViewGroup mAdContainer;

        public NativeCustomRender(Context context, ViewGroup viewGroup, NativeAd nativeAd, PidConfig pidConfig, Params params) {
            mContext = context;
            mNativeAd = nativeAd;
            mPidConfig = pidConfig;
            mParams = params;
            mAdContainer = viewGroup;
        }

        private String getNetwork(ATAdInfo atAdInfo) {
            if (atAdInfo != null) {
                int networkFirmId = atAdInfo.getNetworkFirmId();
                String networkName = ToponLoader.sNetworkFirmTable.get(networkFirmId);
                if (TextUtils.isEmpty(networkName)) {
                    networkName = String.valueOf(networkFirmId);
                } else {
                    networkName = networkName.toLowerCase();
                }
                return networkName;
            }
            return null;
        }

        @Override
        public View createView(Context context, int i) {
            String sourceName = null;
            try {
                sourceName = getNetwork(mNativeAd.getAdInfo());
            } catch (Exception e) {
            }
            int adRootLayout = getBestNativeLayout(mContext, mPidConfig, mParams, sourceName);
            if (adRootLayout > 0) {
                return LayoutInflater.from(mContext).inflate(adRootLayout, null);
            } else {
                Log.e(Log.TAG, "Can not find " + mPidConfig.getSdk() + " native layout###");
            }
            return new FrameLayout(mContext);
        }

        @Override
        public void renderAdView(View view, CustomNativeAd customNativeAd) {
            if (customNativeAd.isNativeExpress()) {
                View mediaView = customNativeAd.getAdMediaView(mAdContainer, -1);
                mAdContainer.addView(mediaView, -1, -1);
                return;
            }
            mClickView.clear();
            TextView titleView = view.findViewById(mParams.getAdTitle());
            TextView descView = view.findViewById(mParams.getAdDetail());
            TextView ctaView = view.findViewById(mParams.getAdAction());
            TextView adSocial = view.findViewById(mParams.getAdSocial());
            ViewGroup contentArea = view.findViewById(mParams.getAdMediaView());
            FrameLayout iconArea = imageToViewGroup(mContext, view.findViewById(mParams.getAdIcon()));
            ViewGroup adChoiceLayout = view.findViewById(mParams.getAdChoices());

            if (titleView != null) {
                titleView.setText("");
            }
            if (descView != null) {
                descView.setText("");
            }
            if (ctaView != null) {
                ctaView.setText("");
            }
            if (adSocial != null) {
                adSocial.setText("");
            }
            if (titleView != null) {
                titleView.setText("");
            }
            if (contentArea != null) {
                contentArea.removeAllViews();
            }
            if (iconArea != null) {
                iconArea.removeAllViews();
            }
            if (adChoiceLayout != null) {
                adChoiceLayout.removeAllViews();
            }

            View adIconView = customNativeAd.getAdIconView();
            if (adIconView != null && iconArea != null) {
                iconArea.addView(adIconView);
                if (isClickable(AD_ICON, mPidConfig)) {
                    mClickView.add(adIconView);
                }
            } else {
                if (iconArea != null) {
                    final ATNativeImageView atNativeImageView = new ATNativeImageView(mContext);
                    iconArea.addView(atNativeImageView);
                    atNativeImageView.setImage(customNativeAd.getIconImageUrl());
                    if (isClickable(AD_ICON, mPidConfig)) {
                        mClickView.add(atNativeImageView);
                    }
                }
            }

            if (adChoiceLayout != null) {
                String adChoiceIconUrl = customNativeAd.getAdChoiceIconUrl();
                Bitmap logoBmp = customNativeAd.getAdLogo();
                View logoView = customNativeAd.getAdLogoView();
                Log.iv(Log.TAG, "topon ad choice logo url : " + adChoiceIconUrl);
                Log.iv(Log.TAG, "topon ad choice logo bitmap : " + logoBmp);
                Log.iv(Log.TAG, "topon ad choice logo view : " + logoView);
                if (!TextUtils.isEmpty(adChoiceIconUrl)) {
                    ATNativeImageView atNativeImageView = new ATNativeImageView(mContext);
                    atNativeImageView.setImage(customNativeAd.getAdChoiceIconUrl());
                    adChoiceLayout.addView(atNativeImageView);
                } else if (logoBmp != null) {
                    ATNativeImageView atNativeImageView = new ATNativeImageView(mContext);
                    adChoiceLayout.addView(atNativeImageView);
                    atNativeImageView.setImageBitmap(logoBmp);
                } else if (logoView != null) {
                    adChoiceLayout.addView(logoView);
                } else {
                    String sourceName = null;
                    try {
                        sourceName = getNetwork(mNativeAd.getAdInfo());
                    } catch (Exception e) {
                    }
                    if (Constant.AD_SDK_MOPUB.equalsIgnoreCase(sourceName)) {
                        ImageView imageView = new ImageView(mContext);
                        imageView.setImageDrawable(Drawables.NATIVE_PRIVACY_INFORMATION_ICON.createDrawable(mContext));
                        int size = Utils.dp2px(mContext, 24);
                        adChoiceLayout.addView(imageView, size, size);
                    }
                }
            }
            if (contentArea != null) {
                int width = contentArea.getWidth();
                if (width == 0) {
                    width = -1;
                }
                View mediaView = customNativeAd.getAdMediaView(contentArea, width);
                if (mediaView != null) {
                    if (mediaView.getParent() != null) {
                        ((ViewGroup) mediaView.getParent()).removeView(mediaView);
                    }
                    contentArea.addView(mediaView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                } else {
                    final ATNativeImageView imageView = new ATNativeImageView(mContext);
                    imageView.setImage(customNativeAd.getMainImageUrl());
                    ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    imageView.setLayoutParams(params);
                    contentArea.addView(imageView, params);
                    if (isClickable(AD_COVER, mPidConfig)) {
                        mClickView.add(imageView);
                    }
                }
            }

            if (titleView != null) {
                titleView.setText(customNativeAd.getTitle());
            }
            if (descView != null) {
                descView.setText(customNativeAd.getDescriptionText());
            }
            if (ctaView != null) {
                ctaView.setText(customNativeAd.getCallToActionText());
            }
            String adFromText = customNativeAd.getAdFrom();
            Log.iv(Log.TAG, "topon ad from text : " + adFromText);
            if (adSocial != null) {
                if (!TextUtils.isEmpty(adFromText)) {
                    adSocial.setText(adFromText != null ? adFromText : "");
                    adSocial.setVisibility(View.VISIBLE);
                } else {
                    adSocial.setVisibility(View.GONE);
                }
            }
            if (isClickable(AD_TITLE, mPidConfig)) {
                mClickView.add(titleView);
            }
            if (isClickable(AD_DETAIL, mPidConfig)) {
                mClickView.add(descView);
            }
            if (isClickable(AD_CTA, mPidConfig)) {
                mClickView.add(ctaView);
            }
        }

        public List<View> getClickView() {
            return mClickView;
        }
    }

    public void bindNativeView(Context context, ViewGroup viewGroup, NativeAd nativeAd, PidConfig pidConfig, Params params) {
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        NativeCustomRender render = new NativeCustomRender(context, viewGroup, nativeAd, pidConfig, params);
        ATNativeAdView atNativeAdView = new ATNativeAdView(context);
        viewGroup.addView(atNativeAdView, -1, -2);
        nativeAd.renderAdView(atNativeAdView, render);
        nativeAd.prepare(atNativeAdView, render.getClickView(), null);
    }

    private FrameLayout imageToViewGroup(Context context, ImageView icon) {
        FrameLayout iconArea = null;
        if (icon != null) {
            iconArea = new FrameLayout(context);
            if (iconArea != null) {
                iconArea.setId(icon.getId());
                iconArea.setVisibility(icon.getVisibility());
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
                    viewGroup.addView(iconArea, index, iconViewParams);
                }
            }
        }
        return iconArea;
    }
}
