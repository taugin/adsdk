package com.earch.sunny.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by Administrator on 2019-12-20.
 */

public class ScrollLayout extends FrameLayout {

    private static final int UNKNOWN = 0;
    private static final int VERTICAL = 1;
    private static final int HORIZONTAL = 2;
    private GestureDetector mGestureDetector1;
    private GestureDetector mGestureDetector2;
    private float mDownY;
    private float mDownX;
    private Rect mViewRect = new Rect();
    private Rect mClipRect = new Rect();
    private Scroller mScroller;
    private boolean mOpened = false;
    private int mMinSlop = 0;
    private int mOrientation = UNKNOWN;

    public ScrollLayout(Context context) {
        super(context);
        init();
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGestureDetector1 = new GestureDetector(getContext(), new GestureListener(false));
        mGestureDetector2 = new GestureDetector(getContext(), new GestureListener(true));
        mScroller = new Scroller(getContext());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mViewRect.set(0, 0, dm.widthPixels, dm.heightPixels);
        mClipRect.set(mViewRect);
        mMinSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalArgumentException("max child is 1");
        }
        super.addView(child, index, params);
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset()) {
            if (mOrientation == VERTICAL) {
                float dy = mScroller.getCurrY() - mClipRect.bottom;
                updateVertical(dy, true);
            } else if (mOrientation == HORIZONTAL) {
                float dx = mScroller.getCurrX() - mClipRect.left;
                updateHorizontal(dx, true);
            }
        } else {
            if (mOpened && mScroller != null && mScroller.isFinished()) {
                mOpened = false;
                mOrientation = UNKNOWN;
                openScreen();
            }
        }
    }

    private void openScreen() {
        if (mOnScreenListener != null) {
            mOnScreenListener.onScreenUnlocked();
        }
    }

    private void updateVertical(float deltaY, boolean forceUpdate) {
        if (mClipRect.bottom + deltaY < mViewRect.bottom) {
            mClipRect.bottom += deltaY;
        } else if (mScroller != null && mScroller.isFinished()) {
            mClipRect.bottom = mViewRect.bottom;
        }
        if (Math.abs(mClipRect.bottom - mViewRect.bottom) > mMinSlop || forceUpdate) {
            if (getChildAt(0) != null) {
                getChildAt(0).setTranslationY(mClipRect.bottom - mViewRect.bottom);
                invalidate();
            }
        }
        if (mScroller != null && mScroller.isFinished() && Math.abs(mViewRect.bottom - mClipRect.bottom) <= 0) {
            mOrientation = UNKNOWN;
        }
    }

    private void updateHorizontal(float deltaX, boolean forceUpdate) {
        if (mClipRect.left + deltaX > mViewRect.left) {
            mClipRect.left += deltaX;
        } else if (mScroller != null && mScroller.isFinished()) {
            mClipRect.left = mViewRect.left;
        }
        if (Math.abs(mClipRect.left - mViewRect.left) > mMinSlop || forceUpdate) {
            if (getChildAt(0) != null) {
                getChildAt(0).setTranslationX(mClipRect.left - mViewRect.left);
                invalidate();
            }
        }
        if (mScroller != null && mScroller.isFinished() && Math.abs(mClipRect.left - mViewRect.left) <= 0) {
            mOrientation = UNKNOWN;
        }
    }

    private void resetByAnimateVertical(boolean forceOpen) {
        int dy;
        int duration;
        if (mClipRect.bottom > mViewRect.height() / 2 && !forceOpen) {
            dy = mViewRect.bottom - mClipRect.bottom;
            mOpened = false;
            mScroller = new Scroller(getContext(),
                    new BounceInterpolator());
            duration = 1000;
        } else {
            dy = -mClipRect.bottom;
            mOpened = true;
            mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
            duration = 500;
        }
        mScroller.startScroll(0, mClipRect.bottom, 0, dy, duration);
        invalidate();
    }

    private void resetByAnimateHorizontal(boolean forceOpen) {
        int dx;
        int duration;
        if (mClipRect.left < mViewRect.width() / 2 && !forceOpen) {
            dx = mViewRect.left - mClipRect.left;
            mOpened = false;
            mScroller = new Scroller(getContext(),
                    new BounceInterpolator());
            duration = 1000;
        } else {
            dx = mClipRect.left;
            mOpened = true;
            mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
            duration = 500;
        }
        mScroller.startScroll(mClipRect.left, 0, dx, 0, duration);
        invalidate();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mGestureDetector1.onTouchEvent(event)) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mOrientation == VERTICAL) {
                if (mViewRect.bottom != mClipRect.bottom) {
                    resetByAnimateVertical(false);
                }
            } else if (mOrientation == HORIZONTAL) {
                if (mViewRect.left != mClipRect.left) {
                    resetByAnimateHorizontal(false);
                }
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector2.onTouchEvent(event)) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mOrientation == VERTICAL) {
                if (mViewRect.bottom != mClipRect.bottom) {
                    resetByAnimateVertical(false);
                }
            } else if (mOrientation == HORIZONTAL) {
                if (mViewRect.left != mClipRect.left) {
                    resetByAnimateHorizontal(false);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mInterceptDownEvent;

        public GestureListener(boolean interceptDownEvent) {
            mInterceptDownEvent = interceptDownEvent;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mDownX = e.getX(0);
            mDownY = e.getY(0);
            if (mScroller != null) {
                mScroller.abortAnimation();
            }
            return mInterceptDownEvent;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            float x1 = 0f;
            float y1 = 0f;
            float x2 = 0f;
            float y2 = 0f;
            try {
                x1 = e1.getX(0);
                x2 = e2.getX(0);

                y1 = e1.getY(0);
                y2 = e2.getY(0);
            } catch (Exception e) {
            }
            if (mOrientation == UNKNOWN) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    mOrientation = HORIZONTAL;
                } else {
                    mOrientation = VERTICAL;
                }
            }
            if (mOrientation == VERTICAL) {
                if (mScroller != null) {
                    mScroller.fling(0, mClipRect.bottom, 0, (int) (velocityY / 2.5f),
                            0, 0, 0, mClipRect.bottom);
                    mClipRect.bottom = mScroller.getFinalY();
                    if (velocityY > 0 && mClipRect.bottom < getHeight() / 2) {
                        mClipRect.bottom = getHeight() / 2 + 10;
                    }
                    resetByAnimateVertical(velocityY < 0 && mScroller.getFinalY() < getHeight() / 2);
                }
                return true;
            } else if (mOrientation == HORIZONTAL) {
                if (mScroller != null) {
                    mScroller.fling(mClipRect.left, 0, (int) (velocityX / 2.5f), 0,
                            0, mClipRect.right, 0, 0);
                    mClipRect.left = mScroller.getFinalX();
                    if (velocityX < 0 && mClipRect.left > getWidth() / 2) {
                        mClipRect.left = getWidth() / 2 - 10;
                    }
                    resetByAnimateHorizontal(velocityX > 0 && mScroller.getFinalX() > getWidth() * 2 / 3);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            float x1 = 0f;
            float y1 = 0f;
            float x2 = 0f;
            float y2 = 0f;
            try {
                x1 = e1.getX(0);
                x2 = e2.getX(0);

                y1 = e1.getY(0);
                y2 = e2.getY(0);
            } catch (Exception e) {
            }
            if (mOrientation == UNKNOWN && Math.abs(x1 - x2) > mMinSlop) {
                mOrientation = HORIZONTAL;
            } else if (mOrientation == UNKNOWN && Math.abs(y1 - y2) > mMinSlop) {
                mOrientation = VERTICAL;
            }
            if (mOrientation == VERTICAL) {
                float y = e2.getY(0);
                float deltaY = y - mDownY;
                mDownY = y;
                updateVertical(deltaY, false);
                return true;
            } else if (mOrientation == HORIZONTAL) {
                float x = e2.getX(0);
                float deltaX = x - mDownX;
                mDownX = x;
                updateHorizontal(deltaX, false);
                return true;
            }
            return false;
        }
    }

    private OnScreenListener mOnScreenListener;

    public void setOnScreenListener(OnScreenListener l) {
        mOnScreenListener = l;
    }

    public interface OnScreenListener {
        void onScreenUnlocked();
    }
}