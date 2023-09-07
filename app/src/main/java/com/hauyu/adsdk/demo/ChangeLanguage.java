package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p>语言切换辅助类，使用方法：</p>
 * <p>1. 调用ChangeLanguage.init(context, StartupActivity.class);</p>
 * <p>2. 在Application、Activity、Service重写attachBaseContext方法，根据切换的语言生成新的上下文，如下：</p>
 * <pre class="prettyprint">
 * protected void attachBaseContext(Context newBase) {
 *     super.attachBaseContext(ChangeLanguage.createConfigurationContext(newBase));
 * }
 * </pre>
 * <p>3. 调用ChangeLanguage.getCurrentLanguage(context);获取当前显示语言</p>
 * <p>4. 调用ChangeLanguage.showLanguageDialog(activity);展示语言列表，选择要切换的语言</p>
 */
public class ChangeLanguage {
    private static final String SP_LANGUAGE_SWITCH_NAME = "sp_change_language";
    private static final String PREF_LANGUAGE_SWITCH_LANGUAGE = "pref_language_switch_language";
    private static final String PREF_LANGUAGE_SWITCH_COUNTRY = "pref_language_switch_country";
    private static final String TAG = "ChangeLanguage";

    public static class LocaleInfo {
        /**
         * 表示国际化语言，当locale为null时，表示跟随系统语言
         */
        private Locale locale;
        /**
         * 显示语言的区域
         */
        private String display;
        /**
         * display的中文翻译版本
         */
        private String display2;
        /**
         * Follow System的翻译语言
         */
        private String followSystem;

        public LocaleInfo(Locale locale, String display) {
            this(locale, display, "", null);
        }

        public LocaleInfo(Locale locale, String display, String display2, String followSystem) {
            this.locale = locale;
            this.display = display;
            this.display2 = display2;
            this.followSystem = followSystem;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getDisplay2() {
            return display2;
        }

        public void setDisplay2(String display2) {
            this.display2 = display2;
        }

        public String getFollowSystem() {
            return followSystem;
        }

        public void setFollowSystem(String followSystem) {
            this.followSystem = followSystem;
        }
    }

    private static final List<LocaleInfo> sLocaleList;
    private static final List<Activity> sActivityList = new ArrayList<Activity>();
    private static Class<?> sMainClass;
    private static List<LocaleInfo> sUserLocaleList;
    private static WeakReference<Activity> sTopActivity;

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, Class<?> mainClass) {
        sMainClass = mainClass;
        registerActivityLifeCycleCallback(context);
    }

