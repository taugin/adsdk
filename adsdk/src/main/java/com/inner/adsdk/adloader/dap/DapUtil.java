package com.inner.adsdk.adloader.dap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DapUtil {

    public static ArrayList<Integer> mPidList = new ArrayList<>();

    public static synchronized void addPid(int pid) {
        if (!mPidList.contains(pid)) {
            mPidList.add(pid);
        }
    }

    // 获取 DuappsAd 初始化的参数，包括各种广告的 pid。
    public static String getAdJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("native", getNativeArray());
//            object.put("list", getListArray());
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private static synchronized JSONArray getNativeArray() {
        JSONArray nativeArray = new JSONArray();
        for (int i = 0; i < mPidList.size(); i++) {
            nativeArray.put(getAdJson(mPidList.get(i)));
        }
        return nativeArray;
    }

//    private static JSONArray getListArray() {
//        JSONArray listArray = new JSONArray();
//        listArray.put(getAdJson(DuAdPid.REC_APP));
//        return listArray;
//    }

    private static JSONObject getAdJson(int adPid) {
        JSONObject adObject = new JSONObject();
        try {
            adObject.put("pid", adPid);
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        return adObject;
    }
}
