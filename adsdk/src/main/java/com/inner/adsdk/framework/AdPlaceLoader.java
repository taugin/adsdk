package com.inner.adsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.inner.adsdk.AdParams;
import com.inner.adsdk.adloader.adfb.FBLoader;
import com.inner.adsdk.adloader.admob.AdmobLoader;
import com.inner.adsdk.adloader.adx.AdxLoader;
import com.inner.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.ISdkLoader;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.adloader.wemob.WemobLoader;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.OnAdSdkListener;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.policy.AdPolicy;

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

public class AdPlaceLoader extends AdBaseLoader implements IManagerListener {
    private List<ISdkLoader> mAdLoaders = new ArrayList<ISdkLoader>();
    private AdPlace mAdPlace;
    private Map<String, String> mAdIds;
    private Context mContext;
    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mOnAdPlaceLoaderListener = new AdPlaceLoaderListener();
    private AdParams mAdParams;
    private boolean mHasNotifyLoaded = false;
    // banner和native的listener集合
    private Map<ISdkLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<ISdkLoader, OnAdBaseListener>();
    private WeakReference<Activity> mActivity;
    private ViewGroup mAdContainer;
    private ISdkLoader mCurrentAdLoader;

    public AdPlaceLoader(Context context) {
        mContext = context;
    }

    @Override
    public void init() {
        generateLoaders();
    }

    @Override
    public void setAdPlaceConfig(AdPlace adPlace) {
        mAdPlace = adPlace;
    }

    @Override
    public void setAdIds(Map<String, String> adids) {
        mAdIds = adids;
    }

    @Override
    public boolean needReload(AdPlace adPlace) {
        if (mAdPlace != null && adPlace != null) {
            Log.d(Log.TAG, "pidName : " + mAdPlace.getName() + " , usingUnique : " + mAdPlace.getUniqueValue() + " , remoteUnique : " + adPlace.getUniqueValue());
            return !TextUtils.equals(mAdPlace.getUniqueValue(), adPlace.getUniqueValue());
        }
        return false;
    }

