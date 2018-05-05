package com.inner.adsdk.stat;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.inner.adsdk.config.AdSwitch;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class StatImpl implements IStat {

    private static StatImpl sStatImpl;

    public static StatImpl get() {
        synchronized (StatImpl.class) {
            if (sStatImpl == null) {
                createInstance();
            }
        }
        return sStatImpl;
    }

    private static void createInstance() {
        synchronized (StatImpl.class) {
            if (sStatImpl == null) {
                sStatImpl = new StatImpl();
            }
        }
    }

    private StatImpl() {
    }

    private Tracker tracker;

    public void init(Context context, String gaTrackerId) {
        if (!TextUtils.isEmpty(gaTrackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            tracker = analytics.newTracker(gaTrackerId);
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
            tracker.set("&cd1", null);
        }
    }

    private boolean checkArgument(Context context, String pidName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(pidName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.e(Log.TAG, "context or pidname or sdk or type all must not be empty or null");
            return false;
        }
        return true;
    }

    private String generateEventId(String action, String sdk, String type) {
        StringBuilder builder = new StringBuilder();
        builder.append(action);
        builder.append("_");
        builder.append(type);
        builder.append("_");
        builder.append(sdk);
        return builder.toString();
    }

    private void sendGoogleAnalytics(String label, String action, String category) {
        Log.d(Log.TAG, "StatImpl sendGoogle Analytics");
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
        if (!TextUtils.isEmpty(category)) {
            builder.setCategory(category);
        }
        if (!TextUtils.isEmpty(action)) {
            builder.setAction(action);
        }
        if (!TextUtils.isEmpty(label)) {
            builder.setLabel(label);
        }
        Map<String, String> event = builder.build();
        if (tracker != null) {
            tracker.send(event);
        }
    }

    private void sendUmeng(Context context, String value, String eventId, Map<String, String> extra) {
        Log.d(Log.TAG, "StatImpl sendUmeng Analytics");
        HashMap<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(value)) {
            map.put("entry_point", value);
        } else {
            map.put("entry_point", eventId);
        }
        if (extra != null && !extra.isEmpty()) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                if (entry != null) {
                    if (!TextUtils.isEmpty(entry.getKey()) && TextUtils.isEmpty(entry.getValue())) {
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
            Log.v(Log.TAG, "StatImpl sendUmeng error : " + error);
        }
    }

    private void sendAppsflyer(Context context, String value, String eventId, Map<String, String> extra) {
        Log.d(Log.TAG, "StatImpl sendAppsflyer Analytics");
        Map<String, Object> eventValue = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(value)) {
            eventValue.put("entry_point", value);
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
            Log.v(Log.TAG, "StatImpl sendAppsflyer error : " + error);
        }
    }

    @Override
    public void reportAdRequest(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("request", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }

    @Override
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("loaded", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }

    @Override
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("show", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }

    @Override
    public void reportAdClick(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (!checkArgument(context, pidName, sdk, type)) {
            return;
        }
        String eventId = generateEventId("click", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }

    @Override
    public void reportAdOuterRequest(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_request";
        String category = "user_action";
        sendGoogleAnalytics(null, eventId, category);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "");
    }

    @Override
    public void reportAdOuterLoaded(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_loaded";
        String category = "user_action";
        sendGoogleAnalytics(null, eventId, category);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "");
    }

    @Override
    public void reportAdOuterShow(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_show";
        String category = "user_action";
        sendGoogleAnalytics(null, eventId, category);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "");
    }

    @Override
    public void reportAdOuterShowing(Context context) {
        if (context == null) {
            return;
        }
        String eventId = "outer_gt_showing";
        String category = "user_action";
        sendGoogleAnalytics(null, eventId, category);
        sendUmeng(context, null, eventId, null);
        Log.v(Log.TAG, "");
    }

    @Override
    public void reportAdLoading(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        if (context == null) {
            return;
        }
        String eventId = generateEventId("loading", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }

    @Override
    public void reportAdError(Context context, String pidName, String sdk, String type, Map<String, String> extra) {
        AdSwitch adSwitch = DataManager.get(context).getAdSwitch();
        if (adSwitch == null || !adSwitch.isReportError()) {
            return;
        }
        if (context == null) {
            return;
        }
        String eventId = generateEventId("error", sdk, type);
        String category = "user_action";
        sendGoogleAnalytics(pidName, eventId, category);
        sendUmeng(context, pidName, eventId, extra);
        sendAppsflyer(context, pidName, eventId, extra);
        Log.v(Log.TAG, "StatImpl stat key : " + eventId + " , value : " + pidName + " , category : " + category);
    }
}