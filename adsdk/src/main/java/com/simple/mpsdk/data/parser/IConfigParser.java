package com.simple.mpsdk.data.parser;

import com.simple.mpsdk.config.MpConfig;
import com.simple.mpsdk.config.MpPlace;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IConfigParser {

    // 广告位汇总
    String ADPLACES = "placements";
    // 自定义广告位名称
    String NAME = "name";
    // 具体广告平台的广告位ID
    String PID = "place_id";

    String LOAD_TIME = "load_time";
    // 广告类型 interstitial, banner, native
    String TYPE = "type";
    String CACHE_TIME = "cache_time";
    // app id
    String APPID = "aid";
    // ext id
    String EXTID = "eid";

    MpConfig parseAdConfig(String data);

    MpPlace parseAdPlace(String data);
}