    private void generateLoaders() {
        if (mAdPlace != null) {
            List<PidConfig> pidList = mAdPlace.getPidsList();
            if (pidList != null && !pidList.isEmpty()) {
                ISdkLoader loader = null;
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
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isFB()) {
                            loader = new FBLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isAdx()) {
                            loader = new AdxLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isWemob()) {
                            loader = new WemobLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        }
                    }
                }
            }
        }
    }

    private Params getParams(ISdkLoader loader) {
        Params params = null;
        try {
            params = mAdParams.getParams(loader.getSdkName());
            if (params == null) {
                params = mAdParams.getParams(Constant.AD_SDK_COMMON);
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, new Throwable());
        }
        return params;
    }

    /**
     * 根据SDK名字获取banner大小
     *
     * @param loader
     * @return
     */
    private int getBannerSize(ISdkLoader loader) {
        try {
            Map<String, Integer> map = getParams(loader).getBannerSize();
            return (int) map.get(loader.getSdkName());
        } catch (Exception e) {
        }
        return Constant.NOSET;
    }

    private String getPidByLoader(ISdkLoader loader) {
        try {
            return loader.getPidConfig().getPid();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 设置外部监听器
     *
     * @param l
     */
    @Override
    public void setOnAdSdkListener(OnAdSdkListener l) {
        mOnAdSdkListener = l;
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
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
    @Override
    public void loadInterstitial(Activity activity) {
        if (mAdPlace == null) {
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            return;
        }
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        mCurrentAdLoader = null;
        mHasNotifyLoaded = false;
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
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                            loader.getSdkName(), Constant.TYPE_INTERSTITIAL, getPidByLoader(loader), this));
                }
            }
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.allowUseLoader()) {
                    loader.loadInterstitial();
                }
            }
        }
    }

    private void loadInterstitialSequence() {
        if (mAdLoaders != null) {
            // 使用迭代器处理
            final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
            loadInterstitialSequenceInternal(iterator);
        }
    }

    private void loadInterstitialRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            ISdkLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowUseLoader()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                        loader.getSdkName(), Constant.TYPE_INTERSTITIAL, getPidByLoader(loader), this));
                loader.loadInterstitial();
            }
        }
    }

    private void loadInterstitialSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), Constant.TYPE_INTERSTITIAL, getPidByLoader(loader), this) {
                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next interstitial");
                        loadInterstitialSequenceInternal(iterator);
                    } else {
                        super.onInterstitialError(error);
                    }
                }
            });
            loader.loadInterstitial();
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showInterstitial() {
        Log.d(Log.TAG, "showInterstitial");
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isInterstitialLoaded() && loader.showInterstitial()) {
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public boolean isAdViewLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
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
     *
     * @param adParams
     */
    @Override
    public void loadAdView(AdParams adParams) {
        if (mAdPlace == null) {
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            return;
        }
        mAdParams = adParams;
        mCurrentAdLoader = null;
        mHasNotifyLoaded = false;
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
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                            loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                }
            }
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.allowUseLoader()) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(getParams(loader));
                    } else {
                        Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                    }
                }
            }
        }
    }

    private void loadAdViewSequence() {
        final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
        loadAdViewSequenceInternal(iterator);
    }

    private void loadAdViewRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            ISdkLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowUseLoader()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                        loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(getParams(loader));
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    private void loadAdViewSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next adview");
                        loadAdViewSequenceInternal(iterator);
                    } else {
                        super.onAdFailed(error);
                    }
                }
            });
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     */
    @Override
    public void showAdView(ViewGroup adContainer) {
        Log.d(Log.TAG, "showAdView");
        mAdContainer = adContainer;
        showAdViewInternal();
    }

    private void showAdViewInternal() {
        Log.d(Log.TAG, "showAdViewInternal");
        if (mAdLoaders != null && mAdContainer != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isBannerLoaded()) {
                        mCurrentAdLoader = loader;
                        loader.showBanner(mAdContainer);
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    } else if (loader.isNativeLoaded()) {
                        mCurrentAdLoader = loader;
                        loader.showNative(mAdContainer);
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // 加载应用外

    /**
     * 混合广告是否加载成功
     *
     * @return
     */
    @Override
    public boolean isComplexAdsLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
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
     *
     * @return
     */
    @Override
    public String getLoadedType() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
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
     *
     * @param adParams
     */
    @Override
    public void loadComplexAds(AdParams adParams) {
        if (mAdPlace == null) {
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            return;
        }
        mAdParams = adParams;
        mCurrentAdLoader = null;
        mHasNotifyLoaded = false;
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
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                            loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                }
            }
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.allowUseLoader()) {
                    if (loader.isBannerType()) {
                        loader.loadBanner(getBannerSize(loader));
                    } else if (loader.isNativeType()) {
                        loader.loadNative(getParams(loader));
                    } else if (loader.isInterstitialType()) {
                        loader.loadInterstitial();
                    } else {
                        Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                    }
                }
            }
        }
    }

    private void loadComplexAdsSequence() {
        final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
        loadComplexAdsSequenceInternal(iterator);
    }

    private void loadComplexAdsSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        super.onInterstitialError(error);
                    }
                }
            });
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    private void loadComplexAdsRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            ISdkLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowUseLoader()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                        loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(getParams(loader));
                } else if (loader.isInterstitialType()) {
                    loader.loadInterstitial();
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    /**
     * 展示混合广告
     *
     * @param adContainer
     */
    @Override
    public void showComplexAds(ViewGroup adContainer) {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.useAndClearFlag()) {
                    if (loader.isBannerLoaded()) {
                        loader.showBanner(adContainer);
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer);
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    } else if (loader.isInterstitialLoaded()) {
                        loader.showInterstitial();
                        AdPolicy.get(mContext).reportAdPlaceShow(mAdPlace);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void resume() {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.resume();
                }
            }
        }
    }

    @Override
    public void pause() {
        Log.d(Log.TAG, "");
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.pause();
                }
            }
        }
    }

    @Override
    public void destroy() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    loader.destroy();
                }
            }
        }
        clearAdBaseListener();
    }

    @Override
    public synchronized void registerAdBaseListener(ISdkLoader loader, OnAdBaseListener l) {
        if (mAdViewListener != null) {
            mAdViewListener.put(loader, l);
        }
    }

    @Override
    public synchronized OnAdBaseListener getAdBaseListener(ISdkLoader loader) {
        OnAdBaseListener listener = null;
        if (mAdViewListener != null) {
            listener = mAdViewListener.get(loader);
        }
        return listener;
    }

    @Override
    public OnAdSdkListener getOnAdSdkListener() {
        return mOnAdSdkListener;
    }

    @Override
    public OnAdSdkListener getOnAdPlaceLoaderListener() {
        return mOnAdPlaceLoaderListener;
    }

    @Override
    public synchronized void setLoader(ISdkLoader adLoader) {
        if (mCurrentAdLoader == null) {
            mCurrentAdLoader = adLoader;
        }
    }

    @Override
    public boolean hasNotifyLoaded() {
        if (mAdPlace != null && mAdPlace.isLoadOnlyOnce()) {
            return mHasNotifyLoaded;
        }
        return false;
    }

    @Override
    public void notifyAdLoaded() {
        mHasNotifyLoaded = true;
    }

    @Override
    public boolean isCurrent(String source, String type, String pidName) {
        boolean isCurrentLoader = false;
        if (mCurrentAdLoader != null) {
            isCurrentLoader = TextUtils.equals(mCurrentAdLoader.getSdkName(), source)
                    && TextUtils.equals(mCurrentAdLoader.getAdType(), type)
                    && TextUtils.equals(getPidByLoader(mCurrentAdLoader), pidName);
        }
        return isCurrentLoader;
    }

    private void clearAdBaseListener() {
        try {
            if (mAdViewListener != null) {
                mAdViewListener.clear();
            }
        } catch (Exception e) {
        }
    }

    /**
     * AdPlaceLoader类使用的监听器
     */
    private class AdPlaceLoaderListener extends SimpleAdSdkListener {
        @Override
        public void onClick(String pidName, String source, String adType) {
            if (mAdPlace != null && mAdPlace.isAutoSwitch()) {
                Log.d(Log.TAG, "adplaceloader pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                if (TextUtils.equals(adType, Constant.TYPE_NATIVE) || TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                    resume();
                    showNextAdView();
                }
            }
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            if (mAdPlace != null && mAdPlace.isAutoSwitch()) {
                Log.d(Log.TAG, "adplaceloader pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)) {
                    showInterstitial();
                }
            }
        }
    }

    /**
     * 展示下一个已经加载的AdView
     */
    private void showNextAdView() {
        if (isAdViewLoaded()) {
            showAdViewInternal();
        }
    }
}