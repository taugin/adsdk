package com.rabbit.adsdk.constant;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/9.
 */

public class Constant {
    public static final String AD_SDK_COMMON = "common";
    public static final String AD_SDK_ADMOB = "admob";
    public static final String AD_SDK_SPREAD = "spread";
    public static final String AD_SDK_APPLOVIN = "applovin";
    public static final String AD_SDK_TRADPLUS = "tradplus";

    public static final String AD_SDK_PREFIX = "pref_hauyu_";

    public static final String PREF_GAID = "pref_gaid";

    public static final int NO_SET = -1;
    public static final int BANNER = 1000;
    public static final int FULL_BANNER = 1001;
    public static final int LARGE_BANNER = 1002;
    public static final int LEADERBOARD = 1003;
    public static final int MEDIUM_RECTANGLE = 1004;
    public static final int WIDE_SKYSCRAPER = 1005;
    public static final int SMART_BANNER = 1006;
    public static final int ADAPTIVE_BANNER = 1007;

    public static final String NATIVE_CARD_MICRO = "micro";
    public static final String NATIVE_CARD_TINY = "tiny";
    public static final String NATIVE_CARD_LITTLE = "little";
    public static final String NATIVE_CARD_SMALL = "small";
    public static final String NATIVE_CARD_MEDIUM = "medium";
    public static final String NATIVE_CARD_LARGE = "large";
    public static final String NATIVE_CARD_ROUND = "round";
    public static final String NATIVE_CARD_FULL = "full";
    public static final String NATIVE_CARD_WRAP = "wrap";
    public static final String NATIVE_CARD_HEAD = "head";
    public static final String NATIVE_CARD_MIX = "mix";
    public static final String NATIVE_CARD_FOOT = "foot";

    public static final List<String> NATIVE_CARD_FULL_LIST = Arrays.asList(NATIVE_CARD_ROUND, NATIVE_CARD_FULL, NATIVE_CARD_WRAP, NATIVE_CARD_HEAD, NATIVE_CARD_MIX, NATIVE_CARD_FOOT);

    public static final String MODE_SEQ = "seq";
    public static final String MODE_CON = "con";
    public static final String MODE_RAN = "ran";

    public static final String TYPE_BANNER = "banner";
    public static final String TYPE_NATIVE = "native";
    public static final String TYPE_INTERSTITIAL = "interstitial";
    public static final String TYPE_REWARD = "reward";
    public static final String TYPE_SPLASH = "splash";

    public static final String PLACE_TYPE_ADVIEW = "adview";
    public static final String PLACE_TYPE_INTERSTITIAL = "interstitial";
    public static final String PLACE_TYPE_REWARD_VIDEO = "reward";
    public static final String PLACE_TYPE_SPLASH = "splash";
    public static final String PLACE_TYPE_COMPLEX = "complex";

    public static final String KEY_PASSWORD = "123456789";

    public static final String APP_ID = "app_id";

    // 配置文件中使用的名字++++++
    public static final String SHARE_PLACE = "share_place";
    public static final String COMPLEX_PLACES = "cfg_complex_info";
    public static final List<String> DEFAULT_COMPLEX_ORDER = Arrays.asList(new String[]{Constant.TYPE_INTERSTITIAL, Constant.TYPE_NATIVE, Constant.TYPE_BANNER, Constant.TYPE_REWARD});
    public static final String PREF_USER_ACTIVE_TIME = "pref_user_active_time";
    // 配置文件中使用的名字======

    public static final String AF_STATUS = "af_status";
    public static final String AF_MEDIA_SOURCE = "af_media_source";
    public static final String AF_ORGANIC = "Organic";
    public static final String PREF_REMOTE_CONFIG_UPDATE_TIME = "pref_remote_config_update_time";

    public static final int ONE_DAY_MS = 24 * 3600 * 1000;

    /**
     * ADLOADER错误
     */
    public static final int AD_ERROR_LOADER = -1;
    /**
     * 未知错误
     */
    public static final int AD_ERROR_UNKNOWN = 0;
    /**
     * 配置错误
     */
    public static final int AD_ERROR_CONFIG = 1;
    /**
     * 无填充
     */
    public static final int AD_ERROR_FILLTIME = 2;
    /**
     * 正在加载中
     */
    public static final int AD_ERROR_LOADING = 3;
    /**
     * 加载失败
     */
    public static final int AD_ERROR_LOAD = 4;
    /**
     * 超时
     */
    public static final int AD_ERROR_TIMEOUT = 5;
    /**
     * 上下文错误
     */
    public static final int AD_ERROR_CONTEXT = 6;
    /**
     * 不支持
     */
    public static final int AD_ERROR_UNSUPPORT = 7;

