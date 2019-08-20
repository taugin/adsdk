package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.scconfig.LvCg;
import com.bacad.ioc.gsb.common.BasePolicy;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class LvPcy extends BasePolicy {
    private static LvPcy sLvPcy;

    public static LvPcy get(Context context) {
        synchronized (LvPcy.class) {
            if (sLvPcy == null) {
                createInstance(context);
            }
        }
        return sLvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (LvPcy.class) {
            if (sLvPcy == null) {
                sLvPcy = new LvPcy(context);
            }
        }
    }

    private LvPcy(Context context) {
        super(context, "lt");
    }

    private LvCg mLvCg;

    public void init() {
    }

    public void setPolicy(LvCg lvCg) {
        super.setPolicy(lvCg);
        mLvCg = lvCg;
    }

    public boolean isLtAllowed() {
        Log.iv(Log.TAG, "l_value : " + mLvCg);
        if (!checkBaseConfig()) {
            return false;
        }
        return true;
    }
}
