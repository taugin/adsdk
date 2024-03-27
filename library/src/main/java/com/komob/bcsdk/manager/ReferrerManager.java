package com.komob.bcsdk.manager;

import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.komob.adsdk.InternalStat;
import com.komob.bcsdk.BcSdk;
import com.komob.bcsdk.constant.Constant;
import com.komob.bcsdk.OnDataListener;
import com.komob.bcsdk.log.Log;
import com.komob.bcsdk.utils.BcUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-29.
 */

public class ReferrerManager implements InstallReferrerStateListener, Runnable {
    private static final String AT_ORGANIC = "Organic";
    private static final String AT_NON_ORGANIC = "Non-organic";
    private static ReferrerManager sReferrerManager;

    public static ReferrerManager get(Context context) {
        synchronized (ReferrerManager.class) {
            if (sReferrerManager == null) {
                createInstance(context);
            }
        }
        return sReferrerManager;
    }

    public String getAttribution() {
        return BcUtils.getString(mContext, Constant.AT_STATUS, null);
    }

    public String getMediaSource() {
        return BcUtils.getString(mContext, Constant.AT_MEDIA_SOURCE);
    }

    public boolean isFromClick() {
        return BcUtils.getBoolean(mContext, Constant.AT_FROM_CLICK);
    }

    private static void createInstance(Context context) {
        synchronized (ReferrerManager.class) {
            if (sReferrerManager == null) {
                sReferrerManager = new ReferrerManager(context);
            }
        }
    }

    private static final int DELAY_REFERRER_CLIENT = 5 * 1000;
    private Context mContext;
    private InstallReferrerClient mReferrerClient;
    private Handler mHandler;

    private ReferrerManager(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void init() {
        if (TextUtils.isEmpty(BcUtils.getString(mContext, Constant.AT_STATUS))) {
            try {
                obtainReferrer();
            } catch (Exception e) {
                BcUtils.putString(mContext, Constant.AT_STATUS, Constant.AT_ORGANIC);
                OnDataListener l = BcSdk.getOnDataListener();
                if (l != null) {
                    l.onReferrerResult(Constant.AT_ORGANIC, null, false);
                }
            }
        }
    }

    /**
     * 获取install_referrer, 分别处理超时和没有安装googleplay的情况
     */
    private void obtainReferrer() {
        if (BcUtils.isInstalled(mContext, Constant.GOOGLE_PLAY_PKGNAME)) {
            mReferrerClient = InstallReferrerClient.newBuilder(mContext).build();
            mReferrerClient.startConnection(this);
            // 5秒钟没有回调的话，按照超时处理
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_REFERRER_CLIENT);
        } else {
            reportReferrer("referrer_unknown_no_google_play");
        }
    }

    private Map<String, Object> referrerToMap(String referrer) {
        if (TextUtils.isEmpty(referrer)) {
            return null;
        }
        Map<String, Object> ref = null;
        String[] split = referrer.split("&");
        if (split != null && split.length > 0) {
            ref = new HashMap<String, Object>();
            for (String s : split) {
                String[] tmp = s.split("=");
                if (tmp != null && tmp.length == 2) {
                    ref.put(tmp[0], tmp[1]);
                }
            }
        }
        if (ref != null) {
            ref.put("has_gclid", ref.containsKey("gclid"));
        }
        return ref;
    }

    private boolean isOrganic(Map<String, Object> ref) {
        if (ref != null) {
            try {
                String medium = (String) ref.get("utm_medium");
                return "organic".equalsIgnoreCase(medium);
            } catch (Exception e) {
            }
        }
        return false;
    }

