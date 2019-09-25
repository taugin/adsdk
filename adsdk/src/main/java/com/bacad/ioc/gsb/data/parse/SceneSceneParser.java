package com.bacad.ioc.gsb.data.parse;

import android.text.TextUtils;

import com.bacad.ioc.gsb.common.BCg;
import com.bacad.ioc.gsb.scconfig.CvCg;
import com.bacad.ioc.gsb.scconfig.GvCg;
import com.bacad.ioc.gsb.scconfig.HvCg;
import com.bacad.ioc.gsb.scconfig.LvCg;
import com.bacad.ioc.gsb.scconfig.SvCg;
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

    private void parseBaseConfig(BCg BCg, JSONObject jobj) {
        if (BCg == null) {
            return;
        }
        try {
            if (jobj.has(ENABLE)) {
                BCg.setEnable(jobj.getInt(ENABLE) == 1);
            }
            if (jobj.has(UPDELAY)) {
                BCg.setUpDelay(jobj.getLong(UPDELAY));
            }
            if (jobj.has(INTERVAL)) {
                BCg.setInterval(jobj.getLong(INTERVAL));
            }
            if (jobj.has(MAX_COUNT)) {
                BCg.setMaxCount(jobj.getInt(MAX_COUNT));
            }
            if (jobj.has(MAX_VERSION)) {
                BCg.setMaxVersion(jobj.getInt(MAX_VERSION));
            }
            if (jobj.has(MIN_INTERVAL)) {
                BCg.setMinInterval(jobj.getLong(MIN_INTERVAL));
            }
            if (jobj.has(SCREEN_ORIENTATION)) {
                BCg.setScreenOrientation(jobj.getInt(SCREEN_ORIENTATION));
            }
            if (jobj.has(TIMEOUT)) {
                BCg.setTimeOut(jobj.getLong(TIMEOUT));
            }
            if (jobj.has(SHOW_BOTTOM)) {
                BCg.setShowBottom(jobj.getInt(SHOW_BOTTOM) == 1);
            }
            if (jobj.has(AD_EXTRA)) {
                BCg.setAdExtra(jobj.getString(AD_EXTRA));
            }
            if (jobj.has(AD_MAIN)) {
                BCg.setAdMain(jobj.getString(AD_MAIN));
            }
            if (jobj.has(SCENE_INTERVAL)) {
                BCg.setSceneInterval(jobj.getLong(SCENE_INTERVAL));
            }
            if (jobj.has(DELAY_CLOSE)) {
                BCg.setDelayClose(jobj.getLong(DELAY_CLOSE));
            }
            parseAttrConfig(BCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseBasePolicyInternal error : " + e);
        }
    }

    @Override
    public GvCg parseGtPolicy(String data) {
        data = getContent(data);
        return parseGtPolicyLocked(data);
    }

    private GvCg parseGtPolicyLocked(String content) {
        GvCg gvCg = null;
        try {
            JSONObject jobj = new JSONObject(content);
            gvCg = new GvCg();
            parseBaseConfig(gvCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseGtPolicyInternal error : " + e);
        }
        return gvCg;
    }

    @Override
    public SvCg parseStPolicy(String data) {
        data = getContent(data);
        return parseStPolicyLocked(data);
    }

    private SvCg parseStPolicyLocked(String content) {
        SvCg svCg = null;
        try {
            JSONObject jobj = new JSONObject(content);
            svCg = new SvCg();
            parseBaseConfig(svCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseStPolicyInternal error : " + e);
        }
        return svCg;
    }

    @Override
    public LvCg parseLtPolicy(String data) {
        data = getContent(data);
        return parseLtPolicyLocked(data);
    }

    private LvCg parseLtPolicyLocked(String data) {
        LvCg lvCg = null;
        try {
            JSONObject jobj = new JSONObject(data);
            lvCg = new LvCg();
            parseBaseConfig(lvCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseLtConfigInternal error : " + e);
        }
        return lvCg;
    }

    @Override
    public HvCg parseHtPolicy(String data) {
        data = getContent(data);
        return parseHtPolicyLocked(data);
    }

    private HvCg parseHtPolicyLocked(String data) {
        HvCg hvCg = null;
        try {
            JSONObject jobj = new JSONObject(data);
            hvCg = new HvCg();
            parseBaseConfig(hvCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseHtConfigInternal error : " + e);
        }
        return hvCg;
    }

    @Override
    public CvCg parseCtPolicy(String data) {
        data = getContent(data);
        return parseCtPolicyLocked(data);
    }

    private CvCg parseCtPolicyLocked(String data) {
        CvCg cvCg = null;
        try {
            JSONObject jobj = new JSONObject(data);
            cvCg = new CvCg();
            parseBaseConfig(cvCg, jobj);
            if (jobj.has(DISABLE_INTERVAL)) {
                cvCg.setDisableInterval(jobj.getLong(DISABLE_INTERVAL));
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseCtConfigInternal error : " + e);
        }
        return cvCg;
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
     * @param BCg
     * @param jobj
     */
    private void parseAttrConfig(BCg BCg, JSONObject jobj) {
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
                    BCg.setCountryList(list);
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
                    BCg.setAttrList(list);
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
                    BCg.setMediaList(list);
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
                    BCg.setVerList(list);
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "parseAttrConfig error : " + e);
        }
    }
}