package com.inner.adsdk.adloader.base;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.inner.adsdk.adloader.listener.IAdLoader;
import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.IStat;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AbstractAdLoader implements IAdLoader {

    protected static final long MAX_CACHED_TIME = 15 * 60 * 1000;
    private   static Map<Object, Long> mCachedTime = new ConcurrentHashMap<Object, Long>();
    protected PidConfig mPidConfig;
    protected Context mContext;
    protected IStat mStat;
    protected IManagerListener mManagerListener;
    protected String mAdId;
    private   boolean mLoading = false;
    private   boolean mLoadedFlag = false;

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        mStat = StatImpl.get();
    }

    @Override
    public void setAdId(String adId) {
        mAdId = adId;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public String getSdkName() {
        return null;
    }

    public String getAdType() {
        if (mPidConfig != null) {
            return mPidConfig.getAdType();
        }
        return null;
    }

    @Override
    public void setPidConfig(PidConfig config) {
        mPidConfig = config;
    }

    @Override
    public PidConfig getPidConfig() {
        return mPidConfig;
    }

    @Override
    public void loadInterstitial() {
    }

    @Override
    public boolean showInterstitial() {
        return false;
    }

    @Override
    public void loadNative(Params params) {
    }

    @Override
    public void showNative(ViewGroup viewGroup) {

    }

    @Override
    public void loadBanner(int adSize) {
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public boolean isInterstitialLoaded() {
        return false;
    }

    @Override
    public boolean isBannerLoaded() {
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        return false;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getAdPlaceName() {
        if (mPidConfig != null) {
            return mPidConfig.getAdPlaceName();
        }
        return null;
    }

    @Override
    public boolean isBannerType() {
        return false;
    }

    @Override
    public boolean isNativeType() {
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        return false;
    }

    @Override
    public void setLoadedFlag() {
        mLoadedFlag = true;
        if (mManagerListener != null) {
            mManagerListener.setLoader(this);
        }
    }

    @Override
    public boolean hasLoadedFlag() {
        return mLoadedFlag;
    }

    @Override
    public boolean useAndClearFlag() {
        boolean flag = mLoadedFlag;
        mLoadedFlag = false;
        return flag;
    }

    protected boolean checkPidConfig() {
        if (mPidConfig == null) {
            Log.e(Log.TAG, "pidconfig is null");
            return false;
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.e(Log.TAG, "sdk not equals");
            return false;
        }
        if (TextUtils.isEmpty(mPidConfig.getPid())) {
            Log.e(Log.TAG, "pid is empty");
            return false;
        }
        return true;
    }

    protected OnAdBaseListener getAdListener() {
        if (mManagerListener != null) {
            return mManagerListener.getAdBaseListener(this);
        }
        return null;
    }

    protected synchronized boolean isLoading() {
        return mLoading;
    }

    protected synchronized void setLoading(boolean loading) {
        mLoading = loading;
    }

    protected void putCachedAdTime(Object object) {
        mCachedTime.put(object, SystemClock.elapsedRealtime());
    }

    protected boolean isCachedAdExpired(Object object) {
        try {
            long cachedTime = mCachedTime.get(object);
            if (cachedTime <= 0) {
                return true;
            }
            return SystemClock.elapsedRealtime() - cachedTime > getMaxCachedTime();
        } catch(Exception e) {
        }
        return false;
    }

    protected void clearCachedAdTime(Object object) {
        mCachedTime.remove(object);
    }

    @Override
    public boolean allowUseLoader() {
        if (mPidConfig != null) {
            return !mPidConfig.isDisable();
        }
        return true;
    }


    /**
     * 防止多次加载NoFill的广告导致惩罚时间
     * @return
     */
    protected boolean matchNoFillTime() {
        return System.currentTimeMillis() - getLastNoFillTime() >= mPidConfig.getNoFill();
    }

    protected void updateLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            Utils.putLong(mContext, pref, System.currentTimeMillis());
            Log.d(Log.TAG, pref + " : " + System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    protected long getLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            return Utils.getLong(mContext, pref, 0);
        } catch (Exception e) {
        }
        return 0;
    }

    private long getMaxCachedTime() {
        long cacheTime = 0;
        if (mPidConfig != null) {
            cacheTime = mPidConfig.getCacheTime();
        }
        if (cacheTime <= 0) {
            cacheTime = MAX_CACHED_TIME;
        }
        return cacheTime;
    }
}