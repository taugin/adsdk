package com.komob.adsdk.adloader.spread;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.data.config.SpreadConfig;
import com.komob.adsdk.http.Http;
import com.komob.adsdk.http.OnImageCallback;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/26.
 */

public class SpreadBindNativeView extends BaseBindNativeView {
    private Params mParams;
    private SpLoader.ClickClass mClickClass;

    public void setClickListener(SpLoader.ClickClass clickListener) {
        mClickClass = clickListener;
    }

    public void bindNative(Params params, ViewGroup adContainer, PidConfig pidConfig, SpreadConfig spreadConfig) {
        mParams = params;
        if (mParams == null) {
            Log.iv(Log.TAG, "bindNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.iv(Log.TAG, "bindNative adContainer == null###");
            return;
        }
        int rootLayout = getBestNativeLayout(adContainer.getContext(), pidConfig, mParams, Constant.AD_SDK_SPREAD);
        if (rootLayout > 0) {
            bindNativeViewWithRootView(adContainer, rootLayout, pidConfig, spreadConfig);
            updateCtaButtonBackground(adContainer, pidConfig, mParams);
        } else {
            Log.iv(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, int rootLayout, PidConfig pidConfig, SpreadConfig spreadConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootLayout <= 0) {
            throw new AndroidRuntimeException("rootLayout is 0x0");
        }
        View view = null;
        try {
            View rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
            view = showUnifiedAdView(rootView, pidConfig, spreadConfig);
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

    private View showUnifiedAdView(View rootView, PidConfig pidConfig, final SpreadConfig spreadConfig) throws Exception {
        try {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = rootView.findViewById(mParams.getAdCover());
        View detailView = rootView.findViewById(mParams.getAdDetail());
        View ctaView = rootView.findViewById(mParams.getAdAction());

        final View adIconView = rootView.findViewById(mParams.getAdIcon());

        // 设置广告元素内容
        if (!TextUtils.isEmpty(spreadConfig.getTitle())) {
            if (titleView instanceof TextView) {
                ((TextView) titleView).setText(spreadConfig.getTitle());
                titleView.setVisibility(View.VISIBLE);
                titleView.setOnClickListener(mClickClass);
            }
        }

        if (!TextUtils.isEmpty(spreadConfig.getDetail())) {
            if (detailView instanceof TextView) {
                ((TextView) detailView).setText(spreadConfig.getDetail());
                detailView.setVisibility(View.VISIBLE);
                detailView.setOnClickListener(mClickClass);
            }
        }

        if (!TextUtils.isEmpty(spreadConfig.getCta())) {
            if (ctaView instanceof TextView) {
                ((TextView) ctaView).setText(getCTAText(rootView.getContext(), spreadConfig));
                ctaView.setVisibility(View.VISIBLE);
                ctaView.setOnClickListener(mClickClass);
            }
        }

        String iconUrl = spreadConfig.getIcon();
        if (iconUrl != null) {
            if (adIconView instanceof ImageView) {
                loadAndShowImage((ImageView) adIconView, iconUrl);
                adIconView.setVisibility(View.VISIBLE);
                adIconView.setOnClickListener(mClickClass);
            }
        }

        ImageView mediaView = rootView.findViewById(mParams.getAdCover());
        if (mediaView != null) {
            loadAndShowImage(mediaView, spreadConfig.getBanner());
            mediaView.setOnClickListener(mClickClass);
        }
        putAdvertiserInfo(spreadConfig);
        return rootView;
    }

    private String getCTAText(Context context, SpreadConfig spreadConfig) {
        if (spreadConfig == null) {
            return null;
        }
        String ctaText = spreadConfig.getCta();
        try {
            Map<String, String> map = spreadConfig.getCtaLocale();
            if (map != null) {
                String language = getLanguage(context);
                if (!TextUtils.isEmpty(language)) {
                    String ctaLocale = map.get(language);
                    if (!TextUtils.isEmpty(ctaLocale)) {
                        ctaText = ctaLocale;
                    }
                }
            }
        } catch (Exception e) {
        }
        return ctaText;
    }

    private static String getLanguage(Context context) {
        String language = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            language = locale.getLanguage().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
        }
        return language;
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

    private void putAdvertiserInfo(SpreadConfig spreadConfig) {
        try {
            putValue(AD_TITLE, spreadConfig.getTitle());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, spreadConfig.getDetail());
        } catch (Exception e) {
        }
        try {
            putValue(AD_COVER, spreadConfig.getBanner());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, spreadConfig.getCta());
        } catch (Exception e) {
        }
        try {
            putValue(AD_ICON, spreadConfig.getIcon());
        } catch (Exception e) {
        }
    }
}
