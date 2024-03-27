package com.komob.api;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.framework.ActivityMonitor;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.VUIHelper;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Administrator on 2018-10-16.
 */

public class RFileConfig {
    public static String getVersion() {
        return BuildConfig.SDK_VERSION_NAME;
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static void bindLayoutMap(Map<String, Integer> LAYOUT_MAPS) {
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MICRO, R.layout.kom_layout_micro);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_TINY, R.layout.kom_layout_tiny);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LITTLE, R.layout.kom_layout_little);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_SMALL, R.layout.kom_layout_small);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MEDIUM, R.layout.kom_layout_medium);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LARGE, R.layout.kom_layout_large);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_RECT, R.layout.kom_layout_rect);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_WRAP, R.layout.kom_layout_wrap);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_ROUND, R.layout.kom_layout_round);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FULL, R.layout.kom_layout_full);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MIX, R.layout.kom_layout_mix);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FOOT, R.layout.kom_layout_foot);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_HEAD, R.layout.kom_layout_head);
    }

    public static int getLayoutLittle() {
        return R.layout.kom_layout_little;
    }

    public static void bindLayoutId(Params params) {
        if (params == null) {
            return;
        }
        params.setAdTitle(R.id.kom_native_title);
        params.setAdSocial(R.id.kom_native_social);
        params.setAdDetail(R.id.kom_native_detail);
        params.setAdIcon(R.id.kom_native_icon);
        params.setAdAction(R.id.kom_native_action_btn);
        params.setAdCover(R.id.kom_native_image_cover);
        params.setAdChoices(R.id.kom_native_ad_choices_container);
        params.setAdMediaView(R.id.kom_native_media_cover);
    }

    public static int getDefaultIconColor() {
        return R.color.komDefaultIconColor;
    }

    public static int kom_layout_grid = R.layout.kom_layout_grid;
    public static int kom_layout_item = R.layout.kom_layout_item;
    public static int kom_title_view = R.id.kom_title_view;
    public static int kom_spread_grid = R.id.kom_spread_grid;
    public static int kom_group_single = R.id.kom_group_single;
    public static int kom_group_multiple = R.id.kom_group_multiple;
    public static int kom_app_icon_single = R.id.kom_app_icon_single;
    public static int kom_app_name_single = R.id.kom_app_name_single;
    public static int kom_app_detail_single = R.id.kom_app_detail_single;
    public static int kom_action_view_single = R.id.kom_action_view_single;
    public static int kom_app_icon_multiple = R.id.kom_app_icon_multiple;
    public static int kom_app_name_multiple = R.id.kom_app_name_multiple;
    public static int kom_action_view_multiple = R.id.kom_action_view_multiple;
    public static int kom_arrow_back = R.id.kom_arrow_back;


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
            Log.iv(Log.TAG, "error : " + e);
        }
        return activity;
    }

    public static Context createAContext(final Context context) {
        AppContext appContext = new AppContext(context);
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