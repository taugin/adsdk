package com.inner.adsdk.constant;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/9.
 */

public class Constant {
    public static final String AD_SDK_COMMON = "common";
    public static final String AD_SDK_MOPUB = "mopub";

    public static final int    NOSET = -1;
    public static final int    BANNER = 1000;
    public static final int    FULL_BANNER = 1001;
    public static final int    LARGE_BANNER = 1002;
    public static final int    LEADERBOARD = 1003;
    public static final int    MEDIUM_RECTANGLE = 1004;
    public static final int    WIDE_SKYSCRAPER = 1005;
    public static final int    SMART_BANNER = 1006;

    public static final int NATIVE_CARD_SMALL = 1;
    public static final int NATIVE_CARD_MEDIUM = 2;
    public static final int NATIVE_CARD_LARGE = 3;
    public static final int NATIVE_CARD_FULL = 4;

    public static final String TYPE_BANNER = "banner";
    public static final String TYPE_NATIVE = "native";
    public static final String TYPE_INTERSTITIAL = "interstitial";
    public static final String TYPE_REWARD = "reward";

    public static final String ECPM = "ecpm";

    public static final String KEY_PASSWORD = "123456789";

    public static final String AF_STATUS = "af_status";
    public static final String AF_MEDIA_SOURCE = "af_media_source";
    public static final String AF_ORGANIC = "Organic";

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
        SMART_BANNER(Constant.SMART_BANNER);

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
                default:
                    return null;
            }
        }

        public int value() {
            return value;
        }
    }
}
