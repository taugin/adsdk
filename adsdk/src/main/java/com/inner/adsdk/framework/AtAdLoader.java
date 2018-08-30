package com.inner.adsdk.framework;

import android.content.Context;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.AtPolicy;
import com.inner.adsdk.utils.TaskUtils;

/**
 * Created by Administrator on 2018/7/19.
 */

public class AtAdLoader implements TaskMonitor.OnTaskMonitorListener {

    private static AtAdLoader sAtAdLoader;

    public static AtAdLoader get(Context context) {
        if (sAtAdLoader == null) {
            create(context);
        }
        return sAtAdLoader;
    }

    private static void create(Context context) {
        synchronized (AtAdLoader.class) {
            if (sAtAdLoader == null) {
                sAtAdLoader = new AtAdLoader(context);
            }
        }
    }

    private Context mContext;
    private AdSdk mAdSdk;

    private AtAdLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public void init(AdSdk adsdk) {
        mAdSdk = adsdk;
        TaskMonitor.get(mContext).setOnTaskMonitorListener(this);
    }

    private void updateAtPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        AtConfig atConfig = DataManager.get(mContext).getRemoteAtPolicy();
        if (atConfig == null && adConfig != null) {
            atConfig = adConfig.getAtConfig();
        }
        AtPolicy.get(mContext).setPolicy(atConfig);
    }

    public void resumeLoader() {
        Log.v(Log.TAG, "resume loader");
        if (mAdSdk.isInterstitialLoaded(Constant.ATPLACE_OUTER_NAME)) {
            TaskMonitor.get(mContext).startMonitor();
        } else {
            Log.v(Log.TAG, "miss app usage");
        }
    }

    public void onFire() {
        if (TaskUtils.hasAppUsagePermission(mContext)) {
            updateAtPolicy();
            if (AtPolicy.get(mContext).isAtAllowed()) {
                mAdSdk.loadInterstitial(Constant.ATPLACE_OUTER_NAME, mAdSdkListener);
                TaskMonitor.get(mContext).startMonitor();
            }
        } else {
            Log.v(Log.TAG, "miss app usage");
        }
    }

    @Override
    public void onAppSwitch(String pkgname, String className) {
        Log.d(Log.TAG, "app switch pkgname : " + pkgname + " , className : " + className);
        updateAtPolicy();
        if (AtPolicy.get(mContext).isAtAllowed() && !AtPolicy.get(mContext).isInWhiteList(pkgname, className)) {
            if (AtPolicy.get(mContext).isShowOnFirstPage()) {
                if (mAdSdk.isInterstitialLoaded(Constant.ATPLACE_OUTER_NAME)) {
                    mAdSdk.showInterstitial(Constant.ATPLACE_OUTER_NAME);
                } else {
                    mAdSdk.loadInterstitial(Constant.ATPLACE_OUTER_NAME, mAdSdkListener);
                }
            } else {
                if (!mAdSdk.isInterstitialLoaded(Constant.ATPLACE_OUTER_NAME)) {
                    mAdSdk.loadInterstitial(Constant.ATPLACE_OUTER_NAME, mAdSdkListener);
                }
            }
        }
    }

    @Override
    public void onActivitySwitch(String pkgname, String oldActivity, String newActivity) {
        Log.d(Log.TAG, "activity switch pkgname : " + pkgname + " , oldActivity : " + oldActivity + " , newActivity : " + newActivity);
        updateAtPolicy();
        if (AtPolicy.get(mContext).isAtAllowed() && !AtPolicy.get(mContext).isInWhiteList(pkgname, newActivity)) {
            if (mAdSdk.isInterstitialLoaded(Constant.ATPLACE_OUTER_NAME)) {
                mAdSdk.showInterstitial(Constant.ATPLACE_OUTER_NAME);
            } else {
                mAdSdk.loadInterstitial(Constant.ATPLACE_OUTER_NAME, mAdSdkListener);
            }
        }
    }

    private SimpleAdSdkListener mAdSdkListener = new SimpleAdSdkListener() {
        @Override
        public void onShow(String pidName, String source, String adType) {
            Log.v(Log.TAG, "show ads and stop task monitor");
            AtPolicy.get(mContext).reportAtShow();
            TaskMonitor.get(mContext).stopMonitor();
        }
    };
}
