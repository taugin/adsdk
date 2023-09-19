package com.hauyu.adsdk.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.InternalStat;
import com.hauyu.adsdk.Utils;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.stat.EventImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FBStatManager {
    private static FBStatManager sFBStatManager;

    public static FBStatManager get(Context context) {
        synchronized (FBStatManager.class) {
            if (sFBStatManager == null) {
                createInstance(context);
            }
        }
        return sFBStatManager;
    }

    private static void createInstance(Context context) {
        synchronized (FBStatManager.class) {
            if (sFBStatManager == null) {
                sFBStatManager = new FBStatManager(context);
            }
        }
    }

    private Context mContext;

    private FBStatManager(Context context) {
        mContext = context;
    }


    private boolean isNewUser() {
        return EventImpl.get().getActiveDays() == 0;
    }

    public void reportFirebaseImpression(Map<String, Object> adImpMap) {
        String network = (String) adImpMap.get(Constant.AD_NETWORK);
        String formatNetwork = Utils.formatNetwork(network);
        String adType = (String) adImpMap.get(Constant.AD_TYPE);
        if (TextUtils.isEmpty(adType)) {
            adType = "unknown";
        }
        String sceneName = (String) adImpMap.get(Constant.AD_PLACEMENT);
        if (TextUtils.isEmpty(sceneName)) {
            sceneName = "unknown";
        }
        Double adRevenue = (Double) adImpMap.get(Constant.AD_VALUE);
        if (isNewUser()) {
            String impTotalPattern = "gav_imp_new_total";
            sendFirebaseGavImpEvent(impTotalPattern, adRevenue);
            String impNetworkPattern = String.format(Locale.ENGLISH, "gav_imp_new_network_%s_%s", formatNetwork, adType);
            sendFirebaseGavImpEvent(impNetworkPattern, adRevenue);
            String impScenePattern = String.format(Locale.ENGLISH, "gav_imp_new_scene_%s", sceneName);
            sendFirebaseGavImpEvent(impScenePattern, adRevenue);
        }
        String impTotalPattern = "gav_imp_active_total";
        sendFirebaseGavImpEvent(impTotalPattern, adRevenue);
        String impNetworkPattern = String.format(Locale.ENGLISH, "gav_imp_active_network_%s_%s", formatNetwork, adType);
        sendFirebaseGavImpEvent(impNetworkPattern, adRevenue);
        String impScenePattern = String.format(Locale.ENGLISH, "gav_imp_active_scene_%s", sceneName);
        sendFirebaseGavImpEvent(impScenePattern, adRevenue);
    }

    private void sendFirebaseGavImpEvent(String eventName, Double adRevenue) {
        Map<String, Object> map = new HashMap<>();
        map.put("currency", "USD");
        map.put("value", adRevenue != null ? adRevenue.doubleValue() : 0f);
        InternalStat.sendFirebaseAnalytics(mContext, eventName, null, map, InternalStat.isInFirebaseWhiteList(eventName));
    }

    public void reportFirebaseClick(String adType, String network, String sceneName) {
        String formatNetwork = Utils.formatNetwork(network);
        if (TextUtils.isEmpty(adType)) {
            adType = "unknown";
        }
        if (TextUtils.isEmpty(sceneName)) {
            sceneName = "unknown";
        }
        if (isNewUser()) {
            String clkTotalPattern = "gav_clk_new_total";
            sendFirebaseGavClkEvent(clkTotalPattern);
            String clkNetworkPattern = String.format(Locale.ENGLISH, "gav_clk_new_network_%s_%s", formatNetwork, adType);
            sendFirebaseGavClkEvent(clkNetworkPattern);
            String clkScenePattern = String.format(Locale.ENGLISH, "gav_clk_new_scene_%s", sceneName);
            sendFirebaseGavClkEvent(clkScenePattern);
        }
        String clkTotalPattern = "gav_clk_active_total";
        sendFirebaseGavClkEvent(clkTotalPattern);
        String clkNetworkPattern = String.format(Locale.ENGLISH, "gav_clk_active_network_%s_%s", formatNetwork, adType);
        sendFirebaseGavClkEvent(clkNetworkPattern);
        String clkScenePattern = String.format(Locale.ENGLISH, "gav_clk_active_scene_%s", sceneName);
        sendFirebaseGavClkEvent(clkScenePattern);
    }

    private void sendFirebaseGavClkEvent(String eventName) {
        InternalStat.sendFirebaseAnalytics(mContext, eventName, null, null, InternalStat.isInFirebaseWhiteList(eventName));
    }
}
