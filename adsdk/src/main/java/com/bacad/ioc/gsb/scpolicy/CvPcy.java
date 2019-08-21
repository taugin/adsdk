package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.common.BasePolicy;
import com.bacad.ioc.gsb.scconfig.CvCg;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class CvPcy extends BasePolicy {

    private static final String PREF_CHARGE_MONITOR = "pref_ch_disable";
    private static CvPcy sCvPcy;

    public static CvPcy get(Context context) {
        synchronized (CvPcy.class) {
            if (sCvPcy == null) {
                createInstance(context);
            }
        }
        return sCvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (CvPcy.class) {
            if (sCvPcy == null) {
                sCvPcy = new CvPcy(context);
            }
        }
    }

    private CvPcy(Context context) {
        super(context, "ct");
    }

    private CvCg mCvCg;

    public void init() {
    }

    public void setPolicy(CvCg cvCg) {
        super.setPolicy(cvCg);
        mCvCg = cvCg;
    }

    public void disableMonitor() {
        Utils.putLong(mContext, PREF_CHARGE_MONITOR, System.currentTimeMillis());
    }

    public boolean allowDisableMonitor() {
        if (mCvCg != null) {
            return mCvCg.getDisableInterval() > 0;
        }
        return false;
    }

    private boolean isDisable() {
        if (mCvCg != null) {
            long now = System.currentTimeMillis();
            long dis = Utils.getLong(mContext, PREF_CHARGE_MONITOR);
            long inv = mCvCg.getDisableInterval();
            return now - dis < inv;
        }
        return false;
    }

    public boolean isCtAllowed() {
        Log.iv(Log.TAG, "c_value : " + mCvCg);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isDisable()) {
            Log.iv(Log.TAG, "user close the function");
            return false;
        }

        return true;
    }
}
