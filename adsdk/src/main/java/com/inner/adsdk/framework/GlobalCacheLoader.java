package com.inner.adsdk.framework;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.inner.adsdk.AdReward;
import com.inner.adsdk.AdSdk;
import com.inner.adsdk.BuildConfig;
import com.inner.adsdk.config.AdConfig;
import com.inner.adsdk.config.AdPlace;
import com.inner.adsdk.listener.OnAdRewardListener;
import com.inner.adsdk.listener.SimpleAdSdkListener;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by taugin on 2018/5/1.
 */

public class GlobalCacheLoader implements Handler.Callback {
    private static GlobalCacheLoader sGlobalCacheLoader;

    public static GlobalCacheLoader get(Context context) {
        synchronized (GlobalCacheLoader.class) {
            if (sGlobalCacheLoader == null) {
                createInstance(context);
            }
        }
        if (sGlobalCacheLoader != null) {
            sGlobalCacheLoader.setActivity(context);
        }
        return sGlobalCacheLoader;
    }

    private static void createInstance(Context context) {
        synchronized (GlobalCacheLoader.class) {
            if (sGlobalCacheLoader == null) {
                sGlobalCacheLoader = new GlobalCacheLoader(context);
            }
        }
    }

    private static final int MSG_LOAD_ADS = 10001;
    private static final int DELAY_LOAD_TIME_FIRST = 1 * 1000;
    private static final int DELAY_LOAD_TIME = 10 * 1000;
    private static final int TIMEOUT = 40 * 1000;
    private List<String> mAllAdPlaces = new ArrayList<String>();
    private Set<String> mLoadingAdPlaces = new HashSet<String>();
    private List<RewardItem> mLoadedAdPlaces = new ArrayList<RewardItem>();
    private Handler mHandler;
    private Context mContext;
    private Stack<OnAdRewardListener> mListeners;
    private int mCoreAdCount = 2;
    private Map<String, AdSdkCallback> mListenerMap = new HashMap<String, AdSdkCallback>();
    private WeakReference<Activity> mActivity = null;

    private GlobalCacheLoader(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(this);
        mListeners = new Stack<OnAdRewardListener>();
    }

    public void init() {
        fetchGlobalCacheAdPlace();
        sendMessageDelayInternal(true);
    }

