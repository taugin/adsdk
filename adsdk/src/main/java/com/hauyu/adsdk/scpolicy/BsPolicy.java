package com.hauyu.adsdk.scpolicy;


import android.os.BatteryManager;

import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.lang.reflect.Method;

public class BsPolicy {
    public long timestamp;
    public int health, level, plugged, scale, status, temperature, voltage;
    public boolean present;
    public String technology;
    public boolean isCharging;

    private double cachedFactor = 0;

    private static BsPolicy sBsPolicy;

    public static BsPolicy get() {
        synchronized (BsPolicy.class) {
            if (sBsPolicy == null) {
                createInstance();
            }
        }
        return sBsPolicy;
    }

    private static void createInstance() {
        synchronized (BsPolicy.class) {
            if (sBsPolicy == null) {
                sBsPolicy = new BsPolicy();
            }
        }
    }

    private BsPolicy() {
    }

    public int getPercent() {
        if (scale == 0) {
            return 0;
        }
        return (int) ((float) level / (float) scale * 100);
    }

    public boolean isCharging() {
        return isCharging;
    }

    public int estimateRemainChargingTime() {
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

    public int estimateRemainBatteryTime() {
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

    @Override
    public String toString() {
        return "BsPolicy{" +
                "timestamp=" + timestamp +
                ", health=" + health +
                ", level=" + level +
                ", plugged=" + plugged +
                ", scale=" + scale +
                ", status=" + status +
                ", temperature=" + temperature +
                ", voltage=" + voltage +
                ", present=" + present +
                ", technology='" + technology + '\'' +
                ", isCharging=" + isCharging +
                ", cachedFactor=" + cachedFactor +
                '}';
    }
}
