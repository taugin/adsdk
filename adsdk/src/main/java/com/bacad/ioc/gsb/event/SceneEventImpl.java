package com.bacad.ioc.gsb.event;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.ScFl;
import com.bacad.ioc.gsb.data.SceneData;
import com.hauyu.adsdk.log.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class SceneEventImpl implements IEvent {

    private static SceneEventImpl sSceneEventImpl;

    private Object mFacebookObject = null;

    public static SceneEventImpl get() {
        synchronized (SceneEventImpl.class) {
            if (sSceneEventImpl == null) {
                createInstance();
            }
        }
        return sSceneEventImpl;
    }

    private static void createInstance() {
        synchronized (SceneEventImpl.class) {
            if (sSceneEventImpl == null) {
                sSceneEventImpl = new SceneEventImpl();
            }
        }
    }

    private SceneEventImpl() {
    }

    public void init() {
    }

    private String generateEventIdAlias(Context context, String eventId) {
        return eventId;
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
        Log.d(Log.TAG, "SceneEventImpl Firebase Analytics");
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        } else {
            bundle.putString("entry_point", eventId);
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
            Log.v(Log.TAG, "SceneEventImpl Firebase error : " + error);
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
        Log.d(Log.TAG, "SceneEventImpl sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        } else {
            map.put("entry_point", eventId);
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
            Log.v(Log.TAG, "SceneEventImpl sendUmeng error : " + error);
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
        if (!isReportUmeng(context)) {
            return;
        }
        Log.d(Log.TAG, "SceneEventImpl sendUmeng Analytics");
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
            Log.v(Log.TAG, "SceneEventImpl sendUmengEventValue error : " + error);
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
            Log.v(Log.TAG, "SceneEventImpl initFacebook error : " + error);
        }
    }

    private void sendFacebook(Context context, String value, String eventId, Map<String, String> extra) {
        initFacebook(context);
        if (mFacebookObject == null) {
            return;
        }
        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(value)) {
            bundle.putString("entry_point", value);
        } else {
            bundle.putString("entry_point", eventId);
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
            Log.v(Log.TAG, "SceneEventImpl sendFacebook error : " + error);
        }
    }

    private String generateAdSceneKey(String adSceneType, String op) {
        return "scene_" + adSceneType + "_" + op;
    }

    @Override
    public void reportAdSceneRequest(Context context, String adSceneType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "request");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdSceneLoaded(Context context, String adSceneType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "loaded");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdSceneShow(Context context, String adSceneType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "show");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdSceneImp(Context context, String adSceneType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "imp");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdSceneDisallow(Context context, String adSceneType, String pidName) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "disallow");
        eventId = generateEventIdAlias(context, eventId);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, pidName, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, pidName, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, pidName, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId);
    }

    @Override
    public void reportAdSceneShowTimes(Context context, String adSceneType, int times) {
        if (context == null) {
            return;
        }
        String eventId = generateAdSceneKey(adSceneType, "showtimes");
        eventId = generateEventIdAlias(context, eventId);
        String value = String.valueOf(times);
        if (isReportFirebase(context)) {
            sendFirebaseAnalytics(context, value, eventId, null);
        }
        if (isReportUmeng(context)) {
            sendUmeng(context, value, eventId, null);
        }
        if (isReportFacebook(context)) {
            sendFacebook(context, value, eventId, null);
        }
        Log.iv(Log.TAG, "SceneEventImpl stat key : " + eventId + ", times : " + times);
    }

    private boolean isReportUmeng(Context context) {
        ScFl ScFl = SceneData.get(context).getScFl();
        if (ScFl != null) {
            return ScFl.isReportUmeng();
        }
        return true;
    }

    private boolean isReportFirebase(Context context) {
        ScFl ScFl = SceneData.get(context).getScFl();
        if (ScFl != null) {
            return ScFl.isReportFirebase();
        }
        return true;
    }

    private boolean isReportFacebook(Context context) {
        ScFl ScFl = SceneData.get(context).getScFl();
        if (ScFl != null) {
            return ScFl.isReportFacebook();
        }
        return true;
    }
}