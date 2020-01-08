package com.hauyu.adsdk.data;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.dock.vost.moon.SpConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.AdSwitch;
import com.hauyu.adsdk.data.config.PlaceConfig;
import com.hauyu.adsdk.data.parse.AdParser;
import com.hauyu.adsdk.data.parse.IParser;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager implements Runnable {

    private static final String DATA_CONFIG_FORMAT = "data_%s";
    private static final String DATA_CONFIG = "cfg_data_config";
    private static final String PREF_ADSWITCH_FLAG = "pref_adswitch_flag";
    private static final String CONFIG_SUFFIX1 = ".dat";
    private static final String CONFIG_SUFFIX2 = ".json";
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
    private PlaceConfig mLocalPlaceConfig;
    private IParser mParser;
    private AdSwitch mAdSwitch;
    private Handler mHandler = new Handler();

    public void init() {
        parseLocalData();
        if (mDataRequest == null) {
            mDataRequest = new DataConfigRemote(mContext);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 5000);
        }
    }

    @Override
    public void run() {
        if (mDataRequest != null) {
            mDataRequest.request();
        }
    }

    private void parseLocalData() {
        String cfgName = getConfigName();
        String defName = getDefaultName();
        Log.iv(Log.TAG, "name : " + cfgName + "/" + defName);
        if (mLocalPlaceConfig == null && mParser != null) {
            String data = Utils.readAssets(mContext, cfgName + CONFIG_SUFFIX1);
            if (TextUtils.isEmpty(data)) {
                data = Utils.readAssets(mContext, cfgName + CONFIG_SUFFIX2);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readAssets(mContext, defName + CONFIG_SUFFIX1);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readAssets(mContext, defName + CONFIG_SUFFIX2);
            }
            mLocalPlaceConfig = mParser.parseAdConfig(data);
            if (mLocalPlaceConfig != null) {
                mLocalPlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                Log.v(Log.TAG, "locale data has been set success");
            }
        }
    }

    private void parseRemoteData() {
        Log.iv(Log.TAG, "remote data reading");
        String data = null;
        data = getString(DATA_CONFIG);
        data = checkLastData(data, DATA_CONFIG);
        if (!TextUtils.isEmpty(data)
                && (mLocalPlaceConfig == null || !TextUtils.equals(mLocalPlaceConfig.getAdConfigMd5(), Utils.string2MD5(data)))) {
            if (mParser != null) {
                mLocalPlaceConfig = mParser.parseAdConfig(data);
                if (mLocalPlaceConfig != null) {
                    mLocalPlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                    Log.iv(Log.TAG, "remote data has been set success");
                }
            }
        }
    }

    public PlaceConfig getAdConfig() {
        parseRemoteData();
        parseLocalData();
        return mLocalPlaceConfig;
    }

    private String getConfigName() {
        String cfgName = null;
        try {
            String pkgmd5 = Utils.string2MD5(mContext.getPackageName());
            pkgmd5 = pkgmd5.toLowerCase(Locale.getDefault());
            String filename = pkgmd5.substring(0, 8);
            cfgName = "cfg" + filename;
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
        }
        return cfgName;
    }

    private String getDefaultName() {
        return String.format(Locale.getDefault(), DATA_CONFIG_FORMAT, "config");
    }

    public AdPlace getRemoteAdPlace(String key) {
        String data = getString(key);
        data = checkLastData(data, key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPlace(data);
        }
        return null;
    }

    public AdSwitch getAdSwitch() {
        String data = getString(Constant.ADSWITCH_NAME);
        if (!TextUtils.isEmpty(data)) {
            String oldSwitchMd5 = Utils.getString(mContext, PREF_ADSWITCH_FLAG);
            String newSwitchMd5 = Utils.string2MD5(data);
            if (mAdSwitch == null || !TextUtils.equals(oldSwitchMd5, newSwitchMd5)) {
                mAdSwitch = mParser.parseAdSwitch(data);
                Utils.putString(mContext, PREF_ADSWITCH_FLAG, newSwitchMd5);
            }
        }
        if (mAdSwitch == null && mLocalPlaceConfig != null) {
            mAdSwitch = mLocalPlaceConfig.getAdSwitch();
        }
        Log.iv(Log.TAG, "ads : " + mAdSwitch);
        return mAdSwitch;
    }

    public Map<String, String> getRemoteAdRefs() {
        String data = getString(Constant.ADREFS_NAME);
        data = checkLastData(data, Constant.ADREFS_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdRefs(data);
        }
        return null;
    }

    public List<SpConfig> getRemoteSpread() {
        String data = getString(SpConfig.AD_SPREAD_NAME);
        data = checkLastData(data, SpConfig.AD_SPREAD_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseSpread(data);
        }
        return null;
    }

    public String getString(String key) {
        if (mDataRequest != null) {
            return mDataRequest.getString(key);
        }
        return null;
    }

    /**
     * 获取默认数据
     *
     * @param data
     * @param key
     * @return
     */
    private String checkLastData(String data, String key) {
        return data;
    }
}
