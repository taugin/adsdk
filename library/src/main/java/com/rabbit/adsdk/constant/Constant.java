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
    public static final String AD_SDK_FACEBOOK = "fb";
    public static final String AD_SDK_SPREAD = "spread";
    public static final String AD_SDK_MOPUB = "mopub";
    public static final String AD_SDK_APPLOVIN = "applovin";
    public static final String AD_SDK_MINTEGRAL = "mintegral";
    public static final String AD_SDK_INMOBI = "inmobi";

    public static final String AD_SDK_PREFIX = "pref_hauyu_";

    public static final String PREF_GAID = "pref_gaid";

    public static final int NOSET = -1;
    public static final int BANNER = 1000;
    public static final int FULL_BANNER = 1001;
    public static final int LARGE_BANNER = 1002;
    public static final int LEADERBOARD = 1003;
    public static final int MEDIUM_RECTANGLE = 1004;
    public static final int WIDE_SKYSCRAPER = 1005;
    public static final int SMART_BANNER = 1006;
    public static final int ADAPTIVE_BANNER = 1007;

    public static final int NATIVE_CARD_SMALL = 1;
    public static final int NATIVE_CARD_MEDIUM = 2;
    public static final int NATIVE_CARD_LARGE = 3;
    public static final int NATIVE_CARD_FULL = 4;
    public static final int NATIVE_CARD_TINY = 5;
    public static final int NATIVE_CARD_MICRO = 6;
    public static final int NATIVE_CARD_WRAP = 7;
    public static final int NATIVE_CARD_HEAD = 8;
    public static final int NATIVE_CARD_MIX = 9;
    public static final int NATIVE_CARD_FOOT = 10;
    public static final int NATIVE_CARD_ROUND = 11;
    public static final int NATIVE_CARD_LITTLE = 12;

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
    public static final String PLACE_TYPE_REWARDEDVIDEO = "reward";
    public static final String PLACE_TYPE_SPLASH = "splash";
    public static final String PLACE_TYPE_COMPLEX = "complex";

    public static final String ECPM = "ecpm";

    public static final String KEY_PASSWORD = "123456789";

    public static final long ONE_DAY_TIME = 24 * 60 * 60 * 1000;

    // 配置文件中使用的名字++++++
    public static final String ADREFS_NAME = "adrefs";
    public static final String COMPLEX_PLACES = "placecfg";
    public static final List<String> DEFAULT_COMPLEX_ORDER = Arrays.asList(new String[]{Constant.TYPE_INTERSTITIAL, Constant.TYPE_NATIVE, Constant.TYPE_BANNER, Constant.TYPE_REWARD});
    public static final String PREF_USER_ACTIVE_TIME = "pref_user_active_time";
    public static final String AD_MEDIATION_CONFIG = "mdncfg";
    // 配置文件中使用的名字======

    public static final String AF_STATUS = "af_status";
    public static final String AF_MEDIA_SOURCE = "af_media_source";
    public static final String AF_ORGANIC = "Organic";
    public static final String PREF_REMOTE_CONFIG_UPDATE_TIME = "pref_remote_config_update_time";

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
     * 被判断为作弊用户
     */
    public static final int AD_ERROR_BLOCK_ADS = 16;

    /**
     * 判断adloader是否被过滤
     */
    public static final int AD_ERROR_FILTERED = 17;

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

    public static final SimpleDateFormat SDF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    public static final SimpleDateFormat SDF_LEFT_TIME = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public enum Banner {
        NOSET(Constant.NOSET),
        BANNER(Constant.BANNER),
        FULL_BANNER(Constant.FULL_BANNER),
        LARGE_BANNER(Constant.LARGE_BANNER),
        LEADERBOARD(Constant.LEADERBOARD),
        MEDIUM_RECTANGLE(Constant.MEDIUM_RECTANGLE),
        WIDE_SKYSCRAPER(Constant.WIDE_SKYSCRAPER),
        SMART_BANNER(Constant.SMART_BANNER),
        ADAPTIVE_BANNER(Constant.ADAPTIVE_BANNER);

        private int value = 0;

        private Banner(int value) {     //必须是private的，否则编译错误
            this.value = value;
        }

        public static Banner valueOf(int value) {    //手写的从int到enum的转换函数
            switch (value) {
                case Constant.NOSET:
                    return NOSET;
                case Constant.BANNER:
                    return BANNER;
                case Constant.FULL_BANNER:
                    return FULL_BANNER;
                case Constant.LARGE_BANNER:
                    return LARGE_BANNER;
                case Constant.LEADERBOARD:
                    return LEADERBOARD;
                case Constant.MEDIUM_RECTANGLE:
                    return MEDIUM_RECTANGLE;
                case Constant.WIDE_SKYSCRAPER:
                    return WIDE_SKYSCRAPER;
                case Constant.SMART_BANNER:
                    return SMART_BANNER;
                case Constant.ADAPTIVE_BANNER:
                    return ADAPTIVE_BANNER;
                default:
                    return NOSET;
            }
        }

        public int value() {
            return value;
        }
    }
}
