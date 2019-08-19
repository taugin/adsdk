package com.bac.ioc.gsb.scloader;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.bac.ioc.gsb.scconfig.CtConfig;
import com.bac.ioc.gsb.scpolicy.CtPolicy;
import com.gekes.fvs.tdsvap.GFAPSD;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.common.BaseLoader;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.data.config.AdConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2019/8/18.
 */

public class CtAdLoader extends BaseLoader {

    private static CtAdLoader sCtAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;

    private CtAdLoader(Context context) {
        mContext = context.getApplicationContext();
        AdReceiver.get(context).registerTriggerListener(this);
    }

    public static CtAdLoader get(Context context) {
        if (sCtAdLoader == null) {
            create(context);
        }
        return sCtAdLoader;
    }

    private static void create(Context context) {
        synchronized (CtAdLoader.class) {
            if (sCtAdLoader == null) {
                sCtAdLoader = new CtAdLoader(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        CtPolicy.get(mContext).init();
        updateCtPolicy();
    }

    @Override
    protected Context getContext() {
        return mContext;
    }

    @Override
    public void onBatteryChange(Context context, Intent intent) {
        fillBattery(intent);
    }

    @Override
    public void onPowerConnect(Context context, Intent intent) {
        startCMActivity(context, true);
    }

    @Override
    public void onPowerDisconnect(Context context, Intent intent) {
        startCMActivity(context, false);
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
            intent = new Intent(mContext, GFAPSD.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(~Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(Intent.EXTRA_QUIET_MODE, true);
        BatteryInfo.isCharging = charging;
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
        BatteryInfo.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
        BatteryInfo.scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        BatteryInfo.plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0); // default set as battery
        BatteryInfo.health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN);
        BatteryInfo.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN);
        BatteryInfo.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        BatteryInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        BatteryInfo.present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
        BatteryInfo.technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        BatteryInfo.timestamp = System.currentTimeMillis();
    }

    public static class BatteryInfo {
        public static long timestamp;
        public static int health, level, plugged, scale, status, temperature, voltage;
        public static boolean present;
        public static String technology;
        public static boolean isCharging;

        private static double cachedFactor = 0;

        public static int getPercent() {
            if (scale == 0) {
                return 0;
            }
            return (int) ((float) level / (float) scale * 100);
        }

        public static boolean isCharging() {
            return isCharging;
        }

        public static int estimateRemainChargingTime() {
            int pluggedType = plugged;
            int avgChargingTime = -1;
            if (avgChargingTime == -1) {
                if (pluggedType == BatteryManager.BATTERY_PLUGGED_AC) {
                    avgChargingTime = 90;
                } else {
                    avgChargingTime = 144;
                }
            }
            return avgChargingTime * (100 - getPercent());
        }

        public static int estimateRemainBatteryTime() {
            if (scale == 0) return 0;
            if (cachedFactor == 0) {
                cachedFactor = factor();
            }
            double factor = (cachedFactor * 0.5D + 0.5D);
            return (int) (36.0D * level / scale * 60.0D * factor);
        }

        private static double factor() {
            try {
                Method readProcMethod = Utils.getClassMethod(
                        "android.os.Process", "readProcLines",
                        String.class, String[].class, long[].class);
                String[] requiredFields = new String[]{
                        "MemTotal:", "MemFree:", "Buffers:", "Cached:"
                };
                long[] outSizes = new long[4];
                outSizes[0] = 30;
                outSizes[1] = -30;
                readProcMethod.invoke(null, "/proc/meminfo", requiredFields, outSizes);
                for (int index = 0; index < outSizes.length; ++index) {
                    Log.d(Log.TAG, "outSizes[" + index + "]:" + outSizes[index]);
                }
                return (outSizes[1] + outSizes[2] + outSizes[3]) * 1.0D /
                        outSizes[0];
            } catch (Exception e) {
            }
            return 0;
        }
    }
}
