package com.inner.adsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.data.DataManager;
import com.inner.adsdk.framework.ActivityMonitor;
import com.inner.adsdk.framework.AdPlaceLoader;
import com.inner.adsdk.listener.OnAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;
import com.sub.great.good.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdSdk {

    /**
     * COMMON SDK 名字
     */
    public static final String AD_SDK_COMMON = Constant.AD_SDK_COMMON;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_SMALL = Constant.NATIVE_CARD_SMALL;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_MEDIUM = Constant.NATIVE_CARD_MEDIUM;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_LARGE = Constant.NATIVE_CARD_LARGE;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_FULL = Constant.NATIVE_CARD_FULL;

    /**
     * BANNER类型
     */
    public static final String AD_TYPE_BANNER = Constant.TYPE_BANNER;

    /**
     * INTERSTITIAL类型
     */
    public static final String AD_TYPE_INTERSTITIAL = Constant.TYPE_INTERSTITIAL;

    /**
     * NATIVE类型
     */
    public static final String AD_TYPE_NATIVE = Constant.TYPE_NATIVE;

    /**
     * REWARD类型
     */
    public static final String AD_TYPE_REWARD = Constant.TYPE_REWARD;

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
        Log.v(Log.TAG, "adver : " + getSdkVersion());
        DataManager.get(mContext).init();
        ActivityMonitor.get(mContext).init();
        StatImpl.get().init();
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

    public String getString(String key) {
        return DataManager.get(mContext).getString(key);
    }

    private AdPlaceLoader getAdLoader(String pidName) {
        return getAdLoader(pidName, false);
    }

    private AdPlaceLoader getAdLoader(String pidName, boolean forLoad) {
        Log.d(Log.TAG, "getAdLoader forLoad : " + forLoad);
        // 获取引用的pidname
        String refPidName = pidName;

        Log.v(Log.TAG, "pidName : " + pidName + " , refPidName : " + refPidName);
        boolean useShareObject = false;
        // 如果共享loader对象
        if (!TextUtils.equals(pidName, refPidName)) {
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
            return loader;
        }
        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(refPidName);
        // loader为null，或者AdPlace内容有变化，则重新加载loader
        if (loader == null || loader.needReload(adPlace)) {
            loader = createAdPlaceLoader(refPidName, adPlace);
            if (loader != null) {
                mAdLoaders.put(pidName, loader);
                if (useShareObject && !mAdLoaders.containsKey(refPidName)) {
                    mAdLoaders.put(refPidName, loader);
                }
            }
        }
        return loader;
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
        AdConfig localConfig = DataManager.get(mContext).getAdConfig();
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
            loader.setOnAdSdkListener(l);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadInterstitial(activity);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showInterstitial(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showInterstitial();
        }
    }

    public boolean isBannerLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isBannerLoaded();
        }
        return false;
    }

    public void loadBanner(String pidName, OnAdSdkListener l) {
        loadBanner(pidName, new AdParams.Builder().build(), l);
    }

    public void loadBanner(String pidName, AdParams adParams) {
        loadBanner(pidName, adParams, null);
    }

    public void loadBanner(String pidName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadBanner(adParams);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showBanner(String pidName, ViewGroup adContainer) {
        showBanner(pidName, null, adContainer);
    }

    public void showBanner(String pidName, AdParams adParams, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showBanner(adContainer, adParams);
        }
    }
    ///////////////////////////////////////////////////////////////////////
    public boolean isNativeLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isNativeLoaded();
        }
        return false;
    }

    public void loadNative(String pidName, OnAdSdkListener l) {
        loadNative(pidName, new AdParams.Builder().build(), l);
    }

    public void loadNative(String pidName, AdParams adParams) {
        loadNative(pidName, adParams, null);
    }

    public void loadNative(String pidName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadNative(adParams);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showNative(String pidName, ViewGroup adContainer) {
        showNative(pidName, null, adContainer);
    }

    public void showNative(String pidName, AdParams adParams, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showNative(adContainer, adParams);
        }
    }

    public boolean isRewardVideoLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isRewardVideoLoaded();
        }
        return false;
    }

    public void loadRewardVideo(String pidName) {
        loadRewardVideo(null, pidName);
    }

    public void loadRewardVideo(String pidName, OnAdSdkListener l) {
        loadRewardVideo(null, pidName, l);
    }

    public void loadRewardVideo(Activity activity, String pidName) {
        loadRewardVideo(activity, pidName, null);
    }

    public void loadRewardVideo(Activity activity, String pidName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadRewardVideo(activity);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showRewardVideo(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showRewardVideo();
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
}
