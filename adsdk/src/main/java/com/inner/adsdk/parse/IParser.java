package com.inner.adsdk.parse;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdPolicy;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {

    String WHITE_LIST = "wlist";
    String IMEI = "imei";
    String AID = "aid";
    String STATUS = "s";
    String DATA = "data";

    String ADIDS = "adids";
    String POLICY = "policy";
    String ADPLACES = "adplaces";
    String NAME = "name";
    String MODE = "mode";
    String PTYPE = "ptype";

    String MAXCOUNT = "maxcount";
    String PERCENT = "percent";
    String PIDS = "pids";

    String SDK = "sdk";
    String PID = "pid";
    String CTR = "ctr";
    String TYPE = "type";
    String DISABLE = "disable";

    String ENABLE = "e";
    String UPDELAY = "d";
    String INTERVAL = "i";
    String MAX_COUNT = "mc";
    String MAX_VERSION = "mv";
    String COUNTRY_LIST = "ec";
    String ATTRS = "attr";
    String MEDIA_SOURCE = "ms";

    AdConfig parseAdConfig(String data);
    AdPlace parseAdPlace(String data);
    AdPolicy parseAdPolicy(String data);
    Map<String, String> parseAdIds(String data);
}
