package com.hauyu.adsdk.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.BaseRequest;
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

@SuppressWarnings("unchecked")
public class DataConfigRemote extends BaseRequest implements OnCompleteListener {

    private static final long CACHE_EXPIRETIME = Long.parseLong("900");
    private static final long REFRESH_INTERVAL = Long.parseLong("900000");
    private static final String PREF_REFRESH_INTERVAL = "pref_refresh_interval";
    private static final String PREF_REMOTE_CONFIG_REQUEST_TIME = "pref_data_config_rtime";
    private static final SimpleDateFormat SDF_LEFT_TIME = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static List<String> REFRESH_INTERVALS;

    private Context mContext;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Random mRandom = new Random(System.currentTimeMillis());

    static {
        REFRESH_INTERVALS = new ArrayList<String>();
        REFRESH_INTERVALS.add("900000");
        REFRESH_INTERVALS.add("1200000");
        REFRESH_INTERVALS.add("1500000");
        REFRESH_INTERVALS.add("1800000");
    }

    public DataConfigRemote(Context context) {
        mContext = context;
        ensureFirebase();
        updateRefreshInterval();
    }

    @Override
    public void request() {
        refresh();
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task == null) {
            Log.e(Log.TAG, "vconfig task == null");
            return;
        }
        if (task.isSuccessful()) {
            Log.v(Log.TAG, "vconfig complete successfully");
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
                long last = Utils.getLong(mContext, PREF_REMOTE_CONFIG_REQUEST_TIME);
                boolean needRequest = now - last > getRefreshInterval();
                long leftTime = getRefreshInterval() - (now - last);
                if (leftTime > 0) {
                    SDF_LEFT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                    Log.iv(Log.TAG, "vconfig time left : " + SDF_LEFT_TIME.format(new Date(leftTime)));
                } else {
                    Log.iv(Log.TAG, "vconfig time left : time is not correct");
                }
                if (needRequest) {
                    try {
                        mFirebaseRemoteConfig.fetch(CACHE_EXPIRETIME).addOnCompleteListener(this);
                        Utils.putLong(mContext, PREF_REMOTE_CONFIG_REQUEST_TIME, System.currentTimeMillis());
                        updateRefreshInterval();
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                    }
                }
            }
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
        long interval =  Utils.getLong(mContext, PREF_REFRESH_INTERVAL, REFRESH_INTERVAL);
        Log.iv(Log.TAG, "interval : " + interval);
        return interval;
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
                Log.iv(Log.TAG, "media suffix : " + mediaSourceSuffix + " , attr suffix : " + attrSuffix);
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
        try {
            suffix = suffix.replaceAll("[^0-9a-zA-Z_]+", "");
        } catch (Exception e) {
            suffix = "attr";
        }
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
        try {
            suffix = suffix.replaceAll("[^0-9a-zA-Z_]+", "");
        } catch (Exception e) {
            suffix = "ms";
        }
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