    private static void registerActivityLifeCycleCallback(Context context) {
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                sActivityList.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                sTopActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                sTopActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                sActivityList.remove(activity);
            }
        });
    }

    static {
        sLocaleList = new ArrayList<>();
        sLocaleList.add(0, new LocaleInfo(null, "Follow System"));
        sLocaleList.add(new LocaleInfo(Locale.ENGLISH, "English", "英语", "Follow System"));
        sLocaleList.add(new LocaleInfo(new Locale("ar"), "العربية", "阿拉伯语", "اتبع النظام"));
        sLocaleList.add(new LocaleInfo(new Locale("bn", "IN"), "বাংলা", "孟加拉语", "نظام المتابعة"));
        sLocaleList.add(new LocaleInfo(Locale.GERMANY, "Deutsch", "德语", "System folgen"));
        sLocaleList.add(new LocaleInfo(new Locale("es"), "Español", "西班牙语", "Seguir sistema"));
        sLocaleList.add(new LocaleInfo(Locale.FRENCH, "Français", "法语", "Suivre le système"));
        sLocaleList.add(new LocaleInfo(new Locale("hi"), "हिंदी", "印地语", "सिस्टम का पालन करें"));
        sLocaleList.add(new LocaleInfo(Locale.ITALY, "Italiano", "意大利语", "Segui il sistema"));
        sLocaleList.add(new LocaleInfo(new Locale("ja"), "日本語", "日语", "フォローシステム"));
        sLocaleList.add(new LocaleInfo(Locale.KOREA, "한국어", "韩语", "팔로우 시스템"));
        sLocaleList.add(new LocaleInfo(new Locale("in", "ID"), "Indonesia", "印尼语", "Ikuti sistem"));
        sLocaleList.add(new LocaleInfo(new Locale("ms"), "Melayu", "马来语", "Ikut sistem"));
        sLocaleList.add(new LocaleInfo(new Locale("nl"), "Nederlands", "荷兰语", "Volg systeem"));
        sLocaleList.add(new LocaleInfo(new Locale("pt"), "Português", "葡萄牙语", "Siga o sistema"));
        sLocaleList.add(new LocaleInfo(new Locale("ru"), "Русский", "俄语", "Следуйте системе"));
        sLocaleList.add(new LocaleInfo(new Locale("sv"), "Svenska", "瑞典语", "Följ systemet"));
        sLocaleList.add(new LocaleInfo(new Locale("th"), "ภาษาไทย", "泰语", "ติดตามระบบ"));
        sLocaleList.add(new LocaleInfo(new Locale("tr"), "Türkçe", "土耳其语", "takip sistemi"));
        sLocaleList.add(new LocaleInfo(new Locale("uk"), "Український", "乌克兰语", "Слідкуйте за системою"));
        sLocaleList.add(new LocaleInfo(new Locale("vi"), "Việt", "越南语", "Theo hệ thống"));
        sLocaleList.add(new LocaleInfo(Locale.SIMPLIFIED_CHINESE, "简体中文", "", "跟随系统"));
        sLocaleList.add(new LocaleInfo(Locale.TAIWAN, "繁體中文", "", "跟隨系統"));
        sLocaleList.add(new LocaleInfo(new Locale("fa"), "فارسی", "波斯语", "سیستم را دنبال کنید"));
        sLocaleList.add(new LocaleInfo(new Locale("pl"), "Polski", "波兰语", "Śledź system"));
        sLocaleList.add(new LocaleInfo(new Locale("fil"), "Pilipinas", "菲律宾语", "Sundin ang sistema"));
        sLocaleList.add(new LocaleInfo(Locale.SIMPLIFIED_CHINESE, "简体中文", "简体中文", "跟随系统"));
        sUserLocaleList = sLocaleList;
    }

    public static List<LocaleInfo> getLocaleList() {
        return sUserLocaleList;
    }

    /**
     * Android N以下的主题
     */
    public static final class CustomContextThemeWrapper extends ContextThemeWrapper {
        Configuration mConfiguration;

        CustomContextThemeWrapper(Configuration configuration, Context context, int index) {
            super(context, index);
            mConfiguration = configuration;
            applyOverrideConfiguration(configuration);
        }

        public void applyOverrideConfiguration(Configuration configuration) {
            if (configuration != null) {
                configuration.setTo(this.mConfiguration);
            }
            try {
                super.applyOverrideConfiguration(configuration);
            } catch (Exception e) {
                Log.v(TAG, "e : " + e);
            }
        }
    }

    /**
     * 获取用户在应用内设置的语言
     *
     * @param context
     * @return
     */
    private static Locale getSelectLocale(Context context) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SP_LANGUAGE_SWITCH_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                String language = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_LANGUAGE, null);
                String country = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_COUNTRY, "");
                if (!TextUtils.isEmpty(language)) {
                    return new Locale(language, country);
                }
            }
        }
        return null;
    }

    /**
     * 持久化设置的语言，如果设置的语言为空，则清除设置, 清除设置后会使用系统默认设置
     *
     * @param context
     * @param userSetLocale
     */
    public static void setSelectLocale(Context context, Locale userSetLocale) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SP_LANGUAGE_SWITCH_NAME, Context.MODE_PRIVATE);
            if (userSetLocale != null) {
                String language = userSetLocale.getLanguage();
                String country = userSetLocale.getCountry();
                if (sharedPreferences != null) {
                    sharedPreferences.edit().putString(PREF_LANGUAGE_SWITCH_LANGUAGE, language).commit();
                    sharedPreferences.edit().putString(PREF_LANGUAGE_SWITCH_COUNTRY, country).commit();
                }
            } else {
                if (sharedPreferences != null) {
                    sharedPreferences.edit().putString(PREF_LANGUAGE_SWITCH_LANGUAGE, null).commit();
                    sharedPreferences.edit().putString(PREF_LANGUAGE_SWITCH_COUNTRY, null).commit();
                }
            }
        }
    }

    /**
     * 获取当前系统默认的语言
     *
     * @param context
     * @return
     */
    private static Locale getCurrentLocale(Context context) {
        Locale locale;
        try {
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            if (Build.VERSION.SDK_INT >= 24) {
                locale = configuration.getLocales().get(0);
            } else {
                locale = configuration.locale;
            }
        } catch (Exception e) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * 创建更改过语言设置的上线文Context
     *
     * @param context
     * @return
     */
    public static Context createConfigurationContext(Context context) {
        Context configContext = context;
        try {
            Locale locale = getSelectLocale(context);
            if (locale != null) {
                Resources resources = context.getResources();
                Configuration configuration = resources.getConfiguration();
                configuration.setLocale(locale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    configContext = context.createConfigurationContext(configuration);
                } else {
                    configContext = new CustomContextThemeWrapper(configuration, context, android.R.style.Theme_DeviceDefault);
                }
            }
        } catch (Exception e) {
        }
        if (configContext == null) {
            configContext = context;
        }
        return configContext;
    }

    public static String getCurrentLanguage(Context context) {
        return getCurrentLanguage(context, false);
    }

    /**
     * 获取显示语言
     *
     * @param context
     * @return
     */
    public static String getCurrentLanguage(Context context, boolean showChinese) {
        String currentLanguageDisplay = null;
        int index = findLocaleIndex(context);
        String followSystemString = getFollowSystemTranslation(context, index);
        LocaleInfo localeInfo = sUserLocaleList.get(index);
        if (localeInfo != null && !TextUtils.isEmpty(localeInfo.display)) {
            if (localeInfo.locale == null) {
                if (!TextUtils.isEmpty(followSystemString)) {
                    currentLanguageDisplay = followSystemString;
                } else {
                    currentLanguageDisplay = localeInfo.display;
                }
            } else {
                currentLanguageDisplay = localeInfo.display;
            }
            if (showChinese) {
                if (!TextUtils.isEmpty(localeInfo.display2)) {
                    currentLanguageDisplay = currentLanguageDisplay + " (" + localeInfo.display2 + ")";
                } else if (localeInfo.locale == null) {
                    try {
                        currentLanguageDisplay = currentLanguageDisplay + " (跟随系统)";
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            try {
                currentLanguageDisplay = Locale.getDefault().getDisplayLanguage();
            } catch (Exception e) {
            }
        }
        return currentLanguageDisplay;
    }

    /**
     * 语言设置成功后，重启应用
     *
     * @param activity
     * @param clazz
     */
    public static void restartApp(Activity activity, final Class<?> clazz) {
        try {
            for (Activity act : sActivityList) {
                act.finish();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (activity != null) {
                        if (clazz != null) {
                            try {
                                activity.startActivity(new Intent(activity, clazz));
                            } catch (Exception e) {
                                Log.e(TAG, "error : " + e);
                            }
                        } else {
                            try {
                                Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
                                activity.startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "error : " + e);
                            }
                        }
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 500);
        } catch (Exception e2) {
        }
    }

    /**
     * 更新语言区域
     *
     * @param context
     * @param locale
     */
    public static void changeLocale(Context context, Locale locale) {
        try {
            if (locale == null) {
                locale = getCurrentLocale(context);
            }
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = locale;
            context.getResources().updateConfiguration(configuration, null);
        } catch (Exception e2) {
            Log.e(TAG, "error : " + e2);
        }
    }

    private static int findLocaleIndexByLanguageAndCountry(String language, String country) {
        int selectIndex = -1;
        for (int index = 0; index < sUserLocaleList.size(); index++) {
            LocaleInfo info = sUserLocaleList.get(index);
            if (info != null && info.locale != null) {
                if (selectIndex == -1 && TextUtils.equals(info.locale.getLanguage(), language)) {
                    selectIndex = index;
                }
                if (TextUtils.equals(info.locale.getLanguage(), language) && TextUtils.equals(info.locale.getCountry(), country)) {
                    selectIndex = index;
                    break;
                }
            }
        }
        return selectIndex;
    }

    /**
     * 查找当前设置的语言在列表中的位置
     *
     * @param context
     * @return
     */
    public static int findLocaleIndex(Context context) {
        int selectIndex = -1;
        if (context != null) {
            String language = null;
            String country = "";
            SharedPreferences sharedPreferences = context.getSharedPreferences(SP_LANGUAGE_SWITCH_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                language = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_LANGUAGE, null);
                country = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_COUNTRY, "");
            }
            if (TextUtils.isEmpty(language)) {
                selectIndex = 0;
            } else {
                selectIndex = findLocaleIndexByLanguageAndCountry(language, country);
            }
        }
        if (selectIndex < 0) {
            selectIndex = 0;
        }
        return selectIndex;
    }

    public static void showLanguageDialogForTestMode(View view) {
        final long[] mClicks = new long[10];
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //每次点击时，数组向前移动一位
                System.arraycopy(mClicks, 1, mClicks, 0, mClicks.length - 1);
                //为数组最后一位赋值
                mClicks[mClicks.length - 1] = SystemClock.uptimeMillis();
                //当点击到底10次的时候，拿到点击第一次的时间，获取点击到底10次的时间，看两者之间的差值是否在5s之内，如果是连续点击成功，反之失败。
                if (mClicks[0] >= (SystemClock.uptimeMillis() - 5000)) {
                    showLanguageDialog(true);
                }
            }
        });
    }

    public static void showLanguageDialog() {
        showLanguageDialog(false);
    }

    public static void showLanguageDialog(boolean showChinese) {
        if (sTopActivity != null && sTopActivity.get() != null && !sTopActivity.get().isFinishing()) {
            showLanguageDialog(sTopActivity.get(), showChinese);
        }
    }

    /**
     * 获取当前系统语言的在UserLocaleList里面的索引
     *
     * @return
     */
    public static String getFollowSystemTranslation(Context context, int selectIndex) {
        try {
            LocaleInfo localeInfoForFollowSystem = sUserLocaleList.get(selectIndex);
            Locale locale = getCurrentLocale(context);
            String currentLanguage = null;
            String currentLanguageDisplay = null;
            if (locale != null) {
                currentLanguage = locale.getLanguage();
            }
            for (LocaleInfo lInfo : sUserLocaleList) {
                try {
                    if (lInfo != null && TextUtils.equals(lInfo.getLocale().getLanguage(), currentLanguage)) {
                        currentLanguageDisplay = lInfo.followSystem;
                    }
                } catch (Exception e) {
                }
            }
            if (!TextUtils.isEmpty(currentLanguageDisplay)) {
                return currentLanguageDisplay;
            }
            return localeInfoForFollowSystem.followSystem;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 展示切换语言对话框
     *
     * @param activity
     */
    public static void showLanguageDialog(Activity activity, boolean showChinese) {
        final int selectIndex = findLocaleIndex(activity);
        String followSystemString = getFollowSystemTranslation(activity, selectIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String arrays[] = new String[sUserLocaleList.size()];
        for (int index = 0; index < sUserLocaleList.size(); index++) {
            LocaleInfo localeInfo = sUserLocaleList.get(index);
            if (localeInfo != null) {
                // locale为空时，跟随系统语言
                if (localeInfo.locale == null) {
                    if (!TextUtils.isEmpty(followSystemString)) {
                        arrays[index] = followSystemString;
                    } else {
                        arrays[index] = localeInfo.display;
                    }
                } else {
                    arrays[index] = localeInfo.display;
                }
                if (showChinese) {
                    if (!TextUtils.isEmpty(localeInfo.display2)) {
                        arrays[index] = arrays[index] + " (" + localeInfo.display2 + ")";
                    } else if (localeInfo.locale == null) {
                        try {
                            arrays[index] = arrays[index] + " (跟随系统)";
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        builder.setSingleChoiceItems(arrays, selectIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (selectIndex != which) {
                    try {
                        LocaleInfo localeInfo = sUserLocaleList.get(which);
                        if (localeInfo != null) {
                            Locale locale = localeInfo.locale;
                            setSelectLocale(activity, locale);
                            changeLocale(activity, locale);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error : " + e);
                    }
                    restartApp(activity, sMainClass);
                }
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
