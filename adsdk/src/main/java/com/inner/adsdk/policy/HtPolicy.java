package com.inner.adsdk.policy;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.inner.adsdk.config.HtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.Date;

/**
 * Created by Administrator on 2018/3/19.
 */

public class HtPolicy implements Handler.Callback {
    private static HtPolicy sHtPolicy;

    public static HtPolicy get(Context context) {
        synchronized (HtPolicy.class) {
            if (sHtPolicy == null) {
                createInstance(context);
            }
        }
        return sHtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (HtPolicy.class) {
            if (sHtPolicy == null) {
                sHtPolicy = new HtPolicy(context);
            }
        }
    }

    private HtPolicy(Context context) {
        mContext = context;
        mAttrChecker = new AttrChecker(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private static final int MSG_HT_LOADING = 10001;

    private Context mContext;
    private HtConfig mHtConfig;
    private AttrChecker mAttrChecker;
    private boolean mLoading = false;
    private Handler mHandler;

    public void init() {
    }

    public void setPolicy(HtConfig htConfig) {
        mHtConfig = htConfig;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null && msg.what == MSG_HT_LOADING) {
            mLoading = false;
        }
        return false;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        if (mLoading) {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_HT_LOADING);
                Log.v(Log.TAG, "ht send loading timeout : " + getTimeout());
                mHandler.sendEmptyMessageDelayed(MSG_HT_LOADING, getTimeout());
            }
        } else {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_HT_LOADING);
                Log.v(Log.TAG, "ht remove loading timeout");
            }
        }
    }

    public boolean isLoading() {
        return mLoading;
    }

    private long getTimeout() {
        return 300000;
    }

    /**
     * 更新ad最后展示时间
     */
    private void updateLastShowTime() {
        Utils.putLong(mContext, Constant.PREF_HT_LAST_TIME, System.currentTimeMillis());
    }

    /**
     * 获取ad最后展示时间
     *
     * @return
     */
    private long getLastShowTime() {
        return Utils.getLong(mContext, Constant.PREF_HT_LAST_TIME, 0);
    }


    /**
     * 记录ad展示标记
     *
     * @param showing
     */
    public void reportHtShowing(boolean showing) {
        if (showing) {
            updateLastShowTime();
        }
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mHtConfig != null) {
            return mHtConfig.isEnable();
        }
        return false;
    }

    /**
     * 获取应用首次展示时间
     *
     * @return
     */
    private long getFirstStartUpTime() {
        return Utils.getLong(mContext, Constant.PREF_FIRST_STARTUP_TIME, 0);
    }

    /**
     * 延迟间隔是否允许
     *
     * @return
     */
    private boolean isDelayAllow() {
        if (mHtConfig != null && mHtConfig.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mHtConfig.getUpDelay();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    private boolean isAppVerAllow() {
        if (mHtConfig != null && mHtConfig.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mHtConfig.getMaxVersion();
        }
        return true;
    }

    private boolean matchInstallTime() {
        if (mHtConfig != null) {
            long configInstallTime = mHtConfig.getConfigInstallTime();
            long firstInstallTime = getFirstInstallTime();
            String cit = configInstallTime > 0 ? Constant.SDF_1.format(new Date(configInstallTime)) : "0";
            String fit = firstInstallTime > 0 ? Constant.SDF_1.format(new Date(firstInstallTime)) : "0";
            Log.v(Log.TAG, "cit : " + cit + " , fit : " + fit);
            if (configInstallTime <= 0 || firstInstallTime <= 0) {
                return true;
            }
            return firstInstallTime <= configInstallTime;
        }
        return true;
    }

    private long getFirstInstallTime() {
        long firstInstallTime = 0;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            firstInstallTime = packageInfo.firstInstallTime;
        } catch (Exception e) {
        } catch (Error e) {
        }
        return firstInstallTime;
    }

    /**
     * 展示间隔是否允许
     *
     * @return
     */
    private boolean isIntervalAllow() {
        if (mHtConfig != null && mHtConfig.getInterval() > 0) {
            long now = System.currentTimeMillis();
            long last = getLastShowTime();
            Log.v(Log.TAG, "ht i allow now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(last)));
            return now - last > mHtConfig.getInterval();
        }
        return true;
    }

    private boolean checkAdHtConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "con not allowed");
            return false;
        }

        if (mHtConfig != null && !mAttrChecker.isAttributionAllow(mHtConfig.getAttrList())) {
            Log.v(Log.TAG, "attr not allowed");
            return false;
        }

        if (mHtConfig != null && !mAttrChecker.isCountryAllow(mHtConfig.getCountryList())) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (mHtConfig != null && !mAttrChecker.isMediaSourceAllow(mHtConfig.getMediaList())) {
            Log.v(Log.TAG, "ms not allowed");
            return false;
        }

        if (!isDelayAllow()) {
            Log.v(Log.TAG, "d not allowed");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.v(Log.TAG, "mv not allowed");
            return false;
        }

        if (!isIntervalAllow()) {
            Log.v(Log.TAG, "i not allow");
            return false;
        }

        if (!matchInstallTime()) {
            Log.v(Log.TAG, "cit not allowed");
            return false;
        }
        return true;
    }

    public boolean isHtAllowed() {
        Log.v(Log.TAG, "ht : " + mHtConfig);
        if (!checkAdHtConfig()) {
            return false;
        }
        return true;
    }
}
