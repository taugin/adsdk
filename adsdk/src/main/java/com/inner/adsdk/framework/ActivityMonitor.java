package com.inner.adsdk.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.policy.CtrChecker;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/2/12.
 */

public class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    private static ActivityMonitor sActivityMonitor;
    private AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private AtomicBoolean mAtomicBoolean = new AtomicBoolean();

    private CtrChecker mCtrChecker = new CtrChecker();
    private PidConfig mPidConfg;

    public static ActivityMonitor get(Context context) {
        synchronized (ActivityMonitor.class) {
            if (sActivityMonitor == null) {
                createInstance(context);
            }
        }
        return sActivityMonitor;
    }

    private static void createInstance(Context context) {
        synchronized (ActivityMonitor.class) {
            if (sActivityMonitor == null) {
                sActivityMonitor = new ActivityMonitor(context);
            }
        }
    }

    private ActivityMonitor(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    private Context mContext;

    public void init() {
        try {
            if (mContext instanceof Application) {
                ((Application) mContext).registerActivityLifecycleCallbacks(this);
            }
        } catch(Exception e) {
        } catch(Error e) {
        }
    }

    public void setPidConfig(PidConfig pidConfig) {
        mPidConfg = pidConfig;
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        if (mCtrChecker != null && mPidConfg != null) {
            mCtrChecker.checkCTR(activity, mPidConfg);
            // 置空，防止拦截其他的Activity
            mPidConfg = null;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (mAtomicInteger != null) {
            mAtomicInteger.incrementAndGet();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mAtomicBoolean != null) {
            mAtomicBoolean.set(true);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mAtomicBoolean != null) {
            mAtomicBoolean.set(false);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mAtomicInteger != null) {
            mAtomicInteger.decrementAndGet();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public boolean appOnTop() {
        if (mAtomicInteger != null) {
            return mAtomicInteger.get() > 0;
        }
        return false;
    }

    public boolean appOnTopForReward() {
        if (mAtomicBoolean != null) {
            return mAtomicBoolean.get();
        }
        return false;
    }

    private boolean isLauncherActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Intent intent = activity.getIntent();
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        Set<String> categories = intent.getCategories();
        if (TextUtils.isEmpty(action) || categories == null || categories.isEmpty()) {
            return false;
        }
        return TextUtils.equals(action, Intent.ACTION_MAIN)
                && categories.contains(Intent.CATEGORY_LAUNCHER);
    }
}
