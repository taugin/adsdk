package com.hauyu.adsdk.adloader.mopub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hauyu.adsdk.adloader.base.BaseBindNativeView;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;
import com.mopub.nativeads.MediaLayout;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.VideoNativeAd;
import com.mopub.nativeads.ViewBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/2/11.
 */

public class MopubBindNativeView extends BaseBindNativeView {
    private Params mParams;

    public void bindMopubNative(Params params, Context context, MoPubNative nativeAd, PidConfig pidConfig) {
        mParams = params;
        if (mParams == null) {
            Log.e(Log.TAG, "bindMopubNative mParams == null###");
            return;
        }
        if (context == null) {
            Log.e(Log.TAG, "bindMopubNative context == null###");
            return;
        }
        int rootLayout = mParams.getNativeRootLayout();
        if (rootLayout <= 0 && mParams.getNativeCardStyle() > 0) {
            rootLayout = getAdViewLayout(context, mParams.getNativeCardStyle(), pidConfig);
            bindParamsViewId(mParams);
        }

        if (rootLayout > 0) {
            bindNativeViewWithRootView(context, rootLayout, nativeAd, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find " + pidConfig.getSdk() + " native layout###");
        }
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootLayout
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, int rootLayout, MoPubNative nativeAd, PidConfig pidConfig) {
        if (rootLayout <= 0) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootLayout == 0x0");
            return;
        }
        if (nativeAd == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView nativeAd == null");
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

        View rootView = null;
        try {
            rootView = LayoutInflater.from(context).inflate(rootLayout, null);
            bindVideoRender(context, nativeAd, rootView);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        try {
            rootView = LayoutInflater.from(context).inflate(rootLayout, null);
            bindStaticRender(context, nativeAd, rootView);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindVideoRender(Context context, MoPubNative nativeAd, View layout) {
        MoPubVideoAdRender mopubVideoRender = new MoPubVideoAdRender(getVideoViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(mopubVideoRender);
    }

    private MediaViewBinder getVideoViewBinder(Context context, View layout) {
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        MediaLayout mediaLayout = createMediaLayout(context);
        mediaLayout.setId(getMediaLayoutId());
        coverLayout.addView(mediaLayout);
        centerChildView(coverLayout);

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(context);
        imageView.setId(getImageViewId());
        int size = Utils.dp2px(context, 20);
        adChoiceLayout.addView(imageView, size, size);

        MediaViewBinder videoViewBinder = new MediaViewBinder.Builder(mParams.getNativeRootLayout())
                .mediaLayoutId(mediaLayout.getId())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(imageView.getId())
                .build();
        return videoViewBinder;
    }

    private void bindStaticRender(Context context, MoPubNative nativeAd, View layout) {
        MoPubStaticAdRender moPubAdRenderer = new MoPubStaticAdRender(getStaticViewBinder(context, layout), layout);
        nativeAd.registerAdRenderer(moPubAdRenderer);
    }

    private ViewBinder getStaticViewBinder(Context context, View layout) {
        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(context);
        imageView.setId(getImageViewId());
        int size = Utils.dp2px(context, 20);
        adChoiceLayout.addView(imageView, size, size);

        ViewBinder viewBinder = new ViewBinder.Builder(mParams.getNativeRootLayout())
                .mainImageId(mParams.getAdCover())
                .iconImageId(mParams.getAdIcon())
                .titleId(mParams.getAdTitle())
                .textId(mParams.getAdDetail())
                .callToActionId(mParams.getAdAction())
                .privacyInformationIconImageId(imageView.getId())
                .build();
        return viewBinder;
    }


    private MediaLayout createMediaLayout(Context context) {
        try {
            return new MediaLayout(context);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private int getMediaLayoutId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000001;
    }

    private int getImageViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        return 0x1000002;
    }

    public void notifyAdViewShowing(View view, PidConfig pidConfig, boolean staticRender) {
        updateCtaButtonBackground(view, pidConfig, mParams);
        updateClickView(view, pidConfig);
        updateAdViewVisibility(staticRender, view);
    }

    private void updateClickView(View view, PidConfig pidConfig) {
        if (view == null || pidConfig == null || mParams == null) {
            return;
        }
        try {
            List<String> clickViews = getClickViews(pidConfig);
            if (clickViews == null || clickViews.isEmpty()) {
                return;
            }
            Map<String, View> viewMap = new HashMap<String, View>();
            viewMap.put(AD_ICON, view.findViewById(mParams.getAdIcon()));
            viewMap.put(AD_TITLE, view.findViewById(mParams.getAdTitle()));
            viewMap.put(AD_SUBTITLE, view.findViewById(mParams.getAdSubTitle()));
            viewMap.put(AD_DETAIL, view.findViewById(mParams.getAdDetail()));
            viewMap.put(AD_CTA, view.findViewById(mParams.getAdAction()));
            viewMap.put(AD_MEDIA, view.findViewById(mParams.getAdMediaView()));
            viewMap.put(AD_COVER, view.findViewById(mParams.getAdCover()));
            viewMap.put(AD_CHOICES, view.findViewById(mParams.getAdChoices()));
            viewMap.put(AD_SPONSORED, view.findViewById(mParams.getAdSponsored()));
            viewMap.put(AD_SOCIAL, view.findViewById(mParams.getAdSocial()));
            List<View> clickElements = new ArrayList<View>(clickViews.size());
            for (String text : clickViews) {
                clickElements.add(viewMap.get(text));
            }
            clickElements.add(viewMap.get(AD_CHOICES));
            traversalView(view, clickElements);
        } catch (Exception e) {
        }
    }

    private void traversalView(View view, List<View> enableClickView) {
        if (view == null) {
            return;
        }
        if (!enableClickView.contains(view)) {
            view.setOnClickListener(null);
            view.setClickable(false);
            if (view instanceof ViewGroup) {
                int size = ((ViewGroup) view).getChildCount();
                for (int index = 0; index < size; index++) {
                    traversalView(((ViewGroup) view).getChildAt(index), enableClickView);
                }
            }
        }
    }

    private void updateAdViewVisibility(boolean staticRender, View adView) {
        updateIconView(adView);
        updateDetailView(adView);
        updateMediaView(staticRender, adView);
    }

    private void updateIconView(View adView) {
        try {
            int iconId = mParams.getAdIcon();
            if (iconId > 0) {
                View view = adView.findViewById(iconId);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();
                    if (drawable != null) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                } else if (view instanceof ViewGroup) {
                    ImageView imageView = null;
                    ViewGroup iconLayout = (ViewGroup) view;
                    if (iconLayout != null) {
                        int count = iconLayout.getChildCount();
                        for (int index = 0; index < count; index++) {
                            View v = iconLayout.getChildAt(index);
                            if (v instanceof ImageView) {
                                imageView = (ImageView) v;
                                break;
                            }
                        }
                        if (imageView != null) {
                            Drawable drawable = imageView.getDrawable();
                            if (drawable != null) {
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void updateDetailView(View adView) {
        try {
            int detailId = mParams.getAdDetail();
            View detailView = adView.findViewById(detailId);
            if (detailView instanceof TextView) {
                String detail = ((TextView) detailView).getText().toString();
                if (!TextUtils.isEmpty(detail)) {
                    detailView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
        }
    }

    public void updateMediaView(boolean staticRender, View adView) {
        if (staticRender) {
            if (mParams.getAdMediaView() > 0) {
                View view = adView.findViewById(mParams.getAdMediaView());
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
            if (mParams.getAdCover() > 0) {
                View view = adView.findViewById(mParams.getAdCover());
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mParams.getAdMediaView() > 0) {
                View view = adView.findViewById(mParams.getAdMediaView());
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            if (mParams.getAdCover() > 0) {
                View view = adView.findViewById(mParams.getAdCover());
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void putAdvertiserInfo(com.mopub.nativeads.NativeAd nativeAd) {
        try {
            if (nativeAd.getBaseNativeAd() instanceof StaticNativeAd) {

                StaticNativeAd staticNativeAd = (StaticNativeAd) nativeAd.getBaseNativeAd();
                putStaticInfo(staticNativeAd);
            }
        } catch (Exception e) {
        }
        try {
            if (nativeAd.getBaseNativeAd() instanceof VideoNativeAd) {
                VideoNativeAd videoNativeAd = (VideoNativeAd) nativeAd.getBaseNativeAd();
                putVideoInfo(videoNativeAd);
            }
        } catch (Exception e) {
        }
    }

    private void putStaticInfo(StaticNativeAd staticNativeAd) {
        if (staticNativeAd != null) {
            try {
                putValue(AD_TITLE, staticNativeAd.getTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, staticNativeAd.getText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_SPONSORED, staticNativeAd.getSponsored());
            } catch (Exception e) {
            }
            try {
                putValue(AD_MEDIA, staticNativeAd.getMainImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, staticNativeAd.getCallToAction());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, staticNativeAd.getIconImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_RATE, staticNativeAd.getStarRating().toString());
            } catch (Exception e) {
            }
        }
    }

    private void putVideoInfo(VideoNativeAd videoNativeAd) {
        if (videoNativeAd != null) {
            try {
                putValue(AD_TITLE, videoNativeAd.getTitle());
            } catch (Exception e) {
            }
            try {
                putValue(AD_DETAIL, videoNativeAd.getText());
            } catch (Exception e) {
            }
            try {
                putValue(AD_SPONSORED, videoNativeAd.getSponsored());
            } catch (Exception e) {
            }
            try {
                putValue(AD_MEDIA, videoNativeAd.getMainImageUrl());
            } catch (Exception e) {
            }
            try {
                putValue(AD_CTA, videoNativeAd.getCallToAction());
            } catch (Exception e) {
            }
            try {
                putValue(AD_ICON, videoNativeAd.getIconImageUrl());
            } catch (Exception e) {
            }
            try {
                String vastVideo = videoNativeAd.getVastVideo();
                String urlRegex = "https?://(.*)?\\.mp4";
                Pattern pattern = Pattern.compile(urlRegex);
                Matcher matcher = pattern.matcher(vastVideo);
                List<String> list = new ArrayList<>();
                while(matcher.find()) {
                    list.add(matcher.group());
                }
                if (list != null) {
                    putValue(AD_VIDEO, list.size() == 1 ? list.get(0) : list.toString());
                }
            } catch (Exception e) {
            }
        }
    }
}