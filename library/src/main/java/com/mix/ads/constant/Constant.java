package com.mix.ads.constant;

import com.mix.ads.MiError;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/9.
 */

public class Constant {
    public static final String AD_SDK_COMMON = "common";
    public static final String AD_SDK_ADMOB = "admob";
    public static final String AD_SDK_APPLOVIN = "applovin";
    public static final String AD_NETWORK_ADMOB = "admob";
    public static final String AD_NETWORK_APPLOVIN = "applovin";
    public static final String AD_NETWORK_FACEBOOK = "facebook";
    public static final String AD_NETWORK_MINTEGRAL = "mintegral";
    public static final String AD_NETWORK_INMOBI = "inmobi";
    public static final String AD_NETWORK_SMAATO = "smaato";
    public static final String AD_NETWORK_UNITY = "unity";
    public static final String AD_NETWORK_TAPJOY = "tapjoy";
    public static final String AD_NETWORK_VUNGLE = "vungle";
    public static final String AD_NETWORK_IRONSOURCE = "ironsource";
    public static final String AD_NETWORK_BIGO = "bigo";
    public static final String AD_NETWORK_UNKNOWN = "unknown";
    public static final String AD_NETWORK_EMPTY = "empty";

    public static final String AD_SDK_PREFIX = "pref_mis_";

    public static final String PREF_GAID = "pref_google_advertise_id";

    public static final int NO_SET = -1;
    public static final int BANNER = 1000;
    public static final int FULL_BANNER = 1001;
    public static final int LARGE_BANNER = 1002;
    public static final int LEADERBOARD = 1003;
    public static final int MEDIUM_RECTANGLE = 1004;
    public static final int WIDE_SKYSCRAPER = 1005;
    public static final int SMART_BANNER = 1006;
    public static final int ADAPTIVE_BANNER = 1007;

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

    // 配置文件中使用的名字++++++
    public static final String PREF_USER_ACTIVE_TIME = "pref_user_active_time";
    // 配置文件中使用的名字======

    public static final String PREF_REMOTE_CONFIG_UPDATE_TIME = "pref_remote_config_update_time";

    public static final int ONE_DAY_MS = 24 * 3600 * 1000;
    public static final MiError AD_ERROR_LOADER = MiError.valueOf("sdk loader error");
    public static final MiError AD_ERROR_UNKNOWN = MiError.valueOf("unknown error");;
    public static final MiError AD_ERROR_CONFIG = MiError.valueOf("sdk config error");;
    public static final MiError AD_ERROR_LOADING = MiError.valueOf("ad is loading");;
    public static final MiError AD_ERROR_LOAD = MiError.valueOf("sdk load error");;
    public static final MiError AD_ERROR_TIMEOUT = MiError.valueOf("load timeout");;
    public static final MiError AD_ERROR_CONTEXT = MiError.valueOf("sdk context error");;
    public static final MiError AD_ERROR_UNSUPPORT = MiError.valueOf("sdk unsupport error");;
    public static final MiError AD_ERROR_NETWORK = MiError.valueOf("sdk network error");;
    public static final MiError AD_ERROR_INVALID_REQUEST = MiError.valueOf("sdk invalid request error");;
    public static final MiError AD_ERROR_INTERNAL = MiError.valueOf("sdk internal error");;
    public static final MiError AD_ERROR_NOFILL = MiError.valueOf("sdk no fill error");;
    public static final MiError AD_ERROR_DISABLE_LOADING = MiError.valueOf("sdk disable error");;
    public static final MiError AD_ERROR_RATIO = MiError.valueOf("sdk ratio error");;
    public static final MiError AD_ERROR_INITIALIZE = MiError.valueOf("sdk initialize error");;
    public static final MiError AD_ERROR_SHOW = MiError.valueOf("sdk show error");;
    public static final MiError AD_ERROR_LIMIT_ADS = MiError.valueOf("sdk limit error");;
    public static final MiError AD_ERROR_DISABLE_VPN = MiError.valueOf("sdk disable load vpn error");;
    public static final MiError AD_ERROR_DISABLE_DEBUG = MiError.valueOf("sdk disable debug error");;
    public static final MiError AD_ERROR_BLOCK_MISTAKE_CLICK = MiError.valueOf("sdk block mistake click error");;

    public static final SimpleDateFormat SDF_ACTIVE_DATE = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
    public static final String PREF_LAST_ELAPSED_TIME = "pref_last_elapsed_time";
    public static final String PREF_LAST_CURRENT_TIME = "pref_last_current_time";

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

    public static final String AD_IMPRESSION = "ad_impression";
    public static final String AD_IMPRESSION_REVENUE = "Ad_Impression_Revenue";
    public static final String AD_VALUE = "value";
    public static final String AD_CURRENCY = "currency";
    public static final String AD_NETWORK = "ad_network";
    public static final String AD_NETWORK_PID = "ad_network_pid";
    public static final String AD_UNIT_ID = "ad_unit_id";
    public static final String AD_TYPE = "ad_type";
    public static final String AD_FORMAT = "ad_format";
    public static final String AD_UNIT_NAME = "ad_unit_name";
    public static final String AD_PLACEMENT = "ad_placement";
    public static final String AD_PLATFORM = "ad_platform";
    public static final String AD_PRECISION = "ad_precision";
    public static final String AD_COUNTRY_CODE = "ad_country_code";
    public static final String AD_SDK_VERSION = "ad_sdk_version";
    public static final String AD_APP_VERSION = "ad_app_version";
    public static final String AD_BIDDING = "ad_bidding";
    public static final String AD_IMPRESSION_ID = "ad_impression_id";
    public static final String AD_IMP_TIME = "ad_imp_time";
}
