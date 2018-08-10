package com.inner.adsdk.parse;

import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AttrConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Aes;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdParser implements IParser {

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

    @Override
    public AdConfig parseAdConfig(String data) {
        data = getContent(data);
        AdConfig adconfig = parseAdConfigInternal(data);
        return adconfig;
    }

    private AdConfig parseAdConfigInternal(String data) {
        AdConfig adConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            Map<String, String> adIds = null;
            GtConfig gtConfig = null;
            StConfig stConfig = null;
            List<AdPlace> adPlaces = null;
            AdSwitch adSwitch = null;
            Map<String, String> adrefs = null;
            if (jobj.has(ADIDS)) {
                adIds = parseAdIds(jobj.getString(ADIDS));
            }
            if (jobj.has(GTCONFIG)) {
                gtConfig = parseGtPolicyInternal(jobj.getString(GTCONFIG));
            }
            if (jobj.has(STCONFIG)) {
                stConfig = parseStPolicyInternal(jobj.getString(STCONFIG));
            }
            if (jobj.has(ADPLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (jobj.has(ADSWITCH)) {
                adSwitch = parseAdSwitch(jobj.getString(ADSWITCH));
            }
            if (jobj.has(ADREFS)) {
                adrefs = parseAdRefs(jobj.getString(ADREFS));
            }
            if (adPlaces != null || gtConfig != null
                    || adIds != null || adSwitch != null
                    || adrefs != null || stConfig != null) {
                adConfig = new AdConfig();
                adConfig.setAdPlaceList(adPlaces);
                adConfig.setGtConfig(gtConfig);
                adConfig.setAdIds(adIds);
                adConfig.setAdSwitch(adSwitch);
                adConfig.setAdRefs(adrefs);
                adConfig.setStConfig(stConfig);
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdConfigInternal error : " + e);
        }
        return adConfig;
    }

    @Override
    public Map<String, String> parseAdIds(String content) {
        Map<String, String> adIds = null;
        try {
            content = getContent(content);
            JSONObject jobj = new JSONObject(content);
            adIds = new HashMap<String, String>();
            Iterator<String> iterator = jobj.keys();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = iterator.next();
                value = jobj.getString(key);
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    adIds.put(key, value);
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdIds error : " + e);
        }
        return adIds;
    }

    @Override
    public GtConfig parseGtPolicy(String data) {
        data = getContent(data);
        return parseGtPolicyInternal(data);
    }

    private GtConfig parseGtPolicyInternal(String content) {
        GtConfig gtConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            gtConfig = new GtConfig();
            if (jobj.has(ENABLE)) {
                gtConfig.setEnable(jobj.getInt(ENABLE) == 1);
            }
            if (jobj.has(UPDELAY)) {
                gtConfig.setUpDelay(jobj.getLong(UPDELAY));
            }
            if (jobj.has(INTERVAL)) {
                gtConfig.setInterval(jobj.getLong(INTERVAL));
            }
            if (jobj.has(MAX_COUNT)) {
                gtConfig.setMaxCount(jobj.getInt(MAX_COUNT));
            }
            if (jobj.has(MAX_VERSION)) {
                gtConfig.setMaxVersion(jobj.getInt(MAX_VERSION));
            }
            if (jobj.has(MIN_INTERVAL)) {
                gtConfig.setMinInterval(jobj.getLong(MIN_INTERVAL));
            }
            parseAttrConfig(gtConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseGtPolicyInternal error : " + e);
        }
        return gtConfig;
    }

    @Override
    public StConfig parseStPolicy(String data) {
        data = getContent(data);
        return parseStPolicyInternal(data);
    }

    private StConfig parseStPolicyInternal(String content) {
        StConfig stConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            stConfig = new StConfig();
            if (jobj.has(ENABLE)) {
                stConfig.setEnable(jobj.getInt(ENABLE) == 1);
            }
            parseAttrConfig(stConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseStPolicyInternal error : " + e);
        }
        return stConfig;
    }

    /**
     * 解析归因配置
     *
     * @param attrConfig
     * @param jobj
     */
    private void parseAttrConfig(AttrConfig attrConfig, JSONObject jobj) {
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
                    attrConfig.setCountryList(list);
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
                    attrConfig.setAttrList(list);
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
                    attrConfig.setMediaList(list);
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "parseAttrConfig error : " + e);
        }
    }

    private List<AdPlace> parseAdPlaces(String content) {
        List<AdPlace> list = null;
        try {
            JSONArray jarray = new JSONArray(content);
            int len = jarray.length();
            if (len > 0) {
                list = new ArrayList<AdPlace>(len);
                for (int index = 0; index < len; index++) {
                    list.add(parseAdPlace(jarray.getString(index)));
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return list;
    }

    @Override
    public AdPlace parseAdPlace(String content) {
        AdPlace adPlace = null;
        try {
            content = getContent(content);
            JSONObject jobj = new JSONObject(content);
            adPlace = new AdPlace();
            if (jobj.has(NAME)) {
                adPlace.setName(jobj.getString(NAME));
            }
            if (jobj.has(MODE)) {
                adPlace.setMode(jobj.getString(MODE));
            }
            if (jobj.has(MAXCOUNT)) {
                adPlace.setMaxCount(jobj.getInt(MAXCOUNT));
            }
            if (jobj.has(PERCENT)) {
                adPlace.setPercent(jobj.getInt(PERCENT));
            }
            if (jobj.has(PIDS)) {
                adPlace.setPidsList(parsePidList(jobj.getString(PIDS)));
            }
            if (jobj.has(AUTO_SWITCH)) {
                adPlace.setAutoSwitch(jobj.getInt(AUTO_SWITCH) == 1);
            }
            if (jobj.has(LOAD_ONLY_ONCE)) {
                adPlace.setLoadOnlyOnce(jobj.getBoolean(LOAD_ONLY_ONCE));
            }
            if (jobj.has(ECPMSORT)) {
                adPlace.setEcpmSort(jobj.getInt(ECPMSORT));
            }
            adPlace.setUniqueValue(Utils.string2MD5(content.trim()));
            sortPidList(adPlace);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdPlace error : " + e);
        }
        return adPlace;
    }

    private void sortPidList(AdPlace adPlace) {
        if (adPlace == null) {
            return;
        }
        int ecpmSort = adPlace.getEcpmSort();
        List<PidConfig> list = adPlace.getPidsList();
        if (ecpmSort == 0 || list == null || list.isEmpty()) {
            return;
        }
        if (ecpmSort > 0) {
            Collections.sort(list, new Comparator<PidConfig>() {
                @Override
                public int compare(PidConfig o1, PidConfig o2) {
                    if (o1 != null && o2 != null) {
                        return o2.getEcpm() - o1.getEcpm();
                    }
                    return 0;
                }
            });
        } else {
            Collections.sort(list, new Comparator<PidConfig>() {
                @Override
                public int compare(PidConfig o1, PidConfig o2) {
                    if (o1 != null && o2 != null) {
                        return o1.getEcpm() - o2.getEcpm();
                    }
                    return 0;
                }
            });
        }
    }

    private List<PidConfig> parsePidList(String content) {
        List<PidConfig> list = null;
        try {
            JSONArray jarray = new JSONArray(content);
            int len = jarray.length();
            if (len > 0) {
                list = new ArrayList<PidConfig>();
                PidConfig pidConfig = null;
                for (int index = 0; index < len; index++) {
                    pidConfig = parsePidConfig(jarray.getString(index));
                    list.add(pidConfig);
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    private PidConfig parsePidConfig(String content) {
        PidConfig pidConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            pidConfig = new PidConfig();
            if (jobj.has(SDK)) {
                pidConfig.setSdk(jobj.getString(SDK));
            }
            if (jobj.has(PID)) {
                String pid = jobj.getString(PID);
                if (pid != null) {
                    pid = pid.trim();
                }
                pidConfig.setPid(pid);
            }
            if (jobj.has(CTR)) {
                pidConfig.setCtr(jobj.getInt(CTR));
            }
            if (jobj.has(TYPE)) {
                pidConfig.setAdType(jobj.getString(TYPE));
            }
            if (jobj.has(DISABLE)) {
                pidConfig.setDisable(jobj.getInt(DISABLE) == 1);
            }
            if (jobj.has(NOFILL)) {
                pidConfig.setNoFill(jobj.getLong(NOFILL));
            }
            if (jobj.has(CACHE_TIME)) {
                pidConfig.setCacheTime(jobj.getLong(CACHE_TIME));
            }
            if (jobj.has(TIMEOUT)) {
                pidConfig.setTimeOut(jobj.getLong(TIMEOUT));
            }
            if (jobj.has(DELAY_LOAD_TIME)) {
                pidConfig.setDelayLoadTime(jobj.getLong(DELAY_LOAD_TIME));
            }
            if (jobj.has(ECPM)) {
                pidConfig.setEcpm(jobj.getInt(ECPM));
            }
            if (jobj.has(FINISH_FORCTR)) {
                pidConfig.setFinishForCtr(jobj.getInt(FINISH_FORCTR) == 1);
            }
            parseAttrConfig(pidConfig, jobj);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return pidConfig;
    }

    @Override
    public AdSwitch parseAdSwitch(String content) {
        AdSwitch adSwitch = null;
        try {
            content = getContent(content);
            JSONObject jobj = new JSONObject(content);
            adSwitch = new AdSwitch();
            if (jobj.has(BLOCK_LOADING)) {
                adSwitch.setBlockLoading(jobj.getInt(BLOCK_LOADING) == 1);
            }
            if (jobj.has(REPORT_ERROR)) {
                adSwitch.setReportError(jobj.getInt(REPORT_ERROR) == 1);
            }
            if (jobj.has(REPORT_TIME)) {
                adSwitch.setReportTime(jobj.getInt(REPORT_TIME) == 1);
            }
            if (jobj.has(REPORT_UMENG)) {
                adSwitch.setReportUmeng(jobj.getInt(REPORT_UMENG) == 1);
            }
            if (jobj.has(REPORT_APPSFLYER)) {
                adSwitch.setReportAppsflyer(jobj.getInt(REPORT_APPSFLYER) == 1);
            }
            if (jobj.has(REPORT_FIREBASE)) {
                adSwitch.setReportFirebase(jobj.getInt(REPORT_FIREBASE) == 1);
            }
            if (jobj.has(REPORT_FACEBOOK)) {
                adSwitch.setReportFacebook(jobj.getInt(REPORT_FACEBOOK) == 1);
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdSwitch error : " + e);
        }
        return adSwitch;
    }

    @Override
    public Map<String, String> parseAdRefs(String data) {
        Map<String, String> adRefs = null;
        try {
            data = getContent(data);
            JSONObject jobj = new JSONObject(data);
            adRefs = new HashMap<String, String>();
            Iterator<String> iterator = jobj.keys();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = iterator.next();
                value = jobj.getString(key);
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    adRefs.put(key, value);
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdRefs error : " + e);
        }
        return adRefs;
    }
}