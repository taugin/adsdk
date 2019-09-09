package com.inner.adsdk.parse;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {

    // 广告位汇总
    String ADPLACES = "adplaces";
    // 自定义广告位名称
    String NAME = "name";
    // 具体广告平台的广告位ID
    String PID = "pid";

    String LOAD_TIME = "load_time";
    // 广告类型 interstitial, banner, native
    String TYPE = "type";
    String CACHE_TIME = "cache_time";
    // app id
    String APPID = "aid";
    // ext id
    String EXTID = "eid";

    AdConfig parseAdConfig(String data);

    AdPlace parseAdPlace(String data);
}
