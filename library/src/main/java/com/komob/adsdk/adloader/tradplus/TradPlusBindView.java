package com.komob.adsdk.adloader.tradplus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.data.config.PidConfig;
import com.tradplus.ads.base.adapter.nativead.TPNativeAdView;
import com.tradplus.ads.base.common.TPImageLoader;
import com.tradplus.ads.mgr.nativead.TPCustomNativeAd;
import com.tradplus.ads.open.nativead.TPNativeAdRender;

import java.util.Locale;

public class TradPlusBindView extends BaseBindNativeView {

    private CustomTPNativeAdRender mCustomTPNativeAdRender;

    public void bindNativeView(Context context, PidConfig pidConfig, Params params, TPCustomNativeAd customNativeAd) {
        mCustomTPNativeAdRender = new CustomTPNativeAdRender(context, pidConfig, params, customNativeAd);
    }

    public CustomTPNativeAdRender getCustomTPNativeAdRender() {
        return mCustomTPNativeAdRender;
    }

    public class CustomTPNativeAdRender extends TPNativeAdRender {
        final private Context mContext;
        final private PidConfig mPidConfig;
        final private Params mParams;
        final private TPCustomNativeAd mTPCustomNativeAd;

        public CustomTPNativeAdRender(Context context, PidConfig pidConfig, Params params, TPCustomNativeAd customNativeAd) {
            mContext = context;
            mPidConfig = pidConfig;
            mParams = params;
            mTPCustomNativeAd = customNativeAd;
        }

        private String toLower(String str) {
            if (!TextUtils.isEmpty(str)) {
                return str.toLowerCase(Locale.ENGLISH);
            }
            return str;
        }

        @Override
        public ViewGroup createAdLayoutView() {
            try {
                String sourceName = null;
                try {
                    sourceName = toLower(mTPCustomNativeAd.getCustomNetworkName());
                } catch (Exception e) {
                }
                int adRootLayout = getBestNativeLayout(mContext, mPidConfig, mParams, sourceName);
                if (adRootLayout > 0) {
                    return (ViewGroup) LayoutInflater.from(mContext).inflate(adRootLayout, null);
                } else {
                    Log.iv(Log.TAG, "Can not find " + mPidConfig.getSdk() + " native layout###");
                }
            } catch (Exception e) {
            }
            return new FrameLayout(mContext);
        }

