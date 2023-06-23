package com.hauyu.adsdk.adloader.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.Utils;
import com.unity3d.widget.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2018-12-12.
 */

public class BaseBindNativeView {

    protected static final String AD_TITLE = "title";
    protected static final String AD_ICON = "icon";
    protected static final String AD_COVER = "cover";
    protected static final String AD_MEDIA = "media";
    protected static final String AD_DETAIL = "detail";
    protected static final String AD_CTA = "cta";
    protected static final String AD_CHOICES = "choice";
    protected static final String AD_SPONSORED = "sponsored";
    protected static final String AD_SOCIAL = "social";
    protected static final String AD_RATE = "rate";
    protected static final String AD_ADVERTISER = "advertiser";
    protected static final String AD_PRICE = "price";
    protected static final String AD_STORE = "store";
    protected static final String AD_VIDEO = "video";
    protected static final String AD_BACKGROUND = "background";

    private static final Map<String, Integer> LAYOUT_MAPS;

    static {
        LAYOUT_MAPS = new HashMap<String, Integer>();
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MICRO, R.layout.rab_card_micro);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_TINY, R.layout.rab_card_tiny);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LITTLE, R.layout.rab_card_little);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_SMALL, R.layout.rab_card_small);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MEDIUM, R.layout.rab_card_medium);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LARGE, R.layout.rab_card_large);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_WRAP, R.layout.rab_card_wrap);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_ROUND, R.layout.rab_card_round);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FULL, R.layout.rab_card_full);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MIX, R.layout.rab_card_mix);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FOOT, R.layout.rab_card_foot);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_HEAD, R.layout.rab_card_head);
    }

    private Random mRandom = new Random(System.currentTimeMillis());

    private Map<String, String> mAdvMap = new HashMap<>();

    public Map<String, String> getAdvMap() {
        return mAdvMap;
    }

    protected void putValue(String key, String value) {
        mAdvMap.put(key, value);
    }

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
        }
        // 获取 color
        if (normalColor == Color.TRANSPARENT || pressedColor == Color.TRANSPARENT) {
            try {
                normalColor = Color.parseColor(pidConfig.getAdPlace().getCtaColor().get(0));
                pressedColor = Color.parseColor(pidConfig.getAdPlace().getCtaColor().get(1));
            } catch (Exception e) {
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

    /**
     * 获取最佳布局
     *
     * @param context
     * @param pidConfig
     * @param params
     * @param network
     * @return
     */
    protected int getBestNativeLayout(Context context, PidConfig pidConfig, Params params, String network) {
        boolean useCardStyle;
        int adRootLayout;
        int rootLayout = params.getNativeRootLayout();
        String template = params.getNativeCardStyle();
        if (rootLayout > 0) {
            useCardStyle = false;
            template = "custom";
        } else {
            useCardStyle = true;
            rootLayout = getAdViewLayout(context, template, pidConfig);
            bindParamsViewId(params);
            if (TextUtils.isEmpty(template)) {
                template = "default";
            }
        }

        String layout = "none";
        if (useCardStyle && !TextUtils.isEmpty(network)) {
            try {
                Pair<String, Integer> pair = getSubNativeLayout(pidConfig, network);
                adRootLayout = pair.second;
                layout = pair.first;
            } catch (Exception e) {
                adRootLayout = 0;
            }
            if (adRootLayout > 0) {
                bindParamsViewId(params);
            } else {
                adRootLayout = rootLayout;
            }
        } else {
            adRootLayout = rootLayout;
        }
        try {
            Log.iv(Log.TAG, "bind native layout [" + pidConfig.getSdk() + " : " + template + "] [" + network + " : " + (adRootLayout > 0 ? layout : "none") + "] - [" + pidConfig.getPlaceName() + "]");
        } catch (Exception e) {
        }
        return adRootLayout;
    }

    private int getNativeLayout(Context context, PidConfig pidConfig) {
        if (pidConfig != null && context != null) {
            List<String> layoutFlag = pidConfig.getNativeLayout();
            // 获取 layout flag
            if (layoutFlag == null || layoutFlag.isEmpty()) {
                try {
                    layoutFlag = pidConfig.getAdPlace().getNativeLayout();
                } catch (Exception e) {
                    Log.iv(Log.TAG, "nl error : " + e);
                }
            }
            if (layoutFlag != null && !layoutFlag.isEmpty()) {
                String layout = layoutFlag.get(mRandom.nextInt(layoutFlag.size()));
                if (!TextUtils.isEmpty(layout)) {
                    Log.iv(Log.TAG, "layout flag : " + layout);
                    Integer nativeLayout = LAYOUT_MAPS.get(layout);
                    if (nativeLayout != null) {
                        return nativeLayout.intValue();
                    }
                }
            }
        }
        return 0;
    }

    protected boolean isClickable(String view, PidConfig pidConfig) {
        if (pidConfig == null) {
            return false;
        }
        List<String> clickView = getClickView(pidConfig);
        if (clickView == null || clickView.isEmpty()) {
            return true;
        }
        return clickView.contains(view);
    }

    protected List<String> getClickView(PidConfig pidConfig) {
        List<String> clickView = pidConfig.getClickView();
        // 获取clickviews
        if (clickView == null || clickView.isEmpty()) {
            try {
                clickView = pidConfig.getAdPlace().getClickView();
            } catch (Exception e) {
            }
        }
        return clickView;
    }

    protected List<String> getClickViewRender(PidConfig pidConfig) {
        List<String> clickViewRender = pidConfig.getClickViewRender();
        // 获取clickviews
        if (clickViewRender == null || clickViewRender.isEmpty()) {
            try {
                clickViewRender = pidConfig.getAdPlace().getClickViewRender();
            } catch (Exception e) {
            }
        }
        return clickViewRender;
    }

    private int getAdViewLayout(Context context, String template, PidConfig pidConfig) {
        int layoutId = getNativeLayout(context, pidConfig);
        if (layoutId == 0) {
            try {
                layoutId = LAYOUT_MAPS.get(template);
            } catch (Exception e) {
                layoutId = R.layout.rab_card_little;
            }
        }
        return layoutId;
    }

    private Pair<String, Integer> getSubNativeLayout(PidConfig pidConfig, String sdk) {
        if (pidConfig != null && !TextUtils.isEmpty(sdk)) {
            Map<String, String> subNativeLayout = pidConfig.getSubNativeLayout();
            if (subNativeLayout != null) {
                String layout = subNativeLayout.get(sdk);
                if (!TextUtils.isEmpty(layout)) {
                    Integer nativeLayout = LAYOUT_MAPS.get(layout);
                    if (nativeLayout != null) {
                        return new Pair<>(layout, nativeLayout.intValue());
                    }
                }
            }
        }
        return null;
    }

    protected void bindParamsViewId(Params params) {
        if (params == null) {
            return;
        }
        params.setAdTitle(R.id.rab_native_title);
        params.setAdSocial(R.id.rab_native_social);
        params.setAdDetail(R.id.rab_native_detail);
        params.setAdIcon(R.id.rab_native_icon);
        params.setAdAction(R.id.rab_native_action_btn);
        params.setAdCover(R.id.rab_native_image_cover);
        params.setAdChoices(R.id.rab_native_ad_choices_container);
        params.setAdMediaView(R.id.rab_native_media_cover);
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

    protected void replaceSrcViewToDstView(View srcView, View dstView) {
        if (srcView != null && dstView != null) {
            int paddingStart = srcView.getPaddingStart();
            int paddingTop = srcView.getPaddingTop();
            int paddingEnd = srcView.getPaddingEnd();
            int paddingBottom = srcView.getPaddingBottom();
            dstView.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
            dstView.setId(srcView.getId());
            ViewGroup.LayoutParams layoutParams = srcView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(-2, -2);
            }
            ViewGroup viewGroup = (ViewGroup) srcView.getParent();
            if (viewGroup != null) {
                int index = viewGroup.indexOfChild(srcView);
                viewGroup.removeView(srcView);
                viewGroup.addView(dstView, index, layoutParams);
            }
        }
    }
}
