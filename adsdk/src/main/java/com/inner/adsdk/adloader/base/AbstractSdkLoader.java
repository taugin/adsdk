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
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.config.PidConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.framework.Params;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.stat.IStat;
import com.inner.adsdk.stat.StatImpl;
import com.inner.adsdk.utils.Utils;

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
    protected PidConfig mPidConfig;
    protected Context mContext;
    protected IStat mStat;
    protected IManagerListener mManagerListener;
    protected String mAdId;
    protected Handler mHandler = null;

    private boolean mLoading = false;
    private boolean mLoadedFlag = false;
    private long mRequestTime = 0;
    private int mBannerSize = Constant.NOSET;

    @Override
    public void setListenerManager(IManagerListener l) {
        mManagerListener = l;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        mStat = StatImpl.get();
        mHandler = new Handler(this);
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
    public abstract String getSdkName();

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
    public String getAdPlaceName() {
        String adPlaceName = null;
        if (mManagerListener != null) {
            adPlaceName = mManagerListener.getOriginPidName();
        }
        if (!TextUtils.isEmpty(adPlaceName)) {
            return adPlaceName;
        }
        if (mPidConfig != null) {
            return mPidConfig.getAdPlaceName();
        }
        return null;
    }

    @Override
    public boolean isBannerType() {
        if (mPidConfig != null) {
            return mPidConfig.isBannerType();
        }
        return false;
    }

    @Override
    public boolean isNativeType() {
        if (mPidConfig != null) {
            return mPidConfig.isNativeType();
        }
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        if (mPidConfig != null) {
            return mPidConfig.isInterstitialType();
        }
        return false;
    }

    @Override
    public boolean isRewardedVideoType() {
        if (mPidConfig != null) {
            return mPidConfig.isRewardedVideoType();
        }
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

    protected synchronized void setLoading(boolean loading, int state) {
        mLoading = loading;
        reportLoadAdTime(state);
        if (mLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIMEOUT, getTimeout());
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - send time out message : " + getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOADING_TIMEOUT);
                Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - remove time out message");
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

    protected boolean isDestroyAfterClick() {
        try {
            return mPidConfig.isDestroyAfterClick();
        } catch(Exception e) {
        }
        return false;
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
     *
     * @return
     */
    protected boolean matchNoFillTime() {
        return System.currentTimeMillis() - getLastNoFillTime() >= mPidConfig.getNoFill();
    }

    /**
     * 更新最后填充
     */
    protected void updateLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            Utils.putLong(mContext, pref, System.currentTimeMillis());
            Log.d(Log.TAG, pref + " : " + System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    /**
     * 获取最后填充时间
     *
     * @return
     */
    protected long getLastNoFillTime() {
        try {
            String pref = getSdkName() + "_" + mPidConfig.getPid();
            return Utils.getLong(mContext, pref, 0);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 获取广告最大缓存时间
     *
     * @return
     */
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

    /**
     * 获取广告加载超时时间
     *
     * @return
     */
    private long getTimeout() {
        long timeOut = 0;
        if (mPidConfig != null) {
            timeOut = mPidConfig.getTimeOut();
        }
        if (timeOut <= 0) {
            timeOut = LOADING_TIMEOUT;
        }
        return timeOut;
    }

    /**
     * 阻塞正在加载的loader
     *
     * @return
     */
    protected boolean blockLoading() {
        AdSwitch adSwitch = DataManager.get(mContext).getAdSwitch();
        if (adSwitch != null) {
            return adSwitch.isBlockLoading();
        }
        return true;
    }

    protected void onLoadTimeout() {
        Log.v(Log.TAG, getSdkName() + " - " + getAdType() + " - " + getAdPlaceName() + " - load time out");
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
        if (mPidConfig != null) {
            return mPidConfig.getPid();
        }
        return null;
    }

    private void reportLoadAdTime(int state) {
        if (state == STATE_REQUEST) {
            mRequestTime = SystemClock.elapsedRealtime();
        } else if (state == STATE_SUCCESS) {
            if (mRequestTime > 0) {
                try {
                    int time = Math.round((SystemClock.elapsedRealtime() - mRequestTime) / (float) 100);
                    mStat.reportAdLoadSuccessTime(mContext, getSdkName(), getAdType(), time);
                } catch (Exception e) {
                }
                mRequestTime = 0;
            }
        } else {
            if (mRequestTime > 0) {
                try {
                    String error = "STATE_FAILURE";
                    if (state == STATE_TIMTOUT) {
                        error = "STATE_TIMTOUT";
                    }
                    int time = Math.round((SystemClock.elapsedRealtime() - mRequestTime) / (float) 100);
                    mStat.reportAdLoadFailureTime(mContext, getSdkName(), getAdType(), error, time);
                } catch (Exception e) {
                }
                mRequestTime = 0;
            }
        }
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
        if (mPidConfig != null) {
            delay = mPidConfig.getDelayLoadTime();
        }

        // 缓存的banner最小延迟为500ms
        if (cached && TextUtils.equals(getAdType(), Constant.TYPE_BANNER)) {
            if (delay <= 0) {
                delay = 500;
            }
        }
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
            setLoadedFlag();
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
        if (mPidConfig != null) {
            return mPidConfig.getAppId();
        }
        return null;
    }

    protected String getExtId() {
        if (mPidConfig != null) {
            return mPidConfig.getExtId();
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

    @Override
    public int getEcpm() {
        return getPidConfig().getEcpm();
    }

    @Override
    public String toString() {
        return  "pn = " + getAdPlaceName() + " , " +
                "tp = " + getAdType() + " , " +
                "sr = " + getSdkName() + " , " +
                "ba = " + Constant.Banner.valueOf(getBannerSize()) + " , " +
                "ec = " + getEcpm();
    }

}