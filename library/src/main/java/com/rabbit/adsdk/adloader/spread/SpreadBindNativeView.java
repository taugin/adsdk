package com.rabbit.adsdk.adloader.spread;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.http.OnImageCallback;
import com.rabbit.adsdk.log.Log;
import com.rabbit.sunny.SpreadCfg;

/**
 * Created by Administrator on 2018/4/26.
 */

public class SpreadBindNativeView extends BaseBindNativeView {
    private Params mParams;
    private SpLoader.ClickClass mClickClass;

    public void setClickListener(SpLoader.ClickClass clickListener) {
        mClickClass = clickListener;
    }

    public void bindNative(Params params, ViewGroup adContainer, PidConfig pidConfig, SpreadCfg spreadCfg) {
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
        if (rootLayout <= 0 && mParams.getNativeCardStyle() > 0) {
            rootLayout = getAdViewLayout(adContainer.getContext(), mParams.getNativeCardStyle(), pidConfig);
            bindParamsViewId(mParams);
        }

        if (rootLayout > 0) {
            bindNativeViewWithRootView(adContainer, rootLayout, pidConfig, spreadCfg);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, PidConfig pidConfig, SpreadCfg spreadCfg) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootLayout <= 0) {
            throw new AndroidRuntimeException("rootLayout is 0x0");
        }
        View view = null;
        try {
            View rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
            view = showUnifiedAdView(rootView, pidConfig, spreadCfg);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        try {
            adContainer.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            adContainer.addView(view, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private View showUnifiedAdView(View rootView, PidConfig pidConfig, final SpreadCfg spreadCfg) throws Exception {
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = rootView.findViewById(mParams.getAdCover());
        View detailView = rootView.findViewById(mParams.getAdDetail());
        View ctaView = rootView.findViewById(mParams.getAdAction());

        final View adIconView = rootView.findViewById(mParams.getAdIcon());

        // 设置广告元素内容
        if (!TextUtils.isEmpty(spreadCfg.getTitle())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(spreadCfg.getTitle());
                titleView.setVisibility(View.VISIBLE);
                titleView.setOnClickListener(mClickClass);
            }
        }

        if (!TextUtils.isEmpty(spreadCfg.getDetail())) {
            if (detailView instanceof TextView) {
                ((TextView) detailView).setText(spreadCfg.getDetail());
                detailView.setVisibility(View.VISIBLE);
                detailView.setOnClickListener(mClickClass);
            }
        }

        if (!TextUtils.isEmpty(spreadCfg.getCta())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(spreadCfg.getCta());
                ctaView.setVisibility(View.VISIBLE);
                ctaView.setOnClickListener(mClickClass);
            }
        }

        String iconUrl = spreadCfg.getIcon();
        if (iconUrl != null) {
            if (adIconView instanceof ImageView) {
                loadAndShowImage((ImageView) adIconView, iconUrl);
                adIconView.setVisibility(View.VISIBLE);
                adIconView.setOnClickListener(mClickClass);
            }
        }

        ViewGroup mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        ImageView mediaView = new ImageView(mediaViewLayout.getContext());
        if (mediaViewLayout != null && mediaView != null) {
            mediaView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAndShowImage(mediaView, spreadCfg.getBanner());
            mediaViewLayout.addView(mediaView, -1, -1);
            mediaViewLayout.setVisibility(View.VISIBLE);
            mediaView.setOnClickListener(mClickClass);
        }
        putAdvertiserInfo(spreadCfg);
        return rootView;
    }

    private void loadAndShowImage(final ImageView imageView, String url) {
        try {
            Http.get(imageView.getContext()).loadImage(url, null, new OnImageCallback() {

                @Override
                public void onSuccess(Bitmap bitmap) {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFailure(int code, String error) {
                }
            });
        } catch (Exception e) {
        }
    }

    private void putAdvertiserInfo(SpreadCfg spreadCfg) {
        try {
            putValue(AD_TITLE, spreadCfg.getTitle());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, spreadCfg.getDetail());
        } catch (Exception e) {
        }
        try {
            putValue(AD_COVER, spreadCfg.getBanner());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, spreadCfg.getCta());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ICON, spreadCfg.getIcon());
        } catch (Exception e) {
        }
    }
}
