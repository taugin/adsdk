package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.content.Context;

public class BaseActivity extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ChangeLanguage.createConfigurationContext(newBase));
    }
}
