package com.rabbit.adsdk.stat;

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
    public void reportAdLoaded(Context context, String placeName, String sdk, String type, String pid, double ecpm, Map<String, Object> extra);

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
    public void reportAdClick(Context context, String placeName, String sdk, String network, String type, String pid, String networkPid, double ecpm, Map<String, Object> extra);

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

    /**
     * 广告加载成功时间
     *
     * @param context
     * @param value
     */
    public void reportAdLoadSuccessTime(Context context, String placeName, String sdk, String type, int value);

    /**
     * 广告加载成功时间
     *
     * @param context
     * @param value
     */
    public void reportAdLoadFailureTime(Context context, String placeName, String sdk, String type, String error, int value);

    /**
     * 上报FSA结束事件
     *
     * @param context
     * @param key
     * @param value
     */
    public void reportKVEvent(Context context, String key, String value, Map<String, Object> extra);

    /**
     * 顺序请求
     *
     * @param context
     * @param placeName
     */
    public void reportAdPlaceSeqRequest(Context context, String placeName);

    /**
     * 顺序已加载
     *
     * @param context
     * @param placeName
     */
    public void reportAdPlaceSeqLoaded(Context context, String placeName);

    /**
     * 顺序失败
     *
     * @param context
     * @param placeName
     */
    public void reportAdPlaceSeqError(Context context, String placeName);
}
