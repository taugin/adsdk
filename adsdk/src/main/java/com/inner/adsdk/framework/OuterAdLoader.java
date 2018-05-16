package com.inner.adsdk.framework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.GtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.GtPolicy;
import com.inner.adsdk.stat.StatImpl;

/**
 * Created by Administrator on 2018/3/19.
 */

public class OuterAdLoader {

    private static OuterAdLoader sOuterAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;

    private OuterAdLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public static OuterAdLoader get(Context context) {
        if (sOuterAdLoader == null) {
            create(context);
        }
        return sOuterAdLoader;
    }

    private static void create(Context context) {
        synchronized (OuterAdLoader.class) {
            if (sOuterAdLoader == null) {
                sOuterAdLoader = new OuterAdLoader(context);
            }
        }
    }

    public void init(AdSdk adSdk) {
        mAdSdk = adSdk;
        if (mAdSdk == null) {
            return;
        }
        GtPolicy.get(mContext).init();
        updateAdPolicy();
        if (!hasAlarmService()) {
            Log.d(Log.TAG, "no alarm service, so start loop");
            startLoop();
        } else {
            IntentFilter filter = new IntentFilter(Constant.ACTION_BASIC_ALARM);
            mContext.registerReceiver(mBroadcastReceiver, filter);
        }
    }

    private boolean hasAlarmService() {
        try {
            Class.forName(Constant.ALARM_SERVICE);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    private void updateAdPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getLocalAdConfig();
        if (adConfig == null) {
            return;
        }
        GtConfig gtConfig = DataManager.get(mContext).getRemoteAdPolicy(Constant.ADPOLICY_NAME);
        if (gtConfig == null && adConfig != null) {
            gtConfig = adConfig.getGtConfig();
        }
        GtPolicy.get(mContext).setPolicy(gtConfig);
    }

    public void startLoop() {
        Intent alarmIntent = new Intent(mContext, IService.class);
        alarmIntent.setAction(Constant.ACTION_ALARM);
        alarmIntent.setPackage(mContext.getPackageName());
        PendingIntent pIntent = PendingIntent.getService(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Service.ALARM_SERVICE);
        long pendingTime = SystemClock.elapsedRealtime() + Constant.ALARM_INTERVAL_TIME;
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME, pendingTime, pIntent);
    }

    public void onFire() {
        Log.d(Log.TAG, "onFire");
        DataManager.get(mContext).refresh();
        fireOuterAd();
    }

    private void fireOuterAd() {
        if (mAdSdk != null) {
            updateAdPolicy();
            if (!GtPolicy.get(mContext).shouldShowAdOuter()) {
                return;
            }
            Log.v(Log.TAG, "");
            StatImpl.get().reportAdOuterRequest(mContext);
            mAdSdk.loadComplexAds(Constant.ADPLACE_OUTER_NAME, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    StatImpl.get().reportAdOuterLoaded(mContext);
                    if (GtPolicy.get(mContext).shouldShowAdOuter()) {
                        mAdSdk.showComplexAds(pidName, null);
                        StatImpl.get().reportAdOuterShow(mContext);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportOuterShowing(false);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    GtPolicy.get(mContext).reportOuterShowing(true);
                    StatImpl.get().reportAdOuterShowing(mContext);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                }
            });
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onFire();
        }
    };
}