package com.bacad.ioc.gsb.common;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import com.dock.vost.moon.IAdvance;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.listener.OnTriggerListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.Locale;

/**
 * Created by Administrator on 2018/12/9.
 */

public abstract class Bldr<Policy> implements OnTriggerListener {

    private static final int MSG_START_SCENE = 0x10000;

    private static final int START_SCENE_INTERVAL = 10 * 1000;

    protected Policy mPolicy;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    protected abstract Context getContext();

    protected Bldr(Policy policy) {
        mPolicy = policy;
    }

    /**
     * 记录展示
     */
    public void reportShowing() {
        try {
            ((BPcy) mPolicy).reportImpression(true);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    public String getPlaceNameAdv() {
        try {
            return ((BPcy) mPolicy).getPlaceNameAdv();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    public String getPlaceNameInt() {
        try {
            return ((BPcy) mPolicy).getPlaceNameInt();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    protected String getType() {
        try {
            return ((BPcy) mPolicy).getType();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    protected long getDelayClose() {
        try {
            return ((BPcy) mPolicy).getDelayClose();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return 0;
    }

    /**
     * 调用函数启动场景，避免同时启动多个场景
     */
    public final void startScene(Object... object) {
        synchronized (Bldr.class) {
            if (sHandler != null && !sHandler.hasMessages(MSG_START_SCENE)) {
                sHandler.sendEmptyMessageDelayed(MSG_START_SCENE, getStartInterval());
                onStartScene(object);
            }
        }
    }

    protected int getStartInterval() {
        return START_SCENE_INTERVAL;
    }

    protected void onStartScene(Object... object) {
        throw new AndroidRuntimeException("onStartScene should be override by subclass");
    }

    protected AdParams generateAdParams() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setBannerSize(AdExtra.AD_SDK_COMMON, AdExtra.COMMON_MEDIUM_RECTANGLE);
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_FULL);
        AdParams adParams = builder.build();
        return adParams;
    }

    protected void show(String pidName, String source, String adType, String pType) {
        String action = null;
        if (!TextUtils.isEmpty(pType)) {
            String actType = pType.replace("t", "a");
            action = actType.toUpperCase(Locale.getDefault()) + "VIEW";
        }
        Log.iv(Log.TAG, "filter : " + action);
        Intent intent = null;
        if (!TextUtils.isEmpty(action)) {
            intent = Utils.getIntentByAction(getContext(), getContext().getPackageName() + ".action." + action);
        }
        if (intent == null) {
            intent = new Intent();
            ComponentName cmp = new ComponentName(getContext(), IAdvance.ACT_NAME);
            intent.setComponent(cmp);
        }
        intent.putExtra(Intent.ACTION_TIME_TICK, getDelayClose());
        intent.putExtra(Intent.EXTRA_TITLE, pidName);
        intent.putExtra(Intent.EXTRA_TEXT, source);
        intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
        intent.putExtra(Intent.EXTRA_REPLACING, pType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            try {
                getContext().startActivity(intent);
            } catch (Exception e1) {
                Log.e(Log.TAG, "error : " + e1);
            }
        }
    }

    protected void hide() {
        try {
            Intent intent = new Intent(getContext().getPackageName() + ".action.FA");
            intent.setPackage(getContext().getPackageName());
            getContext().sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onAlarm(Context context) {
    }

    @Override
    public void onHomePressed(Context context) {
    }

    @Override
    public void onScreenOn(Context context) {
    }

    @Override
    public void onScreenOff(Context context) {
    }

    @Override
    public void onUserPresent(Context context) {
    }

    @Override
    public void onPowerConnect(Context context, Intent intent) {
    }

    @Override
    public void onPowerDisconnect(Context context, Intent intent) {
    }

    @Override
    public void onBatteryChange(Context context, Intent intent) {
    }

    @Override
    public void onPackageAdded(Context context, Intent intent) {
    }

    @Override
    public void onPackageReplaced(Context context, Intent intent) {
    }

    @Override
    public void onPackageRemoved(Context context, Intent intent) {
    }

    @Override
    public void onNetworkChange(Context context, Intent intent) {
    }
}
