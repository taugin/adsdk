package com.rabbit.adsdk.data.parse;

import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PlaceConfig;
import com.rabbit.sunny.SpreadCfg;

import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {

    // 广告位汇总
    String ADPLACES = "adplaces";
    // 广告位引用影射表
    String ADREFS = "adrefs";
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
    // 24小时最大展示次数
    String MAXCOUNT = "maxcount";
    // 展示的百分比
    String PERCENT = "percent";
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
    // 全屏布局样式 针对GT全屏模式下的布局样式
    String NATIVE_LAYOUT = "nl";
    // cta按钮颜色
    String CTA_COLOR = "cc";
    // 场景加载失败，自动重试
    String RETRY = "retry";

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
    // ECPM
    String ECPM = "ecpm";
    // bannersize
    String BANNER_SIZE = "bs";
    // 可点击原生视图
    String CLICK_VIEW = "cv";
    // 可点击原生视图
    String CLICK_VIEW_RENDER = "cvr";
    // 加载原生个数
    String LOAD_NATIVE_COUNT = "lnc";
    // 使用activity上下文
    String ACTIVITY_CONTEXT = "ac";
    // 展示比例
    String RATIO = "ratio";
    // 子原生布局
    String SUB_NATIVE_LAYOUT = "snl";

    // banner
    String BANNER = "banner";
    String ICON = "icon";
    String TITLE = "title";
    String PKGNAME = "pkgname";
    String DETAIL = "detail";
    String LINKURL = "linkurl";
    String CTA = "cta";

    PlaceConfig parseAdConfig(String data);

    AdPlace parseAdPlace(String data);

    Map<String, String> parseAdRefs(String data);

    Map<String, Map<String, String>> parseMediationConfig(String data);

    List<SpreadCfg> parseSpread(String data);
}
