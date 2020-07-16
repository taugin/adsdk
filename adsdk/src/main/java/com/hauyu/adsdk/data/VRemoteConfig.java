package com.hauyu.adsdk.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

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

public class VRemoteConfig implements OnCompleteListener {

    private static final int CACHE_EXPIRETIME = 15 * 60;
    private static final int REFRESH_INTERVAL = CACHE_EXPIRETIME * 1000;
    private static final String PREF_REFRESH_INTERVAL = "pref_refresh_interval";
    private static final String PREF_REMOTE_CONFIG_REQUEST_TIME = "pref_data_config_rtime";
    private static final SimpleDateFormat SDF_LEFT_TIME = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static List<String> REFRESH_INTERVALS;
    private Context mContext;
    private FirebaseRemoteConfig mInstance;
    private Random mRandom = new Random(System.currentTimeMillis());

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

    public VRemoteConfig(Context context) {
        mContext = context;
        updateRefreshInterval();
        ensureFirebase();
    }

    public void updateRemoteConfig() {
        requestInternal();
    }

    private void requestInternal() {
        ensureFirebase();
        if (mInstance != null) {
            synchronized (mInstance) {
                if (isNeedRequest()) {
                    requestDataConfig();
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
            mInstance.fetch(CACHE_EXPIRETIME).addOnCompleteListener(this);
            Utils.putLong(mContext, PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
            updateRefreshInterval();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void updateRefreshInterval() {
        long interval;
        try {
            String str = REFRESH_INTERVALS.get(mRandom.nextInt(REFRESH_INTERVALS.size()));
            interval = Long.parseLong(str);
        } catch (Exception e) {
            interval = REFRESH_INTERVAL;
        }
        Utils.putLong(mContext, PREF_REFRESH_INTERVAL, interval);
    }

    private long getRefreshInterval() {
        long interval = Utils.getLong(mContext, PREF_REFRESH_INTERVAL, REFRESH_INTERVAL);
        return interval;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task == null) {
            Log.e(Log.TAG, "task == null");
            return;
        }
        if (task.isSuccessful()) {
            Log.iv(Log.TAG, "successfully");
            if (mInstance != null) {
                mInstance.fetchAndActivate();
            }
        } else {
            Log.e(Log.TAG, "error : " + task.getException());
        }
    }

    private void ensureFirebase() {
        if (mInstance == null) {
            try {
                mInstance = FirebaseRemoteConfig.getInstance();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e + "[miss google-services.json file]");
            }
        }
    }
}
