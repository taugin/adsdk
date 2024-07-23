package com.komob.adsdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SpUtils {

    private static SharedPreferences getSharedPreferences(Context context) {
        String spName;
        try {
            spName = "ads" + Utils.string2MD5(context.getPackageName()) + ".sdk_prefs";
        } catch (Exception | Error e) {
            spName = "ads" + context.getPackageName() + ".sdk_prefs";
        }
        return context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public static void clearPrefs(Context context, String key) {
        try {
            key = encryptSpKey(context, key);
            getSharedPreferences(context).edit().remove(key).apply();
        } catch (Exception | Error e) {
        }
    }

    public static void putString(Context context, String key, String value) {
        putString(context, key, value, false);
    }

    public static void putString(Context context, String key, String value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = getSharedPreferences(context).edit().putString(key, value);
            if (sync) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception | Error e) {
        }
    }

    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    public static String getString(Context context, String key, String defValue) {
        key = encryptSpKey(context, key);
        return getSharedPreferences(context).getString(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        putBoolean(context, key, value, false);
    }

    public static void putBoolean(Context context, String key, boolean value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = getSharedPreferences(context).edit().putBoolean(key, value);
            if (sync) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception | Error e) {
        }
    }

    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        key = encryptSpKey(context, key);
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        putLong(context, key, value, false);
    }

    public static void putLong(Context context, String key, long value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = getSharedPreferences(context).edit().putLong(key, value);
            if (sync) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception e) {
        }
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, 0);
    }

    public static long getLong(Context context, String key, long defValue) {
        key = encryptSpKey(context, key);
        return getSharedPreferences(context).getLong(key, defValue);
    }

    public static void putFloat(Context context, String key, float value) {
        putFloat(context, key, value, false);
    }

    public static void putFloat(Context context, String key, float value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = getSharedPreferences(context).edit().putFloat(key, value);
            if (sync) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception e) {
        }
    }

    public static float getFloat(Context context, String key) {
        return getFloat(context, key, 0);
    }

    public static float getFloat(Context context, String key, float defValue) {
        key = encryptSpKey(context, key);
        return getSharedPreferences(context).getFloat(key, defValue);
    }

    public static void putStringSet(Context context, String key, Set<String> value) {
        putStringSet(context, key, value, false);
    }

    public static void putStringSet(Context context, String key, Set<String> value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = getSharedPreferences(context).edit().putStringSet(key, value);
            if (sync) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception e) {
        }
    }

    public static Set<String> getStringSet(Context context, String key) {
        return getStringSet(context, key, null);
    }

    public static Set<String> getStringSet(Context context, String key, Set<String> sets) {
        key = encryptSpKey(context, key);
        return getSharedPreferences(context).getStringSet(key, sets);
    }

    private static String encryptSpKey(Context context, String key) {
        if (isContainKey(context, key)) {
            return key;
        }
        return "pref_" + Utils.string2MD5(key);
    }

    private static boolean isContainKey(Context context, String key) {
        try {
            return getSharedPreferences(context).contains(key);
        } catch (Exception e) {
        }
        return false;
    }
}
