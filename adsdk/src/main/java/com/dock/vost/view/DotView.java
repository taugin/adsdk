package com.dock.vost.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dock.vost.moon.IAdvance;
import com.dock.vost.moon.R;

/**
 * Created by Administrator on 2019-12-20.
 */

public class DotView extends View implements IAdvance.Dot{
    private Paint emptyPaint;
    private Paint filledPaint;
    private boolean init = false;
    private int step = -1;
    private float dotRadius;
    private float margin;
    private final int stepCount;

    public DotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.DotView, 0, 0);
        int baseColor, emptyAlpha, filledAlpha;
        try {
            emptyAlpha = a.getInteger(R.styleable.DotView_emptyAlpha, 63);
            filledAlpha = a.getInteger(R.styleable.DotView_filledAlpha, 159);
            stepCount = a.getInteger(R.styleable.DotView_stepCount, 3);
            baseColor = a.getColor(R.styleable.DotView_dotBaseColor, Color.WHITE);
            dotRadius = a.getDimensionPixelSize(R.styleable.DotView_dotRadius,
                    (int) (getResources().getDisplayMetrics().density * 3));
        } finally {
            a.recycle();
        }
        int baseColorR = Color.red(baseColor);
        int baseColorG = Color.green(baseColor);
        int baseColorB = Color.blue(baseColor);
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setARGB(emptyAlpha, baseColorR, baseColorG, baseColorB);
        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint.setARGB(filledAlpha, baseColorR, baseColorG, baseColorB);
    }

    @Override
    public void setDotLength(int nextStep) {
        if (nextStep >= stepCount || nextStep < -1) {
            return;
        }
        this.step = nextStep;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!init) {
            init = true;
            margin = (canvas.getWidth() - stepCount * dotRadius * 2) / (stepCount + 1);
        }
        for (int index = 0; index < stepCount; ++index) {
            canvas.drawCircle(margin + dotRadius + index * (margin + 2 * dotRadius), canvas.getHeight() / 2,
                    dotRadius, index <= step ? filledPaint : emptyPaint);
        }
    }
}