        @Override
        public ViewGroup renderAdView(TPNativeAdView tpNativeAdView) {
            try {
                ViewGroup viewGroup = createAdLayoutView();
                if (tpNativeAdView != null) {
                    ImageView adCoverImageView = null;
                    View mediaView = tpNativeAdView.getMediaView();
                    Drawable coverDrawable = tpNativeAdView.getMainImage();
                    String mainImageUrl = tpNativeAdView.getMainImageUrl();
                    putValue(AD_COVER, mainImageUrl);
                    if (mediaView != null) {
                        ViewGroup mediaLayout = viewGroup.findViewById(mParams.getAdMediaView());
                        if (mediaLayout != null) {
                            mediaLayout.addView(mediaView);
                        }
                    } else if (coverDrawable != null) {
                        adCoverImageView = viewGroup.findViewById(mParams.getAdCover());
                        if (adCoverImageView != null) {
                            adCoverImageView.setImageDrawable(coverDrawable);
                        }
                    } else if (mainImageUrl != null) {
                        adCoverImageView = viewGroup.findViewById(mParams.getAdCover());
                        if (adCoverImageView != null) {
                            TPImageLoader.getInstance().loadImage(adCoverImageView, mainImageUrl);
                        }
                    }
                    View iconTempView = viewGroup.findViewById(mParams.getAdIcon());
                    ImageView adIconView = null;
                    if (iconTempView instanceof ViewGroup) {
                        if (tpNativeAdView.getIconImage() != null) {
                            adIconView = new ImageView(mContext);
                            adIconView.setImageDrawable(tpNativeAdView.getIconImage());
                            ((ViewGroup) iconTempView).addView(adIconView);
                        } else if (tpNativeAdView.getIconImageUrl() != null) {
                            adIconView = new ImageView(mContext);
                            TPImageLoader.getInstance().loadImage(adIconView, tpNativeAdView.getIconImageUrl());
                            ((ViewGroup) iconTempView).addView(adIconView);
                            putValue(AD_ICON, tpNativeAdView.getIconImageUrl());
                        }
                    } else if (iconTempView instanceof ImageView) {
                        adIconView = (ImageView) iconTempView;
                        if (tpNativeAdView.getIconView() != null) {
                            replaceSrcViewToDstView(adIconView, tpNativeAdView.getIconView());
                        } else if (tpNativeAdView.getIconImage() != null) {
                            adIconView.setImageDrawable(tpNativeAdView.getIconImage());
                        } else if (tpNativeAdView.getIconImageUrl() != null) {
                            TPImageLoader.getInstance().loadImage(adIconView, tpNativeAdView.getIconImageUrl());
                            putValue(AD_ICON, tpNativeAdView.getIconImageUrl());
                        } else {
                            try {
                                setDefaultAdIcon(mPidConfig.getSdk(), adIconView);
                            } catch (Exception e) {
                                Log.iv(Log.TAG, "error : " + e);
                            }
                        }
                    }
                    TextView titleView = viewGroup.findViewById(mParams.getAdTitle());
                    putValue(AD_TITLE, tpNativeAdView.getTitle());
                    if (titleView != null && tpNativeAdView.getTitle() != null) {
                        titleView.setText(tpNativeAdView.getTitle());
                    }

                    TextView subTitleView = viewGroup.findViewById(mParams.getAdDetail());
                    putValue(AD_DETAIL, tpNativeAdView.getSubTitle());
                    if (subTitleView != null) {
                        if (!TextUtils.isEmpty(tpNativeAdView.getSubTitle())) {
                            subTitleView.setText(tpNativeAdView.getSubTitle());
                        } else {
                            subTitleView.setText(tpNativeAdView.getTitle());
                        }
                    }

                    TextView callToActionView = viewGroup.findViewById(mParams.getAdAction());
                    putValue(AD_CTA, tpNativeAdView.getCallToAction());
                    if (callToActionView != null && tpNativeAdView.getCallToAction() != null) {
                        callToActionView.setText(tpNativeAdView.getCallToAction());
                    }

                    // facebook会需要一个adchoice的容器来填充adchoice
                    ViewGroup adChoiceViewLayout = viewGroup.findViewById(mParams.getAdChoices());
                    if (adChoiceViewLayout != null) {
                        if (!renderAdChoice(adChoiceViewLayout, mTPCustomNativeAd)) {
                            Drawable adChoiceDrawable = tpNativeAdView.getAdChoiceImage();
                            String adChoiceUrl = tpNativeAdView.getAdChoiceUrl();
                            Log.iv(Log.TAG, "adChoiceDrawable : " + adChoiceDrawable);
                            Log.iv(Log.TAG, "adChoiceUrl : " + adChoiceUrl);
                            if (adChoiceDrawable != null) {
                                ImageView imageView = new ImageView(mContext);
                                imageView.setImageDrawable(adChoiceDrawable);
                                adChoiceViewLayout.addView(imageView);
                            } else if (adChoiceUrl != null) {
                                ImageView imageView = new ImageView(mContext);
                                TPImageLoader.getInstance().loadImage(imageView, adChoiceUrl);
                                adChoiceViewLayout.addView(imageView);
                                putValue(AD_CHOICES, adChoiceUrl);
                            } else {
                                FrameLayout frameLayout = new FrameLayout(mContext);
                                int size = Utils.dp2px(mContext, 20);
                                adChoiceViewLayout.addView(frameLayout, -2, size);
                                setAdChoicesContainer(frameLayout, false);
                            }
                        }
                    }

                    setImageView(adCoverImageView, isClickable(AD_COVER, mPidConfig));
                    setIconView(adIconView, isClickable(AD_ICON, mPidConfig));
                    setTitleView(titleView, isClickable(AD_TITLE, mPidConfig));
                    setSubTitleView(subTitleView, isClickable(AD_DETAIL, mPidConfig));
                    setCallToActionView(callToActionView, isClickable(AD_CTA, mPidConfig));
                    updateCtaButtonBackground(viewGroup, mPidConfig, mParams);
                    updateAdViewStatus(viewGroup, mParams);
                }
                return viewGroup;
            } catch (Exception e) {
                Log.iv(Log.TAG, "show native ad view error : " + e);
            }
            return null;
        }
    }

    private boolean renderAdChoice(ViewGroup viewGroup, TPCustomNativeAd customNativeAd) {
        try {
            Object obj = customNativeAd.getCustomNetworkObj();
            if (obj instanceof com.mbridge.msdk.out.Campaign) {
                // 设置mintegral的ad choice
                com.mbridge.msdk.out.Campaign campaign = (com.mbridge.msdk.out.Campaign) obj;
                com.mbridge.msdk.widget.MBAdChoice mbAdChoice = new com.mbridge.msdk.widget.MBAdChoice(viewGroup.getContext());
                mbAdChoice.setCampaign(campaign);
                viewGroup.addView(mbAdChoice);
                return true;
            }
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "show native ad choice view error : " + e);
        }
        return false;
    }
}
