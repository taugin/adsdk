package com.hauyu.adsdk.request;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.utils.Utils;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/2/12.
 */

@SuppressWarnings("unchecked")
public class RemoteConfigRequest implements IDataRequest, OnCompleteListener {

    private static final int CACHE_EXPIRETIME = 15 * 60;
    private static final int REFRESH_INTERVAL = CACHE_EXPIRETIME * 1000;
    private Context mContext;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public RemoteConfigRequest(Context context) {
        mContext = context;
        ensureFirebase();
    }

    @Override
    public void setAddress(String address) {
    }

    @Override
    public void request() {
        refresh();
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task == null) {
            Log.e(Log.TAG, "onComplete task == null");
            return;
        }
        if (task.isSuccessful()) {
            Log.v(Log.TAG, "onComplete fetch successfully");
            mFirebaseRemoteConfig.activateFetched();
        } else {
            Log.e(Log.TAG, "error : " + task.getException());
        }
    }

    @Override
    public void refresh() {
        ensureFirebase();
        if (mFirebaseRemoteConfig != null) {
            synchronized (mFirebaseRemoteConfig) {
                long now = System.currentTimeMillis();
                long last = Utils.getLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME);
                boolean needRequest = now - last > REFRESH_INTERVAL;
                long leftTime = REFRESH_INTERVAL - (now - last);
                if (leftTime > 0) {
                    Constant.SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                    Log.iv(Log.TAG, "adsdk refresh next : " + Constant.SDF_LEFT_TIME.format(new Date(leftTime)));
                } else {
                    Log.iv(Log.TAG, "adsdk refresh next : time is not correct");
                }
                if (needRequest) {
                    try {
                        mFirebaseRemoteConfig.fetch(CACHE_EXPIRETIME).addOnCompleteListener(this);
                        Utils.putLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
                        Log.iv(Log.TAG, "adsdk refresh fetch called");
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                    }
                }
            }
        }
    }

    @Override
    public String getString(String key) {
        String value = readConfigFromAsset(key);
        Log.iv(Log.TAG, "locale config : " + key + " , value : " + value);
        if (TextUtils.isEmpty(value)) {
            if (mFirebaseRemoteConfig != null) {
                String attrKey = key + getSuffix();
                Log.iv(Log.TAG, "remote suffix : " + getSuffix());
                // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
                String attrData = mFirebaseRemoteConfig.getString(attrKey);
                if (TextUtils.isEmpty(attrData)) {
                    value = mFirebaseRemoteConfig.getString(key);
                } else {
                    if (!TextUtils.isEmpty(attrData)) {
                        Log.iv(Log.TAG, "remote config : " + key + "[" + getAfStatus() + "]");
                        value = attrData;
                    }
                }
            }
        }
        return value;
    }

    private String getAfStatus() {
        try {
            return Utils.getString(mContext, Constant.AF_STATUS, Constant.AF_ORGANIC);
        } catch(Exception e) {
        }
        return null;
    }

    private String getSuffix() {
        String afStatus = getAfStatus();
        if (!TextUtils.isEmpty(afStatus)) {
            afStatus = afStatus.replaceAll("[^0-9a-zA-Z_]+","");
            return "_" + afStatus.toLowerCase(Locale.getDefault());
        }
        return "";
    }

    private void ensureFirebase() {
        if (mFirebaseRemoteConfig == null) {
            try {
                mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            } catch(Exception e) {
                Log.e(Log.TAG, "error : " + e + "[Should add google-services.json file to root]");
            }
        }
    }

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
    }
}
