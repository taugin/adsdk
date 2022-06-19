package com.hauyu.adsdk.demo.view;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.hauyu.adsdk.demo.R;


public class ScanningAnimatorView extends View implements ValueAnimator.AnimatorUpdateListener {
    private Bitmap fgBitmap, bgBitmap;
    private int scanningLineWidth;
    private int scanningLineColor, coverColor;
    private boolean isStarted = false;
    private int scanningLineY;
    private Paint paint;
    private Path path;

    ValueAnimator scanningAnim;

    public ScanningAnimatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ScanningAnimatorView, 0, 0);
        try {
            Drawable fgDrawable = a.getDrawable(R.styleable.ScanningAnimatorView_fgImage);
            if (fgDrawable != null && fgDrawable.getCurrent() instanceof BitmapDrawable) {
                fgBitmap = ((BitmapDrawable) fgDrawable.getCurrent()).getBitmap();
            } else {
                fgBitmap = null;
            }

            Drawable bgDrawable = a.getDrawable(R.styleable.ScanningAnimatorView_bgImage);
            if (bgDrawable != null && bgDrawable.getCurrent() instanceof BitmapDrawable) {
                bgBitmap = ((BitmapDrawable) bgDrawable.getCurrent()).getBitmap();
            } else {
                bgBitmap = null;
            }
            scanningLineWidth = a.getDimensionPixelSize(R.styleable.ScanningAnimatorView_saLineWidth,
                    2);
            scanningLineColor = a.getColor(R.styleable.ScanningAnimatorView_saLineColor, Color.WHITE);
            coverColor = a.getColor(R.styleable.ScanningAnimatorView_coverColor,
                    Color.argb(31, 255, 255, 255));
        } finally {
            a.recycle();
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        path = new Path();
    }

    public void startAnim() {
        if (!isStarted) {
            int height = getHeight();
            scanningAnim = ValueAnimator.ofObject(new IntEvaluator(), height - scanningLineWidth / 2,
                    scanningLineWidth / 2);
            scanningAnim.setRepeatMode(ValueAnimator.REVERSE);
            scanningAnim.setRepeatCount(ValueAnimator.INFINITE);
            scanningAnim.setDuration(1200); // milliseconds
            scanningAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            scanningAnim.addUpdateListener(this);
            scanningAnim.start();
            isStarted = true;
        }
    }

    public void stopAnim() {
        if (isStarted) {
            scanningAnim.removeUpdateListener(this);
            scanningAnim.end();
            postDrawView();
            isStarted = false;
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        scanningLineY = (Integer) valueAnimator.getAnimatedValue();
        postDrawView();
    }

    private void postDrawView() {
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawView(canvas);
    }

    private void drawView(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int imgHeight = fgBitmap.getHeight();
        int imgWidth = fgBitmap.getWidth();
        int halfDeltaH = (height - imgHeight) / 2;
        int halfDeltaW = (width - imgWidth) / 2;

        paint.setColor(coverColor);
        canvas.drawRect(0, scanningLineY, width, height, paint);
        paint.setColor(scanningLineColor);
        canvas.drawLine(0, scanningLineY, width, scanningLineY, paint);

        // draw foreground image
        if (scanningLineY < halfDeltaH) {
            canvas.drawBitmap(fgBitmap, halfDeltaW, halfDeltaH, null);
        } else if ((scanningLineY <= (height - halfDeltaH)) && (scanningLineY >= halfDeltaH)) {
            canvas.save();
            path.reset();
            path.moveTo(halfDeltaW, halfDeltaH);
            path.lineTo(halfDeltaW + imgWidth, halfDeltaH);
            path.lineTo(halfDeltaW + imgWidth, scanningLineY);
            path.lineTo(halfDeltaW, scanningLineY);
            path.close();
            canvas.clipPath(path);
            canvas.drawBitmap(bgBitmap, halfDeltaW, halfDeltaH, null);
            canvas.restore();

            canvas.save();
            path.reset();
            path.moveTo(halfDeltaH, scanningLineY);
            path.lineTo(halfDeltaW + imgWidth, scanningLineY);
            path.lineTo(halfDeltaW + imgWidth, height - halfDeltaH);
            path.lineTo(halfDeltaW, height - halfDeltaH);
            path.close();
            canvas.clipPath(path);
            canvas.drawBitmap(fgBitmap, halfDeltaW, halfDeltaH, null);
            canvas.restore();
        } else {
            canvas.drawBitmap(bgBitmap, halfDeltaW, halfDeltaH, null);
        }

    }
}