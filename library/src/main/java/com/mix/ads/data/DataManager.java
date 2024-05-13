package com.mix.ads.data;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.mix.ads.constant.Constant;
import com.mix.ads.data.config.AdPlace;
import com.mix.ads.data.config.PlaceConfig;
import com.mix.ads.data.config.SpreadConfig;
import com.mix.ads.data.parse.AdParser;
import com.mix.ads.data.parse.IParser;
import com.mix.ads.log.Log;
import com.mix.ads.utils.Utils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/2/12.
 */

public class DataManager {

    private static final String DATA_CONFIG_FORMAT = "data_%s";
    private static final String DATA_CONFIG = "cfg_data_config";
    private static final String CONFIG_SUFFIX1 = ".dat";
    private static final String CONFIG_SUFFIX2 = ".json";
    private static DataManager sDataManager;

    public static DataManager get(Context context) {
        synchronized (DataManager.class) {
            if (sDataManager == null) {
                createInstance(context);
            }
        }
        return sDataManager;
    }

    private static void createInstance(Context context) {
        synchronized (DataManager.class) {
            if (sDataManager == null) {
                sDataManager = new DataManager(context);
            }
        }
    }

    private DataManager(Context context) {
        mContext = context;
        mParser = new AdParser();
    }

    private Context mContext;
    private PlaceConfig mLocalPlaceConfig;
    private PlaceConfig mRemotePlaceConfig;
    private IParser mParser;
    private boolean mAdmobInTestMode;
    private boolean mApplovinInTestMode;

    public void init() {
        recordFistActiveTime();
        VRemoteConfig.get(mContext).init();
        parseLocalData();
        printGoogleAdvertisingId();
    }

    public void setAdmobInTestMode(boolean inTestMode) {
        mAdmobInTestMode = inTestMode;
    }

    public boolean isAdmobInTestMode() {
        return mAdmobInTestMode;
    }

    public void setApplovinInTestMode(boolean inTestMode) {
        mApplovinInTestMode = inTestMode;
    }

    public boolean isApplovinInTestMode() {
        return mApplovinInTestMode;
    }

