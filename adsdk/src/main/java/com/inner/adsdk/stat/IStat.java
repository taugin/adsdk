package com.inner.adsdk.stat;

import android.content.Context;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IStat {
    /**
     * 广告请求
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdRequest(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告请求成功
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告展示
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdCallShow(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告展示
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告点击
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdClick(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告加载错误
     * @param context
     * @param error
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdError(Context context, String error, String sdk, String type, Map<String, String> extra);

    /**
     * 广告加载成功时间
     * @param context
     * @param value
     */
    public void reportAdLoadSuccessTime(Context context, String sdk, String type, int value);

    /**
     * 广告加载成功时间
     * @param context
     * @param value
     */
    public void reportAdLoadFailureTime(Context context, String sdk, String type, String error, int value);

    /**
     * ltv上报展示
     * @param context
     * @param sdk
     * @param pid
     */
    public void reportAdShowForLTV(Context context, String sdk, String pid, String ecpm);

    /**
     * ltv上报展示
     * @param context
     * @param sdk
     * @param pid
     */
    public void reportAdImpForLTV(Context context, String sdk, String pid, String ecpm);

    /**
     * ltv上报点击
     * @param context
     * @param sdk
     * @param pid
     */
    public void reportAdClickForLTV(Context context, String sdk, String pid, String ecpm);

}
