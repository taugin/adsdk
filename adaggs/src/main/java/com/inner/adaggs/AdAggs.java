package com.inner.adaggs;

import android.content.Context;
import android.view.ViewGroup;

import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.framework.AdPlaceLoader;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.parse.AdParser;
import com.inner.adaggs.parse.IParser;
import com.inner.adaggs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdAggs {

    private static AdAggs sAdAggs;

    private Context mContext;
    private IParser mParser;
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
        mParser = new AdParser();
        mAdConfig = mParser.parse(Utils.readAssets(mContext, "adconfig.dat"));
        if (mAdConfig == null) {
            mAdConfig = new AdConfig();
        }
    }

    private AdPlaceLoader getAdLoader(String pidName) {
        AdPlaceLoader loader = mAdLoaders.get(pidName);
        if (loader == null && mAdConfig != null) {
            loader = new AdPlaceLoader(mContext);
            AdPlace config = mAdConfig.get(pidName);
            Log.d(Log.TAG, "config : " + config);
            loader.setAdPlaceConfig(config);
            Map<String, String> adIds = null;
            if (mAdConfig != null) {
                adIds = mAdConfig.getAdIds();
            }
            loader.init(adIds);
            mAdLoaders.put(pidName, loader);
        }
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
        loadInterstitial(pidName, null);
    }

    public void loadInterstitial(String pidName, OnAdAggsListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadInterstitial();
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

    public boolean isMixedAdsLoaded(String pidName) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isMixedAdsLoaded();
        }
        return false;
    }

    public void loadMixedAds(String pidName) {
        loadMixedAds(pidName, null, null);
    }

    public void loadMixedAds(String pidName, OnAdAggsListener l) {
        loadMixedAds(pidName, null, l);
    }

    public void loadMixedAds(String pidName, Map<String, Object> extra, OnAdAggsListener l) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadMixedAds(extra);
        }
    }

    public void showMixedAds(String pidName, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showMixedAds(adContainer);
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
