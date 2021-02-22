package com.rabbit.adsdk.adloader.spread;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.DataManager;
import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;
import com.rabbit.sunny.SpreadCfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private static final int MOCK_LOADING_TIME = 200;
    private SpreadCfg mSpread;
    private Params mParams;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SpreadBindNativeView spreadBindNativeView = new SpreadBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return spreadBindNativeView;
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_SPREAD;
    }

    @Override
    public void loadNative(Params params) {
        if (mPidConfig == null) {
            Log.e(Log.TAG, "pid config is null");
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.e(Log.TAG, "sdk not equals");
        }
        if (isNativeLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        if (isUserCheat()) {
            processCheatUser();
            return;
        }
        final SpreadCfg spreadCfg = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(spreadCfg)) {
            setLoading(true, STATE_REQUEST);
            printInterfaceLog(ACTION_LOAD);
            loadIcon(spreadCfg.getIcon());
            loadBanner(spreadCfg.getBanner());
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSpread = spreadCfg;
                        setLoading(false, STATE_SUCCESS);
                        reportAdLoaded();
                        notifyAdLoaded(false);
                    }
                }, MOCK_LOADING_TIME);
            }
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
            SpreadCfg spreadCfg = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadCfg));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadCfg);
            if (getAdListener() != null) {
                getAdListener().onAdImp();
            }
            mSpread = null;
            reportAdImp();
        }
    }

    @Override
    public void loadInterstitial() {
        if (mPidConfig == null) {
            Log.e(Log.TAG, "pid config is null");
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.e(Log.TAG, "sdk not equals");
        }
        if (isInterstitialLoaded()) {
            Log.d(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_LOADING);
            }
            return;
        }
        if (isUserCheat()) {
            processCheatUser();
            return;
        }
        final SpreadCfg spreadCfg = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(spreadCfg)) {
            setLoading(true, STATE_REQUEST);
            printInterfaceLog(ACTION_LOAD);
            loadIcon(spreadCfg.getIcon());
            loadBanner(spreadCfg.getBanner());
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSpread = spreadCfg;
                        setLoading(false, STATE_SUCCESS);
                        reportAdLoaded();
                        notifyAdLoaded(false);
                    }
                }, MOCK_LOADING_TIME);
            }
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
    private SpreadCfg checkSpConfig(List<SpreadCfg> spList) {
        if (spList == null || spList.isEmpty()) {
            return null;
        }
        List<SpreadCfg> availableSp = new ArrayList<SpreadCfg>();
        for (SpreadCfg config : spList) {
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
     * @param spreadCfg
     * @return
     */
    private boolean checkArgs(SpreadCfg spreadCfg) {
        if (spreadCfg == null) {
            return false;
        }
        if (TextUtils.isEmpty(spreadCfg.getBanner())
                || TextUtils.isEmpty(spreadCfg.getIcon())
                || TextUtils.isEmpty(spreadCfg.getTitle())
                || TextUtils.isEmpty(spreadCfg.getPkgname())
                || TextUtils.isEmpty(spreadCfg.getDetail())
                || TextUtils.isEmpty(spreadCfg.getCta())) {
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
            showInterstitialInternal();
            mSpread = null;
            return true;
        }
        return false;
    }

    private void showInterstitialInternal() {
        try {
            if (mSpread != null) {
                SpreadCfg spreadCfg = mSpread;
                Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.AFPICKER");
                if (intent == null) {
                    intent = new Intent();
                    ComponentName cmp = new ComponentName(mContext, Utils.getActivityNameByAction(getContext(), getContext().getPackageName() + ".action.MATCH_DOING"));
                    intent.setComponent(cmp);
                }
                intent.putExtra(Intent.EXTRA_STREAM, spreadCfg);
                intent.putExtra(Intent.EXTRA_TITLE, getAdPlaceName());
                intent.putExtra(Intent.EXTRA_TEXT, getSdkName());
                intent.putExtra(Intent.EXTRA_TEMPLATE, getAdType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                reportAdShow();
                registerEvent();
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void registerEvent() {
        unregisterEvent();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(getClickAction(mContext));
            filter.addAction(getShowAction(mContext));
            filter.addAction(getDismissAction(mContext));
            filter.addAction(getImpAction(mContext));
            mContext.registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
        }
    }

    private static String getClickAction(Context context) {
        if (context == null) {
            return "com.hauyu.adsdk.action.SP_CLICK";
        }
        return context.getPackageName() + ".action.SP_CLICK";
    }

    private static String getShowAction(Context context) {
        if (context == null) {
            return "com.hauyu.adsdk.action.SP_SHOW";
        }
        return context.getPackageName() + ".action.SP_SHOW";
    }

    private static String getImpAction(Context context) {
        if (context == null) {
            return "com.hauyu.adsdk.action.SP_IMP";
        }
        return context.getPackageName() + ".action.SP_IMP";
    }

    private static String getDismissAction(Context context) {
        if (context == null) {
            return "com.hauyu.adsdk.action.SP_DISMISS";
        }
        return context.getPackageName() + ".action.SP_DISMISS";
    }

    public static void reportShow(Context context) {
        try {
            context.sendBroadcast(new Intent(SpLoader.getShowAction(context)).setPackage(context.getPackageName()));
        } catch (Exception e) {
        }
    }

    public static void reportImp(Context context) {
        try {
            context.sendBroadcast(new Intent(SpLoader.getImpAction(context)).setPackage(context.getPackageName()));
        } catch (Exception e) {
        }
    }

    public static void reportClick(Context context) {
        try {
            context.sendBroadcast(new Intent(SpLoader.getClickAction(context)).setPackage(context.getPackageName()));
        } catch (Exception e) {
        }
    }

    public static void reportDismiss(Context context) {
        try {
            context.sendBroadcast(new Intent(SpLoader.getDismissAction(context)).setPackage(context.getPackageName()));
        } catch (Exception e) {
        }
    }

    private void unregisterEvent() {
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
            if (TextUtils.equals(getDismissAction(context), intent.getAction())) {
                unregisterEvent();
                reportAdClose();
                if (getAdListener() != null) {
                    getAdListener().onAdDismiss();
                }
            } else if (TextUtils.equals(getClickAction(context), intent.getAction())) {
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            } else if (TextUtils.equals(getShowAction(context), intent.getAction())) {
                reportAdShow();
                if (getAdListener() != null) {
                    getAdListener().onAdShow();
                }
            } else if (TextUtils.equals(getImpAction(context), intent.getAction())) {
                reportAdImp();
                if (getAdListener() != null) {
                    getAdListener().onAdImp();
                }
            }
        }
    };

    public class ClickClass implements View.OnClickListener {
        private SpreadCfg mSpreadCfg;

        public ClickClass(SpreadCfg spreadCfg) {
            mSpreadCfg = spreadCfg;
        }

        @Override
        public void onClick(View v) {
            if (mSpreadCfg != null) {
                String url = mSpreadCfg.getLinkUrl();
                if (TextUtils.isEmpty(url)) {
                    url = "market://details?id=" + mSpreadCfg.getPkgname();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.v(Log.TAG, "error : " + e);
                }
                reportAdClick();
                if (getAdListener() != null) {
                    getAdListener().onAdClick();
                }
            }
        }
    }
}
