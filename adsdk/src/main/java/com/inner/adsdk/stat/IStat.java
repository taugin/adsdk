package com.inner.adsdk.stat;

import android.content.Context;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IStat {
    public void reportAdRequest(Context context, String pidName, String sdk, String type, Map<String, String> extra);
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, Map<String, String> extra);
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra);
    public void reportAdClick(Context context, String pidName, String sdk, String type, Map<String, String> extra);
    public void reportAdOuterRequest(Context context);
    public void reportAdOuterLoaded(Context context);
    public void reportAdOuterShow(Context context);
    public void reportAdOuterShowing(Context context);
}
