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

    private CoreLoader getAdLoader(String pidName) {
        return getAdLoader(pidName, false);
    }

    private CoreLoader getAdLoader(String pidName, boolean forLoad) {
        LogHelper.d(LogHelper.TAG, "getAdLoader forLoad : " + forLoad);
        // 获取引用的pidname
        String refPidName = pidName;

        LogHelper.v(LogHelper.TAG, "pidName : " + pidName + " , refPidName : " + refPidName);
        boolean useShareObject = false;
        // 如果共享loader对象
        if (!TextUtils.equals(pidName, refPidName)) {
            useShareObject = true;
        }
        if (useShareObject) {
            // 将共享对象赋值给新的场景
            if (mAdLoaders.containsKey(refPidName) && !mAdLoaders.containsKey(pidName)) {
                mAdLoaders.put(pidName, mAdLoaders.get(refPidName));
            }
        }
        CoreLoader loader = mAdLoaders.get(pidName);
        if (!forLoad) {
            return loader;
        }
        MpPlace mpPlace = DataConfig.get(mContext).getRemoteAdPlace(refPidName);
        // loader为null，或者AdPlace内容有变化，则重新加载loader
        if (loader == null || loader.needReload(mpPlace)) {
            loader = createAdPlaceLoader(refPidName, mpPlace);
            if (loader != null) {
                mAdLoaders.put(pidName, loader);
                if (useShareObject && !mAdLoaders.containsKey(refPidName)) {
                    mAdLoaders.put(refPidName, loader);
                }
            }
        }
        return loader;
    }

    /**
     * 如果adPlace和adIds为空，则使用本地的adPlace和adIds
     *
     * @param pidName
     * @param mpPlace
     * @return
     */
    private CoreLoader createAdPlaceLoader(String pidName, MpPlace mpPlace) {
        CoreLoader loader = null;
        boolean useRemote = true;
        MpConfig localConfig = DataConfig.get(mContext).getAdConfig();
        if (localConfig != null && mpPlace == null) {
            mpPlace = localConfig.get(pidName);
            useRemote = false;
        }
        LogHelper.v(LogHelper.TAG, "pidName : " + pidName + " , mpPlace : " + mpPlace);
        if (mpPlace != null) {
            loader = new CoreLoader(mContext);
            loader.setAdPlaceConfig(mpPlace);
            loader.init();
        }
        LogHelper.v(LogHelper.TAG, "pidName [" + pidName + "] use remote adplace : " + useRemote);
        return loader;
    }

    public boolean isInterstitialLoaded(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isInterstitialLoaded();
        }
        return false;
    }

    public void loadInterstitial(String pidName) {
        loadInterstitial(null, pidName);
    }

    public void loadInterstitial(String pidName, OnMpSdkListener l) {
        loadInterstitial(null, pidName, l);
    }

    public void loadInterstitial(Activity activity, String pidName) {
        loadInterstitial(activity, pidName, null);
    }

    public void loadInterstitial(Activity activity, String pidName, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(pidName, true);
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
                l.onError(pidName, null);
            }
        }
    }

    public void showInterstitial(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showInterstitial();
        }
    }

    public boolean isBannerLoaded(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isBannerLoaded();
        }
        return false;
    }

    public void loadBanner(String pidName, OnMpSdkListener l) {
        loadBanner(pidName, new MpParams.Builder().build(), l);
    }

    public void loadBanner(String pidName, MpParams mpParams) {
        loadBanner(pidName, mpParams, null);
    }

    public void loadBanner(String pidName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadBanner(mpParams);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showBanner(String pidName, ViewGroup adContainer) {
        showBanner(pidName, null, adContainer);
    }

    public void showBanner(String pidName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showBanner(adContainer, mpParams);
        }
    }
    ///////////////////////////////////////////////////////////////////////
    public boolean isNativeLoaded(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isNativeLoaded();
        }
        return false;
    }

    public void loadNative(String pidName, OnMpSdkListener l) {
        loadNative(pidName, new MpParams.Builder().build(), l);
    }

    public void loadNative(String pidName, MpParams mpParams) {
        loadNative(pidName, mpParams, null);
    }

    public void loadNative(String pidName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadNative(mpParams);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showNative(String pidName, ViewGroup adContainer) {
        showNative(pidName, null, adContainer);
    }

    public void showNative(String pidName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showNative(adContainer, mpParams);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    public boolean isCommonViewLoaded(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isCommonViewLoaded();
        }
        return false;
    }

    public void loadCommonView(String pidName, OnMpSdkListener l) {
        loadCommonView(pidName, new MpParams.Builder().build(), l);
    }

    public void loadCommonView(String pidName, MpParams mpParams) {
        loadCommonView(pidName, mpParams, null);
    }

    public void loadCommonView(String pidName, MpParams mpParams, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(pidName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l);
            loader.loadCommonView(mpParams);
        } else {
            if (l != null) {
                l.onError(pidName, null);
            }
        }
    }

    public void showCommonView(String pidName, ViewGroup adContainer) {
        showCommonView(pidName, null, adContainer);
    }

    public void showCommonView(String pidName, MpParams mpParams, ViewGroup adContainer) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showCommonView(adContainer, mpParams);
        }
    }
    /////////////////////////////////////////////////////////////////////////

    public boolean isRewardVideoLoaded(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            return loader.isRewardVideoLoaded();
        }
        return false;
    }

    public void loadRewardVideo(String pidName) {
        loadRewardVideo(null, pidName);
    }

    public void loadRewardVideo(String pidName, OnMpSdkListener l) {
        loadRewardVideo(null, pidName, l);
    }

    public void loadRewardVideo(Activity activity, String pidName) {
        loadRewardVideo(activity, pidName, null);
    }

    public void loadRewardVideo(Activity activity, String pidName, OnMpSdkListener l) {
        CoreLoader loader = getAdLoader(pidName, true);
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
                l.onError(pidName, null);
            }
        }
    }

    public void showRewardVideo(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.showRewardVideo();
        }
    }

    public void resume(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.resume();
        }
    }

    public void pause(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
        if (loader != null) {
            loader.pause();
        }
    }

    public void destroy(String pidName) {
        CoreLoader loader = getAdLoader(pidName);
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
