package com.inner.adsdk.compand;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.GtAdLoader;

/**
 * Created by Administrator on 2018/3/19.
 */

public class IService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (Constant.ACTION_ALARM.equals(intent.getAction())) {
                GtAdLoader.get(this).onFire();
                GtAdLoader.get(this).startLoop();
            }
        }
        return START_REDELIVER_INTENT;
    }
}
