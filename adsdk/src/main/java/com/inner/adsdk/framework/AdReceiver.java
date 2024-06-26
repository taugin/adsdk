package com.inner.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.text.TextUtils;

import com.appub.ads.a.FSA;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.CtConfig;
import com.inner.adsdk.config.LtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.OnTriggerListener;
import com.inner.adsdk.loader.AtAdLoader;
import com.inner.adsdk.loader.GtAdLoader;
import com.inner.adsdk.loader.HtAdLoader;
import com.inner.adsdk.loader.TaskMonitor;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.BsPolicy;
import com.inner.adsdk.policy.CtPolicy;
import com.inner.adsdk.policy.LtPolicy;
import com.inner.adsdk.utils.TaskUtils;
import com.inner.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AdReceiver {

    private static AdReceiver sAdReceiver;
    private static final int MSG_SHOW_LOCKSCREEN = 123456789;
    private static final int DELAY = 5000;

    private Context mContext;
    private Handler mHandler;
    private List<OnTriggerListener> mTriggerList = new ArrayList<OnTriggerListener>();

    private AdReceiver(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler();
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
        if (Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0) <= 0) {
            Utils.putLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, System.currentTimeMillis());
        }
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    public long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
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

    private String getAlarmAction() {
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
                triggerAlarm(context);
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                TaskMonitor.get(context).stopMonitor();
                triggerScreenOff(context);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                AtAdLoader.get(context).resumeLoader();
                showLockScreen();
                triggerScreenOn(context);
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                showLockScreen();
                triggerUserPresent(context);
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason) || "recentapps".equals(reason)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            homeKeyPressed();
                        }
                    }, 500);
                    triggerHome(context);
                }
            } else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())
                    || (getAlarmAction() + "_CONNECT").equals(intent.getAction())) {
                startCMActivity(context, true);
                triggerPowerConnect(context, intent);
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                    || (getAlarmAction() + "_DISCONNECT").equals(intent.getAction())) {
                startCMActivity(context, false);
                triggerPowerDisconnect(context, intent);
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                fillBattery(intent);
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
        }
    }

    private boolean isGtAtExclusive(Context context) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isGtAtExclusive();
        }
        return false;
    }

    private void showLockScreen() {
        if (mHandler != null) {
            if (!mHandler.hasMessages(MSG_SHOW_LOCKSCREEN)) {
                showLs();
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOCKSCREEN, DELAY);
            } else {
                mHandler.removeMessages(MSG_SHOW_LOCKSCREEN);
            }
        }
    }

    private void showLs() {
        updateLtPolicy();
        if (!LtPolicy.get(mContext).isLtAllowed()) {
            return;
        }
        try {
            Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.LSPICKER");
            if (intent == null) {
                intent = new Intent(mContext, FSA.class);
            }
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
            LtPolicy.get(mContext).reportShowing(true);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private void updateLtPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        LtConfig ltConfig = DataManager.get(mContext).getRemoteLtPolicy();
        if (ltConfig == null && adConfig != null) {
            ltConfig = adConfig.getLtConfig();
        }
        LtPolicy.get(mContext).setPolicy(ltConfig);
    }

    private void homeKeyPressed() {
        HtAdLoader.get(mContext).fireHome();
    }

    private void updateCtPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        CtConfig ctConfig = DataManager.get(mContext).getRemoteCtPolicy();
        if (ctConfig == null && adConfig != null) {
            ctConfig = adConfig.getCtConfig();
        }
        CtPolicy.get(mContext).setPolicy(ctConfig);
    }

    private void startCMActivity(Context context, boolean charging) {
        updateCtPolicy();
        if (!CtPolicy.get(mContext).isCtAllowed()) {
            return;
        }
        Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.CMPICKER");
        if (intent == null) {
            intent = new Intent(mContext, FSA.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(~Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(Intent.EXTRA_QUIET_MODE, true);
        BsPolicy.get().isCharging = charging;
        try {
            context.startActivity(intent);
            CtPolicy.get(mContext).reportShowing(true);
        } catch (Exception e) {
        }
    }

    private void fillBattery(Intent intent) {
        if (intent == null) {
            return;
        }
        BsPolicy.get().level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
        BsPolicy.get().scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        BsPolicy.get().plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0); // default set as battery
        BsPolicy.get().health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN);
        BsPolicy.get().status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN);
        BsPolicy.get().temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        BsPolicy.get().voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        BsPolicy.get().present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
        BsPolicy.get().technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        BsPolicy.get().timestamp = System.currentTimeMillis();
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
