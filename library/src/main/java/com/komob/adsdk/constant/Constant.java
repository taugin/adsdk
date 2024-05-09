package com.komob.adsdk.constant;

import com.komob.adsdk.AdError;

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
    public static final String NATIVE_CARD_RECT = "rect";
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

    public static final String PREF_REMOTE_CONFIG_UPDATE_TIME = "pref_remote_config_update_time";
    public static final String PREF_REMOTE_CONFIG_SUFFIX = "pref_remote_config_suffix";

    public static final int ONE_DAY_MS = 24 * 3600 * 1000;

    public static final AdError AD_ERROR_UNKNOWN = AdError.valueOf("unknown error");
    public static final AdError AD_ERROR_SUCCESS = AdError.valueOf("success");
    public static final AdError AD_ERROR_LOADER = AdError.valueOf("sdk loader error");
    public static final AdError AD_ERROR_CONFIG = AdError.valueOf("sdk config error");
    public static final AdError AD_ERROR_LOADING = AdError.valueOf("ad is loading");
    public static final AdError AD_ERROR_LOAD = AdError.valueOf("sdk load error");
    public static final AdError AD_ERROR_TIMEOUT = AdError.valueOf("load timeout");
    public static final AdError AD_ERROR_CONTEXT = AdError.valueOf("sdk context error");
    public static final AdError AD_ERROR_UNSUPPORT = AdError.valueOf("sdk unsupport error");
    public static final AdError AD_ERROR_NETWORK = AdError.valueOf("sdk network error");
    public static final AdError AD_ERROR_INVALID_REQUEST = AdError.valueOf("sdk invalid request error");
    public static final AdError AD_ERROR_INTERNAL = AdError.valueOf("sdk internal error");
    public static final AdError AD_ERROR_TOO_FREQUENCY = AdError.valueOf("load too frequency error");
    public static final AdError AD_ERROR_NOFILL = AdError.valueOf("sdk no fill error");
    public static final AdError AD_ERROR_DISABLE_LOADING = AdError.valueOf("sdk disable error");
    public static final AdError AD_ERROR_RATIO = AdError.valueOf("sdk ratio error");
    public static final AdError AD_ERROR_INITIALIZE = AdError.valueOf("sdk initialize error");
    public static final AdError AD_ERROR_SHOW = AdError.valueOf("sdk show error");
    public static final AdError AD_ERROR_LIMIT_ADS = AdError.valueOf("sdk limit error");
    public static final AdError AD_ERROR_DISABLE_VPN = AdError.valueOf("sdk disable load vpn error");
    public static final AdError AD_ERROR_DISABLE_DEBUG = AdError.valueOf("sdk disable debug error");
    public static final AdError AD_ERROR_BLOCK_MISTAKE_CLICK = AdError.valueOf("sdk block mistake click error");
    public static final AdError AD_ERROR_PACK_NOT_MATCH = AdError.valueOf("pack not match");
    public static final AdError AD_ERROR_SIGN_NOT_MATCH = AdError.valueOf("sign not match");
    public static final AdError AD_ERROR_EXCEED_REQ_TIME = AdError.valueOf("exceed max req times");


    public static final SimpleDateFormat SDF_ACTIVE_DATE = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat SDF_ACTIVE_YEAR = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat SDF_WHOLE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
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
    public static final String AD_IMP_TIME = "ad_imp_time";
}
