package com.inner.adaggs.framework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.SimpleAdAggsListener;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.manager.DataManager;
import com.inner.adaggs.policy.OuterPolicy;
import com.inner.adaggs.stat.StatImpl;

/**
 * Created by Administrator on 2018/3/19.
 */

public class OuterAdLoader {

    private static OuterAdLoader sOuterAdLoader;

    private Context mContext;
    private AdAggs mAdAggs;

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

    public void init(AdAggs adAggs) {
        mAdAggs = adAggs;
        if (mAdAggs == null) {
            return;
        }
        OuterPolicy.get(mContext).init();
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
        } catch(Exception e) {
        }
        return false;
    }

    private void updateAdPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getLocalAdConfig();
        if (adConfig == null) {
            return;
        }
        AdPolicy adPolicy = DataManager.get(mContext).getRemoteAdPolicy(Constant.ADPOLICY_NAME);
        if (adPolicy == null && adConfig != null) {
            adPolicy = adConfig.getAdPolicy();
        }
        OuterPolicy.get(mContext).setPolicy(adPolicy);
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
        fireOuterAd();
    }

    private void fireOuterAd() {
        if (mAdAggs != null) {
            updateAdPolicy();
            if (!OuterPolicy.get(mContext).shouldShowAdOuter()) {
                return;
            }
            Log.v(Log.TAG, "");
            StatImpl.get().reportAdOuterRequest(mContext);
            mAdAggs.loadComplexAds(Constant.ADPLACE_OUTER_NAME, new SimpleAdAggsListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    StatImpl.get().reportAdOuterLoaded(mContext);
                    if (OuterPolicy.get(mContext).shouldShowAdOuter()) {
                        mAdAggs.showComplexAds(pidName, null);
                        StatImpl.get().reportAdOuterShow(mContext);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    OuterPolicy.get(mContext).reportOuterShowing(false);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.v(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    OuterPolicy.get(mContext).reportOuterShowing(true);
                    StatImpl.get().reportAdOuterShowing(mContext);
                }
            });
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Log.TAG, "intent : " + intent);
            fireOuterAd();
        }
    };
}