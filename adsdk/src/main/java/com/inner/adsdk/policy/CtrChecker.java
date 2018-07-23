package com.inner.adsdk.policy;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;

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
            if (!mAttrChecker.isAttributionAllow(pidConfig.getAttrList())) {
                Log.v(Log.TAG, "attr not allow");
                return;
            }
            if (!mAttrChecker.isMediaSourceAllow(pidConfig.getMediaList())) {
                Log.v(Log.TAG, "media source not allow");
                return;
            }
            if (!mAttrChecker.isCountryAllow(pidConfig.getCountryList())) {
                Log.v(Log.TAG, "country list not allow");
                return;
            }
        }
        if (pidConfig.getCtr() < MIN_CTR_VALUE || pidConfig.getCtr() > MAX_CTR_VALUE) {
            return;
        }
        handleClickConfirm(activity, pidConfig);
    }

    private void handleClickConfirm(final Activity activity, PidConfig pidConfig) {
        Log.v(Log.TAG, "handle click confirm");
        mActivity = activity;
        mPidConfig = pidConfig;
        boolean needClickConfirm = needClickConfirmByCtr(pidConfig != null ? pidConfig.getCtr() : 0);
        Log.v(Log.TAG, "need click confirm : " + needClickConfirm + " , pidname : " + pidConfig.getAdPlaceName());
        if (mHandler != null && needClickConfirm) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_HANDLE_CTR);
        }
    }

    /**
     * 判断是否需要点击确认
     *
     * @param ctr
     * @return true if need click confirm otherwise false
     */
    public boolean needClickConfirmByCtr(int ctr) {
        Log.v(Log.TAG, "need click confirm by ctr : " + ctr);
        try {
            if (ctr < MIN_CTR_VALUE || ctr > MAX_CTR_VALUE) return false;
            int randomVal = mRandom.nextInt(MAX_CTR_VALUE);
            return randomVal > ctr;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void run() {
        if (mActivity != null) {
            try {
                List<View> allViews = getAllChildViews(mActivity.getWindow().getDecorView());
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
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
        } else {
            Log.v(Log.TAG, "mActivity == null");
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

    private void finishAdActivity() {
        Log.d(Log.TAG, "");
        try {
            if (mActivity != null) {
                mActivity.finish();
            }
        } catch(Exception e) {
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
}
