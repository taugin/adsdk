package com.inner.adsdk;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.AdPlaceLoader;
import com.inner.adsdk.framework.OuterAdLoader;
import com.inner.adsdk.listener.OnAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.stat.StatImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdSdk {

    private static AdSdk sAdSdk;

    private Context mContext;
    private Map<String, AdPlaceLoader> mAdLoaders = new HashMap<String, AdPlaceLoader>();
    private AdConfig mLocalAdConfig;

    private AdSdk(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AdSdk get(Context context) {
        if (sAdSdk == null) {
            create(context);
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

    /**
     * 获取版本号
     * @return
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 初始化
     * @param containerId Google Tag ContainerId
     */
    public void init(String containerId) {
        init(containerId, null);
    }

    /**
     * 初始化
     * @param containerId Google Tag ContainerId
     * @param gaTrackerId Google Analytic TrackerId
     */
    public void init(String containerId, String gaTrackerId) {
        StatImpl.get().init(mContext, gaTrackerId);
        DataManager.get(mContext).init(containerId);
        mLocalAdConfig = DataManager.get(mContext).getLocalAdConfig();
        OuterAdLoader.get(mContext).init(this);
    }

    private AdPlaceLoader getAdLoader(String pidName) {
        AdPlaceLoader loader = mAdLoaders.get(pidName);
        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(pidName);
        Map<String, String> adids = DataManager.get(mContext).getRemoteAdIds(Constant.ADIDS_NAME);
        if (loader == null || (!loader.isFromRemote() && adPlace != null)) {
            loader = createAdPlaceLoader(pidName, adPlace, adids);
            if (loader != null) {
                mAdLoaders.put(pidName, loader);
            }
        }
        return loader;
    }

    /**
     * 如果adPlace和adIds为空，则使用本地的adPlace和adIds
     * @param pidName
     * @param adPlace
     * @param adIds
     * @return
     */
    private AdPlaceLoader createAdPlaceLoader(String pidName, AdPlace adPlace, Map<String, String> adIds) {
        AdPlaceLoader loader = null;
        boolean useRemote = false;
        if (adPlace == null) {
            adPlace = mLocalAdConfig.get(pidName);
        } else {
            useRemote = true;
        }
        if (adIds == null) {
            adIds = mLocalAdConfig.getAdIds();
        }
        Log.v(Log.TAG, "pidName : " + pidName + " , adPlace : " + adPlace);
        if (adPlace != null) {
            loader = new AdPlaceLoader(mContext);
            loader.setAdPlaceConfig(adPlace);
            loader.setAdIds(adIds);
            loader.init();
            loader.setFromRemote(useRemote);
        }
        Log.d(Log.TAG, "pidName [" + pidName + "] use remote config : " + useRemote);
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
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
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
        loadAdView(pidName, null, l);
    }

    public void loadAdView(String pidName, AdParams adParams) {
        loadAdView(pidName, adParams, null);
    }

    public void loadAdView(String pidName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
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
        AdPlaceLoader loader = getAdLoader(pidName);
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
