package com.inner.adsdk.framework;

import android.content.Context;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.TtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.TtPolicy;
import com.inner.adsdk.utils.TaskUtils;

/**
 * Created by Administrator on 2018/7/19.
 */

public class FtTtAdLoader implements TaskMonitor.OnTaskMonitorListener {

    private static final int MSG_LOOP = 1;
    private static final int LOOP_DELAY = 500;

    private static FtTtAdLoader sFtTtAdLoader;

    public static FtTtAdLoader get(Context context) {
        if (sFtTtAdLoader == null) {
            create(context);
        }
        return sFtTtAdLoader;
    }

    private static void create(Context context) {
        synchronized (FtTtAdLoader.class) {
            if (sFtTtAdLoader == null) {
                sFtTtAdLoader = new FtTtAdLoader(context);
            }
        }
    }

    private Context mContext;
    private AdSdk mAdSdk;

    private FtTtAdLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public void init(AdSdk adsdk) {
        mAdSdk = adsdk;
        TaskMonitor.get(mContext).setOnTaskMonitorListener(this);
    }

    private void updateTtPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        TtConfig ttConfig = DataManager.get(mContext).getRemoteTtPolicy();
        if (ttConfig == null && adConfig != null) {
            ttConfig = adConfig.getTtConfig();
        }
        TtPolicy.get(mContext).setPolicy(ttConfig);
    }

    public void onFire() {
        if (TaskUtils.hasAppUsagePermission(mContext)) {
            updateTtPolicy();
            if (TtPolicy.get(mContext).isTtAllowed()) {
                mAdSdk.loadInterstitial(Constant.ADPLACE_TASK_NAME, mAdSdkListener);
                TaskMonitor.get(mContext).startMonitor();
            }
        } else {
            Log.v(Log.TAG, "miss app usage");
        }
    }

    @Override
    public void onAppSwitch(String pkgname, String className) {
        Log.d(Log.TAG, "app switch pkgname : " + pkgname + " , className : " + className);
        updateTtPolicy();
        if (TtPolicy.get(mContext).isTtAllowed()  && !TtPolicy.get(mContext).isInWhiteList(pkgname, className)) {
            if (mAdSdk.isInterstitialLoaded(Constant.ADPLACE_TASK_NAME)) {
                mAdSdk.showInterstitial(Constant.ADPLACE_TASK_NAME);
            } else {
                mAdSdk.loadInterstitial(Constant.ADPLACE_TASK_NAME, mAdSdkListener);
            }
        }
    }

    @Override
    public void onActivitySwitch(String pkgname, String oldActivity, String newActivity) {
        Log.d(Log.TAG, "activity switch pkgname : " + pkgname + " , oldActivity : " + oldActivity + " , newActivity : " + newActivity);
        updateTtPolicy();
        if (TtPolicy.get(mContext).isTtAllowed()  && !TtPolicy.get(mContext).isInWhiteList(pkgname, newActivity)) {
            if (mAdSdk.isInterstitialLoaded(Constant.ADPLACE_TASK_NAME)) {
                mAdSdk.showInterstitial(Constant.ADPLACE_TASK_NAME);
            } else {
                mAdSdk.loadInterstitial(Constant.ADPLACE_TASK_NAME, mAdSdkListener);
            }
        }
    }

    private SimpleAdSdkListener mAdSdkListener = new SimpleAdSdkListener() {
        @Override
        public void onShow(String pidName, String source, String adType) {
            Log.v(Log.TAG, "show ads and stop task monitor");
            TtPolicy.get(mContext).reportTtShow();
            TaskMonitor.get(mContext).stopMonitor();
        }
    };
}
