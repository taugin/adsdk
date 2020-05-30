package com.earch.sunny.picfg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

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
                    application.startActivity(intent);
                } catch (Exception | Error e) {
                }
            }

            @Override
            public Context getApplicationContext() {
                try {
                    return application.getApplicationContext();
                } catch (Exception | Error e) {
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
}
