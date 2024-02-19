package com.komob.adsdk.adloader.spread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.komob.adsdk.InternalStat;
import com.komob.adsdk.adloader.base.AbstractSdkLoader;
import com.komob.adsdk.adloader.base.BaseBindNativeView;
import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.core.framework.AdPlaceLoader;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.data.config.SpreadConfig;
import com.komob.adsdk.http.Http;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.stat.EventImpl;
import com.komob.adsdk.utils.Utils;
import com.komob.api.AdViewUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private static AtomicBoolean sRegister = new AtomicBoolean(false);
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
        if (!sRegister.getAndSet(true)) {
            register(context);
        }
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
        if (!checkArgs(spreadConfig)) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "spread contains error");
            return;
        }

        if (Utils.isInstalled(mContext, spreadConfig.getBundle())) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "app has installed");
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
        if (!checkArgs(spreadConfig)) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "spread contains error");
            return;
        }

        if (Utils.isInstalled(mContext, spreadConfig.getBundle())) {
            notifyAdLoadFailed(Constant.AD_ERROR_LOAD, "app has installed");
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
            if (checkArgs(config) && !config.isDisable()) {
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
            Intent intent = new Intent(mContext, AdViewUI.class);
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
            params.setAdCardStyle(Constant.NATIVE_CARD_FULL_LIST.get(new Random().nextInt(Constant.NATIVE_CARD_FULL_LIST.size())));
            mParams = params;
            sceneName = params.getSceneName();
        }
        if (mSpread != null) {
            SpreadConfig spreadConfig = mSpread;
            spreadBindNativeView.setClickListener(new ClickClass(spreadConfig));
            spreadBindNativeView.bindNative(mParams, viewGroup, mPidConfig, spreadConfig);
            mSpread = null;
            reportAdSpreadImp(spreadConfig);
            notifyAdImp(null, sceneName);
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
                String packageName = mSpreadConfig.getBundle();
                String referrer = null;
                try {
                    referrer = generateReferrer(v.getContext());
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
                if (TextUtils.isEmpty(url)) {
                    url = "market://details?id=" + packageName;
                    if (!TextUtils.isEmpty(referrer)) {
                        url = url + "&" + referrer;
                    }
                }
                Log.iv(Log.TAG, "spread url : " + url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (mSpreadConfig.isPlay() && Utils.isInstalled(v.getContext(), "com.android.vending")) {
                    intent.setPackage("com.android.vending");
                }
                try {
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.iv(Log.TAG, "error : " + e);
                }
                try {
                    DBManager.get(v.getContext()).insertOrUpdateClick(packageName, System.currentTimeMillis());
                } catch (Exception e) {
                }
                reportAdSpreadClk(mSpreadConfig);
                notifyAdClick();
            }
        }
    }

    private static String generateReferrer(Context context) {
        String packageName = context.getPackageName();
        String gclid = Utils.string2MD5(packageName);
        return String.format(Locale.ENGLISH, "referrer=utm_source%%3D%s%%26utm_medium%%3Dcpc%%26utm_campaign%%3Dspread%%26gclid%%3D%s", packageName, gclid);
    }

    private void reportAdSpreadImp(SpreadConfig spreadConfig) {
        try {
            String packageName = null;
            if (spreadConfig != null) {
                packageName = spreadConfig.getBundle();
            }
            Map<String, Object> extra = new HashMap<>();
            extra.put("bundle", packageName);
            EventImpl.get().reportAdImp(mContext, getAdPlaceName(), getSdkName(), null, getAdType(), getPid(), null, getCpm(), extra);
        } catch (Exception e) {
        }
    }

    private void reportAdSpreadClk(SpreadConfig spreadConfig) {
        try {
            String packageName = null;
            if (spreadConfig != null) {
                packageName = spreadConfig.getBundle();
            }
            Map<String, Object> extra = new HashMap<>();
            extra.put("bundle", packageName);
            EventImpl.get().reportAdClick(mContext, getAdPlaceName(), getSdkName(), null, getAdType(), getPid(), null, getCpm(), extra, null);
        } catch (Exception e) {
        }
    }

    private static void reportAdSpreadInstalled(Context context, String packageName) {
        try {
            InternalStat.reportEvent(context, "ad_spread_installed", packageName);
        } catch (Exception e) {
        }
    }

    private static void register(Context context) {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");
            context.registerReceiver(sBroadcastReceiver, filter);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private static String parsePackageName(Intent intent) {
        try {
            String data = intent.getDataString();
            return data.substring("package:".length());
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    private static BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }
            try {
                String action = intent.getAction();
                if (TextUtils.equals(action, Intent.ACTION_PACKAGE_ADDED)) {
                    String packageName = parsePackageName(intent);
                    DBManager.SpreadClickInfo spreadClickInfo = DBManager.get(context).queryClickSpread(packageName);
                    if (spreadClickInfo != null) {
                        int _id = spreadClickInfo._id;
                        Log.iv(Log.TAG, "install package name : " + packageName + " , _id : " + _id);
                        if (_id >= 0) {
                            DBManager.get(context).updateInstallTime(_id, System.currentTimeMillis(), spreadClickInfo.installCount + 1);
                            reportAdSpreadInstalled(context, packageName);
                        }
                    }
                }
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    };
}
