package com.bac.ioc.gsb.scloader;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.gekes.fvs.tdsvap.GFAPSD;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.common.BaseLoader;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.data.DataManager;
import com.bac.ioc.gsb.scconfig.CtConfig;
import com.bac.ioc.gsb.scpolicy.BsPolicy;
import com.bac.ioc.gsb.scpolicy.CtPolicy;
import com.hauyu.adsdk.utils.Utils;

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
}
