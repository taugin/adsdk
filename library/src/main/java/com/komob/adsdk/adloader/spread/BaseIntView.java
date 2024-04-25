package com.komob.adsdk.adloader.spread;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.komob.adsdk.data.config.SpreadConfig;
import com.komob.adsdk.http.Http;
import com.komob.adsdk.http.OnImageCallback;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class BaseIntView {

    private static Random sRandom = new Random(System.currentTimeMillis());

    protected ViewGroup mRootView;
    protected TextView mTitleView;
    protected TextView mDetailView;
    protected ImageView mMediaView;
    protected ImageView mIconView;
    protected TextView mActionView;
    protected View mCloseView;
    protected RatingBar mRatingBar;

    protected void loadAndShowImage(final ImageView imageView, String url) {
        try {
            Http.get(imageView.getContext()).loadImage(url, ImageView.ScaleType.CENTER_CROP, new OnImageCallback() {

                @Override
                public void onSuccess(Bitmap bitmap) {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }

                @Override
                public void onFailure(int code, String error) {
                    Log.iv(Log.TAG, "code : " + code + " , error : " + error);
                }
            });
        } catch (Exception e) {
        }
    }

    protected ImageView generateCloseView(Context context, int imageColor, int normalColor, int pressedColor) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        imageView.setColorFilter(imageColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setElevation(15f);
        }
        Shape shape = new OvalShape();
        ShapeDrawable shapeNormal = new ShapeDrawable(shape);
        shapeNormal.getPaint().setColor(normalColor);

        shape = new OvalShape();
        ShapeDrawable shapePressed = new ShapeDrawable(shape);
        shapePressed.getPaint().setColor(pressedColor);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        imageView.setBackground(drawable);
        int padding = Utils.dp2px(context, 2);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setClickable(true);
        return imageView;
    }

    protected Drawable generateBackground(Context context, int colorNormal, int colorPressed) {
        int radius = Utils.dp2px(context, 12);
        float[] radii = {radius, radius, radius, radius, radius, radius, radius, radius}; // 每个角的圆角半径
        Shape shape = new RoundRectShape(radii, null, null);

        ShapeDrawable shapePressed = new ShapeDrawable(shape);
        shapePressed.getPaint().setColor(colorPressed);

        shape = new RoundRectShape(radii, null, null);
        ShapeDrawable shapeNormal = new ShapeDrawable(shape);
        shapeNormal.getPaint().setColor(colorNormal);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
        return drawable;
    }

    private static String getLanguage(Context context) {
        String language = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            language = locale.getLanguage().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
        }
        return language;
    }

    protected String getCTAText(Context context, SpreadConfig spreadConfig) {
        if (spreadConfig == null) {
            return null;
        }
        String ctaText = spreadConfig.getCta();
        try {
            Map<String, String> map = spreadConfig.getCtaLocale();
            if (map != null) {
                String language = getLanguage(context);
                if (!TextUtils.isEmpty(language)) {
                    String ctaLocale = map.get(language);
                    if (!TextUtils.isEmpty(ctaLocale)) {
                        ctaText = ctaLocale;
                    }
                }
            }
        } catch (Exception e) {
        }
        return ctaText;
    }

    protected void scaleView(View view) {
        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setRepeatCount(Animation.INFINITE);
                        scaleAnimation.setDuration(1000);
                        scaleAnimation.setRepeatMode(Animation.REVERSE);
                        view.startAnimation(scaleAnimation);
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    public static BaseIntView generate(Context context) {
        if (sRandom.nextBoolean()) {
            return new IntView2().init(context);
        }
        return new IntView1().init(context);
    }

    public View render(SpreadConfig spreadConfig) {
        if (spreadConfig != null) {
            String title = spreadConfig.getTitle();
            if (mTitleView != null) {
                mTitleView.setText(title);
            }
            String detail = spreadConfig.getDetail();
            if (mDetailView != null) {
                mDetailView.setText(detail);
            }
            String banner = spreadConfig.getBanner();
            if (mMediaView != null) {
                loadAndShowImage(mMediaView, banner);
            }
            String icon = spreadConfig.getIcon();
            if (mIconView != null) {
                loadAndShowImage(mIconView, icon);
            }
            if (mActionView != null) {
                String action = getCTAText(mActionView.getContext(), spreadConfig);
                mActionView.setText(action);
            }
            double score = spreadConfig.getScore();
            if (mRatingBar != null) {
                mRatingBar.setRating((float) score);
            }
        }
        return mRootView;
    }

    public void reset() {
        if (mTitleView != null) {
            mTitleView.setText(null);
        }
        if (mDetailView != null) {
            mDetailView.setText(null);
        }
        if (mMediaView != null) {
            mMediaView.setImageDrawable(null);
        }
        if (mIconView != null) {
            mIconView.setImageDrawable(null);
        }
        if (mActionView != null) {
            mActionView.setText(null);
        }
    }

    public BaseIntView setActionClickListener(View.OnClickListener actionClickListener) {
        if (mActionView != null) {
            mActionView.setOnClickListener(actionClickListener);
        }
        return this;
    }

    public BaseIntView setCloseClickListener(View.OnClickListener closeClickListener) {
        if (mCloseView != null) {
            mCloseView.setOnClickListener(closeClickListener);
        }
        return this;
    }
}

