package com.scene.crazy.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.http.OnStringCallback;
import com.scene.crazy.log.Log;
import com.rabbit.adsdk.utils.Utils;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2020-3-14.
 */

public class IpManager {

    private final Context mContext;
    private static final int MSG_REQUEST_ONGOING = 0x100861;
    private static final String IP_CHECK_URL1 = "http://ip-api.com/json/";
    private static final String IP_CHECK_URL2 = "http://whois.pconline.com.cn/ipJson.jsp?json=true";
    private static final String IP_CHECK_URL3 = "http://pv.sohu.com/cityjson?ie=utf-8";
    private static final String PREF_CURRENT_IPADDR = "pref_current_ipaddr";
    private static final Map<String, IpParser> sParserMap = new LinkedHashMap<String, IpParser>();

    static {
        sParserMap.put(IP_CHECK_URL1, new Parser1());
        sParserMap.put(IP_CHECK_URL2, new Parser2());
        sParserMap.put(IP_CHECK_URL3, new Parser3());
    }

    private static IpManager sIpManager;
    public static IpManager get(Context context) {
        synchronized (IpManager.class) {
            if (sIpManager == null) {
                createInstance(context);
            }
        }
        return sIpManager;
    }

    private static void createInstance(Context context) {
        synchronized (IpManager.class) {
            if (sIpManager == null) {
                sIpManager = new IpManager(context);
            }
        }
    }

    private Handler mHandler = new Handler();
    private IpManager(Context context) {
        mContext = context;
    }

    public String getIpAddr() {
        return Utils.getString(mContext, PREF_CURRENT_IPADDR);
    }

    public void check() {
        if (mHandler.hasMessages(MSG_REQUEST_ONGOING)) {
            return;
        }
        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_ONGOING, 5000);
        if (sParserMap != null) {
            Set<Map.Entry<String, IpParser>> keySet = sParserMap.entrySet();
            if (keySet != null) {
                Iterator<Map.Entry<String, IpParser>> iterator = keySet.iterator();
                checkAddress(iterator);
            }
        }
    }

    private void checkAddress(final Iterator<Map.Entry<String, IpParser>> iterator) {
        if (!iterator.hasNext()) {
            return;
        }
        final Map.Entry<String, IpParser> entry = iterator.next();
        if (entry == null) {
            return;
        }
        String addr = entry.getKey();
        final IpParser ipParser = entry.getValue();
        if (TextUtils.isEmpty(addr) || ipParser == null) {
            return;
        }
        Http.get(mContext).request(addr, null, new OnStringCallback() {
            @Override
            public void onSuccess(String content) {
                boolean success = false;
                if (!TextUtils.isEmpty(content)) {
                    content = content.trim();
                    if (parseAndSave(content, ipParser)) {
                        success = true;
                    }
                }
                if (!success) {
                    checkAddress(iterator);
                }
            }

            @Override
            public void onFailure(int code, String error) {
                checkAddress(iterator);
            }
        });
    }

    private boolean parseAndSave(String content, IpParser ipParser) {
        Log.iv(Log.TAG, "result : " + content);
        if (ipParser != null) {
            String ipAddr = ipParser.parse(content);
            Log.iv(Log.TAG, "ipaddr : " + ipAddr);
            if (!TextUtils.isEmpty(ipAddr)) {
                Utils.putString(mContext, PREF_CURRENT_IPADDR, ipAddr);
                return true;
            }
        }
        return false;
    }

    private interface IpParser {
        String parse(String content);
    }

    private static class Parser1 implements IpParser {
        @Override
        public String parse(String content) {
            try {
                JSONObject jobj = new JSONObject(content);
                if (jobj.has("query")) {
                    return jobj.getString("query");
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            return null;
        }
    }

    private static class Parser2 implements IpParser {
        @Override
        public String parse(String content) {
            try {
                JSONObject jobj = new JSONObject(content);
                if (jobj.has("ip")) {
                    return jobj.getString("ip");
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            return null;
        }
    }

    private static class Parser3 implements IpParser {
        @Override
        public String parse(String content) {
            try {
                int index = content.indexOf("=");
                int length = content.length();
                content = content.substring(index + 1, length - 1);
                content = content.trim();
                JSONObject jobj = new JSONObject(content);
                if (jobj.has("cip")) {
                    return jobj.getString("cip");
                }
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
            }
            return null;
        }
    }
}
