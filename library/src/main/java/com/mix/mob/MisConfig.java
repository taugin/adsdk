package com.mix.mob;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mix.ads.constant.Constant;
import com.mix.ads.core.framework.ActivityMonitor;
import com.mix.ads.core.framework.Params;
import com.mix.ads.log.Log;
import com.mix.ads.utils.VUIHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-10-16.
 */

public class MisConfig {

    public static String getVersion() {
        return BuildConfig.SDK_VERSION_NAME;
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    static class F0O0O0O extends Activity {
        private Application application;

        public F0O0O0O(Application application) {
            this.application = application;
        }

        @Override
        public boolean isFinishing() {
            return false;
        }

        @Override
        public void startActivity(Intent intent) {
            try {
                configIntent(application, intent);
                application.startActivity(intent);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }

        @Override
        public Context getApplicationContext() {
            try {
                return application.getApplicationContext();
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return super.getApplicationContext();
        }

        @Override
        public String getLocalClassName() {
            try {
                return super.getLocalClassName();
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return VUIHelper.class.getName();
        }

        @Override
        public Object getSystemService(String name) {
            try {
                return application.getSystemService(name);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            return super.getSystemService(name);
        }

        @Override
        public <T extends View> T findViewById(int id) {
            try {
                return super.findViewById(id);
            } catch (Exception | Error e) {
                Log.iv(Log.TAG, "error : " + e);
                try {
                    Activity topActivity = ActivityMonitor.get(this).getTopActivity();
                    if (topActivity != null) {
                        Window window = topActivity.getWindow();
                        if (window != null) {
                            return window.findViewById(id);
                        }
                    }
                } catch (Exception | Error error) {
                    Log.iv(Log.TAG, "error : " + error);
                }
            }
            return null;
        }

        @Override
        public Window getWindow() {
            try {
                Activity topActivity = ActivityMonitor.get(this).getTopActivity();
                if (topActivity != null) {
                    Window window = topActivity.getWindow();
                    return window;
                }
            } catch (Exception e) {
            }
            return super.getWindow();
        }
    }

    public static Activity getFA(final Application application) {
        Activity activity = new F0O0O0O(application);
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
            Log.iv(Log.TAG, "error : " + e);
        }
        return activity;
    }

    private static void configIntent(Context context, Intent intent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}