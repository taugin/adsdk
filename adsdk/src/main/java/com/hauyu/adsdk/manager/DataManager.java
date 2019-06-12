package com.hauyu.adsdk.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.AdPlace;
import com.hauyu.adsdk.config.AdSwitch;
import com.hauyu.adsdk.config.AtConfig;
import com.hauyu.adsdk.common.BaseConfig;
import com.hauyu.adsdk.config.CtConfig;
import com.hauyu.adsdk.config.GtConfig;
import com.hauyu.adsdk.config.HtConfig;
import com.hauyu.adsdk.config.LtConfig;
import com.hauyu.adsdk.config.SpConfig;
import com.hauyu.adsdk.config.StConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.parse.AdParser;
import com.hauyu.adsdk.listener.IParseListener;
import com.hauyu.adsdk.parse.IParser;
import com.hauyu.adsdk.request.IDataRequest;
import com.hauyu.adsdk.request.RemoteConfigRequest;
import com.hauyu.adsdk.utils.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager implements Runnable {

    private static final String DATA_CONFIG_FORMAT = "data_%s.dat";
    private static final String DATA_CONFIG = "cfg_data_config";
    private static final String PREF_ADSWITCH_MD5 = "pref_adswitch_md5";
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
    private AdSwitch mAdSwitch;
    private Handler mHandler = new Handler();

    public void init() {
        parseLocalData();
        if (mDataRequest == null) {
            mDataRequest = new RemoteConfigRequest(mContext);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 5000);
        }
    }

    public void refresh() {
        if (mDataRequest != null) {
            mDataRequest.refresh();
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
        Log.v(Log.TAG, "cfg : " + cfgName + " , def : " + defName);
        if (mLocalAdConfig == null && mParser != null) {
            String data = Utils.readAssets(mContext, cfgName);
            if (TextUtils.isEmpty(data)) {
                data = Utils.readAssets(mContext, defName);
            }
            mLocalAdConfig = mParser.parseAdConfig(data);
            if (mLocalAdConfig != null) {
                mLocalAdConfig.setAdConfigMd5(Utils.string2MD5(data));
                Log.v(Log.TAG, "use lo data");
            }
        }
    }

    private void parseRemoteData() {
        Log.v(Log.TAG, "parse re data");
        String data = null;
        if (mDataRequest != null) {
            data = mDataRequest.getString(DATA_CONFIG);
        }
        if (!TextUtils.isEmpty(data)
                && (mLocalAdConfig == null || !TextUtils.equals(mLocalAdConfig.getAdConfigMd5(), Utils.string2MD5(data)))) {
            if (mParser != null) {
                mLocalAdConfig = mParser.parseAdConfig(data);
                if (mLocalAdConfig != null) {
                    mLocalAdConfig.setAdConfigMd5(Utils.string2MD5(data));
                    Log.v(Log.TAG, "use re data");
                }
            }
        }
    }

    public AdConfig getAdConfig() {
        parseRemoteData();
        parseLocalData();
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
        return String.format(Locale.getDefault(), DATA_CONFIG_FORMAT, "config");
    }

    public AdPlace getRemoteAdPlace(String key) {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(key);
            data = checkLastData(data, key);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdPlace(data);
            }
        }
        return null;
    }

    public GtConfig getRemoteGtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.GTPOLICY_NAME);
            data = checkLastData(data, Constant.GTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseGtPolicy(data);
            }
        }
        return null;
    }

    public StConfig getRemoteStPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.STPOLICY_NAME);
            data = checkLastData(data, Constant.STPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseStPolicy(data);
            }
        }
        return null;
    }

    public AtConfig getRemoteAtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ATPOLICY_NAME);
            data = checkLastData(data, Constant.ATPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAtPolicy(data);
            }
        }
        return null;
    }

    public LtConfig getRemoteLtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.LTPOLICY_NAME);
            data = checkLastData(data, Constant.LTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseLtPolicy(data);
            }
        }
        return null;
    }

    public HtConfig getRemoteHtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.HTPOLICY_NAME);
            data = checkLastData(data, Constant.HTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseHtPolicy(data);
            }
        }
        return null;
    }

    public CtConfig getRemoteCtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.CTPOLICY_NAME);
            data = checkLastData(data, Constant.CTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseCtPolicy(data);
            }
        }
        return null;
    }

    public Map<String, String> getRemoteAdIds() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADIDS_NAME);
            data = checkLastData(data, Constant.ADIDS_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdIds(data);
            }
        }
        return null;
    }

    public AdSwitch getAdSwitch() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADSWITCH_NAME);
            data = checkLastData(data, Constant.ADSWITCH_NAME);
            if (!TextUtils.isEmpty(data)) {
                String oldSwitchMd5 = Utils.getString(mContext, PREF_ADSWITCH_MD5);
                String newSwitchMd5 = Utils.string2MD5(data);
                if (mAdSwitch == null || !TextUtils.equals(oldSwitchMd5, newSwitchMd5)) {
                    mAdSwitch = mParser.parseAdSwitch(data);
                    Utils.putString(mContext, PREF_ADSWITCH_MD5, newSwitchMd5);
                }
            }
            if (mAdSwitch == null && mLocalAdConfig != null) {
                mAdSwitch = mLocalAdConfig.getAdSwitch();
            }
            Log.iv(Log.TAG, "ads : " + mAdSwitch);
        }
        return mAdSwitch;
    }

    public Map<String, String> getRemoteAdRefs() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADREFS_NAME);
            data = checkLastData(data, Constant.ADREFS_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdRefs(data);
            }
        }
        return null;
    }

    public List<SpConfig> getRemoteSpread() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADSPREAD_NAME);
            data = checkLastData(data, Constant.ADSPREAD_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseSpread(data);
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

    public void parseRemotePolicy(BaseConfig baseConfig, IParseListener parserCallback) {
        if (mDataRequest != null && baseConfig != null) {
            String name = baseConfig.getName();
            if (TextUtils.isEmpty(name)) {
                Log.iv(Log.TAG, "can not find cfg name");
                return;
            }
            String data = mDataRequest.getString(name);
            data = checkLastData(data, name);
            if (!TextUtils.isEmpty(data)) {
                mParser.parsePolicy(data, baseConfig, parserCallback);
            }
        }
    }

    /**
     * 与push过来的数据比对版本号，使用版本号高的数据，如果版本号相等，则使用推送的数据
     * @param data 从firebase远程配置上获取的数据
     * @param key 当前配置的key值
     * @return
     */
    private String checkLastData(String data, String key) {
        boolean useConfig = true;
        String pushData = Utils.getString(mContext, Constant.AD_SDK_PUSH_PREFIX + key);
        if (TextUtils.isEmpty(data) && !TextUtils.isEmpty(pushData)) {
            // 直接使用push配置数据
            useConfig = false;
        } else if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(pushData)) {
            // 比较两种配置数据的版本，使用高版本的数据
            int pushVer = -1;
            int confVer = -1;
            // 读取推送过来的数据的版本号
            try {
                JSONObject jobj = new JSONObject(pushData);
                if (jobj.has(Constant.AD_SDK_JSON_VER)) {
                    pushVer = jobj.getInt(Constant.AD_SDK_JSON_VER);
                }
            } catch (Exception e) {
            }
            // 读取配置的数据的版本号
            try {
                JSONObject jobj = new JSONObject(data);
                if (jobj.has(Constant.AD_SDK_JSON_VER)) {
                    confVer = jobj.getInt(Constant.AD_SDK_JSON_VER);
                }
            } catch (Exception e) {
            }
            Log.v(Log.TAG, "confVer : " + confVer + " , pushVer : " + pushVer);
            useConfig = confVer >= pushVer;
        } else {
            // 其他情况使用配置的数据
            useConfig = true;
        }
        Log.iv(Log.TAG, "use " + (useConfig ? "config" : "push") + " data for " + key);
        return useConfig ? data : pushData;
    }
}
