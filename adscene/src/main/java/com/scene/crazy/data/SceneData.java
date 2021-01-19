package com.scene.crazy.data;

import android.content.Context;
import android.text.TextUtils;

import com.scene.crazy.base.ScFl;
import com.scene.crazy.data.parse.SceneSceneParser;
import com.scene.crazy.scconfig.CvCg;
import com.scene.crazy.scconfig.GvCg;
import com.scene.crazy.scconfig.HvCg;
import com.scene.crazy.scconfig.LvCg;
import com.scene.crazy.scconfig.SvCg;
import com.rabbit.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018/2/12.
 */

public class SceneData {

    private static SceneData sSceneData;
    private static final String PREF_SCENE_FLAG = "pref_scene_flag";

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
    private ScFl mScFl;

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

    public ScFl getScFl() {
        String data = getString(ScFl.SCFL_NAME);
        if (!TextUtils.isEmpty(data)) {
            String oldSwitchMd5 = Utils.getString(mContext, PREF_SCENE_FLAG);
            String newSwitchMd5 = Utils.string2MD5(data);
            if (mScFl == null || !TextUtils.equals(oldSwitchMd5, newSwitchMd5)) {
                mScFl = mParser.parseSceneFlag(data);
                Utils.putString(mContext, PREF_SCENE_FLAG, newSwitchMd5);
            }
        }
        return mScFl;
    }
}
