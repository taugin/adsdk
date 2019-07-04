package com.inner.adsdk.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.inner.adsdk.common.BaseConfig;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.AtConfig;
import com.inner.adsdk.config.CtConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.config.HtConfig;
import com.inner.adsdk.config.LtConfig;
import com.inner.adsdk.config.SpConfig;
import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.IParseListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.parse.AdParser;
import com.inner.adsdk.parse.IParser;
import com.inner.adsdk.request.IDataRequest;
import com.inner.adsdk.request.RemoteConfigRequest;
import com.inner.adsdk.utils.Utils;

import org.json.JSONObject;

import java.lang.reflect.Method;
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
        mHasInsightsSdk = hasInsightSdk();
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
        data = getString(DATA_CONFIG);
        data = checkLastData(data, DATA_CONFIG);
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
        String data = getString(key);
        data = checkLastData(data, key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPlace(data);
        }
        return null;
    }

    public GtConfig getRemoteGtPolicy() {
        String data = getString(Constant.GTPOLICY_NAME);
        data = checkLastData(data, Constant.GTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseGtPolicy(data);
        }
        return null;
    }

    public StConfig getRemoteStPolicy() {
        String data = getString(Constant.STPOLICY_NAME);
        data = checkLastData(data, Constant.STPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseStPolicy(data);
        }
        return null;
    }

    public AtConfig getRemoteAtPolicy() {
        String data = getString(Constant.ATPOLICY_NAME);
        data = checkLastData(data, Constant.ATPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAtPolicy(data);
        }
        return null;
    }

    public LtConfig getRemoteLtPolicy() {
        String data = getString(Constant.LTPOLICY_NAME);
        data = checkLastData(data, Constant.LTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseLtPolicy(data);
        }
        return null;
    }

    public HtConfig getRemoteHtPolicy() {
        String data = getString(Constant.HTPOLICY_NAME);
        data = checkLastData(data, Constant.HTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseHtPolicy(data);
        }
        return null;
    }

    public CtConfig getRemoteCtPolicy() {
        String data = getString(Constant.CTPOLICY_NAME);
        data = checkLastData(data, Constant.CTPOLICY_NAME);
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
        String data = getString(Constant.ADSPREAD_NAME);
        data = checkLastData(data, Constant.ADSPREAD_NAME);
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
                baseConfig.clear();
                mParser.parsePolicy(data, baseConfig, parserCallback);
            }
        }
    }

    /**
     * 与push过来的数据比对版本号，使用版本号高的数据，如果版本号相等，则使用推送的数据
     *
     * @param data 从firebase远程配置上获取的数据
     * @param key  当前配置的key值
     * @return
     * @deprecated
     */
    private String checkLastData2(String data, String key) {
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

    /**
     * 默认先从insights获取配置的数据，如果数据为空，则使用默认数据
     *
     * @param data
     * @param key
     * @return
     */
    private String checkLastData(String data, String key) {
        if (!isForbidFromInsights(mContext)) {
            String configText = getStringFromInsights(mContext, key);
            if (!TextUtils.isEmpty(configText)) {
                return configText;
            }
        }
        return data;
    }

    /**
     * 禁止从insights获取数据
     *
     * @param context
     * @return
     */
    private boolean isForbidFromInsights(Context context) {
        AdSwitch adSwitch = getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isForbidFromInsights();
        }
        return false;
    }

    /**
     * 从insights获取配置的数据
     *
     * @param context
     * @param key
     * @return
     */
    private String getStringFromInsights(Context context, String key) {
        if (!mHasInsightsSdk) {
            Log.iv(Log.TAG, "get string from insights not found : " + INSIGHTS_SDK);
            return null;
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName(INSIGHTS_SDK);
            Method method = clazz.getMethod(INSIGHTS_METHOD, Context.class, String.class);
            return (String) method.invoke(null, context, key);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv("--Insights--", "InsightsSDK get string error : " + error);
        }
        return null;
    }

    /**
     * 判断是否有InsightSdk存在
     *
     * @return
     */
    private boolean hasInsightSdk() {
        try {
            Class.forName(INSIGHTS_SDK);
            return true;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return false;
    }

    private static final String INSIGHTS_SDK = "we.studio.insights.InsightsSDK";
    private static final String INSIGHTS_METHOD = "getLatestConfig";
    private boolean mHasInsightsSdk = false;
}
