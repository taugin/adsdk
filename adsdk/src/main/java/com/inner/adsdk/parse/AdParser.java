package com.inner.adsdk.parse;

import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdPolicy;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Aes;
import com.inner.adsdk.log.Log;

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

    private String getContent(String content) {
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(content);
        } catch(Exception e) {
            Log.v(Log.TAG, "encrypt content");
        }
        if (jobj != null) {
            return jobj.toString();
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
            AdPolicy adPolicy = null;
            List<AdPlace> adPlaces = null;
            if (jobj.has(ADIDS)) {
                adIds = parseAdIds(jobj.getString(ADIDS));
            }
            if (jobj.has(POLICY)) {
                adPolicy = parseAdPolicyInternal(jobj.getString(POLICY));
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
            while(iterator.hasNext()) {
                key = iterator.next();
                value = jobj.getString(key);
                adIds.put(key, value);
            }
        } catch(Exception e) {
            Log.v(Log.TAG, "parseAdIds error : " + e);
        }
        return adIds;
    }

    @Override
    public AdPolicy parseAdPolicy(String data) {
        data = getContent(data);
        return parseAdPolicyInternal(data);
    }

    private AdPolicy parseAdPolicyInternal(String content) {
        AdPolicy adPolicy = null;
        try {
            JSONObject jobj = new JSONObject(content);
            adPolicy = new AdPolicy();
            if (jobj.has(ENABLE)) {
                adPolicy.setEnable(jobj.getInt(ENABLE) == 1);
            }
            if (jobj.has(UPDELAY)) {
                adPolicy.setUpDelay(jobj.getLong(UPDELAY));
            }
            if (jobj.has(INTERVAL)) {
                adPolicy.setInterval(jobj.getLong(INTERVAL));
            }
            if (jobj.has(MAX_COUNT)) {
                adPolicy.setMaxCount(jobj.getInt(MAX_COUNT));
            }
            if (jobj.has(MAX_VERSION)) {
                adPolicy.setMaxVersion(jobj.getInt(MAX_VERSION));
            }
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
                    adPolicy.setCountryList(list);
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
                    adPolicy.setAttrList(list);
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
                    adPolicy.setMediaList(list);
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdPolicyInternal error : " + e);
        }
        return adPolicy;
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
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdPlace error : " + e);
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
            if (jobj.has(DISABLE)) {
                pidConfig.setDisable(jobj.getInt(DISABLE) == 1);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return pidConfig;
    }
}
