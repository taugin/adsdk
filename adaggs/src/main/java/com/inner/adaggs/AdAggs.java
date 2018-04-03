package com.inner.adaggs;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.framework.AdPlaceLoader;
import com.inner.adaggs.framework.OuterAdLoader;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.manager.DataManager;
import com.inner.adaggs.stat.StatImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdAggs {

    private static AdAggs sAdAggs;

    private Context mContext;
    private Map<String, AdPlaceLoader> mAdLoaders = new HashMap<String, AdPlaceLoader>();
    private AdConfig mAdConfig;

    private AdAggs(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AdAggs get(Context context) {
        if (sAdAggs == null) {
            create(context);
        }
        return sAdAggs;
    }

    private static void create(Context context) {
        synchronized (AdAggs.class) {
            if (sAdAggs == null) {
                sAdAggs = new AdAggs(context);
            }
        }
    }

    public void init() {
        init(false);
    }

    public void init(boolean l) {
        StatImpl.get().init(mContext);
        DataManager.get(mContext).init();
        mAdConfig = DataManager.get(mContext).getAdConfig();
        if (mAdConfig == null) {
            mAdConfig = new AdConfig();
        }
        OuterAdLoader.get(mContext).init(this);
        if (l) {
            OuterAdLoader.get(mContext).startLoop();
        }
    }

    private AdPlaceLoader getAdLoader(String pidName) {
        AdPlaceLoader loader = mAdLoaders.get(pidName);
        AdPlace adPlace = DataManager.get(mContext).getAdPlace(pidName);
        Map<String, String> adids = DataManager.get(mContext).getAdIds(Constant.ADIDS_NAME);
        if (loader == null || (!loader.isFromRemote() && adPlace != null)) {
            loader = createAdPlaceLoader(pidName, adPlace, adids);
            if (loader != null) {
                mAdLoaders.remove(pidName);
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
            adPlace = mAdConfig.get(pidName);
        } else {
            useRemote = true;
        }
        if (adIds == null) {
            adIds = mAdConfig.getAdIds();
        }
        Log.v(Log.TAG, "pidName : " + pidName + " , adPlace : " + adPlace);
        if (adPlace != null) {
            loader = new AdPlaceLoader(mContext);
            loader.setAdPlaceConfig(adPlace);
            loader.setAdIds(adIds);
            loader.init();
            loader.setFromRemote(useRemote);
        }
        return loader;
    }

    public void onFire() {
        OuterAdLoader.get(mContext).onFire();
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

    public void loadInterstitial(Activity activity, String pidName) {
        loadInterstitial(activity, pidName, null);
    }

    public void loadInterstitial(Activity activity, String pidName, OnAdAggsListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
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

    public void loadAdView(String pidName, OnAdAggsListener l) {
        loadAdView(pidName, null, l);
    }

    public void loadAdView(String pidName, Map<String, Object> extra) {
        loadAdView(pidName, extra, null);
    }

    public void loadAdView(String pidName, Map<String, Object> extra, OnAdAggsListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadAdView(extra);
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

    public void loadComplexAds(String pidName, OnAdAggsListener l) {
        loadComplexAds(pidName, null, l);
    }

    public void loadComplexAds(String pidName, Map<String, Object> extra, OnAdAggsListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
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
            loader.destroy();
        }
    }
}
