package com.mix.ads.stat;

import android.content.Context;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IEvent {
    /**
     * 广告请求
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdRequest(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告请求成功
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdLoaded(Context context, String placeName, String sdk, String network, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告Reload成功
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdReLoaded(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告展示
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdShow(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告展示
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdImp(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra);

    /**
     * 广告点击
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdClick(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra, String impressionId);

    /**
     * 广告点击
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdReward(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告加载错误
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdError(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

    /**
     * 广告关闭
     *
     * @param context
     * @param placeName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdClose(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);
}
