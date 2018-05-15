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

    private static final int REFRESH_INTERVAL = 15 * 60;
    private Context mContext;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public RemoteConfigRequest(Context context) {
        mContext = context;
        try {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e + "(should add google-services.json file to root)");
        }
    }

    @Override
    public void setAddress(String address) {
    }

    @Override
    public void request() {
        if (mFirebaseRemoteConfig != null) {
            mFirebaseRemoteConfig.fetch(REFRESH_INTERVAL).addOnCompleteListener(this);
        }
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task != null && task.isSuccessful()) {
            mFirebaseRemoteConfig.activateFetched();
        }
    }

    @Override
    public void refresh() {
        if (mFirebaseRemoteConfig != null) {
            long now = System.currentTimeMillis();
            long last = Utils.getLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME);
            Log.v(Log.TAG, "GTagDataRequestrefresh now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)));
            if (now - last > REFRESH_INTERVAL) {
                try {
                    mFirebaseRemoteConfig.fetch(REFRESH_INTERVAL).addOnCompleteListener(this);
                    Utils.putLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
                    Log.v(Log.TAG, "container holder refresh");
                } catch (Exception e) {
                }
            }
        } else {
            long now = System.currentTimeMillis();
            long last = Utils.getLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME);
            if (now - last > REFRESH_INTERVAL) {
                request();
                Utils.putLong(mContext, Constant.PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
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

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
    }
}
