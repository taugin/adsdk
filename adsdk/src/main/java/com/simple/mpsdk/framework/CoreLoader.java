package com.simple.mpsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.simple.mpsdk.MpParams;
import com.simple.mpsdk.mopubloader.SimpleMpBaseBaseListener;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.internallistener.IManagerListener;
import com.simple.mpsdk.internallistener.ISdkLoader;
import com.simple.mpsdk.internallistener.OnMpBaseListener;
import com.simple.mpsdk.listener.OnMpSdkListener;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.mopubloader.MopubLoader;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个广告位对应一个AdPlaceLoader对象
 */

public class CoreLoader extends AbCoreLoader implements IManagerListener {
    private MpPlace mMpPlace;
    private Context mContext;
    private OnMpSdkListener mOnMpSdkListener;
    private MpParams mMpParams;
    // banner和native的listener集合
    private Map<ISdkLoader, OnMpBaseListener> mAdViewListener = new ConcurrentHashMap<ISdkLoader, OnMpBaseListener>();
    private WeakReference<Activity> mActivity;
    private WeakReference<ViewGroup> mAdContainer;
    private ISdkLoader mCurrentAdLoader;

    public CoreLoader(Context context) {
        mContext = context;
    }

    @Override
    public void init() {
        generateLoaders();
    }

    @Override
    public void setAdPlaceConfig(MpPlace mpPlace) {
        mMpPlace = mpPlace;
    }

    @Override
    public boolean needReload(MpPlace mpPlace) {
        if (mMpPlace != null && mpPlace != null) {
            LogHelper.d(LogHelper.TAG, "pidName : " + mMpPlace.getName() + " , usingUnique : " + mMpPlace.getUniqueValue() + " , remoteUnique : " + mpPlace.getUniqueValue());
            return !TextUtils.equals(mMpPlace.getUniqueValue(), mpPlace.getUniqueValue());
        }
        return false;
    }

    private void generateLoaders() {
        if (mMpPlace != null) {
            ISdkLoader loader = new MopubLoader();
            loader.init(mContext, mMpPlace);
            loader.setListenerManager(this);
            mCurrentAdLoader = loader;
        }
    }