class IntView1 extends BaseIntView {
    public IntView1() {
    }

    public IntView1 init(Context context) {
        mRootView = new RelativeLayout(context);
        mRootView.setBackgroundColor(Color.WHITE);
        LinearLayout adLayout = new LinearLayout(context);
        adLayout.setOrientation(LinearLayout.VERTICAL);
        adLayout.setGravity(Gravity.CENTER);
        mRootView.addView(adLayout, -1, -1);

        // Close View
        mCloseView = generateCloseView(context, Color.BLACK, Color.parseColor("#FFFFFFFF"), Color.parseColor("#88FFFFFF"));
        int size = Utils.dp2px(context, 24);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
        int margin = Utils.dp2px(context, 8);
        layoutParams.setMargins(margin, margin, 0, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRootView.addView(mCloseView, layoutParams);

        // AD Label
        TextView adLabel = new TextView(context);
        adLabel.setText("AD");
        adLabel.setTextColor(Color.BLACK);
        adLabel.setTypeface(null, Typeface.BOLD);
        adLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        adLabel.setGravity(Gravity.CENTER);
        int adLabelHPadding = Utils.dp2px(context, 4);
        int adLabelVPadding = Utils.dp2px(context, 2);
        adLabel.setPadding(adLabelHPadding, adLabelVPadding, adLabelHPadding, adLabelVPadding);
        adLabel.setBackground(generateBackground(context, Color.WHITE, Color.WHITE));
        RelativeLayout.LayoutParams adLabelLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        int adLabelHMargin = Utils.dp2px(context, 8);
        int adLabelVMargin = Utils.dp2px(context, 12);
        adLabelLayoutParams.setMargins(0, adLabelVMargin, adLabelHMargin, 0);
        adLabelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        adLabelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRootView.addView(adLabel, adLabelLayoutParams);

        // Media View
        mMediaView = new ImageView(context);
        int mediaViewHeight = (int) (context.getResources().getDisplayMetrics().widthPixels / 1.92f);
        ViewGroup.LayoutParams mediaViewParams = new ViewGroup.LayoutParams(-1, mediaViewHeight);
        adLayout.addView(mMediaView, mediaViewParams);

        // Title detail layout
        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setGravity(Gravity.CENTER);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-1, -1);
        textParams.weight = 1;
        adLayout.addView(textLayout, textParams);

        // Icon View
        mIconView = new ImageView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIconView.setElevation(0.5f);
        }
        int iconSize = Utils.dp2px(context, 64);
        ViewGroup.MarginLayoutParams iconViewParams = new ViewGroup.MarginLayoutParams(iconSize, iconSize);
        int iconMargin = Utils.dp2px(context, 20);
        iconViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mIconView, iconViewParams);

        // Title View
        mTitleView = new TextView(context);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleView.setTypeface(null, Typeface.BOLD);
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setTextColor(Color.BLACK);
        mTitleView.setMaxLines(2);
        mTitleView.setLines(2);
        mTitleView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.MarginLayoutParams titleViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        titleViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mTitleView, titleViewParams);

        // Detail View
        mDetailView = new TextView(context);
        mDetailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mDetailView.setGravity(Gravity.CENTER);
        mDetailView.setTextColor(Color.BLACK);
        mDetailView.setMaxLines(3);
        mDetailView.setLines(3);
        mDetailView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.MarginLayoutParams detailViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        detailViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mDetailView, detailViewParams);

        // RatingBar
        mRatingBar = new RatingBar(context);
        mRatingBar.setMax(5);
        mRatingBar.setNumStars(5);
        mRatingBar.setStepSize(0.1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRatingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF4080FF")));
            mRatingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }
        mRatingBar.setRating(3.5f);
        mRatingBar.setIsIndicator(true);
        LinearLayout.MarginLayoutParams ratingBarViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        ratingBarViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mRatingBar, ratingBarViewParams);
        mRatingBar.setScaleX(0.5f);
        mRatingBar.setScaleY(0.5f);

        // Action View
        mActionView = new TextView(context);
        mActionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mActionView.setTypeface(null, Typeface.BOLD);
        mActionView.setGravity(Gravity.CENTER);
        mActionView.setTextColor(Color.WHITE);
        int actionViewHeight = Utils.dp2px(context, 56);
        ViewGroup.MarginLayoutParams actionParams = new ViewGroup.MarginLayoutParams(-1, actionViewHeight);
        int hMargin = Utils.dp2px(context, 36);
        int vMargin = Utils.dp2px(context, 18);
        actionParams.leftMargin = hMargin;
        actionParams.rightMargin = hMargin;
        actionParams.bottomMargin = vMargin;
        actionParams.topMargin = vMargin;
        adLayout.addView(mActionView, actionParams);
        mActionView.setBackground(generateBackground(context, Color.parseColor("#FF4080FF"), Color.parseColor("#FF3973E5")));
        scaleView(mActionView);
        return this;
    }
}

