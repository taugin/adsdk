package com.bacad.ioc.gsb.scloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.BaseLoader;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.bacad.ioc.gsb.scpolicy.StPolicy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;

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

    public void init() {
        mAdSdk = AdSdk.get(mContext);
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
        StPolicy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteStPolicy());
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
            SceneEventImpl.get().reportAdOuterRequest(mContext, StPolicy.get(mContext).getType(), STPLACE_OUTER_NAME);
            mAdSdk.loadComplexAds(STPLACE_OUTER_NAME, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    SceneEventImpl.get().reportAdOuterLoaded(mContext, StPolicy.get(mContext).getType(), pidName);
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
                        SceneEventImpl.get().reportAdOuterCallShow(mContext, StPolicy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdOuterDisallow(mContext, StPolicy.get(mContext).getType(), pidName);
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
                    SceneEventImpl.get().reportAdOuterShowing(mContext, StPolicy.get(mContext).getType(), pidName);
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
