package com.hauyu.adsdk.adloader.spread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gekes.fvs.tdsvap.SpConfig;
import com.hauyu.adsdk.adloader.base.BaseBindNativeView;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.http.Http;
import com.hauyu.adsdk.http.OnImageCallback;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/4/26.
 */

public class SpreadBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindNative(Params params, ViewGroup adContainer, PidConfig pidConfig, SpConfig spConfig) {
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
            rootLayout = getAdViewLayout(mParams.getNativeCardStyle(), pidConfig);
            bindParamsViewId(mParams);
        }

        if (rootLayout > 0) {
            bindNativeViewWithRootView(adContainer, rootLayout, pidConfig, spConfig);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
        updateCtaButtonBackground(adContainer, pidConfig, mParams);
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, PidConfig pidConfig, SpConfig spConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootLayout <= 0) {
            throw new AndroidRuntimeException("rootLayout is 0x0");
        }
        View view = null;
        try {
            View rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
            view = showUnifiedAdView(rootView, pidConfig, spConfig);
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

    private View showUnifiedAdView(View rootView, PidConfig pidConfig, final SpConfig spConfig) throws Exception {
        ClickClass clickClass = new ClickClass();
        clickClass.setSpConfig(spConfig);
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
        View subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        View ctaView = rootView.findViewById(mParams.getAdAction());

        final View adIconView = rootView.findViewById(mParams.getAdIcon());

        // 设置广告元素内容
        if (!TextUtils.isEmpty(spConfig.getTitle())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(spConfig.getTitle());
                titleView.setVisibility(View.VISIBLE);
                titleView.setOnClickListener(clickClass);
            }
        }

        if (!TextUtils.isEmpty(spConfig.getSubTitle())) {
            if (subTitleView instanceof TextView) {
                ((TextView) subTitleView).setText(spConfig.getSubTitle());
                subTitleView.setVisibility(View.VISIBLE);
                subTitleView.setOnClickListener(clickClass);
            }
        }

        if (!TextUtils.isEmpty(spConfig.getDetail())) {
            if (detailView instanceof TextView) {
                ((TextView) detailView).setText(spConfig.getDetail());
                detailView.setVisibility(View.VISIBLE);
                detailView.setOnClickListener(clickClass);
            }
        }

        if (!TextUtils.isEmpty(spConfig.getCta())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(spConfig.getCta());
                ctaView.setVisibility(View.VISIBLE);
                ctaView.setOnClickListener(clickClass);
            }
        }

        String iconUrl = spConfig.getIcon();
        if (iconUrl != null) {
            if (adIconView instanceof ImageView) {
                loadAndShowImage((ImageView) adIconView, iconUrl);
                adIconView.setVisibility(View.VISIBLE);
                adIconView.setOnClickListener(clickClass);
            }
        }

        ViewGroup mediaViewLayout = rootView.findViewById(mParams.getAdMediaView());
        ImageView mediaView = new ImageView(mediaViewLayout.getContext());
        if (mediaViewLayout != null && mediaView != null) {
            mediaView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAndShowImage(mediaView, spConfig.getBanner());
            mediaViewLayout.addView(mediaView, -1, -1);
            mediaViewLayout.setVisibility(View.VISIBLE);
            mediaView.setOnClickListener(clickClass);
        }

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

    private class ClickClass implements View.OnClickListener {
        private SpConfig mSpConfig;

        public void setSpConfig(SpConfig spConfig) {
            mSpConfig = spConfig;
        }

        @Override
        public void onClick(View v) {
            if (mSpConfig != null) {
                String url = mSpConfig.getLinkUrl();
                if (TextUtils.isEmpty(url)) {
                    url = "market://details?id=" + mSpConfig.getPkgname();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.v(Log.TAG, "error : " + e);
                }
            }
        }
    }
}
