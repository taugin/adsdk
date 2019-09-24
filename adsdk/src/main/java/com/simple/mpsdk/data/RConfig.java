package com.simple.mpsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.utils.Utils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/12.
 */

public class RConfig {

    private Context mContext;

    public RConfig(Context context) {
        mContext = context;
    }

    public String getString(String key) {
        String value = readConfigFromAsset(key);
        LogHelper.iv(LogHelper.TAG, "locale config : " + key + " , value : " + value);
        if (TextUtils.isEmpty(value)) {
            String dataWithSuffix = null;
            String mediaSourceSuffix = getMsSuffix();
            String attrSuffix = getAfSuffix();
            String mediaSourceKey = key + mediaSourceSuffix;
            String attrKey = key + attrSuffix;
            LogHelper.iv(LogHelper.TAG, "media suffix : " + mediaSourceSuffix + " , attr suffix : " + attrSuffix);
            // 首先获取带有归因的配置，如果归因配置为空，则使用默认配置
            String mediaData = getConfigFromFirebase(mediaSourceKey);
            String attrData = getConfigFromFirebase(attrKey);
            if (!TextUtils.isEmpty(mediaData)) {
                dataWithSuffix = mediaData;
            } else {
                dataWithSuffix = attrData;
            }
            if (TextUtils.isEmpty(dataWithSuffix)) {
                value = getConfigFromFirebase(key);
            } else {
                String source = !TextUtils.isEmpty(mediaData) ? getMediaSource() : getAfStatus();
                LogHelper.iv(LogHelper.TAG, "remote config : " + key + "[" + source + "]");
                value = dataWithSuffix;
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

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
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
            LogHelper.iv(LogHelper.TAG, "get config error : " + error);
        }
        return null;
    }
}