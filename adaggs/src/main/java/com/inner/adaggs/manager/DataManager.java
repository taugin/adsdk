package com.inner.adaggs.manager;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.config.DevInfo;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.framework.Aes;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.parse.AdParser;
import com.inner.adaggs.parse.IParser;
import com.inner.adaggs.request.GTagDataRequest;
import com.inner.adaggs.request.HttpDataRequest;
import com.inner.adaggs.request.IDataRequest;
import com.inner.adaggs.request.OnDataListener;
import com.inner.adaggs.utils.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager implements OnDataListener {
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
    }

    private IDataRequest mDataRequest;
    private Context mContext;
    private String mUrl;
    private String mContainerId;
    private AdConfig mAdConfig;
    private IParser mIParser;

    public void init() {
        mIParser = new AdParser();
        parseConfig();
        if (!TextUtils.isEmpty(mContainerId)) {
            mDataRequest = new GTagDataRequest(mContext, mContainerId);
        } else if (!TextUtils.isEmpty(mUrl)) {
            mDataRequest = new HttpDataRequest(mContext, mUrl);
        } else {
            Log.d(Log.TAG, "no container or url configed");
        }
        if (mDataRequest != null) {
            mDataRequest.setOnDataListener(this);
            mDataRequest.request();
        }
    }

    private void parseConfig() {
        String configContent = Utils.readAssets(mContext, "data_config.dat");
        String decryptContent = null;
        if (!TextUtils.isEmpty(configContent)) {
            decryptContent = Aes.decrypt(configContent, Constant.KEY_PASSWORD);
        }
        decryptContent = configContent;
        if (!TextUtils.isEmpty(decryptContent)) {
            try {
                JSONObject jobj = new JSONObject(decryptContent);
                if (jobj.has("container_id")) {
                    mContainerId = jobj.getString("container_id");
                }
                if (jobj.has("url")) {
                    mUrl = jobj.getString("url");
                }
            } catch (Exception e) {
            }
        }
    }

    public AdConfig getAdConfig() {
        if (mAdConfig == null) {
            long status = Utils.getLong(mContext, "content_status", 0);
            String data = Utils.getString(mContext, "content_data", null);
            if (!TextUtils.isEmpty(data)) {
                parseAdConfig(status, data);
            } else {
                data = Utils.readAssets(mContext, "adconfig.dat");
                processData(data);
            }
        }
        return mAdConfig;
    }

    @Override
    public void onData(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        processData(data);
    }

    private void processData(String data) {
        int status = mIParser.parseStatus(data);
        List<DevInfo> list = mIParser.parseDevList(data);
        String content = mIParser.parseContent(data);
        saveContent(status, list, content);
        parseAdConfig(status, content);
    }

    private void parseAdConfig(long status, String content) {
        String adContent = null;
        if (status == 1) {
            adContent = Aes.decrypt(content, Constant.KEY_PASSWORD);
        } else {
            adContent = content;
        }
        mAdConfig = mIParser.parse(adContent);
    }

    private void saveContent(int status, List<DevInfo> list, String content) {
        if (deviceInWhiteList(list)) {
            return;
        }
        if (status > 0) {
            Utils.putLong(mContext, "content_status", status);
        }
        if (!TextUtils.isEmpty(content)) {
            Utils.putString(mContext, "content_data", content);
        }
    }

    private boolean deviceInWhiteList(List<DevInfo> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        String imei = Utils.getImei(mContext);
        String aId = Utils.getAId(mContext);
        for (DevInfo info : list) {
            if (info != null && TextUtils.equals(imei, info.getImei())) {
                return true;
            }
            if (info != null && TextUtils.equals(aId, info.getAndroidId())) {
                return true;
            }
        }
        return false;
    }

    public AdPlace getAdPlace(String key) {
        return null;
    }

    public AdPolicy getAdPolicy(String key) {
        return null;
    }

    public Map<String, String> getAdIds(String key) {
        return null;
    }
}
