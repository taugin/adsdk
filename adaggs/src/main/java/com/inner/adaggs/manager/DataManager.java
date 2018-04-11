package com.inner.adaggs.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.framework.Aes;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.parse.AdParser;
import com.inner.adaggs.parse.IParser;
import com.inner.adaggs.request.GTagDataRequest;
import com.inner.adaggs.request.HttpDataRequest;
import com.inner.adaggs.request.IDataRequest;
import com.inner.adaggs.utils.Utils;

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

    public void init(String containerId, String url) {
        Log.d(Log.TAG, "containerId : " + containerId + " , url : " + url);
        if (mDataRequest == null) {
            if (!TextUtils.isEmpty(containerId)) {
                mDataRequest = new GTagDataRequest(mContext);
                mDataRequest.setAddress(containerId);
            } else if (!TextUtils.isEmpty(url)) {
                mDataRequest = new HttpDataRequest(mContext);
                mDataRequest.setAddress(url);
            }
        }
        if (mDataRequest != null) {
            mDataRequest.request();
        }
    }

    public AdConfig getLocalAdConfig() {
        if (mLocalAdConfig == null) {
            String data = Utils.readAssets(mContext, "data_config.dat");
            int status = mParser.parseStatus(data);
            String content = mParser.parseContent(data);
            String adContent = null;
            if (status == 1) {
                adContent = Aes.decrypt(content, Constant.KEY_PASSWORD);
            } else {
                adContent = content;
            }
            mLocalAdConfig = mParser.parse(adContent);
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