    private Params getParams(ISdkLoader loader) {
        Params params = null;
        try {
            if (mMpParams != null) {
                params = mMpParams.getParams();
            }
            if (params == null) {
                params = new Params();
                params.setAdCardStyle(Constant.NATIVE_CARD_FULL);
                LogHelper.v(LogHelper.TAG, "use default ad params");
            }
        } catch (Exception e) {
            LogHelper.e(LogHelper.TAG, "error : " + e, e);
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

    private int getBannerSize(ISdkLoader loader) {
        return 0;
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
    public void setOnAdSdkListener(OnMpSdkListener l) {
        mOnMpSdkListener = l;
    }

    @Override
    public boolean isInterstitialLoaded() {
        if (mCurrentAdLoader != null) {
            if (mCurrentAdLoader.isInterstitialType() && mCurrentAdLoader.isInterstitialLoaded()) {
                LogHelper.v(LogHelper.TAG, mCurrentAdLoader.getSdkName() + " - " + mCurrentAdLoader.getAdType() + " has loaded");
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
        if (mMpPlace == null) {
            if (mOnMpSdkListener != null) {
                mOnMpSdkListener.onError(null, null);
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
            registerAdBaseListener(loader, new SimpleMpBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isInterstitialType()) {
                loader.loadInterstitial();
            } else {
                LogHelper.d(LogHelper.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showInterstitial() {
        LogHelper.d(LogHelper.TAG, "show interstitial");
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
                LogHelper.v(LogHelper.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
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
        if (mMpPlace == null) {
            if (mOnMpSdkListener != null) {
                mOnMpSdkListener.onError(null, null);
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
            registerAdBaseListener(loader, new SimpleMpBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isRewardedVideoType()) {
                loader.loadRewardedVideo();
            } else {
                LogHelper.d(LogHelper.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示插屏
     */
    @Override
    public void showRewardVideo() {
        LogHelper.d(LogHelper.TAG, "show reward video");
        showRewardVideoInternal();
    }

    private void showRewardVideoInternal() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            if (loader.isRewardedVideoType() && loader.isRewaredVideoLoaded()) {
                loader.showRewardedVideo();
            } else {
                LogHelper.d(LogHelper.TAG, "not load ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public boolean isBannerLoaded() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null && loader.isBannerLoaded()) {
            LogHelper.v(LogHelper.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
            return true;
        }
        return false;
    }

    /**
     * 加载banner和native广告
     *
     * @param mpParams
     */
    @Override
    public void loadBanner(MpParams mpParams) {
        mMpParams = mpParams;
        if (mMpPlace == null) {
            if (mOnMpSdkListener != null) {
                mOnMpSdkListener.onError(null, null);
            }
            return;
        }
        loadBannerLocked();
    }

    private void loadBannerLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleMpBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else {
                LogHelper.d(LogHelper.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param mpParams
     */
    @Override
    public void showBanner(ViewGroup adContainer, MpParams mpParams) {
        LogHelper.d(LogHelper.TAG, "show banner");
        if (mpParams != null) {
            mMpParams = mpParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showBannerInternal(true);
    }

    private void showBannerInternal(boolean needCounting) {
        LogHelper.d(LogHelper.TAG, "show banner internal");
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
            LogHelper.v(LogHelper.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
            return true;
        }
        return false;
    }

    /**
     * 加载banner和native广告
     *
     * @param mpParams
     */
    @Override
    public void loadNative(MpParams mpParams) {
        mMpParams = mpParams;
        if (mMpPlace == null) {
            if (mOnMpSdkListener != null) {
                mOnMpSdkListener.onError(null, null);
            }
            return;
        }
        loadNativeLocked();
    }

    private void loadNativeLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleMpBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else {
                LogHelper.d(LogHelper.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param mpParams
     */
    @Override
    public void showNative(ViewGroup adContainer, MpParams mpParams) {
        LogHelper.d(LogHelper.TAG, "show native");
        if (mpParams != null) {
            mMpParams = mpParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showNativeInternal(true);
    }

    private void showNativeInternal(boolean needCounting) {
        LogHelper.d(LogHelper.TAG, "show native internal");
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

    ///////////////////////////////////////////////////////////////////
    @Override
    public boolean isCommonViewLoaded() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null && (loader.isNativeLoaded() || loader.isBannerLoaded())) {
            LogHelper.v(LogHelper.TAG, loader.getSdkName() + " - " + loader.getAdType() + " has loaded");
            return true;
        }
        return false;
    }

    /**
     * 加载banner和native广告
     *
     * @param mpParams
     */
    @Override
    public void loadCommonView(MpParams mpParams) {
        mMpParams = mpParams;
        if (mMpPlace == null) {
            if (mOnMpSdkListener != null) {
                mOnMpSdkListener.onError(null, null);
            }
            return;
        }
        loadCommonViewLocked();
    }

    private void loadCommonViewLocked() {
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            registerAdBaseListener(loader, new SimpleMpBaseBaseListener(loader.getName(),
                    loader.getAdType(), getPidByLoader(loader), this));
            if (loader.isNativeType()) {
                loader.loadNative(getParams(loader));
            } else if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else {
                LogHelper.d(LogHelper.TAG, "not supported ad type : " + loader.getName() + " - " + loader.getSdkName() + " - " + loader.getAdType());
            }
        }
    }

    /**
     * 展示广告(banner or native)
     *
     * @param adContainer
     * @param mpParams
     */
    @Override
    public void showCommonView(ViewGroup adContainer, MpParams mpParams) {
        LogHelper.d(LogHelper.TAG, "show common view");
        if (mpParams != null) {
            mMpParams = mpParams;
        }
        mAdContainer = new WeakReference<ViewGroup>(adContainer);
        showCommonViewInternal(true);
    }

    private void showCommonViewInternal(boolean needCounting) {
        LogHelper.d(LogHelper.TAG, "show common view internal");
        ISdkLoader loader = mCurrentAdLoader;
        if (loader != null) {
            ViewGroup viewGroup = null;
            if (mAdContainer != null) {
                viewGroup = mAdContainer.get();
            }
            if (loader.isNativeLoaded() && viewGroup != null) {
                loader.showNative(viewGroup, getParams(loader));
            } else if (loader.isBannerLoaded() && viewGroup != null) {
                loader.showBanner(viewGroup);
            }
        }
    }

    @Override
    public void resume() {
        LogHelper.d(LogHelper.TAG, "");
        if (mCurrentAdLoader != null) {
            mCurrentAdLoader.resume();
        }
    }

    @Override
    public void pause() {
        LogHelper.d(LogHelper.TAG, "");
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
    public synchronized void registerAdBaseListener(ISdkLoader loader, OnMpBaseListener l) {
        if (mAdViewListener != null) {
            mAdViewListener.put(loader, l);
        }
    }

    @Override
    public synchronized OnMpBaseListener getAdBaseListener(ISdkLoader loader) {
        OnMpBaseListener listener = null;
        if (mAdViewListener != null) {
            listener = mAdViewListener.get(loader);
        }
        return listener;
    }

    @Override
    public OnMpSdkListener getOnAdSdkListener() {
        return mOnMpSdkListener;
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