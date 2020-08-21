package com.earch.sunny.picfg;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.log.Log;

import java.lang.reflect.Field;

public class ActView {

    public static Activity createFakeActivity(final Application application) {
        Activity activity = new Activity() {
            @Override
            public boolean isFinishing() {
                return false;
            }

            @Override
            public void startActivity(Intent intent) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    application.startActivity(intent);
                } catch (Exception | Error e) {
                    Log.e(Log.TAG, "error : " + e);
                }
            }

            @Override
            public Context getApplicationContext() {
                try {
                    return application.getApplicationContext();
                } catch (Exception | Error e) {
                    Log.e(Log.TAG, "error : " + e);
                }
                return super.getApplicationContext();
            }
        };
        try {
            Class ContextWrapperClass = Class.forName("android.content.ContextWrapper");
            Field mBase = ContextWrapperClass.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(activity, application.getBaseContext());

            Class ActivityClass = Class.forName("android.app.Activity");
            Field mApplication = ActivityClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            mApplication.set(activity, application);

            WindowManager wm = (WindowManager) application.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
            Field mWindowManager = ActivityClass.getDeclaredField("mWindowManager");
            mWindowManager.setAccessible(true);
            mWindowManager.set(activity, wm);
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return activity;
    }

    public static Context createWrapperContext(final Context context) {
        return new ContextWrapper(context) {
            @Override
            public void startActivity(Intent intent) {
                addNohistoryForIntentIfNeed(context, intent);
                super.startActivity(intent);
            }

            @Override
            public void startActivity(Intent intent, Bundle options) {
                addNohistoryForIntentIfNeed(context, intent);
                super.startActivity(intent, options);
            }
        };
    }

    private static void addNohistoryForIntentIfNeed(Context context, Intent intent) {
        if (!ActivityMonitor.get(context).appOnTop() && intent != null) {
            ComponentName cmp = intent.getComponent();
            if (cmp != null) {
                String className = cmp.getClassName();
                if (TextUtils.equals(className, "com.google.android.gms.ads.AdActivity")) {
                    Log.v(Log.TAG, "add no history for AdActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                }
            }
        }
    }
}
