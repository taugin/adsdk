package com.inner.adsdk.constant;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/9.
 */

public class Constant {
    public static final String AD_SDK_COMMON = "common";
    public static final String AD_SDK_ADMOB = "admob";
    public static final String AD_SDK_FACEBOOK = "fb";
    public static final String AD_SDK_ADX = "adx";
    public static final String AD_SDK_WEMOB = "wemob";
    public static final String AD_SDK_DFP = "dfp";
    public static final String AD_SDK_APPLOVIN = "applovin";
    public static final String AD_SDK_MOPUB = "mopub";
    public static final String AD_SDK_APPNEXT = "appnext";
    public static final String AD_SDK_SPREAD = "spread";
    public static final String AD_SDK_INMOBI = "inmobi";
    public static final String AD_SDK_INNERACTIVE = "inneractive";
    public static final String AD_SDK_DSPMOB = "dspmob";
    public static final String AD_SDK_DAP = "dap";
    public static final String AD_SDK_ALTAMOB = "altamob";
    public static final String AD_SDK_CLOUDMOBI = "cloudmobi";
    public static final String AD_SDK_MOBVISTA = "mobvista";
    public static final String AD_SDK_DISPLAYIO = "displayio";

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

    public static final String MODE_SEQ = "seq";
    public static final String MODE_CON = "con";
    public static final String MODE_RAN = "ran";

    public static final String TYPE_BANNER = "banner";
    public static final String TYPE_NATIVE = "native";
    public static final String TYPE_INTERSTITIAL = "interstitial";
    public static final String TYPE_REWARD = "reward";

    public static final String ECPM = "ecpm";

    public static final String APPKEY = "appkey";

    public static final String CHANNEL = "channel";

    public static final String KEY_PASSWORD = "123456789";

    public static final long   ONE_DAY_TIME = 24 * 60 * 60 * 1000;

    public static final String PREF_FIRST_STARTUP_TIME = "pref_first_startup_time";

    public static final String PREF_REMOTE_CONFIG_REQUEST_TIME = "pref_remote_config_request_time";

    public static final String PREF_LAST_SHOWTIME = "pref_%s_last_showtime";
    public static final String PREF_TOTAL_SHOWTIMES = "pref_%s_total_showtimes";
    public static final String PREF_FIRST_SHOWTIME_ONEDAY = "pref_%s_first_showtime_oneday";
    public static final String PREF_REQUEST_TIME = "pref_%s_request_time";
    public static final String PREF_CHARGE_MONITOR = "pref_cm_monitor";

    // 配置文件中使用的名字++++++
    public static final String GTPLACE_OUTER_NAME = "gt_outer_place";
    public static final String ATPLACE_OUTER_NAME = "at_outer_place";
    public static final String STPLACE_OUTER_NAME = "st_outer_place";
    public static final String NTPLACE_OUTER_NAME = "nt_outer_place";
    public static final String LTPLACE_OUTER_NAME = "lt_outer_place";
    public static final String HTPLACE_OUTER_NAME = "ht_outer_place";
    public static final String CTPLACE_OUTER_NAME = "ct_outer_place";
    public static final String ADIDS_NAME = "adids";
    public static final String GTPOLICY_NAME = "gtconfig";
    public static final String STPOLICY_NAME = "stconfig";
    public static final String ATPOLICY_NAME = "atconfig";
    public static final String LTPOLICY_NAME = "ltconfig";
    public static final String HTPOLICY_NAME = "htconfig";
    public static final String CTPOLICY_NAME = "ctconfig";
    public static final String ADSWITCH_NAME = "adswitch";
    public static final String ADREFS_NAME = "adrefs";
    public static final String ADSPREAD_NAME = "spconfig";
    // 配置文件中使用的名字======

    public static final String AF_STATUS = "af_status";
    public static final String AF_MEDIA_SOURCE = "af_media_source";

    public static final int AD_ERROR_CONFIG = 1;
    public static final int AD_ERROR_FILLTIME = 2;
    public static final int AD_ERROR_LOADING = 3;
    public static final int AD_ERROR_LOAD = 4;
    public static final int AD_ERROR_TIMEOUT = 5;
    public static final int AD_ERROR_CONTEXT = 6;
    public static final int AD_ERROR_UNSUPPORT = 7;

    public static final SimpleDateFormat SDF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
}
