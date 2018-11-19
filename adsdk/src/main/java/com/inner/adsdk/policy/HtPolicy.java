package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.config.HtConfig;
import com.inner.adsdk.log.Log;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HtPolicy extends BasePolicy {
    private static HtPolicy sHtPolicy;

    public static HtPolicy get(Context context) {
        synchronized (HtPolicy.class) {
            if (sHtPolicy == null) {
                createInstance(context);
            }
        }
        return sHtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (HtPolicy.class) {
            if (sHtPolicy == null) {
                sHtPolicy = new HtPolicy(context);
            }
        }
    }

    private HtConfig mHtConfig;

    private HtPolicy(Context context) {
        super(context, "ht");
    }

    public void init() {
    }

    public void setPolicy(HtConfig htConfig) {
        super.setPolicy(htConfig);
        mHtConfig = htConfig;
    }

    public boolean isHtAllowed() {
        Log.v(Log.TAG, "ht : " + mHtConfig);
        if (!checkBaseConfig()) {
            return false;
        }
        return true;
    }
}
