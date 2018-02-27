package com.inner.adaggs.parse;

import android.content.Context;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdvInner;
import com.inner.adaggs.config.AdvOuter;
import com.inner.adaggs.config.DevInfo;
import com.inner.adaggs.config.InnerPolicy;
import com.inner.adaggs.config.OuterPolicy;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.framework.Aes;
import com.inner.adaggs.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdParser implements IParser {

    private Context mContext;

    @Override
    public AdConfig parse(String content) {
        if (!checkWhiteList(content)) {
            return null;
        }
        String data = getData(content);
        AdConfig adConfig = parseAdConfig(data);
        return adConfig;
    }

    private boolean checkWhiteList(String content) {
        List<DevInfo> list = parseWhiteList(content);
        if (list == null || list.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 解析白名单
     *
     * @param content
     * @return
     */
    private List<DevInfo> parseWhiteList(String content) {
        List<DevInfo> list = null;
        try {
            JSONObject jobj = new JSONObject(content);
            JSONArray jarray = null;
            if (jobj.has("wdevs")) {
                jarray = jobj.getJSONArray("wdevs");
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
                        if (j.has("imei")) {
                            info.setImei(j.getString("imei"));
                        }
                        if (j.has("aid")) {
                            info.setAndroidId(j.getString("aid"));
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

    private String getData(String content) {
        try {
            JSONObject jobj = new JSONObject(content);
            int status = -1;
            String data = null;
            if (jobj.has("s")) {
                status = jobj.getInt("s");
            }
            if (jobj.has("data")) {
                data = jobj.getString("data");
            }
            if (status == 0) {
                return data;
            }
            if (status == 1) {
                return Aes.decrypt(data, Constant.KEY_PASSWORD);
            }
            Log.e(Log.TAG, "error format");
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private AdConfig parseAdConfig(String data) {
        AdConfig adConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            AdvInner advInner = null;
            AdvOuter advOuter = null;
            if (jobj.has("advinner")) {
                advInner = parseAdvInner(jobj.getString("advinner"));
            }
            if (jobj.has("advouter")) {
                advOuter = parseAdvOuter(jobj.getString("advouter"));
            }
            if (advInner != null || advOuter != null) {
                adConfig = new AdConfig();
                adConfig.setAdvInner(advInner);
                adConfig.setAdvOuter(advOuter);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return adConfig;
    }

    private AdvInner parseAdvInner(String content) {
        AdvInner advInner = null;
        try {
            JSONObject jobj = new JSONObject(content);
            InnerPolicy innerPolicy = null;
            List<AdPlace> adPlaces = null;
            if (jobj.has("policy")) {
                innerPolicy = parseInnerPolicy(jobj.getString("policy"));
            }
            if (jobj.has("adplaces")) {
                adPlaces = parseAdPlaces(jobj.getString("adplaces"));
            }
            if (innerPolicy != null || adPlaces != null) {
                advInner = new AdvInner();
                advInner.setInnerPolicy(innerPolicy);
                advInner.setAdPlaceList(adPlaces);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return advInner;
    }

    private InnerPolicy parseInnerPolicy(String content) throws Exception {
        return null;
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
            if (jobj.has("name")) {
                adPlace.setName(jobj.getString("name"));
            }
            if (jobj.has("mode")) {
                adPlace.setMode(jobj.getString("mode"));
            }
            if (jobj.has("maxcount")) {
                adPlace.setMaxCount(jobj.getInt("maxcount"));
            }
            if (jobj.has("percent")) {
                adPlace.setPercent(jobj.getInt("percent"));
            }
            if (jobj.has("pids")) {
                adPlace.setPidsList(parsePidList(jobj.getString("pids")));
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
            if (jobj.has("sdk")) {
                pidConfig.setSdk(jobj.getString("sdk"));
            }
            if (jobj.has("pid")) {
                pidConfig.setPid(jobj.getString("pid"));
            }
            if (jobj.has("ctr")) {
                pidConfig.setCtr(jobj.getInt("ctr"));
            }
            if (jobj.has("type")) {
                pidConfig.setAdType(jobj.getString("type"));
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return pidConfig;
    }

    private AdvOuter parseAdvOuter(String content) throws Exception {
        AdvOuter advOuter = null;
        try {
            JSONObject jobj = new JSONObject(content);
            OuterPolicy outerPolicy = null;
            AdPlace adPop = null;
            AdPlace adFilm = null;
            if (jobj.has("policy")) {
                outerPolicy = parseOuterPolicy(jobj.getString("policy"));
            }
            if (jobj.has("adpop")) {
                adPop = parseAdPlace(jobj.getString("adpop"));
            }
            if (jobj.has("adfilm")) {
                adFilm = parseAdPlace(jobj.getString("adfilm"));
            }
            if (outerPolicy != null || adPop != null || adFilm != null) {
                advOuter = new AdvOuter();
                advOuter.setOuterPolicy(outerPolicy);
                advOuter.setAdPop(adPop);
                advOuter.setAdFilm(adFilm);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return advOuter;
    }

    private OuterPolicy parseOuterPolicy(String content) throws Exception {
        return null;
    }
}
