package com.hauyu.adsdk.data;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.bac.ioc.gsb.scconfig.CtConfig;
import com.bac.ioc.gsb.scconfig.GtConfig;
import com.bac.ioc.gsb.scconfig.HtConfig;
import com.bac.ioc.gsb.scconfig.LtConfig;
import com.bac.ioc.gsb.scconfig.StConfig;
import com.gekes.fvs.tdsvap.SpConfig;
import com.hauyu.adsdk.common.BaseConfig;
import com.hauyu.adsdk.data.config.AdConfig;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.AdSwitch;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.IParseListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.data.parse.AdParser;
import com.hauyu.adsdk.data.parse.IParser;
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
    private AdConfig mLocalAdConfig;
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
        Log.iv(Log.TAG, "cfg : " + cfgName + ".[dat/json] , def : " + defName + ".[dat/json]");
        if (mLocalAdConfig == null && mParser != null) {
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
            mLocalAdConfig = mParser.parseAdConfig(data);
            if (mLocalAdConfig != null) {
                mLocalAdConfig.setAdConfigMd5(Utils.string2MD5(data));
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
                && (mLocalAdConfig == null || !TextUtils.equals(mLocalAdConfig.getAdConfigMd5(), Utils.string2MD5(data)))) {
            if (mParser != null) {
                mLocalAdConfig = mParser.parseAdConfig(data);
                if (mLocalAdConfig != null) {
                    mLocalAdConfig.setAdConfigMd5(Utils.string2MD5(data));
                    Log.iv(Log.TAG, "remote data has been set success");
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

    public GtConfig getRemoteGtPolicy() {
        String data = getString(GtConfig.GTPOLICY_NAME);
        data = checkLastData(data, GtConfig.GTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseGtPolicy(data);
        }
        return null;
    }

    public StConfig getRemoteStPolicy() {
        String data = getString(StConfig.STPOLICY_NAME);
        data = checkLastData(data, StConfig.STPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseStPolicy(data);
        }
        return null;
    }

    public LtConfig getRemoteLtPolicy() {
        String data = getString(LtConfig.LTPOLICY_NAME);
        data = checkLastData(data, LtConfig.LTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseLtPolicy(data);
        }
        return null;
    }

    public HtConfig getRemoteHtPolicy() {
        String data = getString(HtConfig.HTPOLICY_NAME);
        data = checkLastData(data, HtConfig.HTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseHtPolicy(data);
        }
        return null;
    }

    public CtConfig getRemoteCtPolicy() {
        String data = getString(CtConfig.CTPOLICY_NAME);
        data = checkLastData(data, CtConfig.CTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseCtPolicy(data);
        }
        return null;
    }

    public Map<String, String> getRemoteAdIds() {
        String data = getString(Constant.ADIDS_NAME);
        data = checkLastData(data, Constant.ADIDS_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdIds(data);
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
        if (mAdSwitch == null && mLocalAdConfig != null) {
            mAdSwitch = mLocalAdConfig.getAdSwitch();
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
        String data = getString(SpConfig.ADSPREAD_NAME);
        data = checkLastData(data, SpConfig.ADSPREAD_NAME);
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

    public void parseRemotePolicy(BaseConfig baseConfig, IParseListener parserCallback) {
        if (baseConfig != null) {
            String name = baseConfig.getName();
            if (TextUtils.isEmpty(name)) {
                Log.iv(Log.TAG, "can not find cfg name");
                return;
            }
            String data = getString(name);
            data = checkLastData(data, name);
            if (!TextUtils.isEmpty(data)) {
                mParser.parsePolicy(data, baseConfig, parserCallback);
            }
        }
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
