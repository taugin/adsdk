package com.inner.adsdk.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdPolicy;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.parse.AdParser;
import com.inner.adsdk.parse.IParser;
import com.inner.adsdk.request.GTagDataRequest;
import com.inner.adsdk.request.IDataRequest;
import com.inner.adsdk.utils.Utils;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager {
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

    public void init(String containerId) {
        Log.d(Log.TAG, "containerId : " + containerId);
        if (mDataRequest == null && !TextUtils.isEmpty(containerId)) {
            mDataRequest = new GTagDataRequest(mContext);
            mDataRequest.setAddress(containerId);
        }
        if (mDataRequest != null) {
            mDataRequest.request();
        }
    }

    public AdConfig getLocalAdConfig() {
        if (mLocalAdConfig == null) {
            String data = Utils.readAssets(mContext, "data_config_encrypt.dat");
            mLocalAdConfig = mParser.parseAdConfig(data);
        }
        return mLocalAdConfig;
    }

    public AdPlace getRemoteAdPlace(String key) {
        String data = mDataRequest.getString(key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPlace(data);
        }
        return null;
    }

    public AdPolicy getRemoteAdPolicy(String key) {
        String data = mDataRequest.getString(key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPolicy(data);
        }
        return null;
    }

    public Map<String, String> getRemoteAdIds(String key) {
        String data = mDataRequest.getString(key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdIds(data);
        }
        return null;
    }
}
