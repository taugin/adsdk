package com.inner.adsdk.framework;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.inner.adsdk.utils.TaskUtils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class TaskMonitor implements Handler.Callback {
    private static final int MSG_LOOP = 1;
    private static final int LOOP_DELAY = 500;

    private static TaskMonitor sTaskMonitor;

    public static TaskMonitor get(Context context) {
        if (sTaskMonitor == null) {
            create(context);
        }
        return sTaskMonitor;
    }

    private static void create(Context context) {
        synchronized (TaskMonitor.class) {
            if (sTaskMonitor == null) {
                sTaskMonitor = new TaskMonitor(context);
            }
        }
    }

    private Handler mThreadHandler;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;
    private HandlerThread mHandlerThread = null;
    private ComponentName mLastCmp;

    private TaskMonitor(Context context) {
        mContext = context.getApplicationContext();
        ensureThreadHandler();
    }

    private void ensureThreadHandler() {
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("app_monitor");
            mHandlerThread.start();
            mThreadHandler = new Handler(mHandlerThread.getLooper(), this);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        sendHandleMessageInternal();
        processInternal();
        return false;
    }

    private void sendHandleMessageInternal() {
        if (mThreadHandler != null) {
            mThreadHandler.removeMessages(MSG_LOOP);
            mThreadHandler.sendEmptyMessageDelayed(MSG_LOOP, LOOP_DELAY);
        }
    }

    private void sendHandleMessage() {
        ensureThreadHandler();
        sendHandleMessageInternal();
    }

    private void cancelHandleMessage() {
        if (mThreadHandler != null) {
            mThreadHandler.removeMessages(MSG_LOOP);
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    private void processInternal() {
        ComponentName cmp = TaskUtils.queryTopActivity(mContext);
        if (cmp != null) {
            if (mLastCmp != null && !TextUtils.equals(mLastCmp.getPackageName(), cmp.getPackageName())) {
                onAppSwitchInMainThread(cmp.getPackageName(), cmp.getClassName());
            } else if (mLastCmp != null
                    && TextUtils.equals(mLastCmp.getPackageName(), cmp.getPackageName())
                    && !TextUtils.equals(mLastCmp.getClassName(), cmp.getClassName())) {
                onActivitySwitchInMainThread(mLastCmp.getPackageName(), mLastCmp.getClassName(), cmp.getClassName());
            }
            if (!cmp.equals(mLastCmp)) {
                mLastCmp = cmp;
            }
        }
    }

    private void onAppSwitchInMainThread(final String pkgname, final String className) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnTaskMonitorListener != null) {
                        mOnTaskMonitorListener.onAppSwitch(pkgname, className);
                    }
                }
            });
        }
    }

    private void onActivitySwitchInMainThread(final String pkgname, final String oldActivity, final String newActivity) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnTaskMonitorListener != null) {
                        mOnTaskMonitorListener.onActivitySwitch(pkgname, oldActivity, newActivity);
                    }
                }
            });
        }
    }

    /**
     * 开启Monitor
     */
    public void startMonitor() {
        sendHandleMessage();
    }

    /**
     * 关闭Monitor
     */
    public void stopMonitor() {
        cancelHandleMessage();
    }

    private OnTaskMonitorListener mOnTaskMonitorListener;

    public void setOnTaskMonitorListener(OnTaskMonitorListener l) {
        mOnTaskMonitorListener = l;
    }

    public interface OnTaskMonitorListener {
        void onAppSwitch(String pkgname, String className);

        void onActivitySwitch(String pkgname, String oldActivity, String newActivity);
    }
}
