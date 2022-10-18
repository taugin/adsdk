package com.rabbit.adsdk.core.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rabbit.adsdk.log.Log;


public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "ad_rabbit.db";
    public static final String _ID = "_id";
    public static final String FOO = "foo";

    public static final String TABLE_AD_IMPRESSION = "ad_impression";
    public static final String AD_REQUEST_ID = "ad_request_id";
    public static final String AD_UNIT_ID = "ad_unit_id";
    public static final String AD_UNIT_NAME = "ad_unit_name";
    public static final String AD_PLACEMENT = "ad_placement";
    public static final String AD_SDK_VERSION = "ad_sdk_version";
    public static final String AD_APP_VERSION = "ad_app_version";
    public static final String AD_UNIT_TYPE = "ad_unit_type";
    public static final String AD_UNIT_FORMAT = "ad_unit_format";
    public static final String AD_CURRENCY = "ad_currency";
    public static final String AD_REVENUE = "ad_revenue";
    public static final String AD_PRECISION = "ad_precision";
    public static final String AD_NETWORK_NAME = "ad_network_name";
    public static final String AD_NETWORK_PID = "ad_network_pid";
    public static final String AD_PLATFORM = "ad_platform";
    public static final String AD_COUNTRY = "ad_country";
    public static final String AD_IMP_DATE = "ad_imp_date";
    public static final String AD_IMP_TIME = "ad_imp_time";
    public static final String AD_ACTIVE_DATE = "ad_active_date";
    public static final String AD_ACTIVE_TIME = "ad_active_time";
    public static final String AD_CLICK_COUNT = "ad_click_count";
    public static final String AD_CLICK_TIME = "ad_click_time";
    public static final String RESERVE_TEXT = "reserve_text";
    public static final String RESERVE_LONG = "reserve_long";

    public static final String AD_IMP_TIME_INDEX = "ad_imp_time_index";
    /**
     * 去掉_id字段是为了不显示更新后的整形字段
     */
    // /////////////////////////////////////////////////////////////////////////
    /**
     * 创建组织表
     */
    private static final String CREATE_AD_IMPRESSION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_AD_IMPRESSION
                    + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + AD_REQUEST_ID + " TEXT,"
                    + AD_UNIT_ID + " TEXT,"
                    + AD_UNIT_NAME + " TEXT,"
                    + AD_PLACEMENT + " TEXT,"
                    + AD_SDK_VERSION + " TEXT,"
                    + AD_APP_VERSION + " TEXT,"
                    + AD_UNIT_TYPE + " TEXT,"
                    + AD_UNIT_FORMAT + " TEXT,"
                    + AD_CURRENCY + " TEXT,"
                    + AD_REVENUE + " DOUBLE,"
                    + AD_PRECISION + " TEXT,"
                    + AD_NETWORK_NAME + " TEXT,"
                    + AD_NETWORK_PID + " TEXT,"
                    + AD_PLATFORM + " TEXT,"
                    + AD_COUNTRY + " TEXT,"
                    + AD_IMP_DATE + " TEXT,"
                    + AD_IMP_TIME + " LONG DEFAULT 0,"
                    + AD_ACTIVE_DATE + " TEXT,"
                    + AD_ACTIVE_TIME + " LONG DEFAULT 0,"
                    + AD_CLICK_COUNT + " INTEGER DEFAULT 0,"
                    + AD_CLICK_TIME + " LONG DEFAULT 0,"
                    + RESERVE_TEXT + " TEXT,"
                    + RESERVE_LONG + " LONG DEFAULT 0,"
                    + FOO + " TEXT"
                    + ")";

    private static final String DROP_ORG_TABLE = "DROP TABLE " + TABLE_AD_IMPRESSION;

    private static final String CREATE_DATETIME_UNIQUE_INDEX =
            "CREATE UNIQUE INDEX " + AD_IMP_TIME_INDEX
                    + " ON " + TABLE_AD_IMPRESSION + "(" + AD_IMP_TIME + ");";
    private static final String DROP_DATETIME_UNIQUE_INDEX =
            "DROP INDEX " + AD_IMP_TIME_INDEX;

    private Context mContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_AD_IMPRESSION_TABLE);
            db.execSQL(CREATE_DATETIME_UNIQUE_INDEX);
        } catch (SQLException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(Log.TAG, "oldVersion : " + oldVersion + " , newVersion : " + newVersion);
        if (newVersion > oldVersion) {
            try {
                db.execSQL(DROP_ORG_TABLE);
                db.execSQL(DROP_DATETIME_UNIQUE_INDEX);
            } catch (SQLException e) {
                Log.d(Log.TAG, "error : " + e);
            } finally {
                onCreate(db);
            }
        }
    }

}
