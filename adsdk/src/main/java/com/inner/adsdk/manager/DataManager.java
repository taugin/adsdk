package com.inner.adsdk.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.parse.AdParser;
import com.inner.adsdk.parse.IParser;
import com.inner.adsdk.request.IDataRequest;
import com.inner.adsdk.request.RemoteConfigRequest;
import com.inner.adsdk.utils.Utils;

import java.util.Locale;
import java.util.Map;

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
        if (mDataRequest == null) {
            mDataRequest = new RemoteConfigRequest(mContext);
        }
        if (mDataRequest != null) {
            mDataRequest.request();
        }
    }

    public void refresh() {
        if (mDataRequest != null) {
            mDataRequest.refresh();
        }
    }

    public AdConfig getLocalAdConfig() {
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

    public GtConfig getRemoteGtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseGtPolicy(data);
            }
        }
        return null;
    }

    public Map<String, String> getRemoteAdIds() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADIDS_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdIds(data);
            }
        }
        return null;
    }

    public AdSwitch getAdSwitch() {
        AdSwitch adSwitch = null;
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADSWITCH_NAME);
            if (!TextUtils.isEmpty(data)) {
                adSwitch = mParser.parseAdSwitch(data);
            }
            if (adSwitch == null && mLocalAdConfig != null) {
                adSwitch = mLocalAdConfig.getAdSwitch();
            }
            Log.v(Log.TAG, "adSwitch : " + adSwitch);
        }
        return adSwitch;
    }

    public Map<String, String> getRemoteAdRefs() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADREFS_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdRefs(data);
            }
        }
        return null;
    }
}
