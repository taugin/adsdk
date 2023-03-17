package com.tiger.adsdk.adloader.inmobi;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inmobi.ads.InMobiNative;
import com.mbridge.msdk.out.MBNativeHandler;
import com.tiger.adsdk.adloader.base.BaseBindNativeView;
import com.tiger.adsdk.constant.Constant;
import com.tiger.adsdk.core.framework.Params;
import com.tiger.adsdk.data.config.PidConfig;
import com.tiger.adsdk.http.Http;
import com.tiger.adsdk.http.OnImageCallback;
import com.tiger.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/11.
 */

public class InmobiBindView extends BaseBindNativeView {
    private Params mParams;

    public void bindInmobiNative(Params params, Context context, ViewGroup adContainer, InMobiNative inMobiNative, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindInmobiNative mParams == null###");
            return;
        }
        if (context == null) {
            Log.e(Log.TAG, "bindInmobiNative context == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindInmobiNative adContainer == null###");
            return;
        }
        if (pidConfig == null) {
            Log.e(Log.TAG, "bindInmobiNative pidconfig == null###");
            return;
        }
        int rootLayout = getBestNativeLayout(adContainer.getContext(), pidConfig, mParams, Constant.AD_SDK_INMOBI);
        if (rootLayout > 0) {
            bindNativeViewWithRootView(context, adContainer, rootLayout, inMobiNative, pidConfig, mbNativeHandler);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootLayout
     * @param inMobiNative
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, ViewGroup adContainer, int rootLayout, InMobiNative inMobiNative, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
        if (rootLayout <= 0) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootLayout == 0x0");
            return;
        }
        if (inMobiNative == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView inMobiNative == null");
            return;
        }
        if (pidConfig == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView pidConfig == null");
            return;
        }

        View rootView = LayoutInflater.from(context).inflate(rootLayout, null);
        bindInmobiRender(context, adContainer, inMobiNative, rootView, pidConfig, mbNativeHandler);

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

    ///////////////////////////////////Bind Inmobi Render start////////////////////////////////////////
    private void bindInmobiRender(Context context, ViewGroup adContainer, InMobiNative inMobiNative, View layout, PidConfig pidConfig, MBNativeHandler mbNativeHandler) {
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

        // 设置广告元素内容
        if (!TextUtils.isEmpty(inMobiNative.getAdTitle())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(inMobiNative.getAdTitle());
                titleView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(inMobiNative.getAdDescription())) {
            if (bodyView instanceof TextView) {
                ((TextView) bodyView).setText(inMobiNative.getAdDescription());
                bodyView.setVisibility(View.VISIBLE);
            }
        }

        if (!TextUtils.isEmpty(inMobiNative.getAdCtaText())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(inMobiNative.getAdCtaText());
                ctaView.setVisibility(View.VISIBLE);
            }
        }

        String iconUrl = inMobiNative.getAdIconUrl();
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
                        Log.iv(Log.TAG, "inmobi load image error : " + error);
                    }
                });
            }
        }

        if (mediaLayout != null) {
            mediaLayout.post(new Runnable() {
                @Override
                public void run() {
                    int width = mediaLayout.getWidth();
                    final View primaryView =
                            inMobiNative.getPrimaryViewOfWidth(context, null, mediaLayout,
                                    width);
                    if (primaryView != null) {
                        mediaLayout.addView(primaryView);
                    }
                }
            });
        }
        reportAdClick(clickView, inMobiNative);

        putInmobiInfo(inMobiNative);
    }

    private void reportAdClick(List<View> list, InMobiNative inMobiNative) {
        try {
            for (View view : list) {
                if (view != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (inMobiNative != null) {
                                inMobiNative.reportAdClickAndOpenLandingPage();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
        }
    }

    ///////////////////////////////////Bind Inmobi Render end////////////////////////////////////////
    private void putInmobiInfo(InMobiNative inMobiNative) {
        if (inMobiNative != null) {
            try {
                putValue(AD_TITLE, inMobiNative.getAdTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, inMobiNative.getAdDescription());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, inMobiNative.getAdCtaText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, inMobiNative.getAdIconUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, String.valueOf(inMobiNative.getAdRating()));
            } catch (Exception e) {
            }
        }
    }
}