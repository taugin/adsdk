package com.inner.adsdk.policy;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.facebook.ads.AudienceNetworkActivity;
import com.google.android.gms.ads.AdActivity;
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

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Activity mActivity;
    private List<View> mHandleView = new ArrayList<View>();

    public void checkCTR(Activity activity, PidConfig pidConfig) {
        mHandleView.clear();
        if (pidConfig == null || activity == null) {
            Log.d(Log.TAG, "activity == null or pidConfig == null");
            return;
        }
        if (!Constant.TYPE_INTERSTITIAL.equals(pidConfig.getAdType()) &&
                !Constant.TYPE_REWARD.equals(pidConfig.getAdType())) {
            Log.d(Log.TAG, "neither " + Constant.TYPE_INTERSTITIAL + " nor " + Constant.TYPE_REWARD);
            return;
        }
        if (pidConfig.getCtr() <= 0 || pidConfig.getCtr() >= 100) {
            Log.d(Log.TAG, "ctr less than 0 or great than 100");
            return;
        }
        if (pidConfig.isFB()) {
            checkFacebookCTR(activity, pidConfig);
        } else if (pidConfig.isAdmob() || pidConfig.isAdx() || pidConfig.isDfp()) {
            checkGoogleCTR(activity, pidConfig);
        }
    }

    private void checkFacebookCTR(final Activity activity, PidConfig pidConfig) {
        Log.d(Log.TAG, "");
        if (activity.getClass() != AudienceNetworkActivity.class) {
            Log.d(Log.TAG, "not facebook activity");
            return;
        }
        mActivity = activity;
        boolean allowClick = percentRandomBoolean(pidConfig != null ? pidConfig.getCtr() : 0);
        Log.d(Log.TAG, "allow click : " + allowClick + " , pidName : " + pidConfig.getAdPlaceName());
        if (!allowClick) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 1000);
        }
    }

    private void checkGoogleCTR(final Activity activity, PidConfig pidConfig) {
        Log.d(Log.TAG, "");
        if (activity.getClass() != AdActivity.class) {
            Log.d(Log.TAG, "not google activity");
            return;
        }
        mActivity = activity;
        boolean allowClick = percentRandomBoolean(pidConfig != null ? pidConfig.getCtr() : 0);
        Log.d(Log.TAG, "allow click : " + allowClick + " , pidName : " + pidConfig.getAdPlaceName());
        if (!allowClick) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 1000);
        }
    }

    public static boolean percentRandomBoolean(int percent) {
        if (percent <= 0 || percent > 100) return false;
        int randomVal = new Random().nextInt(100);
        return randomVal <= percent;
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
            }
        } else {
            Log.d(Log.TAG, "mActivity == null");
        }
    }

    /**
     * 设置按钮禁止点击
     *
     * @param view
     */
    private void handleViewCtr(final View view) {
        if (mActivity == null || view == null) {
            Log.d(Log.TAG, "mActivity == null or view == null");
            return;
        }
        final GestureDetector gestureDetector = new GestureDetector(mActivity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                restoreViewClick();
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                restoreViewClick();
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
     * 恢复被禁止点击的view
     */
    private void restoreViewClick() {
        if (mHandleView != null && !mHandleView.isEmpty()) {
            for (View v : mHandleView) {
                Log.d(Log.TAG, "v : " + v);
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
