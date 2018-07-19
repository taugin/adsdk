package com.inner.adsdk.framework;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Administrator on 2018/7/19.
 */

public class AdFilmLoader implements Handler.Callback {

    private static final int MSG_LOOP = 1;
    private static final int LOOP_DELAY = 300;

    private static AdFilmLoader sAdFilmLoader;

    public static AdFilmLoader get(Context context) {
        if (sAdFilmLoader == null) {
            create(context);
        }
        return sAdFilmLoader;
    }

    private static void create(Context context) {
        synchronized (AdFilmLoader.class) {
            if (sAdFilmLoader == null) {
                sAdFilmLoader = new AdFilmLoader(context);
            }
        }
    }

    private Handler mThreadHandler;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;
    private HandlerThread mHandlerThread = null;

    private AdFilmLoader(Context context) {
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
        return false;
    }

    private void sendHandleMessageInternal() {
        if (mThreadHandler != null) {
            mThreadHandler.removeMessages(MSG_LOOP);
            mThreadHandler.sendEmptyMessageDelayed(MSG_LOOP, LOOP_DELAY);
        }
    }

    public void sendHandleMessage() {
        ensureThreadHandler();
        sendHandleMessageInternal();
    }

    public void cancelHandleMessage() {
        if (mThreadHandler != null) {
            mThreadHandler.removeMessages(MSG_LOOP);
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    public void init() {
        sendHandleMessage();
    }
}
