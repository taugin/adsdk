package com.bcsdk.listener;

import android.content.Context;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/12.
 */

public abstract class OnDataListener {
    public void onReferrerResult(String status, String mediaSource, boolean fromClick) {
    }

    public void onReportEvent(Context context, String key, String value, Map<String, Object> map) {
    }
}
