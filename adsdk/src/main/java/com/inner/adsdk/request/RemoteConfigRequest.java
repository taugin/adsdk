package com.inner.adsdk.request;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/12.
 */

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
                Log.v(Log.TAG, "adsdk refresh now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)) + " , do : " + needRequest);
                if (needRequest) {
                    try {
                        mFirebaseRemoteConfig.fetch(CACHE_EXPIRETIME).addOnCompleteListener(this);
                        Utils.putLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
                        Log.v(Log.TAG, "adsdk refresh fetch called");
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
        Log.v(Log.TAG, "local config : " + key + " , value : " + value);
        if (TextUtils.isEmpty(value)) {
            if (mFirebaseRemoteConfig != null) {
                value = mFirebaseRemoteConfig.getString(key);
            }
        }
        return value;
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
