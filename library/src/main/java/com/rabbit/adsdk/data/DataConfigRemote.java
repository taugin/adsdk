package com.rabbit.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/12.
 */

@SuppressWarnings("unchecked")
public class DataConfigRemote {

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

    public String getString(String key) {
        VRemoteConfig.get(mContext).updateRemoteConfig(false);
        boolean isLocalFirst = DataManager.get(mContext).isLocalFirst();
        String value = null;
        if (isLocalFirst) {
            value = readConfigFromLocal(key);
            if (TextUtils.isEmpty(value)) {
                value = readConfigFromRemote(key);
            }
        } else {
            value = readConfigFromRemote(key);
            if (TextUtils.isEmpty(value)) {
                value = readConfigFromLocal(key);
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

    private String readConfigFromLocal(String key) {
        return Utils.readConfig(mContext, key);
    }

    private String readConfigFromRemote(String key) {
        String value = null;
        String dataWithSuffix = null;
        String mediaSourceSuffix = getMsSuffix();
        String attrSuffix = getAfSuffix();
        String mediaSourceKey = key + mediaSourceSuffix;
        String attrKey = key + attrSuffix;
        Log.iv(Log.TAG, "media suffix : " + mediaSourceSuffix + " , attr suffix : " + attrSuffix);
        // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
        String mediaData = getRemoteConfig(mediaSourceKey);
        String attrData = getRemoteConfig(attrKey);
        if (!TextUtils.isEmpty(mediaData)) {
            dataWithSuffix = mediaData;
        } else {
            dataWithSuffix = attrData;
        }
        if (TextUtils.isEmpty(dataWithSuffix)) {
            value = getRemoteConfig(key);
        } else {
            String source = !TextUtils.isEmpty(mediaData) ? getMediaSource() : getAfStatus();
            Log.iv(Log.TAG, "remote config : " + key + "[" + source + "]");
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
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig");
            Method method = clazz.getMethod("getInstance");
            Object instance = method.invoke(null);
            method = clazz.getMethod("getString", String.class);
            Object value = method.invoke(instance, key);
            if (value != null) {
                return (String) value;
            }
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "firebase error : " + error);
        }
        return null;
    }

    private String getConfigFromUmeng(String key) {
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.cconfig.UMRemoteConfig");
            Method method = clazz.getMethod("getInstance");
            Object instance = method.invoke(null);
            method = clazz.getMethod("getConfigValue", String.class);
            Object value = method.invoke(instance, key);
            if (value != null) {
                return (String) value;
            }
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "umeng error : " + error);
        }
        return null;
    }
}
