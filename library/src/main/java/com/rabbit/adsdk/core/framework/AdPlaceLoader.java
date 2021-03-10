package com.rabbit.adsdk.core.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdReward;
import com.rabbit.adsdk.adloader.addfp.AdDfpLoader;
import com.rabbit.adsdk.adloader.adfb.FBLoader;
import com.rabbit.adsdk.adloader.admob.AdmobLoader;
import com.rabbit.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.adloader.mopub.MopubLoader;
import com.rabbit.adsdk.adloader.spread.SpLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.AdHelper;
import com.rabbit.adsdk.core.AdPolicy;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.listener.OnAdSdkListener;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.sunny.IAdvance;
import com.rabbit.sunny.RabActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private View mDotView;
    private boolean mAdPlaceSeqLoading = false;
    private boolean mQueueRunning = true;
    private String mPlaceType = null;
    private int mErrorTimes = 0;
    private int mRetryTimes = 0;
    private boolean mAutoLoad = false;

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
    public boolean needReload(AdPlace adPlace) {
        if (mAdPlace != null && adPlace != null) {
            Log.d(Log.TAG, "pidName : " + mAdPlace.getName() + " , usingUnique : " + mAdPlace.getUniqueValue() + " , remoteUnique : " + adPlace.getUniqueValue());
            return !TextUtils.equals(mAdPlace.getUniqueValue(), adPlace.getUniqueValue());
        }
        return false;
    }

    @Override
    public String getPlaceName() {
        if (mAdPlace != null) {
            return mAdPlace.getName();
        }
        return super.getPlaceName();
    }

    private void generateLoaders() {
        if (mAdPlace != null) {
            List<PidConfig> pidList = mAdPlace.getPidsList();
            if (pidList != null && !pidList.isEmpty()) {
                ISdkLoader loader = null;
                for (PidConfig config : pidList) {
                    if (config != null) {
                        if (config.isAdmob() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdmobLoader();
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isFB() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new FBLoader();
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isMopub() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new MopubLoader();
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isDfp() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new AdDfpLoader();
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
                            if (loader.allowUseLoader()) {
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isSpread() && AdHelper.isModuleLoaded(config.getSdk())) {
                            loader = new SpLoader();
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
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
                params.setBannerSize(Constant.AD_SDK_DFP, Constant.MEDIUM_RECTANGLE);
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
    public OnAdSdkListener getOnAdSdkListener() {
        if (mOnAdSdkLoadedListener != null) {
            return mOnAdSdkLoadedListener;
        }

        if (mOnAdSdkListener != null) {
            return mOnAdSdkListener;
        }

        return super.getOnAdSdkListener();
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
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
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
                mOnAdSdkListener.onError(null, null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
        setPlaceType(Constant.PLACE_TYPE_INTERSTITIAL);
        loadInterstitialInternal();
    }

    private void loadInterstitialInternal() {
        resetPlaceErrorTimes();
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
                    if (loader.isInterstitialType()) {
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
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
                if (loader.isInterstitialType()) {
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
                        Log.iv(Log.TAG, "load next interstitial");
                        loadInterstitialSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
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
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onInterstitialError(Constant.AD_ERROR_CONFIG);
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
     * 使用que模式进行加载，核心，加载够指定个数
     */
    private void loadInterstitialQueue() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isInterstitialReachToCoreCount()) {
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadInterstitialQueueInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " has enough ...");
                if (mOnAdSdkListener != null) {
                    mOnAdSdkListener.onLoaded(mAdPlace.getName(), null, null);
                }
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
        }
    }

    private void loadInterstitialQueueInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
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
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next interstitial in que");
                        loadInterstitialQueueInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onInterstitialLoaded(ISdkLoader loader) {
                    super.onInterstitialLoaded(loader);
                    if (isInterstitialReachToCoreCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                    } else {
                        Log.iv(Log.TAG, "load for core interstitial");
                        loadInterstitialQueueInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    }
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
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
        List<String> loadedSdk = new ArrayList<String>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null
                    && loader.isInterstitialLoaded()) {
                loadCount++;
                loadedSdk.add(loader.getSdkName());
            }
        }
        Log.v(Log.TAG, "interstitial queue core count : " + loadCount + " , source : " + loadedSdk);

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
        showInterstitialInternal();
    }

    private void showInterstitialInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isRewardedVideoLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
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
    public void loadRewardedVideo(Activity activity, boolean auto) {
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
        setPlaceType(Constant.PLACE_TYPE_REWARDEDVIDEO);
        loadRewardedVideoInternal(auto);
    }

    private void loadRewardedVideoInternal(boolean auto) {
        mAutoLoad = auto;
        resetPlaceErrorTimes();
        if (auto) {
            loadRewardedVideoQueue();
        } else {
            if (mAdPlace.isConcurrent()) {
                loadRewardedVideoConcurrent();
            } else if (mAdPlace.isSequence()) {
                loadRewardedVideoSequence();
            } else if (mAdPlace.isRandom()) {
                loadRewardedVideoRandom();
            } else if (mAdPlace.isQueue()) {
                loadRewardedVideoQueue();
            } else {
                loadRewardedVideoConcurrent();
            }
        }
    }

    private void loadRewardedVideoConcurrent() {
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
                        Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                    }
                }
            }
        }
    }

    private void loadRewardedVideoSequence() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            // 使用迭代器处理
            if (!isAdPlaceSeqLoading()) {
                setAdPlaceSeqLoading(true, SeqState.REQUEST);
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadRewardedVideoSequenceInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " seq is loading ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
        }
    }

    private void loadRewardedVideoRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            ISdkLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowUseLoader()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                        loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                if (loader.isRewardedVideoType()) {
                    loader.loadRewardedVideo();
                } else {
                    Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    private void loadRewardedVideoSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {

                @Override
                public void onRewardedVideoError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next rewardedvideo");
                        loadRewardedVideoSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onRewardedVideoError(error);
                    }
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
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onRewardedVideoError(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    private void loadRewardedVideoSequenceInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadRewardedVideoSequenceInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadRewardedVideoSequenceInternal(iterator);
                }
            }, delay);
        }
    }

    /**
     * 使用que模式进行加载，核心，加载够指定个数
     */
    private void loadRewardedVideoQueue() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            if (!isRewardedVideoReachToCoreCount()) {
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadRewardedVideoQueueInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " has enough ...");
                if (mOnAdSdkListener != null) {
                    mOnAdSdkListener.onLoaded(mAdPlace.getName(), null, null);
                }
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
        }
    }

    private void loadRewardedVideoQueueInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {

                @Override
                public void onRewardedVideoAdClosed() {
                    super.onRewardedVideoAdClosed();
                    if (!isRewardedVideoReachToCoreCount()) {
                        loadRewardedVideoQueue();
                    }
                }

                @Override
                public void onRewardedVideoError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next rewardedvideo in que");
                        loadRewardedVideoQueueInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onRewardedVideoError(error);
                    }
                }

                @Override
                public void onRewardedVideoAdLoaded(ISdkLoader loader) {
                    super.onRewardedVideoAdLoaded(loader);
                    if (isRewardedVideoReachToCoreCount()) {
                        setAdPlaceSeqLoading(false, SeqState.LOADED);
                    } else {
                        Log.iv(Log.TAG, "load for core rewardedvideo");
                        loadRewardedVideoQueueInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    }
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onRewardedVideoError(Constant.AD_ERROR_CONFIG);
            }
        }
    }

    /**
     * 判断是否加载够指定成功数
     *
     * @return
     */
    private boolean isRewardedVideoReachToCoreCount() {
        if (!mQueueRunning) {
            return true;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            return true;
        }
        int loadCount = 0;
        List<String> loadedSdk = new ArrayList<String>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null
                    && loader.isRewardedVideoLoaded()) {
                loadCount++;
                loadedSdk.add(loader.getSdkName());
            }
        }
        Log.v(Log.TAG, "reward queue core count : " + loadCount + " , source : " + loadedSdk);

        if (mAdLoaders.size() < mAdPlace.getQueueSize()) {
            return loadCount >= mAdLoaders.size();
        }
        return loadCount >= mAdPlace.getQueueSize();
    }

    private void loadRewardedVideoQueueInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadRewardedVideoQueueInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadRewardedVideoQueueInternal(iterator);
                }
            }, delay);
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showRewardedVideo() {
        Log.d(Log.TAG, "showRewardedVideo");
        showRewardedVideoInternal();
    }

    private void showRewardedVideoInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        if (loader.showRewardedVideo()) {
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
                mOnAdSdkListener.onError(null, null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
        setPlaceType(Constant.PLACE_TYPE_ADVIEW);
        loadAdViewInternal();
    }

    private void loadAdViewInternal() {
        resetPlaceErrorTimes();
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
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
                        Log.iv(Log.TAG, "load next adview");
                        loadAdViewSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
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
                        addDotView(viewGroup);
                        break;
                    } else if (loader.isNativeLoaded() && viewGroup != null) {
                        mCurrentAdLoader = loader;
                        loader.showNative(viewGroup, getParams(loader));
                        if (needCounting) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        }
                        addDotView(viewGroup);
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
                        || loader.isRewardedVideoLoaded())) {
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
                            || loader.isRewardedVideoLoaded();
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
                            || loader.isRewardedVideoLoaded();
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
                mOnAdSdkListener.onError(null, null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidsList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
        setPlaceType(Constant.PLACE_TYPE_COMPLEX);
        loadComplexAdsInternal();
    }

    private void loadComplexAdsInternal() {
        resetPlaceErrorTimes();
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
                mOnAdSdkListener.onError(mAdPlace.getName(), null, null, Constant.AD_ERROR_ADLOADER);
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
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdFailed(error);
                    }
                }

                @Override
                public void onInterstitialError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onInterstitialError(error);
                    }
                }

                @Override
                public void onRewardedVideoError(int error) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onRewardedVideoError(error);
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
    public boolean showComplexAds(ViewGroup adContainer, AdParams adParams, String source, String adType) {
        Log.d(Log.TAG, "");
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
                        loader.showInterstitial();
                        mCurrentAdLoader = loader;
                        AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                        return true;
                    } else if (loader.isRewardedVideoLoaded()) {
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

    @Override
    public boolean showComplexAds() {
        return showComplexAdsInternal();
    }

    private boolean showComplexAdsInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.hasLoadedFlag()) {
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        loader.useAndClearFlag();
                        if (loader.showRewardedVideo()) {
                            mCurrentAdLoader = loader;
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPidName(), mAdPlace);
                            return true;
                        }
                    } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
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
            Intent intent = new Intent(mContext, RabActivity.class);
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
    public int getLoadedAdCount() {
        int loadedAdCount = 0;
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            for (ISdkLoader loader : mAdLoaders) {
                if ((loader.isBannerLoaded()
                        || loader.isNativeLoaded()
                        || loader.isInterstitialLoaded()
                        || loader.isRewardedVideoLoaded())) {
                    loadedAdCount++;
                }
            }
        }
        return loadedAdCount;
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

    private void setPlaceType(String placeType) {
        mPlaceType = placeType;
    }

    private synchronized void startRetryIfNeed(String pidName, String source, String adType) {
        mErrorTimes++;
        Log.iv(Log.TAG, "pidName : " + pidName + " , record error times " + mErrorTimes);
        boolean isRetry = mAdPlace.isRetry();
        boolean isAdError = isAdError();
        boolean isRetryTimesAllow = isRetryTimesAllow();
        Log.iv(Log.TAG, "pidName : " + pidName + " , retry : " + isRetry + " , adError : " + isAdError + " , allowRetry : " + isRetryTimesAllow);
        if (isRetry && isAdError && isRetryTimesAllow) {
            mRetryTimes++;
            startRetry(pidName);
        } else {
            if (!isRetryTimesAllow && isAdError) {
                resetRetryTimes(pidName, source, adType);
            }
        }
    }

    private synchronized void resetRetryTimes(String pidName, String source, String adType) {
        mRetryTimes = 0;
        Log.iv(Log.TAG, "pidName : " + pidName + " , reset retry times " + mRetryTimes);
    }

    private boolean isRetryTimesAllow() {
        if (mAdPlace != null) {
            return mRetryTimes < mAdPlace.getRetryTimes();
        }
        return false;
    }

    private void startRetry(String pidName) {
        Log.iv(Log.TAG, "pidName : " + pidName + " , start retry " + mPlaceType + " " + mRetryTimes);
        if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_ADVIEW)) {
            loadAdViewInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_INTERSTITIAL)) {
            loadInterstitialInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_REWARDEDVIDEO)) {
            loadRewardedVideoInternal(mAutoLoad);
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_COMPLEX)) {
            loadComplexAdsInternal();
        } else {
            Log.iv(Log.TAG, "can not match place type");
        }
    }

    public boolean isAdError() {
        boolean placeError = false;
        if (mAdLoaders != null) {
            if (TextUtils.equals(getAdMode(), Constant.MODE_CON)) {
                placeError = mAdLoaders.size() <= mErrorTimes;
            } else if (TextUtils.equals(getAdMode(), Constant.MODE_SEQ)) {
                placeError = mErrorTimes == 1;
            } else if (TextUtils.equals(getAdMode(), Constant.MODE_RAN)) {
                placeError = mErrorTimes == 1;
            } else if (TextUtils.equals(getAdMode(), Constant.MODE_QUE)) {
                placeError = mErrorTimes == 1;
            } else {
                Log.iv(Log.TAG, "can not match mode");
            }
        }
        return placeError;
    }

    private synchronized void resetPlaceErrorTimes() {
        mErrorTimes = 0;
    }

    /**
     * AdPlaceLoader类使用的监听器
     */
    public class AdPlaceLoaderListener implements OnAdSdkListener {

        @Override
        public void onLoaded(String pidName, String source, String adType) {
            resetRetryTimes(pidName, source, adType);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onLoaded(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoaded(pidName, source, adType);
            }
        }

        @Override
        public void onLoading(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onLoading(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoading(pidName, source, adType);
            }
        }

        @Override
        public void onShow(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onShow(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onShow(pidName, source, adType);
            }
        }

        @Override
        public void onImp(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onImp(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onImp(pidName, source, adType);
            }
        }

        @Override
        public void onError(String pidName, String source, String adType, int errorCode) {
            startRetryIfNeed(pidName, source, adType);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onError(pidName, source, adType, errorCode);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(pidName, source, adType, errorCode);
            }
        }

        @Override
        public void onRewarded(String pidName, String source, String adType, AdReward item) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onRewarded(pidName, source, adType, item);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onRewarded(pidName, source, adType, item);
            }
        }

        @Override
        public void onCompleted(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onCompleted(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onCompleted(pidName, source, adType);
            }
        }

        @Override
        public void onStarted(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onStarted(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onStarted(pidName, source, adType);
            }
        }

        @Override
        public void onUpdate(String pidName, String source, String adType) {
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onUpdate(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onUpdate(pidName, source, adType);
            }
        }

        @Override
        public void onClick(String pidName, String source, String adType) {
            if (mAdPlace != null && mAdPlace.isAutoSwitch()) {
                Log.d(Log.TAG, "ad place loader pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                if (TextUtils.equals(adType, Constant.TYPE_NATIVE) || TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                    resume();
                    showNextAdView();
                }
            }
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onClick(pidName, source, adType);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onClick(pidName, source, adType);
            }
        }

        @Override
        public void onDismiss(String pidName, String source, String adType, boolean onDestroy) {
            if (mAdPlace != null && mAdPlace.isAutoSwitch()) {
                Log.d(Log.TAG, "adplaceloader pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)
                        || TextUtils.equals(adType, Constant.TYPE_REWARD)) {
                    showInterstitialInternal();
                }
            }
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onDismiss(pidName, source, adType, onDestroy);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onDismiss(pidName, source, adType, onDestroy);
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
        if (!mAdPlace.isPlaceCache()) {
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

        if (!isDotViewVisible()) {
            Log.v(Log.TAG, "ai not visible");
            return;
        }
        resume();
        showNextAdView();
        autoSwitchAdView();
    }

    private void addDotView(ViewGroup viewGroup) {
        try {
            Class<?> viewClass = Class.forName(IAdvance.ACT_VIEW_NAME);
            Constructor c = viewClass.getConstructor(new Class[]{Context.class});
            mDotView = (View) c.newInstance(new Object[]{mContext});
        } catch (Exception | Error e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        if (mDotView != null) {
            viewGroup.addView(mDotView, 0, 0);
        }
    }

    private boolean isDotViewVisible() {
        boolean isVisible = false;
        if (mDotView != null) {
            try {
                Class<?> viewClass = Class.forName(IAdvance.ACT_VIEW_NAME);
                Method m = viewClass.getMethod("isVisible");
                isVisible = (boolean) m.invoke(mDotView);
            } catch (Exception | Error e) {
                Log.e(Log.TAG, "error : " + e, e);
            }
        }
        return isVisible;
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
                EventImpl.get().reportAdPlaceSeqRequest(mContext, mAdPlace.getName());
            } else if (seqState == SeqState.LOADED) {
                EventImpl.get().reportAdPlaceSeqLoaded(mContext, mAdPlace.getName());
            } else if (seqState == SeqState.ERROR) {
                EventImpl.get().reportAdPlaceSeqError(mContext, mAdPlace.getName());
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
        return 120000;
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

    @NonNull
    @Override
    public String toString() {
        return getPlaceName();
    }
}