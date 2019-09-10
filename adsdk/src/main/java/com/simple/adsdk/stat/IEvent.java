package com.simple.adsdk.stat;

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
    public void reportAdShow(Context context, String pidName, String sdk, String type, Map<String, String> extra);

    /**
     * 广告展示
     * @param context
     * @param pidName
     * @param sdk
     * @param type
     * @param extra
     */
    public void reportAdImp(Context context, String pidName, String sdk, String type, Map<String, String> extra);

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
     * 设置开关
     * @param umeng
     * @param facebook
     * @param appsflyer
     * @param firebase
     * @param reportError
     */
    public void setReportOption(boolean umeng, boolean facebook, boolean appsflyer, boolean firebase, boolean reportError);
}
