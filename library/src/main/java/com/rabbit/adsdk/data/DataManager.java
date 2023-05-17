package com.rabbit.adsdk.data;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PlaceConfig;
import com.rabbit.adsdk.data.config.SpreadConfig;
import com.rabbit.adsdk.data.parse.AdParser;
import com.rabbit.adsdk.data.parse.IParser;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.AesUtils;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
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
    private PlaceConfig mPlaceConfig;
    private IParser mParser;
    private String mAdMdnCfgMd5 = null;
    private Map<String, Map<String, String>> mMdnCfgMap;

    public void init() {
        recordFistActiveTime();
        VRemoteConfig.get(mContext).init();
        parseLocalData();
        printGoogleAdvertisingId();
    }

    private void parseLocalData() {
        if (mPlaceConfig == null && mParser != null) {
            String cfgName = getConfigName();
            String defName = getDefaultName();
            Log.iv(Log.TAG_SDK, "name : " + cfgName + "/" + defName);
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
            mPlaceConfig = mParser.parseAdConfig(data);
            if (mPlaceConfig != null) {
                mPlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                Log.v(Log.TAG, "locale data has been set success");
            }
        }
    }

    private void parseRemoteData() {
        Log.iv(Log.TAG_SDK, "remote data reading");
        String data = null;
        data = getString(DATA_CONFIG);
        if (!TextUtils.isEmpty(data)
                && (mPlaceConfig == null || !TextUtils.equals(mPlaceConfig.getAdConfigMd5(), Utils.string2MD5(data)))) {
            if (mParser != null) {
                mPlaceConfig = mParser.parseAdConfig(data);
                if (mPlaceConfig != null) {
                    mPlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                    Log.iv(Log.TAG, "remote data has been set success");
                }
            }
        }
    }

    public PlaceConfig getAdConfig() {
        parseRemoteData();
        parseLocalData();
        return mPlaceConfig;
    }

    private String getMd5SubString() {
        try {
            String pkgmd5 = Utils.string2MD5(mContext.getPackageName());
            pkgmd5 = pkgmd5.toLowerCase(Locale.ENGLISH);
            return pkgmd5.substring(0, 8);
        } catch (Exception e) {
        }
        return "_sdk_ads";
    }

    private String getConfigName() {
        String cfgName = null;
        try {
            cfgName = "cfg" + getMd5SubString();
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
        }
        return cfgName;
    }

    private String getMediationConfigKey() {
        try {
            return "mdn" + new StringBuilder(getMd5SubString()).reverse().toString();
        } catch (Exception e) {
        }
        return "mdn_sdk_cfg";
    }


    private String getDefaultName() {
        return String.format(Locale.ENGLISH, DATA_CONFIG_FORMAT, "config");
    }

    public AdPlace getRemoteAdPlace(String key) {
        String data = getString(key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPlace(data);
        }
        return null;
    }

    public Map<String, String> getRemoteAdRefs() {
        String data = getString(Constant.SHARE_PLACE);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseSharePlace(data);
        }
        return null;
    }

    public List<SpreadConfig> getRemoteSpread() {
        String data = getString(SpreadConfig.AD_SPREAD_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseSpread(data);
        }
        return null;
    }

    public Map<String, Map<String, String>> getMediationConfig() {
        String mediationConfigKey = getMediationConfigKey();
        String data = getString(mediationConfigKey);
        if (!TextUtils.isEmpty(data)) {
            String md5 = Utils.string2MD5(data);
            if (mMdnCfgMap == null || mMdnCfgMap.isEmpty() || !TextUtils.equals(md5, mAdMdnCfgMd5)) {
                Log.iv(Log.TAG, "parse mediation config");
                mAdMdnCfgMd5 = md5;
                mMdnCfgMap = mParser.parseMediationConfig(data);
            } else {
                Log.iv(Log.TAG, "mediation config parsed");
            }
        }
        return mMdnCfgMap;
    }

    public Collection<String> getSignList() {
        Map<String, Map<String, String>> mapMap = getMediationConfig();
        if (mapMap != null) {
            Map<String, String> signMap = mapMap.get("allow.sign.list");
            if (signMap != null && !signMap.isEmpty()) {
                return signMap.values();
            }
        }
        return null;
    }


    public Collection<String> getPackList() {
        Map<String, Map<String, String>> mapMap = getMediationConfig();
        if (mapMap != null) {
            Map<String, String> signMap = mapMap.get("allow.pack.list");
            if (signMap != null && !signMap.isEmpty()) {
                return signMap.values();
            }
        }
        return null;
    }

    public boolean isComplexNativeFull() {
        String complexNativeFull = null;
        Map<String, Map<String, String>> mapMap = getMediationConfig();
        if (mapMap != null) {
            Map<String, String> signMap = mapMap.get("complex.ads.config");
            if (signMap != null && !signMap.isEmpty()) {
                complexNativeFull = signMap.get("complex_native_full");
            }
        }
        if (TextUtils.isEmpty(complexNativeFull)) {
            complexNativeFull = "true";
        }
        return TextUtils.equals(complexNativeFull, "true");
    }

    public String getString(String key) {
        if (!TextUtils.isEmpty(key) && key.startsWith("md5:")) {
            key = key.substring(4);
            String md5Key = "md" + Utils.string2MD5(key);
            // Log.iv(Log.TAG, key + " : " + md5Key);
            key = md5Key;
        }
        // 对firebase的内容进行加密，使用固定字符串开头
        String value = DataConfigRemote.get(mContext).getString(key);
        if (value != null && (value.startsWith("DIAMOND:") || value.startsWith("diamond:"))) {
            String content = value.substring("diamond:".length());
            // Log.iv(Log.TAG, "content : " + content);
            value = AesUtils.decrypt(Constant.KEY_PASSWORD, content);
            // Log.iv(Log.TAG, "value : " + value);
        }
        return value;
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

    public String getScenePrefix() {
        String scenePrefix = getString(IParser.SCENE_PREFIX);
        if (TextUtils.isEmpty(scenePrefix) && mPlaceConfig != null) {
            scenePrefix = mPlaceConfig.getScenePrefix();
        }
        return scenePrefix;
    }

    public boolean isDisableVpn() {
        if (mPlaceConfig != null) {
            return mPlaceConfig.isDisableVpnLoad();
        }
        return false;
    }

    /**
     * 验证广告位是否存在
     *
     * @param placeName
     * @return
     */
    public boolean isPlaceValidate(String placeName) {
        AdPlace adPlace = null;
        if (mPlaceConfig != null) {
            adPlace = mPlaceConfig.get(placeName);
        }
        if (adPlace == null) {
            adPlace = getRemoteAdPlace(placeName);
        }
        return adPlace != null;
    }

    private void recordFistActiveTime() {
        try {
            long time = Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
            if (time <= 0) {
                Utils.putLong(mContext, Constant.PREF_USER_ACTIVE_TIME, System.currentTimeMillis());
            }
        } catch (Exception e) {
        }
    }

    public long getFirstActiveTime() {
        return Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
    }

    public long getElapsedTimeMillis() {
        long elapsedTime = SystemClock.elapsedRealtime();
        long currentTime = System.currentTimeMillis();
        long lastElapsedTime = Utils.getLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, 0);
        long lastCurrentTime = Utils.getLong(mContext, Constant.PREF_LAST_CURRENT_TIME, 0);
        if (lastElapsedTime <= 0) {
            // 首次获取时间
            Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
            Utils.putLong(mContext, Constant.PREF_LAST_CURRENT_TIME, currentTime);
            return currentTime;
        }
        if (elapsedTime < lastElapsedTime) {
            // 重启手机后，首次获取时间
            Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
            return lastCurrentTime;
        }
        long finalTime;
        long elapsedDiff = elapsedTime - lastElapsedTime;
        long currentDiff = currentTime - lastCurrentTime;
        long deviation = Math.abs(currentDiff - elapsedDiff);
        if (deviation < 10000) {
            finalTime = currentTime;
        } else {
            finalTime = lastCurrentTime + elapsedDiff;
        }
        Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
        Utils.putLong(mContext, Constant.PREF_LAST_CURRENT_TIME, finalTime);
        // Log.iv(Log.TAG, "elapsed diff : " + elapsedDiff + " , current diff : " + currentDiff + " , deviation : " + deviation + " , final time : " + Constant.SDF_WHOLE_TIME.format(finalTime));
        return finalTime;
    }
}