    private String getSource(Map<String, Object> ref) {
        String source = null;
        if (ref != null && ref.containsKey("utm_source")) {
            try {
                source = (String) ref.get("utm_source");
            } catch (Exception e) {
            }
        }
        return source;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(BcUtils.getString(mContext, Constant.AT_STATUS))) {
            reportReferrer("referrer_client_no_reply");
        }
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        String referrer = null;
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                try {
                    referrer = mReferrerClient.getInstallReferrer().getInstallReferrer();
                } catch (Exception e) {
                    Log.e(Log.TAG, "error : " + e, e);
                }
                break;
        }
        mHandler.removeCallbacks(this);
        reportReferrer(referrer);
        try {
            if (mReferrerClient != null) {
                mReferrerClient.endConnection();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
    }

    /**
     * 通过referrer参数设置归因
     *
     * @param referrer
     */
    public void reportReferrer(String referrer) {
        synchronized (ReferrerManager.class) {
            if (BcUtils.getBoolean(mContext, Constant.PREF_REFERER_REPORT, false)) {
                // Referer信息已经上报
                return;
            }
            // 记录Referer已经上报
            BcUtils.putBoolean(mContext, Constant.PREF_REFERER_REPORT, true);

            String atStatus;
            String mediaSource;
            String reportReferrer;
            if (TextUtils.equals(referrer, "referrer_unknown_no_google_play")) {
                reportReferrer = "referrer_unknown_no_google_play";
                referrer = "utm_source=noplay&utm_medium=cpc";
            } else if (TextUtils.equals(referrer, "referrer_client_no_reply")) {
                reportReferrer = "referrer_client_no_reply";
                referrer = "utm_source=noreply&utm_medium=organic";
            } else {
                reportReferrer = "client_referrer";
            }
            if (isNonOrganic()) {
                referrer = "utm_source=google-ads&utm_medium=cpc&gclid=custom";
            }
            Map<String, Object> map = referrerToMap(referrer);
            boolean isOrganic = isOrganic(map);
            if (isOrganic) {
                atStatus = AT_ORGANIC;
            } else {
                atStatus = AT_NON_ORGANIC;
            }
            mediaSource = getSource(map);
            boolean fromClick = false;
            if (map != null) {
                fromClick = map.containsKey("gclid") || map.containsKey("af_tranid") || map.containsKey("adjust_reftag");
            }
            BcUtils.putString(mContext, Constant.AT_STATUS, atStatus);
            BcUtils.putString(mContext, Constant.AT_MEDIA_SOURCE, mediaSource);
            BcUtils.putBoolean(mContext, Constant.AT_FROM_CLICK, fromClick);

            ReportRunnable reportRunnable = new ReportRunnable(mContext, atStatus, mediaSource, reportReferrer, map);
            if (mHandler != null) {
                mHandler.postDelayed(reportRunnable, 4000);
            }
            OnDataListener l = BcSdk.getOnDataListener();
            if (l != null) {
                l.onReferrerResult(atStatus, mediaSource, fromClick);
            }
        }
    }

    private boolean isNonOrganic() {
        try {
            InputStream nonOrganicInputStream = mContext.getAssets().open("non-organic");
            nonOrganicInputStream.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    class ReportRunnable implements Runnable {
        private final String reportReferrer;
        private final String mediaSource;
        private final String atStatus;
        private final Context context;
        private final Map<String, Object> extra;

        public ReportRunnable(Context context, String atStatus, String atMs, String atReferer, Map<String, Object> extra) {
            this.context = context;
            this.atStatus = atStatus;
            this.mediaSource = atMs;
            this.reportReferrer = atReferer;
            this.extra = extra;
        }

        @Override
        public void run() {
            String packageName = null;
            if (context != null) {
                packageName = context.getPackageName();
            }
            String signMd5 = BcUtils.getSignMd5(context);
            String installerPackage = getInstallerPackage();
            String installerAppName = getInstallerAppName(installerPackage);
            String installerInfo = "";
            if (TextUtils.isEmpty(installerPackage)) {
                installerPackage = "unknown";
                installerAppName = "unknown";
            }
            installerInfo = installerPackage + "[" + installerAppName + "]";
            Log.iv(Log.TAG, "status : " + atStatus
                    + " , ms : " + mediaSource
                    + " , ref : " + reportReferrer
                    + " , pkg : " + packageName
                    + " , sign : " + signMd5
                    + " , installer : " + installerPackage
                    + " , installer_info : " + installerInfo
                    + " , utm : " + extra);
            InternalStat.reportEvent(context, "at_status", atStatus, null);
            InternalStat.reportEvent(context, "at_ms", mediaSource, null);
            InternalStat.reportEvent(context, "at_referrer", reportReferrer, null);
            InternalStat.reportEvent(context, "at_pkg_name", packageName, null);
            InternalStat.reportEvent(context, "at_sign_md5", signMd5, null);
            InternalStat.reportEvent(context, "at_installer", installerPackage, null);
            InternalStat.reportEvent(context, "at_installer_info", installerInfo, null);
            InternalStat.reportEvent(context, "at_utm", null, extra);
            if (TextUtils.equals(atStatus, AT_NON_ORGANIC)) {
                InternalStat.reportEvent(context, "app_first_open_ano", null, null);
            } else {
                InternalStat.reportEvent(context, "app_first_open_ao", null, null);
            }
        }

        private String getInstallerPackage() {
            String installerPackage = null;
            try {
                PackageManager packageManager = mContext.getPackageManager();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    InstallSourceInfo installSourceInfo = packageManager.getInstallSourceInfo(mContext.getPackageName());
                    if (installSourceInfo != null) {
                        installerPackage = installSourceInfo.getInstallingPackageName();
                    }
                } else {
                    installerPackage = packageManager.getInstallerPackageName(mContext.getPackageName());
                }
            } catch (Exception e) {
            }
            return installerPackage;
        }

        private String getInstallerAppName(String packageName) {
            String appName = null;
            if (!TextUtils.isEmpty(packageName)) {
                PackageManager packageManager = mContext.getPackageManager();
                try {
                    appName = packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString();
                } catch (Exception e) {
                }
            }
            return appName;
        }
    }
}