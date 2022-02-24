package com.rabbit.adsdk.listener;

/**
 * 广告加载过滤器，返回true表示不加载此广告，返回false表示可以加载广告
 */
public interface OnAdDisableLoadingListener {
    boolean onAdDisableLoading(String placeName, String sdk, String type);
}