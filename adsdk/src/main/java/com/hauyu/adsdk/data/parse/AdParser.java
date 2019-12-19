package com.hauyu.adsdk.data.parse;

import android.text.TextUtils;

import com.gekes.fvs.tdsvap.SpConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.Aes;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.AdSwitch;
import com.hauyu.adsdk.data.config.PidConfig;
import com.hauyu.adsdk.data.config.PlaceConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

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
            AdSwitch adSwitch = null;
            Map<String, String> adrefs = null;
            if (jobj.has(ADPLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (jobj.has(ADSWITCH)) {
                adSwitch = parseAdSwitch(jobj.getString(ADSWITCH));
            }
            if (jobj.has(ADREFS)) {
                adrefs = parseAdRefs(jobj.getString(ADREFS));
            }
            if (adPlaces != null
                    || adSwitch != null
                    || adrefs != null) {
                placeConfig = new PlaceConfig();
                placeConfig.setAdPlaceList(adPlaces);
                placeConfig.setAdSwitch(adSwitch);
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
            if (jobj.has(FULL_LAYOUT)) {
                adPlace.setFullLayout(jobj.getString(FULL_LAYOUT));
            }
            if (jobj.has(CTA_COLOR)) {
                adPlace.setCtaColor(parseStringList(jobj.getString(CTA_COLOR)));
            }
            if (jobj.has(CLICK_VIEWS)) {
                adPlace.setClickViews(parseStringList(jobj.getString(CLICK_VIEWS)));
            }
            if (jobj.has(PIDS)) {
                adPlace.setPidsList(parsePidList(adPlace, jobj.getString(PIDS)));
            }
            if (jobj.has(AUTO_SWITCH)) {
                adPlace.setAutoSwitch(jobj.getInt(AUTO_SWITCH) == 1);
            }
            if (jobj.has(AUTO_INTERVAL)) {
                adPlace.setAutoInterval(jobj.getLong(AUTO_INTERVAL));
            }
            if (jobj.has(LOAD_ONLY_ONCE)) {
                adPlace.setLoadOnlyOnce(jobj.getBoolean(LOAD_ONLY_ONCE));
            }
            if (jobj.has(ECPM_SORT)) {
                adPlace.setEcpmSort(jobj.getInt(ECPM_SORT));
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
            if (jobj.has(HIGH_ECPM)) {
                adPlace.setHighEcpm(jobj.getInt(HIGH_ECPM) == 1);
            }
            if (jobj.has(PLACE_TYPE)) {
                adPlace.setPlaceType(jobj.getString(PLACE_TYPE));
            }
            if (jobj.has(SEQ_TIMEOUT)) {
                adPlace.setSeqTimeout(jobj.getLong(SEQ_TIMEOUT));
            }
            if (jobj.has(QUEUE_SIZE)) {
                adPlace.setQueueSize(jobj.getInt(QUEUE_SIZE));
            }
            if (jobj.has(RETRY)) {
                adPlace.setRetry(jobj.getInt(RETRY) == 1);
            }
            if (jobj.has(RETRY_TIME)) {
                adPlace.setRetryTimes(jobj.getInt(RETRY_TIME));
            }
            adPlace.setUniqueValue(Utils.string2MD5(content.trim()));
            sortPidList(adPlace);
        } catch (Exception e) {
            Log.iv(Log.TAG, "parseAdPlace error : " + e);
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
                        return Double.compare(o2.getEcpm(), o1.getEcpm());
                    }
                    return 0;
                }
            });
        } else {
            Collections.sort(list, new Comparator<PidConfig>() {
                @Override
                public int compare(PidConfig o1, PidConfig o2) {
                    if (o1 != null && o2 != null) {
                        return Double.compare(o1.getEcpm(), o2.getEcpm());
                    }
                    return 0;
                }
            });
        }
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
                    /*
                    if (adPlace != null && pidConfig != null) {
                        if (!TextUtils.isEmpty(adPlace.getFullLayout()) && TextUtils.isEmpty(pidConfig.getFullLayout())) {
                            pidConfig.setFullLayout(adPlace.getFullLayout());
                        }
                        if ((adPlace.getCtaColor() != null && !adPlace.getCtaColor().isEmpty())
                                && (pidConfig.getCtaColor() == null || pidConfig.getCtaColor().isEmpty())) {
                            pidConfig.setCtaColor(adPlace.getCtaColor());
                        }
                        if ((adPlace.getClickViews() != null && !adPlace.getClickViews().isEmpty())
                                && (pidConfig.getClickViews() == null || pidConfig.getClickViews().isEmpty())) {
                            pidConfig.setClickViews(adPlace.getClickViews());
                        }
                    }*/
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
                pidConfig.setEcpm(jobj.getDouble(ECPM));
            }
            if (jobj.has(FINISH_FORCTR)) {
                pidConfig.setFinishForCtr(jobj.getInt(FINISH_FORCTR) == 1);
            }
            if (jobj.has(DELAY_CLICK_TIME)) {
                pidConfig.setDelayClickTime(jobj.getLong(DELAY_CLICK_TIME));
            }
            if (jobj.has(DESTROY_AFTER_CLICK)) {
                pidConfig.setDestroyAfterClick(jobj.getInt(DESTROY_AFTER_CLICK) == 1);
            }
            if (jobj.has(APPID)) {
                pidConfig.setAppId(jobj.getString(APPID));
            }
            if (jobj.has(EXTID)) {
                pidConfig.setExtId(jobj.getString(EXTID));
            }
            if (jobj.has(BANNER_SIZE)) {
                pidConfig.setBannerSize(jobj.getString(BANNER_SIZE));
            }
            if (jobj.has(LOAD_NATIVE_COUNT)) {
                pidConfig.setCnt(jobj.getInt(LOAD_NATIVE_COUNT));
            }
            if (jobj.has(FULL_LAYOUT)) {
                pidConfig.setLayout(jobj.getString(FULL_LAYOUT));
            }
            if (jobj.has(CTA_COLOR)) {
                pidConfig.setCtaColor(parseStringList(jobj.getString(CTA_COLOR)));
            }
            if (jobj.has(CLICK_VIEWS)) {
                pidConfig.setClickViews(parseStringList(jobj.getString(CLICK_VIEWS)));
            }
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
            if (jobj.has(REPORT_ERROR)) {
                adSwitch.setReportError(jobj.getInt(REPORT_ERROR) == 1);
            }
            if (jobj.has(REPORT_TIME)) {
                adSwitch.setReportTime(jobj.getInt(REPORT_TIME) == 1);
            }
            if (jobj.has(REPORT_UMENG)) {
                adSwitch.setReportUmeng(jobj.getInt(REPORT_UMENG) == 1);
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

    @Override
    public List<SpConfig> parseSpread(String data) {
        List<SpConfig> spreads = null;
        data = getContent(data);
        try {
            JSONObject jobj = new JSONObject(data);
            SpConfig spConfig = parseSpConfigLocked(jobj);
            spreads = new ArrayList<SpConfig>(1);
            spreads.add(spConfig);
        } catch (Exception e) {
        }
        if (spreads == null || spreads.isEmpty()) {
            try {
                JSONArray jarray = new JSONArray(data);
                int len = jarray.length();
                if (len > 0) {
                    spreads = new ArrayList<SpConfig>(len);
                    JSONObject jobj = null;
                    SpConfig spConfig = null;
                    for (int index = 0; index < len; index++) {
                        jobj = jarray.getJSONObject(index);
                        spConfig = parseSpConfigLocked(jobj);
                        if (spConfig != null) {
                            spreads.add(spConfig);
                        }
                    }
                }
            } catch (Exception e) {
                Log.v(Log.TAG, "parseSpread error : " + e);
            }
        }
        return spreads;
    }

    private SpConfig parseSpConfigLocked(JSONObject jobj) {
        SpConfig spConfig = null;
        try {
            if (jobj != null) {
                spConfig = new SpConfig();
                if (jobj.has(BANNER)) {
                    spConfig.setBanner(jobj.getString(BANNER));
                }
                if (jobj.has(ICON)) {
                    spConfig.setIcon(jobj.getString(ICON));
                }
                if (jobj.has(TITLE)) {
                    spConfig.setTitle(jobj.getString(TITLE));
                }
                if (jobj.has(PKGNAME)) {
                    spConfig.setPkgname(jobj.getString(PKGNAME));
                }
                if (jobj.has(SUBTITLE)) {
                    spConfig.setSubTitle(jobj.getString(SUBTITLE));
                }
                if (jobj.has(DETAIL)) {
                    spConfig.setDetail(jobj.getString(DETAIL));
                }
                if (jobj.has(LINKURL)) {
                    spConfig.setLinkUrl(jobj.getString(LINKURL));
                }
                if (jobj.has(CTA)) {
                    spConfig.setCta(jobj.getString(CTA));
                }
                if (jobj.has(DISABLE)) {
                    spConfig.setDisable(jobj.getInt(DISABLE) == 1);
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseSpConfig error : " + e);
        }
        return spConfig;
    }
}