package com.github.bcsdk.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.github.bcsdk.log.Log;
import com.github.bcsdk.utils.BcUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-12-29.
 */

public class ReferrerManager implements InstallReferrerStateListener, Runnable {

    public static final String GOOGLE_PLAY_PKGNAME = "com.android.vending";
    public static final String PREF_AT_USER_STATUS = "pref_at_user_status";
    public static final String PREF_AT_MEDIA_SOURCE = "pref_at_media_source";
    public static final String PREF_AT_FROM_CLICK = "pref_at_from_click";
    public static final String PREF_REFERER_REPORT = "pref_referer_report";

    private static final String AT_ORGANIC = new String(Base64.decode("T3JnYW5pYw==", 0));
    private static final String AT_NON_ORGANIC = new String(Base64.decode("Tm9uLW9yZ2FuaWM=", 0));
    private static final String MEDIUM = new String(Base64.decode("b3JnYW5pYw==", 0));
    private static final String GCLID = new String(Base64.decode("Z2NsaWQ=", 0));
    private static final String HAS_GCLID = new String(Base64.decode("aGFzX2djbGlk", 0));

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
        return BcUtils.getString(mContext, PREF_AT_USER_STATUS, null);
    }

    public String getMediaSource() {
        return BcUtils.getString(mContext, PREF_AT_MEDIA_SOURCE);
    }

    public boolean isFromClick() {
        return BcUtils.getBoolean(mContext, PREF_AT_FROM_CLICK);
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

    private OnDataListener mOnDataListener;

    private ReferrerManager(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void init(OnDataListener l) {
        mOnDataListener = l;
        if (TextUtils.isEmpty(BcUtils.getString(mContext, PREF_AT_USER_STATUS))) {
            try {
                obtainReferrer();
            } catch (Exception e) {
                BcUtils.putString(mContext, PREF_AT_USER_STATUS, AT_ORGANIC);
                if (mOnDataListener != null) {
                    mOnDataListener.onReferrerResult(AT_ORGANIC, null, false);
                }
            }
        }
    }

    /**
     * 获取install_referrer, 分别处理超时和没有安装googleplay的情况
     */
    private void obtainReferrer() {
        if (BcUtils.isInstalled(mContext, GOOGLE_PLAY_PKGNAME)) {
            mReferrerClient = InstallReferrerClient.newBuilder(mContext).build();
            mReferrerClient.startConnection(this);
            // 5秒钟没有回调的话，按照超时处理
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_REFERRER_CLIENT);
        } else {
            reportReferrer(null);
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
            ref.put(HAS_GCLID, ref.containsKey(GCLID));
        }
        return ref;
    }

    private boolean isOrganic(Map<String, Object> ref) {
        if (ref != null) {
            try {
                String medium = (String) ref.get("utm_medium");
                return MEDIUM.equalsIgnoreCase(medium);
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
        if (TextUtils.isEmpty(BcUtils.getString(mContext, PREF_AT_USER_STATUS))) {
            reportReferrer(null);
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
            if (BcUtils.getBoolean(mContext, PREF_REFERER_REPORT, false)) {
                // Referer信息已经上报
                return;
            }
            // 记录Referer已经上报
            BcUtils.putBoolean(mContext, PREF_REFERER_REPORT, true);

            String atStatus;
            String mediaSource;
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
                fromClick = map.containsKey(GCLID);
            }
            BcUtils.putString(mContext, PREF_AT_USER_STATUS, atStatus);
            BcUtils.putString(mContext, PREF_AT_MEDIA_SOURCE, mediaSource);
            BcUtils.putBoolean(mContext, PREF_AT_FROM_CLICK, fromClick);

            ReportRunnable reportRunnable = new ReportRunnable(mContext, atStatus, mediaSource, map);
            if (mHandler != null) {
                mHandler.postDelayed(reportRunnable, 4000);
            }
            onReferrerResult(atStatus, mediaSource, fromClick);
        }
    }

    private void onReferrerResult(String atStatus, String mediaSource, boolean fromClick) {
        if (mOnDataListener != null) {
            mOnDataListener.onReferrerResult(atStatus, mediaSource, fromClick);
        }
    }

    class ReportRunnable implements Runnable {
        private final String mediaSource;
        private final String atStatus;
        private final Context context;
        private final Map<String, Object> extra;

        public ReportRunnable(Context context, String atStatus, String atMs, Map<String, Object> extra) {
            this.context = context;
            this.atStatus = atStatus;
            this.mediaSource = atMs;
            this.extra = extra;
        }

        @Override
        public void run() {
            reportEvent(context, "at_user_status", atStatus, null);
            reportEvent(context, "at_media_source", mediaSource, null);
            reportEvent(context, "at_utm", null, extra);
            if (TextUtils.equals(atStatus, AT_NON_ORGANIC)) {
                reportEvent(context, "app_first_open_ano", null, null);
            } else {
                reportEvent(context, "app_first_open_ao", null, null);
            }
        }
    }

    private static void mapToBundle(Map<String, Object> map, Bundle bundle) {
        if (map == null || bundle == null) {
            return;
        }
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry != null) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    if (!TextUtils.isEmpty(key)) {
                        if (valueObj == null) {
                            bundle.putString(key, null);
                        } else if (valueObj instanceof Integer) {
                            bundle.putInt(key, ((Integer) valueObj).intValue());
                        } else if (valueObj instanceof Float) {
                            bundle.putFloat(key, ((Float) valueObj).floatValue());
                        } else if (valueObj instanceof Double) {
                            bundle.putDouble(key, ((Double) valueObj).doubleValue());
                        } else if (valueObj instanceof Boolean) {
                            bundle.putBoolean(key, ((Boolean) valueObj).booleanValue());
                        } else if (valueObj instanceof Byte) {
                            bundle.putByte(key, ((Byte) valueObj).byteValue());
                        } else if (valueObj instanceof Short) {
                            bundle.putShort(key, ((Short) valueObj).shortValue());
                        } else if (valueObj instanceof Long) {
                            bundle.putLong(key, ((Long) valueObj).longValue());
                        } else if (valueObj instanceof String) {
                            bundle.putString(key, valueObj.toString());
                        } else if (valueObj instanceof boolean[]) {
                            bundle.putBooleanArray(key, (boolean[]) valueObj);
                        } else if (valueObj instanceof int[]) {
                            bundle.putIntArray(key, (int[]) valueObj);
                        } else if (valueObj instanceof long[]) {
                            bundle.putLongArray(key, (long[]) valueObj);
                        } else if (valueObj instanceof double[]) {
                            bundle.putDoubleArray(key, (double[]) valueObj);
                        } else if (valueObj instanceof String[]) {
                            bundle.putStringArray(key, (String[]) valueObj);
                        } else {
                            bundle.putString(key, String.valueOf(valueObj));
                        }
                    }
                }
            }
        }
    }

    public static void reportEvent(Context context, String eventId, String value, Map<String, Object> extra) {
        String platform = "firebase";
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        }
        mapToBundle(extra, bundle);
        Log.iv(Log.TAG, platform + " event id : " + eventId + " , value : " + bundle);
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
            Log.iv(Log.TAG, "error : " + error);
        }
    }

    public abstract static class OnDataListener {
        public void onReferrerResult(String status, String mediaSource, boolean fromClick) {
        }
    }
}