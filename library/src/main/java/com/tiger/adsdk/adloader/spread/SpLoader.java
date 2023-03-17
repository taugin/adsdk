package com.tiger.adsdk.adloader.spread;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.tiger.adsdk.adloader.base.AbstractSdkLoader;
import com.tiger.adsdk.adloader.base.BaseBindNativeView;
import com.tiger.adsdk.adloader.listener.ISdkLoader;
import com.tiger.adsdk.constant.Constant;
import com.tiger.adsdk.core.framework.AdPlaceLoader;
import com.tiger.adsdk.core.framework.Params;
import com.tiger.adsdk.data.DataManager;
import com.tiger.adsdk.http.Http;
import com.tiger.adsdk.log.Log;
import com.tiger.adsdk.utils.Utils;
import com.tiger.moon.SpreadInfo;
import com.tiger.moon.TigActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private static final int MOCK_LOADING_TIME = 200;
    private SpreadInfo mSpread;
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
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        final SpreadInfo spreadInfo = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(spreadInfo)) {
            setLoading(true, STATE_REQUEST);
            printInterfaceLog(ACTION_LOAD);
            reportAdRequest();
            notifyAdRequest();
            loadIcon(spreadInfo.getIcon());
            loadBanner(spreadInfo.getBanner());
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSpread = spreadInfo;
                        setLoading(false, STATE_SUCCESS);
                        reportAdLoaded();
                        notifySdkLoaderLoaded(false);
                    }
                }, MOCK_LOADING_TIME);
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
        if (params != null) {
            mParams = params;
        }
        if (mSpread != null) {
            notifyAdShow();
            reportAdShow();
            SpreadInfo spreadInfo = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadInfo));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadInfo);
            mSpread = null;
            notifyAdImp();
            reportAdImp();
        } else {
            Log.e(Log.TAG, formatShowErrorLog("Spread is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "Spread not ready");
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
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.d(Log.TAG, "already loading : " + getAdPlaceName() + " - " + getSdkName() + " - " + getAdType());
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        final SpreadInfo spreadInfo = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (checkArgs(spreadInfo)) {
            setLoading(true, STATE_REQUEST);
            printInterfaceLog(ACTION_LOAD);
            reportAdRequest();
            notifyAdRequest();
            loadIcon(spreadInfo.getIcon());
            loadBanner(spreadInfo.getBanner());
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSpread = spreadInfo;
                        setLoading(false, STATE_SUCCESS);
                        reportAdLoaded();
                        notifySdkLoaderLoaded(false);
                    }
                }, MOCK_LOADING_TIME);
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
    private SpreadInfo checkSpConfig(List<SpreadInfo> spList) {
        if (spList == null || spList.isEmpty()) {
            return null;
        }
        List<SpreadInfo> availableSp = new ArrayList<SpreadInfo>();
        for (SpreadInfo config : spList) {
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
     * @param spreadInfo
     * @return
     */
    private boolean checkArgs(SpreadInfo spreadInfo) {
        if (spreadInfo == null) {
            return false;
        }
        if (TextUtils.isEmpty(spreadInfo.getBanner())
                || TextUtils.isEmpty(spreadInfo.getIcon())
                || TextUtils.isEmpty(spreadInfo.getTitle())
                || TextUtils.isEmpty(spreadInfo.getPkgname())
                || TextUtils.isEmpty(spreadInfo.getDetail())
                || TextUtils.isEmpty(spreadInfo.getCta())) {
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
            Intent intent = new Intent(mContext, TigActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, placeName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void showInterstitialWithNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (params != null) {
            mParams = params;
        }
        if (mSpread != null) {
            SpreadInfo spreadInfo = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadInfo));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadInfo);
            mSpread = null;
            notifyAdImp();
            reportAdImp();
        }
    }

    public class ClickClass implements View.OnClickListener {
        private SpreadInfo mSpreadInfo;

        public ClickClass(SpreadInfo spreadInfo) {
            mSpreadInfo = spreadInfo;
        }

        @Override
        public void onClick(View v) {
            if (mSpreadInfo != null) {
                String url = mSpreadInfo.getLinkUrl();
                if (TextUtils.isEmpty(url)) {
                    url = "market://details?id=" + mSpreadInfo.getPkgname();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.v(Log.TAG, "error : " + e);
                }
                reportAdClick();
                notifyAdClick();
            }
        }
    }
}
