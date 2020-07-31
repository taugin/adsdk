package com.hauyu.adsdk.data.parse;

import com.earch.sunny.picfg.SpreadCfg;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.AdSwitch;
import com.hauyu.adsdk.data.config.PlaceConfig;

import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {

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
    // 自动刷新间隔
    String AUTO_INTERVAL = "ai";
    // 开启或关闭单次加载通知
    String LOAD_ONLY_ONCE = "loo";
    // 24小时最大展示次数
    String MAXCOUNT = "maxcount";
    // 展示的百分比
    String PERCENT = "percent";
    // ECPM排序, 1 desc, 0, none, -1, asc
    String ECPM_SORT = "es";
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
    // 优先高ecpm
    String HIGH_ECPM = "he";
    // 场景类型
    String PLACE_TYPE = "pt";
    // 顺序加载超时值
    String SEQ_TIMEOUT = "sto";
    // 队列模式下加载的大小
    String QUEUE_SIZE = "qs";
    // 全屏布局样式 针对GT全屏模式下的布局样式
    String NATIVE_LAYOUT = "nl";
    // cta按钮颜色
    String CTA_COLOR = "cc";
    // 场景加载失败，自动重试
    String RETRY = "retry";
    // 最大重试次数
    String RETRY_TIME = "rt";

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
    // delay to click time
    String DELAY_CLICK_TIME = "dct";
    // destroy after click
    String DESTROY_AFTER_CLICK = "dac";
    // app id
    String APPID = "aid";
    // ext id
    String EXTID = "eid";
    // bannersize
    String BANNER_SIZE = "bs";
    // 可点击原生视图
    String CLICK_VIEWS = "cv";
    // 加载原生个数
    String LOAD_NATIVE_COUNT = "lnc";
    // 使用activity上下文
    String ACTIVITY_CONTEXT = "ac";

    // 是否上报错误日志
    String REPORT_ERROR = "re";
    // 是否上报时间
    String REPORT_TIME = "rt";
    // 是否上报友盟
    String REPORT_UMENG = "ru";
    // 是否上报firebase
    String REPORT_FIREBASE = "rf";
    // 是否上报facebook
    String REPORT_FACEBOOK = "rb";

    // banner
    String BANNER = "banner";
    String ICON = "icon";
    String TITLE = "title";
    String PKGNAME = "pkgname";
    String SUBTITLE = "subtitle";
    String DETAIL = "detail";
    String LINKURL = "linkurl";
    String CTA = "cta";

    PlaceConfig parseAdConfig(String data);

    AdPlace parseAdPlace(String data);

    AdSwitch parseAdSwitch(String data);

    Map<String, String> parseAdRefs(String data);

    List<SpreadCfg> parseSpread(String data);
}
