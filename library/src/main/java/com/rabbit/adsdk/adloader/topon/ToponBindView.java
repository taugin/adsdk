package com.rabbit.adsdk.adloader.topon;

import android.content.Context;
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
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

public class ToponBindView extends BaseBindNativeView {

    public class NativeCustomRender implements ATNativeAdRenderer<CustomNativeAd> {
        private Context mContext;
        List<View> mClickView = new ArrayList<>();
        View mDevelopView;
        int mNetworkFirmId;
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
            TextView adFromView = view.findViewById(mParams.getAdSocial());
            ViewGroup contentArea = view.findViewById(mParams.getAdMediaView());
            FrameLayout iconArea = imageToViewGroup(mContext, view.findViewById(mParams.getAdIcon()));
            ViewGroup adChoiceLayout = view.findViewById(mParams.getAdChoices());
            ATNativeImageView logoView = new ATNativeImageView(mContext);
            adChoiceLayout.addView(logoView);

            titleView.setText("");
            descView.setText("");
            ctaView.setText("");
            adFromView.setText("");
            titleView.setText("");
            contentArea.removeAllViews();
            iconArea.removeAllViews();
            logoView.setImageDrawable(null);

            View adIconView = customNativeAd.getAdIconView();
            if (adIconView != null) {
                iconArea.addView(adIconView);
                if (isClickable(AD_ICON, mPidConfig)) {
                    mClickView.add(adIconView);
                }
            } else {
                final ATNativeImageView atNativeImageView = new ATNativeImageView(mContext);
                iconArea.addView(atNativeImageView);
                atNativeImageView.setImage(customNativeAd.getIconImageUrl());
                if (isClickable(AD_ICON, mPidConfig)) {
                    mClickView.add(atNativeImageView);
                }
            }

            Log.iv(Log.TAG, "topon ad choice icon url : " + customNativeAd.getAdChoiceIconUrl());
            if (!TextUtils.isEmpty(customNativeAd.getAdChoiceIconUrl())) {
                logoView.setImage(customNativeAd.getAdChoiceIconUrl());
            }

            View mediaView = customNativeAd.getAdMediaView(contentArea, contentArea.getWidth());
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

            titleView.setText(customNativeAd.getTitle());
            descView.setText(customNativeAd.getDescriptionText());
            ctaView.setText(customNativeAd.getCallToActionText());
            String adFromText = customNativeAd.getAdFrom();
            Log.iv(Log.TAG, "topon ad from text : " + adFromText);
            if (adFromView != null) {
                if (!TextUtils.isEmpty(adFromText)) {
                    adFromView.setText(adFromText != null ? adFromText : "");
                    adFromView.setVisibility(View.VISIBLE);
                } else {
                    adFromView.setVisibility(View.GONE);
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
