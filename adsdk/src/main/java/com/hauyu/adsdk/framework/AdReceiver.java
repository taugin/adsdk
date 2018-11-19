package com.hauyu.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.appub.ads.a.FSA;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.AdSwitch;
import com.hauyu.adsdk.config.LtConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.policy.LtPolicy;
import com.hauyu.adsdk.utils.TaskUtils;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AdReceiver {

    private static AdReceiver sAdReceiver;

    private Context mContext;
    private Handler mHandler;

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

    /**
     * 记录应用首次启动时间
     */
    private void reportFirstStartUpTime() {
        if (Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0) <= 0) {
            Utils.putLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, System.currentTimeMillis());
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
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
    }

    private void register() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(getAlarmAction());
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
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
                if (isGtAtExclusive(context)) {
                    if (TaskUtils.hasAppUsagePermission(context)) {
                        AtAdLoader.get(context).onFire();
                    } else {
                        GtAdLoader.get(context).onFire();
                    }
                } else {
                    GtAdLoader.get(context).onFire();
                    if (TaskUtils.hasAppUsagePermission(context)) {
                        AtAdLoader.get(context).onFire();
                    }
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                TaskMonitor.get(context).stopMonitor();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                AtAdLoader.get(context).resumeLoader();
                showLs();
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            homeKeyPressed();
                        }
                    }, 1000);
                }
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
            mContext.startActivity(intent);
            LtPolicy.get(mContext).reportShowing(true);
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
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
}
