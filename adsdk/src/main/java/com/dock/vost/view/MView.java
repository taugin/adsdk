package com.dock.vost.view;

/**
 * Created by Administrator on 2019-12-20.
 */

import android.content.Context;
import android.view.View;

/**
 * 监听Banner或native是否可见的类
 */
public class MView extends View {

    private boolean mViewDetached = false;
    private boolean mViewVisible = true;

    public MView(Context context) {
        super(context);
    }

    public boolean isVisible() {
        return !mViewDetached && mViewVisible;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mViewVisible = visibility == View.VISIBLE;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewDetached = true;
    }
}