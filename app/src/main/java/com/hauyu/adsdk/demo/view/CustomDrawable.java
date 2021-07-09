package com.hauyu.adsdk.demo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.hauyu.adsdk.demo.Log;
import com.rabbit.adsdk.utils.Utils;

public class CustomDrawable extends ShapeDrawable {
    private Paint mPaint = new Paint();
    private Context mContext;
    private int mStrokeWidth;
    private int mRadius;
    private RectF mRectF = new RectF();
    private Shader mShader;
    private Matrix mMatrix = new Matrix();
    private Rect mBounds = new Rect();
    private ValueAnimator mValueAnimator;
    private int mBgColor = Color.WHITE;

    public CustomDrawable(Context context) {
        mContext = context;
        mStrokeWidth = Utils.dp2px(mContext, 4);
        mRadius = Utils.dp2px(mContext, 4);
        mPaint.setAntiAlias(true);
    }

    public static void setBackground(View view) {
        setBackground(view, Color.WHITE);
    }

    public static void setBackground(View view, int bgColor) {
        CustomDrawable customDrawable = new CustomDrawable(view.getContext());
        customDrawable.setBgColor(bgColor);
        view.setBackground(customDrawable);
        int padding = customDrawable.getStrokeWidth();
        padding += padding / 2;
        padding += Utils.dp2px(view.getContext(), 1);
        view.setPadding(padding, padding, padding, padding);
    }

    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBounds.set(bounds);
        Log.v(Log.TAG, "bounds : " + bounds);
        int[] colors = new int[]{Color.RED, Color.YELLOW, Color.RED, Color.YELLOW, Color.RED};
        float[] stops = null;//new float[]{0, 0.33f, 0.66f, 1f};
        mShader = new LinearGradient(0f, bounds.height() / 2, bounds.width(), bounds.height() / 2, colors, stops, Shader.TileMode.CLAMP);
        // mShader = new RadialGradient(bounds.width() / 2, bounds.height() / 2, bounds.height() / 2, colors, stops, Shader.TileMode.CLAMP);
        // mShader = new SweepGradient(bounds.width() / 2, bounds.height() / 2, colors, stops);
        mShader.setLocalMatrix(mMatrix);
        setupAnimation();
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();
        mRectF.set(rect);
        mRectF.inset(mStrokeWidth / 2, mStrokeWidth / 2);
        mPaint.setShader(mShader);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
        mRectF.inset(mStrokeWidth, mStrokeWidth);
        mPaint.setColor(mBgColor);
        mPaint.setShader(null);
        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
    }

    private void setupAnimation() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofFloat(0, 360);
            mValueAnimator.setDuration(5000);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (isVisible()) {
                        float degree = (float) animation.getAnimatedValue();
                        if (mMatrix != null && mShader != null) {
                            mMatrix.setRotate(degree, mBounds.width() / 2, mBounds.height() / 2);
                            mShader.setLocalMatrix(mMatrix);
                            invalidateSelf();
                        }
                    } else {
                        mValueAnimator.pause();
                    }
                }
            });
        }
        if (mValueAnimator != null) {
            if (mValueAnimator.isPaused()) {
                mValueAnimator.resume();
            } else {
                mValueAnimator.start();
            }
        }
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        Log.v(Log.TAG, "visible : " + visible);
        if (visible) {
            setupAnimation();
        }
        return super.setVisible(visible, restart);
    }
}
