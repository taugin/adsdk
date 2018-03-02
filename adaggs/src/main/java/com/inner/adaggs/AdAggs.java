package com.inner.adaggs;

import android.content.Context;
import android.view.ViewGroup;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.AdvInner;
import com.inner.adaggs.framework.AdLoader;
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
    private Map<String, AdLoader> mAdLoaders = new HashMap<String, AdLoader>();
    private AdvInner mAdvInner;
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
        if (mAdConfig != null) {
            mAdvInner = mAdConfig.getAdvInner();
        }
        if (mAdvInner == null) {
            mAdvInner = new AdvInner();
        }
    }

    private AdLoader getAdLoader(String pidName) {
        AdLoader loader = mAdLoaders.get(pidName);
        if (loader == null && mAdvInner != null) {
            loader = new AdLoader(mContext);
            AdPlace config = mAdvInner.get(pidName);
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
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isInterstitialLoaded();
        }
        return false;
    }

    public void loadInterstitial(String pidName) {
        loadInterstitial(pidName, null);
    }

    public void loadInterstitial(String pidName, OnAdAggsListener l) {
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadInterstitial();
        }
    }

    public void showInterstitial(String pidName) {
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showInterstitial();
        }
    }

    public boolean isAdViewLoaded(String pidName) {
        AdLoader loader = getAdLoader(pidName);
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
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadAdView(extra);
        }
    }

    public void showAdView(String pidName, ViewGroup adContainer) {
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showAdView(adContainer);
        }
    }

    public boolean isMixedAdsLoaded(String pidName) {
        AdLoader loader = getAdLoader(pidName);
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
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.setOnAdAggsListener(l);
            loader.loadMixedAds(extra);
        }
    }

    public void showMixedAds(String pidName, ViewGroup adContainer) {
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showMixedAds(adContainer);
        }
    }

    public void destroy(String pidName) {
        AdLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.destroy();
        }
    }
}
