package com.hauyu.adsdk.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class TaskUtils {

    public static boolean hasAppUsagePermission(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                long ts = System.currentTimeMillis();
                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext().getSystemService(Activity.USAGE_STATS_SERVICE);
                List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
                if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * 查询顶层Activity
     *
     * @param context
     * @return
     */
    public static ComponentName queryTopActivity(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return queryTopActivityUnderLollipop(context);
        }
        return queryTopActivityGreatLollipop(context);
    }

    /**
     * @param context
     * @return
     */
    private static ComponentName queryTopActivityUnderLollipop(Context context) {
        ComponentName cmp = null;
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                String pkgname = appTasks.get(0).topActivity.getPackageName();
                String className = appTasks.get(0).topActivity.getClassName();
                cmp = new ComponentName(pkgname, className);
            }
        } catch (Exception e) {
        } catch (Error e) {
        }
        return cmp;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ComponentName queryTopActivityGreatLollipop(Context context) {
        ComponentName cmp = null;
        try {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            String pkgname = "";
            String className = null;
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    pkgname = event.getPackageName();
                    className = event.getClassName();
                }
            }
            if (!TextUtils.isEmpty(pkgname) && !TextUtils.isEmpty(className)) {
                cmp = new ComponentName(pkgname, className);
            }
        } catch (Exception e) {
        } catch (Error e) {
        }
        return cmp;
    }
}