    /**
     * 加载太频繁
     */
    public static final int AD_ERROR_TOO_FREQUENCY = 8;

    /**
     * 网络错误
     */
    public static final int AD_ERROR_NETWORK = 9;

    /**
     * 无效请求
     */
    public static final int AD_ERROR_INVALID_REQUEST = 10;

    /**
     * 内部错误
     */
    public static final int AD_ERROR_INTERNAL = 11;

    /**
     * 无填充
     */
    public static final int AD_ERROR_NOFILL = 12;

    /**
     * 服务器错误
     */
    public static final int AD_ERROR_SERVER = 13;

    /**
     * 中介错误
     */
    public static final int AD_ERROR_MEDIATION = 14;

    /**
     * 无效的pid
     */
    public static final int AD_ERROR_INVALID_PID = 15;

    /**
     * 判断adloader是否被过滤
     */
    public static final int AD_ERROR_DISABLE_LOADING = 17;

    /**
     * 判断adloader是否满足展示比率
     */
    public static final int AD_ERROR_RATIO = 18;

    /**
     * 停止队列模式加载
     */
    public static final int AD_ERROR_QUEUE = 19;

    /**
     * 初始化失败
     */
    public static final int AD_ERROR_INITIALIZE = 20;

    /**
     * 展示失败
     */
    public static final int AD_ERROR_SHOW = 21;

    /**
     * 排除变现平台
     */
    public static final int AD_ERROR_LIMIT_ADS = 22;

    /**
     * 超出最大请求次数
     */
    public static final int AD_ERROR_EXCEED_REQ_TIME = 23;


    public static final int AD_ERROR_DISABLE_VPN = 23;

    public static final int AD_ERROR_DISABLE_DEBUG = 24;

    public static final int AD_ERROR_SIGN_NOT_MATCH = 25;

    public static final int AD_ERROR_PACK_NOT_MATCH = 26;

    public static final int AD_ERROR_BLOCK_MISTAKE_CLICK = 27;

    public static final SimpleDateFormat SDF_ACTIVE_DATE = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat SDF_ACTIVE_YEAR = new SimpleDateFormat("yyyy", Locale.ENGLISH);

    public static class Banner {
        public static int valueOf(String bannerSize) {
            switch (bannerSize) {
                case "BANNER":
                    return Constant.BANNER;
                case "FULL_BANNER":
                    return Constant.FULL_BANNER;
                case "LARGE_BANNER":
                    return Constant.LARGE_BANNER;
                case "LEADERBOARD":
                    return Constant.LEADERBOARD;
                case "MEDIUM_RECTANGLE":
                    return Constant.MEDIUM_RECTANGLE;
                case "WIDE_SKYSCRAPER":
                    return Constant.WIDE_SKYSCRAPER;
                case "SMART_BANNER":
                    return Constant.SMART_BANNER;
                case "ADAPTIVE_BANNER":
                    return Constant.ADAPTIVE_BANNER;
                default:
                    return Constant.NO_SET;
            }
        }
    }

    public static final String AD_IMPRESSION_REVENUE = "Ad_Impression_Revenue";
    public static final String AD_TOTAL_ADS_REVENUE_001 = "Total_Ads_Revenue_001";
    public static final String AD_VALUE = "value";
    public static final String AD_MICRO_VALUE = "micro_value";
    public static final String AD_ROUND_CPM = "round_cpm";
    public static final String AD_ROUND_CPM_NEW = "round_cpm_new";
    public static final String AD_CURRENCY = "currency";
    public static final String AD_NETWORK = "ad_network";
    public static final String AD_NETWORK_PID = "ad_network_pid";
    public static final String AD_UNIT_ID = "ad_unit_id";
    public static final String AD_TYPE = "ad_type";
    public static final String AD_FORMAT = "ad_format";
    public static final String AD_UNIT_NAME = "ad_unit_name";
    public static final String AD_PLACEMENT = "ad_placement";
    public static final String AD_PLACEMENT_NEW = "ad_placement_new";
    public static final String AD_PLATFORM = "ad_platform";
    public static final String AD_PRECISION = "ad_precision";
    public static final String AD_COUNTRY_CODE = "ad_country_code";
    public static final String AD_SDK_VERSION = "ad_sdk_version";
    public static final String AD_APP_VERSION = "ad_app_version";
    public static final String AD_BIDDING = "ad_bidding";
    public static final String AD_GAID = "ad_gaid";
    public static final String AD_IMPRESSION_ID = "ad_impression_id";

    public static final String PREF_LAST_APP_ACTIVE_DATE = "pref_last_app_active_date";
}
