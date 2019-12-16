package com.hauyu.adsdk.adloader.spread;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.gekes.fvs.tdsvap.IAdvance;
import com.gekes.fvs.tdsvap.SpConfig;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.Params;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.http.Http;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private SpConfig mSpread;
    private Params mParams;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_SPREAD;
    }

    @Override
    public void loadNative(Params params) {
        mSpread = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(mSpread)) {
            reportAdLoaded();
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onAdLoaded(SpLoader.this);
            }
            printInterfaceLog(ACTION_LOAD);
            loadIcon(mSpread.getIcon());
            loadBanner(mSpread.getBanner());
        } else {
            reportAdError(String.valueOf("ERROR_LOAD"));
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
            }
        }
    }

    private void loadIcon(final String iconUrl) {
        Http.get(mContext).loadImage(iconUrl, null, null);
    }

    private void loadBanner(final String bannerUrl) {
        Http.get(mContext).loadImage(bannerUrl, null, null);
    }

    @Override
    public boolean isNativeLoaded() {
        return checkArgs(mSpread);
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        if (mSpread != null) {
            reportAdImp();
            SpConfig spConfig = mSpread;
            SpreadBindNativeView spreadBindNativeView = new SpreadBindNativeView();
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spConfig);
            if (getAdListener() != null) {
                getAdListener().onAdImp();
            }
        }
    }

    @Override
    public void loadInterstitial() {
        mSpread = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(mSpread)) {
            reportAdLoaded();
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded(this);
            }
            printInterfaceLog(ACTION_LOAD);
            loadIcon(mSpread.getIcon());
            loadBanner(mSpread.getBanner());
        } else {
            reportAdError(String.valueOf("ERROR_LOAD"));
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOAD);
            }
        }
    }

    /**
     * 检出有效的配置
     *
     * @param spList
     * @return
     */
    private SpConfig checkSpConfig(List<SpConfig> spList) {
        if (spList == null || spList.isEmpty()) {
            return null;
        }
        List<SpConfig> availableSp = new ArrayList<SpConfig>();
        for (SpConfig config : spList) {
            // 参数有效，并且未安装
            if (checkArgs(config) && !Utils.isInstalled(mContext, config.getPkgname()) && !config.isDisable()) {
                availableSp.add(config);
            }
        }
        if (availableSp != null && !availableSp.isEmpty()) {
            int size = availableSp.size();
            return availableSp.get(new Random(System.currentTimeMillis()).nextInt(size));
        }
        return null;
    }

    /**
     * 检查参数合法性
     *
     * @param spConfig
     * @return
     */
    private boolean checkArgs(SpConfig spConfig) {
        if (spConfig == null) {
            return false;
        }
        if (TextUtils.isEmpty(spConfig.getBanner())
                || TextUtils.isEmpty(spConfig.getIcon())
                || TextUtils.isEmpty(spConfig.getTitle())
                || TextUtils.isEmpty(spConfig.getPkgname())
                || TextUtils.isEmpty(spConfig.getDetail())
                || TextUtils.isEmpty(spConfig.getCta())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return checkArgs(mSpread);
    }

    @Override
    public boolean showInterstitial() {
        printInterfaceLog(ACTION_SHOW);
        if (mSpread != null) {
            show();
            mSpread = null;
            return true;
        }
        return false;
    }

    private void show() {
        try {
            if (mSpread != null) {
                SpConfig spConfig = mSpread;
                Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.AFPICKER");
                if (intent == null) {
                    intent = new Intent();
                    ComponentName cmp = new ComponentName(mContext, IAdvance.ACT_NAME);
                    intent.setComponent(cmp);
                }
                intent.putExtra(Intent.EXTRA_STREAM, spConfig);
                intent.putExtra(Intent.EXTRA_TITLE, getAdPlaceName());
                intent.putExtra(Intent.EXTRA_TEXT, getSdkName());
                intent.putExtra(Intent.EXTRA_TEMPLATE, getAdType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                reportAdShow();
                registerDismiss();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void registerDismiss() {
        unregisterDismiss();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(mContext.getPackageName() + ".action.SPDISMISS");
            filter.addAction(mContext.getPackageName() + ".action.SPCLICK");
            filter.addAction(mContext.getPackageName() + ".action.SPSHOW");
            mContext.registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
        }
    }

    private void unregisterDismiss() {
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }
            if (TextUtils.equals(context.getPackageName() + ".action.SPDISMISS", intent.getAction())) {
                unregisterDismiss();
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            } else if (TextUtils.equals(context.getPackageName() + ".action.SPCLICK", intent.getAction())) {
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            } else if (TextUtils.equals(context.getPackageName() + ".action.SPSHOW", intent.getAction())) {
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }
        }
    };
}
