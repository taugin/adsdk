package com.hauyu.adsdk.stat;

import android.content.Context;

import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IEvent {
    /**
     * 广告请求
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdRequest(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告请求成功
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdLoaded(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告展示
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdShow(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告展示
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdImp(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告点击
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdClick(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告点击
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdReward(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告加载错误
     * @param context
     * @param error
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdError(Context context, String error, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

    /**
     * 广告关闭
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdClose(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

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
     * 上报FSA结束事件
     * @param context
     * @param key
     * @param value
     */
    public void reportKVEvent(Context context, String key, String value, Map<String, String> extra);

    /**
     * 顺序请求
     * @param context
     * @param pidName
     */
    public void reportAdPlaceSeqRequest(Context context, String pidName);

    /**
     * 顺序已加载
     * @param context
     * @param pidName
     */
    public void reportAdPlaceSeqLoaded(Context context, String pidName);

    /**
     * 顺序失败
     * @param context
     * @param pidName
     */
    public void reportAdPlaceSeqError(Context context, String pidName);
}
