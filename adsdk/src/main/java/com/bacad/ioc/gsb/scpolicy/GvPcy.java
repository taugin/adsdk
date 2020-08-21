package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.base.BPcy;
import com.bacad.ioc.gsb.scconfig.GvCg;
import com.hauyu.adsdk.core.AttrHelper;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2018/3/19.
 */

public class GvPcy extends BPcy {
    private static GvPcy sGvPcy;

    public static GvPcy get(Context context) {
        synchronized (GvPcy.class) {
            if (sGvPcy == null) {
                createInstance(context);
            }
        }
        return sGvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (GvPcy.class) {
            if (sGvPcy == null) {
                sGvPcy = new GvPcy(context);
            }
        }
    }

    private GvPcy(Context context) {
        super(context, "gt");
        mAttrHelper = new AttrHelper(context);
    }

    private GvCg mGvCg;

    public void init() {
    }

    public void setPolicy(GvCg gvCg) {
        super.setPolicy(gvCg);
        mGvCg = gvCg;
    }

    public boolean isGtAllowed() {
        Log.iv(Log.TAG, "g_value : " + mGvCg);
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
