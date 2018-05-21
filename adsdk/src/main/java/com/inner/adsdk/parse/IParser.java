package com.inner.adsdk.parse;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.GtConfig;
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
    String GTPOLICY = "gtconfig";
    String ADPLACES = "adplaces";
    String ADSWITCH = "adswitch";
    String NAME = "name";
    String MODE = "mode";
    // 对于插屏，关闭广告自动切换下一个，对于banner和native，点击自动切换
    String AUTO_SWITCH = "as";
    // 开启或关闭单次加载通知
    String LOAD_ONLY_ONCE = "loo";

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
    String TIMEOUT = "to";
    String DELAY_LOAD_TIME = "dlt";

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
    GtConfig parseGtPolicy(String data);
    Map<String, String> parseAdIds(String data);
    AdSwitch parseAdSwitch(String data);
}
