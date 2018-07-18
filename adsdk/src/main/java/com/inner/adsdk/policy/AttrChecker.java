package com.inner.adsdk.policy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2018/7/18.
 */

public class AttrChecker {

    private Context mContext;

    public AttrChecker(Context context) {
        mContext = context;
    }

    /**
     * 归因是否允许(自然/非自然)
     *
     * @return
     */
    public boolean isAttributionAllow(List<String> attr) {
        boolean disableAttribution = android.util.Log.isLoggable("disable_attribute", android.util.Log.VERBOSE);
        Log.v(Log.TAG, "da : " + disableAttribution);
        if (disableAttribution) {
            return true;
        }
        String afStatus = getAfStatus();
        Log.d(Log.TAG, "af_status : " + afStatus);
        if (attr != null && !attr.contains(afStatus)) {
            return false;
        }
        return true;
    }

    public boolean isCountryAllow(List<String> countryList) {
        String country = getCountry();
        Log.v(Log.TAG, "country : " + country);
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
        Log.d(Log.TAG, "media_source : " + mediaSource);
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

    private String getAfStatus() {
        return Utils.getString(mContext, Constant.AF_STATUS);
    }

    private String getMediaSource() {
        return Utils.getString(mContext, Constant.AF_MEDIA_SOURCE);
    }

    private String getCountry() {
        String country = null;
        try {
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = mContext.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = mContext.getResources().getConfiguration().locale;
            }
            country = locale.getCountry();
        } catch (Exception e) {
        }
        if (!TextUtils.isEmpty(country)) {
            country = country.toLowerCase(Locale.getDefault());
        }
        return country;
    }
}
