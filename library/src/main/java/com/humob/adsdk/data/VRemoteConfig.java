package com.humob.adsdk.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.humob.adsdk.utils.Utils;
import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.log.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/2/12.
 */

public class VRemoteConfig implements Handler.Callback {

    private static final int CACHE_EXPIRE_TIME = 15 * 60;
    private static final int REFRESH_INTERVAL = CACHE_EXPIRE_TIME * 1000;
    private static final String PREF_REFRESH_INTERVAL = "pref_refresh_interval";
    private static final String PREF_REMOTE_CONFIG_REQUEST_TIME = "pref_data_config_rtime";
    private static final SimpleDateFormat SDF_LEFT_TIME = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    private static final int MSG_UPDATE_REMOTE_CONFIG = 0x12345678;
    private static final int DELAY_UPDATE_REMOTE_CONFIG = 2000;
    private static List<String> REFRESH_INTERVALS;
    private Context mContext;
    private FirebaseRemoteConfig mInstance;
    private Random mRandom = new Random(System.currentTimeMillis());
    private Handler mHandler = new Handler(Looper.getMainLooper(), this);

    private static VRemoteConfig sVRemoteConfig;

    static {
        REFRESH_INTERVALS = new ArrayList<String>();
        REFRESH_INTERVALS.add("900000");
        REFRESH_INTERVALS.add("930000");
        REFRESH_INTERVALS.add("960000");
        REFRESH_INTERVALS.add("990000");
    }

    public static VRemoteConfig get(Context context) {
        synchronized (VRemoteConfig.class) {
            if (sVRemoteConfig == null) {
                createInstance(context);
            }
            if (sVRemoteConfig != null) {
                sVRemoteConfig.mContext = context;
            }
        }
        return sVRemoteConfig;
    }

    private static void createInstance(Context context) {
        synchronized (VRemoteConfig.class) {
            if (sVRemoteConfig == null) {
                sVRemoteConfig = new VRemoteConfig(context);
            }
        }
    }

    private VRemoteConfig(Context context) {
        mContext = context;
    }

    public void init() {
        updateRefreshInterval();
        ensureFirebase();
        updateRemoteConfig(true);
    }

    public void updateRemoteConfig(boolean init) {
        if (init) {
            requestInternal();
            return;
        }
        if (mHandler != null) {
            if (mHandler.hasMessages(MSG_UPDATE_REMOTE_CONFIG)) {
                mHandler.removeMessages(MSG_UPDATE_REMOTE_CONFIG);
            }
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_REMOTE_CONFIG, DELAY_UPDATE_REMOTE_CONFIG);
        } else {
            requestInternal();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == MSG_UPDATE_REMOTE_CONFIG) {
            requestInternal();
            return true;
        }
        return false;
    }

    private void requestInternal() {
        ensureFirebase();
        if (mInstance != null) {
            synchronized (mInstance) {
                if (isNeedRequest()) {
                    requestDataConfig();
                    activeFetchConfig();
                }
            }
        }
    }

    private boolean isNeedRequest() {
        long now = System.currentTimeMillis();
        long last = Utils.getLong(mContext, PREF_REMOTE_CONFIG_REQUEST_TIME);
        boolean needRequest = now - last > getRefreshInterval();
        long leftTime = getRefreshInterval() - (now - last);
        if (leftTime > 0) {
            SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
            Log.iv(Log.TAG, "next time : " + SDF_LEFT_TIME.format(new Date(leftTime)));
        }
        return needRequest;
    }

    private void requestDataConfig() {
        try {
            mInstance.fetch(CACHE_EXPIRE_TIME).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task == null) {
                        Log.iv(Log.TAG, "task == null");
                        return;
                    }
                    if (task.isSuccessful()) {
                        Log.iv(Log.TAG, "onComplete successfully");
                        if (mInstance != null) {
                            mInstance.fetchAndActivate();
                        }
                        Utils.putLong(mContext, Constant.PREF_REMOTE_CONFIG_UPDATE_TIME, System.currentTimeMillis());
                    } else {
                        Log.iv(Log.TAG, "error : " + task.getException());
                    }
                }
            });
            Utils.putLong(mContext, PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
            updateRefreshInterval();
        } catch (Exception | Error e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private void updateRefreshInterval() {
        long interval;
        try {
            String str = REFRESH_INTERVALS.get(mRandom.nextInt(REFRESH_INTERVALS.size()));
            interval = Long.parseLong(str);
        } catch (Exception | Error e) {
            interval = REFRESH_INTERVAL;
        }
        Utils.putLong(mContext, PREF_REFRESH_INTERVAL, interval);
    }

    private long getRefreshInterval() {
        long interval = Utils.getLong(mContext, PREF_REFRESH_INTERVAL, REFRESH_INTERVAL);
        return interval;
    }

    private void ensureFirebase() {
        if (mInstance == null) {
            try {
                mInstance = FirebaseRemoteConfig.getInstance();
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : miss google-services.json");
            }
        }
    }

    private void activeFetchConfig() {
        if (DataConfigRemote.sUmengRemoteConfigEnable) {
            String error = null;
            try {
                Class<?> clazz = Class.forName("com.umeng.cconfig.UMRemoteConfig");
                Method method = clazz.getMethod("getInstance");
                Object instance = method.invoke(null);
                method = clazz.getMethod("activeFetchConfig");
                method.invoke(instance);
            } catch (Exception e) {
                error = String.valueOf(e);
            } catch (Error e) {
                error = String.valueOf(e);
            }
            if (!TextUtils.isEmpty(error)) {
                Log.iv(Log.TAG_SDK, "act config error : " + error);
            }
        }
    }
}
