package com.simple.mpsdk.stat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.simple.mpsdk.data.DataConfig;
import com.simple.mpsdk.log.LogHelper;
import com.simple.mpsdk.utils.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class ReportImpl implements IReport {

    private static ReportImpl sStatImpl;

    private Object mFacebookObject = null;
    private boolean mReportUmeng;
    private boolean mReportFacebook;
    private boolean mReportAppsflyer;
    private boolean mReportFirebase;
    private boolean mReportError;
    private long MIN_TIME = Long.parseLong("900000");

    public static ReportImpl get() {
        synchronized (ReportImpl.class) {
            if (sStatImpl == null) {
                createInstance();
            }
        }
        return sStatImpl;
    }

    private static void createInstance() {
        synchronized (ReportImpl.class) {
            if (sStatImpl == null) {
                sStatImpl = new ReportImpl();
            }
        }
    }

    private ReportImpl() {
    }

    public void init() {
    }


    private boolean checkArgument(Context context, String pidName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(pidName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            LogHelper.e(LogHelper.TAG, "context or pidname or sdk or type all must not be empty or null");
            return false;
        }
        return true;
    }

    private String generateEventId(Context context, String action, String sdk, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append("_");
        builder.append(type);
        builder.append("_");
        builder.append(sdk);
        return builder.toString();
    }

    private void updateReportOptions(Context context) {
        try {
            long last = Utils.getLong(context, "last_update_report_time");
            long now = System.currentTimeMillis();
            if (now - last > MIN_TIME) {
                boolean reportUmeng = parseReport(DataConfig.get(context).getString("report_umeng"));
                boolean reportFacebook = parseReport(DataConfig.get(context).getString("report_facebook"));
                boolean reportAppsflyer = parseReport(DataConfig.get(context).getString("report_appsflyer"));
                boolean reportFirebase = parseReport(DataConfig.get(context).getString("report_firebase"));
                boolean reportError = parseReport(DataConfig.get(context).getString("report_error"));
                setReportOption(reportUmeng, reportFacebook, reportAppsflyer, reportFirebase, reportError);
                Utils.putLong(context, "last_update_report_time", now);
            }
        } catch (Exception e) {
            LogHelper.iv(LogHelper.TAG, "error : " + e);
        }
    }

    private boolean parseReport(String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 发送Firebase统计事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendFirebaseAnalytics(Context context, String value, String eventId, Map<String, String> extra) {
        LogHelper.d(LogHelper.TAG, "ReportImpl Firebase Analytics");
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("param", value);
        } else {
            bundle.putString("param", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        bundle.putString(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        String error = null;
        try {
            Class<?> clazz = Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            Method method = clazz.getMethod("getInstance", Context.class);
            Object instance = method.invoke(null, context);
            method = clazz.getMethod("logEvent", String.class, Bundle.class);
            method.invoke(instance, eventId, bundle);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl Firebase error : " + error);
        }
    }

    /**
     * 发送友盟计数事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendUmeng(Context context, String value, String eventId, Map<String, String> extra) {
        LogHelper.d(LogHelper.TAG, "ReportImpl sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(value)) {
            map.put("param", value);
        } else {
            map.put("param", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEvent", Context.class, String.class, Map.class);
            method.invoke(null, context, eventId, map);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl sendUmeng error : " + error);
        }
    }

    /**
     * 发送友盟计算事件
     *
     * @param context
     * @param eventId
     * @param extra
     * @param value
     */
    private void sendUmengEventValue(Context context, String eventId, Map<String, String> extra, int value) {
        if (!mReportUmeng) {
            return;
        }
        LogHelper.d(LogHelper.TAG, "ReportImpl sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.umeng.analytics.MobclickAgent");
            Method method = clazz.getDeclaredMethod("onEventValue", Context.class, String.class, Map.class, int.class);
            method.invoke(null, context, eventId, map, value);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl sendUmengEventValue error : " + error);
        }
    }

    /**
     * 发送appsflyer统计事件
     *
     * @param context
     * @param value
     * @param eventId
     * @param extra
     */
    private void sendAppsflyer(Context context, String value, String eventId, Map<String, String> extra) {
        LogHelper.d(LogHelper.TAG, "ReportImpl sendAppsflyer Analytics");
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("param", value);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        eventValue.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.appsflyer.AppsFlyerLib");
            Method method = clazz.getMethod("getInstance");
            Object instance = method.invoke(null);
            method = clazz.getMethod("trackEvent", Context.class, String.class, Map.class);
            method.invoke(instance, context, eventId, eventValue);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl sendAppsflyer error : " + error);
        }
    }

    private void initFacebook(Context context) {
        if (mFacebookObject != null) {
            return;
        }
        String error = null;
        try {
            Class<?> clazz = Class.forName("com.facebook.appevents.AppEventsLogger");
            Method method = clazz.getMethod("newLogger", Context.class);
            mFacebookObject = method.invoke(null, context);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl initFacebook error : " + error);
        }
    }

    private void sendFacebook(Context context, String value, String eventId, Map<String, String> extra) {
        initFacebook(context);
        if (mFacebookObject == null) {
            return;
        }
        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(value)) {
            bundle.putString("param", value);
        } else {
            bundle.putString("param", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        bundle.putString(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        String error = null;
        try {
            Class<?> clazz = Class.forName("com.facebook.appevents.AppEventsLogger");
            Method method = clazz.getMethod("logEvent", String.class, Bundle.class);
            method.invoke(mFacebookObject, eventId, bundle);
        } catch (Exception e) {
            error = String.valueOf(e);
        } catch (Error e) {
            error = String.valueOf(e);
        }
        if (!TextUtils.isEmpty(error)) {
            LogHelper.v(LogHelper.TAG, "ReportImpl sendFacebook error : " + error);
        }
    }

    @Override
    public void reportAdRequest(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        updateReportOptions(context);
        String eventId = generateEventId(context, "request", sdk, type);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        // sendAppsflyer(context, pidName, eventId, extra);
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "loaded", sdk, type);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        // sendAppsflyer(context, pidName, eventId, extra);
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "show", sdk, type);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (mReportAppsflyer) {
            sendAppsflyer(context, pidName, eventId, extra);
        }
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdImp(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "imp", sdk, type);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (mReportAppsflyer) {
            sendAppsflyer(context, pidName, eventId, extra);
        }
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdClick(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "click", sdk, type);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        if (mReportAppsflyer) {
            sendAppsflyer(context, pidName, eventId, extra);
        }
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void reportAdError(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!mReportError) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId(context, "error", sdk, type);
        extra = addExtraForError(context, extra);
        if (mReportFirebase) {
            sendFirebaseAnalytics(context, pidName, eventId, extra);
        }
        if (mReportUmeng) {
            sendUmeng(context, pidName, eventId, extra);
        }
        // sendAppsflyer(context, pidName, eventId, extra);
        if (mReportFacebook) {
            sendFacebook(context, pidName, eventId, extra);
        }
        LogHelper.pv(LogHelper.TAG, "ReportImpl eventId : " + eventId + " , value : " + pidName);
    }

    @Override
    public void setReportOption(boolean umeng, boolean facebook, boolean appsflyer, boolean firebase, boolean reportError) {
        mReportUmeng = umeng;
        mReportFacebook = facebook;
        mReportAppsflyer = appsflyer;
        mReportFirebase = firebase;
        mReportError = reportError;
    }

    private Map<String, String> addExtraForError(Context context, Map<String, String> extra) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            StringBuilder builder = new StringBuilder();
            boolean isConnected = false;
            String networkType = null;
            String subworkType = null;
            if (networkInfo != null) {
                isConnected = networkInfo.isAvailable();
                networkType = networkInfo.getTypeName();
                subworkType = networkInfo.getSubtypeName();
            }
            builder.append(isConnected ? "Y" : "N");
            if (!TextUtils.isEmpty(networkType)) {
                builder.append("[");
                builder.append(networkType);
                if (!TextUtils.isEmpty(subworkType)) {
                    builder.append("-");
                    builder.append(subworkType);
                }
                builder.append("]");
            }
            if (extra == null) {
                extra = new HashMap<String, String>();
            }
            extra.put("network", builder.toString());
        } catch (Exception e) {
        } catch (Error e) {
        }
        return extra;
    }
}