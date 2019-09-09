package com.inner.adsdk.adloader.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.inner.adsdk.adloader.listener.IManagerListener;
import com.inner.adsdk.adloader.listener.ISdkLoader;
import com.inner.adsdk.adloader.listener.OnAdBaseListener;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.stat.IStat;
import com.inner.adsdk.stat.StatImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public abstract class AbstractSdkLoader implements ISdkLoader, Handler.Callback {

    // 广告的最大默认缓存时间
    protected static final long MAX_CACHED_TIME = 30 * 60 * 1000;
    // 加载未返回的超时消息
    protected static final int MSG_LOADING_TIMEOUT = 1000;
    // 加载未返回的超时时间5分钟
    protected static final int LOADING_TIMEOUT = 5 * 60 * 1000;

    protected static final int STATE_REQUEST = 1;
    protected static final int STATE_SUCCESS = 2;
    protected static final int STATE_FAILURE = 3;
    protected static final int STATE_TIMTOUT = 4;

    private static Map<Object, Long> mCachedTime = new ConcurrentHashMap<Object, Long>();
    protected AdPlace mAdPlace;
    protected Context mContext;
    protected IStat mStat;
    protected IManagerListener mManagerListener;
    protected Handler mHandler = null;

    private boolean mLoading = false;
    private long mRequestTime = 0;
    private int mBannerSize = Constant.NOSET;

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context, AdPlace adPlace) {
        mContext = context;
        mAdPlace = adPlace;
        mStat = StatImpl.get();
        mHandler = new Handler(this);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public abstract String getSdkName();

    public String getAdType() {
        if (mAdPlace != null) {
            return mAdPlace.getType();
        }
        return null;
    }

    @Override
    public AdPlace getAdPlace() {
        return mAdPlace;
    }

    protected boolean checkPidConfig() {
        if (mAdPlace == null) {
            Log.e(Log.TAG, "ad place is null");
            return false;
        }
        if (TextUtils.isEmpty(mAdPlace.getPid())) {
            Log.e(Log.TAG, "pid is empty");
            return false;
        }
        return true;
    }

    @Override
    public void loadInterstitial() {
        if (getAdListener() != null) {
            getAdListener().onAdFailed(Constant.AD_ERROR_UNSUPPORT);
        }
    }

    @Override
    public boolean showInterstitial() {
        return false;
    }

    @Override
    public void loadNative(Params params) {
        if (getAdListener() != null) {
            getAdListener().onAdFailed(Constant.AD_ERROR_UNSUPPORT);
        }
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
    }

    @Override
    public void loadBanner(int adSize) {
        if (getAdListener() != null) {
            getAdListener().onAdFailed(Constant.AD_ERROR_UNSUPPORT);
        }
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public void loadRewardedVideo() {
        if (getAdListener() != null) {
            getAdListener().onAdFailed(Constant.AD_ERROR_UNSUPPORT);
        }
    }

    @Override
    public boolean showRewardedVideo() {
        return false;
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
    public boolean isRewaredVideoLoaded() {
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
    public boolean isBannerType() {
        if (mAdPlace != null) {
            return TextUtils.equals(Constant.TYPE_BANNER, mAdPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isNativeType() {
        if (mAdPlace != null) {
            return TextUtils.equals(Constant.TYPE_NATIVE, mAdPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        if (mAdPlace != null) {
            return TextUtils.equals(Constant.TYPE_INTERSTITIAL, mAdPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoType() {
        if (mAdPlace != null) {
            return TextUtils.equals(Constant.TYPE_REWARD, mAdPlace.getType());
        }
        return false;
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

    protected synchronized void setLoading(boolean loading, int state) {
        mLoading = loading;
        if (mLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIMEOUT, getTimeout());
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + mAdPlace.getName() + " - send time out message : " + getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + mAdPlace.getName() + " - remove time out message");
            }
        }
    }

    protected void putCachedAdTime(Object object) {
        try {
            mCachedTime.put(object, SystemClock.elapsedRealtime());
        } catch (Exception e) {
        }
    }

    protected boolean isCachedAdExpired(Object object) {
        try {
            long cachedTime = mCachedTime.get(object);
            if (cachedTime <= 0) {
                return true;
            }
            return SystemClock.elapsedRealtime() - cachedTime > getMaxCachedTime();
        } catch (Exception e) {
        }
        return false;
    }

    protected void clearCachedAdTime(Object object) {
        try {
            mCachedTime.remove(object);
        } catch (Exception | Error e) {
        }
    }

    /**
     * 获取广告最大缓存时间
     *
     * @return
     */
    private long getMaxCachedTime() {
        long cacheTime = 0;
        if (mAdPlace != null) {
            cacheTime = mAdPlace.getCacheTime();
        }
        if (cacheTime <= 0) {
            cacheTime = MAX_CACHED_TIME;
        }
        return cacheTime;
    }

    /**
     * 获取广告加载超时时间
     *
     * @return
     */
    private long getTimeout() {
        long timeOut = 0;
        if (mAdPlace != null) {
            timeOut = mAdPlace.getLoadTime();
        }
        if (timeOut <= 0) {
            timeOut = LOADING_TIMEOUT;
        }
        return timeOut;
    }

    protected void onLoadTimeout() {
        Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + mAdPlace.getName() + " - load time out");
        setLoading(false, STATE_TIMTOUT);
        if (TextUtils.equals(getAdType(), Constant.TYPE_INTERSTITIAL)
                || TextUtils.equals(getAdType(), Constant.TYPE_REWARD)) {
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_TIMEOUT);
            }
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_BANNER)
                || TextUtils.equals(getAdType(), Constant.TYPE_NATIVE)) {
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_TIMEOUT);
            }
        }
        if (mStat != null) {
            mStat.reportAdError(mContext, "AD_ERROR_TIMEOUT", getSdkName(), getAdType(), null);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == MSG_LOADING_TIMEOUT) {
            onLoadTimeout();
            return true;
        }
        return false;
    }

    protected String getPid() {
        if (mAdPlace != null) {
            return mAdPlace.getPid();
        }
        return null;
    }

    /**
     * 对于已经加载成功的banner，延迟一段时间再进行通知
     *
     * @param runnable
     */
    private void delayNotifyAdLoaded(Runnable runnable, boolean cached) {
        long delay = getDelayNotifyLoadTime(cached);
        Log.v(Log.TAG, "delay notify loaded time : " + delay);
        if (delay <= 0) {
            if (runnable != null) {
                runnable.run();
            }
        } else {
            if (mHandler != null) {
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, delay);
            }
        }
    }

    private long getDelayNotifyLoadTime(boolean cached) {
        long delay = 0;
        return delay;
    }

    /**
     * 通知ad加载成功
     * 增加延迟通知功能，便于优先级高但是加载时间长的ad能够优先展示
     *
     * @param cached
     */
    protected void notifyAdLoaded(boolean cached) {
        delayNotifyAdLoaded(mNotifyLoadRunnable, cached);
    }

    private Runnable mNotifyLoadRunnable = new Runnable() {
        @Override
        public void run() {
            notifyAdLoadedByListener();
        }
    };

    private void notifyAdLoadedByListener() {
        if (getAdListener() != null) {
            getAdListener().onAdLoaded(this);
        }
    }

    protected String getMetaData(String key) {
        ApplicationInfo info = null;
        try {
            info = mContext.getPackageManager()
                    .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info != null ? info.metaData.getString(key) : null;
    }

    protected String getAppId() {
        if (mAdPlace != null) {
            return mAdPlace.getAid();
        }
        return null;
    }

    protected String getExtId() {
        if (mAdPlace != null) {
            return mAdPlace.getEid();
        }
        return null;
    }

    protected void setBannerSize(int size) {
        mBannerSize = size;
    }

    @Override
    public int getBannerSize() {
        return mBannerSize;
    }

    protected double getEcpm() {
        return 0f;
    }
}