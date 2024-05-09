package com.komob.adsdk.core.framework;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.komob.adsdk.AdError;
import com.komob.adsdk.AdParams;
import com.komob.adsdk.AdReward;
import com.komob.adsdk.OnAdSdkListener;
import com.komob.adsdk.adloader.base.SimpleAdBaseBaseListener;
import com.komob.adsdk.adloader.listener.IManagerListener;
import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.adloader.listener.OnAdBaseListener;
import com.komob.adsdk.adloader.listener.OnAdSdkInternalListener;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.core.AdPolicy;
import com.komob.adsdk.core.ModuleLoaderHelper;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.data.config.AdPlace;
import com.komob.adsdk.data.config.PidConfig;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.stat.EventImpl;
import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.utils.VUIHelper;

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
    private Context mContext;
    private OnAdSdkListener mOnAdSdkListener;
    private OnAdSdkListener mOnAdSdkLoadedListener;
    private OnAdSdkInternalListener mOnAdPlaceLoaderListener = new AdPlaceLoaderListener();
    // 加载native时的参数
    private AdParams mAdLoadParams;
    // 展示native时的参数
    private AdParams mAdShowParams;
    private boolean mHasNotifyLoaded = false;
    // banner和native的listener集合
    private Map<ISdkLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<ISdkLoader, OnAdBaseListener>();
    private WeakReference<Activity> mActivity;
    private WeakReference<ViewGroup> mAdContainer;
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
                        loader = ModuleLoaderHelper.generateSdkLoader(config);
                        if (loader != null) {
                            loader.init(mContext, config);
                            loader.setListenerManager(this);
                            mAdLoaders.add(loader);
                        }
                    }
                }
            }
        }
    }

    private Params getParams(ISdkLoader loader) {
        return getParams(loader, null);
    }

    private Params getParams(ISdkLoader loader, AdParams adParams) {
        Params params = null;
        if (adParams == null) {
            adParams = mAdLoadParams;
        }
        try {
            if (adParams != null) {
                params = adParams.getParams(loader.getSdkName());
                if (params == null) {
                    params = adParams.getParams(Constant.AD_SDK_COMMON);
                }
            }
            if (params == null) {
                params = new Params();
                params.setAdCardStyle(Constant.NATIVE_CARD_FULL);
                params.setBannerSize(Constant.AD_SDK_ADMOB, Constant.MEDIUM_RECTANGLE);
                Log.iv(Log.TAG, "use default ad params");
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
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
                    int banner = Constant.NO_SET;
                    try {
                        banner = Constant.Banner.valueOf(bannerSize);
                    } catch (Exception e) {
                    }
                    if (banner != Constant.NO_SET) {
                        return banner;
                    }
                }
            }
        }

        if (mAdPlace != null) {
            String bannerSize = mAdPlace.getBannerSize();
            if (!TextUtils.isEmpty(bannerSize)) {
                int banner = Constant.NO_SET;
                try {
                    banner = Constant.Banner.valueOf(bannerSize);
                } catch (Exception e) {
                }
                if (banner != Constant.NO_SET) {
                    return banner;
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
            Log.iv(Log.TAG, "error : " + e);
        }

        try {
            Map<String, Integer> map = getParams(loader).getBannerSize();
            Integer banner = map.get(Constant.AD_SDK_COMMON);
            if (banner != null) {
                return banner.intValue();
            }
        } catch (Exception e2) {
            Log.iv(Log.TAG, "error : " + e2);
        }
        return Constant.NO_SET;
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
                public void onAdLoadFailed(AdError error, String msg) {
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
    public void showInterstitial(String sceneName) {
        Log.iv(Log.TAG, "showInterstitial");
        showInterstitialInternal(sceneName);
    }

    private void showInterstitialInternal(String sceneName) {
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is empty for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_INTERSTITIAL, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        final List<ISdkLoader> list = new ArrayList<>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null && loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                list.add(loader);
            }
        }
        if (list == null || list.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is not ready for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_INTERSTITIAL, null, Constant.AD_ERROR_NOFILL);
                    }
                }
            });
            return;
        }
        sortLoadedLoaders(list);
        for (ISdkLoader loader : list) {
            if (loader != null && loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                if (loader.showInterstitial(sceneName)) {
                    AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                    break;
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
        setPlaceType(Constant.PLACE_TYPE_REWARD_VIDEO);
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
                public void onAdLoadFailed(AdError error, String msg) {
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
    public void showRewardedVideo(String sceneName) {
        Log.iv(Log.TAG, "showRewardedVideo");
        showRewardedVideoInternal(sceneName);
    }

    private void showRewardedVideoInternal(String sceneName) {
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is empty for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_REWARD, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        final List<ISdkLoader> list = new ArrayList<>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null && loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                list.add(loader);
            }
        }
        if (list == null || list.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is not ready for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_REWARD, null, Constant.AD_ERROR_NOFILL);
                    }
                }
            });
            return;
        }
        sortLoadedLoaders(list);
        for (ISdkLoader loader : list) {
            if (loader != null && loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                if (loader.showRewardedVideo(sceneName)) {
                    AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                    break;
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
                public void onAdLoadFailed(AdError error, String msg) {
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
    public void showSplash(ViewGroup viewGroup, String sceneName) {
        Log.iv(Log.TAG, "showSplash");
        showSplashInternal(viewGroup, sceneName);
    }

    private void showSplashInternal(ViewGroup viewGroup, String sceneName) {
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is empty for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_SPLASH, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        final List<ISdkLoader> list = new ArrayList<>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null && loader.isSplashType() && loader.isSplashLoaded()) {
                list.add(loader);
            }
        }
        if (list == null || list.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is not ready for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.TYPE_SPLASH, null, Constant.AD_ERROR_NOFILL);
                    }
                }
            });
            return;
        }
        sortLoadedLoaders(list);
        for (ISdkLoader loader : list) {
            if (loader != null && loader.isSplashType() && loader.isSplashLoaded()) {
                if (loader.showSplash(viewGroup, sceneName)) {
                    AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                    break;
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isAdViewLoaded(String adType) {
        if (mAdLoaders != null) {
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (TextUtils.equals(adType, Constant.TYPE_BANNER) && loader.isBannerLoaded()) {
                        Log.iv(Log.TAG, loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
                    if (TextUtils.equals(adType, Constant.TYPE_NATIVE) && loader.isNativeLoaded()) {
                        Log.iv(Log.TAG, loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
                    if (loader.isBannerLoaded() || loader.isNativeLoaded()) {
                        Log.iv(Log.TAG, loader.getAdPlaceName() + " - " + loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                        return true;
                    }
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        mAdLoadParams = adParams;
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
                public void onAdLoadFailed(AdError error, String msg) {
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
    public void showAdView(ViewGroup adContainer, String adType, AdParams adParams) {
        mAdShowParams = adParams;
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showAdViewInternal(adType, true);
        autoSwitchAdView();
    }

    private void showAdViewInternal(String adType, boolean needCounting) {
        if (mAdContainer == null) {
            Log.iv(Log.TAG, "error : ad view group is null for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.PLACE_TYPE_ADVIEW, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is empty for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.PLACE_TYPE_ADVIEW, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        final List<ISdkLoader> list = new ArrayList<>();
        for (ISdkLoader loader : mAdLoaders) {
            if (loader != null) {
                if (TextUtils.equals(Constant.TYPE_BANNER, adType)) {
                    if (loader.isBannerLoaded()) {
                        list.add(loader);
                    }
                } else if (TextUtils.equals(Constant.TYPE_NATIVE, adType)) {
                    if (loader.isNativeLoaded()) {
                        list.add(loader);
                    }
                } else {
                    if (((loader.isBannerType() && loader.isBannerLoaded())
                            || (loader.isNativeType() && loader.isNativeLoaded()))) {
                        list.add(loader);
                    }
                }
            }
        }
        if (list == null || list.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is not ready for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.PLACE_TYPE_ADVIEW, null, Constant.AD_ERROR_NOFILL);
                    }
                }
            });
            return;
        }
        sortLoadedLoaders(list);
        ViewGroup viewGroup = null;
        if (mAdContainer != null) {
            viewGroup = mAdContainer.get();
        }
        for (ISdkLoader loader : list) {
            if (loader != null && viewGroup != null) {
                if (loader.isBannerType() && loader.isBannerLoaded()) {
                    loader.showBanner(viewGroup);
                    if (needCounting) {
                        AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                    }
                    break;
                } else if (loader.isNativeType() && loader.isNativeLoaded()) {
                    loader.showNative(viewGroup, getParams(loader, mAdShowParams));
                    if (needCounting) {
                        AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                    }
                    break;
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
                if (isAnyLoaderLoaded(loader)) {
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
                    boolean loaded = isAnyLoaderLoaded(loader);
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
                    boolean loaded = isAnyLoaderLoaded(loader);
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(null, null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }
        if (!AdPolicy.get(mContext).allowAdPlaceLoad(mAdPlace)) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        List<PidConfig> pidList = mAdPlace.getPidList();
        if (pidList == null || pidList.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return;
        }

        mAdLoadParams = adParams;
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
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onLoadFailed(mAdPlace.getName(), null, null, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
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
                public void onAdLoadFailed(AdError error, String msg) {
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
    public boolean showComplexAds(String sceneName) {
        return showComplexAdsInternal(sceneName);
    }

    private boolean showComplexAdsInternal(String sceneName) {
        if (mAdLoaders == null || mAdLoaders.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is empty for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.PLACE_TYPE_COMPLEX, null, Constant.AD_ERROR_LOADER);
                    }
                }
            });
            return false;
        }
        final List<ISdkLoader> list = new ArrayList<>();
        for (ISdkLoader loader : mAdLoaders) {
            if (isAnyLoaderLoaded(loader)) {
                list.add(loader);
            }
        }
        if (list == null || list.isEmpty()) {
            Log.iv(Log.TAG, "error : ad loaders is not ready for place name : " + getPlaceName());
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mOnAdSdkListener != null) {
                        mOnAdSdkListener.onShowFailed(getPlaceName(), null, Constant.PLACE_TYPE_COMPLEX, null, Constant.AD_ERROR_NOFILL);
                    }
                }
            });
            return false;
        }
        sortLoadedLoaders(list);
        for (ISdkLoader loader : list) {
            if (loader != null) {
                if (loader.isRewardedVideoType() && loader.isRewardedVideoLoaded()) {
                    if (loader.showRewardedVideo(sceneName)) {
                        AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                        return true;
                    }
                } else if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                    if (loader.showInterstitial(sceneName)) {
                        AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                        return true;
                    }
                } else if (loader.isSplashType() && loader.isSplashLoaded()) {
                    if (loader.showSplash(null, sceneName)) {
                        AdPolicy.get(mContext).reportAdPlaceShow(getPlaceName(), mAdPlace);
                        return true;
                    }
                } else if ((loader.isBannerType() && loader.isBannerLoaded())
                        || (loader.isNativeType() && loader.isNativeLoaded())) {
                    showAdViewWithUI(loader, sceneName);
                    return true;
                }
            }
        }
        return false;
    }

    private void showAdViewWithUI(ISdkLoader iSdkLoader, String sceneName) {
        Log.iv(Log.TAG, "show complex ads for banner or native");
        try {
            Params params = getParams(iSdkLoader);
            try {
                if (params != null && DataManager.get(mContext).isComplexNativeFull()) {
                    params.setAdCardStyle(Constant.NATIVE_CARD_FULL_LIST.get(new Random().nextInt(Constant.NATIVE_CARD_FULL_LIST.size())));
                    params.setAdRootLayout(0);
                }
            } catch (Exception e) {
            }
            if (params != null) {
                params.setSceneName(sceneName);
            }
            showNativeInterstitialAds(iSdkLoader, params);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private void showNativeInterstitialAds(ISdkLoader iSdkLoader, Params params) {
        Activity activity = ActivityMonitor.get(mContext).getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        try {
            final Dialog dialog = new Dialog(activity, android.R.style.Theme_Material_Light_NoActionBar);
            VUIHelper vuiHelper = new VUIHelper();
            View adRootView = vuiHelper.generateNativeView(mContext, iSdkLoader, params, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                    }
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    try {
                        if (iSdkLoader != null) {
                            iSdkLoader.notifyAdViewUIDismiss();
                        }
                    } catch (Exception e) {
                    }
                }
            });
            dialog.setContentView(adRootView);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
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
                if (isAnyLoaderLoaded(loader)) {
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
    public double getMaxRevenue(String adType, boolean containSlave) {
        double maxValue = -1f;
        if (mAdLoaders != null && !mAdLoaders.isEmpty())
            for (ISdkLoader loader : mAdLoaders) {
                if (loader != null) {
                    if (containSlave || !loader.isSlaveAds()) {
                        double tmpValue = -1f;
                        if (TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                            if (loader.isBannerLoaded()) {
                                tmpValue = loader.getRevenue();
                            }
                        } else if (TextUtils.equals(adType, Constant.TYPE_NATIVE)) {
                            if (loader.isNativeLoaded()) {
                                tmpValue = loader.getRevenue();
                            }
                        } else if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)) {
                            if (loader.isInterstitialLoaded()) {
                                tmpValue = loader.getRevenue();
                            }
                        } else if (TextUtils.equals(adType, Constant.TYPE_REWARD)) {
                            if (loader.isRewardedVideoLoaded()) {
                                tmpValue = loader.getRevenue();
                            }
                        } else if (TextUtils.equals(adType, Constant.TYPE_SPLASH)) {
                            if (loader.isSplashLoaded()) {
                                tmpValue = loader.getRevenue();
                            }
                        } else {
                            if (isAnyLoaderLoaded(loader)) {
                                tmpValue = loader.getRevenue();
                            }
                        }
                        if (tmpValue > maxValue) {
                            maxValue = tmpValue;
                        }
                    }
                }
            }
        return maxValue;
    }

    private boolean isAnyLoaderLoaded(ISdkLoader iSdkLoader) {
        if (iSdkLoader != null) {
            return iSdkLoader.isBannerLoaded()
                    || iSdkLoader.isNativeLoaded()
                    || iSdkLoader.isInterstitialLoaded()
                    || iSdkLoader.isRewardedVideoLoaded()
                    || iSdkLoader.isSplashLoaded();
        }
        return false;
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

    private synchronized boolean hasNotifyLoaded() {
        if (mAdPlace != null && mAdPlace.isLoadOnlyOnce()) {
            return mHasNotifyLoaded;
        }
        return false;
    }

    private synchronized void notifyAdLoaded() {
        mHasNotifyLoaded = true;
    }

    private void resetAdLoaded() {
        mHasNotifyLoaded = false;
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
        } else if (TextUtils.equals(mPlaceType, Constant.PLACE_TYPE_REWARD_VIDEO)) {
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
        public void onLoaded(String placeName, String source, String adType, String pid, String network, double revenue, long costTime, boolean cached) {
            resetRetryTimes(placeName, source, adType);
            if (!cached) {
                Log.iv(Log.TAG, "notify callback onLoaded place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , pid : " + pid + " , network : " + network + " , revenue : " + revenue + " , cost time : " + costTime);
            }
            if (hasNotifyLoaded()) {
                Log.iv(Log.TAG, "place name : " + placeName + " , sdk : " + source + " , type : " + adType + " is loaded, but has already notified ******************");
                return;
            }
            notifyAdLoaded();
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
        public void onImpression(String placeName, String source, String adType, String network, String pid, String sceneName) {
            Log.iv(Log.TAG, "notify callback onImp place name : " + placeName + " , sdk : " + source + " , type : " + adType + " , network : " + network + " , pid : " + pid + " , scene name : " + sceneName);
            if (mOnAdSdkLoadedListener != null) {
                mOnAdSdkLoadedListener.onImpression(placeName, source, adType, network, pid, sceneName);
            }
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onImpression(placeName, source, adType, network, pid, sceneName);
            }
        }

        @Override
        public void onLoadFailed(String placeName, String source, String adType, String pid, AdError error, String msg) {
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
        public void onShowFailed(String placeName, String source, String adType, String pid, AdError error, String msg) {
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
            if (TextUtils.equals(adType, Constant.TYPE_NATIVE)
                    || TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                if ((mAdPlace != null && mAdPlace.isClickSwitch())) {
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
        if (isAdViewLoaded(null)) {
            showAdViewInternal(null, false);
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
        if (isInterstitialLoaded() || isAdViewLoaded(null) || isComplexAdsLoaded()) {
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
        if (!isAdViewLoaded(null)) {
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
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
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

    @Override
    public String toString() {
        return getPlaceName();
    }

    private void sortLoadedLoaders(List<ISdkLoader> list) {
        try {
            if (mAdPlace == null || !mAdPlace.isOrder()) {
                Log.iv(Log.TAG, "disable value order on showing ads");
                return;
            }
            if (list == null || list.size() < 2) {
                return;
            }
            Collections.sort(list, new Comparator<ISdkLoader>() {
                @Override
                public int compare(ISdkLoader o1, ISdkLoader o2) {
                    try {
                        return Double.compare(o2.getRevenue(), o1.getRevenue());
                    } catch (Exception e) {
                    }
                    return 0;
                }
            });
            String logText = "";
            for (ISdkLoader iSdkLoader : list) {
                if (iSdkLoader != null) {
                    logText += "[network sort]" + iSdkLoader.getAdPlaceName() + "|" + iSdkLoader.getAdType() + "|" + iSdkLoader.getSdkName() + "|" + iSdkLoader.getNetwork() + "|" + iSdkLoader.getRevenue() + "\n";
                }
            }
            Log.iv(Log.TAG, logText);
        } catch (Exception e) {
        }
    }
}