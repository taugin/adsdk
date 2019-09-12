package com.simple.mpsdk.data.parser;

import android.text.TextUtils;

import com.simple.mpsdk.config.MpConfig;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.utils.AesUtils;
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
        return AesUtils.decrypt(Constant.KEY_PASSWORD, content);
    }

    @Override
    public MpConfig parseAdConfig(String data) {
        data = getContent(data);
        MpConfig adconfig = parseAdConfigInternal(data);
        return adconfig;
    }

    private MpConfig parseAdConfigInternal(String data) {
        MpConfig mpConfig = null;
        try {
            JSONObject jobj = new JSONObject(data);
            List<MpPlace> mpPlaces = null;
            if (jobj.has(ADPLACES)) {
                mpPlaces = parseAdPlaces(jobj.getString(ADPLACES));
            }
            if (mpPlaces != null) {
                mpConfig = new MpConfig();
                mpConfig.setMpPlaceList(mpPlaces);
            }
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "parse mp config error : " + e);
        }
        return mpConfig;
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

    private List<MpPlace> parseAdPlaces(String content) {
        List<MpPlace> list = null;
        try {
            JSONArray jarray = new JSONArray(content);
            int len = jarray.length();
            if (len > 0) {
                list = new ArrayList<MpPlace>(len);
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
    public MpPlace parseAdPlace(String content) {
        MpPlace mpPlace = null;
        try {
            content = getContent(content);
            JSONObject jobj = new JSONObject(content);
            mpPlace = new MpPlace();
            if (jobj.has(NAME)) {
                mpPlace.setName(jobj.getString(NAME));
            }
            if (jobj.has(TYPE)) {
                mpPlace.setType(jobj.getString(TYPE));
            }
            if (jobj.has(PID)) {
                mpPlace.setPid(jobj.getString(PID));
            }
            if (jobj.has(CACHE_TIME)) {
                mpPlace.setCacheTime(jobj.getLong(CACHE_TIME));
            }
            if (jobj.has(LOAD_TIME)) {
                mpPlace.setLoadTime(jobj.getLong(CACHE_TIME));
            }
            if (jobj.has(APPID)) {
                mpPlace.setAid(jobj.getString(APPID));
            }
            if (jobj.has(EXTID)) {
                mpPlace.setEid(jobj.getString(EXTID));
            }
            mpPlace.setUniqueValue(Utils.string2MD5(content.trim()));
        } catch (Exception e) {
            LogHelper.v(LogHelper.TAG, "parse mp place error : " + e);
        }
        return mpPlace;
    }

}