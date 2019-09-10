package com.simple.mpsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.mp.md.simple.BuildConfig;
import com.simple.mpsdk.config.MpConfig;
import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.data.DataConfig;
import com.simple.mpsdk.framework.CoreLoader;
import com.simple.mpsdk.listener.OnMpSdkListener;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.stat.ReportImpl;
import com.simple.mpsdk.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public class MpSdk {

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_SMALL = Constant.NATIVE_CARD_SMALL;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_MEDIUM = Constant.NATIVE_CARD_MEDIUM;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_LARGE = Constant.NATIVE_CARD_LARGE;

    /**
     * 原生广告模板值
     */
    public static final int NATIVE_CARD_FULL = Constant.NATIVE_CARD_FULL;

    /**
     * BANNER类型
     */
    public static final String AD_TYPE_BANNER = Constant.TYPE_BANNER;

    /**
     * INTERSTITIAL类型
     */
    public static final String AD_TYPE_INTERSTITIAL = Constant.TYPE_INTERSTITIAL;

    /**
     * NATIVE类型
     */
    public static final String AD_TYPE_NATIVE = Constant.TYPE_NATIVE;

    /**
     * REWARD类型
     */
    public static final String AD_TYPE_REWARD = Constant.TYPE_REWARD;

    private static MpSdk sMpSdk;

    private Context mContext;
    private Map<String, CoreLoader> mAdLoaders = new HashMap<String, CoreLoader>();
    private WeakReference<Activity> mActivity;

    private MpSdk(Context context) {
        mContext = context.getApplicationContext();
    }

    public static MpSdk get(Context context) {
        if (sMpSdk == null) {
            create(context);
        }
        if (sMpSdk != null) {
            if (context instanceof Activity) {
                sMpSdk.setActivity((Activity) context);
            }
        }
        return sMpSdk;
    }

    private static void create(Context context) {
        synchronized (MpSdk.class) {
            if (sMpSdk == null) {
                sMpSdk = new MpSdk(context);
            }
        }
    }

    private void setActivity(Activity activity) {
        if (activity != null) {
            mActivity = new WeakReference<Activity>(activity);
        }
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 初始化
     */
    public void init() {
        LogHelper.v(LogHelper.TAG, "simple version : " + getSdkVersion());
        DataConfig.get(mContext).init();
        ReportImpl.get().init();
    }

    /**
     * 设置appsflyer归因
     *
     * @param afStatus
     * @param mediaSource
     */
    public void setAttribution(String afStatus, String mediaSource) {
        if (!TextUtils.isEmpty(afStatus)) {
            Utils.putString(mContext, Constant.AF_STATUS, afStatus);
        }
        if (!TextUtils.isEmpty(mediaSource)) {
            Utils.putString(mContext, Constant.AF_MEDIA_SOURCE, mediaSource);
        }
    }

    public String getString(String key) {
        return DataConfig.get(mContext).getString(key);
    }

    private CoreLoader getAdLoader(String placeName) {
        return getAdLoader(placeName, false);
    }

    private CoreLoader getAdLoader(String placeName, boolean forLoad) {
        LogHelper.d(LogHelper.TAG, "get mp loader is for load : " + forLoad);
        // 获取引用的place name
        String refPlaceName = placeName;

        LogHelper.v(LogHelper.TAG, "placeName : " + placeName + " , refPlaceName : " + refPlaceName);
        boolean useShareObject = false;
        // 如果共享loader对象
        if (!TextUtils.equals(placeName, refPlaceName)) {
            useShareObject = true;
        }
        if (useShareObject) {
            // 将共享对象赋值给新的场景
            if (mAdLoaders.containsKey(refPlaceName) && !mAdLoaders.containsKey(placeName)) {
                mAdLoaders.put(placeName, mAdLoaders.get(refPlaceName));
            }
        }
        CoreLoader loader = mAdLoaders.get(placeName);
        if (!forLoad) {
            return loader;
        }
        MpPlace mpPlace = DataConfig.get(mContext).getRemoteAdPlace(refPlaceName);
        // loader为null，或者AdPlace内容有变化，则重新加载loader
        if (loader == null || loader.needReload(mpPlace)) {
            loader = createAdPlaceLoader(refPlaceName, mpPlace);
            if (loader != null) {
                mAdLoaders.put(placeName, loader);
                if (useShareObject && !mAdLoaders.containsKey(refPlaceName)) {
                    mAdLoaders.put(refPlaceName, loader);
                }
            }
        }
        return loader;
    }

    /**
     * 如果adPlace和adIds为空，则使用本地的adPlace和adIds
     *
     * @param placeName
     * @param mpPlace
     * @return
     */
    private CoreLoader createAdPlaceLoader(String placeName, MpPlace mpPlace) {
        CoreLoader loader = null;
        boolean useRemote = true;
        MpConfig localConfig = DataConfig.get(mContext).getAdConfig();
        if (localConfig != null && mpPlace == null) {
            mpPlace = localConfig.get(placeName);
            useRemote = false;
        }
        LogHelper.v(LogHelper.TAG, "placeName : " + placeName + " , mpPlace : " + mpPlace);
        if (mpPlace != null) {
            loader = new CoreLoader(mContext);
            loader.setAdPlaceConfig(mpPlace);
            loader.init();
        }
        LogHelper.v(LogHelper.TAG, "placeName [" + placeName + "] use remote adplace : " + useRemote);
        return loader;
    }

    public boolean isInterstitialLoaded(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isInterstitialLoaded();
        }
        return false;
    }

    public void loadInterstitial(String placeName) {
        loadInterstitial(null, placeName);
    }

    public void loadInterstitial(String placeName, OnMpSdkListener l) {
        loadInterstitial(null, placeName, l);
    }

    public void loadInterstitial(Activity activity, String placeName) {
        loadInterstitial(activity, placeName, null);
    }

    public void loadInterstitial(Activity activity, String placeName, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadInterstitial(activity);
        } else {
            if (l != null) {
                l.onError(placeName, null);
            }
        }
    }

    public void showInterstitial(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showInterstitial();
        }
    }

    public boolean isBannerLoaded(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isBannerLoaded();
        }
        return false;
    }

    public void loadBanner(String placeName, OnMpSdkListener l) {
        loadBanner(placeName, new MpParams.Builder().build(), l);
    }

    public void loadBanner(String placeName, MpParams mpParams) {
        loadBanner(placeName, mpParams, null);
    }

    public void loadBanner(String placeName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadBanner(mpParams);
        } else {
            if (l != null) {
                l.onError(placeName, null);
            }
        }
    }

    public void showBanner(String placeName, ViewGroup adContainer) {
        showBanner(placeName, null, adContainer);
    }

    public void showBanner(String placeName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showBanner(adContainer, mpParams);
        }
    }
    ///////////////////////////////////////////////////////////////////////
    public boolean isNativeLoaded(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isNativeLoaded();
        }
        return false;
    }

    public void loadNative(String placeName, OnMpSdkListener l) {
        loadNative(placeName, new MpParams.Builder().build(), l);
    }

    public void loadNative(String placeName, MpParams mpParams) {
        loadNative(placeName, mpParams, null);
    }

    public void loadNative(String placeName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadNative(mpParams);
        } else {
            if (l != null) {
                l.onError(placeName, null);
            }
        }
    }

    public void showNative(String placeName, ViewGroup adContainer) {
        showNative(placeName, null, adContainer);
    }

    public void showNative(String placeName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showNative(adContainer, mpParams);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    public boolean isCommonViewLoaded(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isCommonViewLoaded();
        }
        return false;
    }

    public void loadCommonView(String placeName, OnMpSdkListener l) {
        loadCommonView(placeName, new MpParams.Builder().build(), l);
    }

    public void loadCommonView(String placeName, MpParams mpParams) {
        loadCommonView(placeName, mpParams, null);
    }

    public void loadCommonView(String placeName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadCommonView(mpParams);
        } else {
            if (l != null) {
                l.onError(placeName, null);
            }
        }
    }

    public void showCommonView(String placeName, ViewGroup adContainer) {
        showCommonView(placeName, null, adContainer);
    }

    public void showCommonView(String placeName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showCommonView(adContainer, mpParams);
        }
    }
    /////////////////////////////////////////////////////////////////////////

    public boolean isRewardVideoLoaded(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isRewardVideoLoaded();
        }
        return false;
    }

    public void loadRewardVideo(String placeName) {
        loadRewardVideo(null, placeName);
    }

    public void loadRewardVideo(String placeName, OnMpSdkListener l) {
        loadRewardVideo(null, placeName, l);
    }

    public void loadRewardVideo(Activity activity, String placeName) {
        loadRewardVideo(activity, placeName, null);
    }

    public void loadRewardVideo(Activity activity, String placeName, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadRewardVideo(activity);
        } else {
            if (l != null) {
                l.onError(placeName, null);
            }
        }
    }

    public void showRewardVideo(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showRewardVideo();
        }
    }

    public void resume(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.resume();
        }
    }

    public void pause(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.pause();
        }
    }

    public void destroy(String placeName) {
        CoreLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.destroy();
        }
    }

    private Context getFinalContext() {
        Context context = null;
        if (mActivity != null && mActivity.get() != null) {
            context = mActivity.get();
        } else {
            context = mContext;
        }
        return context;
    }
}
