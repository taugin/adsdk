package com.rabbit.adsdk.adloader.tradplus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.widget.MBAdChoice;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
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
        private Context mContext;
        private PidConfig mPidConfig;
        private Params mParams;
        private TPCustomNativeAd mTPCustomNativeAd;

        public CustomTPNativeAdRender(Context context, PidConfig pidConfig, Params params, TPCustomNativeAd customNativeAd) {
            mContext = context;
            mPidConfig = pidConfig;
            mParams = params;
            mTPCustomNativeAd = customNativeAd;
        }

        private String toLower(String str) {
            if (!TextUtils.isEmpty(str)) {
                return str.toLowerCase(Locale.getDefault());
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
                    Log.e(Log.TAG, "Can not find " + mPidConfig.getSdk() + " native layout###");
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
                        if (tpNativeAdView.getIconImage() != null) {
                            adIconView.setImageDrawable(tpNativeAdView.getIconImage());
                        } else if (tpNativeAdView.getIconImageUrl() != null) {
                            TPImageLoader.getInstance().loadImage(adIconView, tpNativeAdView.getIconImageUrl());
                            putValue(AD_ICON, tpNativeAdView.getIconImageUrl());
                        }
                    }
                    TextView titleView = viewGroup.findViewById(mParams.getAdTitle());
                    putValue(AD_TITLE, tpNativeAdView.getTitle());
                    if (titleView != null && tpNativeAdView.getTitle() != null) {
                        titleView.setText(tpNativeAdView.getTitle());
                    }

                    TextView subTitleView = viewGroup.findViewById(mParams.getAdDetail());
                    putValue(AD_DETAIL, tpNativeAdView.getSubTitle());
                    if (subTitleView != null && tpNativeAdView.getSubTitle() != null) {
                        subTitleView.setText(tpNativeAdView.getSubTitle());
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
                                adChoiceViewLayout.addView(frameLayout);
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
                }
                return viewGroup;
            } catch (Exception e) {
                Log.e(Log.TAG, "show native ad view error : " + e);
            }
            return null;
        }
    }

    private boolean renderAdChoice(ViewGroup viewGroup, TPCustomNativeAd customNativeAd) {
        try {
            Object obj = customNativeAd.getCustomNetworkObj();
            if (obj instanceof com.mbridge.msdk.out.Campaign) {
                // 设置mintegral的ad choice
                com.mbridge.msdk.out.Campaign campaign = (Campaign) obj;
                MBAdChoice mbAdChoice = new MBAdChoice(viewGroup.getContext());
                mbAdChoice.setCampaign(campaign);
                viewGroup.addView(mbAdChoice);
                return true;
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "show native ad choice view error : " + e);
        }
        return false;
    }
}
