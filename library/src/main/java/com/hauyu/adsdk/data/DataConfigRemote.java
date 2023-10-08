package com.hauyu.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.Utils;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/2/12.
 */

@SuppressWarnings("unchecked")
public class DataConfigRemote {

    public static final boolean sFirebaseRemoteConfigEnable;

    public static Method firebaseInstanceMethod;
    public static Method firebaseGetStringMethod;

    public static final boolean sUmengRemoteConfigEnable;
    public static Method umengInstanceMethod;
    public static Method umengGetStringMethod;
    private static boolean sRemoteFirst = false;
    private static long sRemoteFirstTime = 0;

    static {
        boolean enable;
        try {
            Class<?> clazz = Class.forName("com.umeng.cconfig.UMRemoteConfig");
            umengInstanceMethod = clazz.getMethod("getInstance");
            umengGetStringMethod = clazz.getMethod("getConfigValue", String.class);
            enable = true;
        } catch (Exception | Error e) {
            Log.iv(Log.TAG_SDK, "umeng error : " + e);
            enable = false;
        }
        sUmengRemoteConfigEnable = enable;

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

    private String getAfStatus() {
        String afStatus = null;
        try {
            afStatus = Utils.getString(mContext, Constant.AF_STATUS, Constant.AF_ORGANIC);
        } catch (Exception e) {
        }
        return TextUtils.equals(afStatus, Constant.AF_ORGANIC) ? "auo" : "ano";
    }

    private String getAfSuffix() {
        String suffix = "";
        try {
            String attr = Utils.getString(mContext, Constant.AF_STATUS, Constant.AF_ORGANIC);
            if (!TextUtils.equals(attr, Constant.AF_ORGANIC)) {
                suffix = "_ano";
            }
        } catch (Exception e) {
        }
        return suffix;
    }

    private String readConfigFromLocal(String key) {
        return Utils.readConfig(mContext, key);
    }

    private String readConfigFromRemote(String key) {
        String value = null;
        String dataWithSuffix = null;
        String attrSuffix = getAfSuffix();
        String attrKey = key + attrSuffix;
        // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
        String attrData = getRemoteConfig(attrKey);
        if (!TextUtils.isEmpty(attrData)) {
            dataWithSuffix = attrData;
        }
        if (TextUtils.isEmpty(dataWithSuffix)) {
            value = getRemoteConfig(key);
        } else {
            Log.iv(Log.TAG, "remote config : " + key + "[" + getAfStatus() + "]");
            value = dataWithSuffix;
        }
        return value;
    }

    private String getRemoteConfig(String key) {
        String remoteValue = getConfigFromUmeng(key);
        if (!TextUtils.isEmpty(remoteValue)) {
            Log.iv(Log.TAG, "umeng config | " + key + " : " + remoteValue);
            return remoteValue;
        }
        remoteValue = getConfigFromFirebase(key);
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

    private String getConfigFromUmeng(String key) {
        if (sUmengRemoteConfigEnable) {
            String error = null;
            try {
                Object instance = umengInstanceMethod.invoke(null);
                Object value = umengGetStringMethod.invoke(instance, key);
                if (value != null) {
                    return (String) value;
                }
            } catch (Exception e) {
                error = String.valueOf(e);
            } catch (Error e) {
                error = String.valueOf(e);
            }
            if (!TextUtils.isEmpty(error)) {
                Log.iv(Log.TAG_SDK, "umeng error : " + error);
            }
        }
        return null;
    }
}
