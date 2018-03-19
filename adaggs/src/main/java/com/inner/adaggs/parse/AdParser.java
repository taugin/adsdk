package com.inner.adaggs.parse;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.config.DevInfo;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdParser implements IParser {

    @Override
    public int parseStatus(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            int status = -1;
            if (jobj.has(STATUS)) {
                status = jobj.getInt(STATUS);
            }
            return status;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return 0;
    }

    @Override
    public List<DevInfo> parseDevList(String data) {
        List<DevInfo> list = parseWhiteList(data);
        return list;
    }

    /**
     * 解析白名单
     *
     * @param data
     * @return
     */
    private List<DevInfo> parseWhiteList(String data) {
        List<DevInfo> list = null;
        try {
            JSONObject jobj = new JSONObject(data);
            JSONArray jarray = null;
            if (jobj.has(WHITE_LIST)) {
                jarray = jobj.getJSONArray(WHITE_LIST);
            }
            DevInfo info = null;
            if (jarray != null) {
                int len = jarray.length();
                if (len > 0) {
                    list = new ArrayList<DevInfo>(len);
                    JSONObject j = null;
                    for (int index = 0; index < len; index++) {
                        info = new DevInfo();
                        j = jarray.getJSONObject(index);
                        if (j.has(IMEI)) {
                            info.setImei(j.getString(IMEI));
                        }
                        if (j.has(AID)) {
                            info.setAndroidId(j.getString(AID));
                        }
                        list.add(info);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return list;
    }

    @Override
    public String parseContent(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            String s = null;
            if (jobj.has(DATA)) {
                s = jobj.getString(DATA);
            }
            return s;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    @Override
    public AdConfig parse(String data) {
        AdConfig adconfig = parseAdConfig(data);
        return adconfig;
    }

    private AdConfig parseAdConfig(String data) {
        AdConfig adConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            Map<String, String> adIds = null;
            AdPolicy adPolicy = null;
            List<AdPlace> adPlaces = null;
            if (jobj.has(ADIDS)) {
                adIds = parseAdIds(jobj.getString(ADIDS));
            }
            if (jobj.has(POLICY)) {
                adPolicy = parsePolicy(jobj.getString(POLICY));
            }
            if (jobj.has(ADPLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (adPlaces != null || adPolicy != null || adIds != null) {
                adConfig = new AdConfig();
                adConfig.setAdPlaceList(adPlaces);
                adConfig.setAdPolicy(adPolicy);
                adConfig.setAdIds(adIds);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return adConfig;
    }

    private Map<String, String> parseAdIds(String content) {
        Map<String, String> adIds = null;
        try {
            JSONObject jobj = new JSONObject(content);
            adIds = new HashMap<String, String>();
            Iterator<String> iterator = jobj.keys();
            String key = null;
            String value = null;
            while(iterator.hasNext()) {
                key = iterator.next();
                value = jobj.getString(key);
                adIds.put(key, value);
            }
        } catch(Exception e) {
        }
        return adIds;
    }

    private AdPolicy parsePolicy(String content) throws Exception {
        return new AdPolicy();
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

    private AdPlace parseAdPlace(String content) {
        AdPlace adPlace = null;
        try {
            JSONObject jobj = new JSONObject(content);
            adPlace = new AdPlace();
            if (jobj.has(NAME)) {
                adPlace.setName(jobj.getString(NAME));
            }
            if (jobj.has(MODE)) {
                adPlace.setMode(jobj.getString(MODE));
            }
            if (jobj.has(PTYPE)) {
                adPlace.setPtype(jobj.getString(PTYPE));
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
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return adPlace;
    }

    private List<PidConfig> parsePidList(String content) {
        List<PidConfig> list = null;
        try {
            JSONArray jarray = new JSONArray(content);
            int len = jarray.length();
            if (len > 0) {
                list = new ArrayList<PidConfig>();
                for (int index = 0; index < len; index++) {
                    list.add(parsePidConfig(jarray.getString(index)));
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
                pidConfig.setPid(jobj.getString(PID));
            }
            if (jobj.has(CTR)) {
                pidConfig.setCtr(jobj.getInt(CTR));
            }
            if (jobj.has(TYPE)) {
                pidConfig.setAdType(jobj.getString(TYPE));
            }
            if (jobj.has(OUTER)) {
                pidConfig.setOuter(jobj.getInt(OUTER));
            }
            if (jobj.has(FILM)) {
                pidConfig.setFilm(jobj.getInt(FILM));
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return pidConfig;
    }
}
