package com.rabbit.adsdk.core.framework;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class BounceRateManager implements ActivityMonitor.OnAppMonitorCallback {

    private static BounceRateManager sBounceRateManager;

    public static BounceRateManager get(Context context) {
        synchronized (BounceRateManager.class) {
            if (sBounceRateManager == null) {
                createInstance(context);
            }
        }
        return sBounceRateManager;
    }

    private static void createInstance(Context context) {
        synchronized (BounceRateManager.class) {
            if (sBounceRateManager == null) {
                sBounceRateManager = new BounceRateManager(context);
            }
        }
    }

    private Context mContext;
    private Map<String, Object> mExtra;
    private long mAdClickTime = 0;
    private volatile boolean mAdClick = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BounceRateManager(Context context) {
        mContext = context;
    }

    public void init() {
        ActivityMonitor.get(mContext).setOnAppMonitorCallback(this);
    }

    public void onAdClick(Map<String, Object> extra) {
        if (isInvalidDurationCheckEnable()) {
            mExtra = extra;
            Log.iv(Log.TAG, "App ad was clicked.");
            mAdClick = true;
            mHandler.removeCallbacks(mResetRunnable);
            mHandler.postDelayed(mResetRunnable, getResetTime());
        }
    }

    private long getResetTime() {
        return getInvalidDuration() + 1000;
    }

    private Runnable mResetRunnable = new Runnable() {
        @Override
        public void run() {
            mAdClick = false;
            mAdClickTime = 0;
            mExtra = null;
        }
    };

    @Override
    public void onForeground(boolean fromBackground, WeakReference<Activity> weakReference) {
        String topClass = "";
        if (weakReference != null) {
            Activity activity = weakReference.get();
            if (activity != null) {
                topClass = " : " + activity.getClass();
            }
        }
        Log.iv(Log.TAG, "App is still foreground." + topClass);
        if (isInvalidDurationCheckEnable()) {
            if (mAdClick) {
                if (mAdClickTime > 0) {
                    long now = System.currentTimeMillis();
                    long adDuration = now - mAdClickTime;
                    long invalidDuration = getInvalidDuration();
                    boolean invalidAdClick = adDuration < invalidDuration;
                    Log.iv(Log.TAG, "ad duration time : " + adDuration + " , invalidAdClick : " + invalidAdClick);
                    mAdClickTime = 0;
                    if (invalidAdClick) {
                        if (mExtra != null) {
                            mExtra.put("ivtime", adDuration);
                        }
                        EventImpl.get().reportEvent(mContext, "e_ad_ivclk", null, mExtra);
                        Log.iv(Log.TAG, "report invalid ad click : " + mExtra);
                        mExtra = null;
                    }
                } else {
                    mAdClickTime = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public void onBackground() {
        Log.iv(Log.TAG, "App went background.");
        if (isInvalidDurationCheckEnable()) {
            if (mAdClick) {
                mAdClickTime = System.currentTimeMillis();
            }
        }
    }

    private long getInvalidDuration() {
        long invalidDuration = 2500;
        String durationString = DataManager.get(mContext).getString("invalid_traffic_check_duration");
        if (!TextUtils.isEmpty(durationString)) {
            try {
                invalidDuration = Long.parseLong(durationString);
            } catch (Exception e) {
            }
        }
        return invalidDuration;
    }

    private boolean isInvalidDurationCheckEnable() {
        boolean ivtCheckEnable = true;
        String checkEnable = DataManager.get(mContext).getString("invalid_traffic_check_enable");
        if (!TextUtils.isEmpty(checkEnable)) {
            try {
                ivtCheckEnable = Boolean.parseBoolean(checkEnable);
            } catch (Exception e) {
            }
        }
        return ivtCheckEnable;
    }
}
