package com.inner.adaggs.framework;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.inner.adaggs.AdExtra;
import com.inner.adaggs.adloader.adfb.FBLoader;
import com.inner.adaggs.adloader.admob.AdmobLoader;
import com.inner.adaggs.adloader.adx.AdxLoader;
import com.inner.adaggs.adloader.listener.IAdLoader;
import com.inner.adaggs.adloader.listener.IManagerListener;
import com.inner.adaggs.adloader.listener.OnAdBaseListener;
import com.inner.adaggs.adloader.listener.SimpleAdBaseBaseListener;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个广告位对应一个AdPlaceLoader对象
 */

public class AdPlaceLoader implements IManagerListener {
    private List<IAdLoader> mAdLoaders = new ArrayList<IAdLoader>();
    private AdPlace mAdPlace;
    private Context mContext;
    private OnAdAggsListener mOnAdAggsListener;
    private Map<String, Object> mAdExtra;
    // banner和native的listener集合
    private Map<IAdLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<IAdLoader, OnAdBaseListener>();

    public AdPlaceLoader(Context context) {
        mContext = context;
    }

    public void init(Map<String, String> adids) {
        generateLoaders(adids);
    }

    public void setAdPlaceConfig(AdPlace config) {
        mAdPlace = config;
    }

    private void generateLoaders(Map<String, String> adids) {
        if (mAdPlace != null) {
            List<PidConfig> pidList = mAdPlace.getPidsList();
            if (pidList != null && !pidList.isEmpty()) {
                IAdLoader loader = null;
                String adId = null;
                for (PidConfig config : pidList) {
                    if (config != null) {
                        if (adids != null && !adids.isEmpty()) {
                            adId = adids.get(config.getSdk());
                        }
                        if (config.isAdmob()) {
                            loader = new AdmobLoader();
                            loader.init(mContext, adId);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            mAdLoaders.remove(loader);
                            mAdLoaders.add(loader);
                        } else if (config.isFB()) {
                            loader = new FBLoader();
                            loader.init(mContext, adId);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            mAdLoaders.remove(loader);
                            mAdLoaders.add(loader);
                        } else if (config.isAdx()) {
                            loader = new AdxLoader();
                            loader.init(mContext, adId);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            mAdLoaders.remove(loader);
                            mAdLoaders.add(loader);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据SDK名字获取banner大小
     *
     * @param loader
     * @return
     */
    private int getBannerSize(IAdLoader loader) {
        try {
            return (int) mAdExtra.get(loader.getSdkName() + AdExtra.BANNER_SIZE_SUFFIX);
        } catch (Exception e) {
        }
        return Constant.NOSET;
    }

    /**
     * 根据SDK获取rootview
     *
     * @param loader
     * @return
     */
    private View getRootView(IAdLoader loader) {
        try {
            return (View) mAdExtra.get(loader.getSdkName() + AdExtra.ROOT_VIEW_SUFFIX);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取模板ID
     * @param loader
     * @return
     */
    private int getTemplateId(IAdLoader loader) {
        try {
            return (int) mAdExtra.get(loader.getSdkName() + AdExtra.TEMPLATE_SUFFIX);
        } catch (Exception e) {
        }
        return Constant.NOSET;
    }

    /**
     * 设置外部监听器
     *
     * @param l
     */
    public void setOnAdAggsListener(OnAdAggsListener l) {
        mOnAdAggsListener = l;
    }

    public boolean isInterstitialLoaded() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.isInterstitialLoaded()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 加载插屏
     */
    public void loadInterstitial() {
        if (mAdPlace == null) {
            return;
        }
        if (mAdPlace.isConcurrent()) {
            loadInterstitialConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadInterstitialSequence();
        } else if (mAdPlace.isRandom()) {
            loadInterstitialRandom();
        } else {
            loadInterstitialConcurrent();
        }
    }

    private void loadInterstitialConcurrent() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.loadInterstitial();
                }
            }
        }
    }

    private void loadInterstitialSequence() {
        if (mAdLoaders != null) {
            // 使用迭代器处理
            final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
            loadInterstitialSequenceInternal(iterator);
        }
    }

    private void loadInterstitialRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            IAdLoader loader = mAdLoaders.get(pos);
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener));
            loader.loadInterstitial();
        }
    }

