package com.hauyu.adsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.appub.ads.a.FSA;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.adloader.addfp.AdDfpLoader;
import com.hauyu.adsdk.adloader.admob.AdmobLoader;
import com.hauyu.adsdk.adloader.adx.AdxLoader;
import com.hauyu.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.hauyu.adsdk.adloader.listener.IManagerListener;
import com.hauyu.adsdk.adloader.listener.ISdkLoader;
import com.hauyu.adsdk.adloader.listener.OnAdBaseListener;
import com.hauyu.adsdk.adloader.mopub.MopubLoader;
import com.hauyu.adsdk.adloader.spread.SpLoader;
import com.hauyu.adsdk.config.AdPlace;
import com.hauyu.adsdk.config.PidConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.OnAdSdkListener;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.policy.AdPolicy;
import com.hauyu.adsdk.utils.Utils;

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

public class AdPlaceLoader extends AdBaseLoader implements IManagerListener, Runnable {
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
    private WeakReference<ViewGroup> mAdContainer;
    private ISdkLoader mCurrentAdLoader;
    private String mOriginPidName;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private FSA.MView mMView;

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
    public void setOriginPidName(String pidName) {
        mOriginPidName = pidName;
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
                        if (config.isAdmob() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdmobLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isMopub() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new MopubLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isAdx() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdxLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isDfp() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdDfpLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isSpread() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new SpLoader();
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
            Log.e(Log.TAG, "error : " + e, e);
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

    @Override
    public Activity getActivity() {
        if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
            return mActivity.get();
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
                if (loader != null) {
                    if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
                    if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                        Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
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
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if ((pidList == null || pidList.isEmpty()) && mOnAdSdkListener != null) {
            mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
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
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                            loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                }
            }
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.allowUseLoader()) {
                    if (loader.isRewardedVideoType()) {
                        loader.loadRewardedVideo();
                    } else {
                        loader.loadInterstitial();
                    }
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
                        loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                if (loader.isRewardedVideoType()) {
                    loader.loadRewardedVideo();
                } else {
                    loader.loadInterstitial();
                }
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
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next interstitial");
                        loadInterstitialSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        super.onInterstitialError(error);
                    }
                }
            });
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                loader.loadInterstitial();
            }
        }
    }

    private void loadInterstitialSequenceInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadInterstitialSequenceInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadInterstitialSequenceInternal(iterator);
                }
            }, delay);
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showInterstitial() {
        Log.d(Log.TAG, "showInterstitial");
        showInterstitialInternal();
    }

    private void showInterstitialInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        if (loader.showRewardedVideo()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            break;
                        }
                    } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        if (loader.showInterstitial()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            break;
                        }
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
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if ((pidList == null || pidList.isEmpty()) && mOnAdSdkListener != null) {
            mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            return;
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
                        Log.iv(Log.TAG, "load next adview");
                        loadAdViewSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
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

    private void loadAdViewSequenceInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadAdViewSequenceInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAdViewSequenceInternal(iterator);
                }
            }, delay);
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param adParams
     */
    @Override
    public void showAdView(ViewGroup adContainer, AdParams adParams) {
        Log.d(Log.TAG, "showAdView");
        if (adParams != null) {
            mAdParams = adParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showAdViewInternal(true);
        autoSwitchAdView();
    }

    private void showAdViewInternal(boolean needCounting) {
        Log.d(Log.TAG, "showAdViewInternal");
        if (mAdLoaders != null && mAdContainer != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    ViewGroup viewGroup = null;
                    if (mAdContainer != null) {
                        viewGroup = mAdContainer.get();
                    }
                    if (loader.isBannerLoaded() && viewGroup != null) {
                        mCurrentAdLoader = loader;
                        loader.showBanner(viewGroup);
                        if (needCounting) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        }
                        viewGroup.addView(mMView = new FSA.MView(mContext), 0, 0);
                        break;
                    } else if (loader.isNativeLoaded() && viewGroup != null) {
                        mCurrentAdLoader = loader;
                        loader.showNative(viewGroup, getParams(loader));
                        if (needCounting) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        }
                        viewGroup.addView(mMView = new FSA.MView(mContext), 0, 0);
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
                        || loader.isInterstitialLoaded())
                        || loader.isRewaredVideoLoaded()) {
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
                    boolean loaded = loader.isBannerLoaded()
                            || loader.isNativeLoaded()
                            || loader.isInterstitialLoaded()
                            || loader.isRewaredVideoLoaded();
                    if (loaded) {
                        return loader.getAdType();
                    }
                }
            }
        }
        return null;
    }

    private String getLoadedSdk() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    boolean loaded = loader.isBannerLoaded()
                            || loader.isNativeLoaded()
                            || loader.isInterstitialLoaded()
                            || loader.isRewaredVideoLoaded();
                    if (loaded) {
                        return loader.getSdkName();
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
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if ((pidList == null || pidList.isEmpty()) && mOnAdSdkListener != null) {
            mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            return;
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
                    } else if (loader.isRewardedVideoType()) {
                        loader.loadRewardedVideo();
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
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
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
            } else if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    private void loadComplexAdsSequenceInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadComplexAdsSequenceInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadComplexAdsSequenceInternal(iterator);
                }
            }, delay);
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
                } else if (loader.isRewardedVideoType()) {
                    loader.loadRewardedVideo();
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    /**
     * 检测是否是指定的source和adtype
     *
     * @param loader
     * @param source
     * @param adType
     * @return
     */
    private boolean checkSourceAndType(ISdkLoader loader, String source, String adType) {
        if (loader == null) {
            return false;
        }
        if (!TextUtils.isEmpty(source) && !TextUtils.equals(loader.getSdkName(), source)) {
            return false;
        }
        if (!TextUtils.isEmpty(adType) && !TextUtils.equals(loader.getAdType(), adType)) {
            return false;
        }
        return true;
    }

    /**
     * 展示混合广告
     *
     * @param adContainer
     */
    @Override
    public void showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType) {
        Log.d(Log.TAG, "");
        if (adParams != null) {
            mAdParams = adParams;
        }
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.useAndClearFlag()) {
                    if (!checkSourceAndType(loader, source, adType)) {
                        continue;
                    }
                    if (loader.isBannerLoaded()) {
                        loader.showBanner(adContainer);
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        break;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer, getParams(loader));
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        break;
                    } else if (loader.isInterstitialLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.showInterstitial();
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        break;
                    } else if (loader.isRewaredVideoLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.showRewardedVideo();
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void showComplexAds() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        if (loader.showRewardedVideo()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            break;
                        }
                    } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        if (loader.showInterstitial()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            break;
                        }
                    } else if (loader.isBannerType() && loader.isBannerLoaded()) {
                        show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                        break;
                    } else if (loader.isNativeType() && loader.isNativeLoaded()) {
                        show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                        break;
                    }
                }
            }
        }
    }

    private void show(String pidName, String source, String adType) {
        try {
            Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.AFPICKER");
            if (intent == null) {
                intent = new Intent(mContext, FSA.class);
            }
            intent.putExtra(Intent.EXTRA_TITLE, pidName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public int getAdCount() {
        if (mAdLoaders != null) {
            return mAdLoaders.size();
        }
        return super.getAdCount();
    }

    @Override
    public String getAdMode() {
        if (mAdPlace != null) {
            return mAdPlace.getMode();
        }
        return super.getAdMode();
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
        // clearAdBaseListener();
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

    @Override
    public String getOriginPidName() {
        return mOriginPidName;
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
                if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)
                        || TextUtils.equals(adType, Constant.TYPE_REWARD)) {
                    showInterstitialInternal();
                }
            }
        }
    }

    /**
     * 展示下一个已经加载的AdView
     */
    private void showNextAdView() {
        if (isAdViewLoaded()) {
            showAdViewInternal(false);
        }
    }

    /**
     * 处理场景缓存
     *
     * @return
     */
    private boolean processAdPlaceCache() {
        if (mAdPlace == null) {
            Log.v(Log.TAG, "place is null");
            return false;
        }
        if (!mAdPlace.isNeedCache()) {
            Log.v(Log.TAG, "place no need cache");
            return false;
        }
        if (isInterstitialLoaded() || isAdViewLoaded() || isComplexAdsLoaded()) {
            notifyLoadedWithDelay();
            return true;
        }
        return false;
    }

    private void notifyLoadedWithDelay() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mNotifyRunnable);
            Log.v(Log.TAG, "notify loaded with delay : " + mAdPlace.getDelayNotifyTime());
            mHandler.postDelayed(mNotifyRunnable, mAdPlace.getDelayNotifyTime());
        }
    }

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnAdSdkListener != null) {
                Log.v(Log.TAG, "notify place loaded " + mAdPlace.getName() + " - " + getLoadedSdk() + " - " + getLoadedType());
                notifyAdLoaded();
                mOnAdSdkListener.onLoaded(mAdPlace.getName(), getLoadedSdk(), getLoadedType());
            }
        }
    };

    private void autoSwitchAdView() {
        if (mAdPlace == null) {
            Log.v(Log.TAG, "place is null");
            return;
        }
        if (mAdPlace.getAutoInterval() <= 0) {
            Log.v(Log.TAG, "no need auto switch");
            return;
        }
        if (mHandler == null) {
            Log.v(Log.TAG, "handler is null");
            return;
        }
        mHandler.removeCallbacks(this);
        Log.v(Log.TAG, "wait " + mAdPlace.getAutoInterval() + " ms");
        mHandler.postDelayed(this, mAdPlace.getAutoInterval());
    }

    @Override
    public void run() {
        if (!isAdViewLoaded()) {
            Log.v(Log.TAG, "ai not loaded");
            return;
        }

        ViewGroup viewGroup = null;
        if (mAdContainer != null) {
            viewGroup = mAdContainer.get();
        }
        if (viewGroup == null) {
            Log.v(Log.TAG, "ai empty view group");
            return;
        }

        if (mMView == null || !mMView.isViewVisible()) {
            Log.v(Log.TAG, "ai not visible");
            return;
        }
        resume();
        showNextAdView();
        autoSwitchAdView();
    }
}