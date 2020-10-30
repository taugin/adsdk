package com.bacad.ioc.gsb.data.parse;

import com.bacad.ioc.gsb.base.ScFl;
import com.bacad.ioc.gsb.scconfig.CvCg;
import com.bacad.ioc.gsb.scconfig.GvCg;
import com.bacad.ioc.gsb.scconfig.HvCg;
import com.bacad.ioc.gsb.scconfig.LvCg;
import com.bacad.ioc.gsb.scconfig.SvCg;


/**
 * Created by Administrator on 2018/2/9.
 */

public interface ISceneParser {

    // 广告缓存超时值
    String TIMEOUT = "to";
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
    // 版本列表 !ver表示排除的版本，ver表示包含的版本
    String VER_LIST = "vl";
    // 屏幕方向 0 : undefined, 1: portrait, 2 : landscape
    String SCREEN_ORIENTATION = "so";
    // 是否展示底层activity
    String SHOW_BOTTOM = "sb";
    // 禁用间隔
    String DISABLE_INTERVAL = "di";
    // view广告位名称
    String AD_MAIN = "adm";
    // 场景间隔
    String SCENE_INTERVAL = "si";
    // 延迟关闭时间
    String DELAY_CLOSE = "dc";
    // 最小系统版本
    String OS_VER = "osver";
    // 使用实时时间
    String USE_REAL_TIME = "urt";
    // 排除的ip列表
    String EX_IPS = "exips";
    // 激活时间列表
    String FAT_ADM = "fatadm";
    // 版本列表
    String VER_ADM = "veradm";
    // 解锁显示类型
    String LT_TYPE = "lt";
    // 使用full intent
    String USE_FULL_INTENT = "ufi";

    // 是否上报友盟
    String REPORT_UMENG = "ru";
    // 是否上报firebase
    String REPORT_FIREBASE = "rf";
    // 是否上报facebook
    String REPORT_FACEBOOK = "rb";

    GvCg parseGtPolicy(String data);

    SvCg parseStPolicy(String data);

    LvCg parseLtPolicy(String data);

    HvCg parseHtPolicy(String data);

    CvCg parseCtPolicy(String data);

    ScFl parseSceneFlag(String data);
}
