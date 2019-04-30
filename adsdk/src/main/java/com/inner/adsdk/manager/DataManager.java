package com.inner.adsdk.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.AtConfig;
import com.inner.adsdk.config.BaseConfig;
import com.inner.adsdk.config.CtConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.config.HtConfig;
import com.inner.adsdk.config.LtConfig;
import com.inner.adsdk.config.SpConfig;
import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.parse.AdParser;
import com.inner.adsdk.parse.IParser;
import com.inner.adsdk.parse.IParseExtra;
import com.inner.adsdk.request.IDataRequest;
import com.inner.adsdk.request.RemoteConfigRequest;
import com.inner.adsdk.utils.Utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager {

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

    public void init() {
        parseLocalData();
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
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAdPlace(data);
            }
        }
        return null;
    }

    public GtConfig getRemoteGtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.GTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseGtPolicy(data);
            }
        }
        return null;
    }

    public StConfig getRemoteStPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.STPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseStPolicy(data);
            }
        }
        return null;
    }

    public AtConfig getRemoteAtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ATPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseAtPolicy(data);
            }
        }
        return null;
    }

    public LtConfig getRemoteLtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.LTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseLtPolicy(data);
            }
        }
        return null;
    }

    public HtConfig getRemoteHtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.HTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseHtPolicy(data);
            }
        }
        return null;
    }

    public CtConfig getRemoteCtPolicy() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.CTPOLICY_NAME);
            if (!TextUtils.isEmpty(data)) {
                return mParser.parseCtPolicy(data);
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
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADSWITCH_NAME);
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
            Log.pv(Log.TAG, "ads : " + mAdSwitch);
        }
        return mAdSwitch;
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

    public List<SpConfig> getRemoteSpread() {
        if (mDataRequest != null) {
            String data = mDataRequest.getString(Constant.ADSPREAD_NAME);
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

    public void parseRemotePolicy(BaseConfig baseConfig, IParseExtra parserCallback) {
        if (mDataRequest != null && baseConfig != null) {
            String name = baseConfig.getName();
            if (TextUtils.isEmpty(name)) {
                Log.pv(Log.TAG, "can not find cfg name");
                return;
            }
            String data = mDataRequest.getString(name);
            if (!TextUtils.isEmpty(data)) {
                baseConfig.clear();
                mParser.parsePolicy(data, baseConfig, parserCallback);
            }
        }
    }
}
