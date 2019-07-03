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
import java.util.Locale;
import java.util.TimeZone;

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
            Log.iv(Log.TAG, "onComplete fetch successfully");
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
                String dataWithSuffix = null;
                String mediaSourceSuffix = getMsSuffix();
                String attrSuffix = getAfSuffix();
                String mediaSourceKey = key + mediaSourceSuffix;
                String attrKey = key + attrSuffix;
                Log.iv(Log.TAG, "media suffix : " + mediaSourceSuffix + " , attribute suffix : " + attrSuffix);
                // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
                String mediaData = mFirebaseRemoteConfig.getString(mediaSourceKey);
                String attrData = mFirebaseRemoteConfig.getString(attrKey);
                if (!TextUtils.isEmpty(mediaData)) {
                    dataWithSuffix = mediaData;
                } else {
                    dataWithSuffix = attrData;
                }
                if (TextUtils.isEmpty(dataWithSuffix)) {
                    value = mFirebaseRemoteConfig.getString(key);
                } else {
                    String source = !TextUtils.isEmpty(mediaData) ? getMediaSource() : getAfStatus();
                    Log.iv(Log.TAG, "remote config : " + key + "[" + source + "]");
                    value = dataWithSuffix;
                }
            }
        }
        return value;
    }

    private String getAfStatus() {
        try {
            return Utils.getString(mContext, Constant.AF_STATUS, Constant.AF_ORGANIC);
        } catch (Exception e) {
        }
        return null;
    }

    private String getMediaSource() {
        try {
            return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
        } catch (Exception e) {
        }
        return null;
    }

    private String getAfSuffix() {
        String suffix = null;
        try {
            suffix = Utils.getString(mContext, Constant.AF_STATUS, Constant.AF_ORGANIC);
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(suffix)) {
            suffix = "attr";
        }
        suffix = suffix.replaceAll("[^0-9a-zA-Z_]+", "");
        return "_" + suffix.toLowerCase(Locale.getDefault());
    }

    private String getMsSuffix() {
        String suffix = null;
        try {
            suffix = Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(suffix)) {
            suffix = "ms";
        }
        suffix = suffix.replaceAll("[^0-9a-zA-Z_]+", "");
        return "_" + suffix.toLowerCase(Locale.getDefault());
    }

    private void ensureFirebase() {
        if (mFirebaseRemoteConfig == null) {
            try {
                mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e + "[Should add google-services.json file to root]");
            }
        }
    }

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
    }
}
