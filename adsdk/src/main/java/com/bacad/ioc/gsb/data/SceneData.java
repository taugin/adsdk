package com.bacad.ioc.gsb.data;

import android.content.Context;
import android.text.TextUtils;

import com.bacad.ioc.gsb.data.parse.SceneSceneParser;
import com.bacad.ioc.gsb.scconfig.CvCg;
import com.bacad.ioc.gsb.scconfig.GvCg;
import com.bacad.ioc.gsb.scconfig.HvCg;
import com.bacad.ioc.gsb.scconfig.LvCg;
import com.bacad.ioc.gsb.scconfig.SvCg;

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
        mSceneCg = new SceneCg(mContext);
    }

    private SceneCg mSceneCg;
    private Context mContext;
    private SceneSceneParser mParser;

    public GvCg getRemoteGtPolicy() {
        String data = getString(GvCg.GTPOLICY_NAME);
        data = checkLastData(data, GvCg.GTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseGtPolicy(data);
        }
        return null;
    }

    public SvCg getRemoteStPolicy() {
        String data = getString(SvCg.STPOLICY_NAME);
        data = checkLastData(data, SvCg.STPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseStPolicy(data);
        }
        return null;
    }

    public LvCg getRemoteLtPolicy() {
        String data = getString(LvCg.LTPOLICY_NAME);
        data = checkLastData(data, LvCg.LTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseLtPolicy(data);
        }
        return null;
    }

    public HvCg getRemoteHtPolicy() {
        String data = getString(HvCg.HTPOLICY_NAME);
        data = checkLastData(data, HvCg.HTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseHtPolicy(data);
        }
        return null;
    }

    public CvCg getRemoteCtPolicy() {
        String data = getString(CvCg.CTPOLICY_NAME);
        data = checkLastData(data, CvCg.CTPOLICY_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseCtPolicy(data);
        }
        return null;
    }

    public String getString(String key) {
        if (mSceneCg != null) {
            return mSceneCg.getString(key);
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
