package com.inner.adsdk.parse;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdPolicy;
import com.inner.adsdk.config.AdSwitch;

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
    String ADPOLICY = "adpolicy";
    String ADPLACES = "adplaces";
    String ADSWITCH = "adswitch";
    String NAME = "name";
    String MODE = "mode";
    String AUTO_SWITCH = "as";

    String MAXCOUNT = "maxcount";
    String PERCENT = "percent";
    String PIDS = "pids";

    String SDK = "sdk";
    String PID = "pid";
    String CTR = "ctr";
    String TYPE = "type";
    String DISABLE = "disable";
    String NOFILL = "nofill";
    String CACHE_TIME = "ctime";

    String ENABLE = "e";
    String UPDELAY = "d";
    String INTERVAL = "i";
    String MAX_COUNT = "mc";
    String MAX_VERSION = "mv";
    String COUNTRY_LIST = "ec";
    String ATTRS = "attr";
    String MEDIA_SOURCE = "ms";

    String BLOCK_LOADING = "bl";
    String REPORT_ERROR = "re";

    AdConfig parseAdConfig(String data);
    AdPlace parseAdPlace(String data);
    AdPolicy parseAdPolicy(String data);
    Map<String, String> parseAdIds(String data);
    AdSwitch parseAdSwitch(String data);
}
