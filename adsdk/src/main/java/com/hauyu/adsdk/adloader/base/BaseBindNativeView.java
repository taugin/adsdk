package com.hauyu.adsdk.adloader.base;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gekes.fvs.tdsvap.R;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

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

    private static final String LAYOUT_FULL = "one";
    private static final String LAYOUT_MIX = "two";
    private static final String LAYOUT_REVER = "three";
    private static final String LAYOUT_LARGE = "large";
    private static final String LAYOUT_SMALL = "small";
    private static final String LAYOUT_TINY = "tiny";
    private static final String LAYOUT_MEDIUM = "medium";
    private static final int[] CARD_LAYOUT = new int[]{R.layout.had_card_full, R.layout.had_card_mix, R.layout.had_card_rever};

    private Random mRandom = new Random(System.currentTimeMillis());

    protected void restoreIconView(View rootView, String source, int iconId) {
        try {
            View iconView = rootView.findViewById(iconId);
            if (!(iconView instanceof ImageView)) {
                replaceSrcViewToDstView(iconView, new ImageView(rootView.getContext()));
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "restore icon view error : " + e);
        }
    }

    protected void restoreAdChoiceView(View rootView, int iconId) {
        try {
            View adChoiceView = rootView.findViewById(iconId);
            if (adChoiceView instanceof ViewGroup) {
                int count = ((ViewGroup) adChoiceView).getChildCount();
                for (int index = 0; index < count; index++) {
                    ((ViewGroup) adChoiceView).getChildAt(index).setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "restore ad choice view error : " + e);
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
        Log.iv(Log.TAG, "replace view for correct type");
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
        if (pidConfig == null || view == null || params == null) {
            return;
        }
        View ctaView = view.findViewById(params.getAdAction());
        int normalColor = Color.TRANSPARENT;
        int pressedColor = Color.TRANSPARENT;
        try {
            normalColor = Color.parseColor(pidConfig.getCtaColor().get(0));
            pressedColor = Color.parseColor(pidConfig.getCtaColor().get(1));
        } catch (Exception e) {
            Log.iv(Log.TAG, "cc error : " + e);
        }
        // 获取 color
        if (normalColor == Color.TRANSPARENT || pressedColor == Color.TRANSPARENT) {
            try {
                normalColor = Color.parseColor(pidConfig.getAdPlace().getCtaColor().get(0));
                pressedColor = Color.parseColor(pidConfig.getAdPlace().getCtaColor().get(1));
            } catch (Exception e) {
                Log.iv(Log.TAG, "cc error : " + e);
            }
        }
        if (normalColor != Color.TRANSPARENT && pressedColor != Color.TRANSPARENT && ctaView != null) {
            setBackgroundByConfig(ctaView, normalColor, pressedColor);
        }
    }

    private void setBackgroundByConfig(View view, int normalColor, int pressedColor) {
        try {
            float corner = Utils.dp2px(view.getContext(), 2);
            float[] roundArray = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
            ShapeDrawable shapePressed = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
            shapePressed.getPaint().setColor(pressedColor);

            ShapeDrawable shapeNormal = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
            shapeNormal.getPaint().setColor(normalColor);

            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
            drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
            view.setBackground(drawable);
        } catch (Exception e) {
            Log.iv(Log.TAG, "set bg error : " + e);
        }
    }

    protected int getFullLayout(PidConfig pidConfig) {
        if (pidConfig == null) {
            return CARD_LAYOUT[mRandom.nextInt(CARD_LAYOUT.length)];
        }
        String layoutFlag = pidConfig.getLayout();
        // 获取 layout flag
        if (TextUtils.isEmpty(layoutFlag)) {
            try {
                layoutFlag = pidConfig.getAdPlace().getLayout();
            } catch (Exception e) {
                Log.iv(Log.TAG, "fl error : " + e);
            }
        }
        if (LAYOUT_FULL.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_full;
        } else if (LAYOUT_MIX.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_mix;
        } else if (LAYOUT_REVER.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_rever;
        } else if (LAYOUT_LARGE.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_large;
        } else if (LAYOUT_SMALL.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_small;
        } else if (LAYOUT_TINY.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_tiny;
        } else if (LAYOUT_MEDIUM.equalsIgnoreCase(layoutFlag)) {
            return R.layout.had_card_medium;
        }
        return CARD_LAYOUT[mRandom.nextInt(CARD_LAYOUT.length)];
    }

    protected boolean isClickable(String view, PidConfig pidConfig) {
        if (pidConfig == null) {
            return false;
        }
        List<String> clickViews = getClickViews(pidConfig);
        if (clickViews == null || clickViews.isEmpty()) {
            return true;
        }
        return clickViews.contains(view);
    }

    /**
     * 恢复视图的初始状态，由于mopub是在加载native的时候绑定的view，因此，在做展示的时候，不能修改视图结构，所以不能移除任何view
     * @param params
     * @param rootView
     */
    protected void restoreAdViewContent(Params params, View rootView) {
        if (params == null || rootView == null) {
            return;
        }
        View view;
        view = rootView.findViewById(params.getAdTitle());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(null);
        }

        view = rootView.findViewById(params.getAdSubTitle());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(null);
        }

        view = rootView.findViewById(params.getAdIcon());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
        }

        view = rootView.findViewById(params.getAdCover());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
        }

        view = rootView.findViewById(params.getAdSocial());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(null);
        }

        view = rootView.findViewById(params.getAdDetail());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(null);
        }

        view = rootView.findViewById(params.getAdAction());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(null);
        }

        view = rootView.findViewById(params.getAdChoices());
        if (view != null) {
            view.setOnClickListener(null);
        }

        view = rootView.findViewById(params.getAdMediaView());
        if (view != null) {
            view.setOnClickListener(null);
        }
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).removeAllViews();
        }
    }

    protected List<String> getClickViews(PidConfig pidConfig) {
        List<String> clickViews = pidConfig.getClickViews();
        // 获取clickviews
        if (clickViews == null || clickViews.isEmpty()) {
            try {
                clickViews = pidConfig.getAdPlace().getClickViews();
            } catch (Exception e) {
            }
        }
        return clickViews;
    }
}
