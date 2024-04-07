package com.mix.ads.core.db;

public interface DBConstant {
    int DB_VERSION = 1;
    String _ID = "_id";
    String FOO = "foo";

    String TABLE_AD_IMPRESSION = "ad_impression";
    String AD_IMPRESSION_ID = "ad_request_id";
    String AD_UNIT_ID = "ad_unit_id";
    String AD_UNIT_NAME = "ad_unit_name";
    String AD_PLACEMENT = "ad_placement";
    String AD_SDK_VERSION = "ad_sdk_version";
    String AD_APP_VERSION = "ad_app_version";
    String AD_TYPE = "ad_type";
    String AD_UNIT_FORMAT = "ad_unit_format";
    String AD_CURRENCY = "ad_currency";
    String AD_REVENUE = "ad_revenue";
    String AD_PRECISION = "ad_precision";
    String AD_NETWORK = "ad_network";
    String AD_NETWORK_PID = "ad_network_pid";
    String AD_PLATFORM = "ad_platform";
    String AD_COUNTRY = "ad_country";
    String AD_IMP_DATE = "ad_imp_date";
    String AD_IMP_TIME = "ad_imp_time";
    String AD_ACTIVE_DATE = "ad_active_date";
    String AD_ACTIVE_TIME = "ad_active_time";
    String AD_CLICK_COUNT = "ad_click_count";
    String AD_CLICK_TIME = "ad_click_time";
    String RESERVE_TEXT = "reserve_text";
    String RESERVE_LONG = "reserve_long";

    String AD_IMP_TIME_INDEX = "ad_imp_time_index";

    String TABLE_AD_SPREAD = "ad_spread";
    String AD_SPREAD_BUNDLE = "ad_spread_bundle";
    String AD_SPREAD_CLICK_TIME = "ad_spread_click_time";
    String AD_SPREAD_CLICK_COUNT = "ad_spread_click_count";
    String AD_SPREAD_INSTALL_TIME = "ad_spread_install_time";
    String AD_SPREAD_INSTALL_COUNT = "ad_spread_install_count";
}
