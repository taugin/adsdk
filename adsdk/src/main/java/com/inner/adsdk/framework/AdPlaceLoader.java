package com.inner.adsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.appub.ads.a.FSA;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.adloader.adcolony.AdColonyLoader;
import com.inner.adsdk.adloader.addfp.AdDfpLoader;
import com.inner.adsdk.adloader.adfb.FBLoader;
import com.inner.adsdk.adloader.admob.AdmobLoader;
import com.inner.adsdk.adloader.adx.AdxLoader;
import com.inner.adsdk.adloader.applovin.AppLovinLoader;
import com.inner.adsdk.adloader.applovinmax.AppLovinMaxLoader;
import com.inner.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.inner.adsdk.adloader.dispio.DisplayIoLoader;
import com.inner.adsdk.adloader.dspmob.DspMobLoader;
import com.inner.adsdk.adloader.inmobi.InmobiLoader;
import com.inner.adsdk.adloader.inneractive.InnerActiveLoader;
import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.ISdkLoader;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.adloader.mopub.MopubLoader;
import com.inner.adsdk.adloader.spread.SpLoader;
import com.inner.adsdk.adloader.wemob.WemobLoader;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.listener.OnAdSdkListener;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.InternalStat;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个广告位对应一个AdPlaceLoader对象
 */

