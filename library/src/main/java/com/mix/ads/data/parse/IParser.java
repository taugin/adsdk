package com.mix.ads.data.parse;

import com.mix.ads.data.config.AdPlace;
import com.mix.ads.data.config.PlaceConfig;

import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {
    // 广告位汇总
    String ALL_PLACES = "all_places";
    // 场景前缀
    String APPLOVIN_SDK_KEY = "applovin_sdk_key";
    // 自定义广告位名称
    String NAME = "name";
    // 广告的加载模式 seq, ran, con
    String MODE = "mode";
    // 对于插屏，关闭广告自动切换下一个，对于banner和native，点击自动切换
    String CLICK_SWITCH = "cs";
    // 自动刷新间隔
    String AUTO_INTERVAL = "ai";
    // 开启或关闭单次加载通知
    String LOAD_ONLY_ONCE = "loo";
    // 具体广告位配置
    String PIDS = "pids";
    // 场景缓存, 避免多次加载
    String PLACE_CACHE = "pc";
    // 延迟通知加载成功的时间
    String DELAY_NOTIFY_TIME = "dnt";
    // 是否共享广告场景的广告
    String REF_SHARE = "rs";
    // waterfall 请求间隔, line item失败到下个line item请求的间隔
    String WATERFALL_INTERVAL = "wfi";
    // 顺序加载超时值
    String SEQ_TIMEOUT = "sto";
    // cta按钮颜色
    String CTA_COLOR = "cc";
    // 场景加载失败，自动重试
    String RETRY = "retry";
    // 设置场景id
    String SCENE_ID = "scene_id";
    // 按照ecpm排序
    String ORDER = "order";

    // 广告平台 fb, admob
    String SDK = "sdk";
    // 具体广告平台的广告位ID
    String PID = "pid";
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
    // CPM
    String CPM = "cpm";
    // bannersize
    String BANNER_SIZE = "size";
    // 可点击原生视图
    String CLICK_VIEW = "cv";
    // 加载原生个数
    String LOAD_NATIVE_COUNT = "lnc";
    // 展示比例
    String RATIO = "ratio";
    // admob开屏方向 1：竖屏，2：横屏
    String SPLASH_ORIENTATION = "so";
    // APPID
    String APP_ID = "aid";
    // 是否为模板渲染
    String TEMPLATE = "template";
    // 是否禁止vpn模式加载
    String DISABLE_VPN_LOAD = "dvl";
    // 对于admob使用平均值作为loaded value
    String USE_AVG_VALUE = "uav";
    // 对于admob使用平均值的次数
    String MIN_AVG_COUNT = "mac";
    // 禁止调试模式加载广告
    String DISABLE_DEBUG_LOAD = "ddl";
    // 是否是单次竞价，每次竞价失败都会重新加载
    String REALTIME_BIDDING = "rtb";
    // 是否是备用广告
    String SLAVE_ADS = "slave";

    PlaceConfig parseAdConfig(String data);
    AdPlace parseAdPlace(String data);
    Map<String, String> parseStringMap(String data);
}
