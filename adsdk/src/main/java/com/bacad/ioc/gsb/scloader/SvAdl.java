package com.bacad.ioc.gsb.scloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.Bldr;
import com.bacad.ioc.gsb.common.CSvr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.event.SceneEventImpl;
import com.bacad.ioc.gsb.scpolicy.SvPcy;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;

/**
 * Created by Administrator on 2018/7/19.
 */

public class SvAdl extends Bldr implements Handler.Callback {

    private static final int LOAD_DELAY = 1000;
    private static final int MSG_ST_LOAD = 1000;

    private static SvAdl sSvAdl;

    public static SvAdl get(Context context) {
        if (sSvAdl == null) {
            create(context);
        }
        return sSvAdl;
    }

    private static void create(Context context) {
        synchronized (SvAdl.class) {
            if (sSvAdl == null) {
                sSvAdl = new SvAdl(context);
            }
        }
    }

    private Context mContext;
    private AdSdk mAdSdk;
    private Handler mHandler = null;

    private SvAdl(Context context) {
        super(SvPcy.get(context));
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
        SvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteStPolicy());
    }

    @Override
    public boolean handleMessage(Message msg) {
        fireInstAd();
        return true;
    }

    private void fireInstAd() {
        if (mAdSdk != null) {
            updateSTPolicy();
            if (!SvPcy.get(mContext).isStAllowed()) {
                return;
            }
            String placeName = getPlaceNameAdv();
            if (TextUtils.isEmpty(placeName)) {
                Log.iv(Log.TAG, getType() + " not found place name");
                return;
            }
            Log.iv(Log.TAG, "");
            SceneEventImpl.get().reportAdSceneRequest(mContext, SvPcy.get(mContext).getType(), placeName);
            mAdSdk.loadComplexAds(placeName, new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    SceneEventImpl.get().reportAdSceneLoaded(mContext, SvPcy.get(mContext).getType(), pidName);
                    if (SvPcy.get(mContext).isStAllowed()) {
                        if (TextUtils.equals(source, Constant.AD_SDK_SPREAD)) {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        } else {
                            if (SvPcy.get(mContext).isShowBottom()
                                    || Constant.TYPE_BANNER.equals(adType)
                                    || Constant.TYPE_NATIVE.equals(adType)) {
                                show(pidName, source, adType, SvPcy.get(mContext).getType());
                            } else {
                                AdSdk.get(mContext).showComplexAds(pidName, null);
                            }
                        }
                        SceneEventImpl.get().reportAdSceneShow(mContext, SvPcy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdSceneDisallow(mContext, SvPcy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType) {
                    SvPcy.get(mContext).reportShowing(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && SvPcy.get(mContext).isShowBottom()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onImp(String pidName, String source, String adType) {
                    SvPcy.get(mContext).reportShowing(true);
                    SceneEventImpl.get().reportAdSceneImp(mContext, SvPcy.get(mContext).getType(), pidName);
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
            CSvr.get(context).onReceive(context, intent);
        }
    };
}
