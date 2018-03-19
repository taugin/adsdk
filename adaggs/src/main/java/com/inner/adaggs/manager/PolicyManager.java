package com.inner.adaggs.manager;

import android.content.Context;

import com.inner.adaggs.config.AdPolicy;

/**
 * Created by Administrator on 2018/3/19.
 */

public class PolicyManager {
    private static PolicyManager sPolicyManager;

    public static PolicyManager get(Context context) {
        synchronized (PolicyManager.class) {
            if (sPolicyManager == null) {
                createInstance(context);
            }
        }
        return sPolicyManager;
    }

    private static void createInstance(Context context) {
        synchronized (PolicyManager.class) {
            if (sPolicyManager == null) {
                sPolicyManager = new PolicyManager(context);
            }
        }
    }

    private PolicyManager(Context context) {
        mContext = context;
    }

    private Context mContext;
    private AdPolicy mAdPolicy;
    private boolean mOuterShowing = false;

    public void setPolicy(AdPolicy adPolicy) {
        mAdPolicy = adPolicy;
    }

    public void setOuterShowing(boolean showing) {
        mOuterShowing = showing;
    }

    public boolean isOuterShowing() {
        return mOuterShowing;
    }
}