    private void parseLocalData() {
        if (mLocalPlaceConfig == null && mParser != null) {
            String cfgName = getConfigName();
            String defName = getDefaultName();
            Log.iv(Log.TAG_SDK, "name : " + cfgName + "/" + defName);
            String data = Utils.readConfig(mContext, cfgName + CONFIG_SUFFIX1);
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, cfgName + CONFIG_SUFFIX2);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, defName + CONFIG_SUFFIX1);
            }
            if (TextUtils.isEmpty(data)) {
                data = Utils.readConfig(mContext, defName + CONFIG_SUFFIX2);
            }
            mLocalPlaceConfig = mParser.parseAdConfig(data);
            if (mLocalPlaceConfig != null) {
                mLocalPlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                Log.iv(Log.TAG, "locale data has been set success");
            }
        }
    }

    private void parseRemoteData() {
        String data = getString(DATA_CONFIG);
        if (!TextUtils.isEmpty(data)
                && (mRemotePlaceConfig == null || !TextUtils.equals(mRemotePlaceConfig.getAdConfigMd5(), Utils.string2MD5(data)))) {
            if (mParser != null) {
                mRemotePlaceConfig = mParser.parseAdConfig(data);
                if (mRemotePlaceConfig != null) {
                    mRemotePlaceConfig.setAdConfigMd5(Utils.string2MD5(data));
                }
            }
        }
    }

    public PlaceConfig getAdConfig() {
        parseRemoteData();
        parseLocalData();
        if (mRemotePlaceConfig != null) {
            return mRemotePlaceConfig;
        }
        return mLocalPlaceConfig;
    }

    private String getMd5SubString() {
        try {
            String pkgmd5 = Utils.string2MD5(mContext.getPackageName());
            pkgmd5 = pkgmd5.toLowerCase(Locale.ENGLISH);
            return pkgmd5.substring(0, 8);
        } catch (Exception e) {
        }
        return "";
    }

    private String getConfigName() {
        String cfgName = null;
        try {
            cfgName = "c" + getMd5SubString() + "fg";
        } catch (Exception e) {
        }
        return cfgName;
    }

    private String getDefaultName() {
        return String.format(Locale.ENGLISH, DATA_CONFIG_FORMAT, "config");
    }

    public AdPlace getRemoteAdPlace(String key) {
        String data = getString(key);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseAdPlace(data);
        }
        return null;
    }

    public List<SpreadConfig> getRemoteSpread() {
        String data = getString(SpreadConfig.AD_SPREAD_NAME);
        if (!TextUtils.isEmpty(data)) {
            return mParser.parseSpread(data);
        }
        return null;
    }

    public String getString(String key) {
        return DataConfigRemote.get(mContext).getString(key);
    }

    private void printGoogleAdvertisingId() {
        new Thread() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                    String gaid = info.getId();
                    boolean isLimited = info.isLimitAdTrackingEnabled();
                    Log.iv(Log.TAG, "google advertising id (gaid) : " + gaid + " , is limit ad tracking : " + isLimited);
                    Utils.putString(mContext, Constant.PREF_GAID, gaid);
                } catch (Exception | Error e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
            }
        }.start();
    }

    public boolean isDisableVpn() {
        if (mRemotePlaceConfig != null) {
            return mRemotePlaceConfig.isDisableVpnLoad();
        }
        if (mLocalPlaceConfig != null) {
            return mLocalPlaceConfig.isDisableVpnLoad();
        }
        return false;
    }

    public String getApplovinSdkKey() {
        if (mRemotePlaceConfig != null && !TextUtils.isEmpty(mRemotePlaceConfig.getApplovinSdkKey())) {
            return mRemotePlaceConfig.getApplovinSdkKey();
        }
        if (mLocalPlaceConfig != null && !TextUtils.isEmpty(mLocalPlaceConfig.getApplovinSdkKey())) {
            return mLocalPlaceConfig.getApplovinSdkKey();
        }
        return null;
    }

    /**
     * 验证广告位是否存在
     *
     * @param placeName
     * @return
     */
    public boolean isPlaceValidate(String placeName) {
        AdPlace adPlace = null;
        if (mLocalPlaceConfig != null) {
            adPlace = mLocalPlaceConfig.get(placeName);
        }
        if (adPlace == null) {
            adPlace = getRemoteAdPlace(placeName);
        }
        return adPlace != null;
    }

    private void recordFistActiveTime() {
        try {
            long time = Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
            if (time <= 0) {
                Utils.putLong(mContext, Constant.PREF_USER_ACTIVE_TIME, System.currentTimeMillis());
            }
        } catch (Exception e) {
        }
    }

    public long getFirstActiveTime() {
        return Utils.getLong(mContext, Constant.PREF_USER_ACTIVE_TIME, 0);
    }

    public long getElapsedTimeMillis() {
        long elapsedTime = SystemClock.elapsedRealtime();
        long currentTime = System.currentTimeMillis();
        long lastElapsedTime = Utils.getLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, 0);
        long lastCurrentTime = Utils.getLong(mContext, Constant.PREF_LAST_CURRENT_TIME, 0);
        if (lastElapsedTime <= 0) {
            // 首次获取时间
            Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
            Utils.putLong(mContext, Constant.PREF_LAST_CURRENT_TIME, currentTime);
            return currentTime;
        }
        if (elapsedTime < lastElapsedTime) {
            // 重启手机后，首次获取时间
            Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
            return lastCurrentTime;
        }
        long finalTime;
        long elapsedDiff = elapsedTime - lastElapsedTime;
        long currentDiff = currentTime - lastCurrentTime;
        long deviation = Math.abs(currentDiff - elapsedDiff);
        if (deviation < 10000) {
            finalTime = currentTime;
        } else {
            finalTime = lastCurrentTime + elapsedDiff;
        }
        Utils.putLong(mContext, Constant.PREF_LAST_ELAPSED_TIME, elapsedTime);
        Utils.putLong(mContext, Constant.PREF_LAST_CURRENT_TIME, finalTime);
        // Log.iv(Log.TAG, "elapsed diff : " + elapsedDiff + " , current diff : " + currentDiff + " , deviation : " + deviation + " , final time : " + Constant.SDF_WHOLE_TIME.format(finalTime));
        return finalTime;
    }

    public int getActiveDays() {
        int activeDays = -1;
        try {
            Calendar calendar = Calendar.getInstance();
            int nowYear = calendar.get(Calendar.YEAR);
            int nowMonth = calendar.get(Calendar.MONTH) + 1;
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long nowDate = calendar.getTimeInMillis();

            long userActiveTime = getFirstActiveTime();
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(userActiveTime);
            int activeYear = calendar.get(Calendar.YEAR);
            int activeMonth = calendar.get(Calendar.MONTH) + 1;
            int activeDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long activeDate = calendar.getTimeInMillis();

            try {
                Log.iv(Log.TAG_SDK, String.format("now : %d-%02d-%02d , active : %d-%02d-%02d, nowDate : %d , activeDate : %d", nowYear, nowMonth, nowDay, activeYear, activeMonth, activeDay, nowDate, activeDate));
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            activeDays = Long.valueOf((nowDate - activeDate) / Constant.ONE_DAY_MS).intValue();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            activeDays = -1;
        }
        if (activeDays < 0) {
            activeDays = 0;
        }
        return activeDays;
    }
}
