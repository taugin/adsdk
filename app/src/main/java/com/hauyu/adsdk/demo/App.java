package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.multidex.MultiDex;

import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.Utils;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * Created by Administrator on 2018/3/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BlockCanary.install(this, new BlockCanaryContext() {
        }).start();
        ChangeLanguage.init(this);
        Va.setNetworkProxy();
        initUmeng();
        initTalkingData();
        AdSdk.get(this).init();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    private void initUmeng() {
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "5f44faa1f9d1496ef418b17c", "umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    private void initTalkingData() {
        String appId = "72EC6DEE7A914070B029C48AAAA7CAD9";
        String channel = getChannel(this);
        TCAgent.init(this, appId, channel);
        TCAgent.setReportUncaughtExceptions(true);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                TCAgent.onPageStart(activity, activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                TCAgent.onPageEnd(activity, activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private static String getChannel(Context context) {
        String channel = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            channel = locale.getCountry().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
            channel = Utils.getMetaData(context, "UMENG_CHANNEL");
        }
        return channel;
    }

    public static final List<String> sSystemApps = Arrays.asList("com.android.systemui", "com.google.android.gms", "com.google.android.gsf.login", "com.google.android.packageinstaller", "android", "com.google.android.gsf", "com.android.settings");
    public static final List<String> sKeywords = Arrays.asList("package", "input", "system", "clock", "launcher", "provider", "time");

    public static List<PackageInfo> getRunningPackageList(Context context) {
        List<PackageInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> allApps = packageManager.queryIntentActivities(intent, 0);
        String processName = "";
        String packageName = "";
        for (ResolveInfo ri : allApps) {
            if (ri != null) {
                ActivityInfo activityInfo = ri.activityInfo;
                if (activityInfo != null) {
                    processName = activityInfo.processName;
                    packageName = activityInfo.packageName;
                }
            }
            if (excludePackageInfo(context, packageName)) {
                continue;
            }
            try {
                boolean isBackgroundAlive = false;
                PackageInfo packageInfo = packageManager.getPackageInfo(processName, 0);
                ApplicationInfo applicationInfo2 = packageInfo.applicationInfo;
                boolean z4 = (applicationInfo2.flags & 1) > 0;
                boolean z5 = (applicationInfo2.flags & 2097152) > 0;
                boolean z6 = (applicationInfo2.flags & 8) > 0;
                isBackgroundAlive = !z4 && !z5 && !z6;
                if (isBackgroundAlive) {
                    list.add(packageInfo);
                }
            } catch (Exception e) {
            }
        }
        return list;
    }

    private static boolean excludePackageInfo(Context context, String packageName) {
        if (TextUtils.equals(context.getPackageName(), packageName)) {
            return true;
        }
        if (sSystemApps.contains(packageName)) {
            return true;
        }
        for (String keyword : sKeywords) {
            if (packageName != null && packageName.contains(keyword)) {
                return true;
            }
        }
        if (TextUtils.equals(Build.BRAND.toLowerCase(Locale.ROOT), "xiaomi")
                && packageName != null && (packageName.startsWith("com.xiaomi") || packageName.startsWith("com.mi") || packageName.startsWith("com.miui"))) {
            return true;
        }
        return false;
    }
}