public class AdPlaceLoader extends AdBaseLoader implements IManagerListener, Runnable, Handler.Callback {
    private List<ISdkLoader> mAdLoaders = new ArrayList<ISdkLoader>();
    private AdPlace mAdPlace;
    private Map<String, String> mAdIds;
    private Context mContext;
    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mOnAdSdkLoadedListener;
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
    private static List<ISdkLoader> sLoadedAdLoaders = new ArrayList<ISdkLoader>();
    private boolean mAdPlaceSeqLoading = false;
    private boolean mQueueRunning = true;

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
                        } else if (config.isFB() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new FBLoader();
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
                        } else if (config.isWemob() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new WemobLoader();
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
                        } else if (config.isAppLovin() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AppLovinLoader();
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
                        } else if (config.isSpread() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new SpLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isInmobi() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new InmobiLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isInnerActive() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new InnerActiveLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isDspMob() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new DspMobLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isDisplayIo() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new DisplayIoLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isAdColony() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdColonyLoader();
                            loader.init(mContext);
                            loader.setPidConfig(config);
                            loader.setListenerManager(this);
                            loader.setAdId(adId);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isAppLovinMax() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AppLovinMaxLoader();
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
            if (mAdParams != null) {
                params = mAdParams.getParams(loader.getSdkName());
                if (params == null) {
                    params = mAdParams.getParams(Constant.AD_SDK_COMMON);
                }
            }
            if (params == null) {
                params = new Params();
                params.setAdCardStyle(Constant.NATIVE_CARD_FULL);
                params.setBannerSize(Constant.AD_SDK_ADMOB, Constant.MEDIUM_RECTANGLE);
                params.setBannerSize(Constant.AD_SDK_ADX, Constant.MEDIUM_RECTANGLE);
                params.setBannerSize(Constant.AD_SDK_DFP, Constant.MEDIUM_RECTANGLE);
                params.setBannerSize(Constant.AD_SDK_FACEBOOK, Constant.MEDIUM_RECTANGLE);
                params.setBannerSize(Constant.AD_SDK_INMOBI, Constant.MEDIUM_RECTANGLE);
                params.setBannerSize(Constant.AD_SDK_DSPMOB, Constant.MEDIUM_RECTANGLE);
                Log.v(Log.TAG, "use default ad params");
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
        // 预先从配置中获取banner大小
        if (loader != null) {
            PidConfig pidConfig = loader.getPidConfig();
            if (pidConfig != null) {
                String bannerSize = pidConfig.getBannerSize();
                if (!TextUtils.isEmpty(bannerSize)) {
                    Constant.Banner banner = null;
                    try {
                        banner = Constant.Banner.valueOf(bannerSize);
                    } catch (Exception e) {
                    }
                    if (banner != null) {
                        return banner.value();
                    }
                }
            }
        }

        if (mAdPlace != null) {
            String bannerSize = mAdPlace.getBannerSize();
            if (!TextUtils.isEmpty(bannerSize)) {
                Constant.Banner banner = null;
                try {
                    banner = Constant.Banner.valueOf(bannerSize);
                } catch (Exception e) {
                }
                if (banner != null) {
                    return banner.value();
                }
            }
        }

        try {
            Map<String, Integer> map = getParams(loader).getBannerSize();
            Integer banner = map.get(loader.getSdkName());
            if (banner != null) {
                return banner.intValue();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }

        try {
            Map<String, Integer> map = getParams(loader).getBannerSize();
            Integer banner = map.get(Constant.AD_SDK_COMMON);
            if (banner != null) {
                return banner.intValue();
            }
        } catch (Exception e2) {
            Log.e(Log.TAG, "error : " + e2);
        }
        return Constant.NOSET;
    }

    /**
     * 获取通用的banner大小
     *
     * @return
     */
    private int getCommonBannerSize() {
        if (mAdPlace != null) {
            String bannerSize = mAdPlace.getBannerSize();
            if (!TextUtils.isEmpty(bannerSize)) {
                Constant.Banner banner = null;
                try {
                    banner = Constant.Banner.valueOf(bannerSize);
                } catch (Exception e) {
                }
                if (banner != null) {
                    return banner.value();
                }
            }
        }

        if (mAdParams != null) {
            Params params = mAdParams.getParams(Constant.AD_SDK_COMMON);
            if (params != null) {
                Map<String, Integer> bannerMap = params.getBannerSize();
                if (bannerMap != null && bannerMap.containsKey(Constant.AD_SDK_COMMON)) {
                    Integer integer = bannerMap.get(Constant.AD_SDK_COMMON);
                    if (integer != null) {
                        return integer.intValue();
                    }
                }
            }
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
    public void setOnAdSdkListener(OnAdSdkListener l, boolean loaded) {
        if (loaded) {
            mOnAdSdkLoadedListener = l;
        } else {
            mOnAdSdkListener = l;
        }
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
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null, null);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
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

        if (mAdPlace.isConcurrent()) {
            loadInterstitialConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadInterstitialSequence();
        } else if (mAdPlace.isRandom()) {
            loadInterstitialRandom();
        } else if (mAdPlace.isQueue()) {
            loadInterstitialQueue();
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
                    } else if (loader.isInterstitialType()) {
                        loader.loadInterstitial();
                    } else {
                        Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                    }
                }
            }
        }
    }

    private void loadInterstitialSequence() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            // 使用迭代器处理
            if (!isAdPlaceSeqLoading()) {
                setAdPlaceSeqLoading(true, SeqState.REQUEST);
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadInterstitialSequenceInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " seq is loading ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
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
                } else if (loader.isInterstitialType()) {
                    loader.loadInterstitial();
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next interstitial");
                        loadInterstitialSequenceInternal(iterator);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onInterstitialLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onInterstitialLoaded(loader);
                }

                @Override
                public void onRewardedVideoAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onRewardedVideoAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 使用que模式进行加载，核心，加载够指定个数
     */
    private void loadInterstitialQueue() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isInterstitialReachToCoreCount()) {
                if (!isAdPlaceSeqLoading()) {
                    setAdPlaceSeqLoading(true, SeqState.REQUEST);
                    final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                    loadInterstitialQueueInternal(iterator);
                } else {
                    Log.iv(Log.TAG, mAdPlace.getName() + " que is loading ...");
                }
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " has enough ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
        }
    }

    private void loadInterstitialQueueInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onInterstitialDismiss() {
                    super.onInterstitialDismiss();
                    if (!isInterstitialReachToCoreCount()) {
                        loadInterstitialQueue();
                    }
                }

                @Override
                public void onAdDismiss() {
                    super.onAdDismiss();
                    if (!isInterstitialReachToCoreCount()) {
                        loadInterstitialQueue();
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    super.onRewardedVideoAdClosed();
                    if (!isInterstitialReachToCoreCount()) {
                        loadInterstitialQueue();
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next interstitial in que");
                        loadInterstitialQueueInternalWithDelay(iterator, 0);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onInterstitialLoaded(ISdkLoader loader) {
                    if (isInterstitialReachToCoreCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onInterstitialLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core interstitial");
                        loadInterstitialQueueInternalWithDelay(iterator, 0);
                    }
                }

                @Override
                public void onRewardedVideoAdLoaded(ISdkLoader loader) {
                    if (isInterstitialReachToCoreCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onRewardedVideoAdLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core interstitial");
                        loadInterstitialQueueInternalWithDelay(iterator, 0);
                    }
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onInterstitialError(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 判断是否加载够指定成功数
     *
     * @return
     */
    private boolean isInterstitialReachToCoreCount() {
        if (!mQueueRunning) {
            return true;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            return true;
        }
        int loadCount = 0;
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null
                    && (loader.isInterstitialLoaded()
                    || loader.isRewaredVideoLoaded())) {
                loadCount++;
            }
        }
        Log.v(Log.TAG, "queue core count : " + loadCount);

        if (mAdLoaders.size() < mAdPlace.getQueueSize()) {
            return loadCount >= mAdLoaders.size();
        }
        return loadCount >= mAdPlace.getQueueSize();
    }

    private void loadInterstitialQueueInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadInterstitialQueueInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadInterstitialQueueInternal(iterator);
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
        boolean shown = false;
        if (mAdPlace != null && mAdPlace.isHighEcpm()) {
            if (TextUtils.equals(mAdPlace.getPlaceType(), Constant.PLACE_TYPE_INTERSTITIAL)) {
                shown = showHighEcpmInterstitial();
            } else if (TextUtils.equals(mAdPlace.getPlaceType(), Constant.PLACE_TYPE_REWARD)) {
                shown = showHighEcpmReward();
            }
        }
        if (!shown) {
            showInterstitialInternal();
        } else {
            Log.iv(Log.TAG, "show he high int or reward");
        }
    }

    private boolean showHighEcpmInterstitial() {
        ISdkLoader loader = getIntSdkLoader();
        if (loader != null) {
            if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                if (loader.showInterstitial()) {
                    mCurrentAdLoader = loader;
                    AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean showHighEcpmReward() {
        ISdkLoader loader = getRewardSdkLoader();
        if (loader != null) {
            if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                if (loader.showRewardedVideo()) {
                    mCurrentAdLoader = loader;
                    AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                    return true;
                }
            }
        }
        return false;
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
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null, null);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }
        mAdParams = adParams;
        mCurrentAdLoader = null;
        mHasNotifyLoaded = false;
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }
        if (mAdPlace.isConcurrent()) {
            loadAdViewConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadAdViewSequence();
        } else if (mAdPlace.isRandom()) {
            loadAdViewRandom();
        } else if (mAdPlace.isQueue()) {
            loadAdViewQueue();
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
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isAdPlaceSeqLoading()) {
                setAdPlaceSeqLoading(true, SeqState.REQUEST);
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadAdViewSequenceInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " seq is loading ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
        }
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
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next adview");
                        loadAdViewSequenceInternal(iterator);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdFailed(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 使用que模式进行加载，核心，加载够指定个数
     */
    private void loadAdViewQueue() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isAdViewReachToCoreCount()) {
                if (!isAdPlaceSeqLoading()) {
                    setAdPlaceSeqLoading(true, SeqState.REQUEST);
                    final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                    loadAdViewQueueInternal(iterator);
                } else {
                    Log.iv(Log.TAG, mAdPlace.getName() + " que is loading ...");
                }
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " has enough ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
        }
    }

    private void loadAdViewQueueInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdDismiss() {
                    super.onAdDismiss();
                    if (!isAdViewReachToCoreCount()) {
                        loadAdViewQueue();
                    }
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (!isAdViewReachToCoreCount()) {
                        loadAdViewQueue();
                    }
                }

                @Override
                public void onAdShow() {
                    super.onAdShow();
                    if (!isAdViewReachToCoreCount()) {
                        loadAdViewQueue();
                    }
                }

                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next AdView in que");
                        loadAdViewQueueInternalWithDelay(iterator, 0);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    if (isAdViewReachToCoreCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onAdLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core AdView");
                        loadAdViewQueueInternalWithDelay(iterator, 0);
                    }
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdFailed(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 判断是否加载够指定成功数
     *
     * @return
     */
    private boolean isAdViewReachToCoreCount() {
        if (!mQueueRunning) {
            return true;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            return true;
        }
        int loadCount = 0;
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null
                    && (loader.isBannerLoaded()
                    || loader.isNativeLoaded())) {
                loadCount++;
            }
        }
        Log.v(Log.TAG, "queue core count : " + loadCount);

        if (mAdLoaders.size() < mAdPlace.getQueueSize()) {
            return loadCount >= mAdLoaders.size();
        }
        return loadCount >= mAdPlace.getQueueSize();
    }

    private void loadAdViewQueueInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadAdViewQueueInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAdViewQueueInternal(iterator);
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
        boolean shown = false;
        if (mAdPlace != null && mAdPlace.isHighEcpm() && TextUtils.equals(mAdPlace.getPlaceType(), Constant.PLACE_TYPE_ADVIEW)) {
            shown = showHighEcpmAdView();
        }
        if (!shown) {
            showAdViewInternal(true);
            autoSwitchAdView();
        } else {
            Log.iv(Log.TAG, "show he high banner or native");
        }
    }

    private boolean showHighEcpmAdView() {
        int bannerSize = getCommonBannerSize();
        ISdkLoader loader = getAdViewSdkLoader(bannerSize);
        if (loader != null) {
            ViewGroup viewGroup = null;
            if (mAdContainer != null) {
                viewGroup = mAdContainer.get();
            }
            if (loader.isBannerLoaded() && viewGroup != null) {
                mCurrentAdLoader = loader;
                loader.showBanner(viewGroup);
                AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                viewGroup.addView(mMView = new FSA.MView(mContext), 0, 0);
                return true;
            } else if (loader.isNativeLoaded() && viewGroup != null) {
                mCurrentAdLoader = loader;
                loader.showNative(viewGroup, getParams(loader));
                AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                viewGroup.addView(mMView = new FSA.MView(mContext), 0, 0);
                return true;
            }
        }
        return false;
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
                        || loader.isInterstitialLoaded()
                        || loader.isRewaredVideoLoaded())) {
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
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null, null);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
            return;
        }

        mAdParams = adParams;
        mCurrentAdLoader = null;
        mHasNotifyLoaded = false;
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }

        if (mAdPlace.isConcurrent()) {
            loadComplexAdsConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadComplexAdsSequence();
        } else if (mAdPlace.isRandom()) {
            loadComplexAdsRandom();
        } else if (mAdPlace.isQueue()) {
            loadComplexAdsQueue();
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
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isAdPlaceSeqLoading()) {
                setAdPlaceSeqLoading(true, SeqState.REQUEST);
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadComplexAdsSequenceInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " seq is loading ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
        }
    }

    private void loadComplexAdsSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.e(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternal(iterator);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onAdLoaded(loader);
                }

                @Override
                public void onInterstitialLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onInterstitialLoaded(loader);
                }

                @Override
                public void onRewardedVideoAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onRewardedVideoAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
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
                simpleAdBaseBaseListener.onAdFailed(Constant.AD_ERROR_CONFIG);
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
                } else if (loader.isRewardedVideoType()) {
                    loader.loadRewardedVideo();
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    /**
     * 使用que模式进行加载，核心，加载够指定个数
     */
    private void loadComplexAdsQueue() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isComplexAdsReachToLoadCount()) {
                if (!isAdPlaceSeqLoading()) {
                    setAdPlaceSeqLoading(true, SeqState.REQUEST);
                    final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                    loadComplexAdsQueueInternal(iterator);
                } else {
                    Log.iv(Log.TAG, mAdPlace.getName() + " que is loading ...");
                }
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " has enough ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null);
            }
        }
    }

    private void loadComplexAdsQueueInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdFailed(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex in que");
                        loadComplexAdsQueueInternalWithDelay(iterator, 0);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onInterstitialDismiss() {
                    super.onInterstitialDismiss();
                    if (!isComplexAdsReachToLoadCount()) {
                        loadComplexAdsQueue();
                    }
                }

                @Override
                public void onAdDismiss() {
                    super.onAdDismiss();
                    if (!isComplexAdsReachToLoadCount()) {
                        loadComplexAdsQueue();
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    super.onRewardedVideoAdClosed();
                    if (!isComplexAdsReachToLoadCount()) {
                        loadComplexAdsQueue();
                    }
                }

                @Override
                public void onAdShow() {
                    super.onAdShow();
                    if (!isComplexAdsReachToLoadCount()) {
                        loadComplexAdsQueue();
                    }
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (!isComplexAdsReachToLoadCount()) {
                        loadComplexAdsQueue();
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex in que");
                        loadComplexAdsQueueInternalWithDelay(iterator, 0);
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    if (isComplexAdsReachToLoadCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onAdLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core");
                        loadComplexAdsQueueInternalWithDelay(iterator, 0);
                    }
                }

                @Override
                public void onInterstitialLoaded(ISdkLoader loader) {
                    if (isComplexAdsReachToLoadCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onInterstitialLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core");
                        loadComplexAdsQueueInternalWithDelay(iterator, 0);
                    }
                }

                @Override
                public void onRewardedVideoAdLoaded(ISdkLoader loader) {
                    if (isComplexAdsReachToLoadCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                        super.onRewardedVideoAdLoaded(loader);
                    } else {
                        Log.iv(Log.TAG, "load for core");
                        loadComplexAdsQueueInternalWithDelay(iterator, 0);
                    }
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
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
                simpleAdBaseBaseListener.onAdFailed(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 判断是否加载够指定成功数
     *
     * @return
     */
    private boolean isComplexAdsReachToLoadCount() {
        if (!mQueueRunning) {
            return true;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            return true;
        }
        int loadCount = 0;
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null && loader.hasLoadedFlag()
                    && (loader.isBannerLoaded()
                    || loader.isNativeLoaded()
                    || loader.isInterstitialLoaded()
                    || loader.isRewaredVideoLoaded())) {
                loadCount++;
            }
        }
        Log.v(Log.TAG, "queue core count : " + loadCount);

        if (mAdLoaders.size() < mAdPlace.getQueueSize()) {
            return loadCount >= mAdLoaders.size();
        }
        return loadCount >= mAdPlace.getQueueSize();
    }

    private void loadComplexAdsQueueInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadComplexAdsQueueInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadComplexAdsQueueInternal(iterator);
                }
            }, delay);
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
    public boolean showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType) {
        Log.d(Log.TAG, "");
        if (isHighEcpmMode() && adContainer != null) {
            return showHighEcpmComplexAdView(adContainer);
        }
        return showComplexAdsInNormalMode(adContainer, adParams, source, adType);
    }

    private boolean showComplexAdsInNormalMode(ViewGroup adContainer, AdParams adParams, String source, String adType) {
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
                        return true;
                    } else if (loader.isNativeLoaded()) {
                        loader.showNative(adContainer, getParams(loader));
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        return true;
                    } else if (loader.isInterstitialLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.showInterstitial();
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        return true;
                    } else if (loader.isRewaredVideoLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.showRewardedVideo();
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取高ECPM的banner和native广告位
     *
     * @param viewGroup
     * @return
     */
    private boolean showHighEcpmComplexAdView(ViewGroup viewGroup) {
        int bannerSize = getCommonBannerSize();
        ISdkLoader loader = getAdViewSdkLoader(bannerSize);
        if (loader != null) {
            if (loader.isBannerLoaded() && viewGroup != null) {
                mCurrentAdLoader = loader;
                loader.showBanner(viewGroup);
                AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                return true;
            } else if (loader.isNativeLoaded() && viewGroup != null) {
                mCurrentAdLoader = loader;
                loader.showNative(viewGroup, getParams(loader));
                AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                return true;
            }
        }
        return false;
    }

    private boolean isHighEcpmMode() {
        if (mAdPlace != null && mAdPlace.isHighEcpm() && TextUtils.equals(mAdPlace.getPlaceType(), Constant.PLACE_TYPE_COMPLEX)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean showComplexAds() {
        boolean shown = false;
        if (isHighEcpmMode()) {
            shown = showHighEcpmComplex();
        }
        if (!shown) {
            return showComplexAdsInternal();
        }
        return false;
    }

    /**
     * 展示复合布局的插屏, 包含banner native interstitial
     *
     * @return
     */
    private boolean showHighEcpmComplex() {
        ISdkLoader loader = getComplexSdkLoader(getCommonBannerSize());
        if (loader != null) {
            if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                if (loader.showInterstitial()) {
                    mCurrentAdLoader = loader;
                    AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                    return true;
                }
            } else if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                if (loader.showRewardedVideo()) {
                    mCurrentAdLoader = loader;
                    AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                    return true;
                }
            } else if (loader.isBannerType() && loader.isBannerLoaded()) {
                show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                return true;
            } else if (loader.isNativeType() && loader.isNativeLoaded()) {
                show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                return true;
            }
        }
        return false;
    }

    private boolean showComplexAdsInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.hasLoadedFlag()) {
                    if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.useAndClearFlag();
                        if (loader.showRewardedVideo()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            return true;
                        }
                    } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        ActivityMonitor.get(mContext).setPidConfig(loader.getPidConfig());
                        loader.useAndClearFlag();
                        if (loader.showInterstitial()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            return true;
                        }
                    } else if (loader.isBannerType() && loader.isBannerLoaded()) {
                        show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                        return true;
                    } else if (loader.isNativeType() && loader.isNativeLoaded()) {
                        show(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void show(String pidName, String source, String adType) {
        try {
            Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.FAPICKER", false);
            if (intent == null) {
                intent = new Intent(mContext, FSA.class);
            }
            intent.putExtra(Intent.EXTRA_TITLE, pidName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
    public boolean isLoading() {
        return isAdPlaceSeqLoading();
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
    public void setQueueRunning(boolean running) {
        mQueueRunning = running;
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
    public OnAdSdkListener getOnAdSdkListener(boolean loaded) {
        return loaded ? mOnAdSdkLoadedListener : mOnAdSdkListener;
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
    public class AdPlaceLoaderListener extends SimpleAdSdkListener {

        public void onLoaded(ISdkLoader loader) {
            putSdkLoader(mContext, loader);
        }

        @Override
        public void onLoaded(String pidName, String source, String adType) {
            super.onLoaded(pidName, source, adType);
        }

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

    private static boolean equalsLoader(ISdkLoader l1, ISdkLoader l2) {
        try {
            if (l1 != null && l2 != null) {
                return TextUtils.equals(l1.getAdPlaceName(), l2.getAdPlaceName())
                        && TextUtils.equals(l1.getPidConfig().getPid(), l2.getPidConfig().getPid());
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return false;
    }

    private static int ecpmSort(ISdkLoader l1, ISdkLoader l2) {
        if (l1 != null && l2 != null) {
            return Double.compare(l1.getEcpm(), l2.getEcpm());
        }
        if (l1 == null && l2 != null) {
            return -1;
        }
        if (l1 != null && l2 == null) {
            return 1;
        }
        return 0;
    }

    /**
     * 按照ecpm的大小排序放置loader，当placename和pid都相同时替换
     *
     * @param loader
     */
    private static synchronized void putSdkLoader(Context context, ISdkLoader loader) {
        try {
            if (sLoadedAdLoaders != null && loader != null) {
                synchronized (sLoadedAdLoaders) {
                    int size = sLoadedAdLoaders.size();
                    ISdkLoader l = null;
                    boolean replaced = false;
                    for (int index = 0; index < size; index++) {
                        l = sLoadedAdLoaders.get(index);
                        if (equalsLoader(l, loader)) {
                            sLoadedAdLoaders.set(index, loader);
                            replaced = true;
                        }
                    }
                    if (!replaced) {
                        sLoadedAdLoaders.add(loader);
                    }
                    Collections.sort(sLoadedAdLoaders, new Comparator<ISdkLoader>() {
                        @Override
                        public int compare(ISdkLoader o1, ISdkLoader o2) {
                            return ecpmSort(o2, o1);
                        }
                    });
                    removeUnuseLoader();
                }
            }
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e);
            if (context != null) {
                InternalStat.reportEvent(context, "putSdkLoader : " + (e != null ? e.getMessage() : "unknown error"));
            }
        }
    }

    private static void removeUnuseLoader() {
        try {
            if (sLoadedAdLoaders != null) {
                synchronized (sLoadedAdLoaders) {
                    ISdkLoader l = null;
                    for (int index = sLoadedAdLoaders.size() - 1; index >= 0; index--) {
                        l = sLoadedAdLoaders.get(index);
                        if (l != null
                                && !l.isBannerLoaded()
                                && !l.isNativeLoaded()
                                && !l.isInterstitialLoaded()
                                && !l.isRewaredVideoLoaded()) {
                            sLoadedAdLoaders.remove(index);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private static ISdkLoader getAdViewSdkLoader(int bannerSize) {
        if (sLoadedAdLoaders != null) {
            synchronized (sLoadedAdLoaders) {
                for (ISdkLoader loader : sLoadedAdLoaders) {
                    if (loader != null && (loader.isBannerLoaded() || loader.isNativeLoaded())) {
                        if (TextUtils.equals(loader.getAdType(), Constant.TYPE_NATIVE)) {
                            return loader;
                        }
                        if (TextUtils.equals(loader.getAdType(), Constant.TYPE_BANNER) && loader.getBannerSize() == bannerSize) {
                            return loader;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ISdkLoader getIntSdkLoader() {
        if (sLoadedAdLoaders != null) {
            synchronized (sLoadedAdLoaders) {
                for (ISdkLoader loader : sLoadedAdLoaders) {
                    if (loader != null && loader.isInterstitialLoaded()
                            && TextUtils.equals(loader.getAdType(), Constant.TYPE_INTERSTITIAL)) {
                        return loader;
                    }
                }
            }
        }
        return null;
    }

    private static ISdkLoader getRewardSdkLoader() {
        if (sLoadedAdLoaders != null) {
            synchronized (sLoadedAdLoaders) {
                for (ISdkLoader loader : sLoadedAdLoaders) {
                    if (loader != null && loader.isRewaredVideoLoaded()
                            && TextUtils.equals(loader.getAdType(), Constant.TYPE_REWARD)) {
                        return loader;
                    }
                }
            }
        }
        return null;
    }

    private static ISdkLoader getComplexSdkLoader(int bannerSize) {
        if (sLoadedAdLoaders != null) {
            synchronized (sLoadedAdLoaders) {
                for (ISdkLoader loader : sLoadedAdLoaders) {
                    if (loader != null) {
                        if (loader.isBannerLoaded() && TextUtils.equals(loader.getAdType(), Constant.TYPE_BANNER)
                                && loader.getBannerSize() == bannerSize) {
                            return loader;
                        } else if (loader.isNativeLoaded() && TextUtils.equals(loader.getAdType(), Constant.TYPE_NATIVE)) {
                            return loader;
                        } else if (loader.isInterstitialLoaded() && TextUtils.equals(loader.getAdType(), Constant.TYPE_INTERSTITIAL)) {
                            return loader;
                        } else if (loader.isInterstitialLoaded() && TextUtils.equals(loader.getAdType(), Constant.TYPE_REWARD)) {
                            return loader;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int getLoadedSize() {
        int size = 0;
        if (sLoadedAdLoaders != null) {
            synchronized (sLoadedAdLoaders) {
                for (ISdkLoader loader : sLoadedAdLoaders) {
                    if (loader != null
                            && (loader.isBannerLoaded()
                            || loader.isNativeLoaded()
                            || loader.isInterstitialLoaded()
                            || loader.isRewaredVideoLoaded())) {
                        size++;
                    }
                }
            }
        }
        return size;
    }

    private void setAdPlaceSeqLoading(boolean loading, SeqState seqState) {
        mAdPlaceSeqLoading = loading;
        if (mAdPlaceSeqLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(getMsgWhat());
                if (mAdPlace != null && !TextUtils.isEmpty(mAdPlace.getName())) {
                    Log.iv(Log.TAG, mAdPlace.getName() + " send seq loading timeout : " + getTimeout());
                }
                mHandler.sendEmptyMessageDelayed(getMsgWhat(), getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(getMsgWhat());
                if (mAdPlace != null && !TextUtils.isEmpty(mAdPlace.getName())) {
                    Log.iv(Log.TAG, mAdPlace.getName() + " remove seq loading timeout");
                }
            }
        }
        if (mAdPlace != null && !TextUtils.isEmpty(mAdPlace.getName())) {
            if (seqState == SeqState.REQUEST) {
                StatImpl.get().reportAdPlaceSeqRequest(mContext, mAdPlace.getName());
            } else if (seqState == SeqState.LOADED) {
                StatImpl.get().reportAdPlaceSeqLoaded(mContext, mAdPlace.getName());
            } else if (seqState == SeqState.ERROR) {
                StatImpl.get().reportAdPlaceSeqError(mContext, mAdPlace.getName());
            }
        }
    }

    private boolean isAdPlaceSeqLoading() {
        return mAdPlaceSeqLoading;
    }

    private long getTimeout() {
        if (mAdPlace != null) {
            return mAdPlace.getSeqTimeout();
        }
        return 0;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == getMsgWhat()) {
            setAdPlaceSeqLoading(false, SeqState.ERROR);
            return true;
        }
        return false;
    }

    /**
     * 获取id
     *
     * @return
     */
    private int getMsgWhat() {
        int msgWhat = 0;
        if (mAdPlace != null && !TextUtils.isEmpty(mAdPlace.getName())) {
            msgWhat = mAdPlace.getName().hashCode();
        }
        return msgWhat;
    }

    enum SeqState {
        REQUEST, LOADED, ERROR
    }
}