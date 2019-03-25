package com.inner.adsdk.loader;

import android.content.Context;
import android.text.TextUtils;

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

public class AtAdLoader extends BottomLoader implements TaskMonitor.OnTaskMonitorListener {

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
    private String mPidName;
    private String mSource;
    private String mAdType;

    private AtAdLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    public void init(AdSdk adsdk) {
        mAdSdk = adsdk;
        TaskMonitor.get(mContext).setOnTaskMonitorListener(this);
    }

    @Override
    protected Context getContext() {
        return mContext;
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
        Log.pv(Log.TAG, "resume loader");
        if (mAdSdk.isInterstitialLoaded(Constant.ATPLACE_OUTER_NAME)) {
            TaskMonitor.get(mContext).startMonitor();
        } else {
            Log.pv(Log.TAG, "miss app usage");
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
            Log.pv(Log.TAG, "miss app usage");
        }
    }

    @Override
    public void onAppSwitch(String pkgname, String className) {
        Log.d(Log.TAG, "app switch pkgname : " + pkgname + " , className : " + className);
        updateAtPolicy();
        if (AtPolicy.get(mContext).isAtAllowed() && !AtPolicy.get(mContext).isInWhiteList(pkgname, className)) {
            if (AtPolicy.get(mContext).isShowOnFirstPage()) {
                if (mAdSdk.isComplexAdsLoaded(Constant.ATPLACE_OUTER_NAME)) {
                    showAds();
                } else {
                    mAdSdk.loadComplexAds(Constant.ATPLACE_OUTER_NAME, generateAdParams(), mAdSdkListener);
                }
            } else {
                if (!mAdSdk.isComplexAdsLoaded(Constant.ATPLACE_OUTER_NAME)) {
                    mAdSdk.loadComplexAds(Constant.ATPLACE_OUTER_NAME, generateAdParams(), mAdSdkListener);
                }
            }
        }
    }

    @Override
    public void onActivitySwitch(String pkgname, String oldActivity, String newActivity) {
        Log.d(Log.TAG, "activity switch pkgname : " + pkgname + " , oldActivity : " + oldActivity + " , newActivity : " + newActivity);
        updateAtPolicy();
        if (AtPolicy.get(mContext).isAtAllowed() && !AtPolicy.get(mContext).isInWhiteList(pkgname, newActivity)) {
            if (mAdSdk.isComplexAdsLoaded(Constant.ATPLACE_OUTER_NAME)) {
                showAds();
            } else {
                mAdSdk.loadComplexAds(Constant.ATPLACE_OUTER_NAME, generateAdParams(), mAdSdkListener);
            }
        }
    }

    private SimpleAdSdkListener mAdSdkListener = new SimpleAdSdkListener() {
        @Override
        public void onLoaded(String pidName, String source, String adType) {
            Log.pv(Log.TAG, "at loaded");
            mPidName = pidName;
            mSource = source;
            mAdType = adType;
        }

        @Override
        public void onShow(String pidName, String source, String adType) {
            Log.pv(Log.TAG, "show ads and stop task monitor");
            AtPolicy.get(mContext).reportShowing(true);
            TaskMonitor.get(mContext).stopMonitor();
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            Log.pv(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
            AtPolicy.get(mContext).reportShowing(false);
            if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                    && AtPolicy.get(mContext).isShowBottomActivity()
                    && !Constant.TYPE_BANNER.equals(adType)
                    && !Constant.TYPE_NATIVE.equals(adType)) {
                hide();
            }
        }
    };

    private void showAds() {
        if (TextUtils.equals(mSource, Constant.AD_SDK_SPREAD)) {
            AdSdk.get(mContext).showComplexAds(mPidName, null);
        } else {
            if (AtPolicy.get(mContext).isShowBottomActivity()
                    || Constant.TYPE_BANNER.equals(mAdType)
                    || Constant.TYPE_NATIVE.equals(mAdType)) {
                show(mPidName, mSource, mAdType);
            } else {
                AdSdk.get(mContext).showComplexAds(mPidName, null);
            }
        }
    }
}
