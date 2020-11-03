package com.bacad.ioc.gsb.base;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.widget.RemoteViews;

import com.earch.sunny.R;
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

    public String getAdMainName() {
        try {
            return ((BPcy) mPolicy).getAdMainName();
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

    private boolean isUseFullIntent() {
        try {
            return ((BPcy) mPolicy).isUseFullIntent();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
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
            ComponentName cmp = new ComponentName(getContext(), Utils.getActivityNameByAction(getContext(), getContext().getPackageName() + ".action.MATCH_DOING"));
            intent.setComponent(cmp);
        }
        intent.putExtra(Intent.ACTION_TIME_TICK, getDelayClose());
        intent.putExtra(Intent.EXTRA_TITLE, pidName);
        intent.putExtra(Intent.EXTRA_TEXT, source);
        intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
        intent.putExtra(Intent.EXTRA_REPLACING, pType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isUseFullIntent()) {
            userFullIntent(getContext(), intent);
        }
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (Exception e1) {
                Log.e(Log.TAG, "error : " + e1);
            }
        }
    }

    private static void startWithAlarm(Context context, Intent intent, int delay) {
        Log.iv(Log.TAG, "delay : " + delay);
        PendingIntent activity = PendingIntent.getActivity(context, 10102, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ((long) delay), activity);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
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

    private static final String NOTIFICATION_CHANNEL = "10010";
    private void createNotificationChannel(NotificationManager notificationManager) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Default Channel", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setLockscreenVisibility(-1);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setShowBadge(false);
                notificationChannel.setSound((Uri) null, (AudioAttributes) null);
                notificationChannel.setBypassDnd(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    protected void userFullIntent(final Context context, Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = new Notification.Builder(context, NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.had_cancel)
                        .setFullScreenIntent(pendingIntent, true)
                        .setCustomHeadsUpContentView(new RemoteViews(context.getPackageName(), R.layout.had_no_layout)).build();
                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                createNotificationChannel(notificationManager);
                notificationManager.cancel(R.drawable.had_cancel);
                notificationManager.notify(R.drawable.had_cancel, notification);
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            notificationManager.cancel(R.drawable.had_cancel);
                        } catch (Exception e) {
                        }
                    }
                }, 500);
                startWithAlarm(context, intent, 200);
            }
        } catch (Exception e) {
        }
    }
}
