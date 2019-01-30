package com.inner.adsdk.loader;

import android.content.Context;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AdReceiver {

    private static AdReceiver sAdReceiver;

    private Context mContext;

    private AdReceiver(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AdReceiver get(Context context) {
        if (sAdReceiver == null) {
            create(context);
        }
        return sAdReceiver;
    }

    private static void create(Context context) {
        synchronized (AdReceiver.class) {
            if (sAdReceiver == null) {
                sAdReceiver = new AdReceiver(context);
            }
        }
    }

    public void init() {
        reportFirstStartUpTime();
    }

    /**
     * 记录应用首次启动时间
     */
    private void reportFirstStartUpTime() {
        if (Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0) <= 0) {
            Utils.putLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, System.currentTimeMillis());
        }
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    public long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
    }
}
