package com.hauyu.adsdk.adloader.mopub;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gekes.fvs.tdsvap.R;
import com.hauyu.adsdk.adloader.base.BaseBindNativeView;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;
import com.mopub.nativeads.MediaLayout;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.ViewBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        View rootView = mParams.getNativeRootView();
        int cardId = mParams.getNativeCardStyle();
        if (rootView != null) {
            bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
        } else if (rootLayout > 0) {
            rootView = LayoutInflater.from(context).inflate(rootLayout, null);
            bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
        } else if (cardId > 0) {
            bindNativeWithCard(context, cardId, nativeAd, pidConfig);
        } else {
            Log.e(Log.TAG, "Can not find mopub native layout###");
        }
    }

    private void bindNativeWithCard(Context context, int template, MoPubNative nativeAd, PidConfig pidConfig) {
        int layoutId = R.layout.had_card_large;
        if (template == Constant.NATIVE_CARD_SMALL) {
            layoutId = R.layout.had_card_small;
        } else if (template == Constant.NATIVE_CARD_MEDIUM) {
            layoutId = R.layout.had_card_medium;
        } else if (template == Constant.NATIVE_CARD_LARGE) {
            layoutId = R.layout.had_card_large;
        } else if (template == Constant.NATIVE_CARD_FULL) {
            layoutId = getFullLayout(pidConfig);
        } else if (template == Constant.NATIVE_CARD_TINY) {
            layoutId = R.layout.had_card_tiny;
        }
        View rootView = LayoutInflater.from(context).inflate(layoutId, null);
        bindNativeViewWithTemplate(context, rootView, nativeAd, pidConfig);
    }

    /**
     * 使用模板显示原生广告
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithTemplate(Context context, View rootView, MoPubNative nativeAd, PidConfig pidConfig) {
        mParams.setAdTitle(R.id.native_title);
        mParams.setAdSubTitle(R.id.native_sub_title);
        mParams.setAdSocial(R.id.native_social);
        mParams.setAdDetail(R.id.native_detail);
        mParams.setAdIcon(R.id.native_icon);
        mParams.setAdAction(R.id.native_action_btn);
        mParams.setAdCover(R.id.native_image_cover);
        mParams.setAdChoices(R.id.native_ad_choices_container);
        mParams.setAdMediaView(R.id.native_media_cover);
        mParams.setAdRootView(rootView);
        bindNativeViewWithRootView(context, rootView, nativeAd, pidConfig);
    }


    /**
     * 外部传入ViewRoot
     *
     * @param rootView
     * @param nativeAd
     * @param pidConfig
     */
    private void bindNativeViewWithRootView(Context context, View rootView, MoPubNative nativeAd, PidConfig pidConfig) {
        if (rootView == null) {
            Log.v(Log.TAG, "bindNativeViewWithRootView rootView == null");
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

        // 恢复icon图标
        try {
            restoreIconView(rootView, pidConfig.getSdk(), mParams.getAdIcon());
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            bindVideoRender(context, nativeAd, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        try {
            bindStaticRender(context, nativeAd, pidConfig);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } catch (Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void bindVideoRender(Context context, MoPubNative nativeAd, PidConfig pidConfig) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "bindVideoRender  root layout == 0x0");
        }
        MoPubVideoAdRender mopubVideoRender = new MoPubVideoAdRender(getVideoViewBinder(context, layout, pidConfig), layout);
        nativeAd.registerAdRenderer(mopubVideoRender);
    }

    private MediaViewBinder getVideoViewBinder(Context context, View layout, PidConfig pidConfig) {
        ViewGroup coverLayout = layout.findViewById(mParams.getAdMediaView());
        MediaLayout mediaLayout = createMediaLayout(context);
        mediaLayout.setId(getMediaLayoutId());
        coverLayout.addView(mediaLayout);

        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(context);
        imageView.setTag(pidConfig.getSdk());
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

    private void bindStaticRender(Context context, MoPubNative nativeAd, PidConfig pidConfig) {
        View layout = null;
        if (mParams.getNativeRootView() != null) {
            layout = mParams.getNativeRootView();
        } else if (mParams.getNativeRootLayout() > 0) {
            layout = LayoutInflater.from(context).inflate(mParams.getNativeRootLayout(), null);
        } else {
            Log.e(Log.TAG, "bindStaticRender  root layout == 0x0");
        }
        MoPubStaticAdRender moPubAdRenderer = new MoPubStaticAdRender(getStaticViewBinder(context, layout, pidConfig), layout);
        nativeAd.registerAdRenderer(moPubAdRenderer);
    }

    private ViewBinder getStaticViewBinder(Context context, View layout, PidConfig pidConfig) {
        ViewGroup adChoiceLayout = layout.findViewById(mParams.getAdChoices());
        adChoiceLayout.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(context);
        imageView.setTag(pidConfig.getSdk());
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

    public void notifyMopubShowing(View view, PidConfig pidConfig) {
        onAdViewShown(view, pidConfig, mParams);
    }

    public void updateClickView(View view, PidConfig pidConfig) {
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

    public void restoreAdViewContent(View adView) {
        restoreAdChoiceView(adView, mParams.getAdChoices());
        restoreAdViewContent(mParams, adView);
    }
}