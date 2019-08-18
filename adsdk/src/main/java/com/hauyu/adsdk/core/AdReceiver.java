package com.hauyu.adsdk.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.hauyu.adsdk.listener.OnTriggerListener;
import com.hauyu.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AdReceiver {
    private static final String PREF_FIRST_STARTUP_TIME = "pref_first_startup_time";

    private static AdReceiver sAdReceiver;

    private Context mContext;
    private List<OnTriggerListener> mTriggerList = new ArrayList<OnTriggerListener>();

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
        reportFirstStartUpTime();
        register();
    }

    public void registerTriggerListener(OnTriggerListener l) {
        try {
            mTriggerList.add(l);
        } catch (Exception e) {
        }
    }

    public void unregisterTriggerListener(OnTriggerListener l) {
        try {
            mTriggerList.remove(l);
        } catch (Exception e) {
        }
    }

    /**
     * 记录应用首次启动时间
     */
    private void reportFirstStartUpTime() {
        if (Utils.getLong(mContext, PREF_FIRST_STARTUP_TIME, 0) <= 0) {
            Utils.putLong(mContext, PREF_FIRST_STARTUP_TIME, System.currentTimeMillis());
        }
    }

    private String getAlarmAction() {
        try {
            return mContext.getPackageName() + ".action.ALARM";
        } catch (Exception e) {
        }
        return Intent.ACTION_SEND + "_ALARM";
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    public long getFirstStartUpTime() {
        return Utils.getLong(mContext, PREF_FIRST_STARTUP_TIME, 0);
    }

    private void register() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(getAlarmAction());
            filter.addAction(getAlarmAction() + "_CONNECT");
            filter.addAction(getAlarmAction() + "_DISCONNECT");
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
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
            if (getAlarmAction().equals(intent.getAction())) {
                triggerAlarm(context);
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                triggerScreenOff(context);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                triggerScreenOn(context);
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                triggerUserPresent(context);
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason) || "recentapps".equals(reason)) {
                    triggerHome(context);
                }
            } else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())
                    || (getAlarmAction() + "_CONNECT").equals(intent.getAction())) {
                triggerPowerConnect(context, intent);
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                    || (getAlarmAction() + "_DISCONNECT").equals(intent.getAction())) {
                triggerPowerDisconnect(context, intent);
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                triggerBatteryChange(context, intent);
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                triggerNetworkChange(context, intent);
            }
        }
    };

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            triggerPackageAdded(context, intent);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            triggerPackageRemoved(context, intent);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            triggerPackageReplaced(context, intent);
        }
    }

    private void triggerAlarm(Context context) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onAlarm(context);
                }
            }
        }
    }

    private void triggerHome(Context context) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onHomePressed(context);
                }
            }
        }
    }

    private void triggerScreenOn(Context context) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onScreenOn(context);
                }
            }
        }
    }

    private void triggerUserPresent(Context context) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onUserPresent(context);
                }
            }
        }
    }

    private void triggerScreenOff(Context context) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onScreenOff(context);
                }
            }
        }
    }

    private void triggerPowerConnect(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onPowerConnect(context, intent);
                }
            }
        }
    }

    private void triggerPowerDisconnect(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onPowerDisconnect(context, intent);
                }
            }
        }
    }

    private void triggerBatteryChange(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onBatteryChange(context, intent);
                }
            }
        }
    }

    private void triggerPackageAdded(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onPackageAdded(context, intent);
                }
            }
        }
    }

    private void triggerPackageRemoved(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onPackageRemoved(context, intent);
                }
            }
        }
    }

    private void triggerPackageReplaced(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onPackageReplaced(context, intent);
                }
            }
        }
    }

    private void triggerNetworkChange(Context context, Intent intent) {
        if (mTriggerList != null && !mTriggerList.isEmpty()) {
            for (OnTriggerListener l : mTriggerList) {
                if (l != null) {
                    l.onNetworkChange(context, intent);
                }
            }
        }
    }
}
