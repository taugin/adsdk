package com.hauyu.adsdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.hauyu.adsdk.adloader.applovin.AppLovinLoader;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.core.framework.ActivityMonitor;
import com.hauyu.adsdk.core.framework.AdLoadManager;
import com.hauyu.adsdk.core.framework.AdPlaceLoader;
import com.hauyu.adsdk.core.framework.LimitAdsManager;
import com.hauyu.adsdk.core.framework.ReplaceManager;
import com.hauyu.adsdk.data.DataManager;
import com.hauyu.adsdk.data.config.AdPlace;
import com.hauyu.adsdk.data.config.PlaceConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.stat.EventImpl;
import com.unity3d.widget.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdSdk {

    public static final String AD_TYPE_BANNER = Constant.TYPE_BANNER;
    public static final String AD_TYPE_NATIVE = Constant.TYPE_NATIVE;
    public static final String AD_TYPE_INTERSTITIAL = Constant.TYPE_INTERSTITIAL;
    public static final String AD_TYPE_REWARD = Constant.TYPE_REWARD;
    public static final String AD_TYPE_SPLASH = Constant.TYPE_SPLASH;

    public static final String PLACE_TYPE_ADVIEW = Constant.PLACE_TYPE_ADVIEW;
    public static final String PLACE_TYPE_INTERSTITIAL = Constant.PLACE_TYPE_INTERSTITIAL;
    public static final String PLACE_TYPE_REWARD_VIDEO = Constant.PLACE_TYPE_REWARD_VIDEO;
    public static final String PLACE_TYPE_SPLASH = Constant.PLACE_TYPE_SPLASH;
    public static final String PLACE_TYPE_COMPLEX = Constant.PLACE_TYPE_COMPLEX;

    private static AdSdk sAdSdk;

    private Context mContext;
    private Context mOriginContext;
    private Map<String, AdPlaceLoader> mAdLoaders = new HashMap<String, AdPlaceLoader>();
    private WeakReference<Activity> mActivity;
    // 激励视频自动加载回调
    private OnAdSdkListener mAutoRewardListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    // 激励视频自动加载间隔
    private long mRewardLoadInterval = 5000;
    // 激励视频场景名称
    private String mRewardPlaceName = null;

    private AtomicBoolean mInitialized = new AtomicBoolean(false);

    private AdSdk(Context context) {
        mOriginContext = context.getApplicationContext();
        mContext = VUIHelper.createAContext(mOriginContext);
    }

    public static AdSdk get(Context context) {
        if (sAdSdk == null) {
            create(context);
        }
        if (sAdSdk != null) {
            if (context instanceof Activity) {
                sAdSdk.setActivity((Activity) context);
            }
        }
        return sAdSdk;
    }

    private static void create(Context context) {
        synchronized (AdSdk.class) {
            if (sAdSdk == null) {
                sAdSdk = new AdSdk(context);
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
        return BuildConfig.SDK_VERSION_NAME;
    }

    /**
     * 初始化
     */
    public void init() {
        Log.iv(Log.TAG, "sdk version : " + getSdkVersion());
        DataManager.get(mContext).init();
        ActivityMonitor.get(mOriginContext).init();
        EventImpl.get().init(mContext);
        ReplaceManager.get(mContext).init();
        if (mInitialized != null) {
            mInitialized.set(true);
        }
    }

    private void checkInitialize() {
        if (mInitialized == null || !mInitialized.get()) {
            boolean debuggable = false;
            try {
                debuggable = 0 != (mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
            } catch (Exception e) {
            }
            if (debuggable) {
                throw new IllegalStateException("You must call AdSdk.get(context).init() first");
            } else {
                Log.iv(Log.TAG, "You must call AdSdk.get(context).init() first");
            }
        }
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

    public String getAttribution() {
        return Utils.getString(mContext, Constant.AF_STATUS);
    }

    public String getMediaSource() {
        return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
    }

    public String getString(String key) {
        checkInitialize();
        return DataManager.get(mContext).getString(key);
    }

    private AdPlaceLoader getAdLoader(String placeName) {
        return getAdLoader(placeName, false);
    }

    private AdPlaceLoader getAdLoader(String placeName, boolean forLoad) {
        checkInitialize();
        Log.iv(Log.TAG_SDK, "getAdLoader forLoad : " + forLoad);
        // 根据规则判断是否需要替换place name
        placeName = ReplaceManager.get(mContext).replacePlaceName(placeName, forLoad);
        // 优先处理场景被限制的情况
        String limitPlaceName = LimitAdsManager.get(mContext).addSuffixForPlaceNameIfNeed(placeName);

        String refPlaceName = null;
        if (TextUtils.equals(limitPlaceName, placeName)) {
            // 获取引用的PlaceName
            refPlaceName = getAdRefPlaceName(placeName);
        } else {
            refPlaceName = limitPlaceName;
        }

        Log.iv(Log.TAG_SDK, "place name : " + placeName + " , refPlaceName : " + refPlaceName);
        boolean useShareObject = false;
        // 如果共享loader对象
        if (!TextUtils.equals(placeName, refPlaceName) && isRefShare(refPlaceName)) {
            useShareObject = true;
        }
        if (useShareObject) {
            // 将共享对象赋值给新的场景
            if (mAdLoaders.containsKey(refPlaceName) && !mAdLoaders.containsKey(placeName)) {
                mAdLoaders.put(placeName, mAdLoaders.get(refPlaceName));
            }
        }
        AdPlaceLoader loader = mAdLoaders.get(placeName);
        if (!forLoad) {
            if (useShareObject && !TextUtils.equals(placeName, refPlaceName) && loader != null) {
                loader.setOriginPlaceName(placeName);
            }
            return loader;
        }
        // 首先获取远程针对广告位的配置是否存在
        AdPlace adPlace = DataManager.get(mContext).getRemoteAdPlace(refPlaceName);
        // 如果远程无配置，则读取本地或者远程整体广告位配置
        if (adPlace == null) {
            PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
            if (localConfig != null) {
                adPlace = localConfig.get(placeName);
            }
        }
        // loader为null，或者AdPlace内容有变化，则重新加载loader
        if (loader == null || loader.needReload(adPlace)) {
            loader = createAdPlaceLoader(refPlaceName, adPlace);
            if (loader != null) {
                if (!TextUtils.equals(placeName, refPlaceName)) {
                    loader.setOriginPlaceName(placeName);
                }
                mAdLoaders.put(placeName, loader);
                if (useShareObject && !mAdLoaders.containsKey(refPlaceName)) {
                    mAdLoaders.put(refPlaceName, loader);
                }
            }
        }
        return loader;
    }

    private boolean isRefShare(String placeName) {
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (localConfig != null) {
            AdPlace adPlace = localConfig.get(placeName);
            if (adPlace != null) {
                return adPlace.isRefShare();
            }
        }
        return false;
    }

    /**
     * 获取引用pid名字
     *
     * @param placeName
     * @return 引用pid名字
     */
    private String getAdRefPlaceName(String placeName) {
        String adrefPlaceName = placeName;
        // 获取通过代码设置的别名
        adrefPlaceName = getAdPlaceAlias(placeName);
        if (!TextUtils.isEmpty(adrefPlaceName)) {
            return adrefPlaceName;
        }
        Map<String, String> adRefs = DataManager.get(mContext).getRemoteAdRefs();
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (adRefs == null && localConfig != null) {
            adRefs = localConfig.getAdRefs();
        }
        if (adRefs != null && adRefs.containsKey(placeName)) {
            adrefPlaceName = adRefs.get(placeName);
        }
        if (!TextUtils.isEmpty(adrefPlaceName)) {
            return adrefPlaceName;
        }
        return placeName;
    }

    /**
     * 如果adPlace和adIds为空，则使用本地的adPlace和adIds
     *
     * @param placeName
     * @param adPlace
     * @return
     */
    private AdPlaceLoader createAdPlaceLoader(String placeName, AdPlace adPlace) {
        AdPlaceLoader loader = null;
        boolean useRemote = true;
        PlaceConfig localConfig = DataManager.get(mContext).getAdConfig();
        if (localConfig != null && adPlace == null) {
            adPlace = localConfig.get(placeName);
            useRemote = false;
        }
        Log.iv(Log.TAG, "placeName : " + placeName + " , adPlace : " + adPlace);
        if (adPlace != null) {
            loader = new AdPlaceLoader(mContext);
            loader.setAdPlaceConfig(adPlace);
            loader.init();
        }
        if (loader != null) {
            Log.iv(Log.TAG, "placeName [" + placeName + "] use remote ad place : " + useRemote);
        }
        return loader;
    }

    public String getLoadedSdk(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getLoadedSdk();
        }
        return null;
    }

    public boolean isInterstitialLoaded(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isInterstitialLoaded();
        }
        return false;
    }

    public void loadInterstitial(String placeName) {
        loadInterstitial(null, placeName);
    }

    public void loadInterstitial(String placeName, OnAdSdkListener l) {
        loadInterstitial(null, placeName, l);
    }

    public void loadInterstitial(Activity activity, String placeName) {
        loadInterstitial(activity, placeName, null);
    }

    public void loadInterstitial(Activity activity, String placeName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadInterstitial(activity);
        } else {
            if (l != null) {
                l.onLoadFailed(placeName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void showInterstitial(String placeName) {
        showInterstitial(placeName, null);
    }

    public void showInterstitial(String placeName, String sceneName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showInterstitial(sceneName);
        }
    }


    public boolean isRewardedVideoLoaded(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isRewardedVideoLoaded();
        }
        return false;
    }

    public void loadRewardedVideo(String placeName) {
        loadRewardedVideo(null, placeName);
    }

    public void loadRewardedVideo(String placeName, OnAdSdkListener l) {
        loadRewardedVideo(null, placeName, l);
    }

    public void loadRewardedVideo(Activity activity, String placeName) {
        loadRewardedVideo(activity, placeName, null);
    }

    public void loadRewardedVideo(Activity activity, String placeName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadRewardedVideo(activity);
        } else {
            if (l != null) {
                l.onLoadFailed(placeName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void showRewardedVideo(String placeName) {
        showRewardedVideo(placeName, null);
    }

    public void showRewardedVideo(String placeName, String sceneName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showRewardedVideo(sceneName);
        }
    }

    public boolean isAdViewLoaded(String placeName) {
        return isAdViewLoaded(placeName, null);
    }

    public boolean isAdViewLoaded(String placeName, String adType) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isAdViewLoaded(adType);
        }
        return false;
    }

    public void loadAdView(String placeName) {
        loadAdView(placeName, new AdParams.Builder().build(), null);
    }

    public void loadAdView(String placeName, OnAdSdkListener l) {
        loadAdView(placeName, new AdParams.Builder().build(), l);
    }

    public void loadAdView(String placeName, AdParams adParams) {
        loadAdView(placeName, adParams, null);
    }

    public void loadAdView(String placeName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            loader.loadAdView(adParams);
        } else {
            if (l != null) {
                l.onLoadFailed(placeName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void showAdView(String placeName, ViewGroup adContainer) {
        showAdView(placeName, null, null, adContainer);
    }

    public void showAdView(String placeName, AdParams adParams, ViewGroup adContainer) {
        showAdView(placeName, null, adParams, adContainer);
    }

    public void showAdView(String placeName, String adType, AdParams adParams, ViewGroup adContainer) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showAdView(adContainer, adType, adParams);
        }
    }

    ///////////////////////////////////////////////////////////////////////

    public boolean isSplashLoaded(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isSplashLoaded();
        }
        return false;
    }

    public void loadSplash(String placeName) {
        loadSplash(null, placeName);
    }

    public void loadSplash(String placeName, OnAdSdkListener l) {
        loadSplash(null, placeName, l);
    }

    public void loadSplash(Activity activity, String placeName) {
        loadSplash(activity, placeName, null);
    }

    public void loadSplash(Activity activity, String placeName, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            if (activity == null) {
                if (mActivity != null && mActivity.get() != null && !mActivity.get().isFinishing()) {
                    activity = mActivity.get();
                }
            }
            loader.loadSplash(activity);
        } else {
            if (l != null) {
                l.onLoadFailed(placeName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void showSplash(String placeName) {
        showSplash(placeName, null, null);
    }

    public void showSplash(String placeName, ViewGroup viewGroup) {
        showSplash(placeName, viewGroup, null);
    }

    public void showSplash(String placeName, ViewGroup viewGroup, String sceneName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showSplash(viewGroup, sceneName);
        }
    }
/////////////////////////////////////////////////////////////////////////////

    public boolean isComplexAdsLoaded(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isComplexAdsLoaded();
        }
        return false;
    }

    public void loadComplexAds(String placeName) {
        loadComplexAds(placeName, null, null);
    }

    public void loadComplexAds(String placeName, OnAdSdkListener l) {
        loadComplexAds(placeName, null, l);
    }

    public void loadComplexAds(String placeName, AdParams adParams, OnAdSdkListener l) {
        AdPlaceLoader loader = getAdLoader(placeName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(l, false);
            loader.loadComplexAds(adParams);
        } else {
            if (l != null) {
                l.onLoadFailed(placeName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void showComplexAds(String placeName) {
        showComplexAds(placeName, null);
    }

    public void showComplexAds(String placeName, String sceneName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.showComplexAds(sceneName);
        }
    }

    public boolean isComplexAdsLoaded() {
        try {
            List<String> placeList = DataManager.get(mContext).getPlaceList();
            if (placeList != null && !placeList.isEmpty()) {
                for (String name : placeList) {
                    AdPlaceLoader adPlaceLoader = getAdLoader(name);
                    if (adPlaceLoader != null && adPlaceLoader.isComplexAdsLoaded()) {
                        Log.iv(Log.TAG, "place name [" + name + "] is loaded");
                        return true;
                    }
                }
            } else {
                for (Map.Entry<String, AdPlaceLoader> entry : mAdLoaders.entrySet()) {
                    AdPlaceLoader adPlaceLoader = getAdLoader(entry.getKey());
                    if (adPlaceLoader != null && adPlaceLoader.isComplexAdsLoaded()) {
                        Log.iv(Log.TAG, "place name [" + entry.getKey() + "] is loaded");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return false;
    }

    public void showGlobalComplexAds() {
        showGlobalComplexAds(null);
    }

    public void showGlobalComplexAds(String sceneName) {
        try {
            List<String> placeList = DataManager.get(mContext).getPlaceList();
            List<AdPlaceLoader> list = new ArrayList<AdPlaceLoader>();
            if (placeList != null && !placeList.isEmpty()) {
                for (String name : placeList) {
                    AdPlaceLoader adPlaceLoader = getAdLoader(name);
                    if (adPlaceLoader != null && adPlaceLoader.isComplexAdsLoaded()) {
                        list.add(adPlaceLoader);
                    }
                }
            } else {
                for (Map.Entry<String, AdPlaceLoader> entry : mAdLoaders.entrySet()) {
                    String placeName = entry.getKey();
                    AdPlaceLoader adPlaceLoader = getAdLoader(placeName);
                    if (adPlaceLoader != null && adPlaceLoader.isComplexAdsLoaded()) {
                        list.add(adPlaceLoader);
                    }
                }
            }
            if (list != null && !list.isEmpty()) {
                if (placeList == null || placeList.isEmpty()) {
                    List<String> finalCpxOrderList = Constant.DEFAULT_COMPLEX_ORDER;
                    Collections.sort(list, new Comparator<AdPlaceLoader>() {
                        @Override
                        public int compare(AdPlaceLoader adPlaceLoader, AdPlaceLoader t1) {
                            try {
                                if (adPlaceLoader != null && t1 != null) {
                                    String type1 = adPlaceLoader.getLoadedType();
                                    String type2 = t1.getLoadedType();
                                    int index1 = finalCpxOrderList.indexOf(type1);
                                    int index2 = finalCpxOrderList.indexOf(type2);
                                    Integer integerType1 = Integer.valueOf(index1);
                                    Integer integerType2 = Integer.valueOf(index2);
                                    return integerType1.compareTo(integerType2);
                                }
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    });
                }
                for (AdPlaceLoader loader : list) {
                    if (loader != null && loader.isComplexAdsLoaded()) {
                        Log.iv(Log.TAG, "place name [" + loader.getPlaceName() + "] is called to show");
                        loader.showComplexAds(sceneName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    /**
     * 独立是设置监听器接口
     *
     * @param placeName
     * @param l
     */
    public void setOnAdSdkListener(String placeName, OnAdSdkListener l) {
        setOnAdSdkListener(placeName, l, false);
    }

    public void setOnAdSdkListener(String placeName, OnAdSdkListener l, boolean loaded) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.setOnAdSdkListener(l, loaded);
        }
    }

    public OnAdSdkListener getOnAdSdkListener(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getOnAdSdkListener();
        }
        return null;
    }

    public int getAdCount(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getAdCount();
        }
        return 0;
    }

    public int getLoadedAdCount(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getLoadedAdCount();
        }
        return 0;
    }

    public String getAdMode(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getAdMode();
        }
        return null;
    }

    public double getMaxRevenue(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.getMaxRevenue(null);
        }
        return 0f;
    }

    public boolean isLoading(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isLoading();
        }
        return false;
    }

    public boolean isAdPlaceError(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            return loader.isAdPlaceError();
        }
        return true;
    }

    public String getMaxPlaceName() {
        return getMaxPlaceName(null, null);
    }

    public String getMaxPlaceName(String adType) {
        return getMaxPlaceName(adType, null);
    }

    public String getMaxPlaceName(String adType, List<String> whiteList) {
        try {
            return getMaxPlaceNameInternal(adType, whiteList);
        } catch (Exception e) {
        }
        return null;
    }

    private String getMaxPlaceNameInternal(String adType, List<String> whiteList) {
        String maxPlaceName = null;
        double maxRevenue = -1f;
        if (mAdLoaders != null && !mAdLoaders.isEmpty()) {
            for (Map.Entry<String, AdPlaceLoader> entry : mAdLoaders.entrySet()) {
                String placeName = entry.getKey();
                AdPlaceLoader adPlaceLoader = getAdLoader(placeName);
                if (!TextUtils.isEmpty(placeName) && adPlaceLoader != null
                        && (whiteList == null || whiteList.isEmpty() || whiteList.contains(placeName))) {
                    double tmpRevenue = -1f;
                    if (TextUtils.equals(adType, Constant.TYPE_SPLASH)) {
                        if (adPlaceLoader.isSplashLoaded()) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    } else if (TextUtils.equals(adType, Constant.TYPE_INTERSTITIAL)) {
                        if (adPlaceLoader.isInterstitialLoaded()) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    } else if (TextUtils.equals(adType, Constant.TYPE_REWARD)) {
                        if (adPlaceLoader.isRewardedVideoLoaded()) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    } else if (TextUtils.equals(adType, Constant.TYPE_NATIVE)) {
                        if (adPlaceLoader.isAdViewLoaded(adType)) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    } else if (TextUtils.equals(adType, Constant.TYPE_BANNER)) {
                        if (adPlaceLoader.isAdViewLoaded(adType)) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    } else {
                        if (adPlaceLoader.isComplexAdsLoaded()) {
                            tmpRevenue = adPlaceLoader.getMaxRevenue(adType);
                        }
                    }
                    if (tmpRevenue > maxRevenue) {
                        maxRevenue = tmpRevenue;
                        maxPlaceName = placeName;
                    }
                }
            }
        }
        Log.iv(Log.TAG, "max place name : " + maxPlaceName + " , ad type : " + adType + " , revenue : " + maxRevenue);
        return maxPlaceName;
    }

    /**
     * 判断广告位是否可用
     *
     * @param placeName
     * @return
     */
    public boolean isPlaceValidate(String placeName) {
        return DataManager.get(mContext).isPlaceValidate(placeName);
    }

    public void resume(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.resume();
        }
    }

    public void pause(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            loader.pause();
        }
    }

    public void destroy(String placeName) {
        AdPlaceLoader loader = getAdLoader(placeName);
        if (loader != null) {
            // mAdLoaders.remove(placeName);
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

    /**
     * 动态设置广告场景的别名
     *
     * @param srcAdPlace
     * @param dstAdPlace
     */
    public void setAdPlaceAlias(String srcAdPlace, String dstAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace) && !TextUtils.isEmpty(dstAdPlace)) {
            Utils.putString(mContext, Constant.AD_SDK_PREFIX + srcAdPlace, dstAdPlace);
            removeObjectFromAdLoaders(srcAdPlace);
        }
    }

    /**
     * 清除广告位别名
     *
     * @param srcAdPlace
     */
    public void clearAdPlaceAlias(String srcAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace)) {
            Utils.clearPrefs(mContext, Constant.AD_SDK_PREFIX + srcAdPlace);
            removeObjectFromAdLoaders(srcAdPlace);
        }
    }

    private void removeObjectFromAdLoaders(String srcAdPlace) {
        try {
            mAdLoaders.remove(srcAdPlace);
        } catch (Exception | Error e) {
        }
    }

    /**
     * 读取广告场景的别名
     *
     * @param srcAdPlace
     * @return
     */
    private String getAdPlaceAlias(String srcAdPlace) {
        if (!TextUtils.isEmpty(srcAdPlace)) {
            return Utils.getString(mContext, Constant.AD_SDK_PREFIX + srcAdPlace, null);
        }
        return null;
    }

    private Runnable mRewardLoadRunnable = new Runnable() {
        @Override
        public void run() {
            if (!AdSdk.get(mContext).isRewardedVideoLoaded(mRewardPlaceName)
                    && ActivityMonitor.get(mContext).appOnTop()
                    && Utils.isScreenOn(mContext)) {
                loadRewardAuto();
            } else {
                setAutoRewardListener(mRewardPlaceName, mAutoRewardListener);
            }
            if (mAutoRewardListener != null) {
                mAutoRewardListener.onUpdate(mRewardPlaceName, null, Constant.TYPE_REWARD, null);
            }
            if (mHandler != null) {
                mHandler.postDelayed(this, mRewardLoadInterval);
            }
        }
    };

    public void setAutoRewardListener(String placeName, OnAdSdkListener l) {
        mAutoRewardListener = l;
        setOnAdSdkListener(placeName, l);
    }

    private void loadRewardAuto() {
        AdPlaceLoader loader = getAdLoader(mRewardPlaceName, true);
        if (loader != null) {
            loader.setOnAdSdkListener(mAutoRewardListener, false);
            loader.loadRewardedVideo(null);
        } else {
            if (mAutoRewardListener != null) {
                mAutoRewardListener.onLoadFailed(mRewardPlaceName, null, null, null, Constant.AD_ERROR_LOADER);
            }
        }
    }

    public void startAutoReward(String placeName, long interval, long firstDelay) {
        mRewardPlaceName = placeName;
        mRewardLoadInterval = interval;
        if (mHandler != null) {
            mHandler.removeCallbacks(mRewardLoadRunnable);
            mHandler.postDelayed(mRewardLoadRunnable, firstDelay);
        }
    }

    public void stopAutoReward() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRewardLoadRunnable);
        }
    }

    public void setOnAdFilterListener(OnAdFilterListener l) {
        AdLoadManager.get(mContext).setOnAdFilterListener(l);
    }

    public void setOnAdEventListener(OnAdEventListener l) {
        AdLoadManager.get(mContext).setOnAdEventListener(l);
    }

    public void showMediationDebugger() {
        AppLovinLoader.showApplovinMediationDebugger(mContext);
    }

    public long getCurrentTimeMillis() {
        return DataManager.get(mContext).getElapsedTimeMillis();
    }
}