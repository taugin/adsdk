package com.komob.bcsdk.utils;

import android.app.Activity;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.komob.bcsdk.BcSdk;
import com.komob.bcsdk.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/11.
 */

public class Utils {
    private static char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};

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
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
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
            Log.d(Log.TAG, "error : " + e);
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
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private static String char2Md5(byte[] byteArray) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
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

    public static String string2MD5(String str) {
        MessageDigest md5 = null;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
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

    public static void putString(Context context, String key, String value) {
        try {
            key = encryptSpKey(context, key);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
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

    public static void putLong(Context context, String key, long value) {
        try {
            key = encryptSpKey(context, key);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
        } catch (Exception | Error e) {
        }
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, 0);
    }

    public static long getLong(Context context, String key, long defValue) {
        key = encryptSpKey(context, key);
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
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
            // Log.d(Log.TAG, "error : " + e);
        }
        return false;
    }

    public static String getVerName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
        }
        return null;
    }

    public static int getVerCode(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (Exception e) {
        }
        return 0;
    }

    public static void putBoolean(Context context, String key, boolean value) {
        try {
            key = encryptSpKey(context, key);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
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

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = -1;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 判断是否开启悬浮窗
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
     */
    public static boolean openOverlaySettings(Context context) {
        try {
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
            } else {
                intent = getIntentUnderM(context);
            }
            if (intent == null || !canStart(context, intent)) {
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
            }
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, BcSdk.REQUEST_OVERLAY_DRAW);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            return true;
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return false;
    }

    public static boolean canStart(Context context, Intent intent) {
        if (intent != null) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list != null && !list.isEmpty()) {
                return true;
            }
        }
        return false;
    }


    public static Intent getIntentUnderM(Context context) {
        String manifacture = Build.MANUFACTURER.toLowerCase(Locale.getDefault());
        Intent intent = null;
        if ("huawei".equals(manifacture)) {
            intent = new Intent();
            intent.setPackage("com.huawei.systemmanager");
            intent.setClassName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        } else if ("meizu".equals(manifacture)) {
            intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", context.getPackageName());
        } else if ("oppo".equals(manifacture)) {
            intent = new Intent();
            intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity");
        } else if ("zte".equals(manifacture)) {
            intent = new Intent();
            intent.setAction("com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER");
        } else if ("xiaomi".equals(manifacture)) {
            String property = getProperty("ro.miui.ui.version.name");
            if ("V8".equalsIgnoreCase(property)) {
                intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                Uri data = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(data);
            } else if ("V5".equalsIgnoreCase(property)) {
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("extra_pkgname", context.getPackageName());
            }
        }
        return intent;
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
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    public static String readConfig(Context context, String configName) {
        String localConfig = null;
        localConfig = readAssets(context, configName);
        if (!TextUtils.isEmpty(localConfig)) {
            Log.iv(Log.TAG, "assets config | [" + configName + "]" + " : " + localConfig);
            return localConfig;
        }
        try {
            String localConfigFile = new File(context.getExternalFilesDir("config"), configName).getAbsolutePath();
            Log.iv(Log.TAG, "config locale : " + localConfigFile);
            localConfig = readLocal(localConfigFile);
        } catch (Exception e) {
        }
        if (!TextUtils.isEmpty(localConfig)) {
            Log.iv(Log.TAG, "sdcard config | [" + configName + "]" + " : " + localConfig);
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

    public static Intent getLaunchIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent == null) {
            Intent queryIntent = new Intent(Utils.getShortcutAction(context));
            queryIntent.setPackage(context.getPackageName());
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(queryIntent, 0);
            if (list != null && !list.isEmpty()) {
                ResolveInfo info = list.get(0);
                if (info != null && info.activityInfo != null) {
                    queryIntent.setClassName(context.getPackageName(), info.activityInfo.name);
                    intent = queryIntent;
                }
            }
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static Bitmap getAppBitmap(Context context) {
        Bitmap bmp = null;
        try {
            PackageManager pm = context.getPackageManager();
            Drawable drawable = pm.getApplicationIcon(context.getPackageName());
            if (drawable == null) {
                drawable = pm.getActivityIcon(pm.getLaunchIntentForPackage(context.getPackageName()));
            }
            if (drawable instanceof BitmapDrawable) {
                bmp = ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return bmp;
    }

    public static boolean launchApplication(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean hasMarket(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        return list != null && !list.isEmpty();
    }

    /**
     * 判断通知使用权是否开启
     *
     * @param context
     * @return
     */
    public static boolean notificationListenersEnable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return true;
        }
        String str = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(str)) {
            String splitArray[] = str.split(":");
            if (splitArray != null && splitArray.length > 0) {
                ComponentName cmp = null;
                for (int index = 0; index < splitArray.length; index++) {
                    cmp = ComponentName.unflattenFromString(splitArray[index]);
                    if ((cmp != null) && (TextUtils.equals(context.getPackageName(), cmp.getPackageName()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 进入通知栏使用权设置界面
     *
     * @param context
     */
    public static boolean enterNotificationSettings(Context context) {
        boolean enterSuccess = false;
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, BcSdk.REQUEST_NOTIFICATION_LISTENER);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            enterSuccess = true;
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, BcSdk.REQUEST_NOTIFICATION_LISTENER);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                enterSuccess = true;
            } catch (Exception ex) {
                Log.e(Log.TAG, "error : " + ex);
            }
        }
        return enterSuccess;
    }

    /**
     * 判断是否启用查看应用使用权
     *
     * @param context
     * @return
     */
    public static boolean isAccessUsageEnable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {   // 如果大于等于5.0 再做判断
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Service.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 进入应用使用权限
     *
     * @param context
     */
    public static boolean enterUsageSettings(Context context) {
        boolean enterSuccess = false;
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                if (!canPerformIntent(context, intent)) {
                    intent.setData(null);
                }
            }
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, BcSdk.REQUEST_USAGE_PRIVILEGE);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            enterSuccess = true;
        } catch (ActivityNotFoundException e) {
        }
        return enterSuccess;
    }

    public static boolean canPerformIntent(Context context, Intent intent) {
        try {
            PackageManager mgr = context.getPackageManager();
            List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : list) {
                if (null != resolveInfo.activityInfo && resolveInfo.activityInfo.exported) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
        }
        return null;
    }

    public static String getProperty(String str) {
        String str2 = "";
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            return (String) cls.getDeclaredMethod("get", new Class[]{String.class}).invoke(cls, new Object[]{str});
        } catch (Exception e) {
        } catch (Error e) {
        }
        return str2;
    }

    public static String getShortcutAction(Context context) {
        try {
            return context.getPackageName() + ".action.LAUNCHER";
        } catch (Exception e) {
        }
        return Intent.ACTION_MAIN;
    }

    public static Intent getIntentByAction(Context context, String action) {
        Intent intent = null;
        if (TextUtils.isEmpty(action)) {
            return intent;
        }
        try {
            Intent queryIntent = new Intent(action);
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(queryIntent, 0);
            List<String> activityNames = new ArrayList<String>();
            for (ResolveInfo info : list) {
                if (info != null && info.activityInfo != null && !TextUtils.isEmpty(info.activityInfo.name)) {
                    activityNames.add(info.activityInfo.name);
                }
            }
            if (!activityNames.isEmpty()) {
                int size = activityNames.size();
                String className = activityNames.get(new Random(System.currentTimeMillis()).nextInt(size));
                intent = new Intent(action);
                intent.setClassName(context.getPackageName(), className);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return intent;
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
            Log.e(Log.TAG, "error : " + e);
        }
        return "";
    }

    public static String getAndroidId(Context context) {
        try {
            String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
            return ANDROID_ID;
        } catch (Exception e) {
        }
        return "";
    }

    public static boolean isDebuggable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
        }
        return false;
    }

    public static Object reflectCall(Object object, String className, String methodName, Class<?>[] argType, Object[] argValue) {
        String error = null;
        try {
            Class<?> cls = Class.forName(className);
            Method method = cls.getMethod(methodName, argType);
            return method.invoke(object, argValue);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "error : " + error);
        }
        return null;
    }

    private static final Map<String, String> STRING_MAP_EN = new HashMap<>();
    private static final Map<String, Map<String, String>> LANGUAGE_MAP = new HashMap<>();

    static {
        LANGUAGE_MAP.put("en", STRING_MAP_EN);
        STRING_MAP_EN.put("bsc_cancel", "Cancel");
        STRING_MAP_EN.put("bsc_settings", "Settings");
        STRING_MAP_EN.put("bsc_permission_title", "Miss Permissions");
        STRING_MAP_EN.put("bsc_permission_content", "Please grant %s necessary permissions.\n\nClick Settings -> Permissions.");
    }

    private static String[] getLanguageAndCountry(Context context) {
        String[] languageAndCountry = new String[]{"", ""};
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            languageAndCountry[0] = locale.getLanguage().toLowerCase(Locale.ENGLISH);
            languageAndCountry[1] = locale.getCountry().toLowerCase(Locale.ENGLISH);
        } catch (Exception e) {
        }
        return languageAndCountry;
    }

    public static String getStringValue(Context context, String key, Object... formatArgs) {
        try {
            if (context == null || TextUtils.isEmpty(key)) {
                return null;
            }
            String languageAndCountry[] = getLanguageAndCountry(context);
            Map<String, String> languageMap = LANGUAGE_MAP.get(languageAndCountry[0] + "-" + languageAndCountry[1]);
            if (languageMap == null) {
                languageMap = LANGUAGE_MAP.get(languageAndCountry[0]);
            }
            if (languageMap == null) {
                languageMap = STRING_MAP_EN;
            }
            if (languageMap != null) {
                return String.format(Locale.getDefault(), languageMap.get(key), formatArgs);
            }
        } catch (Exception e) {

        }
        return "";
    }

}