package com.hauyu.adsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.hauyu.adsdk.config.AdConfig;
import com.hauyu.adsdk.config.AdPlace;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.framework.ActivityMonitor;
import com.hauyu.adsdk.framework.AdPlaceLoader;
import com.hauyu.adsdk.framework.AdReceiver;
import com.hauyu.adsdk.framework.AtAdLoader;
import com.hauyu.adsdk.framework.GtAdLoader;
import com.hauyu.adsdk.framework.StAdLoader;
import com.hauyu.adsdk.listener.OnAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.stat.StatImpl;
import com.hauyu.adsdk.utils.Utils;

import java.lang.ref.WeakReference;
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
            } else {
                sAdSdk.setActivity(null);
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
        } else {
            mActivity = null;
        }
    }

    public void send() {
        try {
            GtAdLoader.get(mContext).onFire();
        } catch(Exception e) {
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
        DataManager.get(mContext).init();
        ActivityMonitor.get(mContext).init();
        StatImpl.get().init();
        AdReceiver.get(mContext).init();
        GtAdLoader.get(mContext).init(this);
        StAdLoader.get(mContext).init(this);
        AtAdLoader.get(mContext).init(this);
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
        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(refPidName);
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
        AdConfig localConfig = DataManager.get(mContext).getAdConfig();
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
        Map<String, String> adRefs = DataManager.get(mContext).getRemoteAdRefs();
        AdConfig localConfig = DataManager.get(mContext).getAdConfig();
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
        AdConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (localConfig != null && adPlace == null) {
            adPlace = localConfig.get(pidName);
            useRemote = false;
        }
        Map<String, String> adIds = DataManager.get(mContext).getRemoteAdIds();
        if (localConfig != null && adIds == null) {
            adIds = localConfig.getAdIds();
        }
        Log.v(Log.TAG, "pidName : " + pidName + " , adPlace : " + adPlace);
        if (adPlace != null) {
            loader = new AdPlaceLoader(mContext);
            loader.setAdPlaceConfig(adPlace);
            loader.setAdIds(adIds);
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
                if (mActivity != null && mActivity.get() != null) {
                    activity = mActivity.get();
                }
            }
            loader.loadInterstitial(activity);
        }
    }

    public void showInterstitial(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showInterstitial();
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
            loader.setOnAdSdkListener(l);
            loader.loadAdView(adParams);
        }
    }

    public void showAdView(String pidName, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showAdView(adContainer);
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

    public void loadComplexAds(String pidName, AdParams extra, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadComplexAds(extra);
        }
    }

    public void showComplexAds(String pidName, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showComplexAds(adContainer);
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
            mAdLoaders.remove(pidName);
            loader.destroy();
        }
    }
}
