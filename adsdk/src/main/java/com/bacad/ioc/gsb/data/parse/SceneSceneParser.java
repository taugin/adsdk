package com.bacad.ioc.gsb.data.parse;

import android.text.TextUtils;

import com.bacad.ioc.gsb.scconfig.CtConfig;
import com.bacad.ioc.gsb.scconfig.GtConfig;
import com.bacad.ioc.gsb.scconfig.HtConfig;
import com.bacad.ioc.gsb.scconfig.LtConfig;
import com.bacad.ioc.gsb.scconfig.StConfig;
import com.bacad.ioc.gsb.common.BaseConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.Aes;
import com.hauyu.adsdk.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27.
 */

public class SceneSceneParser implements ISceneParser {

    private String getContent(String content) {
        JSONObject jobj = null;
        JSONArray jarray = null;
        try {
            jobj = new JSONObject(content);
        } catch (Exception e) {
            Log.v(Log.TAG, "error : not a json object");
        }
        if (jobj != null) {
            return jobj.toString();
        }
        try {
            jarray = new JSONArray(content);
        } catch (Exception e) {
            Log.v(Log.TAG, "error : not a json array");
        }
        if (jarray != null) {
            return jarray.toString();
        }
        return Aes.decrypt(Constant.KEY_PASSWORD, content);
    }

