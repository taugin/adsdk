package com.bacad.ioc.gsb.data.parse;

import android.text.TextUtils;

import com.bacad.ioc.gsb.base.BCg;
import com.bacad.ioc.gsb.base.ScFl;
import com.bacad.ioc.gsb.scconfig.CvCg;
import com.bacad.ioc.gsb.scconfig.GvCg;
import com.bacad.ioc.gsb.scconfig.HvCg;
import com.bacad.ioc.gsb.scconfig.LvCg;
import com.bacad.ioc.gsb.scconfig.SvCg;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.utils.AesUtils;
import com.hauyu.adsdk.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        return AesUtils.decrypt(Constant.KEY_PASSWORD, content);
    }

    private void parseBaseConfig(BCg bCg, JSONObject jobj) {
        if (bCg == null) {
            return;
        }
        try {
            if (jobj.has(ENABLE)) {
                bCg.setEnable(jobj.getInt(ENABLE) == 1);
            }
            if (jobj.has(UPDELAY)) {
                bCg.setUpDelay(jobj.getLong(UPDELAY));
            }
            if (jobj.has(INTERVAL)) {
                bCg.setInterval(jobj.getLong(INTERVAL));
            }
            if (jobj.has(MAX_COUNT)) {
                bCg.setMaxCount(jobj.getInt(MAX_COUNT));
            }
            if (jobj.has(MAX_VERSION)) {
                bCg.setMaxVersion(jobj.getInt(MAX_VERSION));
            }
            if (jobj.has(MIN_INTERVAL)) {
                bCg.setMinInterval(jobj.getLong(MIN_INTERVAL));
            }
            if (jobj.has(SCREEN_ORIENTATION)) {
                bCg.setScreenOrientation(jobj.getInt(SCREEN_ORIENTATION));
            }
            if (jobj.has(TIMEOUT)) {
                bCg.setTimeOut(jobj.getLong(TIMEOUT));
            }
            if (jobj.has(SHOW_BOTTOM)) {
                bCg.setShowBottom(jobj.getInt(SHOW_BOTTOM) == 1);
            }
            if (jobj.has(AD_MAIN)) {
                bCg.setAdMain(jobj.getString(AD_MAIN));
            }
            if (jobj.has(SCENE_INTERVAL)) {
                bCg.setSceneInterval(jobj.getLong(SCENE_INTERVAL));
            }
            if (jobj.has(DELAY_CLOSE)) {
                bCg.setDelayClose(jobj.getLong(DELAY_CLOSE));
            }
            if (jobj.has(OS_VER)) {
                bCg.setOsVer(parseIntegerList(jobj.getString(OS_VER)));
            }
            if (jobj.has(USE_REAL_TIME)) {
                bCg.setUseRealTime(jobj.getInt(USE_REAL_TIME) == 1);
            }
            if (jobj.has(FAT_ADM)) {
                bCg.setFatAdm(parseFatAdmMap(jobj.getJSONObject(FAT_ADM)));
            }
            if (jobj.has(VER_ADM)) {
                bCg.setVerAdm(parseVerAdmMap(jobj.getJSONObject(VER_ADM)));
            }
            if (jobj.has(USE_FULL_INTENT)) {
                bCg.setUseFullIntent(jobj.getInt(USE_FULL_INTENT) == 1);
            }
            parseFieldList(bCg, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseBasePolicyInternal error : " + e);
        }
    }

    private Map<Long, String> parseFatAdmMap(JSONObject jobj) {
        Map<Long, String> fatAdm = null;
        try {
            if (jobj != null) {
                Iterator<String> keys = jobj.keys();
                if (keys != null && keys.hasNext()) {
                    fatAdm = new TreeMap<Long, String>(new Comparator<Long>() {
                        @Override
                        public int compare(Long o1, Long o2) {
                            if (o1 - o2 < 0) {
                                return 1;
                            } else if (o1 - o2 > 0) {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    while(keys.hasNext()) {
                        String key = keys.next();
                        String value = jobj.getString(key);
                        Long fatKey = parseLong(key);
                        if (fatKey != null && fatKey > 0) {
                            fatAdm.put(fatKey, value);
                        } else {
                            Log.v(Log.TAG, key + " is not long type");
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return fatAdm;
    }

    private Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
        }
        return null;
    }

    private Map<String, String> parseVerAdmMap(JSONObject jobj) {
        Map<String, String> verAdm = null;
        try {
            if (jobj != null) {
                Iterator<String> keys = jobj.keys();
                if (keys != null && keys.hasNext()) {
                    verAdm = new LinkedHashMap<String, String>();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        String value = jobj.getString(key);
                        verAdm.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
        }
        return verAdm;
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
            if (jobj.has(LT_TYPE)) {
                lvCg.setLtType(parseStringList(jobj.getString(LT_TYPE)));
            }
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

    private List<Integer> parseIntegerList(String str) {
        List<Integer> list = null;
        try {
            JSONArray jarray = new JSONArray(str);
            if (jarray != null && jarray.length() > 0) {
                list = new ArrayList<Integer>(jarray.length());
                for (int index = 0; index < jarray.length(); index++) {
                    Integer s = jarray.getInt(index);
                    if (s != null) {
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
     * @param bCg
     * @param jobj
     */
    private void parseFieldList(BCg bCg, JSONObject jobj) {
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
                    bCg.setCountryList(list);
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
                    bCg.setAttrList(list);
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
                    bCg.setMediaList(list);
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
                    bCg.setVerList(list);
                }
            }
            if (jobj.has(EX_IPS)) {
                JSONArray jarray = jobj.getJSONArray(EX_IPS);
                if (jarray != null && jarray.length() > 0) {
                    List<String> list = new ArrayList<String>(jarray.length());
                    for (int index = 0; index < jarray.length(); index++) {
                        String s = jarray.getString(index);
                        if (!TextUtils.isEmpty(s)) {
                            list.add(s);
                        }
                    }
                    bCg.setExIps(list);
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "parseAttrConfig error : " + e);
        }
    }

    @Override
    public ScFl parseSceneFlag(String content) {
        ScFl scFl = null;
        try {
            content = getContent(content);
            JSONObject jobj = new JSONObject(content);
            scFl = new ScFl();
            if (jobj.has(REPORT_UMENG)) {
                scFl.setReportUmeng(jobj.getInt(REPORT_UMENG) == 1);
            }
            if (jobj.has(REPORT_FIREBASE)) {
                scFl.setReportFirebase(jobj.getInt(REPORT_FIREBASE) == 1);
            }
            if (jobj.has(REPORT_FACEBOOK)) {
                scFl.setReportFacebook(jobj.getInt(REPORT_FACEBOOK) == 1);
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseScFl error : " + e);
        }
        return scFl;
    }
}