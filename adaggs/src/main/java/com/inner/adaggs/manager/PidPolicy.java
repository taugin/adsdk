package com.inner.adaggs.manager;

import android.content.Context;

/**
 * Created by Administrator on 2018/3/27.
 */

public class PidPolicy {
    private static PidPolicy sPidPolicy;

    public static PidPolicy get(Context context) {
        synchronized (PidPolicy.class) {
            if (sPidPolicy == null) {
                createInstance(context);
            }
        }
        return sPidPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (PidPolicy.class) {
            if (sPidPolicy == null) {
                sPidPolicy = new PidPolicy(context);
            }
        }
    }

    private PidPolicy(Context context) {
        mContext = context;
    }

    private Context mContext;

    public boolean allowLoad() {
        return true;
    }
}
