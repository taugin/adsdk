package com.komob.bcsdk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.komob.api.PermUI;
import com.komob.bcsdk.constant.Constant;
import com.komob.bcsdk.log.Log;
import com.komob.bcsdk.manager.ReferrerManager;
import com.komob.bcsdk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class BcSdk {

    public static final int REQUEST_NOTIFICATION_LISTENER = 10000;
    public static final int REQUEST_USAGE_PRIVILEGE = 10001;
    public static final int REQUEST_OVERLAY_DRAW = 10002;

    private static OnPermissionListener sOnPermissionListener;
    private static OnDataListener sOnDataListener;

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, OnDataListener l) {
        sOnDataListener = l;
        initialize(context);
        ReferrerManager.get(context).init();
    }

    public static String getAttribution(Context context) {
        return ReferrerManager.get(context).getAttribution();
    }

    public static String getMediaSource(Context context) {
        return ReferrerManager.get(context).getMediaSource();
    }

    public static boolean isFromClick(Context context) {
        return ReferrerManager.get(context).isFromClick();
    }

    /**
     * 进入通知栏使用权设置界面
     *
     * @param context
     */
    public static void startNotificationListener(Context context) {
        Utils.enterNotificationSettings(context);
    }

    /**
     * 判断通知栏使用权是否开启
     *
     * @param context
     * @return
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        return Utils.notificationListenersEnable(context);
    }

    public static void startUsageSettings(Context context) {
        Utils.enterUsageSettings(context);
    }

    public static void startOverlayDrawSettings(Context context) {
        Utils.openOverlaySettings(context);
    }

    public static void setOnPermissionListener(OnPermissionListener l) {
        sOnPermissionListener = l;
    }

    public static OnPermissionListener getPermissionListener() {
        return sOnPermissionListener;
    }

    public static OnDataListener getOnDataListener() {
        return sOnDataListener;
    }

    public static boolean isAllPermissionGranted(Context context, List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        for (String permission : permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Context context, List<String> permissions) {
        requestPermissions(context, permissions, false, null);
    }

    public static void requestPermissions(Context context, List<String> permissions, boolean gotoSettings) {
        requestPermissions(context, permissions, gotoSettings, null);
    }

    public static void requestPermissions(Context context, List<String> permissions, OnPermissionListener listener) {
        requestPermissions(context, permissions, false, listener);
    }

    public static void requestPermissions(Context context, List<String> permissions, boolean gotoSettings, OnPermissionListener listener) {
        if (listener != null) {
            setOnPermissionListener(listener);
        }
        OnPermissionListener onPermissionListener = getPermissionListener();
        if (context == null || permissions == null || permissions.isEmpty()) {
            if (onPermissionListener != null) {
                onPermissionListener.onPermissionResult(null, null, false);
            }
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (onPermissionListener != null) {
                onPermissionListener.onPermissionResult(permissions, null, false);
            }
            return;
        }
        ArrayList<String> needReqList = new ArrayList<String>();
        ArrayList<String> grantList = new ArrayList<String>();
        for (String p : permissions) {
            if (context.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                needReqList.add(p);
            } else {
                grantList.add(p);
            }
        }
        // 所有权限已经全部授权
        if (needReqList.isEmpty()) {
            if (onPermissionListener != null) {
                onPermissionListener.onPermissionResult(grantList, null, false);
            }
            return;
        }
        ArrayList<String> allReq = new ArrayList<String>(permissions);
        try {
            Intent intent = new Intent(context, PermUI.class);
            intent.setPackage(context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 调用系统授权界面
            intent.putStringArrayListExtra(Constant.EXTRA_PERMISSIONS, allReq);
            intent.putExtra(Constant.EXTRA_GOTO_SETTINGS, gotoSettings);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            if (onPermissionListener != null) {
                onPermissionListener.onPermissionResult(grantList, null, false);
            }
        }
    }

    public static void requestAllFileAccess(Context context, OnPermissionListener listener) {
        requestAllFileAccess(context, listener, false);
    }

    public static void requestAllFileAccess(Context context, OnPermissionListener listener, boolean autoCheck) {
        if (listener != null) {
            setOnPermissionListener(listener);
        }
        OnPermissionListener onPermissionListener = getPermissionListener();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (context == null) {
                if (onPermissionListener != null) {
                    onPermissionListener.onAllFilesAccessResult(Environment.isExternalStorageManager());
                }
                return;
            }
            List<String> permissions = Arrays.asList(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            if (Environment.isExternalStorageManager()) {
                if (listener != null) {
                    onPermissionListener.onAllFilesAccessResult(Environment.isExternalStorageManager());
                }
            } else {
                ArrayList<String> allReq = new ArrayList<String>(permissions);
                try {
                    Intent intent = new Intent(context, PermUI.class);
                    intent.setPackage(context.getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 调用系统授权界面
                    intent.putStringArrayListExtra(Constant.EXTRA_PERMISSIONS, allReq);
                    intent.putExtra(Constant.EXTRA_AUTO_CHECK, autoCheck);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                    if (onPermissionListener != null) {
                        onPermissionListener.onAllFilesAccessResult(Environment.isExternalStorageManager());
                    }
                }
            }
        } else {
            if (onPermissionListener != null) {
                onPermissionListener.onAllFilesAccessResult(false);
            }
        }
    }

    public static void requestOverlayDraw(Context context, OnPermissionListener listener) {
        requestOverlayDraw(context, listener, false);
    }

    public static void requestOverlayDraw(Context context, OnPermissionListener listener, boolean autoCheck) {
        if (listener != null) {
            setOnPermissionListener(listener);
        }
        OnPermissionListener onPermissionListener = getPermissionListener();
        if (context == null) {
            if (onPermissionListener != null) {
                onPermissionListener.onOverlayDrawResult(false);
            }
            return;
        }
        List<String> permissions = Arrays.asList(Manifest.permission.SYSTEM_ALERT_WINDOW);
        if (Utils.canDrawOverlays(context)) {
            if (listener != null) {
                onPermissionListener.onOverlayDrawResult(true);
            }
        } else {
            ArrayList<String> allReq = new ArrayList<String>(permissions);
            try {
                Intent intent = new Intent(context, PermUI.class);
                intent.setPackage(context.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // 调用系统授权界面
                intent.putStringArrayListExtra(Constant.EXTRA_PERMISSIONS, allReq);
                intent.putExtra(Constant.EXTRA_AUTO_CHECK, autoCheck);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
                if (onPermissionListener != null) {
                    onPermissionListener.onOverlayDrawResult(Utils.canDrawOverlays(context));
                }
            }
        }
    }

    public static void requestNotificationListener(Context context, OnPermissionListener listener) {
        requestNotificationListener(context, listener, false);
    }

    public static void requestNotificationListener(Context context, OnPermissionListener listener, boolean autoCheck) {
        if (listener != null) {
            setOnPermissionListener(listener);
        }
        OnPermissionListener onPermissionListener = getPermissionListener();
        if (context == null) {
            if (onPermissionListener != null) {
                onPermissionListener.onNotificationListenerResult(false);
            }
            return;
        }
        List<String> permissions = Arrays.asList(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
        if (Utils.notificationListenersEnable(context)) {
            if (listener != null) {
                onPermissionListener.onNotificationListenerResult(true);
            }
        } else {
            ArrayList<String> allReq = new ArrayList<String>(permissions);
            try {
                Intent intent = new Intent(context, PermUI.class);
                intent.setPackage(context.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // 调用系统授权界面
                intent.putStringArrayListExtra(Constant.EXTRA_PERMISSIONS, allReq);
                intent.putExtra(Constant.EXTRA_AUTO_CHECK, autoCheck);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
                if (onPermissionListener != null) {
                    onPermissionListener.onNotificationListenerResult(Utils.notificationListenersEnable(context));
                }
            }
        }
    }

    public static void requestUsageAccess(Context context, OnPermissionListener listener) {
        requestUsageAccess(context, listener, false);
    }

    public static void requestUsageAccess(Context context, OnPermissionListener listener, boolean autoCheck) {
        if (listener != null) {
            setOnPermissionListener(listener);
        }
        OnPermissionListener onPermissionListener = getPermissionListener();
        if (context == null) {
            if (onPermissionListener != null) {
                onPermissionListener.onUsageAccessResult(false);
            }
            return;
        }
        List<String> permissions = Arrays.asList(Manifest.permission.PACKAGE_USAGE_STATS);
        if (Utils.isAccessUsageEnable(context)) {
            if (listener != null) {
                onPermissionListener.onUsageAccessResult(true);
            }
        } else {
            ArrayList<String> allReq = new ArrayList<String>(permissions);
            try {
                Intent intent = new Intent(context, PermUI.class);
                intent.setPackage(context.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // 调用系统授权界面
                intent.putStringArrayListExtra(Constant.EXTRA_PERMISSIONS, allReq);
                intent.putExtra(Constant.EXTRA_AUTO_CHECK, autoCheck);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
                if (onPermissionListener != null) {
                    onPermissionListener.onUsageAccessResult(Utils.isAccessUsageEnable(context));
                }
            }
        }
    }

    private static void initialize(Context context) {
        recordFirstStart(context);
    }

    private static void recordFirstStart(Context context) {
        long firstTime = Utils.getLong(context, Constant.PREF_FIRST_START, 0);
        if (firstTime <= 0) {
            Utils.putLong(context, Constant.PREF_FIRST_START, System.currentTimeMillis());
        }
    }
}