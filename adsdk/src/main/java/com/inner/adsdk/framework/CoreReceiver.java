package com.inner.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.inner.adsdk.constant.Constant;

/**
 * Created by Administrator on 2018-8-10.
 */

public class CoreReceiver {

    private static CoreReceiver sCoreReceiver;

    private Context mContext;

    private CoreReceiver(Context context) {
        mContext = context.getApplicationContext();
    }

    public static CoreReceiver get(Context context) {
        if (sCoreReceiver == null) {
            create(context);
        }
        return sCoreReceiver;
    }

    private static void create(Context context) {
        synchronized (CoreReceiver.class) {
            if (sCoreReceiver == null) {
                sCoreReceiver = new CoreReceiver(context);
            }
        }
    }

    public void init() {
        register();
    }

    private void register() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.ACTION_BASIC_ALARM);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (Constant.ACTION_BASIC_ALARM.equals(intent.getAction())) {
                GtAdLoader.get(context).onFire();
                FtTtAdLoader.get(context).onFire();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                TaskMonitor.get(context).stopMonitor();
            }
        }
    };
}
