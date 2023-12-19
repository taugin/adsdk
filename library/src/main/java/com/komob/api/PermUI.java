package com.komob.api;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.komob.adsdk.InternalStat;
import com.komob.bcsdk.BcSdk;
import com.komob.bcsdk.constant.Constant;
import com.komob.bcsdk.log.Log;
import com.komob.bcsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 */

public class PermUI extends Activity {

    private static final int REQUEST_CODE_GOTO_SETTINGS = 0x1230;
    private static final int REQUEST_CODE_RUNTIME_PERMISSION = 0x1231;
    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 0x1232;
    private static final int REQUEST_CODE_OVERLAY_DRAW = 0x1233;
    private static final int REQUEST_CODE_USAGE_ACCESS = 0x1234;
    private static final int REQUEST_CODE_NOTIFICATION_LISTENER = 0x1235;
    private static boolean DEBUG_LOG = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mAutoCheck = false;
    private boolean mGoToSettings = false;
    private List<String> mRuntimePermissions;
    private boolean mRequestAllFileAccess = false;
    private boolean mRequestOverlayDraw = false;
    private boolean mRequestUsageAccess = false;
    private boolean mRequestNotificationListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DEBUG_LOG = Utils.isDebuggable(this);
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        Intent intent = getIntent();
        List<String> reqList = null;
        if (intent != null) {
            reqList = intent.getStringArrayListExtra(Constant.EXTRA_PERMISSIONS);
            mGoToSettings = intent.getBooleanExtra(Constant.EXTRA_GOTO_SETTINGS, false);
            mAutoCheck = intent.getBooleanExtra(Constant.EXTRA_AUTO_CHECK, false);
        }
        if (reqList == null || reqList.isEmpty()) {
            finishActivityForPermission(null, null, false);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && reqList.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
            mRequestAllFileAccess = true;
            requestAllFilesAccess();
        } else if (reqList.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            mRequestOverlayDraw = true;
            requestOverlayDraw();
        } else if (reqList.contains(Manifest.permission.PACKAGE_USAGE_STATS)) {
            mRequestUsageAccess = true;
            requestUsageAccess();
        } else if (reqList.contains(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
            mRequestNotificationListener = true;
            requestNotificationListener();
        } else {
            mRequestAllFileAccess = false;
            requestRuntimePermission(reqList);
        }
    }

