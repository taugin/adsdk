package com.bacad.ioc.gsb.scloader;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.Bldr;
import com.bacad.ioc.gsb.common.CSvr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.scpolicy.CvPcy;
import com.dock.vost.moon.IAdvance;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by Administrator on 2019/8/18.
 */

public class CvAdl extends Bldr {

    private static CvAdl sCvAdl;

    private Context mContext;
    private AdSdk mAdSdk;

    private CvAdl(Context context) {
        super(CvPcy.get(context));
        mContext = context.getApplicationContext();
        CSvr.get(context).registerTriggerListener(this);
    }

    public static CvAdl get(Context context) {
        if (sCvAdl == null) {
            create(context);
        }
        return sCvAdl;
    }

    private static void create(Context context) {
        synchronized (CvAdl.class) {
            if (sCvAdl == null) {
                sCvAdl = new CvAdl(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        CvPcy.get(mContext).init();
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
        CvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteCtPolicy());
    }

    private void startCMActivity(Context context, boolean charging) {
        updateCtPolicy();
        if (!CvPcy.get(mContext).isCtAllowed()) {
            return;
        }
        String placeName = getPlaceNameAdv();
        if (TextUtils.isEmpty(placeName)) {
            Log.iv(Log.TAG, getType() + " not found place name");
            return;
        }
        String pType = CvPcy.get(mContext).getType();
        String action = null;
        if (!TextUtils.isEmpty(pType)) {
            String actType = pType.replace("t", "a");
            action = actType.toUpperCase(Locale.getDefault()) + "VIEW";
        }
        Log.iv(Log.TAG, "filter : " + action);
        Intent intent = null;
        if (!TextUtils.isEmpty(action)) {
            intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action." + action);
        }
        if (intent == null) {
            intent = new Intent();
            ComponentName cmp = new ComponentName(mContext, IAdvance.ACT_NAME);
            intent.setComponent(cmp);
        }
        intent.putExtra(Intent.EXTRA_TITLE, placeName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(~Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(Intent.EXTRA_QUIET_MODE, true);
        intent.putExtra(Intent.EXTRA_REPLACING, pType);
        BatteryInfo.isCharging = charging;
        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (Exception e) {
            try {
                context.startActivity(intent);
                CvPcy.get(mContext).reportImpression(true);
            } catch (Exception e1) {
            }
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
