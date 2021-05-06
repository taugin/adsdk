package com.rabbit.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PlaceConfig;
import com.rabbit.adsdk.data.parse.AdParser;
import com.rabbit.adsdk.data.parse.IParser;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.SpreadCfg;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager {

    private static final String DATA_CONFIG_FORMAT = "data_%s";
    private static final String DATA_CONFIG = "cfg_data_config";
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

    private Context mContext;
    private PlaceConfig mLocalPlaceConfig;
    private IParser mParser;
    private boolean mLocalFirst = false;

    public void init() {
        setLocalFirst();
        VRemoteConfig.get(mContext).init();
        parseLocalData();
        printGoogleAdvertisingId();
    }

    private void setLocalFirst() {
        try {
            File file = new File(mContext.getExternalFilesDir("config"), "local_first");
            mLocalFirst = file.exists();
            Log.iv(Log.TAG, "local first : " + mLocalFirst + " , path : " + file.getAbsolutePath());
        } catch (Exception e) {
        }
    }

    private void parseLocalData() {
        String cfgName = getConfigName();
        String defName = getDefaultName();
        Log.iv(Log.TAG, "name : " + cfgName + "/" + defName);
        if (mLocalPlaceConfig == null && mParser != null) {
            String data = Utils.readConfig(mContext, cfgName + CONFIG_SUFFIX1);
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, cfgName + CONFIG_SUFFIX2);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, defName + CONFIG_SUFFIX1);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, defName + CONFIG_SUFFIX2);
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
        if (!isLocalFirst()) {
            parseRemoteData();
        }
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

    public Map<String, String> getRemoteAdRefs() {
        String data = getString(Constant.ADREFS_NAME);
        data = checkLastData(data, Constant.ADREFS_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdRefs(data);
        }
        return null;
    }

    public List<SpreadCfg> getRemoteSpread() {
        String data = getString(SpreadCfg.AD_SPREAD_NAME);
        data = checkLastData(data, SpreadCfg.AD_SPREAD_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseSpread(data);
        }
        return null;
    }

    public Map<String, Map<String, String>> getMediationConfig() {
        String data = getString(Constant.AD_MEDIATION_CONFIG);
        data = checkLastData(data, Constant.AD_MEDIATION_CONFIG);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseMediationConfig(data);
        }
        return null;
    }

    public String getString(String key) {
        return DataConfigRemote.get(mContext).getString(key);
    }

    public List<String> getPlaceList() {
        return parseStringList(DataManager.get(mContext).getString(Constant.COMPLEX_PLACES));
    }

    private List<String> parseStringList(String str) {
        List<String> list = null;
        try {
            JSONArray jarray = new JSONArray(str);
            if (jarray != null && jarray.length() > 0) {
                list = new ArrayList<String>(jarray.length());
                for (int index = 0; index < jarray.length(); index++) {
                    String s = jarray.getString(index);
                    if (!TextUtils.isEmpty(s)) {
                        list.add(s);
                    }
                }
            }
        } catch (Exception e) {
        }
        return list;
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

    private void printGoogleAdvertisingId() {
        new Thread() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                    String gaid = info.getId();
                    boolean isLimited = info.isLimitAdTrackingEnabled();
                    String gaidmd5 = Utils.string2MD5(gaid);
                    Log.iv(Log.TAG, "google advertising id (gaid) : " + gaid + " , is limit ad tracking : " + isLimited + " , gaid2 : " + gaidmd5);
                    Utils.putString(mContext, Constant.PREF_GAID, gaid);
                } catch (Exception e) {
                    Log.e(Log.TAG, "error : " + e);
                }
            }
        }.start();
    }

    public boolean isLocalFirst() {
        return mLocalFirst;
    }
}