    private void loadInterstitialSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener) {
            @Override
            public void onInterstitialError() {
                if (iterator.hasNext()) {
                    Log.e(Log.TAG, "load next interstitial");
                    loadInterstitialSequenceInternal(iterator);
                } else {
                    super.onInterstitialError();
                }
            }
        });
        loader.loadInterstitial();
    }

    /**
     * 展示插屏
     */
    public void showInterstitial() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isInterstitialLoaded() && loader.showInterstitial()) {
                        return;
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////

    public boolean isAdViewLoaded() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null &&
                        (loader.isBannerLoaded() || loader.isNativeLoaded())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 加载banner和native广告
     * @param extra
     */
    public void loadAdView(Map<String, Object> extra) {
        if (mAdPlace == null) {
            return;
        }
        mAdExtra = extra;
        if (mAdPlace.isConcurrent()) {
            loadAdViewConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadAdViewSequence();
        } else if (mAdPlace.isRandom()) {
            loadAdViewRandom();
        } else {
            loadAdViewConcurrent();
        }
    }

    private void loadAdViewConcurrent() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                            mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(getRootView(loader), getTemplateId(loader));
                    }
                }
            }
        }
    }

    private void loadAdViewSequence() {
        final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
        loadAdViewSequenceInternal(iterator);
    }

    private void loadAdViewRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            IAdLoader loader = mAdLoaders.get(pos);
            if (loader != null) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(getRootView(loader), getTemplateId(loader));
                }
            }
        }
    }

    private void loadAdViewSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                    mOnAdAggsListener) {
                @Override
                public void onAdFailed() {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next adview");
                        loadAdViewSequenceInternal(iterator);
                    } else {
                        super.onAdFailed();
                    }
                }
            });
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getRootView(loader), getTemplateId(loader));
            }
        }
    }

    /**
     * 展示广告(banner or native)
     * @param adContainer
     */
    public void showAdView(ViewGroup adContainer) {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isBannerLoaded()) {
                        loader.showBanner(adContainer);
                        return;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer);
                        return;
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // 加载应用外

    /**
     * 混合广告是否加载成功
     * @return
     */
    public boolean isMixedAdsLoaded() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null
                        && (loader.isBannerLoaded()
                        || loader.isNativeLoaded()
                        || loader.isInterstitialLoaded())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取已加载的广告类型(banner, native, interstitial)
     * @return
     */
    public String getLoadedType() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    boolean loaded = loader.isBannerLoaded() || loader.isNativeLoaded() || loader.isInterstitialLoaded();
                    if (loaded) {
                        return loader.getAdType();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 加载混合广告
     * @param extra
     */
    public void loadMixedAds(Map<String, Object> extra) {
        if (mAdPlace == null) {
            return;
        }
        mAdExtra = extra;
        if (mAdPlace.isConcurrent()) {
            loadMixedAdsConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadMixedAdsSequence();
        } else if (mAdPlace.isRandom()) {
            loadMixedAdsRandom();
        } else {
            loadMixedAdsConcurrent();
        }
    }

    private void loadMixedAdsConcurrent() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                            mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(getRootView(loader), getTemplateId(loader));
                    } else if (loader.isInterstitialType()) {
                        loader.loadInterstitial();
                    }
                }
            }
        }
    }

    private void loadMixedAdsSequence() {
        final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
        loadMixedAdsSequenceInternal(iterator);
    }

    private void loadMixedAdsSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                    mOnAdAggsListener) {
                @Override
                public void onAdFailed() {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next mixed");
                        loadMixedAdsSequenceInternal(iterator);
                    } else {
                        super.onAdFailed();
                    }
                }

                @Override
                public void onInterstitialError() {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next mixed");
                        loadMixedAdsSequenceInternal(iterator);
                    } else {
                        super.onInterstitialError();
                    }
                }
            });
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getRootView(loader), getTemplateId(loader));
            } else if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            }
        }
    }

    private void loadMixedAdsRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            IAdLoader loader = mAdLoaders.get(pos);
            if (loader != null) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getPidName(), loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(getRootView(loader), getTemplateId(loader));
                } else if (loader.isInterstitialType()) {
                    loader.loadInterstitial();
                }
            }
        }
    }

    /**
     * 展示混合广告
     * @param adContainer
     */
    public void showMixedAds(ViewGroup adContainer) {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.useAndClearFlag()) {
                    if (loader.isBannerLoaded()) {
                        loader.showBanner(adContainer);
                        return;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer);
                        return;
                    } else if (loader.isInterstitialLoaded()) {
                        loader.showInterstitial();
                        return;
                    }
                }
            }
        }
    }

    public void resume() {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.resume();
                }
            }
        }
    }

    public void pause() {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.pause();
                }
            }
        }
    }

    public void destroy() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.destroy();
                }
            }
        }
    }

    @Override
    public synchronized void registerAdBaseListener(IAdLoader loader, OnAdBaseListener l) {
        if (mAdViewListener != null) {
            mAdViewListener.put(loader, l);
        }
    }

    @Override
    public synchronized OnAdBaseListener getAdBaseListener(IAdLoader loader) {
        if (mAdViewListener != null) {
            OnAdBaseListener l = mAdViewListener.get(loader);
            return l;
        }
        return null;
    }

    @Override
    public synchronized void clearAdBaseListener(IAdLoader loader) {
        try {
            if (mAdViewListener != null) {
                Iterator<IAdLoader> iterator = mAdViewListener.keySet().iterator();
                while (iterator.hasNext()) {
                    IAdLoader iAdLoader = iterator.next();
                    if (iAdLoader != loader) {
                        mAdViewListener.remove(iAdLoader);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}