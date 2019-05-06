package com.hauyu.adsdk.listener;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2019-5-6.
 */

public interface OnTriggerListener {
    void onAlarm(Context context);

    void onHomePressed(Context context);

    void onScreenOn(Context context);

    void onScreenOff(Context context);

    void onUserPresent(Context context);

    void onPowerConnect(Context context, Intent intent);

    void onPowerDisconnect(Context context, Intent intent);

    void onBatteryChange(Context context, Intent intent);

    void onPackageAdded(Context context, Intent intent);

    void onPackageReplaced(Context context, Intent intent);

    void onPackageRemoved(Context context, Intent intent);

    void onNetworkChange(Context context, Intent intent);
}
