package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;

import com.bacad.ioc.gsb.scconfig.LtConfig;
import com.bacad.ioc.gsb.common.BasePolicy;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class LtPolicy extends BasePolicy {
    private static LtPolicy sLtPolicy;

    public static LtPolicy get(Context context) {
        synchronized (LtPolicy.class) {
            if (sLtPolicy == null) {
                createInstance(context);
            }
        }
        return sLtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (LtPolicy.class) {
            if (sLtPolicy == null) {
                sLtPolicy = new LtPolicy(context);
            }
        }
    }

    private LtPolicy(Context context) {
        super(context, "lt");
    }

    private LtConfig mLtConfig;

    public void init() {
    }

    public void setPolicy(LtConfig ltConfig) {
        super.setPolicy(ltConfig);
        mLtConfig = ltConfig;
    }

    public boolean isLtAllowed() {
        Log.iv(Log.TAG, "l_value : " + mLtConfig);
        if (!checkBaseConfig()) {
            return false;
        }
        return true;
    }
}