class IntView2 extends BaseIntView {
    public IntView2 init(Context context) {
        mRootView = new RelativeLayout(context);
        mRootView.setBackgroundColor(Color.WHITE);
        LinearLayout adLayout = new LinearLayout(context);
        adLayout.setOrientation(LinearLayout.VERTICAL);
        adLayout.setGravity(Gravity.CENTER);
        mRootView.addView(adLayout, -1, -1);

        // Close View
        // Close View
        mCloseView = generateCloseView(context, Color.BLACK, Color.parseColor("#FFFFFFFF"), Color.parseColor("#88FFFFFF"));
        int size = Utils.dp2px(context, 24);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
        int margin = Utils.dp2px(context, 8);
        layoutParams.setMargins(margin, margin, 0, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRootView.addView(mCloseView, layoutParams);

        // AD Label
        TextView adLabel = new TextView(context);
        adLabel.setText("AD");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adLabel.setElevation(15f);
        }
        adLabel.setTextColor(Color.WHITE);
        adLabel.setTypeface(null, Typeface.BOLD);
        adLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        adLabel.setGravity(Gravity.CENTER);
        int adLabelHPadding = Utils.dp2px(context, 4);
        int adLabelVPadding = Utils.dp2px(context, 2);
        adLabel.setPadding(adLabelHPadding, adLabelVPadding, adLabelHPadding, adLabelVPadding);
        adLabel.setBackground(generateBackground(context, Color.GRAY, Color.GRAY));
        RelativeLayout.LayoutParams adLabelLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        int adLabelHMargin = Utils.dp2px(context, 8);
        int adLabelVMargin = Utils.dp2px(context, 12);
        adLabelLayoutParams.setMargins(0, adLabelVMargin, adLabelHMargin, 0);
        adLabelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        adLabelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRootView.addView(adLabel, adLabelLayoutParams);

        LinearLayout topLayout = new LinearLayout(context);
        topLayout.setOrientation(LinearLayout.VERTICAL);
        topLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(-1, -1);
        topParams.weight = 1;
        adLayout.addView(topLayout, topParams);

        LinearLayout bottomLayout = new LinearLayout(context);
        bottomLayout.setOrientation(LinearLayout.VERTICAL);
        bottomLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(-1, -1);
        bottomParams.weight = 1;
        adLayout.addView(bottomLayout, bottomParams);

        // Title detail layout
        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setGravity(Gravity.CENTER);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-1, -1);
        textParams.weight = 1;
        topLayout.addView(textLayout, textParams);

