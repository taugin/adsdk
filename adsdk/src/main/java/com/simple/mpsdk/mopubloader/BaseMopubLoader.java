package com.simple.mpsdk.mopubloader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.simple.mpsdk.config.MpPlace;
import com.simple.mpsdk.internallistener.IManagerListener;
import com.simple.mpsdk.internallistener.ISdkLoader;
import com.simple.mpsdk.internallistener.OnMpBaseListener;
import com.simple.mpsdk.constant.Constant;
import com.simple.mpsdk.framework.Params;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.stat.IReport;
import com.simple.mpsdk.stat.ReportImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/2/9.
 */

public abstract class BaseMopubLoader implements ISdkLoader, Handler.Callback {

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
    protected MpPlace mMpPlace;
    protected Context mContext;
    protected IReport mStat;
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
    public void init(Context context, MpPlace mpPlace) {
        mContext = context;
        mMpPlace = mpPlace;
        mStat = ReportImpl.get();
        mHandler = new Handler(this);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public abstract String getSdkName();

    public String getAdType() {
        if (mMpPlace != null) {
            return mMpPlace.getType();
        }
        return null;
    }

    @Override
    public MpPlace getAdPlace() {
        return mMpPlace;
    }

    protected boolean checkPlaceConfig() {
        if (mMpPlace == null) {
            LogHelper.e(LogHelper.TAG, "mp place is null");
            return false;
        }
        if (TextUtils.isEmpty(mMpPlace.getPid())) {
            LogHelper.e(LogHelper.TAG, "placement id is empty");
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
        if (mMpPlace != null) {
            return TextUtils.equals(Constant.TYPE_BANNER, mMpPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isNativeType() {
        if (mMpPlace != null) {
            return TextUtils.equals(Constant.TYPE_NATIVE, mMpPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        if (mMpPlace != null) {
            return TextUtils.equals(Constant.TYPE_INTERSTITIAL, mMpPlace.getType());
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoType() {
        if (mMpPlace != null) {
            return TextUtils.equals(Constant.TYPE_REWARD, mMpPlace.getType());
        }
        return false;
    }

    protected OnMpBaseListener getAdListener() {
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
                LogHelper.v(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + mMpPlace.getName() + " - set timeout : " + getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                LogHelper.v(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + mMpPlace.getName() + " - unset timeout");
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
            Long obj = mCachedTime.get(object);
            long cachedTime = 0;
            if (obj != null) {
                cachedTime = obj.longValue();
            }
            if (cachedTime <= 0) {
                return true;
            }
            return SystemClock.elapsedRealtime() - cachedTime > getMaxCachedTime();
        } catch (Exception e) {
            LogHelper.e(LogHelper.TAG, "error : " + e);
        }
        return true;
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
        if (mMpPlace != null) {
            cacheTime = mMpPlace.getCacheTime();
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
        if (mMpPlace != null) {
            timeOut = mMpPlace.getLoadTime();
        }
        if (timeOut <= 0) {
            timeOut = LOADING_TIMEOUT;
        }
        return timeOut;
    }

    protected void onLoadTimeout() {
        LogHelper.v(LogHelper.TAG, getSdkName() + " - " + getAdType() + " - " + mMpPlace.getName() + " - load time out");
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
        if (mMpPlace != null) {
            return mMpPlace.getPid();
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
        LogHelper.v(LogHelper.TAG, "delay notify loaded time : " + delay);
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
        if (mMpPlace != null) {
            return mMpPlace.getAid();
        }
        return null;
    }

    protected String getExtId() {
        if (mMpPlace != null) {
            return mMpPlace.getEid();
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