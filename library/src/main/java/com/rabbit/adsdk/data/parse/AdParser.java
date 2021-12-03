package com.rabbit.adsdk.data.parse;

import android.text.TextUtils;

import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.data.config.PlaceConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.AesUtils;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.SpreadCfg;

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

    @Override
    public PlaceConfig parseAdConfig(String data) {
        data = getContent(data);
        PlaceConfig adconfig = parseAdConfigLocked(data);
        return adconfig;
    }

    private PlaceConfig parseAdConfigLocked(String data) {
        PlaceConfig placeConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            List<AdPlace> adPlaces = null;
            Map<String, String> adrefs = null;
            if (jobj.has(ADPLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (jobj.has(ADREFS)) {
                adrefs = parseAdRefs(jobj.getString(ADREFS));
            }
            if (adPlaces != null
                    || adrefs != null) {
                placeConfig = new PlaceConfig();
                placeConfig.setAdPlaceList(adPlaces);
                placeConfig.setAdRefs(adrefs);
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseAdConfigInternal error : " + e);
        }
        return placeConfig;
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
            if (jobj.has(NATIVE_LAYOUT)) {
                adPlace.setNativeLayout(parseStringList(jobj.getString(NATIVE_LAYOUT)));
            }
            if (jobj.has(CTA_COLOR)) {
                adPlace.setCtaColor(parseStringList(jobj.getString(CTA_COLOR)));
            }
            if (jobj.has(CLICK_VIEW)) {
                adPlace.setClickView(parseStringList(jobj.getString(CLICK_VIEW)));
            }
            if (jobj.has(CLICK_VIEW_RENDER)) {
                adPlace.setClickViewRender(parseStringList(jobj.getString(CLICK_VIEW_RENDER)));
            }
            if (jobj.has(PIDS)) {
                adPlace.setPidList(parsePidList(adPlace, jobj.getString(PIDS)));
            }
            if (jobj.has(CLICK_SWITCH)) {
                adPlace.setClickSwitch(jobj.getInt(CLICK_SWITCH) == 1);
            }
            if (jobj.has(AUTO_INTERVAL)) {
                adPlace.setAutoInterval(jobj.getLong(AUTO_INTERVAL));
            }
            if (jobj.has(LOAD_ONLY_ONCE)) {
                adPlace.setLoadOnlyOnce(jobj.getBoolean(LOAD_ONLY_ONCE));
            }
            if (jobj.has(PLACE_CACHE)) {
                adPlace.setPlaceCache(jobj.getInt(PLACE_CACHE) == 1);
            }
            if (jobj.has(DELAY_NOTIFY_TIME)) {
                adPlace.setDelayNotifyTime(jobj.getLong(DELAY_NOTIFY_TIME));
            }
            if (jobj.has(REF_SHARE)) {
                adPlace.setRefShare(jobj.getInt(REF_SHARE) == 1);
            }
            if (jobj.has(WATERFALL_INTERVAL)) {
                adPlace.setWaterfallInt(jobj.getLong(WATERFALL_INTERVAL));
            }
            if (jobj.has(BANNER_SIZE)) {
                adPlace.setBannerSize(jobj.getString(BANNER_SIZE));
            }
            if (jobj.has(SEQ_TIMEOUT)) {
                adPlace.setSeqTimeout(jobj.getLong(SEQ_TIMEOUT));
            }
            if (jobj.has(RETRY)) {
                adPlace.setRetryTimes(jobj.getInt(RETRY));
            }
            adPlace.setUniqueValue(Utils.string2MD5(content.trim()));
        } catch (Exception e) {
            Log.iv(Log.TAG, "parseAdPlace error : " + e);
        }
        return adPlace;
    }

    private List<PidConfig> parsePidList(AdPlace adPlace, String content) {
        List<PidConfig> list = null;
        try {
            JSONArray jarray = new JSONArray(content);
            int len = jarray.length();
            if (len > 0) {
                list = new ArrayList<PidConfig>();
                PidConfig pidConfig = null;
                for (int index = 0; index < len; index++) {
                    pidConfig = parsePidConfig(jarray.getString(index));
                    pidConfig.setAdPlace(adPlace);
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
                pidConfig.setEcpm(jobj.getDouble(ECPM));
            }
            if (jobj.has(BANNER_SIZE)) {
                pidConfig.setBannerSize(jobj.getString(BANNER_SIZE));
            }
            if (jobj.has(LOAD_NATIVE_COUNT)) {
                pidConfig.setCnt(jobj.getInt(LOAD_NATIVE_COUNT));
            }
            if (jobj.has(NATIVE_LAYOUT)) {
                pidConfig.setNativeLayout(parseStringList(jobj.getString(NATIVE_LAYOUT)));
            }
            if (jobj.has(CTA_COLOR)) {
                pidConfig.setCtaColor(parseStringList(jobj.getString(CTA_COLOR)));
            }
            if (jobj.has(CLICK_VIEW)) {
                pidConfig.setClickView(parseStringList(jobj.getString(CLICK_VIEW)));
            }
            if (jobj.has(CLICK_VIEW_RENDER)) {
                pidConfig.setClickViewRender(parseStringList(jobj.getString(CLICK_VIEW_RENDER)));
            }
            if (jobj.has(ACTIVITY_CONTEXT)) {
                pidConfig.setActivityContext(jobj.getInt(ACTIVITY_CONTEXT) == 1);
            }
            if (jobj.has(RATIO)) {
                pidConfig.setRatio(jobj.getInt(RATIO));
            }
            if (jobj.has(SUB_NATIVE_LAYOUT)) {
                pidConfig.setSubNativeLayout(jsonToMap(jobj.getString(SUB_NATIVE_LAYOUT)));
            }
            if (jobj.has(SPLASH_ORIENTATION)) {
                pidConfig.setSplashOrientation(jobj.getInt(SPLASH_ORIENTATION));
            }
            if (jobj.has(EXTRA)) {
                pidConfig.setExtra(jsonToMap(jobj.getString(EXTRA)));
            }
            if (jobj.has(TEMPLATE)) {
                pidConfig.setTemplate(jobj.getInt(TEMPLATE) == 1);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return pidConfig;
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

    @Override
    public List<SpreadCfg> parseSpread(String data) {
        List<SpreadCfg> spreads = null;
        data = getContent(data);
        try {
            JSONObject jobj = new JSONObject(data);
            SpreadCfg spreadCfg = parseSpConfigLocked(jobj);
            spreads = new ArrayList<SpreadCfg>(1);
            spreads.add(spreadCfg);
        } catch (Exception e) {
        }
        if (spreads == null || spreads.isEmpty()) {
            try {
                JSONArray jarray = new JSONArray(data);
                int len = jarray.length();
                if (len > 0) {
                    spreads = new ArrayList<SpreadCfg>(len);
                    JSONObject jobj = null;
                    SpreadCfg spreadCfg = null;
                    for (int index = 0; index < len; index++) {
                        jobj = jarray.getJSONObject(index);
                        spreadCfg = parseSpConfigLocked(jobj);
                        if (spreadCfg != null) {
                            spreads.add(spreadCfg);
                        }
                    }
                }
            } catch (Exception e) {
                Log.v(Log.TAG, "parseSpread error : " + e);
            }
        }
        return spreads;
    }

    private SpreadCfg parseSpConfigLocked(JSONObject jobj) {
        SpreadCfg spreadCfg = null;
        try {
            if (jobj != null) {
                spreadCfg = new SpreadCfg();
                if (jobj.has(BANNER)) {
                    spreadCfg.setBanner(jobj.getString(BANNER));
                }
                if (jobj.has(ICON)) {
                    spreadCfg.setIcon(jobj.getString(ICON));
                }
                if (jobj.has(TITLE)) {
                    spreadCfg.setTitle(jobj.getString(TITLE));
                }
                if (jobj.has(PKGNAME)) {
                    spreadCfg.setPkgname(jobj.getString(PKGNAME));
                }
                if (jobj.has(DETAIL)) {
                    spreadCfg.setDetail(jobj.getString(DETAIL));
                }
                if (jobj.has(LINKURL)) {
                    spreadCfg.setLinkUrl(jobj.getString(LINKURL));
                }
                if (jobj.has(CTA)) {
                    spreadCfg.setCta(jobj.getString(CTA));
                }
                if (jobj.has(DISABLE)) {
                    spreadCfg.setDisable(jobj.getInt(DISABLE) == 1);
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseSpConfig error : " + e);
        }
        return spreadCfg;
    }

    @Override
    public Map<String, Map<String, String>> parseMediationConfig(String data) {
        Map<String, Map<String, String>> config = null;
        data = getContent(data);
        try {
            JSONObject jobj = new JSONObject(data);
            int size = jobj.length();
            if (size > 0) {
                config = new HashMap<String, Map<String, String>>();
                Iterator<String> keys = jobj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String valueObj = jobj.getString(key);
                    config.put(key, jsonToMap(valueObj));
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseMediationConfig error : " + e);
        }
        return config;
    }

    private Map<String, String> jsonToMap(String data) {
        Map<String, String> map = null;
        try {
            JSONObject jobj = new JSONObject(data);
            map = new HashMap<String, String>();
            Iterator<String> keys = jobj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, jobj.getString(key));
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "jsonToMap error : " + e);
        }
        return map;
    }
}