    private void parseBaseConfig(BaseConfig baseConfig, JSONObject jobj) {
        if (baseConfig == null) {
            return;
        }
        try {
            if (jobj.has(ENABLE)) {
                baseConfig.setEnable(jobj.getInt(ENABLE) == 1);
            }
            if (jobj.has(UPDELAY)) {
                baseConfig.setUpDelay(jobj.getLong(UPDELAY));
            }
            if (jobj.has(INTERVAL)) {
                baseConfig.setInterval(jobj.getLong(INTERVAL));
            }
            if (jobj.has(MAX_COUNT)) {
                baseConfig.setMaxCount(jobj.getInt(MAX_COUNT));
            }
            if (jobj.has(MAX_VERSION)) {
                baseConfig.setMaxVersion(jobj.getInt(MAX_VERSION));
            }
            if (jobj.has(MIN_INTERVAL)) {
                baseConfig.setMinInterval(jobj.getLong(MIN_INTERVAL));
            }
            if (jobj.has(SCREEN_ORIENTATION)) {
                baseConfig.setScreenOrientation(jobj.getInt(SCREEN_ORIENTATION));
            }
            if (jobj.has(TIMEOUT)) {
                baseConfig.setTimeOut(jobj.getLong(TIMEOUT));
            }
            if (jobj.has(CONFIG_INSTALL_TIME)) {
                baseConfig.setConfigInstallTime(jobj.getLong(CONFIG_INSTALL_TIME));
            }
            if (jobj.has(SHOW_BOTTOM_ACTIVITY)) {
                baseConfig.setShowBottomActivity(jobj.getInt(SHOW_BOTTOM_ACTIVITY) == 1);
            }
            if (jobj.has(PLACE_NAME_INT)) {
                baseConfig.setPlaceNameInt(jobj.getString(PLACE_NAME_INT));
            }
            if (jobj.has(PLACE_NAME_ADV)) {
                baseConfig.setPlaceNameAdv(jobj.getString(PLACE_NAME_ADV));
            }
            if (jobj.has(SCENE_INTERVAL)) {
                baseConfig.setSceneInterval(jobj.getLong(SCENE_INTERVAL));
            }
            parseAttrConfig(baseConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseBasePolicyInternal error : " + e);
        }
    }

    @Override
    public GtConfig parseGtPolicy(String data) {
        data = getContent(data);
        return parseGtPolicyLocked(data);
    }

    private GtConfig parseGtPolicyLocked(String content) {
        GtConfig gtConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            gtConfig = new GtConfig();
            parseBaseConfig(gtConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseGtPolicyInternal error : " + e);
        }
        return gtConfig;
    }

    @Override
    public StConfig parseStPolicy(String data) {
        data = getContent(data);
        return parseStPolicyLocked(data);
    }

    private StConfig parseStPolicyLocked(String content) {
        StConfig stConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            stConfig = new StConfig();
            parseBaseConfig(stConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseStPolicyInternal error : " + e);
        }
        return stConfig;
    }

    @Override
    public LtConfig parseLtPolicy(String data) {
        data = getContent(data);
        return parseLtPolicyLocked(data);
    }

    private LtConfig parseLtPolicyLocked(String data) {
        LtConfig ltConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            ltConfig = new LtConfig();
            parseBaseConfig(ltConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseLtConfigInternal error : " + e);
        }
        return ltConfig;
    }

    @Override
    public HtConfig parseHtPolicy(String data) {
        data = getContent(data);
        return parseHtPolicyLocked(data);
    }

    private HtConfig parseHtPolicyLocked(String data) {
        HtConfig htConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            htConfig = new HtConfig();
            parseBaseConfig(htConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseHtConfigInternal error : " + e);
        }
        return htConfig;
    }

    @Override
    public CtConfig parseCtPolicy(String data) {
        data = getContent(data);
        return parseCtPolicyLocked(data);
    }

    private CtConfig parseCtPolicyLocked(String data) {
        CtConfig ctConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            ctConfig = new CtConfig();
            parseBaseConfig(ctConfig, jobj);
            if (jobj.has(DISABLE_INTERVAL)) {
                ctConfig.setDisableInterval(jobj.getLong(DISABLE_INTERVAL));
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseCtConfigInternal error : " + e);
        }
        return ctConfig;
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
     * 解析归因配置
     *
     * @param baseConfig
     * @param jobj
     */
    private void parseAttrConfig(BaseConfig baseConfig, JSONObject jobj) {
        try {
            if (jobj.has(COUNTRY_LIST)) {
                JSONArray jarray = jobj.getJSONArray(COUNTRY_LIST);
                if (jarray != null && jarray.length() > 0) {
                    List<String> list = new ArrayList<String>(jarray.length());
                    for (int index = 0; index < jarray.length(); index++) {
                        String s = jarray.getString(index);
                        if (!TextUtils.isEmpty(s)) {
                            list.add(s);
                        }
                    }
                    baseConfig.setCountryList(list);
                }
            }
            if (jobj.has(ATTRS)) {
                JSONArray jarray = jobj.getJSONArray(ATTRS);
                if (jarray != null && jarray.length() > 0) {
                    List<String> list = new ArrayList<String>(jarray.length());
                    for (int index = 0; index < jarray.length(); index++) {
                        String s = jarray.getString(index);
                        if (!TextUtils.isEmpty(s)) {
                            list.add(s);
                        }
                    }
                    baseConfig.setAttrList(list);
                }
            }
            if (jobj.has(MEDIA_SOURCE)) {
                JSONArray jarray = jobj.getJSONArray(MEDIA_SOURCE);
                if (jarray != null && jarray.length() > 0) {
                    List<String> list = new ArrayList<String>(jarray.length());
                    for (int index = 0; index < jarray.length(); index++) {
                        String s = jarray.getString(index);
                        if (!TextUtils.isEmpty(s)) {
                            list.add(s);
                        }
                    }
                    baseConfig.setMediaList(list);
                }
            }
            if (jobj.has(VER_LIST)) {
                JSONArray jarray = jobj.getJSONArray(VER_LIST);
                if (jarray != null && jarray.length() > 0) {
                    List<String> list = new ArrayList<String>(jarray.length());
                    for (int index = 0; index < jarray.length(); index++) {
                        String s = jarray.getString(index);
                        if (!TextUtils.isEmpty(s)) {
                            list.add(s);
                        }
                    }
                    baseConfig.setVerList(list);
                }
            }
            if (jobj.has(NTRATE)) {
                baseConfig.setNtr(jobj.getInt(NTRATE));
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "parseAttrConfig error : " + e);
        }
    }
}