package com.inner.adsdk.adloader.base;

import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.appub.ads.a.R;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018-12-12.
 */

public class BaseBindNativeView {

    protected static final String AD_TITLE = "title";
    protected static final String AD_SUBTITLE = "subtitle";
    protected static final String AD_ICON = "icon";
    protected static final String AD_COVER = "cover";
    protected static final String AD_MEDIA = "media";
    protected static final String AD_DETAIL = "detail";
    protected static final String AD_CTA = "cta";
    protected static final String AD_CHOICES = "choice";
    protected static final String AD_SPONSORED = "sponsored";
    protected static final String AD_SOCIAL = "social";
    protected static final String AD_RATE = "rate";

    private Random mRandom = new Random(System.currentTimeMillis());

    protected void restoreIconView(View rootView, String source, int iconId) {
        try {
            View iconView = rootView.findViewById(iconId);
            if (!(iconView instanceof ImageView)) {
                replaceSrcViewToDstView(iconView, new ImageView(rootView.getContext()));
            }
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    protected void restoreAdChoiceView(View rootView, int iconId) {
        try {
            View adChoiceView = rootView.findViewById(iconId);
            if (adChoiceView instanceof ViewGroup) {
                ((ViewGroup)adChoiceView).removeAllViews();
            }
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
    /**
     * 替换view
     * @param srcView 原始View
     * @param dstView 替换成的最终view
     * @return
     */
    private void replaceSrcViewToDstView(View srcView, View dstView) {
        Log.v(Log.TAG, "replace view for correct type");
        if (srcView != null && dstView != null) {
            dstView.setId(srcView.getId());
            ViewGroup.LayoutParams srcParams = srcView.getLayoutParams();
            android.widget.RelativeLayout.LayoutParams dstParams = new android.widget.RelativeLayout.LayoutParams(srcParams.width, srcParams.height);
            if (srcParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)srcParams;
                dstParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
            }
            if (srcParams instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams)srcParams;
                int[] rules = mainImageViewRelativeLayoutParams.getRules();

                for(int i = 0; i < rules.length; ++i) {
                    dstParams.addRule(i, rules[i]);
                }
            }
            ViewGroup viewGroup = (ViewGroup) srcView.getParent();
            if (viewGroup != null) {
                int index = viewGroup.indexOfChild(srcView);
                viewGroup.removeView(srcView);
                viewGroup.addView(dstView, index, dstParams);
            }
        }
    }

    protected void preSetMediaView(View rootView, PidConfig pidConfig) {
        if (rootView == null || pidConfig == null || pidConfig.getAspectRatio() <= 0f) {
            return;
        }
        try {
            PercentRelativeLayout percentRelativeLayout = rootView.findViewById(R.id.native_cover_info);
            View imageView = rootView.findViewById(R.id.native_image_cover);
            View mediaLayout = rootView.findViewById(R.id.native_media_cover);
            if (percentRelativeLayout == null) {
                return;
            }

            ViewGroup.LayoutParams params = null;
            if (percentRelativeLayout != null) {
                params = percentRelativeLayout.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(rootView.getContext(), 50));
                } else {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = Utils.dp2px(rootView.getContext(), 50);
                }
                percentRelativeLayout.setLayoutParams(params);
            }
            if (imageView != null) {
                params = imageView.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                imageView.setLayoutParams(params);
            }

            if (mediaLayout != null) {
                params = mediaLayout.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mediaLayout.setLayoutParams(params);
            }
        } catch (Exception | Error e) {
        }
    }

    protected void postSetMediaView(View rootView, PidConfig pidConfig) {
        if (rootView == null || pidConfig == null || pidConfig.getAspectRatio() <= 0f) {
            return;
        }

        try {
            PercentRelativeLayout percentRelativeLayout = rootView.findViewById(R.id.native_cover_info);
            if (percentRelativeLayout == null) {
                return;
            }
            calcLayout(percentRelativeLayout, pidConfig.getAspectRatio());
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    private void calcLayout(final View view, final double aspectRatio) {
        if (view == null) {
            return;
        }
        try {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        int width = view.getWidth();
                        if (width > 0) {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
                            params.height = (int) (width / aspectRatio);
                            view.setLayoutParams(params);
                            Log.v(Log.TAG, "width : " + width + " , height : " + params.height);
                        }
                    } catch (Exception | Error e) {
                        Log.e(Log.TAG, "error : " + e, e);
                    }
                }
            });
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
    }

    protected boolean allElementCanClick(int percent) {
        if (percent == 0) {
            return false;
        }
        if (percent < 0 || percent >= 100) return true;
        return mRandom.nextInt(100) < percent;
    }

    protected void onAdViewShown(View view, PidConfig pidConfig, Params params) {
    }

    protected int getFullLayout() {
        return mRandom.nextBoolean() ? R.layout.native_card_full : R.layout.native_card_mix;
    }

    protected boolean isClickable(String view, PidConfig pidConfig) {
        if (pidConfig == null) {
            return false;
        }
        List<String> clickViews = pidConfig.getClickViews();
        if (clickViews == null || clickViews.isEmpty()) {
            return true;
        }
        return clickViews.contains(view);
    }

    protected void clearClickListener(Params params, View rootView) {
        if (params == null || rootView == null) {
            return;
        }
        View view;
        view = rootView.findViewById(params.getAdTitle());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdSubTitle());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdIcon());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdCover());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdSocial());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdDetail());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdAction());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdChoices());
        if (view != null) {
            view.setOnClickListener(null);
        }
        view = rootView.findViewById(params.getAdMediaView());
        if (view != null) {
            view.setOnClickListener(null);
        }
    }
}
