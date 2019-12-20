package com.dock.vost.view;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.dock.vost.moon.IAdvance;
import com.dock.vost.moon.R;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2019-12-20.
 */

public class BlinkImageView extends AppCompatImageView implements ValueAnimator.AnimatorUpdateListener, IAdvance.Blank {
    private int startAlpha, endAlpha, duration;
    private boolean isBlinking = false;

    ValueAnimator colorAnimation;

    public BlinkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BlinkImageView, 0, 0);
        try {
            startAlpha = a.getInteger(R.styleable.BlinkImageView_startAlpha, 63);
            endAlpha = a.getInteger(R.styleable.BlinkImageView_endAlpha, 255);
            duration = a.getInteger(R.styleable.BlinkImageView_blinkDuration, 800);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        } finally {
            a.recycle();
        }
        setAlpha(startAlpha / 256f);
    }

    @Override
    public void startBlink() {
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
    public void stopBlink() {
        if (isBlinking) {
            colorAnimation.removeUpdateListener(this);
            colorAnimation.end();
            postInvalidate();
            isBlinking = false;
        }
    }

    @Override
    public void solid() {
        setAlpha(1.0f);
    }

    @Override
    public void halftrans() {
        setAlpha(startAlpha / 256f);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int alphaValue = (int) valueAnimator.getAnimatedValue();
        setAlpha(alphaValue / 256f);
    }
}
