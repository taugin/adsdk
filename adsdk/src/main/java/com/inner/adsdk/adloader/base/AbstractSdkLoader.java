package com.inner.adsdk.adloader.base;

import android.content.Context;
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

public class AbstractSdkLoader implements ISdkLoader, Handler.Callback {

    // 广告的最大默认缓存时间
    protected static final long MAX_CACHED_TIME = 15 * 60 * 1000;
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
    private boolean mLoading = false;
    private boolean mLoadedFlag = false;
    private Handler mHandler = null;
    private long mRequestTime = 0;

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
        if (TextUtils.equals(getAdType(), Constant.TYPE_INTERSTITIAL)) {
            if (getAdListener() != null) {
                getAdListener().onInterstitialError(Constant.AD_ERROR_TIMEOUT);
            }
        } else if (TextUtils.equals(getAdType(), Constant.TYPE_BANNER)
                || TextUtils.equals(getAdType(), Constant.TYPE_NATIVE)) {
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_TIMEOUT);
            }
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

    private void reportLoadAdTime(int state) {
        if (state == STATE_REQUEST) {
            mRequestTime = System.currentTimeMillis();
        } else if (state == STATE_SUCCESS) {
            if (mRequestTime > 0) {
                try {
                    int time = Math.round((System.currentTimeMillis() - mRequestTime) / (float)100);
                    mStat.reportAdLoadSuccessTime(mContext, getSdkName(), getAdType(), time);
                } catch(Exception e) {
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
                    int time = Long.valueOf(System.currentTimeMillis() - mRequestTime).intValue();
                    mStat.reportAdLoadFailureTime(mContext, getSdkName(), getAdType(), error, time);
                } catch(Exception e) {
                }
                mRequestTime = 0;
            }
        }
    }
}