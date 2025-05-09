package com.komob.adsdk.stat;

import android.content.Context;
import android.text.TextUtils;

import com.komob.adsdk.AdImpData;
import com.komob.adsdk.InternalStat;
import com.komob.adsdk.constant.Constant;
import com.komob.adsdk.data.DataManager;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.SpUtils;
import com.komob.adsdk.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdImpReport {

    private static Map<String, Object> bundleImpData(AdImpData adImpData) {
        String networkName = adImpData.getNetwork();
        String platform = adImpData.getPlatform();
        String unitName = adImpData.getUnitName();
        String placement = adImpData.getPlacement();
        String adType = adImpData.getAdType();
        String networkPid = adImpData.getNetworkPid();
        String unitId = adImpData.getUnitId();
        String adPrecision = adImpData.getPrecision();
        boolean adBidding = adImpData.isBidding();
        String formatNetwork = Utils.formatNetwork(networkName);
        if (TextUtils.isEmpty(networkPid)) {
            networkPid = unitId;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ad_platform", platform);
        params.put("ad_source", networkName);
        params.put("ad_format", adImpData.getAdFormat());
        params.put("ad_type", adType);
        params.put("ad_unit_name", unitName);
        params.put("ad_placement", placement);
        params.put("ad_network_pid", networkPid + "[" + formatNetwork + "_" + adType + "]");
        params.put("ad_unit_id", unitId);
        params.put("ad_precision", adPrecision);
        params.put("ad_bidding", adBidding);
        params.put("ad_network_and_type", formatNetwork + "_" + adType);
        params.put("value", adImpData.getValue());
        params.put("micro_value", Double.valueOf(adImpData.getValue() * 1000000).intValue());
        params.put("currency", "USD"); // All Applovin revenue is sent in USD
        return params;
    }

    /**
     * 上报ad_impression事件，firebase通过此事件计算收入
     *
     * @param adImpData
     */
    public static void reportAdImpression(Context context, AdImpData adImpData) {
        try {
            if (adImpData != null && isEnableReportAdImpression(context)) {
                String networkName = adImpData.getNetwork();
                boolean isReportFirebase = true;
                if (isForbidReportAdImpressionAdmob(context)) {
                    if (networkName != null) {
                        String temp = networkName.toLowerCase(Locale.ENGLISH);
                        if (temp != null && temp.contains("admob")) {
                            isReportFirebase = false;
                        }
                    }
                }
                Map<String, Object> params = bundleImpData(adImpData);
                if (isReportFirebase) {
                    InternalStat.sendFirebaseAnalytics(context, Constant.AD_IMPRESSION, null, params);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void reportAdImpressionAll(Context context, AdImpData adImpData) {
        try {
            if (adImpData != null && isEnableReportAdImpression(context)) {
                Map<String, Object> params = bundleImpData(adImpData);
                InternalStat.sendFirebaseAnalytics(context, "ad_impression_all", null, params);
                if (EventImpl.get().getActiveDays() == 0) {
                    InternalStat.sendFirebaseAnalytics(context, "ad_impression_ado", null, params);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void reportAdClickAll(Context context, AdImpData adImpData) {
        try {
            if (adImpData != null) {
                Map<String, Object> params = bundleImpData(adImpData);
                InternalStat.sendFirebaseAnalytics(context, "ad_click_all", null, params);
                if (EventImpl.get().getActiveDays() == 0) {
                    InternalStat.sendFirebaseAnalytics(context, "ad_click_ado", null, params);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void reportTaichiEvent(Context context, AdImpData adImpData) {
        try {
            if (adImpData != null && isEnableReportTaichi30(context)) {
                Double revenue = adImpData.getValue();
                if (revenue != null && revenue.doubleValue() > 0) {
                    reportTaichiEvent(context, revenue.floatValue());
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 是否禁止上报admob广告展示价值，默认不禁止，只有当admob与firebase关联时，才需要禁止
     *
     * @return
     */
    private static boolean isForbidReportAdImpressionAdmob(Context context) {
        boolean result = false;
        try {
            String str = DataManager.get(context).getString("ad_forbid_report_admob");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }

    /**
     * 上报taichi事件
     *
     * @param context
     * @param revenue
     */
    private static void reportTaichiEvent(Context context, float revenue) {
        String prefRevenue = "pref_total_taichi_revenue";
        float lastTotalRevenue = SpUtils.getFloat(context, prefRevenue);
        float curTotalRevenue = lastTotalRevenue + revenue;
        Log.iv(Log.TAG, "last total revenue : " + lastTotalRevenue + " , current total revenue : " + curTotalRevenue + " , revenue : " + revenue);
        if (curTotalRevenue >= 0.01f) {
            SpUtils.putFloat(context, prefRevenue, 0f);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("currency", "USD");
            map.put("value", curTotalRevenue);
            map.put("micro_value", Double.valueOf(curTotalRevenue * 1000000).intValue());
            InternalStat.reportEvent(context, Constant.AD_TOTAL_ADS_REVENUE_001, map);
        } else {
            SpUtils.putFloat(context, prefRevenue, curTotalRevenue);
        }
    }

    /**
     * 是否允许上报ad_impression事件
     *
     * @return
     */
    private static boolean isEnableReportAdImpression(Context context) {
        boolean result = true;
        try {
            String str = DataManager.get(context).getString("ad_enable_report_ad_impression");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }

    /**
     * 是否允许上报taichi30事件
     *
     * @return
     */
    private static boolean isEnableReportTaichi30(Context context) {
        boolean result = true;
        try {
            String str = DataManager.get(context).getString("ad_enable_report_taichi30");
            if (!TextUtils.isEmpty(str)) {
                result = Boolean.parseBoolean(str);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return result;
    }
}
