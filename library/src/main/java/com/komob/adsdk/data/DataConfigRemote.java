package com.komob.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/2/12.
 */

@SuppressWarnings("unchecked")
public class DataConfigRemote {

    public static final boolean sFirebaseRemoteConfigEnable;

    public static Method firebaseInstanceMethod;
    public static Method firebaseGetStringMethod;

    private static boolean sRemoteFirst = false;
    private static long sRemoteFirstTime = 0;

    static {
        boolean enable;
        try {
            Class<?> clazz = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig");
            firebaseInstanceMethod = clazz.getMethod("getInstance");
            firebaseGetStringMethod = clazz.getMethod("getString", String.class);
            enable = true;
        } catch (Exception | Error e) {
            firebaseGetStringMethod = null;
            firebaseInstanceMethod = null;
            enable = false;
        }
        sFirebaseRemoteConfigEnable = enable;
    }

    private Context mContext;
    private static DataConfigRemote sDataConfigRemote;

    public static DataConfigRemote get(Context context) {
        synchronized (DataConfigRemote.class) {
            if (sDataConfigRemote == null) {
                createInstance(context);
            }
            if (sDataConfigRemote != null) {
                sDataConfigRemote.mContext = context;
            }
        }
        return sDataConfigRemote;
    }

    private static void createInstance(Context context) {
        synchronized (DataConfigRemote.class) {
            if (sDataConfigRemote == null) {
                sDataConfigRemote = new DataConfigRemote(context);
            }
        }
    }

    private DataConfigRemote(Context context) {
        mContext = context;
    }

    public boolean getRemoteFirst() {
        if (System.currentTimeMillis() - sRemoteFirstTime > 15 * 60 * 1000) {
            try {
                sRemoteFirst = TextUtils.equals("true", readConfigFromRemote("control_remote_config_first"));
                sRemoteFirstTime = System.currentTimeMillis();
            } catch (Exception e) {
            }
        }
        return sRemoteFirst;
    }

    public String getString(String key) {
        VRemoteConfig.get(mContext).updateRemoteConfig(false);
        String value;
        if (getRemoteFirst()) {
            value = readConfigFromRemote(key);
            if (TextUtils.isEmpty(value)) {
                value = readConfigFromLocal(key);
            }
        } else {
            value = readConfigFromLocal(key);
            if (TextUtils.isEmpty(value)) {
                value = readConfigFromRemote(key);
            }
        }
        return value;
    }

    private String readConfigFromLocal(String key) {
        return Utils.readConfig(mContext, key);
    }

    private String readConfigFromRemote(String key) {
        String value = null;
        String attrSuffix = Utils.getBoolean(mContext, Constant.PREF_USER_STATUS, false) ? "_ano" : "";
        String attrKey = key + attrSuffix;
        // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
        String attrData = getRemoteConfig(attrKey);
        if (!TextUtils.isEmpty(attrData)) {
            value = attrData;
        } else {
            if (!TextUtils.equals(key, attrKey)) {
                value = getRemoteConfig(key);
            }
        }
        return value;
    }

    private String getRemoteConfig(String key) {
        String remoteValue = getConfigFromFirebase(key);
        if (!TextUtils.isEmpty(remoteValue)) {
            Log.iv(Log.TAG, "firebase config | " + key + " : " + remoteValue);
            return remoteValue;
        }
        return null;
    }

    private String getConfigFromFirebase(String key) {
        if (sFirebaseRemoteConfigEnable) {
            String error = null;
            try {
                Object instance = firebaseInstanceMethod.invoke(null);
                Object value = firebaseGetStringMethod.invoke(instance, key);
                if (value != null) {
                    return (String) value;
                }
            } catch (Exception e) {
                error = String.valueOf(e);
            } catch (Error e) {
                error = String.valueOf(e);
            }
            if (!TextUtils.isEmpty(error)) {
                Log.iv(Log.TAG_SDK, "firebase error : " + error);
            }
        }
        return null;
    }
}
