package com.simple.mpsdk.data.parser;

import android.text.TextUtils;

import com.simple.mpsdk.config.AdConfig;
import com.simple.mpsdk.config.AdPlace;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.framework.Aes;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AdConfigParser implements IConfigParser {

    private String getContent(String content) {
        JSONObject jobj = null;
        JSONArray jarray = null;
        try {
            jobj = new JSONObject(content);
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "error : invalid json object");
        }
        if (jobj != null) {
            return jobj.toString();
        }
        try {
            jarray = new JSONArray(content);
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "error : invalid json array");
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
            List<AdPlace> adPlaces = null;
            if (jobj.has(ADPLACES)) {
                adPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (adPlaces != null) {
                adConfig = new AdConfig();
                adConfig.setAdPlaceList(adPlaces);
            }
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "parseAdConfigInternal error : " + e);
        }
        return adConfig;
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
            LogHelper.e(LogHelper.TAG, "error : " + e);
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
            if (jobj.has(TYPE)) {
                adPlace.setType(jobj.getString(TYPE));
            }
            if (jobj.has(PID)) {
                adPlace.setPid(jobj.getString(PID));
            }
            if (jobj.has(CACHE_TIME)) {
                adPlace.setCacheTime(jobj.getLong(CACHE_TIME));
            }
            if (jobj.has(LOAD_TIME)) {
                adPlace.setLoadTime(jobj.getLong(CACHE_TIME));
            }
            if (jobj.has(APPID)) {
                adPlace.setAid(jobj.getString(APPID));
            }
            if (jobj.has(EXTID)) {
                adPlace.setEid(jobj.getString(EXTID));
            }
            adPlace.setUniqueValue(Utils.string2MD5(content.trim()));
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "parseAdPlace error : " + e);
        }
        return adPlace;
    }

}