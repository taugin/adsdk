package com.inner.adaggs.framework;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.inner.adaggs.AdParams;
import com.inner.adaggs.adloader.adfb.FBLoader;
import com.inner.adaggs.adloader.admob.AdmobLoader;
import com.inner.adaggs.adloader.adx.AdxLoader;
import com.inner.adaggs.adloader.base.SimpleAdBaseBaseListener;
import com.inner.adaggs.adloader.listener.IAdLoader;
import com.inner.adaggs.adloader.listener.IManagerListener;
import com.inner.adaggs.adloader.listener.OnAdBaseListener;
import com.inner.adaggs.config.AdPlace;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;

import java.lang.ref.WeakReference;
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
    private Map<String, String> mAdIds;
    private Context mContext;
    private OnAdAggsListener mOnAdAggsListener;
    private Params mParams;
    private boolean mFromRemote = false;
    // banner和native的listener集合
    private Map<IAdLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<IAdLoader, OnAdBaseListener>();
    private WeakReference<Activity> mActivity;

    public AdPlaceLoader(Context context) {
        mContext = context;
    }

    public void init() {
        generateLoaders();
    }

    public void setAdPlaceConfig(AdPlace adPlace) {
        mAdPlace = adPlace;
    }

    public void setAdIds(Map<String, String> adids) {
        mAdIds = adids;
    }

    private void generateLoaders() {
        if (mAdPlace != null) {
            List<PidConfig> pidList = mAdPlace.getPidsList();
            if (pidList != null && !pidList.isEmpty()) {
                IAdLoader loader = null;
                String adId = null;
                for (PidConfig config : pidList) {
                    if (config != null) {
                        if (mAdIds != null && !mAdIds.isEmpty()) {
                            adId = mAdIds.get(config.getSdk());
                        }
                        if (config.isAdmob()) {
                            loader = new AdmobLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowLoad()) {
                                mAdLoaders.remove(loader);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isFB()) {
                            loader = new FBLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowLoad()) {
                                mAdLoaders.remove(loader);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isAdx()) {
                            loader = new AdxLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowLoad()) {
                                mAdLoaders.remove(loader);
                                mAdLoaders.add(loader);
                            }
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
            Map<String, Integer> map = mParams.getBannerSize();
            return (int) map.get(loader.getSdkName());
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
                    Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 加载插屏
     */
    public void loadInterstitial(Activity activity) {
        if (mAdPlace == null) {
            return;
        }
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
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
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.allowLoad()) {
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
            if (loader != null && loader.allowLoad()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener));
                loader.loadInterstitial();
            }
        }
    }

    private void loadInterstitialSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null && loader.allowLoad()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), Constant.TYPE_INTERSTITIAL, mOnAdAggsListener) {
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
                    Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 加载banner和native广告
     * @param adParams
     */
    public void loadAdView(AdParams adParams) {
        if (mAdPlace == null) {
            return;
        }
        if (adParams != null) {
            mParams = adParams.getParams();
        }
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
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
                            mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.allowLoad()) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(mParams);
                    } else {
                        Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
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
            if (loader != null && loader.allowLoad()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(mParams);
                } else {
                    Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
                }
            }
        }
    }

    private void loadAdViewSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null && loader.allowLoad()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
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
                loader.loadNative(mParams);
            } else {
                Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType());
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
                    } else {
                        Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
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
    public boolean isComplexAdsLoaded() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.hasLoadedFlag()
                        && (loader.isBannerLoaded()
                        || loader.isNativeLoaded()
                        || loader.isInterstitialLoaded())) {
                    Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
     * @param adParams
     */
    public void loadComplexAds(AdParams adParams) {
        if (mAdPlace == null) {
            return;
        }
        if (adParams != null) {
            mParams = adParams.getParams();
        }
        if (mAdPlace.isConcurrent()) {
            loadComplexAdsConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadComplexAdsSequence();
        } else if (mAdPlace.isRandom()) {
            loadComplexAdsRandom();
        } else {
            loadComplexAdsConcurrent();
        }
    }

    private void loadComplexAdsConcurrent() {
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
                            mOnAdAggsListener));
                }
            }
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.allowLoad()) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(mParams);
                    } else if (loader.isInterstitialType()) {
                        loader.loadInterstitial();
                    } else {
                        Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
                    }
                }
            }
        }
    }

    private void loadComplexAdsSequence() {
        final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
        loadComplexAdsSequenceInternal(iterator);
    }

    private void loadComplexAdsSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null && loader.allowLoad()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
                    mOnAdAggsListener) {
                @Override
                public void onAdFailed() {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        super.onAdFailed();
                    }
                }

                @Override
                public void onInterstitialError() {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        super.onInterstitialError();
                    }
                }
            });
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(mParams);
            } else if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
            }
        }
    }

    private void loadComplexAdsRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            IAdLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowLoad()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(mParams);
                } else if (loader.isInterstitialType()) {
                    loader.loadInterstitial();
                } else {
                    Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
                }
            }
        }
    }

    /**
     * 展示混合广告
     * @param adContainer
     */
    public void showComplexAds(ViewGroup adContainer) {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (IAdLoader loader : mAdLoaders) {
                if (loader != null && loader.useAndClearFlag()) {
                    if (loader.isBannerLoaded()) {
                        loader.showBanner(adContainer);
                        break;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer);
                        break;
                    } else if (loader.isInterstitialLoaded()) {
                        loader.showInterstitial();
                        break;
                    } else {
                        Log.d(Log.TAG, "incorrect type : " + loader.getAdPlaceName() + " - " + loader.getAdType() + " - " + loader.getSdkName());
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

    public boolean isFromRemote() {
        return mFromRemote;
    }

    public void setFromRemote(boolean remote) {
        mFromRemote = remote;
    }
}