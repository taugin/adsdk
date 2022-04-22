package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.rabbit.adsdk.AdSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChangeLanguage {
    private static final String PREF_LANGUAGE_SWITCH_LANGUAGE = "pref_language_switch_language";
    private static final String PREF_LANGUAGE_SWITCH_COUNTRY = "pref_language_switch_country";
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

    public static void init(Context context, Class<?> mainClass) {
        sMainClass = mainClass;
        registerActivityLifeCycleCallback(context);
    }

    private static void registerActivityLifeCycleCallback(Context context) {
        ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                sActivityList.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
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
        sUserLocaleList = sLocaleList;
        sUserLocaleList.add(0, new LocaleInfo(Locale.getDefault(), Locale.getDefault().getDisplayLanguage()));
    }

    public static final class CustomContextThemeWrapper extends ContextThemeWrapper {
        Configuration mConfiguration;

        CustomContextThemeWrapper(Configuration configuration, Context context, int index) {
            super(context, index);
            mConfiguration = configuration;
        }

        public void applyOverrideConfiguration(Configuration configuration) {
            if (configuration != null) {
                configuration.setTo(this.mConfiguration);
            }
            super.applyOverrideConfiguration(configuration);
        }
    }

    private static Locale getSelectLocale(Context context) {
        String language = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LANGUAGE_SWITCH_LANGUAGE, null);
        String country = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LANGUAGE_SWITCH_COUNTRY, "");
        if (!TextUtils.isEmpty(language)) {
            return new Locale(language, country);
        }
        return getCurrentLocale(context);
    }

    private static void setSelectLocale(Context context, Locale userSetLocale) {
        if (context != null && userSetLocale != null) {
            String language = userSetLocale.getLanguage();
            String country = userSetLocale.getCountry();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_LANGUAGE_SWITCH_LANGUAGE, language).commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_LANGUAGE_SWITCH_COUNTRY, country).commit();
        }
    }

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

    public static boolean isLanguageSwitchStartup(Context context) {
        String visible = AdSdk.get(context).getString("change_language_visible");
        return TextUtils.equals(visible, "true") || BuildConfig.DEBUG;
    }

    public static Context createConfigurationContext(Context context) {
        Context configContext = context;
        try {
            Locale locale = getSelectLocale(context);
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(locale);
            if (Build.VERSION.SDK_INT >= 24) {
                configContext = context.createConfigurationContext(configuration);
            } else {
                configContext = new CustomContextThemeWrapper(configuration, context, android.R.style.Theme_DeviceDefault);
            }
        } catch (Exception e) {
        }
        if (configContext == null) {
            configContext = context;
        }
        return configContext;
    }

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

    private static void restartApp(Activity activity, Class<?> clazz) {
        try {
            for (Activity activity1 : sActivityList) {
                activity1.finish();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.startActivity(new Intent(activity, clazz));
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 500);
        } catch (Exception e2) {
        }
    }

    private static void changeLocale(Context context, LocaleInfo localeInfo) {
        try {
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = localeInfo.locale;
            context.getResources().updateConfiguration(configuration, null);
        } catch (Exception e2) {
        }
    }

    private static int findLocaleIndex(Context context) {
        String language = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LANGUAGE_SWITCH_LANGUAGE, null);
        String country = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LANGUAGE_SWITCH_COUNTRY, "");
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
        if (selectIndex < 0) {
            selectIndex = 0;
        }
        return selectIndex;
    }

    private static boolean showChineseSimple(Context context) {
        return true;
    }

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
        int selectIndex = findLocaleIndex(activity);
        builder.setSingleChoiceItems(arrays, selectIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    dialog.dismiss();
                    LocaleInfo localeInfo = sUserLocaleList.get(which);
                    changeLocale(activity, localeInfo);
                    if (localeInfo != null) {
                        setSelectLocale(activity, localeInfo.locale);
                    }
                } catch (Exception e) {
                }
                restartApp(activity, sMainClass);
            }
        });
        builder.create().show();
    }
}
