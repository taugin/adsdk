package com.hauyu.adsdk.scloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.common.BaseLoader;
import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.scconfig.StConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.scpolicy.StPolicy;
import com.hauyu.adsdk.stat.EventImpl;

/**
 * Created by Administrator on 2018/7/19.
 */

public class StAdLoader extends BaseLoader implements Handler.Callback {

    public static final String STPLACE_OUTER_NAME = "st_outer_place";
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

    @Override
    protected Context getContext() {
        return mContext;
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
            Log.iv(Log.TAG, "");
            EventImpl.get().reportAdOuterRequest(mContext, StPolicy.get(mContext).getType(), STPLACE_OUTER_NAME);
            mAdSdk.loadComplexAds(STPLACE_OUTER_NAME, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    EventImpl.get().reportAdOuterLoaded(mContext, StPolicy.get(mContext).getType(), pidName);
                    if (StPolicy.get(mContext).isStAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (StPolicy.get(mContext).isShowBottomActivity()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType);
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                            }
                        }
                        EventImpl.get().reportAdOuterCallShow(mContext, StPolicy.get(mContext).getType(), pidName);
                    } else {
                        EventImpl.get().reportAdOuterDisallow(mContext, StPolicy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    StPolicy.get(mContext).reportShowing(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && StPolicy.get(mContext).isShowBottomActivity()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onShow(String pidName, String source, String adType) {
                    StPolicy.get(mContext).reportShowing(true);
                    EventImpl.get().reportAdOuterShowing(mContext, StPolicy.get(mContext).getType(), pidName);
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
            AdReceiver.get(context).onReceive(context, intent);
        }
    };
}
