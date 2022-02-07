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
import com.rabbit.adsdk.adloader.adfb.FBLoader;
import com.rabbit.adsdk.adloader.admob.AdmobLoader;
import com.rabbit.adsdk.adloader.applovin.AppLovinLoader;
import com.rabbit.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.rabbit.adsdk.adloader.inmobi.InmobiLoader;
import com.rabbit.adsdk.adloader.listener.IManagerListener;
import com.rabbit.adsdk.adloader.listener.ISdkLoader;
import com.rabbit.adsdk.adloader.listener.OnAdBaseListener;
import com.rabbit.adsdk.adloader.listener.OnAdSdkInternalListener;
import com.rabbit.adsdk.adloader.mintegral.MintegralLoader;
import com.rabbit.adsdk.adloader.spread.SpLoader;
import com.rabbit.adsdk.adloader.tradplus.TradPlusLoader;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.AdPolicy;
import com.rabbit.adsdk.core.ModuleLoaderHelper;
import com.rabbit.adsdk.data.config.AdPlace;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.listener.OnAdSdkListener;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.stat.EventImpl;
import com.rabbit.sunny.IAdvance;
import com.rabbit.sunny.RabActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个广告位对应一个AdPlaceLoader对象
 */

public class AdPlaceLoader extends AdBaseLoader implements IManagerListener, Runnable, Handler.Callback {
    public static Map<String, ISdkLoader> sLoaderMap = new HashMap<String, ISdkLoader>();
    public static Map<String, Params> sParamsMap = new HashMap<String, Params>();
    private List<ISdkLoader> mAdLoaders = new ArrayList<ISdkLoader>();
    private AdPlace mAdPlace;
    private Context mContext;
    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mOnAdSdkLoadedListener;
    private OnAdSdkInternalListener mOnAdPlaceLoaderListener = new AdPlaceLoaderListener();
    private AdParams mAdParams;
    private boolean mHasNotifyLoaded = false;
    // banner和native的listener集合
    private Map<ISdkLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<ISdkLoader, OnAdBaseListener>();
    private WeakReference<Activity> mActivity;
    private WeakReference<ViewGroup> mAdContainer;
    private String mOriginPlaceName;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private View mDotView;
    private boolean mAdPlaceSeqLoading = false;
    private String mPlaceType = null;
    private int mErrorTimes = 0;
    private int mRetryTimes = 0;

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
    public void setOriginPlaceName(String placeName) {
        mOriginPlaceName = placeName;
    }

