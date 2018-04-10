package com.inner.adaggs.parse;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.config.DevInfo;

import java.util.List;
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

    String ENABLE = "e";
    String UPDELAY = "d";
    String INTERVAL = "interval";
    String MAX_COUNT = "mc";
    String MAX_VERSION = "mv";
    String COUNTRY_LIST = "cl";
    String ATTRS = "attrs";
    String MEDIA_SOURCE = "ms";

    int parseStatus(String data);
    List<DevInfo> parseDevList(String data);
    String parseContent(String data);
    AdConfig parse(String data);
    AdPlace parseAdPlace(String data);
    AdPolicy parseAdPolicy(String data);
    Map<String, String> parseAdIds(String data);
}
