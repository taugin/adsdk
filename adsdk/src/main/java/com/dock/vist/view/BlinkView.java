package com.dock.vist.view;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.dock.vist.sun.IAdvance;
import com.dock.vist.sun.R;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2019-12-20.
 */

public class BlinkView extends AppCompatImageView implements ValueAnimator.AnimatorUpdateListener, IAdvance.IBlank {
    private int startAlpha, endAlpha, duration;
    private boolean isBlinking = false;

    ValueAnimator colorAnimation;

    public BlinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BlinkView, 0, 0);
        try {
            startAlpha = a.getInteger(R.styleable.BlinkView_startAlpha, 63);
            endAlpha = a.getInteger(R.styleable.BlinkView_endAlpha, 255);
            duration = a.getInteger(R.styleable.BlinkView_blinkDuration, 800);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } finally {
            a.recycle();
        }
        setAlpha(startAlpha / 256f);
    }

    @Override
    public void begin() {
        if (!isBlinking) {
            colorAnimation = ValueAnimator.ofObject(new IntEvaluator(), startAlpha, endAlpha);
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
            colorAnimation.setDuration(duration); // milliseconds
            colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            colorAnimation.addUpdateListener(this);
            colorAnimation.start();
            isBlinking = true;
        }
    }

    @Override
    public void end() {
        if (isBlinking) {
            colorAnimation.removeUpdateListener(this);
            colorAnimation.end();
            postInvalidate();
            isBlinking = false;
        }
    }

    @Override
    public void updateTransparent() {
        setAlpha(1.0f);
    }

    @Override
    public void setBg() {
        setAlpha(startAlpha / 256f);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int alphaValue = (int) valueAnimator.getAnimatedValue();
        setAlpha(alphaValue / 256f);
    }
}