        // Icon View
        mIconView = new ImageView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIconView.setElevation(15f);
        }
        int iconSize = Utils.dp2px(context, 64);
        ViewGroup.MarginLayoutParams iconViewParams = new ViewGroup.MarginLayoutParams(iconSize, iconSize);
        int iconMargin = Utils.dp2px(context, 20);
        iconViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mIconView, iconViewParams);

        // Title View
        mTitleView = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTitleView.setElevation(15f);
        }
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleView.setTypeface(null, Typeface.BOLD);
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setTextColor(Color.BLACK);
        mTitleView.setMaxLines(2);
        mTitleView.setLines(2);
        mTitleView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.MarginLayoutParams titleViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        titleViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mTitleView, titleViewParams);

        // Detail View
        mDetailView = new TextView(context);
        mDetailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mDetailView.setGravity(Gravity.CENTER);
        mDetailView.setTextColor(Color.BLACK);
        mDetailView.setMaxLines(3);
        mDetailView.setLines(3);
        mDetailView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.MarginLayoutParams detailViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        detailViewParams.setMargins(iconMargin, iconMargin, iconMargin, iconMargin);
        textLayout.addView(mDetailView, detailViewParams);

        // Media View
        mMediaView = new ImageView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaView.setElevation(15f);
        }
        int mediaMargin = Utils.dp2px(context, 8);
        int mediaViewWidth = context.getResources().getDisplayMetrics().widthPixels;
        int mediaViewHeight = (int) ((mediaViewWidth - mediaMargin * 2) / 1.92f);
        LinearLayout.MarginLayoutParams mediaViewParams = new LinearLayout.MarginLayoutParams(-1, mediaViewHeight);
        mediaViewParams.setMargins(mediaMargin, mediaMargin, mediaMargin, mediaMargin);
        bottomLayout.addView(mMediaView, mediaViewParams);

        // Action View
        mActionView = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActionView.setElevation(15f);
        }
        mActionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mActionView.setTypeface(null, Typeface.BOLD);
        mActionView.setGravity(Gravity.CENTER);
        mActionView.setTextColor(Color.WHITE);
        int actionViewHeight = Utils.dp2px(context, 56);
        ViewGroup.MarginLayoutParams actionParams = new ViewGroup.MarginLayoutParams(-1, actionViewHeight);
        int hMargin = Utils.dp2px(context, 36);
        int vMargin = Utils.dp2px(context, 18);
        actionParams.leftMargin = hMargin;
        actionParams.rightMargin = hMargin;
        actionParams.bottomMargin = vMargin;
        actionParams.topMargin = vMargin;
        bottomLayout.addView(mActionView, actionParams);
        mActionView.setBackground(generateBackground(context, Color.parseColor("#FF4080FF"), Color.parseColor("#FF3973E5")));
        scaleView(mActionView);

        // RatingBar
        mRatingBar = new RatingBar(context);
        mRatingBar.setMax(5);
        mRatingBar.setNumStars(5);
        mRatingBar.setStepSize(0.1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRatingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF4080FF")));
            mRatingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }
        mRatingBar.setRating(3.5f);
        mRatingBar.setIsIndicator(true);
        LinearLayout.MarginLayoutParams ratingBarViewParams = new LinearLayout.MarginLayoutParams(-2, -2);
        ratingBarViewParams.setMargins(iconMargin, 0, iconMargin, iconMargin);
        bottomLayout.addView(mRatingBar, ratingBarViewParams);
        mRatingBar.setScaleX(0.5f);
        mRatingBar.setScaleY(0.5f);
        return this;
    }
}
