package com.inner.adaggs;

import android.content.Context;
import android.view.ViewGroup;

import com.inner.adaggs.config.AdInners;
import com.inner.adaggs.config.AdPlaceConfig;
import com.inner.adaggs.framework.AdLoader;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.parse.IParser;
import com.inner.adaggs.parse.JsonParser;

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
    private AdInners mAdInners;

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
        mParser = new JsonParser();
        mAdInners = mParser.parse(null);
    }

    private AdLoader getAdLoader(String pidName) {
        AdLoader loader = mAdLoaders.get(pidName);
        if (loader == null) {
            loader = new AdLoader(mContext);
            AdPlaceConfig config = mAdInners.get(pidName);
            Log.d(Log.TAG, "config : " + config);
            loader.setAdPlaceConfig(config);
            loader.init();
            mAdLoaders.put(pidName, loader);
        }
        return loader;
    }

    public boolean isInterstitialLoaded(String pidName) {
        AdLoader loader = getAdLoader(pidName);
        return loader.isInterstitialLoaded();
    }

    public void loadInterstitial(String pidName) {
        loadInterstitial(pidName, null);
    }

    public void loadInterstitial(String pidName, OnAdAggsListener l) {
        AdLoader loader = getAdLoader(pidName);
        loader.setOnAdAggsListener(l);
        loader.loadInterstitial();
    }

    public void showInterstitial(String pidName) {
        AdLoader loader = getAdLoader(pidName);
        loader.showInterstitial();
    }

    public boolean isAdViewLoaded(String pidName) {
        AdLoader loader = getAdLoader(pidName);
        return loader.isAdViewLoaded();
    }

    public void loadAdView(String pidName, OnAdAggsListener l) {
        loadAdView(pidName, l, null);
    }

    public void loadAdView(String pidName, Map<String, Object> extra) {
        loadAdView(pidName, null, extra);
    }

    public void loadAdView(String pidName, OnAdAggsListener l, Map<String, Object> extra) {
        AdLoader loader = getAdLoader(pidName);
        loader.setOnAdAggsListener(l);
        loader.loadAdView(extra);
    }

    public void showAdView(String pidName, ViewGroup adContainer) {
        AdLoader loader = getAdLoader(pidName);
        loader.showAdView(adContainer);
    }

    public void destroy() {
    }
}
