package com.inner.adsdk.parse;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.config.AtConfig;

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

    // 各个广告平台的appid
    String ADIDS = "adids";
    // 应用外的配置信息
    String GTCONFIG = "gtconfig";
    // ST配置
    String STCONFIG = "stconfig";
    // TT配置
    String ATCONFIG = "atconfig";
    // 广告位汇总
    String ADPLACES = "adplaces";
    // 开光控制配置信息
    String ADSWITCH = "adswitch";
    // 广告位引用影射表
    String ADREFS = "adrefs";
    // 自定义广告位名称
    String NAME = "name";
    // 广告的加载模式 seq, ran, con
    String MODE = "mode";
    // 对于插屏，关闭广告自动切换下一个，对于banner和native，点击自动切换
    String AUTO_SWITCH = "as";
    // 开启或关闭单次加载通知
    String LOAD_ONLY_ONCE = "loo";
    // 24小时最大展示次数
    String MAXCOUNT = "maxcount";
    // 展示的百分比
    String PERCENT = "percent";
    // ECPM排序, 1 desc, 0, none, -1, asc
    String ECPMSORT = "es";
    // 具体广告位配置
    String PIDS = "pids";
    // 场景缓存
    String NEED_CACHE = "nc";
    // 延迟通知加载成功的时间
    String DELAY_NOTIFY_TIME = "dnt";
    // 是否共享广告场景的广告
    String REF_SHARE = "rs";

    // 广告平台 fb, admob, adx
    String SDK = "sdk";
    // 具体广告平台的广告位ID
    String PID = "pid";
    // 控制原生广告的点击区域百分比
    String CTR = "ctr";
    // 广告类型 interstitial, banner, native
    String TYPE = "type";
    // 禁用某个广告
    String DISABLE = "disable";
    // 广告未填充的间隔时间
    String NOFILL = "nofill";
    String CACHE_TIME = "ctime";
    // 广告缓存超时值
    String TIMEOUT = "to";
    // 延迟通知加载成功的时间
    String DELAY_LOAD_TIME = "dlt";
    // ECPM
    String ECPM = "ecpm";
    // finish for ctr
    String FINISH_FORCTR = "ffc";

    // 启用或禁用GT
    String ENABLE = "e";
    // 首次启动后多久允许GT
    String UPDELAY = "d";
    // GT的展示间隔
    String INTERVAL = "i";
    // 24小时GT的最大展示次数
    String MAX_COUNT = "mc";
    // 此配置设置生效的最大应用版本
    String MAX_VERSION = "mv";
    // 最小请求间隔，如果为0，则默认1分钟
    String MIN_INTERVAL = "mi";
    // 国家列表 !en表示排除的国家，en表示包含的国家
    String COUNTRY_LIST = "ec";
    // 归因配置 Organic 或者 Non-organic
    String ATTRS = "attr";
    // 媒体列表 !adwords表示排除的媒体，adwords表示包含的媒体
    String MEDIA_SOURCE = "ms";
    // 屏幕方向 0 : undefined, 1: portrait, 2 : landscape
    String SCREEN_ORIENTATION = "so";

    // 阻止重复加载(如果当前loader正在处于加载中，则不再重新加载)
    String BLOCK_LOADING = "bl";
    // 是否上报错误日志
    String REPORT_ERROR = "re";
    // 是否上报时间
    String REPORT_TIME = "rt";
    // 是否上报友盟
    String REPORT_UMENG = "ru";
    // 是否上报Appsflyer
    String REPORT_APPSFLYER = "ra";
    // 是否上报firebase
    String REPORT_FIREBASE = "rf";
    // 是否上报facebook
    String REPORT_FACEBOOK = "rfb";
    // 排除的报名
    String EXCLUDE_PACKAGES = "ep";
    // gt at exclusive
    String GT_AT_EXCLUSIVE = "gae";
    // at show on first page
    String SHOW_ON_FIRST_PAGE = "sofp";


    AdConfig parseAdConfig(String data);

    AdPlace parseAdPlace(String data);

    GtConfig parseGtPolicy(String data);

    StConfig parseStPolicy(String data);

    AtConfig parseAtPolicy(String data);

    Map<String, String> parseAdIds(String data);

    AdSwitch parseAdSwitch(String data);

    Map<String, String> parseAdRefs(String data);
}
