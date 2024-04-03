package com.mix.mob;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    private static final Map<String, Integer> LAYOUT_MAPS;

    static {
        LAYOUT_MAPS = new HashMap<String, Integer>();
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MICRO, R.layout.mis_card_micro);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_TINY, R.layout.mis_card_tiny);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LITTLE, R.layout.mis_card_little);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_SMALL, R.layout.mis_card_small);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MEDIUM, R.layout.mis_card_medium);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_LARGE, R.layout.mis_card_large);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_RECT, R.layout.mis_card_rect);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_WRAP, R.layout.mis_card_wrap);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_ROUND, R.layout.mis_card_round);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FULL, R.layout.mis_card_full);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_MIX, R.layout.mis_card_mix);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_FOOT, R.layout.mis_card_foot);
        LAYOUT_MAPS.put(Constant.NATIVE_CARD_HEAD, R.layout.mis_card_head);
    }

    public static String getVersion() {
        return BuildConfig.SDK_VERSION_NAME;
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static int getLayout(String cardStyle) {
        return LAYOUT_MAPS.get(cardStyle);
    }


    public static int getLayoutLittle() {
        return R.layout.mis_card_little;
    }

    public static void bindLayoutId(Params params) {
        if (params == null) {
            return;
        }
        params.setAdTitle(R.id.mis_native_title);
        params.setAdSocial(R.id.mis_native_social);
        params.setAdDetail(R.id.mis_native_detail);
        params.setAdIcon(R.id.mis_native_icon);
        params.setAdAction(R.id.mis_native_action_btn);
        params.setAdCover(R.id.mis_native_image_cover);
        params.setAdChoices(R.id.mis_native_ad_choices_container);
        params.setAdMediaView(R.id.mis_native_media_cover);
    }

    public static int getDefaultIconColor() {
        return R.color.misDefaultIconColor;
    }

    public static int mis_card_grid = R.layout.mis_card_grid;
    public static int mis_card_item = R.layout.mis_card_item;
    public static int mis_title_view = R.id.mis_title_view;
    public static int mis_spread_grid = R.id.mis_spread_grid;
    public static int mis_group_single = R.id.mis_group_single;
    public static int mis_group_multiple = R.id.mis_group_multiple;
    public static int mis_app_icon_single = R.id.mis_app_icon_single;
    public static int mis_app_name_single = R.id.mis_app_name_single;
    public static int mis_app_detail_single = R.id.mis_app_detail_single;
    public static int mis_action_view_single = R.id.mis_action_view_single;
    public static int mis_app_icon_multiple = R.id.mis_app_icon_multiple;
    public static int mis_app_name_multiple = R.id.mis_app_name_multiple;
    public static int mis_action_view_multiple = R.id.mis_action_view_multiple;
    public static int mis_arrow_back = R.id.mis_arrow_back;


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