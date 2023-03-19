package com.android.support;

/**
 * Created by Administrator on 2019-12-20.
 */

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.rabbit.adsdk.core.framework.ActivityMonitor;
import com.rabbit.adsdk.log.Log;

import java.lang.reflect.Field;

/**
 * 监听Banner或native是否可见的类
 */
public class MaskView extends View {

    private boolean mViewDetached = false;
    private boolean mViewVisible = true;

    public MaskView(Context context) {
        super(context);
    }

    public boolean isVisible() {
        return !mViewDetached && mViewVisible;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mViewVisible = visibility == View.VISIBLE;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewDetached = true;
    }

    static class FActivity extends Activity {
        private Application application;

        public FActivity(Application application) {
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

        @Override
        public String getLocalClassName() {
            try {
                return super.getLocalClassName();
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e);
            }
            return MaskView.class.getName();
        }

        @Override
        public Object getSystemService(@NonNull String name) {
            try {
                return application.getSystemService(name);
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e);
            }
            return super.getSystemService(name);
        }

        @Override
        public <T extends View> T findViewById(int id) {
            try {
                return super.findViewById(id);
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e);
                try {
                    Activity topActivity = ActivityMonitor.get(this).getTopActivity();
                    if (topActivity != null) {
                        Window window = topActivity.getWindow();
                        if (window != null) {
                            return window.findViewById(id);
                        }
                    }
                } catch (Exception | Error error) {
                    Log.e(Log.TAG, "error : " + error);
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

    public static Activity createFA(final Application application) {
        Activity activity = new FActivity(application);
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

    public static Context createAContext(final Context context) {
        MaskView.AppContext appContext = new MaskView.AppContext(context);
        return appContext;
    }

    private static class AppContext extends Application {

        public AppContext(Context base) {
            super();
            attachBaseContext(base);
        }

        @Override
        public void startActivity(Intent intent) {
            configIntent(getBaseContext(), intent);
            super.startActivity(intent);
        }

        @Override
        public void startActivity(Intent intent, Bundle options) {
            configIntent(getBaseContext(), intent);
            super.startActivity(intent, options);
        }
    }

    private static void configIntent(Context context, Intent intent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}