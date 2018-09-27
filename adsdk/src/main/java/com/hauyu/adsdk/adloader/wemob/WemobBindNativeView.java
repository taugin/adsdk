package com.hauyu.adsdk.adloader.wemob;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hauyu.adsdk.R;
import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.Params;
import com.hauyu.adsdk.log.Log;
import com.wemob.ads.NativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/6/8.
 */

public class WemobBindNativeView {
    private Params mParams;

    public void bindWemobNative(Params params, ViewGroup adContainer, NativeAd nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindWemobNative mParams == null###");
            return;
        }
        if (adContainer == null) {
            Log.e(Log.TAG, "bindWemobNative adContainer == null###");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
        } else if (rootLayout > 0) {
            if (adContainer != null && adContainer.getContext() != null) {
                rootView = LayoutInflater.from(adContainer.getContext()).inflate(rootLayout, null);
                bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
            }
        } else if (cardId > 0) {
            bindNativeWithCard(adContainer, cardId, nativeAd, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find wemob native layout###");
        }
    }

    private void bindNativeWithCard(ViewGroup adContainer, int template, NativeAd nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.native_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.native_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.native_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.native_card_large;
        }
        Context context = adContainer.getContext();
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(adContainer, rootView, nativeAd, pidConfig);
        try {
            adContainer.removeAllViews();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -2);
            adContainer.addView(rootView, params);
            if (adContainer.getVisibility() != View.VISIBLE) {
                adContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    /**
     * 使用模板显示原生广告
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithTemplate(ViewGroup adContainer, View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        bindNativeViewWithRootView(adContainer, rootView, nativeAd, pidConfig);
    }

    private void bindNativeViewWithRootView(ViewGroup adContainer, View rootView, NativeAd nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootView == null");
            return;
        }
        if (nativeAd == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView nativeAd == null or nativeAd.isAdLoaded() == false");
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

        TextView titleView = rootView.findViewById(mParams.getAdTitle());
        TextView subTitleView = rootView.findViewById(mParams.getAdSubTitle());
        ImageView icon = rootView.findViewById(mParams.getAdIcon());
        ImageView imageCover = rootView.findViewById(mParams.getAdCover());
        TextView detail = rootView.findViewById(mParams.getAdDetail());
        AppCompatButton btnAction = rootView.findViewById(mParams.getAdAction());

        if (imageCover != null) {
            imageCover.setVisibility(View.VISIBLE);
        }

        // 可点击的视图
        List<View> actionView = new ArrayList<View>();

        if (nativeAd != null) {
            String iconUrl = nativeAd.getIconUrl();
            String coverUrl = nativeAd.getCoverUrl();
            if (icon != null) {
                try {
                    com.facebook.ads.NativeAd.Image image = new com.facebook.ads.NativeAd.Image(iconUrl, icon.getWidth(), icon.getHeight());
                    com.facebook.ads.NativeAd.downloadAndDisplayImage(image, icon);
                    actionView.add(icon);
                    if (!TextUtils.isEmpty(iconUrl)) {
                        icon.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                } catch (Error e) {
                }
            }

            if (imageCover != null) {
                try {
                    com.facebook.ads.NativeAd.Image image = new com.facebook.ads.NativeAd.Image(coverUrl, imageCover.getWidth(), imageCover.getHeight());
                    com.facebook.ads.NativeAd.downloadAndDisplayImage(image, imageCover);
                    actionView.add(imageCover);
                    if (!TextUtils.isEmpty(coverUrl)) {
                        imageCover.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                } catch (Error e) {
                }
            }

            if (btnAction != null) {
                btnAction.setText(nativeAd.getCallToAction());
                actionView.add(btnAction);

                if (!TextUtils.isEmpty(nativeAd.getCallToAction())) {
                    btnAction.setVisibility(View.VISIBLE);
                }
            }

            if (titleView != null) {
                titleView.setText(nativeAd.getAdTitle());
                actionView.add(titleView);

                if (!TextUtils.isEmpty(nativeAd.getAdTitle())) {
                    titleView.setVisibility(View.VISIBLE);
                }
            }

            if (subTitleView != null) {
                subTitleView.setText(nativeAd.getAdSubtitle());
                actionView.add(subTitleView);

                if (!TextUtils.isEmpty(nativeAd.getAdSubtitle())) {
                    subTitleView.setVisibility(View.VISIBLE);
                }
            }

            if (detail != null) {
                detail.setText(nativeAd.getAdBody());
                actionView.add(detail);

                if (!TextUtils.isEmpty(nativeAd.getAdBody())) {
                    detail.setVisibility(View.VISIBLE);
                }
            }

            boolean largeInteraction = percentRandomBoolean(pidConfig.getCtr());

            if (largeInteraction && rootView != null) {
                nativeAd.registerViewForInteraction(rootView, actionView);
            } else {
                nativeAd.registerViewForInteraction(btnAction);
            }
        }
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

    public static boolean percentRandomBoolean(int percent) {
        if (percent <= 0 || percent > 100) return false;
        int randomVal = new Random().nextInt(100);
        return randomVal <= percent;
    }
}
