package com.inner.adsdk.parse;

import android.text.TextUtils;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.AtConfig;
import com.inner.adsdk.config.BaseConfig;
import com.inner.adsdk.config.CtConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.config.HtConfig;
import com.inner.adsdk.config.LtConfig;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.config.SpConfig;
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
            AtConfig atConfig = null;
            LtConfig ltConfig = null;
            HtConfig htConfig = null;
            CtConfig ctConfig = null;
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
            if (jobj.has(ATCONFIG)) {
                atConfig = parseAtPolicyInternal(jobj.getString(ATCONFIG));
            }
            if (jobj.has(LTCONFIG)) {
                ltConfig = parseLtPolicyInternal(jobj.getString(LTCONFIG));
            }
            if (jobj.has(HTCONFIG)) {
                htConfig = parseHtPolicyInternal(jobj.getString(HTCONFIG));
            }
            if (jobj.has(CTCONFIG)) {
                ctConfig = parseCtPolicyInternal(jobj.getString(CTCONFIG));
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
                    || adrefs != null || stConfig != null
                    || atConfig != null || ltConfig != null
                    || htConfig != null || ctConfig != null) {
                adConfig = new AdConfig();
                adConfig.setAdPlaceList(adPlaces);
                adConfig.setGtConfig(gtConfig);
                adConfig.setAdIds(adIds);
                adConfig.setAdSwitch(adSwitch);
                adConfig.setAdRefs(adrefs);
                adConfig.setStConfig(stConfig);
                adConfig.setAtConfig(atConfig);
                adConfig.setLtConfig(ltConfig);
                adConfig.setHtConfig(htConfig);
                adConfig.setCtConfig(ctConfig);
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
            parseAttrConfig(baseConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseBasePolicyInternal error : " + e);
        }
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
            parseBaseConfig(gtConfig, jobj);
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
            parseBaseConfig(stConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseStPolicyInternal error : " + e);
        }
        return stConfig;
    }

    @Override
    public AtConfig parseAtPolicy(String data) {
        data = getContent(data);
        return parseAtPolicyInternal(data);
    }

    private AtConfig parseAtPolicyInternal(String content) {
        AtConfig atConfig = null;
        try {
            JSONObject jobj = new JSONObject(content);
            atConfig = new AtConfig();
            if (jobj.has(EXCLUDE_PACKAGES)) {
                atConfig.setExcludes(parseStringList(jobj.getString(EXCLUDE_PACKAGES)));
            }
            if (jobj.has(SHOW_ON_FIRST_PAGE)) {
                atConfig.setShowOnFirstPage(jobj.getInt(SHOW_ON_FIRST_PAGE) == 1);
            }
            parseBaseConfig(atConfig, jobj);
        } catch (Exception e) {
            Log.v(Log.TAG, "parseStPolicyInternal error : " + e);
        }
        return atConfig;
    }

    @Override
    public LtConfig parseLtPolicy(String data) {
        data = getContent(data);
        return parseLtPolicyInternal(data);
    }

    private LtConfig parseLtPolicyInternal(String data) {
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
        return parseHtPolicyInternal(data);
    }

    private HtConfig parseHtPolicyInternal(String data) {
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
        return parseCtPolicyInternal(data);
    }

    private CtConfig parseCtPolicyInternal(String data) {
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
            if (jobj.has(NTRATE)) {
                baseConfig.setNtRate(jobj.getInt(NTRATE));
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
            if (jobj.has(AUTO_INTERVAL)) {
                adPlace.setAutoInterval(jobj.getLong(AUTO_INTERVAL));
            }
            if (jobj.has(LOAD_ONLY_ONCE)) {
                adPlace.setLoadOnlyOnce(jobj.getBoolean(LOAD_ONLY_ONCE));
            }
            if (jobj.has(ECPMSORT)) {
                adPlace.setEcpmSort(jobj.getInt(ECPMSORT));
            }
            if (jobj.has(NEED_CACHE)) {
                adPlace.setNeedCache(jobj.getInt(NEED_CACHE) == 1);
            }
            if (jobj.has(DELAY_NOTIFY_TIME)) {
                adPlace.setDelayNotifyTime(jobj.getLong(DELAY_NOTIFY_TIME));
            }
            if (jobj.has(REF_SHARE)) {
                adPlace.setRefShare(jobj.getInt(REF_SHARE) == 1);
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
            if (jobj.has(GT_AT_EXCLUSIVE)) {
                adSwitch.setGtAtExclusive(jobj.getInt(GT_AT_EXCLUSIVE) == 1);
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
            SpConfig spConfig = parseSpConfigInternal(jobj);
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
                        spConfig = parseSpConfigInternal(jobj);
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

    private SpConfig parseSpConfigInternal(JSONObject jobj) {
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
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "parseSpConfig error : " + e);
        }
        return spConfig;
    }
}