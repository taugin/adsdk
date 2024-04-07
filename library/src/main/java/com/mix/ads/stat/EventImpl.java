package com.mix.ads.stat;

import android.content.Context;
import android.text.TextUtils;

import com.mix.ads.MiStat;
import com.mix.ads.constant.Constant;
import com.mix.ads.core.db.DBManager;
import com.mix.ads.core.framework.AdStatManager;
import com.mix.ads.core.framework.BounceRateManager;
import com.mix.ads.core.framework.FBStatManager;
import com.mix.ads.log.Log;
import com.mix.ads.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/20.
 */

public class EventImpl implements IEvent {

    private static EventImpl sEventImpl;

    public static EventImpl get() {
        synchronized (EventImpl.class) {
            if (sEventImpl == null) {
                createInstance();
            }
        }
        return sEventImpl;
    }

    private static void createInstance() {
        synchronized (EventImpl.class) {
            if (sEventImpl == null) {
                sEventImpl = new EventImpl();
            }
        }
    }

    private Context mContext;

    private EventImpl() {
    }

    public void init(Context context) {
        mContext = context;
        BounceRateManager.get(context).init();
    }

    private String generateEventIdAlias(Context context, String eventId) {
        return eventId;
    }

    private boolean checkArgument(Context context, String placeName, String sdk, String type) {
        if (context == null || TextUtils.isEmpty(placeName) || TextUtils.isEmpty(sdk) || TextUtils.isEmpty(type)) {
            Log.iv(Log.TAG, "context == null or place name == null or sdk == null or type all must not be empty or null");
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
        return generateEventIdAlias(context, builder.toString());
    }

    @Override
    public void reportAdRequest(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdLoaded(Context context, String placeName, String sdk, String network, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdReLoaded(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdShow(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdImp(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        String eventId = generateEventId(context, "imp", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
        reportEvent(context, "e_ad_imp", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
        try {
            AdStatManager.get(mContext).recordAdImp(sdk, placeName, network);
        } catch (Exception e) {
        }
        if (!TextUtils.equals(sdk, Constant.AD_SDK_ADMOB) && network != null && network.toLowerCase(Locale.ENGLISH).contains(Constant.AD_SDK_ADMOB)) {
            eventId = generateEventId(context, "imp", Constant.AD_SDK_ADMOB, type);
            reportEvent(context, eventId, placeName, extra);
        }
    }

    @Override
    public void reportAdClick(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra, String impressionId) {
        if (!checkArgument(context, placeName, sdk, type)) {
            return;
        }
        boolean isAdClicked = DBManager.get(context).isAdClicked(impressionId);
        String eventId = generateEventId(context, "click", sdk, type);
        extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
        if (extra != null) {
            extra.put("first", String.valueOf(!isAdClicked));
        }
        reportEvent(context, "e_ad_click", placeName, extra);
        reportEvent(context, eventId, placeName, extra);
        reportAdClickDistinct(context, placeName, sdk, network, type, pid, networkPid, ecpm, extra, isAdClicked);
        try {
            AdStatManager.get(mContext).recordAdClick(sdk, placeName, pid, network, extra, impressionId);
        } catch (Exception e) {
        }
        if (!TextUtils.equals(sdk, Constant.AD_SDK_ADMOB) && network != null && network.toLowerCase(Locale.ENGLISH).contains(Constant.AD_SDK_ADMOB)) {
            eventId = generateEventId(context, "click", Constant.AD_SDK_ADMOB, type);
            reportEvent(context, eventId, placeName, extra);
            reportAdClickDistinct(context, placeName, Constant.AD_SDK_ADMOB, network, type, pid, networkPid, ecpm, extra, isAdClicked);
        }
        String placement = null;
        if (extra != null) {
            try {
                placement = (String) extra.get("placement");
            } catch (Exception e) {
            }
        }
        FBStatManager.get(context).reportFirebaseClick(type, network, placement);
    }

    private void reportAdClickDistinct(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra, boolean isAdClicked) {
        try {
            if (!isAdClicked) {
                String eventIdDistinct = generateEventId(context, "click", sdk + "_distinct", type);
                extra = addExtra(extra, placeName, sdk, type, pid, ecpm, network, networkPid);
                reportEvent(context, eventIdDistinct, placeName, extra);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void reportAdReward(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdError(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    @Override
    public void reportAdClose(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra) {
    }

    public void reportEvent(Context context, String eventId, String value, Map<String, Object> extra) {
        Map<String, Object> maps = extra;
        MiStat.sendFirebaseAnalytics(context, eventId, value, maps);
    }

    private Map<String, Object> addExtra(Map<String, Object> extra, String name, String sdk, String type, String pid, double ecpm, String network, String networkPid) {
        if (extra == null) {
            extra = new HashMap<String, Object>();
        }
        extra.put("name", name);
        extra.put("sdk", sdk);
        extra.put("type", type);
        extra.put("network", network);
        extra.put("network_pid", networkPid);
        extra.put("pid", pid);
        extra.put("ecpm", ecpm);
        extra.put("country", Utils.getCountryFromLocale(mContext));
        return extra;
    }
}