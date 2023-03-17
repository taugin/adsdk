package com.rabbit.adsdk.adloader.mintegral;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mbridge.msdk.nativex.view.MBMediaView;
import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.out.MBNativeHandler;
import com.mbridge.msdk.widget.MBAdChoice;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.http.OnImageCallback;
import com.rabbit.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/11.
 */

public class MintegralBindView extends BaseBindNativeView {
    private Params mParams;

    public void bindMintegralNative(Params params, Context context, ViewGroup adContainer, Campaign campaign, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindMintegralNative mParams == null###");
            return;
        }
        if (context == null) {
            Log.e(Log.TAG, "bindMintegralNative context == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindMintegralNative adContainer == null###");
            return;
        }
        if (pidConfig == null) {
            Log.e(Log.TAG, "bindMintegralNative pidconfig == null###");
            return;
        }
        int rootLayout = getBestNativeLayout(adContainer.getContext(), pidConfig, mParams, Constant.AD_SDK_MINTEGRAL);
        if (rootLayout > 0) {
            bindNativeViewWithRootView(context, adContainer, rootLayout, campaign, pidConfig, mbNativeHandler);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootLayout
     * @param campaign
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, ViewGroup adContainer, int rootLayout, Campaign campaign, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
        if (rootLayout <= 0) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootLayout == 0x0");
            return;
        }
        if (campaign == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView campaign == null");
            return;
        }
        if (pidConfig == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView pidConfig == null");
            return;
        }

        View rootView = LayoutInflater.from(context).inflate(rootLayout, null);
        bindMintegralRender(context, adContainer, campaign, rootView, pidConfig, mbNativeHandler);

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

    ///////////////////////////////////Bind Mintegral Render start////////////////////////////////////////
    private void bindMintegralRender(Context context, ViewGroup adContainer, Campaign campaign, View layout, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
        View titleView = layout.findViewById(mParams.getAdTitle());
        View adCoverView = layout.findViewById(mParams.getAdCover());
        View bodyView = layout.findViewById(mParams.getAdDetail());
        View ctaView = layout.findViewById(mParams.getAdAction());
        View adIconView = layout.findViewById(mParams.getAdIcon());
        View coverView = layout.findViewById(mParams.getAdCover());
        ViewGroup mediaLayout = layout.findViewById(mParams.getAdMediaView());
        ViewGroup adChoiceContainer = layout.findViewById(mParams.getAdChoices());

        List<View> clickView = new ArrayList<>();

        if (titleView != null && isClickable(AD_TITLE, pidConfig)) {
            clickView.add(titleView);
        }
        if (adCoverView != null && isClickable(AD_COVER, pidConfig)) {
            clickView.add(adCoverView);
        }
        if (bodyView != null && isClickable(AD_DETAIL, pidConfig)) {
            clickView.add(bodyView);
        }
        if (ctaView != null && isClickable(AD_CTA, pidConfig)) {
            clickView.add(ctaView);
        }
        if (adIconView != null && isClickable(AD_ICON, pidConfig)) {
            clickView.add(adIconView);
        }
        if (layout != null && isClickable(AD_BACKGROUND, pidConfig)) {
            clickView.add(layout);
        }
        try {
            if (mbNativeHandler != null) {
                mbNativeHandler.registerView(adChoiceContainer, clickView, campaign);
            }
        } catch (Exception e) {
        }

        // 设置广告元素内容
        if (!TextUtils.isEmpty(campaign.getAppName())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(campaign.getAppName());
                titleView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(campaign.getAppDesc())) {
            if (bodyView instanceof TextView) {
                ((TextView) bodyView).setText(campaign.getAppDesc());
                bodyView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(campaign.getAdCall())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(campaign.getAdCall());
                ctaView.setVisibility(View.VISIBLE);
            }
        }

        String iconUrl = campaign.getIconUrl();
        if (!TextUtils.isEmpty(iconUrl)) {
            if (adIconView instanceof ImageView) {
                Http.get(coverView.getContext()).loadImage(iconUrl, null, new OnImageCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        ((ImageView) adIconView).setImageBitmap(bitmap);
                        adIconView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        Log.iv(Log.TAG, "mintegral load image error : " + error);
                    }
                });
            }
        }

        String imageCoverUrl = campaign.getImageUrl();
        if (!TextUtils.isEmpty(imageCoverUrl)) {
            if (coverView instanceof ImageView) {
                Http.get(coverView.getContext()).loadImage(imageCoverUrl, null, new OnImageCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        ((ImageView) coverView).setImageBitmap(bitmap);
                        coverView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        Log.iv(Log.TAG, "mintegral load image error : " + error);
                    }
                });
            }
        }

        // Add adChoices icon
        if (adChoiceContainer != null) {
            MBAdChoice mbAdChoice = new MBAdChoice(context);
            mbAdChoice.setCampaign(campaign);
            adChoiceContainer.addView(mbAdChoice);
        }

        if (mediaLayout != null) {
            MBMediaView mbMediaView = new MBMediaView(context);
            mbMediaView.setNativeAd(campaign);
            mediaLayout.addView(mbMediaView, -1, -1);
        }
        putMintegralInfo(campaign);
    }

    ///////////////////////////////////Bind Mintegral Render end////////////////////////////////////////
    private void putMintegralInfo(Campaign campaign) {
        if (campaign != null) {
            try {
                putValue(AD_TITLE, campaign.getAppName());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, campaign.getAppDesc());
            } catch (Exception e) {
            }
            try {
                putValue(AD_COVER, campaign.getImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, campaign.getAdCall());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, campaign.getIconUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, String.valueOf(campaign.getRating()));
            } catch (Exception e) {
            }
        }
    }
}