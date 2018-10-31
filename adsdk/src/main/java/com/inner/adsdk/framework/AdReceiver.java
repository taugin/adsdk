package com.inner.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.inner.adsdk.BuildConfig;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.utils.TaskUtils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AdReceiver {

    private static AdReceiver sAdReceiver;

    private Context mContext;

    private AdReceiver(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AdReceiver get(Context context) {
        if (sAdReceiver == null) {
            create(context);
        }
        return sAdReceiver;
    }

    private static void create(Context context) {
        synchronized (AdReceiver.class) {
            if (sAdReceiver == null) {
                sAdReceiver = new AdReceiver(context);
            }
        }
    }

    public void init() {
        register();
    }

    private void register() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(getAlarmAction());
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
        }
    }

    private String getAlarmAction() {
        if (BuildConfig.VERSION_NAME.startsWith("1.1.")) {
            return Constant.ACTION_BASIC_ALARM;
        }
        try {
            return mContext.getPackageName() + ".action.ALARM";
        } catch (Exception e) {
        }
        return Intent.ACTION_SEND + "_ALARM";
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (TextUtils.equals(getAlarmAction(), intent.getAction())) {
                if (isGtAtExclusive(context)) {
                    if (TaskUtils.hasAppUsagePermission(context)) {
                        AtAdLoader.get(context).onFire();
                    } else {
                        GtAdLoader.get(context).onFire();
                    }
                } else {
                    GtAdLoader.get(context).onFire();
                    AtAdLoader.get(context).onFire();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                TaskMonitor.get(context).stopMonitor();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                AtAdLoader.get(context).resumeLoader();
            }
        }
    };

    private boolean isGtAtExclusive(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isGtAtExclusive();
        }
        return false;
    }
}
