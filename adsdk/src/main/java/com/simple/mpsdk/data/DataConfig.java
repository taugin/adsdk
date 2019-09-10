package com.simple.mpsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.simple.mpsdk.config.AdConfig;
import com.simple.mpsdk.config.AdPlace;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.data.parser.AdConfigParser;
import com.simple.mpsdk.data.parser.IConfigParser;
import com.simple.mpsdk.utils.Utils;

import java.util.Locale;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataConfig {

    private static final String DATA_CONFIG = "data_%s.dat";
    private static DataConfig sDataConfig;

    public static DataConfig get(Context context) {
        synchronized (DataConfig.class) {
            if (sDataConfig == null) {
                createInstance(context);
            }
        }
        return sDataConfig;
    }

    private static void createInstance(Context context) {
        synchronized (DataConfig.class) {
            if (sDataConfig == null) {
                sDataConfig = new DataConfig(context);
            }
        }
    }

    private DataConfig(Context context) {
        mContext = context;
        mParser = new AdConfigParser();
    }

    private RConfig mDataRequest;
    private Context mContext;
    private AdConfig mLocalAdConfig;
    private IConfigParser mParser;

    public void init() {
        parserLocalData();
        if (mDataRequest == null) {
            mDataRequest = new RConfig(mContext);
        }
    }

    private void parserLocalData() {
        String cfgName = getConfigName();
        String defName = getDefaultName();
        LogHelper.v(LogHelper.TAG, "cfg : " + cfgName + " , def : " + defName);
        if (mLocalAdConfig == null) {
            String data = Utils.readAssets(mContext, cfgName);
            if (TextUtils.isEmpty(data)) {
                data = Utils.readAssets(mContext, defName);
            }
            mLocalAdConfig = mParser.parseAdConfig(data);
        }
    }

    public AdConfig getAdConfig() {
        if (mLocalAdConfig == null) {
            parserLocalData();
        }
        return mLocalAdConfig;
    }

    private String getConfigName() {
        String cfgName = null;
        try {
            String pkgmd5 = Utils.string2MD5(mContext.getPackageName());
            pkgmd5 = pkgmd5.toLowerCase(Locale.getDefault());
            String filename = pkgmd5.substring(0, 8);
            cfgName = "cfg" + filename + ".dat";
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "error : " + e);
        }
        return cfgName;
    }

    private String getDefaultName() {
        return String.format(Locale.getDefault(), DATA_CONFIG, "config");
    }

    public AdPlace getRemoteAdPlace(String key) {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(key);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdPlace(data);
            }
        }
        return null;
    }

    public String getString(String key) {
        if (mDataRequest != null) {
            return mDataRequest.getString(key);
        }
        return null;
    }
}
