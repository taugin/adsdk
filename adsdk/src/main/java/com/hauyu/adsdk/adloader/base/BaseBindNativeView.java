package com.hauyu.adsdk.adloader.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gekes.fvs.tdsvap.R;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

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
    private static final int[] CARD_LAYOUT = new int[]{R.layout.had_card_full, R.layout.had_card_mix, R.layout.had_card_rever};

    private Random mRandom = new Random(System.currentTimeMillis());

    protected void restoreIconView(View rootView, String source, int iconId) {
        try {
            View iconView = rootView.findViewById(iconId);
            if (!(iconView instanceof ImageView)) {
                replaceSrcViewToDstView(iconView, new ImageView(rootView.getContext()));
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    protected void restoreAdChoiceView(View rootView, int iconId) {
        try {
            View adChoiceView = rootView.findViewById(iconId);
            if (adChoiceView instanceof ViewGroup) {
                ((ViewGroup) adChoiceView).removeAllViews();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    /**
     * 替换view
     *
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
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) srcParams;
                dstParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
            }
            if (srcParams instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) srcParams;
                int[] rules = mainImageViewRelativeLayoutParams.getRules();

                for (int i = 0; i < rules.length; ++i) {
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

    protected void onAdViewShown(View view, PidConfig pidConfig, Params params) {
    }

    protected int getFullLayout() {
        return CARD_LAYOUT[mRandom.nextInt(CARD_LAYOUT.length)];
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
