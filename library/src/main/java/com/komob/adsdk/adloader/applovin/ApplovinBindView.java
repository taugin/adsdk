package com.komob.adsdk.adloader.applovin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.applovin.impl.sdk.nativeAd.AppLovinOptionsView;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.util.List;
import java.util.Locale;

public class ApplovinBindView extends BaseBindNativeView {
    private Params mParams;

    public MaxNativeAdView bindMaxNativeAdView(Context context, Params params, PidConfig pidConfig, String networkName) {
        mParams = params;
        if (pidConfig != null) {
            if (networkName != null) {
                networkName = networkName.toLowerCase(Locale.ENGLISH);
            }
            int rootLayout = getBestNativeLayout(context, pidConfig, mParams, networkName);
            if (rootLayout > 0) {
                View rootView = LayoutInflater.from(context).inflate(rootLayout, null);
                View ctaButton = rootView.findViewById(params.getAdAction());
                if (ctaButton != null) {
                    if (!(ctaButton instanceof Button)) {
                        Button button = new Button(context);
                        if (ctaButton instanceof TextView) {
                            ColorStateList textColor = ((TextView) ctaButton).getTextColors();
                            button.setTextColor(textColor);
                        }
                        button.setClickable(false);
                        button.setSingleLine(true);
                        button.setBackground(ctaButton.getBackground());
                        replaceSrcViewToDstView(ctaButton, button);
                    }
                    ctaButton.setClickable(false);
                }

                View mediaLayout = rootView.findViewById(params.getAdMediaView());
                if (mediaLayout != null && !(mediaLayout instanceof FrameLayout)) {
                    FrameLayout frameLayout = new FrameLayout(context);
                    replaceSrcViewToDstView(mediaLayout, frameLayout);
                }

                View adChoiceLayout = rootView.findViewById(params.getAdChoices());
                if (adChoiceLayout != null && !(adChoiceLayout instanceof FrameLayout)) {
                    FrameLayout frameLayout = new FrameLayout(context);
                    replaceSrcViewToDstView(adChoiceLayout, frameLayout);
                }

                MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(rootView)
                        .setTitleTextViewId(params.getAdTitle())
                        .setBodyTextViewId(params.getAdDetail())
                        .setAdvertiserTextViewId(params.getAdSocial())
                        .setIconImageViewId(params.getAdIcon())
                        .setMediaContentViewGroupId(params.getAdMediaView())
                        .setOptionsContentViewGroupId(params.getAdChoices())
                        .setCallToActionButtonId(params.getAdAction())
                        .build();
                MaxNativeAdView maxNativeAdView = new MaxNativeAdView(binder, context);
                updateCtaButtonBackground(maxNativeAdView, pidConfig, mParams);
                updateAdViewStatus(maxNativeAdView, mParams);
                return maxNativeAdView;
            } else {
                Log.iv(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
            }
        }
        return new MaxNativeAdView("", context);
    }

    public void updateApplovinNative(Context context, MaxNativeAdView maxNativeAdView, PidConfig pidConfig) {
        try {
            if (maxNativeAdView != null) {
                maxNativeAdView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateMediaViewStatus(maxNativeAdView);
                        } catch (Exception e) {
                        }
                    }
                }, 10);
            }
        } catch (Exception e) {
        }
        updateNativeStatus(context, maxNativeAdView);
        updateNativeDetail(maxNativeAdView);
        fillNativeAssets(maxNativeAdView);
        updateClickViewStatus(maxNativeAdView, pidConfig);
    }

    private void updateMediaViewStatus(MaxNativeAdView maxNativeAdView) {
        try {
            if (maxNativeAdView != null) {
                ViewGroup viewGroup = maxNativeAdView.getMediaContentViewGroup();
                if (viewGroup != null && viewGroup.getChildCount() > 0) {
                    View childGroup = viewGroup.getChildAt(0);
                    ViewGroup.LayoutParams layoutParams = childGroup.getLayoutParams();
                    if (layoutParams instanceof FrameLayout.LayoutParams) {
                        layoutParams.height = -2;
                        ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                        childGroup.setLayoutParams(layoutParams);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void updateNativeStatus(Context context, MaxNativeAdView maxNativeAdView) {
        try {
            if (maxNativeAdView != null) {
                ViewGroup viewGroup = maxNativeAdView.getOptionsContentViewGroup();
                if (viewGroup != null && viewGroup.getChildCount() > 0) {
                    View childView = viewGroup.getChildAt(0);
                    if (childView != null) {
                        ViewGroup.LayoutParams params = childView.getLayoutParams();
                        if (params != null) {
                            params.width = -2;
                            params.height = Utils.dp2px(context, 20);
                            childView.setLayoutParams(params);
                        }
                    }
                    try {
                        if (childView instanceof AppLovinOptionsView) {
                            View maxOptionView = ((AppLovinOptionsView) childView).getChildAt(0);
                            ViewGroup.LayoutParams params = maxOptionView.getLayoutParams();
                            if (params != null) {
                                int size = Utils.dp2px(context, 16);
                                params.width = size;
                                params.height = size;
                                maxOptionView.setLayoutParams(params);
                            }
                        }
                    } catch (Exception e) {
                        Log.iv(Log.TAG, "error : " + e);
                    }
                }
                viewGroup = maxNativeAdView.getMediaContentViewGroup();
                if (viewGroup instanceof FrameLayout && viewGroup.getChildCount() > 0) {
                    View childView = viewGroup.getChildAt(0);
                    ViewGroup.LayoutParams layoutParams = childView.getLayoutParams();
                    if (layoutParams instanceof FrameLayout.LayoutParams) {
                        ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
                        childView.setLayoutParams(layoutParams);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 更新原生元素点击状态
     *
     * @param maxNativeAdView
     * @param pidConfig
     */
    private void updateClickViewStatus(MaxNativeAdView maxNativeAdView, PidConfig pidConfig) {
        try {
            List<String> clickView = getClickView(pidConfig);
            if (clickView != null && !clickView.isEmpty()) {
                if (maxNativeAdView != null) {
                    View titleView = maxNativeAdView.getTitleTextView();
                    if (titleView != null) {
                        titleView.setClickable(isClickable(AD_TITLE, pidConfig));
                    }
                    View bodyView = maxNativeAdView.getBodyTextView();
                    if (bodyView != null) {
                        bodyView.setClickable(isClickable(AD_DETAIL, pidConfig));
                    }
                    View iconView = maxNativeAdView.getIconImageView();
                    if (iconView != null) {
                        iconView.setClickable(isClickable(AD_ICON, pidConfig));
                    }
                    View ctaView = maxNativeAdView.getCallToActionButton();
                    if (ctaView != null) {
                        ctaView.setClickable(isClickable(AD_CTA, pidConfig));
                    }
                    View mainView = maxNativeAdView.getMainView();
                    if (mainView != null) {
                        mainView.setClickable(isClickable(AD_MEDIA, pidConfig));
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void updateNativeDetail(MaxNativeAdView maxNativeAdView) {
        try {
            String title = maxNativeAdView.getTitleTextView().getText().toString();
            String detail = maxNativeAdView.getBodyTextView().getText().toString();
            String action = maxNativeAdView.getCallToActionButton().getText().toString();
            if (TextUtils.isEmpty(detail)) {
                maxNativeAdView.getBodyTextView().setText(title);
            }
            if (TextUtils.isEmpty(action)) {
                maxNativeAdView.getCallToActionButton().setText(getActionText(maxNativeAdView.getContext()));
            }
        } catch (Exception e) {
        }

        try {
            ImageView imageView = maxNativeAdView.getIconImageView();
            if (imageView.getDrawable() == null) {
                setDefaultAdIcon("applovin", imageView);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private void fillNativeAssets(MaxNativeAdView maxNativeAdView) {
        try {
            putValue(AD_TITLE, maxNativeAdView.getTitleTextView().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_DETAIL, maxNativeAdView.getBodyTextView().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_CTA, maxNativeAdView.getCallToActionButton().getText().toString());
        } catch (Exception e) {
        }
        try {
            putValue(AD_SOCIAL, maxNativeAdView.getAdvertiserTextView().getText().toString());
        } catch (Exception e) {
        }
    }
}
