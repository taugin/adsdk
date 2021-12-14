package com.rabbit.adsdk.adloader.base;

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

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.R;

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
        LAYOUT_MAPS.put("micro", R.layout.rab_card_micro);
        LAYOUT_MAPS.put("tiny", R.layout.rab_card_tiny);
        LAYOUT_MAPS.put("little", R.layout.rab_card_little);
        LAYOUT_MAPS.put("small", R.layout.rab_card_small);
        LAYOUT_MAPS.put("medium", R.layout.rab_card_medium);
        LAYOUT_MAPS.put("large", R.layout.rab_card_large);
        LAYOUT_MAPS.put("wrap", R.layout.rab_card_wrap);
        LAYOUT_MAPS.put("full", R.layout.rab_card_full);
        LAYOUT_MAPS.put("mix", R.layout.rab_card_mix);
        LAYOUT_MAPS.put("foot", R.layout.rab_card_foot);
        LAYOUT_MAPS.put("head", R.layout.rab_card_head);
        LAYOUT_MAPS.put("round", R.layout.rab_card_round);
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

    protected int getNativeLayout(Context context, PidConfig pidConfig) {
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
                    Log.v(Log.TAG, "layout flag : " + layout);
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

    protected int getAdViewLayout(Context context, int template, PidConfig pidConfig) {
        int layoutId = getNativeLayout(context, pidConfig);
        if (layoutId == 0) {
            if (template == Constant.NATIVE_CARD_SMALL) {
                layoutId = R.layout.rab_card_small;
            } else if (template == Constant.NATIVE_CARD_MEDIUM) {
                layoutId = R.layout.rab_card_medium;
            } else if (template == Constant.NATIVE_CARD_LARGE) {
                layoutId = R.layout.rab_card_large;
            } else if (template == Constant.NATIVE_CARD_FULL) {
                layoutId = R.layout.rab_card_full;
            } else if (template == Constant.NATIVE_CARD_TINY) {
                layoutId = R.layout.rab_card_tiny;
            } else if (template == Constant.NATIVE_CARD_MICRO) {
                layoutId = R.layout.rab_card_micro;
            } else if (template == Constant.NATIVE_CARD_WRAP) {
                layoutId = R.layout.rab_card_wrap;
            } else if (template == Constant.NATIVE_CARD_HEAD) {
                layoutId = R.layout.rab_card_head;
            } else if (template == Constant.NATIVE_CARD_MIX) {
                layoutId = R.layout.rab_card_mix;
            } else if (template == Constant.NATIVE_CARD_FOOT) {
                layoutId = R.layout.rab_card_foot;
            } else if (template == Constant.NATIVE_CARD_ROUND) {
                layoutId = R.layout.rab_card_round;
            } else if (template == Constant.NATIVE_CARD_LITTLE) {
                layoutId = R.layout.rab_card_little;
            } else {
                layoutId = R.layout.rab_card_little;
            }
        }
        return layoutId;
    }

    protected Pair<String, Integer> getSubNativeLayout(PidConfig pidConfig, String sdk) {
        if (pidConfig != null && !TextUtils.isEmpty(sdk)) {
            Map<String, String> subNativeLayout = pidConfig.getSubNativeLayout();
            if (subNativeLayout != null) {
                String layout = subNativeLayout.get(sdk);
                if (!TextUtils.isEmpty(layout)) {
                    Log.v(Log.TAG, "sub native layout flag : " + layout);
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
}
