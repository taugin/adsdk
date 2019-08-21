package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.common.BPcy;
import com.bacad.ioc.gsb.scconfig.HvCg;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HvPcy extends BPcy {
    private static HvPcy sHvPcy;

    public static HvPcy get(Context context) {
        synchronized (HvPcy.class) {
            if (sHvPcy == null) {
                createInstance(context);
            }
        }
        return sHvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (HvPcy.class) {
            if (sHvPcy == null) {
                sHvPcy = new HvPcy(context);
            }
        }
    }

    private HvCg mHvCg;

    private HvPcy(Context context) {
        super(context, "ht");
    }

    public void init() {
    }

    public void setPolicy(HvCg hvCg) {
        super.setPolicy(hvCg);
        mHvCg = hvCg;
    }

    public boolean isHtAllowed() {
        Log.iv(Log.TAG, "h_value : " + mHvCg);
        if (!checkBaseConfig()) {
            return false;
        }
        return true;
    }
}