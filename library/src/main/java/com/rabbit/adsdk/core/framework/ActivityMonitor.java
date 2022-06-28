package com.rabbit.adsdk.core.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.InternalStat;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/2/12.
 */

public class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    private static ActivityMonitor sActivityMonitor;
    private AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private WeakReference<Activity> mTopActivity = null;

    public static ActivityMonitor get(Context context) {
        synchronized (ActivityMonitor.class) {
            if (sActivityMonitor == null) {
                createInstance(context);
            }
        }
        return sActivityMonitor;
    }

    private static void createInstance(Context context) {
        synchronized (ActivityMonitor.class) {
            if (sActivityMonitor == null) {
                sActivityMonitor = new ActivityMonitor(context);
            }
        }
    }

    private ActivityMonitor(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    private Context mContext;

    public void init() {
        try {
            if (mContext instanceof Application) {
                ((Application) mContext).unregisterActivityLifecycleCallbacks(this);
                ((Application) mContext).registerActivityLifecycleCallbacks(this);
            }
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (mAtomicInteger != null) {
            mAtomicInteger.incrementAndGet();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mTopActivity = new WeakReference<Activity>(activity);
        onResumed();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        onPaused();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mAtomicInteger != null) {
            mAtomicInteger.decrementAndGet();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public boolean appOnTop() {
        if (mAtomicInteger != null) {
            return mAtomicInteger.get() > 0;
        }
        return false;
    }

    private boolean isLauncherActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Intent intent = activity.getIntent();
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        Set<String> categories = intent.getCategories();
        if (TextUtils.isEmpty(action) || categories == null || categories.isEmpty()) {
            return false;
        }
        return TextUtils.equals(action, Intent.ACTION_MAIN)
                && categories.contains(Intent.CATEGORY_LAUNCHER);
    }

    public Activity getTopActivity() {
        if (mTopActivity != null) {
            return mTopActivity.get();
        }
        return null;
    }

    private OnAppMonitorCallback mOnAppMonitorCallback;

    public void setOnAppMonitorCallback(OnAppMonitorCallback callback) {
        mOnAppMonitorCallback = callback;
    }

    public interface OnAppMonitorCallback {
        void onForeground(boolean fromBackground, WeakReference<Activity> activityWeakReference);

        void onBackground();
    }

    private final Object mLockObject = new Object();
    private boolean mResumed = true;
    private boolean mPaused = false;
    private Runnable mCheckRunnable;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ForegroundRunnable mForegroundRunnable = new ForegroundRunnable();
    private AtomicBoolean mFromBackground = new AtomicBoolean(true);

    void onPaused() {
        this.mPaused = true;
        if (mCheckRunnable != null) {
            mHandler.removeCallbacks(mCheckRunnable);
        }
        mCheckRunnable = new CheckRunnable(this);
        mHandler.postDelayed(mCheckRunnable, getDelayTime());
    }

    void onResumed() {
        this.mPaused = false;
        boolean isFromBackground = !mResumed;
        this.mResumed = true;
        if (mCheckRunnable != null) {
            mHandler.removeCallbacks(mCheckRunnable);
        }
        synchronized (this.mLockObject) {
            if (mResumed) {
                // Log.v(Log.TAG, "App is still foreground.");
                mHandler.removeCallbacks(mForegroundRunnable);
                mForegroundRunnable.setFromBackground(isFromBackground);
                mHandler.postDelayed(mForegroundRunnable, getDelayTime());
            }
        }
    }

    final class ForegroundRunnable implements Runnable {
        private boolean mFromBackground = false;

        public void setFromBackground(boolean fromBackground) {
            mFromBackground = fromBackground;
        }

        @Override
        public void run() {
            if (mOnAppMonitorCallback != null) {
                mOnAppMonitorCallback.onForeground(mFromBackground, mTopActivity);
            }
            reportAppStart();
        }
    }

    final class CheckRunnable implements Runnable {
        final ActivityMonitor mActivityMonitor;

        CheckRunnable(ActivityMonitor monitor) {
            this.mActivityMonitor = monitor;
        }

        public final void run() {
            synchronized (this.mActivityMonitor.mLockObject) {
                if (!(this.mActivityMonitor.mResumed) || !(this.mActivityMonitor.mPaused)) {
                    if (mOnAppMonitorCallback != null) {
                        mOnAppMonitorCallback.onForeground(false, mTopActivity);
                    }
                    reportAppStart();
                } else {
                    setBackgroundFlag();
                    this.mActivityMonitor.mResumed = false;
                    if (mOnAppMonitorCallback != null) {
                        mOnAppMonitorCallback.onBackground();
                    }
                }
            }
        }
    }

    private long getDelayTime() {
        long delayTime = 500;
        String delayTimeString = DataManager.get(mContext).getString("activity_monitor_delay_time");
        if (!TextUtils.isEmpty(delayTimeString)) {
            try {
                delayTime = Long.parseLong(delayTimeString);
            } catch (Exception e) {
            }
        }
        return delayTime;
    }

    private void setBackgroundFlag() {
        if (mFromBackground != null) {
            mFromBackground.set(true);
        }
    }

    /**
     * 从后台切换前台时，上报e_app_start事件
     */
    private void reportAppStart() {
        if (mFromBackground != null && mFromBackground.getAndSet(false)) {
            InternalStat.reportEvent(mContext, "e_app_start");
        }
    }
}
