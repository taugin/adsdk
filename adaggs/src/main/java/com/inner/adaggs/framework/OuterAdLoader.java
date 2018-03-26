package com.inner.adaggs.framework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.inner.adaggs.AdAggs;
import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdPolicy;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.SimpleAdAggsListener;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.manager.DataManager;
import com.inner.adaggs.manager.PolicyManager;
import com.inner.adaggs.stat.StatImpl;

/**
 * Created by Administrator on 2018/3/19.
 */

public class OuterAdLoader {

    private static OuterAdLoader sOuterAdLoader;

    private Context mContext;
    private AdAggs mAdAggs;
    private AdPlace mOuterPlace;
    private AdPlace mFilmPlace;

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
        PolicyManager.get(mContext).init();
        mAdAggs = adAggs;
        if (mAdAggs == null) {
            return;
        }
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        if (adConfig == null) {
            return;
        }

        AdPolicy adPolicy = DataManager.get(mContext).getAdPolicy(Constant.ADPOLICY_NAME);
        if (adPolicy == null && adConfig != null) {
            adPolicy = adConfig.getAdPolicy();
        } else {
            adConfig.setAdPolicy(adPolicy);
        }
        PolicyManager.get(mContext).setPolicy(adPolicy);

        // 加载应用外广告
        AdPlace adPlace = DataManager.get(mContext).getAdPlace(Constant.ADPLACE_OUTER_NAME);
        if (adPlace == null) {
            adPlace = adConfig.get(Constant.ADPLACE_OUTER_NAME);
        } else {
            adConfig.set(adPlace);
        }
        mOuterPlace = adPlace;

        // 加载贴片广告
        adPlace = DataManager.get(mContext).getAdPlace(Constant.ADPLACE_FILM_NAME);
        if (adPlace == null) {
            adPlace = adConfig.get(Constant.ADPLACE_FILM_NAME);
        } else {
            adConfig.set(adPlace);
        }
        mFilmPlace = adPlace;
    }

    public void startLoop() {
        Intent alarmIntent = new Intent(mContext, IService.class);
        alarmIntent.setAction(Constant.ACTION_ALARM);
        PendingIntent pIntent = PendingIntent.getService(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Service.ALARM_SERVICE);
        long pendingTime = SystemClock.elapsedRealtime() + Constant.ALARM_INTERVAL_TIME;
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME, pendingTime, pIntent);
    }

    public void onFire() {
        fireOuterAd();
    }

    private void fireOuterAd() {
        if (mOuterPlace != null && mAdAggs != null) {
            if (!PolicyManager.get(mContext).shouldShowAdOuter()) {
                return;
            }
            Log.d(Log.TAG, "");
            StatImpl.get().reportAdOuterRequest(mContext);
            mAdAggs.loadMixedAds(mOuterPlace.getName(), new SimpleAdAggsListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    mAdAggs.showMixedAds(pidName, null);
                    StatImpl.get().reportAdOuterShow(mContext);
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    PolicyManager.get(mContext).reportOuterShowing(false);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    Log.d(Log.TAG, "pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    PolicyManager.get(mContext).reportOuterShowing(true);
                }
            });
        }
    }
}
