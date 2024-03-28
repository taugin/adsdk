package com.hauyu.adsdk.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;


import com.github.bcsdk.BcSdk;
import com.github.bcsdk.OnPermissionListener;

import java.util.ArrayList;
import java.util.List;

public class BcSdkActivity extends BaseActivity {

    private boolean mAutoCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printIntent();
        setContentView(R.layout.activity_bcsdk);
    }

    private void runOnUIThread(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    new AlertDialog.Builder(BcSdkActivity.this).setMessage(msg).create().show();
                }
            }
        });
    }

    private void printIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Log.d(Log.TAG, "key : " + key + " , value : " + bundle.get(key));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckBox checkBox = findViewById(R.id.notification_listener);
        checkBox.setChecked(BcSdk.isNotificationListenerEnabled(this));
    }

    public void onClick(View v) {
        if (v.getId() == R.id.notification_listener) {
            BcSdk.requestNotificationListener(this, new OnPermissionListener() {
                @Override
                public void onNotificationListenerResult(boolean grant) {
                    Toast.makeText(getApplicationContext(), "notification listener : " + grant, Toast.LENGTH_SHORT).show();
                }
            }, mAutoCheck);
        } else if (v.getId() == R.id.enter_usage) {
            BcSdk.requestUsageAccess(this, new OnPermissionListener() {
                @Override
                public void onUsageAccessResult(boolean grant) {
                    Toast.makeText(getApplicationContext(), "usage access : " + grant, Toast.LENGTH_SHORT).show();
                }
            }, mAutoCheck);
        } else if (v.getId() == R.id.enter_overdraw) {
            BcSdk.requestOverlayDraw(this, new OnPermissionListener() {
                @Override
                public void onOverlayDrawResult(boolean grant) {
                    Toast.makeText(getApplicationContext(), "overlay draw : " + grant, Toast.LENGTH_SHORT).show();
                }
            }, mAutoCheck);
        } else if (v.getId() == R.id.enter_permission) {
            List<String> list = new ArrayList<String>();
            list.add(Manifest.permission.READ_CONTACTS);
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            list.add(Manifest.permission.CALL_PHONE);
            BcSdk.setOnPermissionListener(new OnPermissionListener() {
                @Override
                public void onPermissionResult(List<String> grantList, List<String> deniedList, boolean goSettings) {
                    Toast.makeText(getApplicationContext(), "grantList : " + grantList + " , deniedList : " + deniedList, Toast.LENGTH_SHORT).show();
                }
            });
            BcSdk.requestPermissions(this, list, true);
        } else if (v.getId() == R.id.all_files_access) {
            BcSdk.requestAllFileAccess(this, new OnPermissionListener() {
                @Override
                public void onAllFilesAccessResult(boolean grant) {
                    Toast.makeText(getApplicationContext(), "all file access grant : " + grant, Toast.LENGTH_SHORT).show();
                }
            }, mAutoCheck);
        }
    }
}
