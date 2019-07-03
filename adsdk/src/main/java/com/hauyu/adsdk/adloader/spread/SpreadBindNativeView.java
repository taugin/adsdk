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

import com.appub.ads.a.R;
import com.hauyu.adsdk.adloader.base.BaseBindNativeView;
import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.config.SpConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.Params;
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
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(adContainer, rootView, pidConfig, spConfig);
        } else if (rootLayout > 0) {
            if (adContainer != null && adContainer.getContext() != null) {
                rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
                bindNativeViewWithRootView(adContainer, rootView, pidConfig, spConfig);
            }
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, pidConfig, spConfig);
        } else {
            Log.e(Log.TAG, "Can not find spread native layout###");
        }
        onAdViewShown(adContainer, pidConfig, mParams);
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, PidConfig pidConfig, SpConfig spConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        View view = null;
        preSetMediaView(rootView, pidConfig);
        try {
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
            postSetMediaView(rootView, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int cardId, PidConfig pidConfig, SpConfig spConfig) {
        if (adContainer == null) {
            throw new AndroidRuntimeException("adContainer is null");
        }
        int layoutId = R.layout.native_card_small;
        if (cardId == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (cardId == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (cardId == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        } else if (cardId == Constant.NATIVE_CARD_FULL) {
            layoutId = getFullLayout();
        }
        View rootView = LayoutInflater.from(adContainer.getContext()).inflate(layoutId, null);
        if (rootView == null) {
            throw new AndroidRuntimeException("rootView is null");
        }
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        View view = null;
        preSetMediaView(rootView, pidConfig);
        try {
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
            postSetMediaView(rootView, pidConfig);
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
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        // 恢复icon图标
        try {
            restoreIconView(rootView, pidConfig.getSdk(), mParams.getAdIcon());
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        /**
         * 默认adchoiceview不可见
         */
        restoreAdChoiceView(rootView, mParams.getAdChoices());

        View titleView = rootView.findViewById(mParams.getAdTitle());
        View adCoverView = rootView.findViewById(mParams.getAdCover());
        View detailView = rootView.findViewById(mParams.getAdDetail());
        View subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        View ctaView = rootView.findViewById(mParams.getAdAction());

        final View adIconView = rootView.findViewById(mParams.getAdIcon());

        // 设置广告元素内容
        if (!TextUtils.isEmpty(spConfig.getTitle())) {
            if (titleView instanceof TextView) {
                ((TextView)titleView).setText(spConfig.getTitle());
                titleView.setVisibility(View.VISIBLE);
                titleView.setOnClickListener(clickClass);
            }
        }

        if (!TextUtils.isEmpty(spConfig.getSubTitle())) {
            if (subTitleView instanceof TextView) {
                ((TextView)subTitleView).setText(spConfig.getSubTitle());
                subTitleView.setVisibility(View.VISIBLE);
                subTitleView.setOnClickListener(clickClass);
            }
        }

        if (!TextUtils.isEmpty(spConfig.getDetail())) {
            if (detailView instanceof TextView) {
                ((TextView)detailView).setText(spConfig.getDetail());
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