    private void requestAllFilesAccess() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//添加该标志后，让activity不保存
                startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
                checkAllFileAccess();
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
                finishActivityForAllFileAccess(Environment.isExternalStorageManager());
            }
        } else {
            finishActivityForAllFileAccess(false);
        }
    }

    private void requestOverlayDraw() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        try {
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
            } else {
                intent = Utils.getIntentUnderM(this);
            }
            if (intent == null || !Utils.canStart(this, intent)) {
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_DRAW);
            checkOverlayDraw();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            finishActivityForOverlayDraw();
        }
    }

    private void requestUsageAccess() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.setData(Uri.parse("package:" + getPackageName()));
                if (!Utils.canPerformIntent(this, intent)) {
                    intent.setData(null);
                }
            }
            startActivityForResult(intent, REQUEST_CODE_USAGE_ACCESS);
            checkUsageAccess();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            finishActivityForUsageAccess();
        }
    }

    private void requestNotificationListener() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            if (!Utils.canPerformIntent(this, intent)) {
                intent = new Intent();
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
            }
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_LISTENER);
            checkNotificationListener();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
            finishActivityForNotificationListener();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "request code : " + Integer.toHexString(requestCode));
        }
        if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                finishActivityForAllFileAccess(Environment.isExternalStorageManager());
            } else {
                finishActivityForAllFileAccess(false);
            }
        } else if (requestCode == REQUEST_CODE_GOTO_SETTINGS) {
            checkPermissionFromSettings();
        } else if (requestCode == REQUEST_CODE_OVERLAY_DRAW) {
            finishActivityForOverlayDraw();
        } else if (requestCode == REQUEST_CODE_USAGE_ACCESS) {
            finishActivityForUsageAccess();
        } else if (requestCode == REQUEST_CODE_NOTIFICATION_LISTENER) {
            finishActivityForNotificationListener();
        } else {
            finishActivityForAllFileAccess(false);
        }
    }

    private void requestRuntimePermission(List<String> reqList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRuntimePermissions = new ArrayList<>(reqList);
            boolean hasException = false;
            try {
                String permissions[] = new String[reqList.size()];
                reqList.toArray(permissions);
                requestPermissions(permissions, REQUEST_CODE_RUNTIME_PERMISSION);
            } catch (Exception | Error e) {
                hasException = true;
            }
            List<String> grantList = null;
            if (reqList != null && !reqList.isEmpty()) {
                grantList = new ArrayList<String>();
                for (String s : reqList) {
                    if (checkSelfPermission(s) == PackageManager.PERMISSION_GRANTED) {
                        grantList.add(s);
                    }
                }
            }
            if (hasException) {
                finishActivityForPermission(grantList, null);
            }
        } else {
            finishActivityForPermission(reqList, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "req code : " + requestCode);
        }
        List<String> grantList = new ArrayList<String>();
        List<String> deniedList = new ArrayList<String>();
        boolean shouldShowSettings = false;
        if (requestCode == REQUEST_CODE_RUNTIME_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String p : permissions) {
                    if (checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED) {
                        grantList.add(p);
                    } else if (checkSelfPermission(p) == PackageManager.PERMISSION_DENIED) {
                        deniedList.add(p);
                        boolean shouldShow = shouldShowRequestPermissionRationale(p);
                        if (!shouldShow) {
                            shouldShowSettings = true;
                        }
                    }
                }
                if (shouldShowSettings && mGoToSettings) {
                    showDialogForUser(grantList, deniedList);
                } else {
                    finishActivityForPermission(grantList, deniedList);
                }
            } else {
                finishActivityForPermission(grantList, deniedList);
            }
        } else {
            finishActivityForPermission(grantList, deniedList);
        }
    }

    private void finishActivityForPermission(List<String> grantList, List<String> deniedList) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        finishActivityForPermission(grantList, deniedList, false);
    }

    private void finishActivityForPermission(List<String> grantList, List<String> deniedList, final boolean goSettings) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (BcSdk.getPermissionListener() != null) {
            BcSdk.getPermissionListener().onPermissionResult(grantList, deniedList, goSettings);
        }
        finish();
    }

    private void finishActivityForAllFileAccess(boolean grant) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (BcSdk.getPermissionListener() != null) {
            BcSdk.getPermissionListener().onAllFilesAccessResult(grant);
        }
        finish();
    }

    private void finishActivityForOverlayDraw() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        boolean grant = Utils.canDrawOverlays(this);
        if (BcSdk.getPermissionListener() != null) {
            BcSdk.getPermissionListener().onOverlayDrawResult(grant);
        }
        finish();
    }

    private void finishActivityForUsageAccess() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        boolean grant = Utils.isAccessUsageEnable(this);
        if (BcSdk.getPermissionListener() != null) {
            BcSdk.getPermissionListener().onUsageAccessResult(grant);
        }
        finish();
    }

    private void finishActivityForNotificationListener() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        boolean grant = Utils.notificationListenersEnable(this);
        if (BcSdk.getPermissionListener() != null) {
            BcSdk.getPermissionListener().onNotificationListenerResult(grant);
        }
        finish();
    }

    private void updatePermissionContent(TextView textView) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (textView == null) {
            return;
        }
        String label = null;
        try {
            PackageManager pm = getPackageManager();
            label = pm.getApplicationInfo(getPackageName(), 0).loadLabel(pm).toString();
        } catch (Exception | Error e) {
        }
        String content = Utils.getStringValue(this, "bsc_permission_content", label);
        SpannableStringBuilder spanBuilder = null;
        try {
            int index = content.indexOf(label);
            int len = label.length();
            spanBuilder = new SpannableStringBuilder(content);
            spanBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
        }
        if (spanBuilder != null) {
            textView.setText(spanBuilder);
        } else {
            textView.setText(content);
        }
    }

    private View generateDialogView(Context context, final List<String> grantList, final List<String> deniedList) {
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setBackgroundColor(Color.WHITE);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(context);
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        titleLayoutParams.topMargin = Utils.dp2px(context, 14);
        titleLayoutParams.bottomMargin = Utils.dp2px(context, 14);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            titleLayoutParams.setMarginStart(Utils.dp2px(context, 24));
            titleLayoutParams.setMarginEnd(Utils.dp2px(context, 24));
        } else {
            titleLayoutParams.leftMargin = Utils.dp2px(context, 24);
            titleLayoutParams.rightMargin = Utils.dp2px(context, 24);
        }
        rootLayout.addView(titleView, titleLayoutParams);
        titleView.setText(Utils.getStringValue(this, "bsc_permission_title"));
        titleView.setTextColor(Color.BLACK);
        titleView.setTextSize(18);

        TextView descView = new TextView(context);
        LinearLayout.LayoutParams descLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            descLayoutParams.setMarginStart(Utils.dp2px(context, 24));
            descLayoutParams.setMarginEnd(Utils.dp2px(context, 24));
        } else {
            descLayoutParams.leftMargin = Utils.dp2px(context, 24);
            descLayoutParams.rightMargin = Utils.dp2px(context, 24);
        }
        rootLayout.addView(descView, descLayoutParams);
        updatePermissionContent(descView);
        descView.setTextColor(Color.BLACK);
        descView.setTextSize(16);

        LinearLayout buttonLayout = new LinearLayout(context);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        buttonLayoutParams.topMargin = Utils.dp2px(context, 14);
        buttonLayoutParams.bottomMargin = Utils.dp2px(context, 8);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            buttonLayoutParams.setMarginStart(Utils.dp2px(context, 12));
            buttonLayoutParams.setMarginEnd(Utils.dp2px(context, 12));
        } else {
            buttonLayoutParams.leftMargin = Utils.dp2px(context, 12);
            buttonLayoutParams.rightMargin = Utils.dp2px(context, 12);
        }
        rootLayout.addView(buttonLayout, buttonLayoutParams);
        buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setWeightSum(3.5f);

        TextView cancelButton = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Utils.dp2px(context, 42));
        params.weight = 1;
        buttonLayout.addView(cancelButton, params);
        cancelButton.setText(Utils.getStringValue(this, "bsc_cancel"));
        cancelButton.setBackgroundResource(android.R.drawable.list_selector_background);
        cancelButton.setTextColor(Color.DKGRAY);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize());
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityForPermission(grantList, deniedList);
                InternalStat.reportEvent(getBaseContext(), "bs_click_cancel", null, null);
            }
        });

        TextView settingsButton = new TextView(context);
        buttonLayout.addView(settingsButton, params);
        settingsButton.setText(Utils.getStringValue(this, "bsc_settings"));
        settingsButton.setBackgroundResource(android.R.drawable.list_selector_background);
        settingsButton.setTextColor(getTextColor());
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize());
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                try {
                    startActivityForResult(intent, REQUEST_CODE_GOTO_SETTINGS);
                } catch (Exception e) {
                    finishActivityForPermission(grantList, deniedList, true);
                    InternalStat.reportEvent(getBaseContext(), "bs_click_settings", null, null);
                }
            }
        });

        return rootLayout;
    }

    private void checkPermissionFromSettings() {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        List<String> grantList = null;
        List<String> deniedList = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mRuntimePermissions != null && !mRuntimePermissions.isEmpty()) {
                for (String s : mRuntimePermissions) {
                    int status = checkSelfPermission(s);
                    if (status == PackageManager.PERMISSION_GRANTED) {
                        if (grantList == null) {
                            grantList = new ArrayList<>();
                        }
                        grantList.add(s);
                    } else if (status == PackageManager.PERMISSION_DENIED) {
                        if (deniedList == null) {
                            deniedList = new ArrayList<>();
                        }
                        deniedList.add(s);
                    }
                }
            }
        }
        finishActivityForPermission(grantList, deniedList);
    }

    private void showDialogForUser(final List<String> grantList, final List<String> deniedList) {
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        Dialog dialog = new Dialog(this);
        dialog.setContentView(generateDialogView(this, grantList, deniedList));
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85f);
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private int getTextColor() {
        try {
            TypedArray a = obtainStyledAttributes(android.R.style.Theme_DeviceDefault, new int[]{
                    android.R.attr.colorAccent
            });
            int color = a.getColor(0, Color.BLACK);
            return color;
        } catch (Exception e) {
        }
        return Color.BLACK;
    }

    private float getTextSize() {
        try {
            TypedArray a = obtainStyledAttributes(android.R.style.Theme_DeviceDefault, new int[]{
                    android.R.attr.textSize
            });
            float textSize = a.getDimension(0, Utils.dp2px(this, 16));
            return textSize;
        } catch (Exception e) {
        }
        return Utils.dp2px(this, 16);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (mRequestAllFileAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            finishActivityForAllFileAccess(Environment.isExternalStorageManager());
        } else if (mRequestOverlayDraw) {
            finishActivityForOverlayDraw();
        } else if (mRequestUsageAccess) {
            finishActivityForUsageAccess();
        } else if (mRequestNotificationListener) {
            finishActivityForNotificationListener();
        } else {
            checkPermissionFromSettings();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG_LOG) {
            Log.iv(Log.TAG, "");
        }
        if (mHandler != null && mCheckRunnable != null) {
            mHandler.removeCallbacks(mCheckRunnable);
        }
    }

    private void checkAllFileAccessRunnable() {
        boolean isGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
        if (mHandler != null && !isGranted) {
            mHandler.postDelayed(mCheckRunnable, 500);
        } else {
            try {
                Intent intent = new Intent(getApplicationContext(), PermUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    }

    private void checkOverlayDrawRunnable() {
        boolean isGranted = Utils.canDrawOverlays(getApplicationContext());
        if (mHandler != null && !isGranted) {
            mHandler.postDelayed(mCheckRunnable, 500);
        } else {
            try {
                Intent intent = new Intent(getApplicationContext(), PermUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    }

    private void checkUsageAccessRunnable() {
        boolean isGranted = Utils.isAccessUsageEnable(getApplicationContext());
        if (mHandler != null && !isGranted) {
            mHandler.postDelayed(mCheckRunnable, 500);
        } else {
            try {
                Intent intent = new Intent(getApplicationContext(), PermUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    }

    private void checkNotificationListenerRunnable() {
        boolean isGranted = Utils.notificationListenersEnable(getApplicationContext());
        if (mHandler != null && !isGranted) {
            mHandler.postDelayed(mCheckRunnable, 500);
        } else {
            try {
                Intent intent = new Intent(getApplicationContext(), PermUI.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    }

    class CheckRunnable implements Runnable {
        private int mRequestCode = -1;

        public CheckRunnable(int requestCode) {
            mRequestCode = requestCode;
        }

        @Override
        public void run() {
            if (mRequestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
                checkAllFileAccessRunnable();
            } else if (mRequestCode == REQUEST_CODE_OVERLAY_DRAW) {
                checkOverlayDrawRunnable();
            } else if (mRequestCode == REQUEST_CODE_USAGE_ACCESS) {
                checkUsageAccessRunnable();
            } else if (mRequestCode == REQUEST_CODE_NOTIFICATION_LISTENER) {
                checkNotificationListenerRunnable();
            }
        }
    }

    private CheckRunnable mCheckRunnable;

    private void checkAllFileAccess() {
        if (mAutoCheck) {
            mCheckRunnable = new CheckRunnable(REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
            mHandler.postDelayed(mCheckRunnable, 1000);
        }
    }

    private void checkOverlayDraw() {
        if (mAutoCheck) {
            mCheckRunnable = new CheckRunnable(REQUEST_CODE_OVERLAY_DRAW);
            mHandler.postDelayed(mCheckRunnable, 1000);
        }
    }

    private void checkUsageAccess() {
        if (mAutoCheck) {
            mCheckRunnable = new CheckRunnable(REQUEST_CODE_USAGE_ACCESS);
            mHandler.postDelayed(mCheckRunnable, 1000);
        }
    }

    private void checkNotificationListener() {
        if (mAutoCheck) {
            mCheckRunnable = new CheckRunnable(REQUEST_CODE_NOTIFICATION_LISTENER);
            mHandler.postDelayed(mCheckRunnable, 1000);
        }
    }
}
