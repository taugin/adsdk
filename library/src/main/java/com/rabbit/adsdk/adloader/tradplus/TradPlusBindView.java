package com.rabbit.adsdk.adloader.tradplus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.widget.MBAdChoice;
import com.mopub.common.util.Drawables;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.StaticNativeAd;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.tradplus.ads.base.adapter.nativead.TPNativeAdView;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.common.TPImageLoader;
import com.tradplus.ads.mgr.nativead.TPCustomNativeAd;
import com.tradplus.ads.open.nativead.TPNativeAdRender;

import java.util.Locale;

public class TradPlusBindView extends BaseBindNativeView {

    private CustomTPNativeAdRender mCustomTPNativeAdRender;

    public void bindNativeView(Context context, PidConfig pidConfig, Params params, TPCustomNativeAd customNativeAd, TPAdInfo tpAdInfo) {
        mCustomTPNativeAdRender = new CustomTPNativeAdRender(context, pidConfig, params, customNativeAd, tpAdInfo);
    }

    public CustomTPNativeAdRender getCustomTPNativeAdRender() {
        return mCustomTPNativeAdRender;
    }

    public class CustomTPNativeAdRender extends TPNativeAdRender {
        private Context mContext;
        private PidConfig mPidConfig;
        private Params mParams;
        private TPCustomNativeAd mTPCustomNativeAd;
        private TPAdInfo mTPAdInfo;

        public CustomTPNativeAdRender(Context context, PidConfig pidConfig, Params params, TPCustomNativeAd customNativeAd, TPAdInfo tpAdInfo) {
            mContext = context;
            mPidConfig = pidConfig;
            mParams = params;
            mTPCustomNativeAd = customNativeAd;
            mTPAdInfo = tpAdInfo;
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
                boolean useCardStyle;
                int adRootLayout;
                int rootLayout = mParams.getNativeRootLayout();
                int cardStyle = mParams.getNativeCardStyle();
                if (rootLayout > 0) {
                    useCardStyle = false;
                } else {
                    useCardStyle = true;
                    rootLayout = getAdViewLayout(mContext, cardStyle, mPidConfig);
                    bindParamsViewId(mParams);
                }

                if (useCardStyle && mTPAdInfo != null && !TextUtils.isEmpty(mTPAdInfo.adSourceName)) {
                    String sourceName = toLower(mTPAdInfo.adSourceName);
                    String layout = null;
                    Log.iv(Log.TAG, "tradplus network name : " + sourceName);
                    try {
                        Pair<String, Integer> pair = getSubNativeLayout(mPidConfig, sourceName);
                        adRootLayout = pair.second;
                        layout = pair.first;
                    } catch (Exception e) {
                        adRootLayout = 0;
                    }
                    if (adRootLayout > 0) {
                        Log.iv(Log.TAG, "tradplus use sub native layout for " + sourceName + "[" + layout + "]");
                        bindParamsViewId(mParams);
                    } else {
                        adRootLayout = rootLayout;
                    }
                } else {
                    adRootLayout = rootLayout;
                }

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
                        }
                    } else if (iconTempView instanceof ImageView) {
                        adIconView = (ImageView) iconTempView;
                        if (tpNativeAdView.getIconImage() != null) {
                            adIconView.setImageDrawable(tpNativeAdView.getIconImage());
                        } else if (tpNativeAdView.getIconImageUrl() != null) {
                            TPImageLoader.getInstance().loadImage(adIconView, tpNativeAdView.getIconImageUrl());
                        }
                    }
                    TextView titleView = viewGroup.findViewById(mParams.getAdTitle());
                    if (titleView != null && tpNativeAdView.getTitle() != null) {
                        titleView.setText(tpNativeAdView.getTitle());
                    }

                    TextView subTitleView = viewGroup.findViewById(mParams.getAdDetail());
                    if (subTitleView != null && tpNativeAdView.getSubTitle() != null) {
                        subTitleView.setText(tpNativeAdView.getSubTitle());
                    }

                    TextView callToActionView = viewGroup.findViewById(mParams.getAdAction());
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
                            } else {
                                FrameLayout frameLayout = new FrameLayout(mContext);
                                adChoiceViewLayout.addView(frameLayout);
                                setAdChoicesContainer(frameLayout, false);
                            }
                        }
                    }

                    setImageView(adCoverImageView, isClickable("cover", mPidConfig));
                    setIconView(adIconView, isClickable("icon", mPidConfig));
                    setTitleView(titleView, isClickable("title", mPidConfig));
                    setSubTitleView(subTitleView, isClickable("detail", mPidConfig));
                    setCallToActionView(callToActionView, isClickable("cta", mPidConfig));
                }
                return viewGroup;
            } catch (Exception e) {
                Log.e(Log.TAG, "render ad view error : " + e);
            }
            return null;
        }
    }

    private String getNetwork(TPAdInfo tpAdInfo) {
        return tpAdInfo != null ? tpAdInfo.adSourceName : null;
    }

    private boolean renderAdChoice(ViewGroup viewGroup, TPCustomNativeAd customNativeAd) {
        try {
            Object obj = customNativeAd.getCustomNetworkObj();
            // 设置mopub的ad choice
            if (obj instanceof com.mopub.nativeads.NativeAd) {
                com.mopub.nativeads.NativeAd nativeAd = (com.mopub.nativeads.NativeAd) obj;
                BaseNativeAd baseNativeAd = nativeAd.getBaseNativeAd();
                if (baseNativeAd instanceof com.mopub.nativeads.StaticNativeAd) {
                    com.mopub.nativeads.StaticNativeAd staticNativeAd = (StaticNativeAd) baseNativeAd;
                    String url = staticNativeAd.getPrivacyInformationIconImageUrl();
                    if (!TextUtils.isEmpty(url)) {
                        ImageView imageView = new ImageView(viewGroup.getContext());
                        TPImageLoader.getInstance().loadImage(imageView, url);
                        int size = Utils.dp2px(viewGroup.getContext(), 24);
                        viewGroup.addView(imageView, size, size);
                        return true;
                    } else {
                        ImageView imageView = new ImageView(viewGroup.getContext());
                        imageView.setImageDrawable(Drawables.NATIVE_PRIVACY_INFORMATION_ICON.createDrawable(viewGroup.getContext()));
                        int size = Utils.dp2px(viewGroup.getContext(), 24);
                        viewGroup.addView(imageView, size, size);
                        return true;
                    }
                }
            } else if (obj instanceof com.mbridge.msdk.out.Campaign) {
                // 设置mintegral的ad choice
                com.mbridge.msdk.out.Campaign campaign = (Campaign) obj;
                MBAdChoice mbAdChoice = new MBAdChoice(viewGroup.getContext());
                mbAdChoice.setCampaign(campaign);
                viewGroup.addView(mbAdChoice);
                return true;
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "render ad choice view error : " + e);
        }
        return false;
    }
}
