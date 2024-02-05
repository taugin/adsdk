package com.humob.adsdk.adloader.spread;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.humob.adsdk.adloader.base.AbstractSdkLoader;
import com.humob.adsdk.adloader.base.BaseBindNativeView;
import com.humob.adsdk.adloader.listener.ISdkLoader;
import com.humob.adsdk.constant.Constant;
import com.humob.adsdk.core.framework.AdPlaceLoader;
import com.humob.adsdk.core.framework.Params;
import com.humob.adsdk.data.DataManager;
import com.humob.adsdk.data.config.SpreadConfig;
import com.humob.adsdk.http.Http;
import com.humob.adsdk.log.Log;
import com.humob.adsdk.utils.Utils;
import com.humob.api.AdActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        if (checkArgs(spreadConfig)) {
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
        } else {
            reportAdError(String.valueOf("ERROR_LOAD"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "error load");
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
            notifyAdImp(null, sceneName);
            reportAdImp();
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
        if (checkArgs(spreadConfig)) {
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
        } else {
            reportAdError(String.valueOf("ERROR_LOAD"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "error load");
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
            if (checkArgs(config) && !Utils.isInstalled(mContext, config.getPackageName()) && !config.isDisable()) {
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
                || TextUtils.isEmpty(spreadConfig.getPackageName())
                || TextUtils.isEmpty(spreadConfig.getDetail())
                || TextUtils.isEmpty(spreadConfig.getCta())) {
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
        showAdViewWithUI(getAdPlaceName(), getSdkName(), getAdType(), this);
        return true;
    }

    private void showAdViewWithUI(String placeName, String source, String adType, ISdkLoader iSdkLoader) {
        Log.iv(Log.TAG, "show spread ads with ui");
        try {
            reportAdShow();
            notifyAdShow();
            Params params = new Params();
            params.setAdCardStyle(Constant.NATIVE_CARD_FULL);
            AdPlaceLoader.sLoaderMap.put(String.format(Locale.ENGLISH, "%s_%s_%s", source, adType, placeName), iSdkLoader);
            AdPlaceLoader.sParamsMap.put(String.format(Locale.ENGLISH, "%s_%s_%s", source, adType, placeName), params);
            Intent intent = new Intent(mContext, AdActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, placeName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void showInterstitialWithNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        String sceneName = null;
        if (params != null) {
            mParams = params;
            sceneName = params.getSceneName();
        }
        if (mSpread != null) {
            SpreadConfig spreadConfig = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadConfig));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadConfig);
            mSpread = null;
            notifyAdImp(null, sceneName);
            reportAdImp();
        }
    }

    public class ClickClass implements View.OnClickListener {
        private SpreadConfig mSpreadConfig;

        public ClickClass(SpreadConfig spreadConfig) {
            mSpreadConfig = spreadConfig;
        }

        @Override
        public void onClick(View v) {
            if (mSpreadConfig != null) {
                String url = mSpreadConfig.getLinkUrl();
                if (TextUtils.isEmpty(url)) {
                    url = "market://details?id=" + mSpreadConfig.getPackageName();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
                reportAdClick();
                notifyAdClick();
            }
        }
    }
}
