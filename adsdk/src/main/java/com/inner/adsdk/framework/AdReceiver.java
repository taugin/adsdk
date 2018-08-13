package com.inner.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.manager.DataManager;

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
            filter.addAction(Constant.ACTION_BASIC_ALARM);
            filter.addAction(Intent.ACTION_SCREEN_ON);
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

                if (isGtTtAll(context)) {
                    GtAdLoader.get(context).onFire();
                    AtAdLoader.get(context).onFire();
                } else if (isGtOnly(context)) {
                    GtAdLoader.get(context).onFire();
                } else if (isTtOnly(context)) {
                    AtAdLoader.get(context).onFire();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                TaskMonitor.get(context).stopMonitor();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                AtAdLoader.get(context).resumeLoader();
            }
        }
    };

    private boolean isGtTtAll(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.getGtTtSwitch() == AdSwitch.GT_TT_SWITCH_ALL;
        }
        return false;
    }

    private boolean isGtOnly(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.getGtTtSwitch() == AdSwitch.GT_TT_SWITCH_GT;
        }
        return false;
    }

    private boolean isTtOnly(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.getGtTtSwitch() == AdSwitch.GT_TT_SWITCH_TT;
        }
        return false;
    }
}
