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
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.rabbit.adsdk.AdSdk;

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
    private static final String PREF_LANGUAGE_SWITCH_LANGUAGE = "pref_language_switch_language";
    private static final String PREF_LANGUAGE_SWITCH_COUNTRY = "pref_language_switch_country";
    private static final String TAG = "ChangeLanguage";

    private static class LocaleInfo {
        private Locale locale;
        private String display;
        private String display2;

        public LocaleInfo(Locale locale, String display) {
            this(locale, display, "");
        }

        public LocaleInfo(Locale locale, String display, String display2) {
            this.locale = locale;
            this.display = display;
            this.display2 = display2;
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
        sLocaleList.add(new LocaleInfo(Locale.ENGLISH, "English", "英语"));
        sLocaleList.add(new LocaleInfo(new Locale("ar"), "العربية", "阿拉伯语"));
        sLocaleList.add(new LocaleInfo(new Locale("bn", "IN"), "বাংলা", "孟加拉语"));
        sLocaleList.add(new LocaleInfo(Locale.GERMANY, "Deutsch", "德语"));
        sLocaleList.add(new LocaleInfo(new Locale("es"), "Español", "西班牙语"));
        sLocaleList.add(new LocaleInfo(Locale.FRENCH, "Français", "法语"));
        sLocaleList.add(new LocaleInfo(new Locale("hi"), "हिंदी", "印地语"));
        sLocaleList.add(new LocaleInfo(Locale.ITALY, "Italiano", "意大利语"));
        sLocaleList.add(new LocaleInfo(new Locale("ja"), "日本語", "日语"));
        sLocaleList.add(new LocaleInfo(Locale.KOREA, "한국어", "韩语"));
        sLocaleList.add(new LocaleInfo(new Locale("in", "ID"), "Indonesia", "印尼语"));
        sLocaleList.add(new LocaleInfo(new Locale("ms"), "Melayu", "马来语"));
        sLocaleList.add(new LocaleInfo(new Locale("nl"), "Nederlands", "荷兰语"));
        sLocaleList.add(new LocaleInfo(new Locale("pt"), "Português", "葡萄牙语"));
        sLocaleList.add(new LocaleInfo(new Locale("ru"), "Русский", "俄语"));
        sLocaleList.add(new LocaleInfo(new Locale("sv"), "Svenska", "瑞典语"));
        sLocaleList.add(new LocaleInfo(new Locale("th"), "ภาษาไทย", "泰语"));
        sLocaleList.add(new LocaleInfo(new Locale("tr"), "Türkçe", "土耳其语"));
        sLocaleList.add(new LocaleInfo(new Locale("uk"), "Український", "乌克兰语"));
        sLocaleList.add(new LocaleInfo(new Locale("vi"), "Việt", "越南语"));
        sLocaleList.add(new LocaleInfo(Locale.SIMPLIFIED_CHINESE, "简体中文", ""));
        sLocaleList.add(new LocaleInfo(Locale.TAIWAN, "繁體中文", ""));
        sLocaleList.add(new LocaleInfo(new Locale("fa"), "فارسی", "波斯语"));
        sLocaleList.add(new LocaleInfo(new Locale("pl"), "Polski", "波兰语"));
        sLocaleList.add(new LocaleInfo(new Locale("fil"), "Pilipinas", "菲律宾语"));
        sUserLocaleList = new ArrayList<>();
        //locale为空时，默认行为是跟随系统的语言
        sUserLocaleList.add(0, new LocaleInfo(null, "Follow System"));
        sUserLocaleList.add(new LocaleInfo(Locale.ENGLISH, "English", "英语"));
        sUserLocaleList.add(new LocaleInfo(new Locale("es"), "Español", "西班牙语"));
        sUserLocaleList.add(new LocaleInfo(new Locale("in", "ID"), "Indonesia", "印尼语"));
        sUserLocaleList.add(new LocaleInfo(new Locale("ja"), "日本語", "日语"));
        sUserLocaleList.add(new LocaleInfo(Locale.KOREA, "한국어", "韩语"));
        sUserLocaleList.add(new LocaleInfo(new Locale("pt"), "Português", "葡萄牙语"));
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
            SharedPreferences sharedPreferences = context.getSharedPreferences("sp_change_language", Context.MODE_PRIVATE);
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
    private static void setSelectLocale(Context context, Locale userSetLocale) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("sp_change_language", Context.MODE_PRIVATE);
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
     * 判断是否开启应用内语言设置
     *
     * @param context
     * @return
     */
    public static boolean isLanguageSwitchStartup(Context context) {
        String visible = AdSdk.get(context).getString("change_language_visible");
        return TextUtils.equals(visible, "true") || BuildConfig.DEBUG;
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

    /**
     * 获取显示语言
     *
     * @param context
     * @return
     */
    public static String getCurrentLanguage(Context context) {
        String currentLanguage = null;
        int index = findLocaleIndex(context);
        LocaleInfo localeInfo = sUserLocaleList.get(index);
        if (localeInfo != null && !TextUtils.isEmpty(localeInfo.display)) {
            currentLanguage = localeInfo.display;
        } else {
            currentLanguage = Locale.getDefault().getDisplayLanguage();
        }
        return currentLanguage;
    }

    /**
     * 语言设置成功后，重启应用
     *
     * @param activity
     * @param clazz
     */
    private static void restartApp(Activity activity, final Class<?> clazz) {
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
                    // android.os.Process.killProcess(android.os.Process.myPid());
                    // System.exit(0);
                }
            }, 100);
        } catch (Exception e2) {
        }
    }

    /**
     * 更新语言区域
     *
     * @param context
     * @param locale
     */
    private static void changeLocale(Context context, Locale locale) {
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

    /**
     * 查找当前设置的语言在列表中的位置
     *
     * @param context
     * @return
     */
    private static int findLocaleIndex(Context context) {
        int selectIndex = -1;
        if (context != null) {
            String language = null;
            String country = "";
            SharedPreferences sharedPreferences = context.getSharedPreferences("sp_change_language", Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                language = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_LANGUAGE, null);
                country = sharedPreferences.getString(PREF_LANGUAGE_SWITCH_COUNTRY, "");
            }
            if (TextUtils.isEmpty(language)) {
                selectIndex = 0;
            } else {
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
            }
        }
        if (selectIndex < 0) {
            selectIndex = 0;
        }
        return selectIndex;
    }

    private static boolean showChineseSimple(Context context) {
        return true;
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
                    showLanguageDialog();
                }
            }
        });
    }

    public static void showLanguageDialog() {
        if (sTopActivity != null && sTopActivity.get() != null && !sTopActivity.get().isFinishing()) {
            showLanguageDialog(sTopActivity.get());
        }
    }

    /**
     * 展示切换语言对话框
     *
     * @param activity
     */
    public static void showLanguageDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String arrays[] = new String[sUserLocaleList.size()];
        for (int index = 0; index < sUserLocaleList.size(); index++) {
            LocaleInfo localeInfo = sUserLocaleList.get(index);
            arrays[index] = localeInfo.display;
            if (showChineseSimple(activity) && !TextUtils.isEmpty(localeInfo.display2)) {
                arrays[index] = arrays[index] + " (" + localeInfo.display2 + ")";
            }
        }
        final int selectIndex = findLocaleIndex(activity);
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
