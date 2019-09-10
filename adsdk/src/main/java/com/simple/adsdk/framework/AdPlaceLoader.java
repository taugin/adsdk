package com.simple.adsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.simple.adsdk.AdParams;
import com.simple.adsdk.baseloader.SimpleAdBaseBaseListener;
import com.simple.adsdk.config.AdPlace;
import com.simple.adsdk.constant.Constant;
import com.simple.adsdk.internallistener.IManagerListener;
import com.simple.adsdk.internallistener.ISdkLoader;
import com.simple.adsdk.internallistener.OnAdBaseListener;
import com.simple.adsdk.listener.OnAdSdkListener;
import com.simple.adsdk.log.Log;
import com.simple.adsdk.mopubloader.MopubLoader;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个广告位对应一个AdPlaceLoader对象
 */

public class AdPlaceLoader extends AdBaseLoader implements IManagerListener {
    private AdPlace mAdPlace;
    private Context mContext;
    private OnAdSdkListener mOnAdSdkListener;
    private AdParams mAdParams;
    // banner和native的listener集合
    private Map<ISdkLoader, OnAdBaseListener> mAdViewListener = new ConcurrentHashMap<ISdkLoader, OnAdBaseListener>();
    private WeakReference<Activity> mActivity;
    private WeakReference<ViewGroup> mAdContainer;
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
    public boolean needReload(AdPlace adPlace) {
        if (mAdPlace != null && adPlace != null) {
            Log.d(Log.TAG, "pidName : " + mAdPlace.getName() + " , usingUnique : " + mAdPlace.getUniqueValue() + " , remoteUnique : " + adPlace.getUniqueValue());
            return !TextUtils.equals(mAdPlace.getUniqueValue(), adPlace.getUniqueValue());
        }
        return false;
    }

    private void generateLoaders() {
        if (mAdPlace != null) {
            ISdkLoader loader = new MopubLoader();
            loader.init(mContext, mAdPlace);
            loader.setListenerManager(this);
            mCurrentAdLoader = loader;
        }
    }

    private Params getParams(ISdkLoader loader) {
        Params params = null;
        try {
            if (mAdParams != null) {
                params = mAdParams.getParams();
            }
            if (params == null) {
                params = new Params();
                params.setAdCardStyle(Constant.NATIVE_CARD_FULL);
                Log.v(Log.TAG, "use default ad params");
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return params;
    }

    private String getPidByLoader(ISdkLoader loader) {
        try {
            return loader.getAdPlace().getPid();
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
        if (mCurrentAdLoader != null) {
            if (mCurrentAdLoader.isInterstitialType() && mCurrentAdLoader.isInterstitialLoaded()) {
                Log.v(Log.TAG, mCurrentAdLoader.getSdkName() + " - " + mCurrentAdLoader.getAdType() + " has loaded");
                return true;
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
                mOnAdSdkListener.onError(null, null);
            }
            return;
        }
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        loadInterstitialLocked();
    }

    private void loadInterstitialLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
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
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            if (loader.isInterstitialType() && loader.isInterstitialLoaded()) {
                loader.showInterstitial();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isRewardVideoLoaded() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
                return true;
            }
        }
        return false;
    }

    /**
     * 加载插屏
     */
    @Override
    public void loadRewardVideo(Activity activity) {
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null);
            }
            return;
        }
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
        loadRewardVideoLocked();
    }

    private void loadRewardVideoLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showRewardVideo() {
        Log.d(Log.TAG, "showInterstitial");
        showRewardVideoInternal();
    }

    private void showRewardVideoInternal() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                loader.showRewardedVideo();
            } else {
                Log.d(Log.TAG, "not load ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public boolean isBannerLoaded() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null && loader.isBannerLoaded()) {
            Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
            return true;
        }
        return false;
    }

    /**
     * 加载banner和native广告
     *
     * @param adParams
     */
    @Override
    public void loadBanner(AdParams adParams) {
        mAdParams = adParams;
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null);
            }
            return;
        }
        loadBannerLocked();
    }

    private void loadBannerLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isBannerType()) {
                loader.loadBanner(0);
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param adParams
     */
    @Override
    public void showBanner(ViewGroup adContainer, AdParams adParams) {
        Log.d(Log.TAG, "showAdView");
        if (adParams != null) {
            mAdParams = adParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showBannerInternal(true);
    }

    private void showBannerInternal(boolean needCounting) {
        Log.d(Log.TAG, "showAdViewInternal");
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            ViewGroup viewGroup = null;
            if (mAdContainer != null) {
                viewGroup = mAdContainer.get();
            }
            if (loader.isBannerLoaded() && viewGroup != null) {
                loader.showBanner(viewGroup);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public boolean isNativeLoaded() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null && loader.isNativeLoaded()) {
            Log.v(Log.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
            return true;
        }
        return false;
    }

    /**
     * 加载banner和native广告
     *
     * @param adParams
     */
    @Override
    public void loadNative(AdParams adParams) {
        mAdParams = adParams;
        if (mAdPlace == null) {
            if (mOnAdSdkListener != null) {
                mOnAdSdkListener.onError(null, null);
            }
            return;
        }
        loadNativeLocked();
    }

    private void loadNativeLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleAdBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else {
                Log.d(Log.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param adParams
     */
    @Override
    public void showNative(ViewGroup adContainer, AdParams adParams) {
        Log.d(Log.TAG, "showAdView");
        if (adParams != null) {
            mAdParams = adParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showNativeInternal(true);
    }

    private void showNativeInternal(boolean needCounting) {
        Log.d(Log.TAG, "showAdViewInternal");
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            ViewGroup viewGroup = null;
            if (mAdContainer != null) {
                viewGroup = mAdContainer.get();
            }
            if (loader.isNativeLoaded() && viewGroup != null) {
                loader.showNative(viewGroup, getParams(loader));
            }
        }
    }

    @Override
    public void resume() {
        Log.d(Log.TAG, "");
        if (mCurrentAdLoader != null) {
            mCurrentAdLoader.resume();
        }
    }

    @Override
    public void pause() {
        Log.d(Log.TAG, "");
        if (mCurrentAdLoader != null) {
            mCurrentAdLoader.pause();
        }
    }

    @Override
    public void destroy() {
        if (mCurrentAdLoader != null) {
            mCurrentAdLoader.destroy();
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
    public OnAdSdkListener getOnAdSdkListener() {
        return mOnAdSdkListener;
    }

    @Override
    public boolean isCurrent(String type, String pidName) {
        boolean isCurrentLoader = false;
        if (mCurrentAdLoader != null) {
            isCurrentLoader = TextUtils.equals(getPidByLoader(mCurrentAdLoader), pidName);
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

}