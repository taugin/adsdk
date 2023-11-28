package com.hauyu.adsdk.adloader.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.widget.R;
import com.hauyu.adsdk.Utils;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

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
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_RECT, R.layout.rab_card_rect);
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

    private static final String IC_BASE64_DEFAULT_ICON = "data:image/png;base64,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";

    protected void setDefaultAdIcon(String platform, ImageView imageView) {
        try {
            byte[] icon = Base64.decode(IC_BASE64_DEFAULT_ICON.split(",")[1], 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
            imageView.setImageBitmap(bitmap);

            int corner = Utils.dp2px(imageView.getContext(), 4);
            float[] outerR = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
            Shape shape = new RoundRectShape(outerR, null, null);
            ShapeDrawable shapeNormal = new ShapeDrawable(shape);
            shapeNormal.getPaint().setColor(imageView.getContext().getResources().getColor(R.color.rabDefaultIconColor));
            imageView.setBackground(shapeNormal);
        } catch (Exception e) {
            Log.iv(Log.TAG, platform + " set default ad icon error : " + e);
        }
    }

    protected void updateAdViewStatus(final View view, final Params params) {
        if (view == null || params == null) {
            return;
        }
        try {
            view.post(() -> updateFacebookChoiceView(view, params));
        } catch (Exception e) {
        }
    }

    private void updateFacebookChoiceView(View view, Params params) {
        try {
            if (view != null && params != null) {
                int adChoiceId = params.getAdChoices();
                if (adChoiceId > 0) {
                    View adChoiceView = view.findViewById(adChoiceId);
                    if (adChoiceView instanceof ViewGroup) {
                        setAdChoiceBgColor((ViewGroup) adChoiceView, "com.facebook.ads.AdOptionsView", Color.parseColor("#EECCCCCC"));
                    }
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private void setAdChoiceBgColor(ViewGroup viewGroup, String className, int color) {
        if (viewGroup == null) {
            return;
        }
        String name = getClassName(viewGroup);
        if (TextUtils.equals(name, className)) {
            viewGroup.setBackgroundColor(color);
            return;
        }
        int count = viewGroup.getChildCount();
        for (int index = 0; index < count; index++) {
            View subView = viewGroup.getChildAt(index);
            String viewClassName = getClassName(subView);
            if (TextUtils.equals(viewClassName, className)) {
                subView.setBackgroundColor(color);
                return;
            }
            if (subView instanceof ViewGroup) {
                setAdChoiceBgColor((ViewGroup) subView, className, color);
            }
        }
    }

    private String getClassName(View view) {
        try {
            return view.getClass().getName();
        } catch (Exception e) {
        }
        return null;
    }
}
