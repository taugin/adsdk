package com.hauyu.adsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bacad.ioc.gsb.base.CSvr;
import com.dock.vist.sun.BuildConfig;
import com.dock.vist.sun.IAdvance;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.core.framework.AdPlaceLoader;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.PlaceConfig;
import com.hauyu.adsdk.listener.OnAdSdkListener;
import com.hauyu.adsdk.listener.OnTriggerListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;
import com.hauyu.adsdk.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdSdk {

    private static AdSdk sAdSdk;

    private Context mContext;
    private Map<String, AdPlaceLoader> mAdLoaders = new HashMap<String, AdPlaceLoader>();
    private WeakReference<Activity> mActivity;

    private AdSdk(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AdSdk get(Context context) {
        if (sAdSdk == null) {
            create(context);
        }
        if (sAdSdk != null) {
            if (context instanceof Activity) {
                sAdSdk.setActivity((Activity) context);
            }
        }
        return sAdSdk;
    }

    private static void create(Context context) {
        synchronized (AdSdk.class) {
            if (sAdSdk == null) {
                sAdSdk = new AdSdk(context);
            }
        }
    }

    private void setActivity(Activity activity) {
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 初始化
     */
    public void init() {
        Log.v(Log.TAG, "sdk version : " + getSdkVersion());
        DataManager.get(mContext).init();
        ActivityMonitor.get(mContext).init();
        EventImpl.get().init();
        callInit(mContext);
    }

    /**
     * 设置appsflyer归因
     *
     * @param afStatus
     * @param mediaSource
     */
    public void setAttribution(String afStatus, String mediaSource) {
        if (!TextUtils.isEmpty(afStatus)) {
            Utils.putString(mContext, Constant.AF_STATUS, afStatus);
        }
        if (!TextUtils.isEmpty(mediaSource)) {
            Utils.putString(mContext, Constant.AF_MEDIA_SOURCE, mediaSource);
        }
    }

    public String getAttribution() {
        return Utils.getString(mContext, Constant.AF_STATUS);
    }

    public String getMediaSource() {
        return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
    }

    public String getString(String key) {
        return DataManager.get(mContext).getString(key);
    }

    private AdPlaceLoader getAdLoader(String pidName) {
        return getAdLoader(pidName, false);
    }

    private AdPlaceLoader getAdLoader(String pidName, boolean forLoad) {
        Log.d(Log.TAG, "getAdLoader forLoad : " + forLoad);
        // 获取引用的pidname
        String refPidName = getAdRefPidName(pidName);

        Log.v(Log.TAG, "pidName : " + pidName + " , refPidName : " + refPidName);
        boolean useShareObject = false;
        // 如果共享loader对象
        if (!TextUtils.equals(pidName, refPidName) && isRefShare(refPidName)) {
            useShareObject = true;
        }
        if (useShareObject) {
            // 将共享对象赋值给新的场景
            if (mAdLoaders.containsKey(refPidName) && !mAdLoaders.containsKey(pidName)) {
                mAdLoaders.put(pidName, mAdLoaders.get(refPidName));
            }
        }
        AdPlaceLoader loader = mAdLoaders.get(pidName);
        if (!forLoad) {
            if (useShareObject && !TextUtils.equals(pidName, refPidName) && loader != null) {
                loader.setOriginPidName(pidName);
            }
            return loader;
        }
        // 首先获取远程针对广告位的配置是否存在
        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(refPidName);
        // 如果远程无配置，则读取本地或者远程整体广告位配置
        if (adPlace == null) {
            PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
            if (localConfig != null ) {
                adPlace = localConfig.get(pidName);
            }
        }
        // loader为null，或者AdPlace内容有变化，则重新加载loader
        if (loader == null || loader.needReload(adPlace)) {
            loader = createAdPlaceLoader(refPidName, adPlace);
            if (loader != null) {
                if (!TextUtils.equals(pidName, refPidName)) {
                    loader.setOriginPidName(pidName);
                }
                mAdLoaders.put(pidName, loader);
                if (useShareObject && !mAdLoaders.containsKey(refPidName)) {
                    mAdLoaders.put(refPidName, loader);
                }
            }
        }
        return loader;
    }

    private boolean isRefShare(String pidName) {
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (localConfig != null) {
            AdPlace adPlace = localConfig.get(pidName);
            if (adPlace != null) {
                return adPlace.isRefShare();
            }
        }
        return false;
    }

    /**
     * 获取引用pid名字
     *
     * @param pidName
     * @return 引用pid名字
     */
    private String getAdRefPidName(String pidName) {
        String adrefPidName = pidName;
        // 获取通过代码设置的别名
        adrefPidName = getAdPlaceAlias(pidName);
        if (!TextUtils.isEmpty(adrefPidName)) {
            return adrefPidName;
        }
        Map<String, String> adRefs = DataManager.get(mContext).getRemoteAdRefs();
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (adRefs == null && localConfig != null) {
            adRefs = localConfig.getAdRefs();
        }
        if (adRefs != null && adRefs.containsKey(pidName)) {
            adrefPidName = adRefs.get(pidName);
        }
        if (!TextUtils.isEmpty(adrefPidName)) {
            return adrefPidName;
        }
        return pidName;
    }

    /**
     * 如果adPlace和adIds为空，则使用本地的adPlace和adIds
     *
     * @param pidName
     * @param adPlace
     * @return
     */
    private AdPlaceLoader createAdPlaceLoader(String pidName, AdPlace adPlace) {
        AdPlaceLoader loader = null;
        boolean useRemote = true;
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (localConfig != null && adPlace == null) {
            adPlace = localConfig.get(pidName);
            useRemote = false;
        }
        Log.v(Log.TAG, "pidName : " + pidName + " , adPlace : " + adPlace);
        if (adPlace != null) {
            loader = new AdPlaceLoader(mContext);
            loader.setAdPlaceConfig(adPlace);
            loader.init();
        }
        Log.v(Log.TAG, "pidName [" + pidName + "] use remote adplace : " + useRemote);
        return loader;
    }

    public boolean isInterstitialLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isInterstitialLoaded();
        }
        return false;
    }

    public void loadInterstitial(String pidName) {
        loadInterstitial(null, pidName);
    }

    public void loadInterstitial(String pidName, OnAdSdkListener l) {
        loadInterstitial(null, pidName, l);
    }

    public void loadInterstitial(Activity activity, String pidName) {
        loadInterstitial(activity, pidName, null);
    }

    public void loadInterstitial(Activity activity, String pidName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadInterstitial(activity);
        } else {
            if (l != null) {
                l.onError(pidName, null, null);
            }
        }
    }

    public void showInterstitial(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showInterstitial();
        }
    }



    public boolean isRewardedVideoLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isRewardedVideoLoaded();
        }
        return false;
    }

    public void loadRewardedVideo(String pidName) {
        loadRewardedVideo(null, pidName);
    }

    public void loadRewardedVideo(String pidName, OnAdSdkListener l) {
        loadRewardedVideo(null, pidName, l);
    }

    public void loadRewardedVideo(Activity activity, String pidName) {
        loadRewardedVideo(activity, pidName, null);
    }

    public void loadRewardedVideo(Activity activity, String pidName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadRewardedVideo(activity);
        } else {
            if (l != null) {
                l.onError(pidName, null, null);
            }
        }
    }

    public void showRewardedVideo(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showRewardedVideo();
        }
    }

    public boolean isAdViewLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isAdViewLoaded();
        }
        return false;
    }

    public void loadAdView(String pidName, OnAdSdkListener l) {
        loadAdView(pidName, new AdParams.Builder().build(), l);
    }

    public void loadAdView(String pidName, AdParams adParams) {
        loadAdView(pidName, adParams, null);
    }

    public void loadAdView(String pidName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            loader.loadAdView(adParams);
        } else {
            if (l != null) {
                l.onError(pidName, null, null);
            }
        }
    }

    public void showAdView(String pidName, ViewGroup adContainer) {
        showAdView(pidName, null, adContainer);
    }

    public void showAdView(String pidName, AdParams adParams, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showAdView(adContainer, adParams);
        }
    }

    public boolean isComplexAdsLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isComplexAdsLoaded();
        }
        return false;
    }

    public void loadComplexAds(String pidName) {
        loadComplexAds(pidName, null, null);
    }

    public void loadComplexAds(String pidName, OnAdSdkListener l) {
        loadComplexAds(pidName, null, l);
    }

    public void loadComplexAds(String pidName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            loader.loadComplexAds(adParams);
        } else {
            if (l != null) {
                l.onError(pidName, null, null);
            }
        }
    }

    public void showComplexAds(String pidName, ViewGroup adContainer) {
        showComplexAds(pidName, null, null, adContainer);
    }

    public void showComplexAds(String pidName, String source, String adType, ViewGroup adContainer) {
        showComplexAds(pidName, null, source, adType, adContainer);
    }

    public void showComplexAds(String pidName, AdParams adParams, String source, String adType, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showComplexAds(adContainer, adParams, source, adType);
        }
    }

    public boolean showComplexAdsWithResult(String pidName, AdParams adParams, String source, String adType, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.showComplexAds(adContainer, adParams, source, adType);
        }
        return false;
    }

    public void showComplexAds(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showComplexAds();
        }
    }

    /**
     * 独立是设置监听器接口
     * @param pidName
     * @param l
     */
    public void setOnAdSdkListener(String pidName, OnAdSdkListener l) {
        setOnAdSdkListener(pidName, l, false);
    }

    public void setOnAdSdkListener(String pidName, OnAdSdkListener l, boolean loaded) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdSdkListener(l, loaded);
        }
    }

    public int getAdCount(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.getAdCount();
        }
        return 0;
    }

    public String getAdMode(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.getAdMode();
        }
        return null;
    }

    public boolean isLoading(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isLoading();
        }
        return false;
    }

    public boolean isAdError(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isAdError();
        }
        return false;
    }

    public void resume(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.resume();
        }
    }

    public void pause(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.pause();
        }
    }

    public void destroy(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            // mAdLoaders.remove(pidName);
            loader.destroy();
        }
    }

    private Context getFinalContext() {
        Context context = null;
        if (mActivity != null && mActivity.get() != null) {
            context = mActivity.get();
        } else {
            context = mContext;
        }
        return context;
    }

    public void registerTriggerListener(OnTriggerListener l) {
        CSvr.get(mContext).registerTriggerListener(l);
    }

    public void unregisterTriggerListener(OnTriggerListener l) {
        CSvr.get(mContext).unregisterTriggerListener(l);
    }

    /**
     * 动态设置广告场景的别名
     * @param srcAdPlace
     * @param dstAdPlace
     */
    public void setAdPlaceAlias(String srcAdPlace, String dstAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace) && !TextUtils.isEmpty(dstAdPlace)) {
            Utils.putString(mContext, Constant.AD_SDK_PREFIX + srcAdPlace, dstAdPlace);
            removeObjectFromAdLoaders(srcAdPlace);
        }
    }

    /**
     * 清除广告位别名
     * @param srcAdPlace
     */
    public void clearAdPlaceAlias(String srcAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace)) {
            Utils.clearPrefs(mContext, Constant.AD_SDK_PREFIX + srcAdPlace);
            removeObjectFromAdLoaders(srcAdPlace);
        }
    }

    private void removeObjectFromAdLoaders(String srcAdPlace) {
        try {
            mAdLoaders.remove(srcAdPlace);
        } catch (Exception | Error e) {
        }
    }

    /**
     * 读取广告场景的别名
     * @param srcAdPlace
     * @return
     */
    private String getAdPlaceAlias(String srcAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace)) {
            return Utils.getString(mContext, Constant.AD_SDK_PREFIX + srcAdPlace, null);
        }
        return null;
    }

    public void setQueueRunning(String pidName, boolean running) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setQueueRunning(running);
        }
    }

    /**
     * 用户禁止场景弹出
     * @param scene
     * @param disable true 禁止场景弹出，false 允许场景弹出
     */
    public void setSceneDisabledByUser(String scene, boolean disable) {
        Utils.putBoolean(mContext, Constant.AD_SDK_SCENE_DISABLED_PREFIX + scene, disable);
    }

    /**
     * 查询当前场景是否被用户禁止弹出
     * @param scene
     * @return
     */
    public boolean isSceneDisabledByUser(String scene) {
        return Utils.getBoolean(mContext, Constant.AD_SDK_SCENE_DISABLED_PREFIX + scene, false);
    }

    /**
     * 判断是否有场景广告展示过
     * @return
     */
    public boolean isSceneShown() {
        return !TextUtils.isEmpty(Utils.getString(mContext, Constant.LAST_SCENE_TYPE));
    }

    private void callInit(Context context) {
        String error  = null;
        try {
            Class<?> cls = Class.forName(IAdvance.ACT_NAME);
            Method method = cls.getMethod("init", Context.class);
            method.invoke(null, context);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            Log.iv(Log.TAG, "error : " + error);
        }
    }
}