    @Override
    public boolean needReload(AdPlace adPlace) {
        if (mAdPlace != null && adPlace != null) {
            Log.iv(Log.TAG, "place name : " + mAdPlace.getName() + " , using unique : " + mAdPlace.getUniqueValue() + " , remote unique : " + adPlace.getUniqueValue());
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
            List<PidConfig> pidList = mAdPlace.getPidList();
            if (pidList != null && !pidList.isEmpty()) {
                ISdkLoader loader = null;
                for (PidConfig config : pidList) {
                    if (config != null) {
                        if (config.isAdmob() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new AdmobLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isFB() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new FBLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isApplovin() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new AppLovinLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isMintegral() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new MintegralLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isInmobi() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new InmobiLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isTradPlus() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new TradPlusLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
                                mAdLoaders.add(loader);
                            }
                        } else if (config.isSpread() && ModuleLoaderHelper.isModuleLoaded(config.getSdk())) {
                            if (!config.isDisable()) {
                                loader = new SpLoader();
                                loader.init(mContext, config);
                                loader.setListenerManager(this);
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
                Log.iv(Log.TAG, "use default ad params");
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
                        Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
                mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        resetAdLoaded();
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
                        Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
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
                    Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                public void onAdLoadFailed(int error, String msg) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next interstitial");
                        loadInterstitialSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdLoadFailed(error, msg);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdLoadFailed(Constant.AD_ERROR_CONFIG, "error config");
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
        Log.iv(Log.TAG, "showInterstitial");
        showInterstitialInternal();
    }

    private void showInterstitialInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        if (loader.showInterstitial()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
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
                        Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
    public void loadRewardedVideo(Activity activity) {
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        resetAdLoaded();
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }
        setPlaceType(Constant.PLACE_TYPE_REWARDEDVIDEO);
        loadRewardedVideoInternal();
    }

    private void loadRewardedVideoInternal() {
        resetPlaceErrorTimes();
        if (mAdPlace.isConcurrent()) {
            loadRewardedVideoConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadRewardedVideoSequence();
        } else if (mAdPlace.isRandom()) {
            loadRewardedVideoRandom();
        } else {
            loadRewardedVideoConcurrent();
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
                        Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
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
                    Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                public void onAdLoadFailed(int error, String msg) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next rewardedvideo");
                        loadRewardedVideoSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdLoadFailed(error, msg);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdLoadFailed(Constant.AD_ERROR_CONFIG, "error config");
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
     * 展示插屏
     */
    @Override
    public void showRewardedVideo() {
        Log.iv(Log.TAG, "showRewardedVideo");
        showRewardedVideoInternal();
    }

    private void showRewardedVideoInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        if (loader.showRewardedVideo()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                            break;
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public boolean isSplashLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isSplashType() && loader.isSplashLoaded()) {
                        Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
    public void loadSplash(Activity activity) {
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        resetAdLoaded();
        // 处理场景缓存
        if (processAdPlaceCache()) {
            return;
        }
        setPlaceType(Constant.PLACE_TYPE_SPLASH);
        loadSplashInternal();
    }

    private void loadSplashInternal() {
        resetPlaceErrorTimes();
        if (mAdPlace.isConcurrent()) {
            loadSplashConcurrent();
        } else if (mAdPlace.isSequence()) {
            loadSplashSequence();
        } else if (mAdPlace.isRandom()) {
            loadSplashRandom();
        } else {
            loadSplashConcurrent();
        }
    }

    private void loadSplashConcurrent() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                            loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                }
            }
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null && loader.allowUseLoader()) {
                    if (loader.isSplashType()) {
                        loader.loadSplash();
                    } else {
                        Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                    }
                }
            }
        }
    }

    private void loadSplashSequence() {
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            // 使用迭代器处理
            if (!isAdPlaceSeqLoading()) {
                setAdPlaceSeqLoading(true, SeqState.REQUEST);
                final Iterator<ISdkLoader> iterator = mAdLoaders.iterator();
                loadSplashSequenceInternal(iterator);
            } else {
                Log.iv(Log.TAG, mAdPlace.getName() + " seq is loading ...");
            }
        } else {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    private void loadSplashRandom() {
        if (mAdLoaders != null) {
            int pos = new Random().nextInt(mAdLoaders.size());
            ISdkLoader loader = mAdLoaders.get(pos);
            if (loader != null && loader.allowUseLoader()) {
                registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                        loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this));
                if (loader.isSplashType()) {
                    loader.loadSplash();
                } else {
                    Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    private void loadSplashSequenceInternal(final Iterator<ISdkLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        ISdkLoader loader = iterator.next();
        if (loader != null && loader.allowUseLoader()) {
            SimpleAdBaseBaseListener simpleAdBaseBaseListener = new SimpleAdBaseBaseListener(loader.getAdPlaceName(),
                    loader.getSdkName(), loader.getAdType(), getPidByLoader(loader), this) {
                @Override
                public void onAdLoadFailed(int error, String msg) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next splash");
                        loadSplashSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdLoadFailed(error, msg);
                    }
                }

                @Override
                public void onAdLoaded(ISdkLoader loader) {
                    setAdPlaceSeqLoading(false, SeqState.LOADED);
                    super.onAdLoaded(loader);
                }
            };
            registerAdBaseListener(loader, simpleAdBaseBaseListener);
            if (loader.isSplashType()) {
                loader.loadSplash();
            } else {
                Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdLoadFailed(Constant.AD_ERROR_CONFIG, "error config");
            }
        }
    }

    private void loadSplashSequenceInternalWithDelay(final Iterator<ISdkLoader> iterator, long delay) {
        if (delay <= 0 || mHandler == null) {
            loadSplashSequenceInternal(iterator);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadSplashSequenceInternal(iterator);
                }
            }, delay);
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showSplash() {
        Log.iv(Log.TAG, "showSplash");
        showSplashInternal();
    }

    private void showSplashInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isSplashType() && loader.isSplashLoaded()) {
                        if (loader.showSplash()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                            break;
                        }
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isAdViewLoaded() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null &&
                        (loader.isBannerLoaded() || loader.isNativeLoaded())) {
                    Log.iv(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
                mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        mAdParams = adParams;
        resetAdLoaded();
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
                        Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
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
                    Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                public void onAdLoadFailed(int error, String msg) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next adview");
                        loadAdViewSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdLoadFailed(error, msg);
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
                Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdLoadFailed(Constant.AD_ERROR_CONFIG, "error config");
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
        if (adParams != null) {
            mAdParams = adParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);

        showAdViewInternal(true);
        autoSwitchAdView();
    }

    private void showAdViewInternal(boolean needCounting) {
        if (mAdLoaders != null && mAdContainer != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    ViewGroup viewGroup = null;
                    if (mAdContainer != null) {
                        viewGroup = mAdContainer.get();
                    }
                    if (loader.isBannerLoaded() && viewGroup != null) {
                        loader.showBanner(viewGroup);
                        if (needCounting) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                        }
                        addDotView(viewGroup);
                        break;
                    } else if (loader.isNativeLoaded() && viewGroup != null) {
                        loader.showNative(viewGroup, getParams(loader));
                        if (needCounting) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
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
                if (loader != null && (loader.isBannerLoaded()
                        || loader.isNativeLoaded()
                        || loader.isInterstitialLoaded()
                        || loader.isRewardedVideoLoaded())
                        || loader.isSplashLoaded()) {
                    Log.iv(Log.TAG, loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
                            || loader.isRewardedVideoLoaded()
                            || loader.isSplashLoaded();
                    if (loaded) {
                        return loader.getAdType();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getLoadedSdk() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    boolean loaded = loader.isBannerLoaded()
                            || loader.isNativeLoaded()
                            || loader.isInterstitialLoaded()
                            || loader.isRewardedVideoLoaded()
                            || loader.isSplashLoaded();
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
                mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
            }
            return;
        }

        mAdParams = adParams;
        resetAdLoaded();
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
                    } else if (loader.isSplashType()) {
                        loader.loadSplash();
                    } else {
                        Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
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
                mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
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
                public void onAdLoadFailed(int error, String msg) {
                    if (iterator.hasNext()) {
                        Log.iv(Log.TAG, "load next complex");
                        loadComplexAdsSequenceInternalWithDelay(iterator, mAdPlace.getWaterfallInt());
                    } else {
                        setAdPlaceSeqLoading(false, SeqState.ERROR);
                        super.onAdLoadFailed(error, msg);
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
            } else if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else if (loader.isSplashType()) {
                loader.loadSplash();
            } else {
                Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                simpleAdBaseBaseListener.onAdLoadFailed(Constant.AD_ERROR_CONFIG, "error config");
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
                } else if (loader.isSplashType()) {
                    loader.loadSplash();
                } else {
                    Log.iv(Log.TAG, "not supported ad type : " + loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
                }
            }
        }
    }

    @Override
    public boolean showComplexAds() {
        return showComplexAdsInternal();
    }

    private boolean showComplexAdsInternal() {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                        if (loader.showRewardedVideo()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                            return true;
                        }
                    } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                        if (loader.showInterstitial()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                            return true;
                        }
                    } else if (loader.isSplashType() && loader.isSplashLoaded()) {
                        if (loader.showSplash()) {
                            AdPolicy.get(mContext).reportAdPlaceShow(getOriginPlaceName(), mAdPlace);
                            return true;
                        }
                    } else if ((loader.isBannerType() && loader.isBannerLoaded())
                            || (loader.isNativeType() && loader.isNativeLoaded())) {
                        showAdViewWithUI(loader.getAdPlaceName(), loader.getSdkName(), loader.getAdType(), loader);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showAdViewWithUI(String placeName, String source, String adType, ISdkLoader iSdkLoader) {
        Log.iv(Log.TAG, "show complex ads for banner or native");
        try {
            sLoaderMap.put(String.format(Locale.getDefault(), "%s_%s_%s", source, adType, placeName), iSdkLoader);
            sParamsMap.put(String.format(Locale.getDefault(), "%s_%s_%s", source, adType, placeName), getParams(iSdkLoader));
            Intent intent = new Intent(mContext, RabActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, placeName);
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
                        || loader.isRewardedVideoLoaded()
                        || loader.isSplashLoaded())) {
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
        Log.iv(Log.TAG, "");
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
        Log.iv(Log.TAG, "");
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
    public OnAdSdkInternalListener getOnAdPlaceLoaderListener() {
        return mOnAdPlaceLoaderListener;
    }

    private boolean hasNotifyLoaded() {
        if (mAdPlace != null && mAdPlace.isLoadOnlyOnce()) {
            return mHasNotifyLoaded;
        }
        return false;
    }

    private void notifyAdLoaded() {
        mHasNotifyLoaded = true;
    }

    private void resetAdLoaded() {
        mHasNotifyLoaded = false;
    }

    @Override
    public String getOriginPlaceName() {
        return mOriginPlaceName;
    }

    private void setPlaceType(String placeType) {
        mPlaceType = placeType;
    }

    private synchronized void startRetryIfNeed(String placeName, String source, String adType) {
        boolean isAdPlaceError = isAdPlaceError();
        if (isAdPlaceError) {
            int cfgRetryTimes = 0;
            if (mAdPlace != null) {
                cfgRetryTimes = mAdPlace.getRetryTimes();
            }
            boolean allowRetry = mRetryTimes < cfgRetryTimes;
            Log.iv(Log.TAG, "placeName : " + placeName + " , allowRetry : " + allowRetry + " , retry times : " + mRetryTimes + " , cfg retry times : " + cfgRetryTimes);
            if (allowRetry) {
                startRetry(placeName);
            } else {
                resetRetryTimes(placeName, source, adType);
            }
        }
    }

    private synchronized void recordErrorTimes(String placeName, String source, String adType) {
        mErrorTimes++;
        Log.iv(Log.TAG, "placeName : " + placeName + " , record error times " + mErrorTimes);
    }

    private synchronized void resetRetryTimes(String placeName, String source, String adType) {
        mRetryTimes = 0;
        Log.iv(Log.TAG, "placeName : " + placeName + " , reset retry times " + mRetryTimes);
    }

    private void startRetry(String placeName) {
        mRetryTimes++;
        Log.iv(Log.TAG, "placeName : " + placeName + " , start retry " + mPlaceType + " " + mRetryTimes);
        if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_ADVIEW)) {
            loadAdViewInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_INTERSTITIAL)) {
            loadInterstitialInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_REWARDEDVIDEO)) {
            loadRewardedVideoInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_SPLASH)) {
            loadSplashInternal();
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_COMPLEX)) {
            loadComplexAdsInternal();
        } else {
            Log.iv(Log.TAG, "can not match place type");
        }
    }

    public boolean isAdPlaceError() {
        boolean placeError = false;
        if (mAdLoaders != null) {
            if (TextUtils.equals(getAdMode(), Constant.MODE_CON)) {
                placeError = mAdLoaders.size() <= mErrorTimes;
            } else if (TextUtils.equals(getAdMode(), Constant.MODE_SEQ)) {
                placeError = mErrorTimes == 1;
            } else if (TextUtils.equals(getAdMode(), Constant.MODE_RAN)) {
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
    public class AdPlaceLoaderListener implements OnAdSdkInternalListener {

        @Override
        public void onRequest(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onRequest place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onRequest(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onRequest(placeName, source, adType, pid);
            }
        }

        @Override
        public void onLoaded(String placeName, String source, String adType, String pid) {
            resetRetryTimes(placeName, source, adType);
            if (hasNotifyLoaded()) {
                Log.iv(Log.TAG, "place name : " + placeName + " , sdk : " + source + " , type : " + adType + " is loaded, but has already notified ******************");
                return;
            }
            notifyAdLoaded();
            Log.iv(Log.TAG, "notify callback onLoaded place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onLoaded(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoaded(placeName, source, adType, pid);
            }
        }

        @Override
        public void onLoading(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onLoading place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onLoading(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoading(placeName, source, adType, pid);
            }
        }

        @Override
        public void onShow(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onShow place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onShow(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onShow(placeName, source, adType, pid);
            }
        }

        @Override
        public void onImp(String placeName, String source, String adType, String network, String pid) {
            Log.iv(Log.TAG, "notify callback onImp place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , network : " + network + " , pid : " + pid);
            AdStatManager.get(mContext).recordAdImp(source, placeName, network);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onImp(placeName, source, adType, network, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onImp(placeName, source, adType, network, pid);
            }
        }

        @Override
        public void onLoadFailed(String placeName, String source, String adType, String pid, int error, String msg) {
            Log.iv(Log.TAG, "notify callback onLoadFailed place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid + " , error : " + msg);
            recordErrorTimes(placeName, source, adType);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onLoadFailed(placeName, source, adType, pid, error);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onLoadFailed(placeName, source, adType, pid, error);
            }
            startRetryIfNeed(placeName, source, adType);
        }

        @Override
        public void onShowFailed(String placeName, String source, String adType, String pid, int error, String msg) {
            Log.iv(Log.TAG, "notify callback onShowFailed place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid + " , error : " + msg);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onShowFailed(placeName, source, adType, pid, error);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onShowFailed(placeName, source, adType, pid, error);
            }
        }

        @Override
        public void onRewarded(String placeName, String source, String adType, String pid, AdReward item) {
            Log.iv(Log.TAG, "notify callback onRewarded place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onRewarded(placeName, source, adType, pid, item);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onRewarded(placeName, source, adType, pid, item);
            }
        }

        @Override
        public void onCompleted(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onCompleted place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onCompleted(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onCompleted(placeName, source, adType, pid);
            }
        }

        @Override
        public void onStarted(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onStarted place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onStarted(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onStarted(placeName, source, adType, pid);
            }
        }

        @Override
        public void onUpdate(String placeName, String source, String adType, String pid) {
            Log.iv(Log.TAG, "notify callback onUpdate place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onUpdate(placeName, source, adType, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onUpdate(placeName, source, adType, pid);
            }
        }

        @Override
        public void onClick(String placeName, String source, String adType, String network, String pid) {
            Log.iv(Log.TAG, "notify callback onClick place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , network : " + network + " , pid : " + pid);
            AdStatManager.get(mContext).recordAdClick(source, placeName, network);
            if (TextUtils.equals(adType, Constant.TYPE_NATIVE)
                    || TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                String sdkName = source;
                if (!TextUtils.isEmpty(network) && !TextUtils.equals(source, network)) {
                    sdkName = network;
                }
                if (BlockAdsManager.get(mContext).replaceAdWithLoadingView(mAdContainer, placeName, sdkName, adType, network) || (mAdPlace != null && mAdPlace.isClickSwitch())) {
                    resume();
                    showNextAdView();
                }
            }
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onClick(placeName, source, adType, network, pid);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onClick(placeName, source, adType, network, pid);
            }
        }

        @Override
        public void onDismiss(String placeName, String source, String adType, String pid, boolean complexAds) {
            Log.iv(Log.TAG, "notify callback onDismiss place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onDismiss(placeName, source, adType, pid, complexAds);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onDismiss(placeName, source, adType, pid, complexAds);
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
            Log.iv(Log.TAG, "place is null");
            return false;
        }
        if (!mAdPlace.isPlaceCache()) {
            Log.iv(Log.TAG, "place no need cache");
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
            Log.iv(Log.TAG, "notify loaded with delay : " + mAdPlace.getDelayNotifyTime());
            mHandler.postDelayed(mNotifyRunnable, mAdPlace.getDelayNotifyTime());
        }
    }

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnAdSdkListener != null) {
                Log.iv(Log.TAG, "notify place loaded " + mAdPlace.getName() + " - " + getLoadedSdk() + " - " + getLoadedType());
                notifyAdLoaded();
                mOnAdSdkListener.onLoaded(mAdPlace.getName(), getLoadedSdk(), getLoadedType(), null);
            }
        }
    };

    private void autoSwitchAdView() {
        if (mAdPlace != null && mHandler != null && mAdPlace.getAutoInterval() > 0) {
            Log.iv(Log.TAG, "wait " + mAdPlace.getAutoInterval() + " ms to switch ads");
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, mAdPlace.getAutoInterval());
        }
    }

    @Override
    public void run() {
        if (!isAdViewLoaded()) {
            Log.iv(Log.TAG, "ai not loaded");
            return;
        }

        ViewGroup viewGroup = null;
        if (mAdContainer != null) {
            viewGroup = mAdContainer.get();
        }
        if (viewGroup == null) {
            Log.iv(Log.TAG, "ai empty view group");
            return;
        }

        if (!isDotViewVisible()) {
            Log.iv(Log.TAG, "ai not visible");
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