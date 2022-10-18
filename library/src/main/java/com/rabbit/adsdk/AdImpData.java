package com.rabbit.adsdk;

import com.rabbit.adsdk.constant.Constant;

import java.util.Map;

public class AdImpData {
    private final Map<String, Object> mAdImpData;

    private AdImpData(Map<String, Object> adImpData) {
        mAdImpData = adImpData;
    }

    public static AdImpData createAdImpData(Map<String, Object> adImpData) {
        return new AdImpData(adImpData);
    }

    public String getRequestId() {
        try {
            return (String) mAdImpData.get(Constant.AD_REQUEST_ID);
        } catch (Exception e) {
        }
        return null;
    }

    public Double getValue() {
        try {
            return (Double) mAdImpData.get(Constant.AD_VALUE);
        } catch (Exception e) {
        }
        return null;
    }

    public String getCurrency() {
        try {
            return (String) mAdImpData.get(Constant.AD_CURRENCY);
        } catch (Exception e) {
        }
        return null;
    }

    public String getNetwork() {
        try {
            return (String) mAdImpData.get(Constant.AD_NETWORK);
        } catch (Exception e) {
        }
        return null;
    }

    public String getNetworkPid() {
        try {
            return (String) mAdImpData.get(Constant.AD_NETWORK_PID);
        } catch (Exception e) {
        }
        return null;
    }

    public String getUnitId() {
        try {
            return (String) mAdImpData.get(Constant.AD_UNIT_ID);
        } catch (Exception e) {
        }
        return null;
    }

    public String getFormat() {
        try {
            return (String) mAdImpData.get(Constant.AD_FORMAT);
        } catch (Exception e) {
        }
        return null;
    }

    public String getType() {
        try {
            return (String) mAdImpData.get(Constant.AD_TYPE);
        } catch (Exception e) {
        }
        return null;
    }

    public String getUnitName() {
        try {
            return (String) mAdImpData.get(Constant.AD_UNIT_NAME);
        } catch (Exception e) {
        }
        return null;
    }

    public String getPlacement() {
        try {
            return (String) mAdImpData.get(Constant.AD_PLACEMENT);
        } catch (Exception e) {
        }
        return null;
    }

    public String getPlatform() {
        try {
            return (String) mAdImpData.get(Constant.AD_PLATFORM);
        } catch (Exception e) {
        }
        return null;
    }

    public String getPrecision() {
        try {
            return (String) mAdImpData.get(Constant.AD_PRECISION);
        } catch (Exception e) {
        }
        return null;
    }

    public String getCountryCode() {
        try {
            return (String) mAdImpData.get(Constant.AD_COUNTRY_CODE);
        } catch (Exception e) {
        }
        return null;
    }

    public String getSdkVersion() {
        try {
            return (String) mAdImpData.get(Constant.AD_SDK_VERSION);
        } catch (Exception e) {
        }
        return null;
    }

    public String getAppVersion() {
        try {
            return (String) mAdImpData.get(Constant.AD_APP_VERSION);
        } catch (Exception e) {
        }
        return null;
    }

    public String getGaid() {
        try {
            return (String) mAdImpData.get(Constant.AD_GAID);
        } catch (Exception e) {
        }
        return null;
    }

    public boolean isBidding() {
        try {
            return (boolean) mAdImpData.get(Constant.AD_BIDDING);
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String toString() {
        return "AdImpData{" +
                "mAdImpData=" + mAdImpData +
                '}';
    }
}
