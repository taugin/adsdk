package com.hauyu.adsdk.core;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/7/18.
 */

public class CtrChecker implements Runnable {

    private static final int MIN_CTR_VALUE = 1;
    private static final int MAX_CTR_VALUE = 100;
    private static final int DELAY_HANDLE_CTR = 1000;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Activity mActivity;
    private List<View> mHandleView = new ArrayList<View>();
    private Random mRandom = new Random(System.currentTimeMillis());
    private AttrChecker mAttrChecker = new AttrChecker();
    private PidConfig mPidConfig;

    public void checkCTR(Activity activity, PidConfig pidConfig) {
        if (mHandleView != null) {
            mHandleView.clear();
        }
        if (pidConfig == null || activity == null) {
            Log.v(Log.TAG, "activity == null or pidConfig == null");
            return;
        }
        if (!Constant.TYPE_INTERSTITIAL.equals(pidConfig.getAdType()) &&
                !Constant.TYPE_REWARD.equals(pidConfig.getAdType())) {
            Log.v(Log.TAG, "neither " + Constant.TYPE_INTERSTITIAL + " nor " + Constant.TYPE_REWARD);
            return;
        }
        if (mAttrChecker != null) {
            mAttrChecker.setContext(activity);
        }
        handleControlCTR(activity, pidConfig);
    }

    private void handleControlCTR(final Activity activity, PidConfig pidConfig) {
        Log.v(Log.TAG, "handle control ctr");
        mActivity = activity;
        mPidConfig = pidConfig;
        boolean needControlCTR = needControlCTR(pidConfig != null ? pidConfig.getCtr() : 0);
        long delayClickTime = pidConfig != null ? pidConfig.getDelayClickTime() : 0;
        Log.v(Log.TAG, "pidname : " + pidConfig.getAdPlaceName() + " , ncc : " + needControlCTR + " , ffc : " + pidConfig.isFinishForCtr() + " , dct : " + delayClickTime);
        if (mHandler != null && (needControlCTR || delayClickTime > 0)) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_HANDLE_CTR);
        }
    }

    /**
     * 判断是否需要控制CTR
     *
     * @param ctr
     * @return true if need control ctr otherwise false
     */
    public boolean needControlCTR(int ctr) {
        Log.v(Log.TAG, "need control by ctr : " + ctr);
        try {
            if (ctr < MIN_CTR_VALUE || ctr > MAX_CTR_VALUE) return false;
            int randomVal = mRandom.nextInt(MAX_CTR_VALUE);
            return randomVal < ctr;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return false;
    }

    @Override
    public void run() {
        if (mActivity != null) {
            try {
                List<View> allViews = getAllChildViews(mActivity.getWindow().getDecorView());
                long delayClickTime = 0;
                if (mPidConfig != null) {
                    delayClickTime = mPidConfig.getDelayClickTime();
                }
                if (delayClickTime > 0) {
                    interceptClickDelayTime(allViews, delayClickTime);
                } else {
                    interceptClickForCtr(allViews);
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        } else {
            Log.v(Log.TAG, "mActivity == null");
        }
    }

    private void interceptClickForCtr(List<View> allViews) {
        if (allViews != null && !allViews.isEmpty()) {
            for (View view : allViews) {
                if (view instanceof WebView || (view != null && view.isClickable())) {
                    if (mHandleView != null) {
                        mHandleView.add(view);
                    }
                    handleViewCtr(view);
                }
            }
        }
    }

    private void interceptClickDelayTime(List<View> allViews, long delayClickTime) {
        if (allViews != null && !allViews.isEmpty()) {
            for (View view : allViews) {
                if (view instanceof WebView || (view != null && view.isClickable())) {
                    if (mHandleView != null) {
                        mHandleView.add(view);
                    }
                    handleViewDelayTime(view);
                }
            }
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restoreViewClick();
                    }
                }, delayClickTime);
            }
        }
    }

    /**
     * 设置按钮禁止点击
     *
     * @param view
     */
    private void handleViewCtr(final View view) {
        if (mActivity == null || view == null) {
            Log.v(Log.TAG, "mActivity == null or view == null");
            return;
        }
        final GestureDetector gestureDetector = new GestureDetector(mActivity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mPidConfig != null && mPidConfig.isFinishForCtr()) {
                    finishAdActivity();
                } else {
                    restoreViewClick();
                }
                return super.onSingleTapUp(e);
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * 设置按钮禁止点击
     *
     * @param view
     */
    private void handleViewDelayTime(final View view) {
        if (mActivity == null || view == null) {
            Log.v(Log.TAG, "mActivity == null or view == null");
            return;
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void finishAdActivity() {
        Log.d(Log.TAG, "");
        try {
            if (mActivity != null) {
                mActivity.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    /**
     * 恢复被禁止点击的view
     */
    private void restoreViewClick() {
        if (mHandleView != null && !mHandleView.isEmpty()) {
            for (View v : mHandleView) {
                v.setOnTouchListener(null);
            }
        }
    }

    /**
     * 获取所有的View
     *
     * @param view
     * @return
     */
    private List<View> getAllChildViews(View view) {
        List<View> allViews = new ArrayList<View>();
        try {
            if (view instanceof ViewGroup) {
                ViewGroup vp = (ViewGroup) view;
                for (int i = 0; i < vp.getChildCount(); i++) {
                    View child = vp.getChildAt(i);
                    allViews.add(child);
                    allViews.addAll(getAllChildViews(child));
                }
            }
        } catch (Exception e) {
        }
        return allViews;
    }

    protected void logv(String msg) {
        Log.v(Log.TAG, msg);
    }
}
