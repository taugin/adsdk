package com.humob.adsdk.data.parse;

import android.text.TextUtils;

import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.data.config.AdPlace;
import com.humob.adsdk.data.config.PidConfig;
import com.humob.adsdk.data.config.PlaceConfig;
import com.humob.adsdk.data.config.SpreadConfig;
import com.humob.adsdk.log.Log;
import com.humob.adsdk.utils.AesUtils;
import com.humob.adsdk.utils.Utils;

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
        JSONObject jObj = null;
        JSONArray jArray = null;
        try {
            jObj = new JSONObject(content);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : not a json object");
        }
        if (jObj != null) {
            return jObj.toString();
        }
        try {
            jArray = new JSONArray(content);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : not a json array");
        }
        if (jArray != null) {
            return jArray.toString();
        }
        return AesUtils.decrypt(Constant.KEY_PASSWORD, content);
    }

    @Override
    public PlaceConfig parseAdConfig(String data) {
        data = getContent(data);
        if (!TextUtils.isEmpty(data)) {
            PlaceConfig adConfig = parseAdConfigLocked(data);
            return adConfig;
        }
        Log.iv(Log.TAG, "ad config is empty");
        return null;
    }

    private PlaceConfig parseAdConfigLocked(String data) {
        PlaceConfig placeConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            List<AdPlace> adPlaces = null;
            Map<String, String> sharePlace = null;
            String scenePrefix = null;
            boolean disableVpnLoad = false;
            if (jobj.has(ALL_PLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ALL_PLACES));
            }
            if (jobj.has(SHARE_PLACE)) {
                sharePlace = parseStringMap(jobj.getString(SHARE_PLACE));
            }
            if (jobj.has(SCENE_PREFIX)) {
                scenePrefix = jobj.getString(SCENE_PREFIX);
            }
            if (jobj.has(DISABLE_VPN_LOAD)) {
                disableVpnLoad = jobj.getInt(DISABLE_VPN_LOAD) == 1;
            }
            if (adPlaces != null
                    || sharePlace != null) {
                placeConfig = new PlaceConfig();
                placeConfig.setAdPlaceList(adPlaces);
                placeConfig.setAdRefs(sharePlace);
            }
            if (placeConfig != null) {
                placeConfig.setScenePrefix(scenePrefix);
                placeConfig.setDisableVpnLoad(disableVpnLoad);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "parseAdConfigInternal error : " + e);
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
            Log.iv(Log.TAG, "error : " + e);
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
            if (jobj.has(MAXSHOW)) {
                adPlace.setMaxCount(jobj.getInt(MAXSHOW));
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
            if (jobj.has(SCENE_ID)) {
                adPlace.setSceneId(jobj.getString(SCENE_ID));
            }
            if (jobj.has(ORDER)) {
                adPlace.setOrder(jobj.getInt(ORDER) == 1);
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
            if (jobj.has(APP_ID)) {
                String appId = jobj.getString(APP_ID);
                if (appId != null) {
                    appId = appId.trim();
                }
                pidConfig.setAppId(appId);
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
            if (jobj.has(CPM)) {
                pidConfig.setCpm(jobj.getDouble(CPM));
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
            if (jobj.has(MAX_REQ_TIME)) {
                pidConfig.setMaxReqTimes(jobj.getInt(MAX_REQ_TIME));
            }
            if (jobj.has(SCENE_ID)) {
                pidConfig.setSceneId(jobj.getString(SCENE_ID));
            }
            if (jobj.has(SPLASH_ICON)) {
                pidConfig.setShowSplashIcon(jobj.getInt(SPLASH_ICON) == 1);
            }
            if (jobj.has(SPLASH_TIME_OUT)) {
                pidConfig.setSplashTimeout(jobj.getInt(SPLASH_TIME_OUT));
            }
            if (jobj.has(DISABLE_VPN_LOAD)) {
                pidConfig.setDisableVpnLoad(jobj.getInt(DISABLE_VPN_LOAD) == 1);
            }
            if (jobj.has(USE_AVG_VALUE)) {
                pidConfig.setUseAvgValue(jobj.getInt(USE_AVG_VALUE) == 1);
            }
            if (jobj.has(MIN_AVG_COUNT)) {
                pidConfig.setMinAvgCount(jobj.getInt(MIN_AVG_COUNT));
            }
            if (jobj.has(DISABLE_DEBUG_LOAD)) {
                pidConfig.setDisableDebugLoad(jobj.getInt(DISABLE_DEBUG_LOAD) == 1);
            }
            if (jobj.has(ONLY_SIGN_LOAD)) {
                pidConfig.setOnlySignLoad(jobj.getInt(ONLY_SIGN_LOAD) == 1);
            }
            if (jobj.has(ONLY_PACK_LOAD)) {
                pidConfig.setOnlyPackLoad(jobj.getInt(ONLY_PACK_LOAD) == 1);
            }
            if (jobj.has(AUTO_LOAD)) {
                pidConfig.setAutoLoad(jobj.getInt(AUTO_LOAD) == 1);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return pidConfig;
    }

    @Override
    public Map<String, String> parseStringMap(String data) {
        Map<String, String> map = null;
        try {
            data = getContent(data);
            JSONObject jsonObject = new JSONObject(data);
            map = new HashMap<String, String>();
            Iterator<String> iterator = jsonObject.keys();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = iterator.next();
                value = jsonObject.getString(key);
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "parseStringMap error : " + e);
        }
        return map;
    }

    @Override
    public List<SpreadConfig> parseSpread(String data) {
        List<SpreadConfig> spreads = null;
        data = getContent(data);
        try {
            JSONObject jobj = new JSONObject(data);
            SpreadConfig spreadConfig = parseSpConfigLocked(jobj);
            spreads = new ArrayList<SpreadConfig>(1);
            spreads.add(spreadConfig);
        } catch (Exception e) {
        }
        if (spreads == null || spreads.isEmpty()) {
            try {
                JSONArray jarray = new JSONArray(data);
                int len = jarray.length();
                if (len > 0) {
                    spreads = new ArrayList<SpreadConfig>(len);
                    JSONObject jobj = null;
                    SpreadConfig spreadConfig = null;
                    for (int index = 0; index < len; index++) {
                        jobj = jarray.getJSONObject(index);
                        spreadConfig = parseSpConfigLocked(jobj);
                        if (spreadConfig != null) {
                            spreads.add(spreadConfig);
                        }
                    }
                }
            } catch (Exception e) {
                Log.iv(Log.TAG, "parseSpread error : " + e);
            }
        }
        return spreads;
    }

    private SpreadConfig parseSpConfigLocked(JSONObject jobj) {
        SpreadConfig spreadConfig = null;
        try {
            if (jobj != null) {
                spreadConfig = new SpreadConfig();
                if (jobj.has(BANNER)) {
                    spreadConfig.setBanner(jobj.getString(BANNER));
                }
                if (jobj.has(ICON)) {
                    spreadConfig.setIcon(jobj.getString(ICON));
                }
                if (jobj.has(TITLE)) {
                    spreadConfig.setTitle(jobj.getString(TITLE));
                }
                if (jobj.has(BUNDLE)) {
                    spreadConfig.setBundle(jobj.getString(BUNDLE));
                }
                if (jobj.has(DETAIL)) {
                    spreadConfig.setDetail(jobj.getString(DETAIL));
                }
                if (jobj.has(URL)) {
                    spreadConfig.setLinkUrl(jobj.getString(URL));
                }
                if (jobj.has(CTA)) {
                    spreadConfig.setCta(jobj.getString(CTA));
                }
                if (jobj.has(DISABLE)) {
                    spreadConfig.setDisable(jobj.getInt(DISABLE) == 1);
                }
                if (jobj.has(CTA_LOCALE)) {
                    spreadConfig.setCtaLocale(parseStringMap(jobj.getString(CTA_LOCALE)));
                }
                if (jobj.has(LOADING_TIME)) {
                    spreadConfig.setLoadingTime(jobj.getLong(LOADING_TIME));
                }
                if (jobj.has(PLAY)) {
                    spreadConfig.setPlay(jobj.getBoolean(PLAY));
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "parseSpConfig error : " + e);
        }
        return spreadConfig;
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
            Log.iv(Log.TAG, "parseMediationConfig error : " + e);
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
            Log.iv(Log.TAG, "jsonToMap error : " + e);
        }
        return map;
    }
}