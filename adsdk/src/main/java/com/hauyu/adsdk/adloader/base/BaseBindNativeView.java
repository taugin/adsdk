package com.hauyu.adsdk.adloader.base;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.earch.sunny.R;
import com.hauyu.adsdk.constant.Constant;
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

    protected void updateCtaButtonBackground(View view, PidConfig pidConfig, Params params) {
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
                layoutFlag = pidConfig.getAdPlace().getFullLayout();
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

    protected int getAdViewLayout(int template, PidConfig pidConfig) {
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
        return layoutId;
    }

    protected void bindParamsViewId(Params params) {
        if (params == null) {
            return;
        }
        params.setAdTitle(R.id.native_title);
        params.setAdSubTitle(R.id.native_sub_title);
        params.setAdSocial(R.id.native_social);
        params.setAdDetail(R.id.native_detail);
        params.setAdIcon(R.id.native_icon);
        params.setAdAction(R.id.native_action_btn);
        params.setAdCover(R.id.native_image_cover);
        params.setAdChoices(R.id.native_ad_choices_container);
        params.setAdMediaView(R.id.native_media_cover);
    }

    protected void centerChildView(ViewGroup viewGroup) {
        try {
            if (viewGroup instanceof LinearLayout) {
                ((LinearLayout) viewGroup).setGravity(Gravity.CENTER);
            } else if (viewGroup instanceof RelativeLayout) {
                ((RelativeLayout) viewGroup).setGravity(Gravity.CENTER);
            } else if (viewGroup instanceof FrameLayout) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
                if (params == null) {
                    params = new FrameLayout.LayoutParams(-1, -2);
                }
                params.gravity = Gravity.CENTER;
                viewGroup.setLayoutParams(params);
            }
        } catch (Exception e) {
        }
    }
}
