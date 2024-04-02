package com.mix.ads.core.framework;

import android.content.Context;
import android.text.TextUtils;

import com.mix.ads.MiStat;
import com.mix.ads.data.DataManager;
import com.mix.ads.utils.Utils;

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
        return DataManager.get(mContext).getActiveDays() == 0;
    }

    public void reportFirebaseImpression(String network, String adType, String sceneName, Double adRevenue) {
        String formatNetwork = Utils.formatNetwork(network);
        if (TextUtils.isEmpty(adType)) {
            adType = "unknown";
        }
        if (TextUtils.isEmpty(sceneName)) {
            sceneName = "unknown";
        }
        if (isNewUser()) {
            String impTotalPattern = "gav_imp_new_total";
            sendFirebaseGavImpEvent(impTotalPattern, adRevenue);
            // gav_imp_new_network_%s_%s
            String impNetworkPattern = String.format(Locale.ENGLISH, "gav_inn_%s_%s", formatNetwork, adType);
            sendFirebaseGavImpEvent(impNetworkPattern, adRevenue);
            // gav_imp_new_scene_%s_%s
            String impScenePattern = String.format(Locale.ENGLISH, "gav_ins_%s", sceneName);
            sendFirebaseGavImpEvent(impScenePattern, adRevenue);
        }
        String impTotalPattern = "gav_imp_active_total";
        sendFirebaseGavImpEvent(impTotalPattern, adRevenue);
        // gav_imp_active_network_%s_%s
        String impNetworkPattern = String.format(Locale.ENGLISH, "gav_ian_%s_%s", formatNetwork, adType);
        sendFirebaseGavImpEvent(impNetworkPattern, adRevenue);
        // gav_imp_active_scene_%s_%s
        String impScenePattern = String.format(Locale.ENGLISH, "gav_ias_%s", sceneName);
        sendFirebaseGavImpEvent(impScenePattern, adRevenue);
    }

    private void sendFirebaseGavImpEvent(String eventName, Double adRevenue) {
        Map<String, Object> map = new HashMap<>();
        map.put("currency", "USD");
        map.put("value", adRevenue != null ? adRevenue.doubleValue() : 0f);
        MiStat.sendFirebaseAnalytics(mContext, eventName, null, map, MiStat.isInFirebaseWhiteList(eventName));
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
            String clkNetworkPattern = String.format(Locale.ENGLISH, "gav_cnn_%s_%s", formatNetwork, adType);
            sendFirebaseGavClkEvent(clkNetworkPattern);
            String clkScenePattern = String.format(Locale.ENGLISH, "gav_cns_%s", sceneName);
            sendFirebaseGavClkEvent(clkScenePattern);
        }
        String clkTotalPattern = "gav_clk_active_total";
        sendFirebaseGavClkEvent(clkTotalPattern);
        String clkNetworkPattern = String.format(Locale.ENGLISH, "gav_can_%s_%s", formatNetwork, adType);
        sendFirebaseGavClkEvent(clkNetworkPattern);
        String clkScenePattern = String.format(Locale.ENGLISH, "gav_cas_%s", sceneName);
        sendFirebaseGavClkEvent(clkScenePattern);
    }

    private void sendFirebaseGavClkEvent(String eventName) {
        MiStat.sendFirebaseAnalytics(mContext, eventName, null, null, MiStat.isInFirebaseWhiteList(eventName));
    }
}
