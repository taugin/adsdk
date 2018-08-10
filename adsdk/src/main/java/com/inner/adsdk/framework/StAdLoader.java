package com.inner.adsdk.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.inner.adsdk.AdSdk;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.StConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.policy.StPolicy;

/**
 * Created by Administrator on 2018/7/19.
 */

public class StAdLoader implements Handler.Callback {

    private static final int LOAD_DELAY = 1000;
    private static final int MSG_ST_LOAD = 1000;

    private static StAdLoader sStAdLoader;

    public static StAdLoader get(Context context) {
        if (sStAdLoader == null) {
            create(context);
        }
        return sStAdLoader;
    }

    private static void create(Context context) {
        synchronized (StAdLoader.class) {
            if (sStAdLoader == null) {
                sStAdLoader = new StAdLoader(context);
            }
        }
    }

    private Context mContext;
    private AdSdk mAdSdk;
    private Handler mHandler = null;

    private StAdLoader(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public void init(AdSdk adSdk) {
        mAdSdk = adSdk;
        register();
    }

    private void register() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            filter.addDataScheme("package");
            mContext.registerReceiver(mPackageReceiver, filter);
        } catch (Exception e) {
        }
    }

    private void updateSTPolicy() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        if (adConfig == null) {
            return;
        }
        StConfig stConfig = DataManager.get(mContext).getRemoteStPolicy();
        if (stConfig == null && adConfig != null) {
            stConfig = adConfig.getStConfig();
        }
        StPolicy.get(mContext).setPolicy(stConfig);
    }

    @Override
    public boolean handleMessage(Message msg) {
        fireInstAd();
        return true;
    }

    private void fireInstAd() {
        if (mAdSdk != null) {
            updateSTPolicy();
            if (!StPolicy.get(mContext).isStAllowed()) {
                return;
            }
            Log.v(Log.TAG, "");
            mAdSdk.loadComplexAds(Constant.STPLACE_OUTER_NAME, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    if (StPolicy.get(mContext).isStAllowed()) {
                        mAdSdk.showComplexAds(pidName, null);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    StPolicy.get(mContext).reportStShowing(false);
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    StPolicy.get(mContext).reportStShowing(true);
                }
            });
        }
    }

    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mHandler.hasMessages(MSG_ST_LOAD)) {
                mHandler.sendEmptyMessageDelayed(MSG_ST_LOAD, LOAD_DELAY);
            }
        }
    };
}