    private void fetchGlobalCacheAdPlace() {
        AdConfig adConfig = DataManager.get(mContext).getAdConfig();
        if (adConfig != null && adConfig.getAdPlaceList() != null && !adConfig.getAdPlaceList().isEmpty()) {
            for (AdPlace adPlace : adConfig.getAdPlaceList()) {
                if (adPlace != null && adPlace.isGlobalCache()) {
                    mAllAdPlaces.add(adPlace.getName());
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == MSG_LOAD_ADS) {
            startRequestRewardVideo();
            sendMessageDelayInternal(false);
            return true;
        }
        return false;
    }

    private void setActivity(Context activity) {
        if (activity instanceof Activity) {
            mActivity = new WeakReference<Activity>((Activity) activity);
        } else {
            mActivity = null;
        }
    }

    private void sendMessageDelayInternal(boolean first) {
        if (mAllAdPlaces != null && !mAllAdPlaces.isEmpty()) {
            if (mHandler != null) {
                if (!mHandler.hasMessages(MSG_LOAD_ADS)) {
                    mHandler.sendEmptyMessageDelayed(MSG_LOAD_ADS, first ? DELAY_LOAD_TIME_FIRST : DELAY_LOAD_TIME);
                }
            }
        }
    }

    private void cancelSendMessageIfNeed() {
        if (mListeners == null && mListeners.isEmpty()) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOAD_ADS);
            }
        }
    }

    private void startRequestRewardVideo() {
        if (Utils.isScreenOn(mContext)
                && !Utils.isScreenLocked(mContext)
                && ActivityMonitor.get(mContext).appOnTop()
                && isNeedLoadReward()) {
            synchronized (mAllAdPlaces) {
                if (mAllAdPlaces != null && !mAllAdPlaces.isEmpty()) {
                    for (String adPlace : mAllAdPlaces) {
                        if (!isContainLoadedAdPlace(adPlace) && !isContainLoadingAdPlace(adPlace)) {
                            if (!AdSdk.get(mContext).isInterstitialLoaded(adPlace)) {
                                addLoadingAdPlace(adPlace);
                                if (BuildConfig.DEBUG) {
                                    Log.v(Log.TAG, "start load reward");
                                }
                                if (mActivity != null && mActivity.get() != null) {
                                    AdSdk.get(mActivity.get()).loadInterstitial(adPlace, getCallback(adPlace));
                                } else {
                                    AdSdk.get(mContext).loadInterstitial(adPlace, getCallback(adPlace));
                                }
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.v(Log.TAG, "add to adplaces from loadAds");
                                }
                                add(adPlace);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 重复使用adsdk callback
     *
     * @param adPlace
     * @return
     */
    private AdSdkCallback getCallback(String adPlace) {
        AdSdkCallback callback = null;
        if (TextUtils.isEmpty(adPlace)) {
            return callback;
        }
        if (mListenerMap == null) {
            mListenerMap = new HashMap<String, AdSdkCallback>();
        }
        if (mListenerMap.containsKey(adPlace)) {
            callback = mListenerMap.get(adPlace);
        }
        if (callback == null) {
            callback = new AdSdkCallback();
            mListenerMap.put(adPlace, callback);
        }
        callback.init();
        return callback;
    }

    /**
     * 是否处在已加载列表中
     *
     * @param pidName
     * @return
     */
    private boolean isContainLoadedAdPlace(String pidName) {
        synchronized (mLoadedAdPlaces) {
            if (mLoadedAdPlaces != null && !mLoadedAdPlaces.isEmpty()) {
                for (RewardItem item : mLoadedAdPlaces) {
                    if (item != null && TextUtils.equals(item.pidName, pidName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 加入正在加载队列
     *
     * @param adPlace
     */
    private void addLoadingAdPlace(String adPlace) {
        synchronized (mLoadingAdPlaces) {
            mLoadingAdPlaces.add(adPlace);
        }
    }

    /**
     * 判断是否正在加载
     *
     * @param adPlace
     * @return
     */
    private boolean isContainLoadingAdPlace(String adPlace) {
        synchronized (mLoadingAdPlaces) {
            return mLoadingAdPlaces.contains(adPlace);
        }
    }

    /**
     * 移除正在加载队列
     *
     * @param adPlace
     */
    private void removeLoadingAdplace(String adPlace) {
        synchronized (mLoadingAdPlaces) {
            if (mLoadingAdPlaces.contains(adPlace)) {
                mLoadingAdPlaces.remove(adPlace);
            }
        }
    }

    private boolean isNeedLoadReward() {
        return getLoadedCount() < mCoreAdCount;
    }

    private class AdSdkCallback extends SimpleAdSdkListener {
        private boolean mImpression = false;

        public void init() {
            mImpression = false;
        }

        @Override
        public void onLoaded(String pidName, String source, String adType) {
            removeLoadingAdplace(pidName);
            if (BuildConfig.DEBUG) {
                Log.v(Log.TAG, "add to adplaces from onloaded");
            }
            add(pidName);
            notifyOnRefresh();
            notifyOnLoaded();
        }

        @Override
        public void onClick(String pidName, String source, String adType) {
            notifyOnClick();
        }

        @Override
        public void onDismiss(String pidName, String source, String adType) {
            Log.v(Log.TAG, "onDismiss source : " + source);
            remove(pidName);
            notifyOnRefresh();
            notifyOnDismiss();
        }

        @Override
        public void onCompleted(String pidName, String source, String adType) {
            Log.v(Log.TAG, "onCompleted source : " + source);
            notifyOnRefresh();
        }

        @Override
        public void onError(String pidName, String source, String adType) {
            Log.e(Log.TAG, "onError source : " + source);
            if (mImpression) {
                remove(pidName);
                notifyOnReward();
            }
            if (BuildConfig.DEBUG && mImpression) {
                Toast.makeText(mContext, "onError source : " + source, Toast.LENGTH_SHORT).show();
            }
            removeLoadingAdplace(pidName);
            notifyOnRefresh();
        }

        @Override
        public void onShow(String pidName, String source, String adType) {
            mImpression = true;
            modify(pidName, true);
            notifyOnRefresh();
            notifyOnShow();
        }

        @Override
        public void onRewarded(String pidName, String source, String adType, AdReward item) {
            Log.v(Log.TAG, "onRewarded source : " + source);
            notifyOnReward();
            notifyOnRefresh();
        }
    }

    /**
     * 将加载成功的广告加入缓存
     *
     * @param s
     */
    private void add(String s) {
        if (mLoadedAdPlaces != null) {
            synchronized (mLoadedAdPlaces) {
                boolean contain = false;
                for (RewardItem item : mLoadedAdPlaces) {
                    if (item != null && TextUtils.equals(item.pidName, s)) {
                        contain = true;
                    }
                }
                if (!contain) {
                    if (BuildConfig.DEBUG) {
                        Log.v(Log.TAG, "add to loaded ad places");
                    }
                    mLoadedAdPlaces.add(new RewardItem(s, false));
                }
                Collections.sort(mLoadedAdPlaces, new Comparator<RewardItem>() {
                    @Override
                    public int compare(RewardItem o1, RewardItem o2) {
                        if (o1 != null && o1.pidName != null && o2 != null) {
                            return o1.pidName.compareTo(o2.pidName);
                        }
                        return 0;
                    }
                });
            }
        }
    }

    private void modify(String s, boolean showing) {
        synchronized (mLoadedAdPlaces) {
            if (mLoadedAdPlaces != null && !mLoadedAdPlaces.isEmpty()) {
                for (RewardItem item : mLoadedAdPlaces) {
                    if (item != null && TextUtils.equals(item.pidName, s)) {
                        if (BuildConfig.DEBUG) {
                            Log.v(Log.TAG, "modify item : " + s + " , to showing : " + showing);
                        }
                        item.showing = showing;
                        if (showing) {
                            item.time = SystemClock.elapsedRealtime();
                        }
                    }
                }
            }
        }
    }

    /**
     * 将失效的广告移除缓存
     *
     * @param s
     */
    private void remove(String s) {
        synchronized (mLoadedAdPlaces) {
            if (mLoadedAdPlaces != null && !mLoadedAdPlaces.isEmpty()) {
                for (int len = mLoadedAdPlaces.size() - 1; len >= 0; len--) {
                    RewardItem item = mLoadedAdPlaces.get(len);
                    if (item != null
                            && TextUtils.equals(item.pidName, s)
                            && mLoadedAdPlaces.contains(item)
                            && item.showing) {
                        if (BuildConfig.DEBUG) {
                            Log.v(Log.TAG, "remove item : " + item.pidName);
                        }
                        mLoadedAdPlaces.remove(item);
                    }
                }
            }
        }
    }

    /**
     * 检查激励视频广告状态
     */
    private void checkRewardStatus() {
        synchronized (mLoadedAdPlaces) {
            if (mLoadedAdPlaces != null) {
                RewardItem item = null;
                for (int len = mLoadedAdPlaces.size() - 1; len >= 0; len--) {
                    item = mLoadedAdPlaces.get(len);
                    if (item != null) {
                        if (needRemove(item) || isShowTimeout(item)) {
                            mLoadedAdPlaces.remove(item);
                        }
                    }
                }
            }
        }
    }

    /**
     * 是否需要移除缓存中的广告
     *
     * @param item
     * @return
     */
    private boolean needRemove(RewardItem item) {
        if (item == null) {
            return false;
        }
        return !(item.showing || AdSdk.get(mContext).isInterstitialLoaded(item.pidName));
    }

    /**
     * 激励视频展示超时
     *
     * @param item
     * @return
     */
    private boolean isShowTimeout(RewardItem item) {
        if (item == null) {
            return false;
        }
        return item.showing && SystemClock.elapsedRealtime() - item.time > TIMEOUT;
    }

    /**
     * 获取加载成功的广告个数
     *
     * @return
     */
    private int getLoadedCount() {
        if (mAllAdPlaces != null && !mAllAdPlaces.isEmpty()) {
            synchronized (mLoadedAdPlaces) {
                checkRewardStatus();
                int count = mLoadedAdPlaces != null ? mLoadedAdPlaces.size() : 0;
                if (BuildConfig.DEBUG) {
                    Log.v(Log.TAG, "count : " + count);
                }
                return count;
            }
        }
        return 0;
    }

    public boolean isRewardLoaded() {
        return getLoadedCount() > 0;
    }

    /**
     * 展示激励视频
     */
    public void showReward() {
        synchronized (mLoadedAdPlaces) {
            checkRewardStatus();
            if (mLoadedAdPlaces != null && !mLoadedAdPlaces.isEmpty()) {
                RewardItem item = null;
                for (RewardItem ri : mLoadedAdPlaces) {
                    if (ri != null && AdSdk.get(mContext).isInterstitialLoaded(ri.pidName)
                            && !ri.showing) {
                        item = ri;
                        break;
                    }
                }
                if (item != null) {
                    modify(item.pidName, true);
                    AdSdk.get(mContext).showInterstitial(item.pidName);
                } else {
                    Log.v(Log.TAG, "No rewards found");
                    notifyOnNoReward();
                }
            } else {
                Log.v(Log.TAG, "No rewards loading success");
                notifyOnNoReward();
            }
        }
    }

    public void registerListener(OnAdRewardListener l) {
        if (mListeners != null) {
            mListeners.push(l);
        }
        sendMessageDelayInternal(false);
    }

    public void unregisterListener(OnAdRewardListener l) {
        if (mListeners != null) {
            mListeners.pop();
        }
        cancelSendMessageIfNeed();
    }

    private void notifyOnReward() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onReward();
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnRefresh() {
        if (mListeners != null) {
            try {
                mListeners.peek().onRefresh(isRewardLoaded());
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnDismiss() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onDismiss();
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnClick() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onClick();
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnNoReward() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onNoReward();
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnShow() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onShow();
            } catch (Exception e) {
            }
        }
    }

    private void notifyOnLoaded() {
        Log.v(Log.TAG, "");
        if (mListeners != null) {
            try {
                mListeners.peek().onLoaded();
            } catch (Exception e) {
            }
        }
    }

    private class RewardItem {
        public RewardItem(String pidName, boolean showing) {
            this.pidName = pidName;
            this.showing = showing;
        }

        public String pidName;
        public boolean showing;
        public long time;
    }
}
