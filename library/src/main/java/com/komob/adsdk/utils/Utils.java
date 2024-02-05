package com.komob.adsdk.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/27.
 */

public class Utils {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private static char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String byte2MD5(byte[] byteArray) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            return "";
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = md5Bytes[i] & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static String string2MD5(String source) {
        return string2MD5(source, "utf-8");
    }

    public static String string2MD5(String source, String encode) {
        try {
            return byte2MD5(source.getBytes(encode));
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return "";
    }

    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    /**
     * 获取进程名称
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    public static void clearPrefs(Context context, String key) {
        try {
            key = encryptSpKey(context, key);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply();
        } catch (Exception | Error e) {
        }
    }

    public static void putString(Context context, String key, String value) {
        putString(context, key, value, false);
    }

    public static void putString(Context context, String key, String value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value);
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
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        putBoolean(context, key, value, false);
    }

    public static void putBoolean(Context context, String key, boolean value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value);
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
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        putLong(context, key, value, false);
    }

    public static void putLong(Context context, String key, long value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value);
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
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
    }

    public static void putFloat(Context context, String key, float value) {
        putFloat(context, key, value, false);
    }

    public static void putFloat(Context context, String key, float value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value);
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
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
    }

    public static void putStringSet(Context context, String key, Set<String> value) {
        putStringSet(context, key, value, false);
    }

    public static void putStringSet(Context context, String key, Set<String> value, boolean sync) {
        try {
            key = encryptSpKey(context, key);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(key, value);
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
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(key, sets);
    }

    private static String encryptSpKey(Context context, String key) {
        if (isContainKey(context, key)) {
            return key;
        }
        return "pref_" + Utils.string2MD5(key);
    }

    private static boolean isContainKey(Context context, String key) {
        try {
            return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
        } catch (Exception e) {
        }
        return false;
    }

    public static void copyAssets(Context context, String fileName, String dstPath) {
        try {
            if (TextUtils.equals(md5sumAssetsFile(context, fileName), md5sum(dstPath))) {
                return;
            }
            InputStream is = context.getAssets().open(fileName);
            File dstFile = new File(dstPath);
            if (dstFile.exists()) {
                dstFile.delete();
            }
            dstFile.getParentFile().mkdirs();
            dstFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(dstPath);
            byte[] buf = new byte[4096];
            int read = 0;
            while ((read = is.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    public static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5 = null;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    public static String md5sumAssetsFile(Context context, String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5 = null;
        try {
            fis = context.getAssets().open(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 判断是否拥有悬浮框权限
     *
     * @param context
     * @return
     */
    public static boolean canDrawOverlays(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context);
            }
        } catch (Exception e) {
        } catch (Error e) {
        }
        return true;
    }

    /**
     * 打开悬浮框设置界面
     *
     * @param context
     * @param requestCode
     */
    public static void openOverlaySettings(Context context, int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
        }
    }

    private static String readFromStream(InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            StringBuilder builder = new StringBuilder();
            int read = 0;
            byte[] buf = new byte[1024];
            while ((read = is.read(buf)) > 0) {
                builder.append(new String(buf, 0, read));
            }
            is.close();
            return builder.toString();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    public static String readConfig(Context context, String configName) {
        String localConfig = null;
        localConfig = readAssets(context, configName);
        if (!TextUtils.isEmpty(localConfig)) {
            Log.iv(Log.TAG_SDK, "assets config | [" + configName + "]" + " : " + localConfig);
            return localConfig;
        }
        return null;
    }

    public static String readAssets(Context context, String filePath) {
        try {
            InputStream is = context.getAssets().open(filePath);
            return readFromStream(is);
        } catch (Exception e) {
        }
        return null;
    }

    public static String readLocal(String filePath) {
        try {
            InputStream is = new FileInputStream(filePath);
            return readFromStream(is);
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean launchApplication(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断应用是否已经安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
            if (info != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static String getImei(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return telephonyManager.getImei();
                } else {
                    return telephonyManager.getDeviceId();
                }
            }
        }
        return null;
    }

    public static String getAndroidId(Context context) {
        try {
            String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return ANDROID_ID;
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 顶层Activity判断
     *
     * @param context
     * @return
     */
    public static boolean isTopActivy(Context context) {
        boolean isTop = false;
        try {
            String packageName = context.getPackageName();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = ((ActivityManager.RunningTaskInfo) am.getRunningTasks(1).get(0)).topActivity;
            String currentPackageName = cn.getPackageName();
            isTop = currentPackageName != null && currentPackageName.equals(packageName);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return isTop;
    }

    public static boolean isScreenOn(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = powerManager.isScreenOn();
            return isScreenOn;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isScreenLocked(Context context) {
        try {
            KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            boolean isScreenLocked = mKeyguardManager.inKeyguardRestrictedInputMode();
            return isScreenLocked;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            return getPackageInfo(context).versionName;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            return getPackageInfo(context).versionCode;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return -1;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return pi;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return pi;
    }

    public static String getMetaData(Context context, String name) {
        String value = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (appInfo != null) {
                value = appInfo.metaData.get(name).toString();
            }
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 获取国家代码
     *
     * @param context
     * @return
     */
    public static String getCountry(Context context) {
        String country = getCountryFromSimOrNetwork(context);
        if (TextUtils.isEmpty(country)) {
            country = getCountryFromLocale(context);
        }
        return country;
    }

    /**
     * 从SIM卡或网络获取国家代码
     *
     * @param context
     * @return
     */
    private static String getCountryFromSimOrNetwork(Context context) {
        String country = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            country = telephonyManager.getSimCountryIso();
            if (TextUtils.isEmpty(country)) {
                country = telephonyManager.getNetworkCountryIso();
            }
            country = country.toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
        }
        return country;
    }

    /**
     * 从locale获取国家代码
     *
     * @param context
     * @return
     */
    public static String getCountryFromLocale(Context context) {
        String country = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            country = locale.getCountry().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
            country = "unknown";
        }
        return country;
    }

    public static Intent getIntentByAction(Context context, String action) {
        return getIntentByAction(context, action, true, true);
    }

    public static Intent getIntentByAction(Context context, String action, boolean addNoHistory) {
        return getIntentByAction(context, action, addNoHistory, true);
    }

    public static Intent getIntentByAction(Context context, String action, boolean addNoHistory, boolean random) {
        Intent intent = null;
        if (TextUtils.isEmpty(action)) {
            return intent;
        }
        try {
            Intent queryIntent = new Intent(action);
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(queryIntent, 0);
            List<String> activityNames = new ArrayList<String>();
            List<Boolean> singleInstance = new ArrayList<Boolean>();
            for (ResolveInfo info : list) {
                if (info != null && info.activityInfo != null && !TextUtils.isEmpty(info.activityInfo.name)) {
                    activityNames.add(info.activityInfo.name);
                    singleInstance.add(info.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE);
                }
            }
            if (!activityNames.isEmpty()) {
                int size = activityNames.size();
                int index = 0;
                if (random) {
                    index = new Random(System.currentTimeMillis()).nextInt(size);
                }
                String className = activityNames.get(index);
                boolean isSingleInstance = false;
                try {
                    isSingleInstance = singleInstance.get(index);
                } catch (Exception e) {
                }
                intent = new Intent(action);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                if (!isSingleInstance && addNoHistory) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                }
                intent.setClassName(context.getPackageName(), className);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return intent;
    }

    public static Method getClassMethod(String packageName, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException, ClassNotFoundException {
        return Class.forName(packageName).getMethod(methodName, parameterTypes);
    }

    public static String getActivityNameByAction(Context context, String action) {
        String actName = null;
        if (TextUtils.isEmpty(action)) {
            return actName;
        }
        try {
            Intent queryIntent = new Intent(action);
            queryIntent.setPackage(context.getPackageName());
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(queryIntent, 0);
            if (list != null && !list.isEmpty()) {
                ResolveInfo info = list.get(0);
                actName = info.activityInfo.name;
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        Log.iv(Log.TAG, "actName : " + actName);
        return actName;
    }

    public static String getStringById(Context context, String strName) {
        try {
            int id = context.getResources().getIdentifier(strName, "string", context.getPackageName());
            return context.getResources().getString(id);
        } catch (Exception e) {
        }
        return null;
    }

    public static long getTodayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static String char2Md5(byte[] byteArray) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            return "";
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = md5Bytes[i] & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static String getSignMd5(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            String signStr = char2Md5(sign.toByteArray());
            return signStr;
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return "";
    }

    public static int getScreenWidth(Context context) {
        try {
            return context.getResources().getDisplayMetrics().widthPixels;
        } catch (Exception e) {
        }
        return 0;
    }

    public static int getApplicationIcon(Context context) {
        try {
            return context.getApplicationInfo().icon;
        } catch (Exception e) {
        }
        return 0;
    }

    public static boolean isVPNConnected(Context context) {
        boolean isVpnConn = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_VPN);
                isVpnConn = networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception | Error e) {
        }
        return isVpnConn;
    }

    public static boolean isDebugEnabled(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
        } catch (Exception | Error e) {
        }
        return false;
    }

    public static boolean isUsbConnected(Context context) {
        try {
            Intent intent = context.registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
            return intent.getExtras().getBoolean("connected");
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isAdbUsbEnabled(Context context) {
        return isUsbConnected(context) && isDebugEnabled(context);
    }

    public static String calcRoundCpm(double revenue) {
        if (revenue < 0.000001) {
            return String.valueOf(0);
        }
        if (revenue >= 0.000001 && revenue < 0.0001) {
            return BigDecimal.valueOf(revenue).setScale(6, RoundingMode.HALF_EVEN).toPlainString();
        }
        if (revenue >= 0.0001 && revenue < 0.01) {
            return BigDecimal.valueOf(revenue).setScale(4, RoundingMode.HALF_EVEN).toPlainString();
        }
        if (revenue >= 0.01 && revenue < 1) {
            return BigDecimal.valueOf(revenue).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
        }
        if (revenue >= 1f && revenue < 10f) {
            return BigDecimal.valueOf(revenue).setScale(1, RoundingMode.HALF_EVEN).toPlainString();
        }
        if (revenue >= 10f && revenue < 100f) {
            return BigDecimal.valueOf(revenue).setScale(0, RoundingMode.HALF_EVEN).toPlainString();
        }
        if (revenue >= 100f && revenue < 500f) {
            return String.valueOf(Math.round(revenue));
        }
        if (revenue >= 500f && revenue < 1000f) {
            return String.valueOf(Math.round(revenue / 10) * 10);
        }
        return String.valueOf(Math.round(revenue / 100) * 100);
    }

    public static String formatNetwork(String network) {
        if (network == null) {
            return Constant.AD_NETWORK_EMPTY;
        }
        network = network.toLowerCase(Locale.ENGLISH);
        if (network.contains("applovin")) {
            return Constant.AD_NETWORK_APPLOVIN;
        }
        if (network.contains("admob")) {
            return Constant.AD_NETWORK_ADMOB;
        }
        if (network.contains("facebook") || network.contains("meta audience network") || network.contains("audience-network")) {
            return Constant.AD_NETWORK_FACEBOOK;
        }
        if (network.contains("mintegral")) {
            return Constant.AD_NETWORK_MINTEGRAL;
        }
        if (network.contains("inmobi")) {
            return Constant.AD_NETWORK_INMOBI;
        }
        if (network.contains("smaato")) {
            return Constant.AD_NETWORK_SMAATO;
        }
        if (network.contains("unity")) {
            return Constant.AD_NETWORK_UNITY;
        }
        if (network.contains("tapjoy")) {
            return Constant.AD_NETWORK_TAPJOY;
        }
        if (network.contains("vungle") || network.contains("liftoff")) {
            return Constant.AD_NETWORK_VUNGLE;
        }
        if (network.contains("ironsource")) {
            return Constant.AD_NETWORK_IRONSOURCE;
        }
        if (network.contains("bigo")) {
            return Constant.AD_NETWORK_BIGO;
        }
        return Constant.AD_NETWORK_UNKNOWN;
    }

    public static void runOnUIThread(Runnable runnable) {
        if (sHandler != null) {
            sHandler.post(runnable);
        }
    }

    public static boolean isDebuggable(Context context) {
        boolean debuggable = false;
        try {
            debuggable = 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
        } catch (Exception e) {
        }
        return debuggable;
    }
}