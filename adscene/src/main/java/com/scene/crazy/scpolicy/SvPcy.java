package com.scene.crazy.scpolicy;

import android.content.Context;

import com.scene.crazy.base.BPcy;
import com.scene.crazy.scconfig.SvCg;
import com.scene.crazy.log.Log;
import com.rabbit.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018-8-10.
 */

public class SvPcy extends BPcy {
    private static SvPcy sSvPcy;

    public static SvPcy get(Context context) {
        synchronized (SvPcy.class) {
            if (sSvPcy == null) {
                createInstance(context);
            }
        }
        return sSvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (SvPcy.class) {
            if (sSvPcy == null) {
                sSvPcy = new SvPcy(context);
            }
        }
    }

    private SvPcy(Context context) {
        super(context, "st");
    }

    private SvCg mSvCg;

    public void init() {
    }

    public void setPolicy(SvCg svCg) {
        super.setPolicy(svCg);
        mSvCg = svCg;
    }

    public boolean isStAllowed() {
        Log.iv(Log.TAG, "s_value : " + mSvCg);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            Log.iv(Log.TAG, "app is on top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.iv(Log.TAG, "app is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.iv(Log.TAG, "app is not on");
            return false;
        }
        return true;
    }
}
