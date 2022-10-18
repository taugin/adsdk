package com.rabbit.adsdk.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rabbit.adsdk.AdImpData;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DBManager {

    private static final SimpleDateFormat sSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static DBManager sDBManager;


    public static DBManager get(Context context) {
        synchronized (DBManager.class) {
            if (sDBManager == null) {
                createInstance(context);
            }
        }
        return sDBManager;
    }

    private static void createInstance(Context context) {
        synchronized (DBManager.class) {
            if (sDBManager == null) {
                sDBManager = new DBManager(context);
            }
        }
    }

    private final Context mContext;
    private final DBHelper mDBHelper;

    private DBManager(Context context) {
        mContext = context;
        mDBHelper = new DBHelper(context);
    }

    public boolean insertAdImpression(AdImpData mpImpData) {
        if (mpImpData == null) {
            return false;
        }
        SQLiteDatabase db = null;
        ContentValues values = impDateToContentValues(mpImpData);
        try {
            db = mDBHelper.getReadableDatabase();
            db.beginTransaction();
            db.insertOrThrow(DBHelper.TABLE_AD_IMPRESSION, DBHelper.FOO, values);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
        return false;
    }

    public void updateClickTimes(String requestId) {
        String sql1 = String.format(Locale.ENGLISH, "update %s set %s=%s+1 where %s='%s'", DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_CLICK_COUNT, DBHelper.AD_CLICK_COUNT, DBHelper.AD_REQUEST_ID, requestId);
        String sql2 = String.format(Locale.ENGLISH, "update %s set %s=%s where %s='%s' and %s='0'", DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_CLICK_TIME, System.currentTimeMillis(), DBHelper.AD_REQUEST_ID, requestId, DBHelper.AD_CLICK_TIME);
        Log.iv(Log.TAG, "update click count sql : " + sql1);
        Log.iv(Log.TAG, "update click time sql : " + sql2);
        SQLiteDatabase db = null;
        try {
            db = mDBHelper.getReadableDatabase();
            db.beginTransaction();
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    private ContentValues impDateToContentValues(AdImpData adImpData) {
        if (adImpData == null) {
            return null;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.AD_REQUEST_ID, adImpData.getRequestId());
            values.put(DBHelper.AD_UNIT_ID, adImpData.getUnitId());
            values.put(DBHelper.AD_UNIT_NAME, adImpData.getUnitName());
            values.put(DBHelper.AD_PLACEMENT, adImpData.getPlacement());
            values.put(DBHelper.AD_SDK_VERSION, adImpData.getSdkVersion());
            values.put(DBHelper.AD_APP_VERSION, adImpData.getAppVersion());
            values.put(DBHelper.AD_UNIT_TYPE, adImpData.getType());
            values.put(DBHelper.AD_UNIT_FORMAT, adImpData.getFormat());
            values.put(DBHelper.AD_CURRENCY, adImpData.getCurrency());
            values.put(DBHelper.AD_REVENUE, adImpData.getValue());
            values.put(DBHelper.AD_PRECISION, adImpData.getPrecision());
            values.put(DBHelper.AD_NETWORK_NAME, adImpData.getNetwork());
            values.put(DBHelper.AD_NETWORK_PID, adImpData.getNetworkPid());
            values.put(DBHelper.AD_PLATFORM, adImpData.getPlatform());
            values.put(DBHelper.AD_COUNTRY, adImpData.getCountryCode());

            long now = System.currentTimeMillis();
            sSdf.setTimeZone(TimeZone.getDefault());
            values.put(DBHelper.AD_IMP_DATE, sSdf.format(new Date(now)));
            values.put(DBHelper.AD_IMP_TIME, now);
            values.put(DBHelper.AD_ACTIVE_DATE, EventImpl.get().getActiveDate());
            values.put(DBHelper.AD_ACTIVE_TIME, DataManager.get(mContext).getFirstActiveTime());
            values.put(DBHelper.AD_CLICK_COUNT, 0);
            return values;
        } catch (Exception e) {
        }
        return null;
    }

    public double queryAdRevenue(String datetime) {
        double adRevenue = 0.0f;
        String sql = String.format(Locale.ENGLISH, "select sum(%s) as total_revenue from %s where %s='%s'",
                DBHelper.AD_REVENUE, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_IMP_DATE, datetime);
        Log.iv(Log.TAG, "calculate ad revenue sql : " + sql);
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                adRevenue = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.iv(Log.TAG, "calculate ad revenue : " + adRevenue);
        return adRevenue;
    }
}
