package com.inner.adaggs.framework;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.inner.adaggs.AdExtra;
import com.inner.adaggs.adloader.adfb.FBLoader;
import com.inner.adaggs.adloader.admob.AdmobLoader;
import com.inner.adaggs.adloader.listener.IAdLoader;
import com.inner.adaggs.adloader.listener.SimpleAdListener;
import com.inner.adaggs.adloader.listener.SimpleInterstitialListener;
import com.inner.adaggs.config.AdPlaceConfig;
import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.listener.OnAdAggsListener;
import com.inner.adaggs.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdLoader {
    private List<IAdLoader> mAdLoaders = new ArrayList<IAdLoader>();
    private AdPlaceConfig mAdPlaceConfig;
    private Context mContext;
    private OnAdAggsListener mOnAdAggsListener;
    private Map<String, Object> mAdExtra;

    public AdLoader(Context context) {
        mContext = context;
    }

    public void init() {
        generateLoaders();
    }

    public void setAdPlaceConfig(AdPlaceConfig config) {
        mAdPlaceConfig = config;
    }

    private void generateLoaders() {
        if (mAdPlaceConfig != null) {
            List<PidConfig> pidList = mAdPlaceConfig.getPidsList();
            if (pidList != null && !pidList.isEmpty()) {
                IAdLoader loader = null;
                for (PidConfig config : pidList) {
                    if (config != null) {
                        if (config.isAdmob()) {
                            loader = new AdmobLoader();
                            loader.setContext(mContext);
                            loader.setPidConfig(config);
                            mAdLoaders.remove(loader);
                            mAdLoaders.add(loader);
                        } else if (config.isFB()) {
                            loader = new FBLoader();
                            loader.setContext(mContext);
                            loader.setPidConfig(config);
                            mAdLoaders.remove(loader);
                            mAdLoaders.add(loader);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据SDK名字获取banner大小
     *
     * @param loader
     * @return
     */
    private int getBannerSize(IAdLoader loader) {
        try {
            return (int) mAdExtra.get(loader.getSdkName() + AdExtra.BANNER_SIZE_SUFFIX);
        } catch (Exception e) {
        }
        return Constant.NOSET;
    }

    /**
     * 根据SDK获取rootview
     *
     * @param loader
     * @return
     */
    private View getRootView(IAdLoader loader) {
        try {
            return (View) mAdExtra.get(loader.getSdkName() + AdExtra.ROOT_VIEW_SUFFIX);
        } catch (Exception e) {
        }
        return null;
    }

    private int getTemplateId(IAdLoader loader) {
        try {
            return (int) mAdExtra.get(loader.getSdkName() + AdExtra.TEMPLATE_SUFFIX);
        } catch (Exception e) {
        }
        return Constant.NOSET;
    }

    /**
     * 设置外部监听器
     *
     * @param l
     */
    public void setOnAdAggsListener(OnAdAggsListener l) {
        mOnAdAggsListener = l;
    }

    public boolean isInterstitialLoaded() {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null && loader.isInterstitialLoaded()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 加载插屏
     */
    public void loadInterstitial() {
        if (mAdPlaceConfig == null) {
            return;
        }
        if (mAdPlaceConfig.isConcurrent()) {
            loadInterstitialConcurrent();
        } else if (mAdPlaceConfig.isSequence()) {
            loadInterstitialSequence();
        } else if (mAdPlaceConfig.isRandom()) {
            loadInterstitialRandom();
        } else {
            loadInterstitialConcurrent();
        }
    }

    private void loadInterstitialConcurrent() {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null) {
                loader.setOnInterstitialListener(new SimpleInterstitialListener(loader.getSdkName(), mOnAdAggsListener));
                loader.loadInterstitial();
            }
        }
    }

    private void loadInterstitialSequence() {
        // 使用迭代器处理
        final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
        loadInterstitialSequenceInternal(iterator);
    }

    private void loadInterstitialRandom() {
        int pos = new Random().nextInt(mAdLoaders.size());
        IAdLoader loader = mAdLoaders.get(pos);
        loader.setOnInterstitialListener(new SimpleInterstitialListener(loader.getSdkName(), mOnAdAggsListener));
        loader.loadInterstitial();
    }

    private void loadInterstitialSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        loader.setOnInterstitialListener(new SimpleInterstitialListener(loader.getSdkName(), mOnAdAggsListener) {
            @Override
            public void onInterstitialError() {
                if (iterator.hasNext()) {
                    Log.e(Log.TAG, "load next interstitial");
                    loadInterstitialSequenceInternal(iterator);
                } else {
                    super.onInterstitialError();
                }
            }
        });
        loader.loadInterstitial();
    }

    /**
     * 展示插屏
     */
    public void showInterstitial() {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null) {
                if (loader.isInterstitialLoaded() && loader.showInterstitial()) {
                    return;
                } else {
                    loader.setOnInterstitialListener(null);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////

    public boolean isAdViewLoaded() {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null) {
                return loader.isBannerLoaded() || loader.isNativeLoaded();
            }
        }
        return false;
    }

    public void loadAdView(Map<String, Object> extra) {
        if (mAdPlaceConfig == null) {
            return;
        }
        mAdExtra = extra;
        if (mAdPlaceConfig.isConcurrent()) {
            loadAdViewConcurrent();
        } else if (mAdPlaceConfig.isSequence()) {
            loadAdViewSequence();
        } else if (mAdPlaceConfig.isRandom()) {
            loadAdViewRandom();
        } else {
            loadAdViewConcurrent();
        }
    }

    private void loadAdViewConcurrent() {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null) {
                loader.setOnAdListener(new SimpleAdListener(loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener));
                if (loader.isBannerType()) {
                    loader.loadBanner(getBannerSize(loader));
                } else if (loader.isNativeType()) {
                    loader.loadNative(getRootView(loader), getTemplateId(loader));
                }
            }
        }
    }

    private void loadAdViewSequence() {
        final Iterator<IAdLoader> iterator = mAdLoaders.iterator();
        loadAdViewSequenceInternal(iterator);
    }

    private void loadAdViewRandom() {
        int pos = new Random().nextInt(mAdLoaders.size());
        IAdLoader loader = mAdLoaders.get(pos);
        if (loader != null) {
            loader.setOnAdListener(new SimpleAdListener(loader.getSdkName(),  loader.getAdType(),
                    mOnAdAggsListener));
            if (loader.isBannerType()) {
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.loadNative(getRootView(loader), getTemplateId(loader));
            }
        }
    }

    private void loadAdViewSequenceInternal(final Iterator<IAdLoader> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        IAdLoader loader = iterator.next();
        if (loader != null) {
            if (loader.isBannerType()) {
                loader.setOnAdListener(new SimpleAdListener(loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener) {
                    @Override
                    public void onAdFailed() {
                        Log.e(Log.TAG, "load next banner");
                        loadAdViewSequenceInternal(iterator);
                    }
                });
                loader.loadBanner(getBannerSize(loader));
            } else if (loader.isNativeType()) {
                loader.setOnAdListener(new SimpleAdListener(loader.getSdkName(), loader.getAdType(),
                        mOnAdAggsListener) {
                    @Override
                    public void onAdFailed() {
                        if (iterator.hasNext()) {
                            Log.e(Log.TAG, "load next native");
                            loadAdViewSequenceInternal(iterator);
                        } else {
                            super.onAdFailed();
                        }
                    }
                });
                loader.loadNative(getRootView(loader), getTemplateId(loader));
            }
        }
    }

    public void showAdView(ViewGroup adContainer) {
        for (IAdLoader loader : mAdLoaders) {
            if (loader != null) {
                if (loader.isBannerLoaded()) {
                    loader.showBanner(adContainer);
                    return;
                } else if (loader.isNativeLoaded()) {
                    loader.showNative(adContainer);
                    return;
                } else {
                    loader.setOnAdListener(null);
                }
            }
        }
    }
}
