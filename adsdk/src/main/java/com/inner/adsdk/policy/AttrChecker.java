package com.inner.adsdk.policy;

import android.content.Context;

import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/7/18.
 */

public class AttrChecker {

    private Context mContext;

    public AttrChecker() {
    }

    public AttrChecker(Context context) {
        mContext = context;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * 归因是否允许(自然/非自然)
     *
     * @return
     */
    public boolean isAttributionAllow(List<String> attr) {
        boolean disableAttribution = android.util.Log.isLoggable("organic", android.util.Log.VERBOSE);
        Log.iv(Log.TAG, "da : " + disableAttribution);
        if (disableAttribution) {
            return true;
        }
        String afStatus = getAfStatus();
        Log.iv(Log.TAG, "af_s : " + afStatus);
        if (attr != null && !attr.contains(afStatus)) {
            return false;
        }
        return true;
    }

    public boolean isCountryAllow(List<String> countryList) {
        String country = getCountry();
        Log.iv(Log.TAG, "country : " + country);
        if (countryList != null && !countryList.isEmpty()) {
            List<String> includeCountries = new ArrayList<String>();
            List<String> excludeCountries = new ArrayList<String>();
            for (String s : countryList) {
                if (s != null) {
                    if (s.startsWith("!")) {
                        excludeCountries.add(s);
                    } else {
                        includeCountries.add(s);
                    }
                }
            }
            if (includeCountries.size() > 0) {
                // 包含列表如果不包含当前国家，则返回false
                if (!includeCountries.contains(country)) {
                    return false;
                }
            } else if (excludeCountries.size() > 0) {
                // 排斥列表如果包含当前国家，则返回
                if (excludeCountries.contains("!" + country)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断媒体源是否允许
     *
     * @return
     */
    public boolean isMediaSourceAllow(List<String> mediaList) {
        String mediaSource = getMediaSource();
        Log.iv(Log.TAG, "ms : " + mediaSource);
        if (mediaList != null && !mediaList.isEmpty()) {
            List<String> includeMs = new ArrayList<String>();
            List<String> excludeMs = new ArrayList<String>();
            for (String s : mediaList) {
                if (s != null) {
                    if (s.startsWith("!")) {
                        excludeMs.add(s);
                    } else {
                        includeMs.add(s);
                    }
                }
            }
            if (includeMs.size() > 0) {
                // 包含列表如果不包含当前媒体源，则返回false
                if (!includeMs.contains(mediaSource)) {
                    return false;
                }
            } else if (excludeMs.size() > 0) {
                // 排斥列表如果包含当前媒体源，则返回false
                if (excludeMs.contains("!" + mediaSource)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断版本号是否允许
     *
     * @return
     */
    public boolean isVersionAllow(List<String> verList) {
        String version = getVersion();
        Log.iv(Log.TAG, "ver : " + version);
        if (verList != null && !verList.isEmpty()) {
            List<String> includeVl = new ArrayList<String>();
            List<String> excludeVl = new ArrayList<String>();
            for (String s : verList) {
                if (s != null) {
                    if (s.startsWith("!")) {
                        excludeVl.add(s);
                    } else {
                        includeVl.add(s);
                    }
                }
            }
            if (includeVl.size() > 0) {
                // 包含列表如果不包含当前版本，则返回false
                if (!includeVl.contains(version)) {
                    return false;
                }
            } else if (excludeVl.size() > 0) {
                // 排斥列表如果包含当前版本，则返回false
                if (excludeVl.contains("!" + version)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getAfStatus() {
        try {
            return Utils.getString(mContext, Constant.AF_STATUS);
        } catch(Exception e) {
        }
        return null;
    }

    private String getMediaSource() {
        try {
            return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
        } catch(Exception e) {
        }
        return null;
    }

    private String getVersion() {
        try {
            return Utils.getVersionName(mContext);
        } catch(Exception e) {
        }
        return null;
    }

    private String getCountry() {
        return Utils.getCountry(mContext);
    }
}
