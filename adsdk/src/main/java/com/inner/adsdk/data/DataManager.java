package com.inner.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.parse.AdParser;
import com.inner.adsdk.parse.IParser;
import com.inner.adsdk.utils.Utils;

import java.util.Locale;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager {

    private static final String DATA_CONFIG = "data_%s.dat";
    private static DataManager sDataManager;

    public static DataManager get(Context context) {
        synchronized (DataManager.class) {
            if (sDataManager == null) {
                createInstance(context);
            }
        }
        return sDataManager;
    }

    private static void createInstance(Context context) {
        synchronized (DataManager.class) {
            if (sDataManager == null) {
                sDataManager = new DataManager(context);
            }
        }
    }

    private DataManager(Context context) {
        mContext = context;
        mParser = new AdParser();
    }

    private IDataRequest mDataRequest;
    private Context mContext;
    private AdConfig mLocalAdConfig;
    private IParser mParser;

    public void init() {
        parserLocalData();
        if (mDataRequest == null) {
            mDataRequest = new RConfig(mContext);
        }
    }

    private void parserLocalData() {
        String cfgName = getConfigName();
        String defName = getDefaultName();
        Log.v(Log.TAG, "cfg : " + cfgName + " , def : " + defName);
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
            Log.v(Log.TAG, "error : " + e);
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
