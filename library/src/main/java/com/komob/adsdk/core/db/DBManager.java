package com.komob.adsdk.core.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.komob.adsdk.AdImpData;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.log.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public void updateClickTimes(String impressionId) {
        if (TextUtils.isEmpty(impressionId)) {
            return;
        }
        String sql1 = String.format(Locale.ENGLISH, "update %s set %s=%s+1 where %s='%s'", DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_CLICK_COUNT, DBHelper.AD_CLICK_COUNT, DBHelper.AD_IMPRESSION_ID, impressionId);
        String sql2 = String.format(Locale.ENGLISH, "update %s set %s=%s where %s='%s' and %s='0'", DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_CLICK_TIME, System.currentTimeMillis(), DBHelper.AD_IMPRESSION_ID, impressionId, DBHelper.AD_CLICK_TIME);
        // Log.iv(Log.TAG, "update click count sql : " + sql1);
        // Log.iv(Log.TAG, "update click time sql : " + sql2);
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
            values.put(DBHelper.AD_IMPRESSION_ID, adImpData.getImpressionId());
            values.put(DBHelper.AD_UNIT_ID, adImpData.getUnitId());
            values.put(DBHelper.AD_UNIT_NAME, adImpData.getUnitName());
            values.put(DBHelper.AD_PLACEMENT, adImpData.getPlacement());
            values.put(DBHelper.AD_SDK_VERSION, adImpData.getSdkVersion());
            values.put(DBHelper.AD_APP_VERSION, adImpData.getAppVersion());
            values.put(DBHelper.AD_TYPE, adImpData.getAdType());
            values.put(DBHelper.AD_UNIT_FORMAT, adImpData.getAdFormat());
            values.put(DBHelper.AD_CURRENCY, adImpData.getCurrency());
            values.put(DBHelper.AD_REVENUE, adImpData.getValue());
            values.put(DBHelper.AD_PRECISION, adImpData.getPrecision());
            values.put(DBHelper.AD_NETWORK, adImpData.getNetwork());
            values.put(DBHelper.AD_NETWORK_PID, adImpData.getNetworkPid());
            values.put(DBHelper.AD_PLATFORM, adImpData.getPlatform());
            values.put(DBHelper.AD_COUNTRY, adImpData.getCountryCode());

            long now = System.currentTimeMillis();
            sSdf.setTimeZone(TimeZone.getDefault());
            values.put(DBHelper.AD_IMP_DATE, sSdf.format(new Date(now)));
            values.put(DBHelper.AD_IMP_TIME, now);
            values.put(DBHelper.AD_ACTIVE_DATE, getActiveDate());
            values.put(DBHelper.AD_ACTIVE_TIME, DataManager.get(mContext).getFirstActiveTime());
            values.put(DBHelper.AD_CLICK_COUNT, 0);
            return values;
        } catch (Exception e) {
        }
        return null;
    }

    private String getActiveDate() {
        String activeDate;
        try {
            long userActiveTime = DataManager.get(mContext).getFirstActiveTime();
            activeDate = Constant.SDF_ACTIVE_DATE.format(new Date(userActiveTime));
        } catch (Exception e) {
            activeDate = "00-00";
        }
        return activeDate;
    }

    public double queryAdRevenue() {
        return queryAdRevenue(null);
    }

    public double queryAdRevenue(String datetime) {
        double adRevenue = 0.0f;
        String sql = null;
        if (TextUtils.isEmpty(datetime)) {
            sql = String.format(Locale.ENGLISH, "select sum(%s) as total_revenue from %s",
                    DBHelper.AD_REVENUE, DBHelper.TABLE_AD_IMPRESSION);
        } else {
            sql = String.format(Locale.ENGLISH, "select sum(%s) as total_revenue from %s where %s='%s'",
                    DBHelper.AD_REVENUE, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_IMP_DATE, datetime);
        }
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
        return adRevenue;
    }

    @SuppressLint("Range")
    public List<AdImpData> queryAllImps() {
        List<AdImpData> list = null;
        String sql = String.format(Locale.ENGLISH, "select * from %s order by %s desc", DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_IMP_TIME);
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                Map<String, Object> objectMap = null;
                do {
                    objectMap = new HashMap<>();
                    objectMap.put(Constant.AD_UNIT_NAME, cursor.getString(cursor.getColumnIndex(DBHelper.AD_UNIT_NAME)));
                    objectMap.put(Constant.AD_UNIT_ID, cursor.getString(cursor.getColumnIndex(DBHelper.AD_UNIT_ID)));
                    objectMap.put(Constant.AD_TYPE, cursor.getString(cursor.getColumnIndex(DBHelper.AD_TYPE)));
                    objectMap.put(Constant.AD_FORMAT, cursor.getString(cursor.getColumnIndex(DBHelper.AD_UNIT_FORMAT)));
                    objectMap.put(Constant.AD_PLATFORM, cursor.getString(cursor.getColumnIndex(DBHelper.AD_PLATFORM)));
                    objectMap.put(Constant.AD_PLACEMENT, cursor.getString(cursor.getColumnIndex(DBHelper.AD_PLACEMENT)));
                    objectMap.put(Constant.AD_NETWORK, cursor.getString(cursor.getColumnIndex(DBHelper.AD_NETWORK)));
                    objectMap.put(Constant.AD_NETWORK_PID, cursor.getString(cursor.getColumnIndex(DBHelper.AD_NETWORK_PID)));
                    objectMap.put(Constant.AD_VALUE, cursor.getDouble(cursor.getColumnIndex(DBHelper.AD_REVENUE)));
                    objectMap.put(Constant.AD_IMP_TIME, cursor.getLong(cursor.getColumnIndex(DBHelper.AD_IMP_TIME)));
                    list.add(AdImpData.createAdImpData(objectMap));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public double queryAverageRevenue(String pid, int minCount) {
        String sql = String.format(Locale.ENGLISH, "select avg(%s) as revenue_avg, count(%s) as imp_count from %s where %s='%s'", DBHelper.AD_REVENUE, DBHelper.AD_UNIT_ID, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_UNIT_ID, pid);
        Cursor cursor = null;
        double averageRevenue = 0f;
        int impCount = 0;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                averageRevenue = cursor.getDouble(0);
                impCount = cursor.getInt(1);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        double finalRevenue = 0f;
        if (impCount >= minCount) {
            finalRevenue = averageRevenue;
        }
        Log.iv(Log.TAG, "pid [" + pid + "] avg : " + averageRevenue + " , final value : " + finalRevenue + " , imp count : " + impCount + " , min count : " + minCount);
        return finalRevenue;
    }

    public List<Map<String, Object>> queryAllAdType() {
        List<Map<String, Object>> list = null;
        String sql = String.format(Locale.ENGLISH, "select %s, sum(%s) as revenue_sum, count(%s) as imp_count from %s group by %s", DBHelper.AD_TYPE, DBHelper.AD_REVENUE, DBHelper.AD_TYPE, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_TYPE);
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    Map<String, Object> allTypeMap = new HashMap<>();
                    String adType = cursor.getString(0);
                    double typeRevenue = cursor.getDouble(1);
                    int typeImp = cursor.getInt(2);
                    allTypeMap.put("ad_type", adType);
                    allTypeMap.put("ad_type_revenue", typeRevenue);
                    allTypeMap.put("ad_type_impression", typeImp);
                    list.add(allTypeMap);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public boolean isAdClicked(String impressionId) {
        if (TextUtils.isEmpty(impressionId)) {
            return true;
        }
        String sql = String.format(Locale.ENGLISH, "select %s from %s where %s='%s' and %s>0", DBHelper.AD_IMPRESSION_ID, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_IMPRESSION_ID, impressionId, DBHelper.AD_CLICK_COUNT);
        Cursor cursor = null;
        int count = 0;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count > 0;
    }

    public String queryAdPlacement(String impressionId) {
        String sql = String.format(Locale.ENGLISH, "select %s from %s where %s='%s'", DBHelper.AD_PLACEMENT, DBHelper.TABLE_AD_IMPRESSION, DBHelper.AD_IMPRESSION_ID, impressionId);
        Cursor cursor = null;
        String adPlacement = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                adPlacement = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return adPlacement;
    }

    public void insertOrUpdateClick(String bundle, long clickTime) {
        SpreadClickInfo spreadClickInfo = queryClickSpread(bundle);
        if (spreadClickInfo != null) {
            updateClickInfo(spreadClickInfo._id, clickTime, spreadClickInfo.clickCount + 1);
        } else {
            insertClickInfo(bundle, clickTime);
        }
    }

    private void insertClickInfo(String bundle, long clickTime) {
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(DBHelper.AD_SPREAD_BUNDLE, bundle);
        values.put(DBHelper.AD_SPREAD_CLICK_TIME, clickTime);
        values.put(DBHelper.AD_SPREAD_CLICK_COUNT, 1);
        try {
            db = mDBHelper.getReadableDatabase();
            db.beginTransaction();
            db.insertOrThrow(DBHelper.TABLE_AD_SPREAD, DBHelper.FOO, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    private void updateClickInfo(int _id, long clickTime, int clickCount) {
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(DBHelper.AD_SPREAD_CLICK_TIME, clickTime);
        values.put(DBHelper.AD_SPREAD_CLICK_COUNT, clickCount);
        try {
            db = mDBHelper.getReadableDatabase();
            db.beginTransaction();
            db.update(DBHelper.TABLE_AD_SPREAD, values, DBHelper._ID + "=?", new String[]{String.valueOf(_id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    @SuppressLint("Range")
    public SpreadClickInfo queryClickSpread(String bundle) {
        Cursor cursor = null;
        SpreadClickInfo spreadClickInfo = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            cursor = db.query(true, DBHelper.TABLE_AD_SPREAD, null, DBHelper.AD_SPREAD_BUNDLE + "=?", new String[]{bundle}, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                spreadClickInfo = new SpreadClickInfo();
                spreadClickInfo._id = cursor.getInt(cursor.getColumnIndex(DBHelper._ID));
                spreadClickInfo.clickCount = cursor.getInt(cursor.getColumnIndex(DBHelper.AD_SPREAD_CLICK_COUNT));
                spreadClickInfo.installCount = cursor.getInt(cursor.getColumnIndex(DBHelper.AD_SPREAD_INSTALL_COUNT));
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return spreadClickInfo;
    }

    public void updateInstallTime(int _id, long installTime, int installCount) {
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(DBHelper.AD_SPREAD_INSTALL_TIME, installTime);
        values.put(DBHelper.AD_SPREAD_INSTALL_COUNT, installCount);
        try {
            db = mDBHelper.getReadableDatabase();
            db.beginTransaction();
            db.update(DBHelper.TABLE_AD_SPREAD, values, DBHelper._ID + "=?", new String[]{String.valueOf(_id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public class SpreadClickInfo {
        public int _id;
        public int clickCount;
        public int installCount;
    }
}
