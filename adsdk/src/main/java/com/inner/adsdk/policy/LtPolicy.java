package com.inner.adsdk.policy;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.inner.adsdk.config.LtConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.Date;

/**
 * Created by Administrator on 2018/3/19.
 */

public class LtPolicy {
    private static LtPolicy sLtPolicy;

    public static LtPolicy get(Context context) {
        synchronized (LtPolicy.class) {
            if (sLtPolicy == null) {
                createInstance(context);
            }
        }
        return sLtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (LtPolicy.class) {
            if (sLtPolicy == null) {
                sLtPolicy = new LtPolicy(context);
            }
        }
    }

    private LtPolicy(Context context) {
        mContext = context;
        mAttrChecker = new AttrChecker(context);
    }

    private Context mContext;
    private LtConfig mLtConfig;
    private AttrChecker mAttrChecker;

    public void init() {
    }

    public void setPolicy(LtConfig ltConfig) {
        mLtConfig = ltConfig;
    }

    /**
     * 配置是否允许
     *
     * @return
     */
    private boolean isConfigAllow() {
        if (mLtConfig != null) {
            return mLtConfig.isEnable();
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
        if (mLtConfig != null && mLtConfig.getUpDelay() > 0) {
            long now = System.currentTimeMillis();
            long firstStartTime = getFirstStartUpTime();
            return now - firstStartTime > mLtConfig.getUpDelay();
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    private boolean isAppVerAllow() {
        if (mLtConfig != null && mLtConfig.getMaxVersion() > 0) {
            int verCode = Utils.getVersionCode(mContext);
            return verCode <= mLtConfig.getMaxVersion();
        }
        return true;
    }

    private boolean matchInstallTime() {
        if (mLtConfig != null) {
            long configInstallTime = mLtConfig.getConfigInstallTime();
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

    private boolean checkAdLtConfig() {
        if (!isConfigAllow()) {
            Log.v(Log.TAG, "con not allowed");
            return false;
        }

        if (mLtConfig != null && !mAttrChecker.isAttributionAllow(mLtConfig.getAttrList())) {
            Log.v(Log.TAG, "attr not allowed");
            return false;
        }

        if (mLtConfig != null && !mAttrChecker.isCountryAllow(mLtConfig.getCountryList())) {
            Log.v(Log.TAG, "country not allowed");
            return false;
        }

        if (mLtConfig != null && !mAttrChecker.isMediaSourceAllow(mLtConfig.getMediaList())) {
            Log.v(Log.TAG, "ms not allowed");
            return false;
        }

        if (!isDelayAllow()) {
            Log.v(Log.TAG, "d not allowed");
            return false;
        }

        if (!isAppVerAllow()) {
            Log.v(Log.TAG, "maxver not allowed");
            return false;
        }

        if (!matchInstallTime()) {
            Log.v(Log.TAG, "cit not allowed");
            return false;
        }
        return true;
    }

    public boolean isLtAllowed() {
        Log.v(Log.TAG, "lt : " + mLtConfig);
        if (!checkAdLtConfig()) {
            return false;
        }
        return true;
    }
}
