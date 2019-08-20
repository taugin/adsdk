package com.bacad.ioc.gsb.data;

import android.content.Context;
import android.text.TextUtils;

import com.bacad.ioc.gsb.data.parse.SceneSceneParser;
import com.bacad.ioc.gsb.scconfig.CtConfig;
import com.bacad.ioc.gsb.scconfig.GtConfig;
import com.bacad.ioc.gsb.scconfig.HtConfig;
import com.bacad.ioc.gsb.scconfig.LtConfig;
import com.bacad.ioc.gsb.scconfig.StConfig;

/**
 * Created by Administrator on 2018/2/12.
 */

public class SceneData {

    private static SceneData sSceneData;

    public static SceneData get(Context context) {
        synchronized (SceneData.class) {
            if (sSceneData == null) {
                createInstance(context);
            }
        }
        return sSceneData;
    }

    private static void createInstance(Context context) {
        synchronized (SceneData.class) {
            if (sSceneData == null) {
                sSceneData = new SceneData(context);
            }
        }
    }

    private SceneData(Context context) {
        mContext = context;
        mParser = new SceneSceneParser();
        mSceneConfig = new SceneConfig(mContext);
    }

    private SceneConfig mSceneConfig;
    private Context mContext;
    private SceneSceneParser mParser;

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

    public String getString(String key) {
        if (mSceneConfig != null) {
            return mSceneConfig.getString(key);
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
