package com.mix.ads.adloader.spread;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.mix.ads.adloader.base.AbstractSdkLoader;
import com.mix.ads.adloader.base.BaseBindNativeView;
import com.mix.ads.constant.Constant;
import com.mix.ads.core.framework.Params;
import com.mix.ads.data.DataManager;
import com.mix.ads.data.config.PidConfig;
import com.mix.ads.data.config.SpreadConfig;
import com.mix.ads.http.Http;
import com.mix.ads.log.Log;
import com.mix.ads.stat.EventImpl;
import com.mix.ads.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private static final int MOCK_LOADING_TIME = 200;
    private SpreadConfig mSpread;
    private Params mParams;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SpreadBindNativeView spreadBindNativeView = new SpreadBindNativeView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return spreadBindNativeView;
    }

    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_SPREAD;
    }

    @Override
    public void loadNative(Params params) {
        if (mPidConfig == null) {
            Log.iv(Log.TAG, "pid config is null");
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.iv(Log.TAG, "sdk not equals");
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        final SpreadConfig spreadConfig = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (spreadConfig == null) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "no available spread");
            return;
        }
        if (!checkArgs(spreadConfig)) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "spread contains error");
            return;
        }

        setLoading(true, STATE_REQUEST);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadIcon(spreadConfig.getIcon());
        loadBanner(spreadConfig.getBanner());
        long cfgLoadingTime = spreadConfig.getLoadingTime();
        final long loadingTime = cfgLoadingTime > 0 ? cfgLoadingTime : MOCK_LOADING_TIME;
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSpread = spreadConfig;
                    setLoading(false, STATE_SUCCESS);
                    setSpreadRevenue(getSdkName(), getCpm() / 1000f);
                    reportAdLoaded();
                    notifySdkLoaderLoaded(false);
                }
            }, loadingTime);
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
        String sceneName = null;
        if (params != null) {
            mParams = params;
            sceneName = params.getSceneName();
        }
        if (mSpread != null) {
            notifyAdShow();
            reportAdShow();
            SpreadConfig spreadConfig = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadConfig));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadConfig);
            mSpread = null;
            reportAdSpreadImp(spreadConfig);
            notifyAdImp(null, sceneName);
        } else {
            Log.iv(Log.TAG, formatShowErrorLog("Spread is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "Spread not ready");
        }
    }

    @Override
    public void loadInterstitial() {
        if (mPidConfig == null) {
            Log.iv(Log.TAG, "pid config is null");
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.iv(Log.TAG, "sdk not equals");
        }
        if (isInterstitialLoaded()) {
            Log.iv(Log.TAG, "already loaded : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        final SpreadConfig spreadConfig = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (spreadConfig == null) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "no available spread");
            return;
        }

        if (!checkArgs(spreadConfig)) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "spread contains error");
            return;
        }

        setLoading(true, STATE_REQUEST);
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        loadIcon(spreadConfig.getIcon());
        loadBanner(spreadConfig.getBanner());
        long cfgLoadingTime = spreadConfig.getLoadingTime();
        final long loadingTime = cfgLoadingTime > 0 ? cfgLoadingTime : MOCK_LOADING_TIME;
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSpread = spreadConfig;
                    setLoading(false, STATE_SUCCESS);
                    setSpreadRevenue(getSdkName(), getCpm() / 1000f);
                    reportAdLoaded();
                    notifySdkLoaderLoaded(false);
                }
            }, loadingTime);
        }
    }

    /**
     * 检出有效的配置
     *
     * @param spList
     * @return
     */
    private SpreadConfig checkSpConfig(List<SpreadConfig> spList) {
        if (spList == null || spList.isEmpty()) {
            return null;
        }
        List<SpreadConfig> availableSp = new ArrayList<SpreadConfig>();
        for (SpreadConfig config : spList) {
            // 参数有效，并且未安装
            if (checkArgs(config) && !config.isDisable() && !Utils.isInstalled(mContext, config.getBundle())) {
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
     * @param spreadConfig
     * @return
     */
    private boolean checkArgs(SpreadConfig spreadConfig) {
        if (spreadConfig == null) {
            return false;
        }
        if (TextUtils.isEmpty(spreadConfig.getBanner())
                || TextUtils.isEmpty(spreadConfig.getIcon())
                || TextUtils.isEmpty(spreadConfig.getTitle())
                || TextUtils.isEmpty(spreadConfig.getBundle())
                || TextUtils.isEmpty(spreadConfig.getDetail())
                || TextUtils.isEmpty(spreadConfig.getCta())
                || (!TextUtils.isEmpty(spreadConfig.getStore()) && !Utils.isInstalled(mContext, spreadConfig.getStore()))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return checkArgs(mSpread);
    }

    @Override
    public boolean showInterstitial(String sceneName) {
        printInterfaceLog(ACTION_SHOW);
        try {
            if (mSpread != null) {
                reportAdShow();
                notifyAdShow();
                if (!SpreadManager.get(mContext).showFullScreenAds(mSpread, new ClickClass(mSpread), this)) {
                    notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show spread interstitial error");
                    return false;
                } else {
                    reportAdSpreadImp(mSpread);
                    notifyAdImp(null, sceneName);
                }
                mSpread = null;
                return true;
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return false;
    }

    public class ClickClass implements View.OnClickListener {
        private SpreadConfig mSpreadConfig;

        public ClickClass(SpreadConfig spreadConfig) {
            mSpreadConfig = spreadConfig;
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = SpreadManager.get(context).generateIntent(context, mSpreadConfig, "placement");
            if (intent != null && mSpreadConfig != null) {
                String packageName = mSpreadConfig.getBundle();
                try {
                    context.startActivity(intent);
                    SpreadManager.get(context).insertOrUpdateClick(packageName, System.currentTimeMillis());
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                    try {
                        intent.setPackage(null);
                        context.startActivity(intent);
                    } catch (Exception error) {
                    }
                }
                reportAdSpreadClk(mSpreadConfig);
                notifyAdClick();
            }
        }
    }


    private void reportAdSpreadImp(SpreadConfig spreadConfig) {
        try {
            String bundle = null;
            String remark = null;
            if (spreadConfig != null) {
                bundle = spreadConfig.getBundle();
                remark = spreadConfig.getRemark();
            }
            Map<String, Object> extra = new HashMap<>();
            extra.put("bundle", bundle);
            extra.put("remark", remark);
            EventImpl.get().reportAdImp(mContext, getAdPlaceName(), getSdkName(), null, getAdType(), getPid(), null, getCpm(), extra);
        } catch (Exception e) {
        }
    }

    private void reportAdSpreadClk(SpreadConfig spreadConfig) {
        try {
            String bundle = null;
            String remark = null;
            if (spreadConfig != null) {
                bundle = spreadConfig.getBundle();
                remark = spreadConfig.getRemark();
            }
            Map<String, Object> extra = new HashMap<>();
            extra.put("bundle", bundle);
            extra.put("remark", remark);
            EventImpl.get().reportAdClick(mContext, getAdPlaceName(), getSdkName(), null, getAdType(), getPid(), null, getCpm(), extra, null);
        } catch (Exception e) {
        }
    }
}
