package com.hauyu.adsdk.stat;

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
    public void reportAdCallShow(Context context, String pidName, String sdk, String type, String pid, String ecpm, Map<String, String> extra);

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
     * GT广告请求
     * @param context
     */
    public void reportAdOuterRequest(Context context, String adOuterType, String pidName);

    /**
     * GT广告加载成功
     * @param context
     */
    public void reportAdOuterLoaded(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示
     * @param context
     */
    public void reportAdOuterCallShow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示成功
     * @param context
     */
    public void reportAdOuterShowing(Context context, String adOuterType, String pidName);

    /**
     * GT不允许展示
     * @param context
     */
    public void reportAdOuterDisallow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示次数
     * @param context
     * @param times
     */
    public void reportAdOuterShowTimes(Context context, String adOuterType, int times);

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

    /**
     * 上报FSA结束事件
     * @param context
     * @param key
     * @param value
     */
    public void reportFinishFSA(Context context, String key, String value);

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
